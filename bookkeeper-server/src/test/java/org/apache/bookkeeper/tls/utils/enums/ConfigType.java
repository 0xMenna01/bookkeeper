package org.apache.bookkeeper.tls.utils.enums;


public enum ConfigType {
    VALID_SINGLE_ROLE("DummyRole"),
    VALID_MULTIPLE_ROLES("Role1, Role2"),
    INVALID(""),
    NULL(null);

    private String roles;

    private ConfigType(String roles) {
        this.roles = roles;
    }

    public String getRoles() {
        return roles;
    }


}
