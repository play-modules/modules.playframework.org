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
import models.User;
import models.memory.Sitemap;
import models.ss.ExternalAccount;
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
import java.util.List;
import java.util.Map;

import static actions.CurrentUser.currentUser;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@With(CurrentUser.class)
public class Application extends AbstractController
{
    public static Result index()
    {
        final List<Module> mostRecentModules = Module.findMostRecent(5);
        final List<Module> highestRatedModules = Collections.emptyList(); // best way to use the rating algorithm for the db call?  pre-calculate before storing?
        final List<FeaturedModule> featuredModules = FeaturedModule.getAll();
        final List<PlayVersion> playVersions = PlayVersion.getAll();

        return ok(index.render(currentUser(),
                               mostRecentModules,
                               highestRatedModules,
                               featuredModules,
                               playVersions,
                               Module.count()));
    }

    /**
     * Returns the sitemap of the application
     */
    public static Result sitemap()
    {
        // todo - steve - 04/09 - do we need to repeatedly generate this?  Couldn't we have it generated once
        // a day for catch-all updates, and every time a new module is added?
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
        User currentUser = currentUser();
        Result result;
        if (currentUser != null && currentUser.userName.equals(userName))
        {
            // todo - steve 07/09/2012 - this is pretty clumsy
            List<ExternalAccount> externalAccounts = ExternalAccount.findByUser(currentUser);
            List<String> existingAccounts = new ArrayList<String>(externalAccounts.size());
            for (ExternalAccount externalAccount : externalAccounts)
            {
                existingAccounts.add(externalAccount.provider);
            }

            Map<String,IdentityProvider> allProviders = JavaConversions.asMap(ProviderRegistry.all());
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
                                         otherProviders));
        }
        else
        {
            User user = User.getByUserName(userName);
            if (user == null)
            {
                result = ok(noAccount.render(currentUser));
            }
            else
            {
                List<Module> modules = Module.ownedBy(user);
                result = ok(account.render(currentUser,
                                           user,
                                           modules));
            }
        }
        return result;
    }
}
