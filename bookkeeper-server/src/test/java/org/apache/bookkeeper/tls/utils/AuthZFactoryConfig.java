package org.apache.bookkeeper.tls.utils;

import org.apache.bookkeeper.utils.GenericInstance;

public class AuthZFactoryConfig {
    private ConfigType authConfig;
    private GenericInstance bookieConnectionPeer;
    private GenericInstance callback;

    public AuthZFactoryConfig(ConfigType authConfig, GenericInstance bookieConnectionPeer, GenericInstance callback) {
        this.authConfig = authConfig;
        this.bookieConnectionPeer = bookieConnectionPeer;
        this.callback = callback;
    }

    public ConfigType getAuthConfig() {
        return authConfig;
    }

    public GenericInstance getBookieConnectionPeer() {
        return bookieConnectionPeer;
    }

    public GenericInstance getCallback() {
        return callback;
    }

    public boolean shouldAuthenticate() {
        return authConfig.equals(ConfigType.VALID_SINGLE_ROLE) &&
            bookieConnectionPeer.equals(GenericInstance.VALID) &&
            callback.equals(GenericInstance.VALID);
    }

    public boolean isValidInputConfig() {
        return !authConfig.equals(ConfigType.NULL) && !authConfig.equals(ConfigType.INVALID);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- Auth configuration: ");
        sb.append(authConfig.toString());
        sb.append("\n");

        sb.append("- Peer Connection: ");
        sb.append(bookieConnectionPeer.toString());
        sb.append("\n");

        sb.append("- Auth Callback: ");
        sb.append(callback.toString());
        sb.append("\n");

        return sb.toString();
    }
}
