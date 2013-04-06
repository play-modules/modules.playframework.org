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
package models.ss;

import models.AbstractModel;
import models.User;
import securesocial.core.AuthenticationMethod;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class ExternalAccount extends AbstractModel {
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    public User user;

    @Column(nullable = false)
    public String externalId;

    @Column(nullable = false)
    public String provider;

    @Column(nullable = true)
    public String firstName;

    @Column(nullable = true)
    public String lastName;

    @Column(nullable = true)
    public String fullName;

    @Column(nullable = true)
    public String email;

    @Column(nullable = true)
    public String avatarUrl;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    public MPOOAuth1Info oAuth1Info;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    public MPOOAuth2Info oAuth2Info;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    public MPOPasswordInfo passwordInfo;

    @Column(nullable = false)
    private String authenticationMethod;

    public AuthenticationMethod getAuthenticationMethod() {
        return new AuthenticationMethod(authenticationMethod);
    }

    public void setAuthenticationMethod(AuthenticationMethod authMethod) {
        authenticationMethod = authMethod.method();
    }

    public static final Finder<Long, ExternalAccount> FIND = new Finder<Long, ExternalAccount>(Long.class, ExternalAccount.class);

    public static ExternalAccount findByUserIdAndProvider(String id, String provider) {
        return FIND.where()
                .eq("externalId", id)
                .eq("provider", provider)
                .findUnique();
    }

    public static ExternalAccount findByEmailAndProvider(String email, String provider) {
        return FIND.where()
                .eq("email", email)
                .eq("provider", provider)
                .findUnique();
    }

    public static List<ExternalAccount> findByUser(User currentUser) {
        return FIND.where()
                .eq("user", currentUser)
                .order().asc("provider")
                .findList();
    }
}