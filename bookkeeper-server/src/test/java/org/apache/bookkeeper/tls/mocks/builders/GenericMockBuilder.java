package org.apache.bookkeeper.tls.mocks.builders;

import org.apache.bookkeeper.tls.mocks.MockBehaviour;
import org.apache.bookkeeper.tls.mocks.MockException;
import org.apache.bookkeeper.tls.utils.enums.GenericInstance;

public abstract class GenericMockBuilder<T extends MockBehaviour> {

    protected GenericInstance instanceType;

    public void setup(GenericInstance instanceType) {
        this.instanceType = instanceType;
    }

    abstract T build() throws MockException;
}
