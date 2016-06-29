package com.hgyw.bookshare.dataAccess;


import java.io.IOException;

/**
 * Unchecked exception for IO exceptions in {@link DataAccess} interface.
 * This is because the methods of {@code DataAccess} don't throw IOException.
 */
public class DataAccessIoException extends RuntimeException {
    public DataAccessIoException() { }

    public DataAccessIoException(String detailMessage) {
        super(detailMessage);
    }

    public DataAccessIoException(String detailMessage, Exception cause) {
        super(detailMessage, cause);
    }

    public DataAccessIoException(Exception cause) {
        super(cause);
    }

    @Override
    public Exception getCause() {
        return (Exception) super.getCause();
    }
}
