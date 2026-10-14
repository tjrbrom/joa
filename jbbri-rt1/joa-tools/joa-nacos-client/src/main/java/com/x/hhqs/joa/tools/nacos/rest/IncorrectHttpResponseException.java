package com.x.hhqs.joa.tools.nacos.rest;

public class IncorrectHttpResponseException extends Exception {

    private static final long serialVersionUID = 2965260694130849027L;

    public IncorrectHttpResponseException(String errorMessage) {
        super(errorMessage);
    }

}
