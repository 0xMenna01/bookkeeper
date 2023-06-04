package org.apache.bookkeeper.tls.utils.enums;

public enum GenericInstance {
    VALID,
    INVALID,
    NULL;

    public GenericInstance checkAndReturnIfNull() {
        return this.equals(NULL) ? null : this;
    }
}
