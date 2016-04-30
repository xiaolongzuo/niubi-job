/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.zuoxiaolong.niubi.job.persistent.shiro;

import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.SimpleHash;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class HashHelper {

    private static final String hashAlgorithm = "MD5";

    public static String getHashedPassword(String password, String salt) {
        Hash hash = new SimpleHash(hashAlgorithm, password, salt);
        return hash.toHex();
    }

}
