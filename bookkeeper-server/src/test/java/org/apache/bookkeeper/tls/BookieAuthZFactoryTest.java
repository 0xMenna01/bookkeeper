/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.bookkeeper.tls;

import org.apache.bookkeeper.auth.BookieAuthProvider;
import org.apache.bookkeeper.common.util.ReflectionUtils;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.tls.utils.enums.ConfigType;
import org.apache.bookkeeper.tls.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/**
 * Unit Tests for BookieAuthZFactory.
 */

@RunWith(Parameterized.class)
public class BookieAuthZFactoryTest {

    private boolean isExpectedException;
    private ServerConfiguration conf;
    private ConfigType authConfig;
    private BookieAuthProvider.Factory factory;


    public BookieAuthZFactoryTest(ConfigType authConfig, boolean isExpectedException) {
        String factoryClassName = BookieAuthZFactory.class.getName();
        factory = ReflectionUtils.newInstance(factoryClassName, BookieAuthProvider.Factory.class);

        this.authConfig = authConfig;

        conf = null;
        if (authConfig.getRoles() != null)
            conf = buildConfig(authConfig.getRoles());

        this.isExpectedException = isExpectedException;
    }

    private ServerConfiguration buildConfig(String roles) {
        return new ServerConfiguration().setAuthorizedRoles(roles);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return TestUtils.buildAuthConfigParameters();
    }

    @Test
    public void testInitConfig() {

        try {
            factory.init(conf);
            Assert.assertFalse("An exception was expected, " +
                authConfig.toString(), this.isExpectedException);
        } catch (Exception e) {
            Assert.assertTrue("No exception was expected, " +
                    authConfig.toString() + ", but " + e.getClass().getName() + " has been thrown",
                this.isExpectedException);
        }
    }





}