/*
 * Copyright 2012 The Play! Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import actions.CurrentUser;
import be.objectify.deadbolt.actions.RoleHolderPresent;
import com.avaje.ebean.Ebean;
import forms.modules.RatingForm;
import forms.modules.RatingResponseForm;
import forms.modules.VoteResponseForm;
import models.*;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.cache.Cache;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.SitemapServices;
import utils.CollectionUtils;
import utils.Filter;
import utils.RequestUtils;
import views.html.modules.genericModuleList;
import views.html.modules.manageVersionsForm;
import views.html.modules.moduleDetails;
import views.html.modules.moduleRegistrationForm;
import views.html.modules.moduleEditForm;
import views.html.modules.myModules;

import java.util.*;

import static actions.CurrentUser.currentUser;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@With(CurrentUser.class)
public class Modules extends AbstractController
{
    @RoleHolderPresent
    public static Result myModules()
    {
        User currentUser = currentUser();
        return ok(myModules.render(currentUser,
                                   Module.ownedBy(currentUser)));
    }

    @RoleHolderPresent
    public static Result showModuleRegistrationForm()
    {
        return ok(moduleRegistrationForm.render(currentUser(),
                                                form(Module.class)));
    }

    @RoleHolderPresent
    public static Result submitModuleRegistrationForm()
    {
        Form<Module> form = form(Module.class).bindFromRequest();
        Result result;
        User user = currentUser();
        if (form.hasErrors())
        {
            result = badRequest(moduleRegistrationForm.render(user,
                                                              form));
        }
        else
        {
            Module module = form.get();
            module.owner = user;
            module.rating = new Rating(true);
            module.save();

            createHistoricalEvent("New module - " + module.name,
                                  String.format("%s (%s) created a new module - %s",
                                                user.displayName,
                                                user.userName,
                                                module.name),
                                  module.key);

            // We clean the cached result for sitemaps as we have a new entry
            Cache.set(SitemapServices.SITEMAP_CACHE_KEY, null, 0);

            result = redirect(routes.Modules.myModules());
        }

        return result;
    }

    @RoleHolderPresent
    public static Result showModuleEditForm(String moduleKey)
    {
        Module module = Module.findByModuleKey(moduleKey);
        Form<Module> form = form(Module.class).fill(module);
        return ok(moduleEditForm.render(currentUser(), moduleKey, form));
    }

    @RoleHolderPresent
    public static Result submitModuleEditForm(String moduleKey)
    {
        Form<Module> form = form(Module.class).bindFromRequest();
        Result result;
        User user = currentUser();
        if (form.hasErrors())
        {
            result = badRequest(moduleEditForm.render(user, moduleKey, form));
        }
        else
        {
            Module module = form.get();
            Module original = Module.findByModuleKey(moduleKey);
            original.name = module.name;
            original.organisation = module.organisation;
            original.summary = module.summary;
            original.description = module.description;
            original.category = module.category;
            original.projectUrl = module.projectUrl;
            original.licenseType = module.licenseType;
            original.save();

            createHistoricalEvent("Updated module - " + module.name,
                    String.format("%s (%s) updated module - %s",
                            user.displayName,
                            user.userName,
                            module.name),
                    module.key);

            // We clean the cached result for sitemaps as we have a new entry
            Cache.set(SitemapServices.SITEMAP_CACHE_KEY, null, 0);

            result = redirect(routes.Modules.myModules());
        }

        return result;
    }

    @RoleHolderPresent
    public static Result showVersionManagement(String moduleKey)
    {
        Form<ModuleVersion> form = form(ModuleVersion.class);
        Module module = Module.findByModuleKey(moduleKey);
        return ok(manageVersionsForm.render(currentUser(),
                                            module,
                                            PlayVersion.getAll(),
                                            form));
    }

    @RoleHolderPresent
    public static Result uploadNewVersion(String moduleKey)
    {
        Result result;
        Form<ModuleVersion> form = form(ModuleVersion.class).bindFromRequest();
        User user = currentUser();
        if (form.hasErrors())
        {
            result = badRequest(manageVersionsForm.render(user,
                                                          Module.findByModuleKey(moduleKey),
                                                          PlayVersion.getAll(),
                                                          form));
        }
        else
        {
            ModuleVersion moduleVersion = form.get();
            moduleVersion.playModule = Module.findByModuleKey(moduleKey);
            moduleVersion.releaseDate = new Date();

            moduleVersion.compatibility.addAll(RequestUtils.getListFromRequest(request(),
                                                                               "compatibility.id",
                                                                               PlayVersion.FIND));

            // everything below here needs to be implemented
            moduleVersion.binaryFile = new BinaryContent();
            moduleVersion.binaryFile.content = new byte[]{1};
            moduleVersion.binaryFile.contentLength = 1;
            moduleVersion.save();
            moduleVersion.saveManyToManyAssociations("compatibility");

            createHistoricalEvent("Module updated - " + moduleVersion.playModule.name,
                                  String.format("%s (%s) uploaded version %s of %s",
                                                user.displayName,
                                                user.userName,
                                                moduleVersion.versionCode,
                                                moduleVersion.playModule.name),
                                  moduleVersion.playModule.key);

            result = redirect(routes.Modules.showVersionManagement(moduleKey));
        }
        return result;
    }

    /**
     * Deletes the given version from the chosen module
     * Checks that the current user owns the module and that the version id belongs to that module.
     * If the user can't delete the module or the data is wrong it fails silently, to avoid users trying to exploit it
     * @return ok()
     */
    @RoleHolderPresent
    public static Result deleteVersion()
    {
        Result result = ok();
        // get params from body
        Map<String,String[]> postParams = request().body().asFormUrlEncoded();
        String[] ids = postParams.get("id");
        String[] modKeys = postParams.get("moduleKey");

        // check we have params to avoid array out of bounds/etc
        if(ids != null && ids.length > 0 && modKeys != null && modKeys.length > 0) {
            Long id = Long.valueOf(ids[0]);
            String moduleKey = modKeys[0];
            User user = currentUser();
            Module playModule = Module.findByModuleKey(moduleKey);
            Logger.debug("Received "+playModule);
            // check that we own the module
            if(playModule.owner.equals(user))
            {
                //search for the given id in the versions linked to the module, to validate input
                ModuleVersion version = ModuleVersion.FIND.byId(id);

                // only remove if the id belongs to the module the user owns
                if(version != null && version.playModule.equals(playModule))
                {
                    // we need to remove m-m rel before deleting. Clear doesn't work, not sure why
                    List<PlayVersion> all = new ArrayList(version.compatibility);
                    version.compatibility.removeAll(all);
                    version.save();
                    version.saveManyToManyAssociations("compatibility");

                    version.delete();

                    createHistoricalEvent("Module updated - " + version.playModule.name,
                            String.format("%s (%s) removed version %s of %s",
                                    user.displayName,
                                    user.userName,
                                    version.versionCode,
                                    version.playModule.name),
                            version.playModule.key);

                    // return id to hide row
                    ObjectNode node = Json.newObject();
                    node.put("id", id);
                    result = ok(node);
                }
            }
        }

        return result;
    }

    public static Result getModules(String ofType)
    {
        return TODO;
    }

    // e.g. /modules/play-1.2.4
    public static Result getModulesByPlayVersion(String version)
    {
        List<PlayVersion> playVersionList = PlayVersion.findByLooseName(version);
        Result result;
        if (playVersionList.isEmpty()){
            result = notFound("Play version not found: " + version);
        }                                                                        
        else 
        {
            User currentUser = currentUser();
            String title = String.format("Play %s.x modules", version);
            List<Module> modules = ModuleVersion.findModulesByPlayVersion(playVersionList);
            result = ok(genericModuleList.render(currentUser, title, modules));
        }
        return result; 
    }

    //e.g.: /modules/play-1/latest
    public static Result getLatestModulesByPlayVersion(String version) {
        List<PlayVersion> playVersionList = PlayVersion.findByLooseName(version);
        Result result;
        if (playVersionList.isEmpty()){
            result = notFound("Play version not found: " + version);
        }
        else
        {
            User currentUser = currentUser();
            String title = String.format("Latest Play %s.x modules", version);
            List<Module> modules = Module.findMostRecentModules(playVersionList);
            result = ok(genericModuleList.render(currentUser, title, modules));
        }
        return result;
    }

    //e.g.: /modules/play-1/rating
    public static Result getHighestRatedModulesByPlayVersion(String version) {
        List<PlayVersion> playVersionList = PlayVersion.findByLooseName(version);
        Result result;
        if (playVersionList.isEmpty()){
            result = notFound("Play version not found: " + version);
        }
        else
        {
            User currentUser = currentUser();
            String title = String.format("Highest Rated Play %s.x modules", version);
            List<Module> modules = Module.findHighestRatedModules(playVersionList);
            result = ok(genericModuleList.render(currentUser, title, modules));
        }
        return result;
    }

    //e.g: /modules/play-1.2/featured
    public static Result getFeaturedModulesByPlayVersion(String version) {
        List<PlayVersion> playVersionList = PlayVersion.findByLooseName(version);
        Result result;
        if (playVersionList.isEmpty()){
            result = notFound("Play version not found: " + version);
        }
        else
        {
            User currentUser = currentUser();
            String title = String.format("Featured Play %s.x modules", version);
            List<Module> modules = FeaturedModule.findFeaturedModulesByVersion(playVersionList);
            result = ok(genericModuleList.render(currentUser, title, modules));
        }
        return result;
    }

    public static Result getModulesByCategory(String version,
                                              String category)
    {
        return TODO;
    }

    public static Result details(String moduleKey)
    {
        Result result;
        final Module module = Module.findByModuleKey(moduleKey);
        if (module == null)
        {
            result = notFound("Module not found: " + moduleKey);
        }
        else
        {
            List<ModuleVersion> moduleVersions = ModuleVersion.findByModule(module);
            User user = currentUser();
            Rate rate = null;
            Vote vote = null;

            if (user != null)
            {
                rate = CollectionUtils.filterFirst(user.rates,
                                                   new Filter<Rate>()
                                                   {
                                                       public boolean isAcceptable(Rate rate)
                                                       {
                                                           return rate.playModule.id.equals(module.id);
                                                       }
                                                   });
                vote = CollectionUtils.filterFirst(user.votes,
                                                   new Filter<Vote>()
                                                   {
                                                       public boolean isAcceptable(Vote vote)
                                                       {
                                                           return vote.playModule.id.equals(module.id);
                                                       }
                                                   });
            }

            Set<PlayVersion.MajorVersion> majorVersions = new HashSet<PlayVersion.MajorVersion>();

            for (ModuleVersion moduleVersion : moduleVersions)
            {
                majorVersions.addAll(moduleVersion.getMajorVersions());
            }

            Ebean.refresh(module.rating);

            result = ok(moduleDetails.render(user,
                                             module,
                                             moduleVersions,
                                             majorVersions,
                                             rate,
                                             vote));
        }
        return result;
    }

    @RoleHolderPresent
    public static Result voteUp(String moduleKey)
    {
        Result result;
        final Module module = Module.findByModuleKey(moduleKey);
        if (module == null)
        {
            result = notFound("Module not found: " + moduleKey);
        }
        else
        {
            User user = currentUser();
            Vote vote = CollectionUtils.filterFirst(user.votes,
                                                    new Filter<Vote>()
                                                    {
                                                        @Override
                                                        public boolean isAcceptable(Vote vote)
                                                        {
                                                            return vote.playModule.id.equals(module.id);
                                                        }
                                                    });
            if (vote != null)
            {
                if (vote.voteType == Vote.VoteType.DOWN)
                {
                    --module.downVoteCount;
                    ++module.upVoteCount;
                    vote.voteType = Vote.VoteType.UP;
                    vote.lastChangeDate = new Date();
                    vote.save();
                    module.save();
                }
                else
                {
                    Logger.info(String.format("User [%s] tried to upvote module [%s] but vote already existed",
                                              user.userName,
                                              module.key));
                }
            }
            else
            {
                vote = new Vote();
                vote.playModule = module;
                vote.voteType = Vote.VoteType.UP;
                vote.lastChangeDate = new Date();
                user.votes.add(vote);
                user.save();

                ++module.upVoteCount;
                module.save();
            }

            VoteResponseForm responseForm = new VoteResponseForm();
            responseForm.upVotes = module.upVoteCount;
            responseForm.downVotes = module.downVoteCount;
            result = ok(Json.toJson(responseForm));
        }
        return result;
    }

    @RoleHolderPresent
    public static Result voteDown(String moduleKey)
    {
        Result result;
        final Module module = Module.findByModuleKey(moduleKey);
        if (module == null)
        {
            result = notFound("Module not found: " + moduleKey);
        }
        else
        {
            User user = currentUser();
            Vote vote = CollectionUtils.filterFirst(user.votes,
                                                    new Filter<Vote>()
                                                    {
                                                        @Override
                                                        public boolean isAcceptable(Vote vote)
                                                        {
                                                            return vote.playModule.id.equals(module.id);
                                                        }
                                                    });
            if (vote != null)
            {
                if (vote.voteType == Vote.VoteType.UP)
                {
                    --module.upVoteCount;
                    ++module.downVoteCount;
                    vote.voteType = Vote.VoteType.DOWN;
                    vote.save();
                    module.save();
                }
                else
                {
                    Logger.info(String.format("User [%s] tried to downvote module [%s] but vote already existed",
                                              user.userName,
                                              module.key));
                }
            }
            else
            {
                vote = new Vote();
                vote.playModule = module;
                vote.voteType = Vote.VoteType.DOWN;
                vote.lastChangeDate = new Date();
                user.votes.add(vote);
                user.save();

                ++module.downVoteCount;
                module.save();
            }

            VoteResponseForm responseForm = new VoteResponseForm();
            responseForm.upVotes = module.upVoteCount;
            responseForm.downVotes = module.downVoteCount;
            result = ok(Json.toJson(responseForm));
        }
        return result;
    }

    // If a user has already rated, then change the rate, don't add a new one
    @RoleHolderPresent
    public static Result rate(String moduleKey)
    {
        Result result;
        Form<RatingForm> form = form(RatingForm.class).bindFromRequest();

        if (form.hasErrors())
        {
            result = badRequest(form.errorsAsJson());
        }
        else
        {
            RatingForm ratingForm = form.get();
            final Module module = Module.findById(ratingForm.id);

            if (module == null)
            {
                result = badRequest("Module does not exist");
            }
            else
            {
                User user = currentUser();
                Rate rate = null;

                if (user != null)
                {
                    // user shouldn't be null because of @RoleHolderPresent
                    rate = CollectionUtils.filterFirst(user.rates,
                                                       new Filter<Rate>()
                                                       {
                                                           @Override
                                                           public boolean isAcceptable(Rate rate)
                                                           {
                                                               return rate.playModule.id.equals(module.id);
                                                           }
                                                       });
                    if (rate != null)
                    {
                        module.rating.subtract(rate);
                    }
                    else
                    {
                        rate = new Rate();
                        rate.playModule = module;
                        user.rates.add(rate);
                    }
                    rate.rating = ratingForm.rating;
                    rate.lastChangeDate = new Date();
                    module.rating.add(rate);
                    module.rating.calculateAverage();

                    module.save();
                    user.save();
                }

                RatingResponseForm responseForm = new RatingResponseForm();
                responseForm.totalRatings = module.rating.totalRatings();
                responseForm.averageRating = module.rating.averageRating;
                result = ok(Json.toJson(responseForm));
            }
        }

        return result;
    }
}
