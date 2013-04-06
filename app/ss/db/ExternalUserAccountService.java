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
import play.Play;
import scala.Option;
import scala.Some;
import securesocial.core.*;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;
import security.RoleDefinitions;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ExternalUserAccountService extends BaseUserService {

    public ExternalUserAccountService(Application application) {
        super(application);
    }

    @Override
    public SocialUser doFind(UserId userId) {
        ExternalAccount externalAccount = ExternalAccount.FIND.where()
                .eq("externalId", userId.id())
                .eq("provider", userId.providerId())
                .findUnique();

        return externalAccountToSocialUser(externalAccount);
    }

    @Override
    public SocialUser doFindByEmailAndProvider(String email, String providerId) {
        ExternalAccount externalAccount = ExternalAccount.findByEmailAndProvider(email, providerId);

        return externalAccountToSocialUser(externalAccount);
    }

    @Override
    public Identity doSave(Identity socialUser) {
        ExternalAccount externalAccount = ExternalAccount.FIND.where()
                .eq("externalId", socialUser.id().id())
                .eq("provider", socialUser.id().providerId())
                .findUnique();
        if (externalAccount == null) {
            externalAccount = new ExternalAccount();
            List<UserRole> roles = new ArrayList<UserRole>(Arrays.asList(UserRole.findByRoleName(RoleDefinitions.USER)));
            checkForAdminRights(socialUser, roles);
            String name = socialUser.fullName() != null ? socialUser.fullName() :
                    socialUser.firstName() != null ? socialUser.firstName() :
                            socialUser.lastName() != null ? socialUser.lastName() :
                                    !socialUser.email().isEmpty() ? socialUser.email().get() : "";
            externalAccount.user = createUser("" + System.nanoTime(),
                    name,
                    socialUser.avatarUrl().isEmpty() ? "" : socialUser.avatarUrl().get(),
                    roles);
        }

        externalAccount.externalId = socialUser.id().id();
        externalAccount.provider = socialUser.id().providerId();
        externalAccount.firstName = socialUser.firstName();
        externalAccount.lastName = socialUser.lastName();
        externalAccount.fullName = socialUser.fullName();
        externalAccount.email = socialUser.email().isEmpty() ? "" : socialUser.email().get();
        externalAccount.avatarUrl = socialUser.avatarUrl().isEmpty() ? "" : socialUser.avatarUrl().get();

        externalAccount.setAuthenticationMethod(socialUser.authMethod());

        if (!socialUser.oAuth1Info().isEmpty()) {
            if (externalAccount.oAuth1Info == null) {
                externalAccount.oAuth1Info = new MPOOAuth1Info();
            }
            externalAccount.oAuth1Info.secret = socialUser.oAuth1Info().get().secret();
            externalAccount.oAuth1Info.token = socialUser.oAuth1Info().get().token();
            externalAccount.oAuth1Info.save();
            Ebean.saveAssociation(externalAccount, "oAuth1Info");
        } else {
            externalAccount.oAuth1Info = null;
        }

        if (!socialUser.oAuth2Info().isEmpty()) {
            if (externalAccount.oAuth2Info == null) {
                externalAccount.oAuth2Info = new MPOOAuth2Info();
            }
            externalAccount.oAuth2Info.accessToken = socialUser.oAuth2Info().get().accessToken();
            externalAccount.oAuth2Info.expiresIn = (Integer) (socialUser.oAuth2Info().get().expiresIn().isEmpty() ? null : socialUser.oAuth2Info().get().expiresIn().get());
            externalAccount.oAuth2Info.refreshToken = socialUser.oAuth2Info().get().refreshToken().isEmpty() ? null : socialUser.oAuth2Info().get().refreshToken().get();
            externalAccount.oAuth2Info.tokenType = socialUser.oAuth2Info().get().tokenType().isEmpty() ? null : socialUser.oAuth2Info().get().tokenType().get();
            externalAccount.oAuth2Info.save();
            Ebean.saveAssociation(externalAccount, "oAuth2Info");

        } else {
            externalAccount.oAuth2Info = null;
        }

        if (!socialUser.passwordInfo().isEmpty()) {
            if (externalAccount.passwordInfo == null) {
                externalAccount.passwordInfo = new MPOPasswordInfo();
            }
            externalAccount.passwordInfo.hasher = socialUser.passwordInfo().get().hasher();
            externalAccount.passwordInfo.password = socialUser.passwordInfo().get().password();
            externalAccount.passwordInfo.salt = socialUser.passwordInfo().get().salt().isEmpty() ? null : socialUser.passwordInfo().get().salt().get();
            externalAccount.passwordInfo.save();
            Ebean.saveAssociation(externalAccount, "passwordInfo");

        } else {
            externalAccount.passwordInfo = null;
        }

        externalAccount.save();
        return socialUser;
    }

    @Override
    public void doSave(Token token) {
        ExternalToken externalToken = new ExternalToken(token);
        externalToken.save();
    }

    @Override
    public Token doFindToken(String uuid) {
        Token token = null;
        ExternalToken externalToken = ExternalToken.FIND.where()
                .eq("uuid", uuid)
                .findUnique();

        if (externalToken != null) {
            token = externalToken.toToken();
        }

        return token;
    }

    @Override
    public void doDeleteToken(String uuid) {
        ExternalToken externalToken = ExternalToken.FIND.where()
                .eq("uuid", uuid)
                .findUnique();

        if (externalToken != null) {
            externalToken.delete();
        }
    }

    @Override
    public void doDeleteExpiredTokens() {
        List<ExternalToken> tokens = ExternalToken.FIND.all();
        for (ExternalToken t : tokens) {
            if (t.isExpired()) {
                t.delete();
            }
        }
    }

    private void checkForAdminRights(Identity socialUser, List<UserRole> roles) {
        // steve - this is a bit of a hack
        Configuration configuration = Play.application().configuration();
        String initialAdmin = configuration.getString("admin.initial");
        if (!StringUtils.isEmpty(initialAdmin)) {
            List<String> adminIds = new ArrayList<String>(Arrays.asList(initialAdmin.split(",")));
            if (User.getAdmins().size() < adminIds.size()) {
                if (adminIds.contains(socialUser.id().id())) {
                    roles.clear();
                    roles.add(UserRole.findByRoleName(RoleDefinitions.ADMIN));
                }
            }
        }
    }

    private SocialUser externalAccountToSocialUser(ExternalAccount externalAccount) {
        SocialUser socialUser = null;
        if (externalAccount != null) {
            socialUser = new SocialUser(
                    new UserId(externalAccount.externalId, externalAccount.provider),
                    externalAccount.firstName,
                    externalAccount.lastName,
                    externalAccount.fullName,
                    externalAccount.email != null ? new Some(externalAccount.email) : Option.<String>empty(),
                    externalAccount.avatarUrl != null ? new Some(externalAccount.avatarUrl) : Option.<String>empty(),
                    externalAccount.getAuthenticationMethod(),
                    externalAccount.oAuth1Info != null ? new Some(externalAccount.oAuth1Info.toOAuth1Info()) : Option.<OAuth1Info>empty(),
                    externalAccount.oAuth2Info != null ? new Some(externalAccount.oAuth2Info.toOAuth2Info()) : Option.<OAuth2Info>empty(),
                    externalAccount.passwordInfo != null ? new Some(externalAccount.passwordInfo.toPasswordInfo()) : Option.<PasswordInfo>empty()
            );
        }
        return socialUser;
    }

    private User createUser(String userName,
                            String displayName,
                            String avatarUrl,
                            List<UserRole> roles) {
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
