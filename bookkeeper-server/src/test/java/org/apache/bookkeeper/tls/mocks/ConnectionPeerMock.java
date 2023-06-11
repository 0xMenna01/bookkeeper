package org.apache.bookkeeper.tls.mocks;

import static org.mockito.ArgumentMatchers.any;

import org.apache.bookkeeper.auth.BookKeeperPrincipal;
import org.apache.bookkeeper.proto.BookieConnectionPeer;
import org.apache.bookkeeper.tls.mocks.builders.CertificatesBuilder;
import org.apache.bookkeeper.tls.utils.enums.GenericInstance;
import org.mockito.Mockito;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class ConnectionPeerMock implements MockBehaviour{
    private GenericInstance instance;
    BookieConnectionPeer connectionPeerMock = Mockito.mock(BookieConnectionPeer.class);

    /// If authorized is set to true the onProtocolUpgrade tested method sets a bookkeeper Principal
    CertificatesMock certificatesMock;


    public ConnectionPeerMock(GenericInstance instance) {
        this.instance = instance;
    }

    /// The connection peer *INVALID* instance is mocked through invalid certificate roles
    /// associated to a secure connection.

    @Override
    public ConnectionPeerMock mock() throws MockException {

        if (instance.equals(GenericInstance.NULL))
            connectionPeerMock = null;
        else {
            // Mock certificates
            CertificatesBuilder.getInstance().setup(instance);

            certificatesMock = CertificatesBuilder.getInstance()
                .build()
                .mock();

            // For both the *VALID* and *INVALID* instance the connection is secure and has a valid socket addr
            Mockito.when(connectionPeerMock.isSecure()).thenReturn(true);
            mockValidConnectionSocket();
            // This changes the behaviour between *VALID* and *INVALID* instances
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
