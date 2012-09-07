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
package models.ss;

import models.AbstractModel;
import models.User;
import securesocial.core.java.AuthenticationMethod;

import javax.persistence.*;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class ExternalAccount extends AbstractModel
{
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    public User user;

    @Column(nullable = false)
    public String externalId;

    @Column(nullable = false)
    public String provider;

    @Column(nullable = false)
    public String displayName;

    @Column(nullable = true)
    public String email;

    @Column(nullable = true)
    public String avatarUrl;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    public MPOOAuth1Info oAuth1Info;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    public MPOOAuth2Info oAuth2Info;

    @Enumerated(EnumType.STRING)
    public AuthenticationMethod authenticationMethod;

    public static final Finder<Long, ExternalAccount> FIND = new Finder<Long, ExternalAccount>(Long.class,
                                                                                               ExternalAccount.class);

    public static ExternalAccount findByUserIdAndProvider(String id,
                                                          String provider)
    {
        return FIND.where()
                   .eq("externalId", id)
                   .eq("provider", provider)
                   .findUnique();
    }

    public static List<ExternalAccount> findByUser(User currentUser)
    {
        return FIND.where()
                .eq("user", currentUser)
                .order().asc("provider")
                .findList();
    }
}