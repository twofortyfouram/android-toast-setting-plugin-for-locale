package com.markadamson.taskerplugin.homeassistant.model;

public class HAAPIResult<T> {
    private final HAAPIException mException ;
    private final T mResult;

    public HAAPIResult(T result) {
        mResult = result;
        mException = null;
    }

    public HAAPIResult(HAAPIException exception) {
        mException = exception;
        mResult = null;
    }

    public HAAPIException getException() {
        return mException;
    }

    public T getResult() {
        return mResult;
    }
}
