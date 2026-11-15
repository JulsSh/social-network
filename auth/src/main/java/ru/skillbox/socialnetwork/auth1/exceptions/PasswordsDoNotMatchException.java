package ru.skillbox.socialnetwork.auth1.exceptions;

public class PasswordsDoNotMatchException extends RuntimeException{
    public PasswordsDoNotMatchException(){
        super("Passwords do not match");
    }
}
