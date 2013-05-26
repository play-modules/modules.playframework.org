package services.support;


import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import controllers.routes;
import models.HistoricalEvent;
import play.Logger;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Creates the output of a feed of the given type
 * @author Pere Villega (pere.villega@gmail.com)
 */
public class CreateFeed implements Callable<String>
{
    private String feedType = "";
    private String domain = "";
    private Http.Request request = null;


    public CreateFeed(String feedType, Http.Request request)
    {
        this.feedType = feedType;
        this.domain = request.host();
        this.request = request;
    }

    @Override
    public String call() throws Exception
    {
        String result = "";
        try
        {
            List<HistoricalEvent> historicalEvents = HistoricalEvent.findMostRecent(20);

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(feedType);
            feed.setTitle("Play! Modules");
            feed.setLink(domain);
            feed.setUri(domain);
            feed.setPublishedDate(new Date());
            feed.setDescription("The Play! Framework's module repository feed");

            List<SyndEntry> entries = new ArrayList<>(historicalEvents.size());
            for (HistoricalEvent historicalEvent : historicalEvents)
            {
                SyndEntry entry = new SyndEntryImpl();
                entry.setTitle(historicalEvent.category);
                entry.setAuthor("Play framework modules");
                entry.setPublishedDate(historicalEvent.creationDate);

                entry.setLink(routes.Modules.details(historicalEvent.moduleKey).absoluteURL(request));
                entry.setUri("mpo-he-" + historicalEvent.id);

                SyndContent description = new SyndContentImpl();
                description.setType("text/plain");
                description.setValue(historicalEvent.message);
                entry.setDescription(description);

                entries.add(entry);
            }

            feed.setEntries(entries);

            SyndFeedOutput output = new SyndFeedOutput();
            result = output.outputString(feed);

        }
        catch (FeedException e)
        {
            Logger.error(String.format("A problem occurred when generating the %s feed",
                    feedType),
                    e);
        }
        return result;
    }
}

