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
package controllers;

import actions.CurrentUser;
import be.objectify.deadbolt.actions.Restrict;
import models.User;
import play.mvc.Result;
import play.mvc.With;
import security.RoleDefinitions;
import views.html.admin.users;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@With(CurrentUser.class)
@Restrict(RoleDefinitions.ADMIN)
public class Users extends AbstractController
{
    public static Result getUsers()
    {
        return ok(users.render(CurrentUser.currentUser(),
                               User.all()));
    }

    public static Result makeAdmin()
    {
        return TODO;
    }

    public static Result mergeAccounts()
    {
        return TODO;
    }
}
