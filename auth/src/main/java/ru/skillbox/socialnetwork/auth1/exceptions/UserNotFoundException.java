package ru.skillbox.socialnetwork.auth1.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(){
        super("Email or password is incorrect");
    }

}
