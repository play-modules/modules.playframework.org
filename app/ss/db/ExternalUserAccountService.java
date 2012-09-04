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
package ss.db;

import com.avaje.ebean.Ebean;
import models.Rate;
import models.User;
import models.UserRole;
import models.Vote;
import models.ss.ExternalAccount;
import models.ss.MPOOAuth1Info;
import models.ss.MPOOAuth2Info;
import play.Application;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.SocialUser;
import securesocial.core.java.UserId;
import security.RoleDefinitions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ExternalUserAccountService extends BaseUserService
{
    public ExternalUserAccountService(Application application)
    {
        super(application);
    }

    @Override
    public SocialUser doFind(UserId userId)
    {
        SocialUser socialUser = null;
        ExternalAccount externalAccount = ExternalAccount.FIND.where()
                .eq("externalId", userId.id)
                .eq("provider", userId.provider)
                .findUnique();

        if (externalAccount != null)
        {
            socialUser = new SocialUser();
            socialUser.id = userId;
            socialUser.authMethod = externalAccount.authenticationMethod;
            socialUser.displayName = externalAccount.displayName;
            socialUser.avatarUrl = externalAccount.avatarUrl;
            socialUser.email = externalAccount.email;
            socialUser.isEmailVerified = false;
            socialUser.oAuth1Info = externalAccount.oAuth1Info == null ? null : externalAccount.oAuth1Info.toOAuth1Info();
            socialUser.oAuth2Info = externalAccount.oAuth2Info == null ? null : externalAccount.oAuth2Info.toOAuth2Info();
        }

        return socialUser;
    }

    @Override
    public void doSave(SocialUser socialUser)
    {
        ExternalAccount externalAccount = ExternalAccount.FIND.where()
                                                         .eq("externalId", socialUser.id.id)
                                                         .eq("provider", socialUser.id.provider)
                                                         .findUnique();
        if (externalAccount == null)
        {
            externalAccount = new ExternalAccount();
            User user = new User();
            // todo - steve - 04/09/2012 - remove user name
            user.userName = "" + System.nanoTime();
            user.displayName = socialUser.displayName;
            user.avatarUrl = socialUser.avatarUrl;
            user.roles = new ArrayList<UserRole>(Arrays.asList(UserRole.findByRoleName(RoleDefinitions.USER)));
            user.rates = new ArrayList<Rate>();
            user.votes = new ArrayList<Vote>();
            user.accountActive = true;
            user.save();

            externalAccount.user = user;
        }

        externalAccount.externalId = socialUser.id.id;
        externalAccount.provider = socialUser.id.provider;
        externalAccount.authenticationMethod = socialUser.authMethod;
        externalAccount.displayName = socialUser.displayName;
        externalAccount.avatarUrl = socialUser.avatarUrl;
        externalAccount.email = socialUser.email;
        if (socialUser.oAuth1Info != null)
        {
            if (externalAccount.oAuth1Info == null)
            {
                externalAccount.oAuth1Info = new MPOOAuth1Info();
            }
            externalAccount.oAuth1Info.secret = socialUser.oAuth1Info.secret;
            externalAccount.oAuth1Info.token = socialUser.oAuth1Info.token;
            Ebean.saveAssociation(externalAccount, "oAuth1Info");
        }
        else
        {
            externalAccount.oAuth1Info = null;
        }

        if (socialUser.oAuth2Info != null)
        {
            if (externalAccount.oAuth2Info == null)
            {
                externalAccount.oAuth2Info = new MPOOAuth2Info();
            }
            externalAccount.oAuth2Info.accessToken = socialUser.oAuth2Info.accessToken;
            externalAccount.oAuth2Info.expiresIn = socialUser.oAuth2Info.expiresIn;
            externalAccount.oAuth2Info.refreshToken = socialUser.oAuth2Info.refreshToken;
            externalAccount.oAuth2Info.tokenType = socialUser.oAuth2Info.tokenType;
            Ebean.saveAssociation(externalAccount, "oAuth2Info");

        }
        else
        {
            externalAccount.oAuth2Info = null;
        }

        externalAccount.save();
    }
}
