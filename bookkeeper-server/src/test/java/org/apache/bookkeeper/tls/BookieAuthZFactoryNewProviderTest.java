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
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.common.util.ReflectionUtils;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.proto.BookieConnectionPeer;
import org.apache.bookkeeper.tls.mocks.CallBackMock;
import org.apache.bookkeeper.utils.mocks.MockException;
import org.apache.bookkeeper.tls.mocks.builders.CallbackMockBuilder;
import org.apache.bookkeeper.tls.mocks.builders.ConnectionPeerMockBuilder;
import org.apache.bookkeeper.tls.utils.AuthZFactoryConfig;
import org.apache.bookkeeper.tls.utils.ConfigType;
import org.apache.bookkeeper.tls.utils.TestUtils;
import org.apache.bookkeeper.utils.GenericInstance;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collection;

/**
 * Unit Tests for BookieAuthZFactory.
 */
@RunWith(Parameterized.class)
public class BookieAuthZFactoryNewProviderTest {

    private ServerConfiguration conf;
    private AuthZFactoryConfig authConfig;
    private BookieConnectionPeer connectionPeerMock;
    private CallBackMock callBackMock;
    private boolean isExpectedException;
    private BookieAuthProvider.Factory factory;


    public BookieAuthZFactoryNewProviderTest(GenericInstance connectionPeerType, GenericInstance authCallbackType, boolean isExpectedException) throws MockException {

        this.authConfig = new AuthZFactoryConfig(connectionPeerType, authCallbackType);

        // 1. Initialize factory and init with server config
        String factoryClassName = BookieAuthZFactory.class.getName();
        factory = ReflectionUtils.newInstance(factoryClassName, BookieAuthProvider.Factory.class);

        this.conf = buildConfig();

        try {
            factory.init(this.conf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2. Build mocks for bookieConnectionPeer
        ConnectionPeerMockBuilder.getInstance()
            .setup(connectionPeerType);


        this.connectionPeerMock = ConnectionPeerMockBuilder.getInstance()
            .build()
            .mock()
            .getConnectionPeerMock();


        // 3. Construct the authentication callback
        CallbackMockBuilder.getInstance()
            .setup(authCallbackType);

        this.callBackMock = CallbackMockBuilder.getInstance()
            .build()
            .mock();

        // 4. Set weather an exception is expected
        this.isExpectedException = isExpectedException;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return TestUtils.buildAuthConfigParameters();
    }


    private ServerConfiguration buildConfig() {
        return new ServerConfiguration().setAuthorizedRoles(ConfigType.VALID_SINGLE_ROLE.getRoles());
    }

    @Ignore
    @Test
    public void testProviderInit() {

        System.out.println(authConfig.toString());

        try {

            BookieAuthProvider provider = factory.newProvider(connectionPeerMock, callBackMock.getCbMock());
            provider.onProtocolUpgrade();
            Assert.assertFalse("An exception was expected due to a null reference \n" +
                authConfig.toString(), this.isExpectedException);

            Assert.assertEquals("The authentication had an unexpected behaviour",
                authConfig.shouldAuthenticate() ? BKException.Code.OK : BKException.Code.UnauthorizedAccessException,
                callBackMock.getAuthCode()
            );

            if (authConfig.shouldAuthenticate()) {
                Assert.assertNotEquals("The peer connection must have an authorized Id\n" +
                    authConfig.toString(), connectionPeerMock.getAuthorizedId(), null);

                Assert.assertEquals("Certificate roles must be equals", conf.getAuthorizedRoles()[0], connectionPeerMock.getAuthorizedId().getName());
            }

        } catch (Exception e) {
            Assert.assertTrue("No exception was expected" +
                    ", but " + e.getClass().getName() + " has been thrown\n" +
                    authConfig.toString(),
                this.isExpectedException);
        }
    }
}