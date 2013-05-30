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
package models;

import com.avaje.ebean.Query;
import com.google.common.base.Function;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static models.PlayVersion.findByLooseName;
import static models.PlayVersion.findVersionsByMajorVersion;

/**
 * Represent a module listed in the "Featured Modules" list
 * Non sticky featured modules will be removed by the FeaturedModulesSelectionActor after 24h
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class FeaturedModule extends AbstractModel implements ModuleAccessor
{

    @OneToOne(optional = false)
    public Module playModule;

    @Column(nullable = true, length = 1000)
    public String description;  //TODO: not used when displaying modules, really needed?

    @Column(nullable = false)
    public Date creationDate;

    @Column(nullable = false)
    public Boolean sticky;

    public static final Finder<Long, FeaturedModule> FIND = new Finder<Long, FeaturedModule>(Long.class,
                                                                                             FeaturedModule.class);

    public static List<FeaturedModule> getAll()
    {
        return FIND.all();
    }

    public static List<FeaturedModule> getStickyModules()
    {
        return FIND.where()
                   .eq("sticky", Boolean.TRUE)
                   .findList();
    }

    public static List<FeaturedModule> getEphemeralModules()
    {
        return FIND.where()
                   .eq("sticky", Boolean.FALSE)
                   .findList();
    }

    public static List<Module> findFeaturedModulesByVersion(int count, PlayVersion.MajorVersion majorVersion) {
        List<Module> modules = findFeaturedModulesByVersion(findVersionsByMajorVersion(majorVersion));
        return modules.size() <= count ? modules : modules.subList(0, count);
    }

    public static List<Module> findFeaturedModulesByVersion(List<PlayVersion> versions) {
        List<FeaturedModule> featuredModules = FIND
                .where()
                .in("playModule.versions.compatibility", versions)
                .query()
                .fetch("playModule")
                .fetch("playModule.owner")
                .findList();
        return newArrayList(newHashSet(transform(featuredModules, toModules())));
    }

    private static Function<FeaturedModule, Module> toModules() {
        return new Function<FeaturedModule, Module>() {
            public Module apply(FeaturedModule featuredModule) {
                return featuredModule.playModule;
            }
        };
    }

    @Override
    public Module getModule()
    {
        return playModule;
    }
}
