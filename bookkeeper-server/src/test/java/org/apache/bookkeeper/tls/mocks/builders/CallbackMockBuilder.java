package org.apache.bookkeeper.tls.mocks.builders;

import org.apache.bookkeeper.tls.mocks.CallBackMock;
import org.apache.bookkeeper.utils.mocks.MockException;
import org.apache.bookkeeper.utils.mocks.GenericMockBuilder;

public class CallbackMockBuilder extends GenericMockBuilder<CallBackMock> {

    private static CallbackMockBuilder instance = null;

    private CallbackMockBuilder(){}

    public static CallbackMockBuilder getInstance() {
        if(instance == null) {
            instance = new CallbackMockBuilder();
        }
        return instance;
    }

    @Override
    public CallBackMock build() throws MockException {
        return new CallBackMock(this.instanceType);
    }
}
