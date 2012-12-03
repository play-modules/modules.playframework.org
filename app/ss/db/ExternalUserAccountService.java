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
package ss.db;

import com.avaje.ebean.Ebean;
import models.Rate;
import models.User;
import models.UserRole;
import models.Vote;
import models.ss.*;
import play.Application;
import play.Configuration;
import play.Logger;
import play.Play;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.SocialUser;
import securesocial.core.java.Token;
import securesocial.core.java.UserId;
import security.RoleDefinitions;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        ExternalAccount externalAccount = ExternalAccount.FIND.where()
                .eq("externalId", userId.id)
                .eq("provider", userId.provider)
                .findUnique();

        return externalAccountToSocialUser(externalAccount);
    }

    @Override
    public SocialUser doFindByEmailAndProvider(String email, String providerId)
    {
        ExternalAccount externalAccount =  ExternalAccount.findByEmailAndProvider(email, providerId);

        return externalAccountToSocialUser(externalAccount);
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
            List<UserRole> roles = new ArrayList<UserRole>(Arrays.asList(UserRole.findByRoleName(RoleDefinitions.USER)));
            checkForAdminRights(socialUser, roles);
            String name = socialUser.getFullName() != null ? socialUser.getFullName() :
                            socialUser.getFirstName() != null ? socialUser.getFirstName() :
                            socialUser.getLastName() != null ? socialUser.getLastName() :
                            socialUser.getEmail() != null ? socialUser.getEmail() : "";
            externalAccount.user = createUser("" + System.nanoTime(), name, socialUser.avatarUrl, roles);
        }

        externalAccount.externalId = socialUser.id.id;
        externalAccount.provider = socialUser.id.provider;
        externalAccount.firstName = socialUser.firstName;
        externalAccount.lastName = socialUser.lastName;
        externalAccount.fullName = socialUser.fullName;
        externalAccount.email = socialUser.email;
        externalAccount.avatarUrl = socialUser.avatarUrl;

        externalAccount.authenticationMethod = socialUser.authMethod;

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

        if (socialUser.passwordInfo != null)
        {
            if (externalAccount.passwordInfo == null)
            {
                externalAccount.passwordInfo = new MPOPasswordInfo();
            }
            externalAccount.passwordInfo.password = socialUser.passwordInfo.password;
            externalAccount.passwordInfo.salt = socialUser.passwordInfo.salt;
            Ebean.saveAssociation(externalAccount, "passwordInfo");

        }
        else
        {
            externalAccount.passwordInfo = null;
        }

        externalAccount.save();
    }

    @Override
    public void doSave(Token token)
    {
        ExternalToken externalToken = new ExternalToken(token);
        externalToken.save();
    }

    @Override
    public Token doFindToken(String uuid)
    {
        Token token = null;
        ExternalToken externalToken = ExternalToken.FIND.where()
                .eq("uuid", uuid)
                .findUnique();

        if(externalToken != null)
        {
            token = externalToken.toToken();
        }

        return token;
    }

    @Override
    public void doDeleteToken(String uuid)
    {
        ExternalToken externalToken = ExternalToken.FIND.where()
                .eq("uuid", uuid)
                .findUnique();

        if(externalToken != null)
        {
            externalToken.delete();
        }
    }

    @Override
    public void doDeleteExpiredTokens()
    {
        List<ExternalToken> tokens = ExternalToken.FIND.all();
        for(ExternalToken t: tokens)
        {
            if(t.isExpired())
            {
                t.delete();
            }
        }
    }

    private void checkForAdminRights(SocialUser socialUser, List<UserRole> roles)
    {
        // steve - this is a bit of a hack
        Configuration configuration = Play.application().configuration();
        String initialAdmin = configuration.getString("admin.initial");
        if (!StringUtils.isEmpty(initialAdmin))
        {
            List<String> adminIds = new ArrayList<String>(Arrays.asList(initialAdmin.split(",")));
            if (User.getAdmins().size() < adminIds.size())
            {
                if (adminIds.contains(socialUser.id.id))
                {
                    roles.clear();
                    roles.add(UserRole.findByRoleName(RoleDefinitions.ADMIN));
                }
            }
        }
    }

    private SocialUser externalAccountToSocialUser(ExternalAccount externalAccount)
    {
        SocialUser socialUser = null;
        if (externalAccount != null)
        {
            socialUser = new SocialUser();
            socialUser.id = new UserId();
            socialUser.id.setId(externalAccount.externalId);
            socialUser.id.setProvider(externalAccount.externalId);
            socialUser.firstName = externalAccount.firstName;
            socialUser.lastName = externalAccount.lastName;
            socialUser.fullName = externalAccount.fullName;
            socialUser.email = externalAccount.email;
            socialUser.avatarUrl = externalAccount.avatarUrl;

            socialUser.authMethod = externalAccount.authenticationMethod;
            socialUser.oAuth1Info = externalAccount.oAuth1Info == null ? null : externalAccount.oAuth1Info.toOAuth1Info();
            socialUser.oAuth2Info = externalAccount.oAuth2Info == null ? null : externalAccount.oAuth2Info.toOAuth2Info();
            socialUser.passwordInfo = externalAccount.passwordInfo == null ? null : externalAccount.passwordInfo.toPasswordInfo();
        }
        return socialUser;
    }

    private User createUser(String userName,
                            String displayName,
                            String avatarUrl,
                            List<UserRole> roles)
    {
        User user = new User();
        user.userName = userName;
        user.displayName = displayName;
        user.avatarUrl = avatarUrl;
        user.accountActive = true;
        user.roles = new ArrayList<UserRole>(roles);
        user.rates = new ArrayList<Rate>();
        user.votes = new ArrayList<Vote>();
        user.save();
        user.saveManyToManyAssociations("roles");
        return user;
    }
}
