package services;

import controllers.routes;
import models.Module;
import models.memory.Sitemap;
import play.api.mvc.RequestHeader;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Pere Villega (pere.villega@gmail.com)
 */
public class SitemapServices {

    public static final String SITEMAP_CACHE_KEY = "siteMap";

    /**
     * Generates the sitemaps entries for the application.
     * Unfortunately this has to be generated "by hand", if we want to display more entries we have to add them to the list ourselves
     *
     * @param request the current request
     * @return a list of sitemap entries
     */
    public static List<Sitemap> generateSitemap(Http.Request request){
        List<Sitemap> list = new ArrayList<Sitemap>();

        // home
        list.add(new Sitemap(routes.Application.index().absoluteURL(request)));

        // users
        list.add(new Sitemap(routes.Application.listUsers().absoluteURL(request), "daily", "0.8" ));

        // modules lists
        list.add(new Sitemap(routes.Modules.getModulesByPlayVersion("1").absoluteURL(request), "daily", "0.8" ));
        list.add(new Sitemap(routes.Modules.getModulesByPlayVersion("2").absoluteURL(request), "daily", "0.8" ));

        // latest modules
        list.add(new Sitemap(routes.Modules.getLatestModulesByPlayVersion("1").absoluteURL(request), "daily", "0.8" ));
        list.add(new Sitemap(routes.Modules.getLatestModulesByPlayVersion("2").absoluteURL(request), "daily", "0.8" ));

        // highest rated modules
        list.add(new Sitemap(routes.Modules.getHighestRatedModulesByPlayVersion("1").absoluteURL(request), "daily", "0.8" ));
        list.add(new Sitemap(routes.Modules.getHighestRatedModulesByPlayVersion("2").absoluteURL(request), "daily", "0.8" ));

        // modules details
        List<Module> modules = Module.all();
        for(Module mod: modules) {
            list.add(new Sitemap(routes.Modules.details(mod.key).absoluteURL(request), "daily", "1" ));
        }

        return list;
    }
}
