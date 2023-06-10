package org.apache.bookkeeper.tls.mocks;

import org.apache.bookkeeper.auth.AuthCallbacks;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.tls.utils.enums.GenericInstance;
import org.mockito.Mockito;

// This mock is used for a *UNIT TEST*  of the class BookieAuthZFactory
// The authentication callback is used to communicate to the AuthHandler instance weather
// the authentication has been successful.
//
// For the purpose of the unit test there is no check weather the operationComplete method
// has set the authentication successfully on the handler, that will be tested
// during INTEGRATION TESTING

public class CallBackMock implements MockBehaviour{

   private GenericInstance instance;

   private AuthCallbacks.GenericCallback<Void> cbMock = Mockito.mock(AuthCallbacks.GenericCallback.class);

    public CallBackMock(GenericInstance instance) {
        this.instance = instance;
    }

    @Override
    public CallBackMock mock() throws MockException {
        switch (instance) {
            case VALID:
                // Do nothing in particular -> Not necessary for unit test
                break;
            case INVALID:
                // An exception is thrown if the code given is OK
                Mockito.doThrow(new RuntimeException()).when(cbMock).operationComplete(BKException.Code.OK, null);
                break;
            case NULL:
                cbMock = null;
                break;
            default:
                throw new MockException("invalid instance to mock");

        }

        return this;
    }

    public AuthCallbacks.GenericCallback<Void> getCbMock() {
        return cbMock;
    }
}
