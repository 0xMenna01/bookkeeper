package org.apache.bookkeeper.tls.utils;

import org.apache.bookkeeper.utils.GenericInstance;

public class AuthZFactoryConfig {
    private GenericInstance bookieConnectionPeer;
    private GenericInstance callback;

    public AuthZFactoryConfig(GenericInstance bookieConnectionPeer, GenericInstance callback) {
        this.bookieConnectionPeer = bookieConnectionPeer;
        this.callback = callback;
    }

    public GenericInstance getBookieConnectionPeer() {
        return bookieConnectionPeer;
    }

    public GenericInstance getCallback() {
        return callback;
    }

    public boolean shouldAuthenticate() {
        return bookieConnectionPeer.equals(GenericInstance.VALID) &&
            callback.equals(GenericInstance.VALID);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- Peer Connection: ");
        sb.append(bookieConnectionPeer.toString());
        sb.append("\n");

        sb.append("- Auth Callback: ");
        sb.append(callback.toString());
        sb.append("\n");

        return sb.toString();
    }
}
