package org.apache.bookkeeper.utils.mocks;

public class MockException extends Exception{

    public MockException() {
        super();
    }

    public MockException(String message, Throwable cause) {
        super(message, cause);
    }

    public MockException(String message) {
        super(message);
    }
}
