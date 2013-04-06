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
import scala.Option;
import scala.Some;
import securesocial.core.OAuth2Info;

import javax.persistence.Entity;

/**
 * Persistence wrapper for SecureSocial's {@link OAuth2Info} class.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class MPOOAuth2Info extends AbstractModel {
    public String accessToken;

    public String tokenType;

    public Integer expiresIn;

    public String refreshToken;

    public MPOOAuth2Info() {
        // no-op
    }

    public MPOOAuth2Info(OAuth2Info oAuth2Info) {
        this.accessToken = oAuth2Info.accessToken();
        this.tokenType = oAuth2Info.tokenType().isEmpty() ? null : oAuth2Info.tokenType().get();
        this.expiresIn = (Integer) (oAuth2Info.expiresIn().isEmpty() ? null : oAuth2Info.expiresIn().get());
        this.refreshToken = oAuth2Info.refreshToken().isEmpty() ? null : oAuth2Info.refreshToken().get();
    }

    public OAuth2Info toOAuth2Info() {
        OAuth2Info oAuth2Info = new OAuth2Info(this.accessToken,
                this.tokenType != null ? new Some(this.tokenType) : Option.<String>empty(),
                this.expiresIn != null ? new Some(this.expiresIn) : Option.empty(),
                this.refreshToken != null ? new Some(this.refreshToken) : Option.<String>empty());

        return oAuth2Info;
    }
}
