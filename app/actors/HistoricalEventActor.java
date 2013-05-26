/*
 * Copyright 2012 Steve Chaloner
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
package actors;

import akka.actor.UntypedActor;
import models.HistoricalEvent;
import services.FeedServices;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class HistoricalEventActor extends UntypedActor
{
    @Override
    public void onReceive(Object o) throws Exception
    {
        if (o instanceof HistoricalEvent)
        {
            ((HistoricalEvent)o).save();
            // we ensure the changes propagate to feeds
            FeedServices.clearCachedFeeds();
        }
    }
}
