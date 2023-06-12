package org.apache.bookkeeper.utils;

public enum GenericInstance {
    VALID,
    INVALID,
    NULL;

    public GenericInstance checkAndReturnIfNull() {
        return this.equals(NULL) ? null : this;
    }
}
