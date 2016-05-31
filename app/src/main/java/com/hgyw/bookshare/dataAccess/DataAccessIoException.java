package com.hgyw.bookshare.dataAccess;

import java.io.IOException;

/**
 * Created by haim7 on 30/05/2016.
 */
public class DataAccessIoException extends RuntimeException {
    public DataAccessIoException() {
    }

    public DataAccessIoException(String detailMessage) {
        super(detailMessage);
    }

    public DataAccessIoException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }

    public DataAccessIoException(Throwable cause) {
        super(cause);
    }
}
