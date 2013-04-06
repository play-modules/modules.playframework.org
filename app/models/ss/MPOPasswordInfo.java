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
import securesocial.core.PasswordInfo;

import javax.persistence.Entity;

/**
 * Persistence wrapper for SecureSocial's {@link PasswordInfo} class.
 * <p/>
 * User: pvillega
 */
@Entity
public class MPOPasswordInfo extends AbstractModel {
    public String hasher;

    public String password;

    public String salt;

    public MPOPasswordInfo() {
        //no-op
    }

    public MPOPasswordInfo(PasswordInfo pwdInfo) {
        this.hasher = pwdInfo.hasher();
        this.password = pwdInfo.password();
        this.salt = pwdInfo.salt().isEmpty() ? null : pwdInfo.salt().get();
    }

    public PasswordInfo toPasswordInfo() {
        PasswordInfo pwdInfo = new PasswordInfo(hasher, password, salt != null ? new Some(salt) : Option.<String>empty());

        return pwdInfo;
    }
}
