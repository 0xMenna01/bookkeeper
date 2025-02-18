package org.apache.bookkeeper.tls.mocks;

import static org.mockito.ArgumentMatchers.any;

import org.apache.bookkeeper.auth.BookKeeperPrincipal;
import org.apache.bookkeeper.proto.BookieConnectionPeer;
import org.apache.bookkeeper.tls.mocks.builders.CertificatesBuilder;
import org.apache.bookkeeper.tls.utils.TestUtils;
import org.apache.bookkeeper.utils.GenericInstance;
import org.apache.bookkeeper.utils.mocks.MockException;
import org.apache.bookkeeper.utils.mocks.MockBehaviour;
import org.mockito.Mockito;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class ConnectionPeerMock implements MockBehaviour {
    private TestUtils.ConnectionPeerType instance;
    BookieConnectionPeer connectionPeerMock = Mockito.mock(BookieConnectionPeer.class);

    /// If authorized is set to true the onProtocolUpgrade tested method sets a bookkeeper Principal
    CertificatesMock certificatesMock;


    public ConnectionPeerMock(TestUtils.ConnectionPeerType instance) {
        this.instance = instance;
    }

    /// The connection peer *INVALID* instance is mocked through invalid certificate roles
    /// associated to a secure connection.

    @Override
    public ConnectionPeerMock mock() throws MockException {

        if (instance.equals(TestUtils.ConnectionPeerType.NULL))
            connectionPeerMock = null;
        else {
            // Mock certificates
            CertificatesBuilder.getInstance().setup(instance);

            certificatesMock = CertificatesBuilder.getInstance()
                .build()
                .mock();

            // For *VALID* and *INVALID* instance the connection is secure and has a valid socket addr
            if (instance.equals(TestUtils.ConnectionPeerType.INSECURE)) {
                Mockito.when(connectionPeerMock.isSecure()).thenReturn(false);
            } else {
                // INVALID, VALID and EMPTY_CERT instances
                Mockito.when(connectionPeerMock.isSecure()).thenReturn(true);
            }

            mockValidConnectionSocket();
            // This changes the behaviour between *VALID*, *INVALID* and *INSECURE* instances
            Mockito.when(connectionPeerMock.getProtocolPrincipals()).thenReturn(certificatesMock.getMockCertificates());

            Mockito.doAnswer(invocation -> {
                // Access the arguments passed to the method
                BookKeeperPrincipal bookKeeperInputPrincipal = invocation.getArgument(0);

                Mockito.when(connectionPeerMock.getAuthorizedId()).thenReturn(bookKeeperInputPrincipal);

                return null;
            }).when(connectionPeerMock).setAuthorizedId(any(BookKeeperPrincipal.class));

        }

        return this;
    }


    private void mockValidConnectionSocket() {

        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        int port = 443;
        SocketAddress socketAddress = new InetSocketAddress(ipAddress, port);
        Mockito.when(connectionPeerMock.getRemoteAddr()).thenReturn(socketAddress);
    }

    public BookieConnectionPeer getConnectionPeerMock() {
        return connectionPeerMock;
    }

}
