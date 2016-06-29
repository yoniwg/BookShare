package com.hgyw.bookshare.app_drivers;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Immutable class that hold result or exception, but only one of them.
 */
public final class OptionalResult<Result, Exception> {

    private final Result result;
    private final Exception exception;

    private OptionalResult(Result result, Exception exception) {
        this.result = result;
        this.exception = exception;
    }

    /**
     * Factory method for result
     */
    public static <Result, Exception> OptionalResult<Result, Exception> ofResult(Result result) {
        return new OptionalResult<>(result, null);
    }

    /**
     * Factory method for exception
     */
    public static <Result, Exception> OptionalResult<Result, Exception> ofException(Exception exception) {
        return new OptionalResult<>(null, Objects.requireNonNull(exception));
    }

    /**
     * @return true if has result, false if haas exception.
     */
    public boolean hasResult() {
        return exception == null;
    }

    /**
     * @throws NoSuchElementException if hasResult()==false
     */
    public Result getResult() {
        if (exception != null) throw new NoSuchElementException();
        return result;
    }

    /**
     * get result or defaul value if hasResult()==false
     */
    public Result getResultOrElse(Result defaultValue) {
        if (exception != null) return defaultValue;
        return result;
    }

    /**
     * @throws NoSuchElementException if hasResult()==true
     */
    public Exception getException() {
        if (exception == null) throw new NoSuchElementException();
        return exception;
    }
}
