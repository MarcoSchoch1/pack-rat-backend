package com.packrat.backend.exception;

public class UsernameDuplicateException extends RuntimeException {
    public UsernameDuplicateException(String username) { super("Username: " + username + " already exists"); }    
}
