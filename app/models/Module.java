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

import com.avaje.ebean.Ebean;
import com.petebevin.markdown.MarkdownProcessor;
import play.data.validation.Constraints;
import utils.CollectionUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.avaje.ebean.Ebean.sort;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.LAZY;
import static models.ModuleVersion.findModulesByMajorVersion;
import static models.ModuleVersion.findModulesByPlayVersion;
import static models.PlayVersion.MajorVersion;
import static play.data.validation.Constraints.MaxLength;
import static play.data.validation.Constraints.Required;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
@Table(name = "MPO_MODULE")
public class Module extends AbstractModel implements Comparable<Module>, ModuleAccessor {
    @ManyToOne(optional = false)
    public User owner;

    @Column(name = "module_key", nullable = false, unique = true)
    @Required
    public String key;

    @Column(nullable = false)
    @Required
    public String name;

    @Column(nullable = false, length = 500)
    @Required
    @MaxLength(500)
    public String summary;

    @Column(nullable = false, length = 4000)
    @Required
    public String description;

    @Column(nullable = false)
    @Constraints.Required
    public String organisation;

    @ManyToOne(optional = true, cascade = {DETACH, REFRESH})
    public Category category;

    // there's no length limit for a URL according to RFC 2616, but 2500 should be enough
    @Column(nullable = true, length = 2500)
    public String projectUrl;

    @Column(nullable = true, length = 2500)
    public String demoUrl;

    @Column(nullable = true, length = 2500)
    public String avatarUrl;

    @Column(nullable = false)
    @Required
    public String licenseType;

    @Column(nullable = true)
    public String contributors;

    @Column(nullable = true)
    public String licenseUrl;

    @Column(nullable = false)
    public Long downloadCount = 0L;

    @Column(nullable = false)
    public Long upVoteCount = 0L;

    @Column(nullable = false)
    public Long downVoteCount = 0L;

    @Column(nullable = false)
    public Date createdOn = new Date();

    @Column(nullable = false)
    public Date updatedOn = new Date();

    @OneToOne(optional = true, cascade = ALL)
    public Rating rating;

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    public List<Comment> comments;

    @ManyToMany(cascade = {DETACH, PERSIST, REFRESH})
    public List<Tag> tags;

    @OneToMany(mappedBy = "playModule")
    protected List<ModuleVersion> versions;

    @Override
    public int compareTo(Module module) {
        return name == null ? -1 : name.compareTo(module.name);
    }

    public String getDescriptionHtml() {
        return new MarkdownProcessor().markdown(description);
    }

    public static final Finder<Long, Module> FIND = new Finder<Long, Module>(Long.class, Module.class);

    public List<ModuleVersion> getVersions() {
        return ModuleVersion.findByModule(this);
    }

    public ModuleVersion getMostRecentVersion() {
        return CollectionUtils.last(getVersions());
    }

    public static Module findByModuleKey(String moduleKey) {
        return FIND.fetch("owner").where().eq("key", moduleKey.trim()).findUnique();
    }

    public static Map<String, String> options() {
        Map<String, String> options = new LinkedHashMap<String, String>();

        for (Module module : all()) {
            options.put(module.id.toString(),
                    module.name);
        }

        return options;
    }

    @Override
    public Module getModule() {
        return this;
    }

    public static List<Module> all() {
        return FIND.findList();
    }

    public static int count() {
        return FIND.findRowCount();
    }

    public static List<Module> findByTag(Tag tag) {
        return FIND.where().in("tags", tag).findList();
    }

    public static List<Module> findByCategory(Category category) {
        return FIND.where().eq("category", category).findList();
    }

    public static List<Module> findMostRecent(int count) {
        return FIND.where().orderBy("updatedOn DESC").setMaxRows(count).findList();
    }

    public static List<Module> findMostRecentModules(int count, MajorVersion version) {
        List<Module> modules = findModulesByMajorVersion(version);
        sort(modules, "updatedOn DESC, rating.averageRating DESC");
        return modules.size() <= count ? modules : modules.subList(0, count);
    }

    public static List<Module> findMostRecentModules(List<PlayVersion> versions) {
        List<Module> modules = findModulesByPlayVersion(versions);
        sort(modules, "updatedOn DESC, rating.averageRating DESC");
        return modules;
    }

    public static List<Module> findHighestRatedModules(int count, MajorVersion version) {
        List<Module> modules = findModulesByMajorVersion(version);
        sort(modules, "rating.averageRating DESC, updatedOn DESC");
        return modules.size() <= count ? modules : modules.subList(0, count);
    }

    public static List<Module> findHighestRatedModules(List<PlayVersion> versions) {
        List<Module> modules = findModulesByPlayVersion(versions);
        sort(modules, "rating.averageRating DESC, updatedOn DESC");
        return modules;
    }

    public static List<Module> ownedBy(User user) {
        return FIND.where().eq("owner", user).findList();
    }

    public static Module findById(Long id) {
        return FIND.where().eq("id", id).findUnique();
    }

}
