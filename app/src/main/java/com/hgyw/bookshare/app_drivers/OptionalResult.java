package com.hgyw.bookshare.app_drivers;

import com.annimon.stream.Optional;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Created by haim7 on 08/06/2016.
 */
public final class OptionalResult<Result, Exception> {

    private final Result result;
    private final Exception exception;

    private OptionalResult(Result result, Exception exception) {
        this.result = result;
        this.exception = exception;
    }

    public static <Result, Exception> OptionalResult<Result, Exception> ofResult(Result result) {
        return new OptionalResult<>(result, null);
    }

    public static <Result, Exception> OptionalResult<Result, Exception> ofException(Exception exception) {
        return new OptionalResult<>(null, Objects.requireNonNull(exception));
    }

    public boolean hasResult() {
        return exception == null;
    }

    public Result getResult() {
        if (exception != null) throw new NoSuchElementException();
        return result;
    }

    public Result getResultOrElse(Result defaultValue) {
        if (exception != null) return defaultValue;
        return result;
    }

    public Exception getException() {
        if (exception == null) throw new NoSuchElementException();
        return exception;
    }
}
