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

import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import com.google.common.base.Function;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.*;

import static com.avaje.ebean.Expr.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static models.PlayVersion.MajorVersion;
import static models.PlayVersion.findVersionsByMajorVersion;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class ModuleVersion extends AbstractModel {

    @ManyToOne
    public Module playModule;

    // this will be taken from Build.scala
    @Column(nullable = false)
    @Constraints.Required
    public String versionCode;

    @Column(nullable = false)
    @Constraints.Required
    @Constraints.MaxLength(255)
    public String releaseNotes;

    @Column(nullable = false)
    public Date releaseDate;

    @ManyToMany
    public List<PlayVersion> compatibility;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public BinaryContent binaryFile;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public BinaryContent sourceFile;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public BinaryContent documentFile;

    public static final Finder<Long, ModuleVersion> FIND = new Finder<Long, ModuleVersion>(Long.class, ModuleVersion.class);

    public static int count() {
        return FIND.findRowCount();
    }

    public Set<PlayVersion.MajorVersion> getMajorVersions() {
        return newHashSet(transform(compatibility, toMajorVersions()));
    }

    public static List<ModuleVersion> findByModule(Module module) {
        return FIND.where()
                .eq("playModule", module)
                .order("versionCode ASC")
                .findList();
    }

    public static List<Module> findModulesByMajorVersion(MajorVersion majorVersion){
        return findModulesByPlayVersion(findVersionsByMajorVersion(majorVersion));
    }

    public static List<Module> findModulesByPlayVersion(List<PlayVersion> playVersions) {
        List<ModuleVersion> moduleVersions = FIND
                .where()
                .in("compatibility", playVersions)
                .query()
                .fetch("playModule")
                .fetch("playModule.owner")
                .findList();
        return newArrayList(newHashSet(transform(moduleVersions, toModules())));
    }

    private static Function<ModuleVersion, Module> toModules() {
        return new Function<ModuleVersion, Module>() {
            public Module apply(ModuleVersion moduleVersion) {
                return moduleVersion.playModule;
            }
        };
    }

    private Function<PlayVersion, PlayVersion.MajorVersion> toMajorVersions() {
        return new Function<PlayVersion, MajorVersion>() {
            public MajorVersion apply(PlayVersion playVersion) {
                return playVersion.majorVersion;
            }
        };
    }


}
