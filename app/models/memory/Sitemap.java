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
package models.memory;

/**
 * @author Pere Villega (pere.villega@gmail.com)
 */
public class Sitemap {

    public String url;
    public String changeFreq;
    public String priority;

    /**
     * Stores information on a link for a sitemap
     * Change Frequency is set to monthly
     * Priority is set to "0.5"
     * @param url the link to add to the sitemap
     */
    public Sitemap(String url) {
        this(url, "monthly", "0.5");
    }

    /**
     * Stores information on a link for a sitemap
     * Priority is set to "0.5"
     * @param url the link to add to the sitemap
     * @param changeFreq how often is the link updated. Default: monthly
     */
    public Sitemap(String url, String changeFreq) {
        this(url, changeFreq, "0.5");
    }

    /**
     * Stores information on a link for a sitemap
     * @param url the link to add to the sitemap
     * @param changeFreq how often is the link updated. Default: Monthly
     * @param priority priority of the link (relevance). Default "0.5"
     */
    public Sitemap(String url, String changeFreq, String priority) {
        this.url = url;
        this.changeFreq = changeFreq;
        this.priority = priority;
    }

}
