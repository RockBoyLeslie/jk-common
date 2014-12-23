package com.jk.common.param;

public class ParamContextException extends Exception {

    private static final long serialVersionUID = 1744585138919636984L;

    public ParamContextException(String message) {
        super(message);
    }
    
    public ParamContextException(Throwable cause) {
        super(cause);
    }
    
    public ParamContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
