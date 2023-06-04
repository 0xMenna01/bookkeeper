package org.apache.bookkeeper.tls.mocks;

import org.apache.bookkeeper.tls.utils.enums.GenericInstance;

public interface MockBuilder<T> {
    void setup(GenericInstance instanceType);
    T build() throws MockException;
}
