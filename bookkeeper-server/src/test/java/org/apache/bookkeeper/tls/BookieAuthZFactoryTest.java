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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
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
        conf = getServerConfig();

        this.isExpectedException = isExpectedException;
    }

    private ServerConfiguration getServerConfig() {

        switch (authConfig) {
            case VALID:
                return new ServerConfiguration().setAuthorizedRoles("dummyRole");
            case EMPTY:
                return new ServerConfiguration().setAuthorizedRoles("");

            case INVALID:
                return new ServerConfiguration().setAuthorizedRoles(",");

            case NULL:
                return null;

            default:
                throw new RuntimeException();
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
            //CONFIG               EXCEPTION
            { ConfigType.VALID,     false},
            { ConfigType.EMPTY,     true},
            { ConfigType.INVALID,     true},
            { ConfigType.NULL,     true},
        });
    }

    @Test
    public void testInitConfig() {
        try {
            factory.init(conf);
            Assert.assertFalse("An exception was expected", this.isExpectedException);
        } catch (Exception e) {
            Assert.assertTrue("No exception was expected, but " + e.getClass().getName() + " has been thrown.",
                this.isExpectedException);
        }
    }




    private enum ConfigType {
        VALID,
        EMPTY,
        INVALID,
        NULL
    }


}

