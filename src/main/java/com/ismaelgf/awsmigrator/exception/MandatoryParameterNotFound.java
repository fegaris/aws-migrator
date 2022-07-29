package com.ismaelgf.awsmigrator.exception;

public class MandatoryParameterNotFound extends RuntimeException {
    public MandatoryParameterNotFound(String message) {
        super(message);
    }
}
