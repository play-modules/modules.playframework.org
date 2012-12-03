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
import org.joda.time.DateTime;
import securesocial.core.java.Token;
import javax.persistence.Entity;

/**
 * Persistence wrapper for SecureSocial's {@link Token} class
 *
 * User: pvillega
 */
@Entity
public class ExternalToken extends AbstractModel
{

    public String uuid;
    public String email;
    public DateTime creationTime;
    public DateTime expirationTime;
    public boolean isSignUp;

    public static final Finder<Long, ExternalToken> FIND = new Finder<Long, ExternalToken>(Long.class, ExternalToken.class);

    public ExternalToken()
    {
        //no-op
    }

    public ExternalToken(Token token)
    {
        this.uuid = token.uuid;
        this.email = token.email;
        this.creationTime = token.creationTime;
        this.expirationTime = token.expirationTime;
        this.isSignUp = token.isSignUp;
    }

    public boolean isExpired() {
        return expirationTime.isBeforeNow();
    }

    public Token toToken()
    {
        Token token = new Token();
        token.setUuid(uuid);
        token.setEmail(email);
        token.setCreationTime(creationTime);
        token.setExpirationTime(expirationTime);
        token.setIsSignUp(isSignUp);

        return token;
    }
}
