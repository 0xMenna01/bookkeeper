package org.apache.bookkeeper.utils.mocks;

import org.apache.bookkeeper.utils.GenericInstance;

public abstract class GenericMockBuilder<T extends MockBehaviour, J> {

    protected J instanceType;

    public void setup(J instanceType) {
        this.instanceType = instanceType;
    }

    protected abstract T build() throws MockException;
}
