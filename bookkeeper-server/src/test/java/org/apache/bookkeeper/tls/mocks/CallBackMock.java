package org.apache.bookkeeper.tls.mocks;

import static org.mockito.ArgumentMatchers.*;
import org.apache.bookkeeper.auth.AuthCallbacks;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.utils.GenericInstance;
import org.apache.bookkeeper.utils.mocks.MockException;
import org.apache.bookkeeper.utils.mocks.MockBehaviour;
import org.mockito.Mockito;

// This mock is used for a *UNIT TEST*  of the class BookieAuthZFactory
// The authentication callback is used to communicate to the AuthHandler instance weather
// the authentication has been successful.
//
// For the purpose of the unit test there is no check weather the operationComplete method
// has set the authentication successfully on the handler, that will be tested
// during INTEGRATION TESTING

public class CallBackMock implements MockBehaviour {

   private GenericInstance instance;

   private AuthCallbacks.GenericCallback<Void> cbMock = Mockito.mock(AuthCallbacks.GenericCallback.class);

   private Integer authCode = null;

    public CallBackMock(GenericInstance instance) {
        this.instance = instance;
    }

    @Override
    public CallBackMock mock() throws MockException {
        switch (instance) {
            case VALID:
                captureAuthCode();
                break;
            case INVALID:
                // An exception is thrown if the code given is OK
                Mockito.doThrow(new RuntimeException()).when(cbMock).operationComplete(BKException.Code.OK, null);
                captureAuthCodeInvalid();
                break;
            case NULL:
                cbMock = null;
                break;
            default:
                throw new MockException("invalid instance to mock");

        }

        return this;
    }


    private void captureAuthCode() {
        Mockito.doAnswer(invocation -> {
            // Access the arguments passed to the method
            int authCode = invocation.getArgument(0);
            this.authCode = authCode;

            return null;
        }).when(cbMock).operationComplete(any(int.class), any());
    }

    private void captureAuthCodeInvalid() {
        Mockito.doAnswer(invocation -> {
            // Access the arguments passed to the method
            int authCode = invocation.getArgument(0);
            this.authCode = authCode;

            return null;
        }).when(cbMock).operationComplete(BKException.Code.UnauthorizedAccessException, null);
    }


    public AuthCallbacks.GenericCallback<Void> getCbMock() {
        return cbMock;
    }

    public int getAuthCode() {
        return authCode;
    }
}
