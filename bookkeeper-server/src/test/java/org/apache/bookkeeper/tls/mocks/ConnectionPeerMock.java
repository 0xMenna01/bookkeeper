package org.apache.bookkeeper.tls.mocks;

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
    CertificatesMock certificatesMock;


    public ConnectionPeerMock(GenericInstance instance) {
        this.instance = instance;
        CertificatesBuilder.getInstance().setup(this.instance);
    }

    /// The connection peer *INVALID* instance is mocked through the incorrect certificate roles
    /// associated to a secure connection.

    /// By definition a secure connection *MUST* have valid certification roles.
    @Override
    public void mock() throws MockException {
        certificatesMock = CertificatesBuilder.getInstance().build();

        if (instance.equals(GenericInstance.NULL))
            connectionPeerMock = null;
        else {
            // For both the *VALID* and *INVALID* instance the connection is secure and has a valid socket addr
            Mockito.when(connectionPeerMock.isSecure()).thenReturn(true);
            mockValidConnectionSocket();
            // This changes the behaviour between *VALID* and *INVALID* instances
            Mockito.when(connectionPeerMock.getProtocolPrincipals()).thenReturn(certificatesMock.getMockCertificates());
        }
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

    public CertificatesMock getCertificatesMock() {
        return certificatesMock;
    }
}
