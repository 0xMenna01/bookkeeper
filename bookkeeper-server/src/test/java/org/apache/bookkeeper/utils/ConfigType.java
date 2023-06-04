package org.apache.bookkeeper.utils;

public enum ConfigType {
    VALID_SINGLE_ROLE("role1, role2"),
    VALID_MULTIPLE_ROLES("dummyRole"),
    EMPTY(""),
    INVALID(","),
    NULL(null);

    private String roles;

    private ConfigType(String roles) {
        this.roles = roles;
    }

    public String getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("roles provided: ");
        sb.append(roles);
        return sb.toString();
    }
}
