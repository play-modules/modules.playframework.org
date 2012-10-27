package actors;

import akka.actor.UntypedActor;
import com.google.common.collect.Lists;
import models.FeaturedModule;
import models.Module;
import models.ModuleVersion;
import models.PlayVersion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static controllers.AbstractController.createHistoricalEvent;

/**
 * Selects the Featured Modules for the day
 * @author Pere Villega (pere.villega@gmail.com)
 */
public class FeaturedModulesSelectionActor extends UntypedActor
{
    private static final int MAX_FEATURED = 5;

    /**
     * When receiving the message it recalculates the featured modules for the day
     * The previous day's featured modules are removed, if they are not sticky
     * The description of the featured module will be taken from the module summary, but may be overwritten from the admin console to something specific
     * Featured modules created in this way are not sticky.
     * Featured modules can be made sticky to have them stick around for more than 24 hours via admin console - featured modules can also be created from there
     * @param message received message (ignored)
     */
    @Override
    public void onReceive(Object message)
    {

        // Remove non-sticky modules
        List<FeaturedModule> ephemeral = FeaturedModule.getEphemeralModules();
        for(FeaturedModule fm : ephemeral)
        {
            fm.delete();
        }

        // Select new Featured modules - we select some for each version in addition to any sticky featured modules we may already have
        for(PlayVersion.MajorVersion mv : PlayVersion.MajorVersion.values())
        {
            // select 5 random modules
            Random rnd = new Random();
            List<Module> allModules = ModuleVersion.findModulesByMajorVersion(mv);

            List<Module> newFeatured = new ArrayList<Module>();
            while(newFeatured.size() < MAX_FEATURED && newFeatured.size() < allModules.size()){
                int pos = rnd.nextInt(allModules.size());
                newFeatured.add(allModules.get(pos));
            }

            for(Module m : newFeatured)
            {

                FeaturedModule fm = new FeaturedModule();
                fm.sticky = false;
                fm.playModule = m;
                fm.description = m.description;
                fm.creationDate = new Date();
                fm.save();

                createHistoricalEvent("Selected new Featured Module - " + m.name,
                        String.format("Module %s by %s added as a new ephemeral featured module",
                                m.name,
                                m.owner));
            }
        }

    }
}
