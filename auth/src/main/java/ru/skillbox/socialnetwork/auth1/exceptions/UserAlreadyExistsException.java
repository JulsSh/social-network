package ru.skillbox.socialnetwork.auth1.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email){
        super("User already exists "+ email);
    }
}
