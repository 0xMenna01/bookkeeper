package org.apache.bookkeeper.tls.utils.enums;


public enum ConfigType {
    VALID_SINGLE_ROLE("dummyRole"),
    VALID_MULTIPLE_ROLES("role1, role2"),
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
