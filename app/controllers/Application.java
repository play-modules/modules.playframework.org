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
import models.FeaturedModule;
import models.Module;
import models.PlayVersion;
import models.SocialActivity;
import models.User;
import models.memory.Sitemap;
import models.ss.ExternalAccount;
import play.Logger;
import play.cache.Cached;
import play.mvc.Result;
import play.mvc.With;
import scala.collection.JavaConversions;
import securesocial.core.IdentityProvider;
import securesocial.core.ProviderRegistry;
import services.SitemapServices;
import views.html.index;
import views.html.user.account;
import views.html.user.listUsers;
import views.html.user.myAccount;
import views.html.user.noAccount;
import views.html.sitemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static actions.CurrentUser.currentUser;
import static models.Module.findHighestRatedModules;
import static models.Module.findMostRecentModules;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@With(CurrentUser.class)
public class Application extends AbstractController
{
    public static Result index()
    {
        final List<Module> mostRecentModules1 = findMostRecentModules(5, PlayVersion.MajorVersion.ONE);
        final List<Module> mostRecentModules2 = findMostRecentModules(5, PlayVersion.MajorVersion.TWO);
        final List<Module> highestRatedModules1 = findHighestRatedModules(5, PlayVersion.MajorVersion.ONE);
        final List<Module> highestRatedModules2 = findHighestRatedModules(5, PlayVersion.MajorVersion.TWO);
        final List<FeaturedModule> featuredModules = FeaturedModule.getAll();
        final List<PlayVersion> playVersions = PlayVersion.getAll();

        return ok(index.render(currentUser(),
                               mostRecentModules1,
                               mostRecentModules2,
                               highestRatedModules1,
                               highestRatedModules2,
                               featuredModules,
                               playVersions,
                               Module.count()));
    }

    /**
     * Returns the sitemap of the application. This shouldn't be called too often and only by crawlers.
     *
     * Result is cached for 1 day (so we only regenerate the list every 1 day on request)
     * Cache entry is wiped when a new module is added so it is included on next request
     */
    @Cached(key=SitemapServices.SITEMAP_CACHE_KEY, duration=24*60*60)
    public static Result sitemap()
    {
        List<Sitemap> list = SitemapServices.generateSitemap(request());
        return ok(sitemap.render(list)).as("application/xml");
    }

    public static Result listUsers()
    {
        return ok(listUsers.render(currentUser(),
                                   User.all()));
    }

    public static Result viewAccount(String userName)
    {
        Result result;
        User currentUser = currentUser();
        User user = User.getByUserName(userName);
        if (user == null)
        {
            result = ok(noAccount.render(currentUser));
        }
        else
        {
            if (currentUser != null && currentUser.userName.equals(userName))
            {
                // todo - steve 07/09/2012 - this is pretty clumsy
                List<ExternalAccount> externalAccounts = ExternalAccount.findByUser(currentUser);
                List<String> existingAccounts = new ArrayList<String>(externalAccounts.size());
                for (ExternalAccount externalAccount : externalAccounts)
                {
                    existingAccounts.add(externalAccount.provider);
                }

                Map<String, IdentityProvider> allProviders = JavaConversions.asMap(ProviderRegistry.all());
                List<IdentityProvider> otherProviders = new ArrayList<IdentityProvider>();
                if (externalAccounts.size() < allProviders.size())
                {
                    for (Map.Entry<String, IdentityProvider> entry : allProviders.entrySet())
                    {
                        if (!existingAccounts.contains(entry.getKey()))
                        {
                            otherProviders.add(entry.getValue());
                        }
                    }
                }
                result = ok(myAccount.render(currentUser,
                                             externalAccounts,
                                             otherProviders,
                                             Module.ownedBy(currentUser),
                                             getSocialActivities(currentUser, 10)));
            }
            else
            {
                result = ok(account.render(currentUser,
                                           user,
                                           Module.ownedBy(user),
                                           getSocialActivities(user, 10)));
            }
        }

        return result;
    }

    private static List<SocialActivity> getSocialActivities(User user,
                                                            int limitTo)
    {
        List<SocialActivity> socialActivities = new ArrayList<SocialActivity>();
        socialActivities.addAll(user.rates);
        socialActivities.addAll(user.votes);
        Collections.sort(socialActivities,
                         new Comparator<SocialActivity>()
                         {
                             @Override
                             public int compare(SocialActivity a,
                                                SocialActivity b)
                             {
                                 return a.getDate().after(b.getDate()) ? -1 : 1;
                             }
                         });
        return socialActivities.size() > limitTo ? socialActivities.subList(0, limitTo) : socialActivities;
    }
}
