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
import securesocial.core.java.PasswordInfo;

import javax.persistence.Entity;

/**
 * Persistence wrapper for SecureSocial's {@link PasswordInfo} class.
 *
 * User: pvillega
 */
@Entity
public class MPOPasswordInfo extends AbstractModel
{

    public String password;

    public String salt;

    public MPOPasswordInfo()
    {
        //no-op
    }

    public MPOPasswordInfo(PasswordInfo pwdInfo)
    {
        this.password = pwdInfo.getPassword();
        this.salt = pwdInfo.getSalt();
    }

    public PasswordInfo toPasswordInfo()
    {
        PasswordInfo pwdInfo = new PasswordInfo();
        pwdInfo.setPassword(password);
        pwdInfo.setSalt(salt);

        return pwdInfo;
    }
}
