package org.apache.bookkeeper.utils.mocks;

import org.apache.bookkeeper.utils.GenericInstance;

public abstract class GenericMockBuilder<T extends MockBehaviour> {

    protected GenericInstance instanceType;

    public void setup(GenericInstance instanceType) {
        this.instanceType = instanceType;
    }

    protected abstract T build() throws MockException;
}
