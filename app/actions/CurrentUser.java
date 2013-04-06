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
package actions;

import com.avaje.ebean.Ebean;
import models.User;
import models.ss.ExternalAccount;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CurrentUser extends Action.Simple {
    @Override
    public Result call(Http.Context ctx) throws Throwable {
        accessUser(ctx);
        return delegate.call(ctx);
    }

    private static User accessUser(Http.Context ctx) {
        Http.Context.current.set(ctx);

        User user = null;
        Identity socialUser = SecureSocial.currentUser();
        if (socialUser != null) {
            ExternalAccount externalAccount = ExternalAccount.findByUserIdAndProvider(socialUser.id().id(),
                    socialUser.id().providerId());
            if (externalAccount != null) {
                user = externalAccount.user;
                Ebean.refresh(externalAccount.user);
            }
        }

        return user;
    }

    public static User currentUser() {
        return currentUser(Http.Context.current());
    }

    public static User currentUser(Http.Context context) {
        return accessUser(context);
    }
}
