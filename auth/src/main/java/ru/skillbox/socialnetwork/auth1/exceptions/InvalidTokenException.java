package ru.skillbox.socialnetwork.auth1.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(){
        super("Invalid or expired refresh token");
    }
}
