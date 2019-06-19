package com.markadamson.taskerplugin.homeassistant.model;

import java.lang.Exception;

public class HAAPIException extends Exception {
    private static final long serialVersionUID = 7395216398898021862L;

    public HAAPIException() { super(); }
    public HAAPIException(String message) { super(message); }
    public HAAPIException(String message, Throwable cause) { super(message, cause); }
    public HAAPIException(Throwable cause) { super(cause); }
}
