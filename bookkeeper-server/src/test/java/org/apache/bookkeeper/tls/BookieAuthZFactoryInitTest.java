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
import org.apache.bookkeeper.tls.utils.ConfigType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Unit Tests for BookieAuthZFactory.
 */
@RunWith(Parameterized.class)
public class BookieAuthZFactoryInitTest {

    private ServerConfiguration conf;
    private boolean isExpectedException;
    private BookieAuthProvider.Factory factory;


    public BookieAuthZFactoryInitTest(ConfigType authConfig) {

        // 1. Initialize factory
        String factoryClassName = BookieAuthZFactory.class.getName();
        factory = ReflectionUtils.newInstance(factoryClassName, BookieAuthProvider.Factory.class);

        conf = null;
        if (!authConfig.equals(ConfigType.NULL))
            conf = buildConfig(authConfig.getRoles());

        switch (authConfig) {
            case VALID_SINGLE_ROLE:
                this.isExpectedException = false;
                break;
            case VALID_MULTIPLE_ROLES:
                this.isExpectedException = false;
                break;
            default:
                this.isExpectedException = true;
        }
    }


    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {

        List<Object[]> parameters = new ArrayList<>();

        for (ConfigType configType : ConfigType.values()) {
            Object[] parameterSet = {configType};
            parameters.add(parameterSet);
        }

        return parameters;
    }

    private ServerConfiguration buildConfig(String roles) {
        return new ServerConfiguration().setAuthorizedRoles(roles);
    }


    @Test
    public void testProviderInit() {

        try {
            factory.init(conf);
            Assert.assertFalse("An exception was expected because of wrong input configuration.\n", this.isExpectedException);

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown\n",
                this.isExpectedException);
        }
    }
}