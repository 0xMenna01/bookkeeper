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

import org.apache.bookkeeper.auth.AuthCallbacks;
import org.apache.bookkeeper.auth.BookieAuthProvider;
import org.apache.bookkeeper.common.util.ReflectionUtils;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.proto.BookieConnectionPeer;
import org.apache.bookkeeper.tls.mocks.ConnectionPeerMock;
import org.apache.bookkeeper.tls.mocks.MockException;
import org.apache.bookkeeper.tls.mocks.builders.ConnectionPeerMockBuilder;
import org.apache.bookkeeper.tls.utils.enums.ConfigType;
import org.apache.bookkeeper.tls.utils.TestUtils;
import org.apache.bookkeeper.tls.utils.enums.GenericInstance;
import org.apache.bookkeeper.util.CertUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;

import java.util.Collection;

/**
 * Unit Tests for BookieAuthZFactory.
 */

@RunWith(Parameterized.class)
public class BookieAuthZFactoryTest {

    private ConfigType authConfig;
    private ServerConfiguration conf;
    private BookieConnectionPeer connectionPeerMock;
    private AuthCallbacks.GenericCallback<Void> completeCb;
    private boolean isExpectedException;
    private CertUtils certUtilsMock;
    @InjectMocks
    private BookieAuthProvider.Factory factory;


    public BookieAuthZFactoryTest(ConfigType authConfig, GenericInstance connectionPeerType, GenericInstance authCallbackType, boolean isExpectedException) throws MockException {
        // 1. Initialize factory
        String factoryClassName = BookieAuthZFactory.class.getName();
        factory = ReflectionUtils.newInstance(factoryClassName, BookieAuthProvider.Factory.class);

        this.authConfig = authConfig;

        conf = null;
        if (authConfig.getRoles() != null)
            conf = buildConfig(authConfig.getRoles());


        // 2. Build mocks for bookieConnectionPeer
        ConnectionPeerMockBuilder.getInstance()
            .setup(connectionPeerType);

        ConnectionPeerMock connectionPeerMock = ConnectionPeerMockBuilder.getInstance()
            .build();

        this.connectionPeerMock = connectionPeerMock.getConnectionPeerMock();
        this.certUtilsMock = connectionPeerMock.getCertificatesMock().getMockCertUtils();


        // 3. Construct the authentication callback
        if (authCallbackType.equals(GenericInstance.VALID)){
            // TODO -> mock a valid instance
        } else completeCb = null;

        // 4. Set weather an exception is expected
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
    public void testProviderInit() {

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