package com.cmswe.alumni.common.exception;

/**
 * HTTP 客户端异常
 */
public class HttpClientException extends RuntimeException {
    
    private final int statusCode;
    
    public HttpClientException(String message) {
        super(message);
        this.statusCode = 500;
    }
    
    public HttpClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }
    
    public HttpClientException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}