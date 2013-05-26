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

import actors.FeaturedModulesSelectionActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Duration;
import com.avaje.ebean.Ebean;
import models.BinaryContent;
import models.Module;
import models.ModuleVersion;
import models.PlayVersion;
import models.Rate;
import models.Rating;
import models.User;
import models.UserRole;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import play.libs.Yaml;
import security.RoleDefinitions;
import utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class Global extends GlobalSettings
{
    @Override
    public void onStart(Application application)
    {
        // Add code or TODOs here for startup behaviour
        // I'll add this to the wiki later
        // this space for rent

        if (UserRole.findByRoleName(RoleDefinitions.ADMIN) == null)
        {
            UserRole role = new UserRole();
            role.roleName = RoleDefinitions.ADMIN;
            role.description = "MPO administrator";
            role.save();
        }
        if (UserRole.findByRoleName(RoleDefinitions.USER) == null)
        {
            UserRole role = new UserRole();
            role.roleName = RoleDefinitions.USER;
            role.description = "MPO user";
            role.save();
        }

        loadInitialData();

        scheduleJobs();
    }

    public void scheduleJobs()
    {
        ActorSystem actorSystem = Akka.system();

        // Select featured modules daily
        ActorRef featuredModulesActor = actorSystem.actorOf(new Props(FeaturedModulesSelectionActor.class));
        actorSystem.scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS),
                Duration.create(24, TimeUnit.HOURS),
                featuredModulesActor,
                "select");
    }

    /**
     * Simplistic loading of YAML initial data file.
     */
    @SuppressWarnings("unchecked")
    public void loadInitialData()
    {
        Logger.info("Loading initial data...");
        Map<String, List<Object>> data = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
        savePlayVersions(data);
        saveUsers(data);
        saveModules(data);
        saveModuleVersions(data);
    }

    private void saveModuleVersions(Map<String, List<Object>> data) {
        if (ModuleVersion.count() == 0)
        {
            final List<ModuleVersion> versions = CollectionUtils.castTo(data.get("versions"),
                    ModuleVersion.class);
            Logger.debug(String.format("ModuleVersion: %d loaded", versions.size()));
            for (ModuleVersion version : versions) {
                BinaryContent binaryContent = new BinaryContent();
                binaryContent.content = new byte[0];
                binaryContent.contentLength = 0;
                version.binaryFile = binaryContent;
            }
            Ebean.save(versions);
            for (Object version : versions){
                Ebean.saveManyToManyAssociations(version, "compatibility");
            }
        }
    }

    private void saveModules(Map<String, List<Object>> data) {
        if (Module.count() == 0)
        {
            final List<Module> modules = CollectionUtils.castTo(data.get("modules"),
                    Module.class);
            Logger.debug(String.format("Module: %d loaded", modules.size()));

            // programatically add some attributes
            for (Module module : modules)
            {
                module.rating = new Rating(true);

                if (module.organisation == null ||
                    "(unknown)".equals(module.organisation))
                {
                    module.organisation = module.key;
                }
            }

            Ebean.save(modules);
        }
    }

    private void saveUsers(Map<String, List<Object>> data) {
        // The 'admin' user is already loaded.
        if (User.count() <= 1)
        {
            final List<User> users = CollectionUtils.castTo(data.get("users"),
                    User.class);
            // programatically add some attributes
            for (User user : users)
            {
                user.rates = new ArrayList<Rate>();
            }
            Logger.debug(String.format("User: %d loaded", users.size()));
            Ebean.save(users);
        }
    }

    private void savePlayVersions(Map<String, List<Object>> data) {
        if (PlayVersion.count() == 0)
        {
            final List<Object> versions = data.get("playVersions");
            Logger.debug(String.format("PlayVersion: %d loaded", versions.size()));
            Ebean.save(versions);
        }
    }

}
