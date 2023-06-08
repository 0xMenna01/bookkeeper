package org.apache.bookkeeper.tls.mocks.builders;

import org.apache.bookkeeper.tls.mocks.ConnectionPeerMock;
import org.apache.bookkeeper.tls.mocks.MockException;

public class ConnectionPeerMockBuilder extends GenericMockBuilder<ConnectionPeerMock> {

    private static ConnectionPeerMockBuilder instance = null;

    private ConnectionPeerMockBuilder(){}

    public static ConnectionPeerMockBuilder getInstance() {
        if(instance == null) {
            instance = new ConnectionPeerMockBuilder();
        }
        return instance;
    }

    @Override
    public ConnectionPeerMock build() throws MockException {
        return new ConnectionPeerMock(this.instanceType);
    }
}
