package org.apache.bookkeeper.client.utils;

public class ExceptionExpected {

    private Boolean constraintException;
    private Boolean customMetaException;

    public ExceptionExpected() {

    }

    public void setConstraintException(Boolean constraintException) {
        this.constraintException = constraintException;
    }

    public void setCustomMetaException(boolean customMetaException) {
        this.customMetaException = customMetaException;
    }

    public boolean shouldThrow() {
        return (constraintException || customMetaException);
    }

    public Boolean getConstraintException() {
        return constraintException;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("constraint exception: ");
        sb.append(constraintException.toString());
        sb.append("\n");
        
        sb.append("custom metadata exception: ");
        sb.append(customMetaException.toString());
        sb.append("\n");

        return sb.toString();
    }
}
