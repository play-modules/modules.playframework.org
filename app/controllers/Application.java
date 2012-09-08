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
import models.memory.Sitemap;
import play.mvc.Result;
import play.mvc.With;
import services.SitemapServices;
import views.html.index;
import views.html.sitemap;

import java.util.Collections;
import java.util.List;

import static actions.CurrentUser.currentUser;
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
        final List<Module> highestRatedModules = Collections.emptyList(); // best way to use the rating algorithm for the db call?  pre-calculate before storing?
        final List<FeaturedModule> featuredModules = FeaturedModule.getAll();
        final List<PlayVersion> playVersions = PlayVersion.getAll();

        return ok(index.render(currentUser(),
                               mostRecentModules1,
                               mostRecentModules2,
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

    public static Result createAccount()
    {
        return redirect(routes.Application.index());
    }
}
