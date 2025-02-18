package org.apache.bookkeeper.tls.utils;


public enum ConfigType {
    VALID_SINGLE_ROLE("DummyRole"),

    VALID_MULTIPLE_ROLES("Role1, Role2"),

    EMPTY(""),

    NULL_STRING(null),

    NULL(null);

    private String roles;

    private ConfigType(String roles) {
        this.roles = roles;
    }

    public String getRoles() {
        return roles;
    }


}
