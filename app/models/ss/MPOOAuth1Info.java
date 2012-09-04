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
import securesocial.core.java.OAuth1Info;
import securesocial.core.java.OAuth2Info;

import javax.persistence.Entity;

/**
 * Persistence wrapper for SecureSocial's {@link securesocial.core.java.OAuth2Info} class.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class MPOOAuth1Info extends AbstractModel
{
    public String token;

    public String secret;

    public MPOOAuth1Info()
    {
        // no-op
    }

    public MPOOAuth1Info(OAuth1Info oAuth1Info)
    {
        this.token = oAuth1Info.token;
        this.secret = oAuth1Info.secret;
    }

    public OAuth1Info toOAuth1Info()
    {
        OAuth1Info oAuth1Info = new OAuth1Info();

        oAuth1Info.token = this.token;
        oAuth1Info.secret = this.secret;

        return oAuth1Info;
    }

}
