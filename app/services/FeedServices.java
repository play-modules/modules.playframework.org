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
package services;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import models.HistoricalEvent;
import play.Logger;
import play.cache.Cache;
import play.mvc.Http;
import services.support.CreateFeed;
import utils.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Pere Villega (pere.villega@gmail.com)
 */
public class FeedServices
{
    public static final String ATOM = "atom_1.0";
    public static final String RSS1 = "rss_1.0";
    public static final String RSS2 = "rss_2.0";

    public static final Map<String, String> feedTypes = new HashMap<String, String>(){
        { put(ATOM, "application/atom+xml"); }
        { put(RSS1, "application/rss+xml"); }
        { put(RSS2, "application/rss+xml"); }
    };


    /**
     * Empties the cached values to propagate changes made to historical events
     */
    public static void clearCachedFeeds()
    {
        for(String type: feedTypes.keySet()) {
           Cache.set(type, "", 1);
        }
    }

    /**
     * Generates the feed entry for the application, based on the type of feed requested
     * Data is cached for 1 day unless something modifies the Historical Events list
     *
     * @param feedType the type of feed requested
     * @param request the request received
     * @throws Exception if the feed couldn't be created
     * @return the content of the feed
     */
    public static String generateFeed(String feedType, Http.Request request) throws Exception
    {
        return Cache.getOrElse(feedType, new CreateFeed(feedType, request), 24 * 60 * 60);
    }
}
