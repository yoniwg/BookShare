package com.hgyw.bookshare.exceptions;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.UserType;

import java.util.Objects;

/**
 * Exception for wrong login.
 */
public class WrongLoginException extends Exception {
    private final Issue issue;
    private final UserType userType;

    public enum Issue{
        WRONG_USERNAME_OR_PASSWORD("Wrong username and password"),
        USERNAME_TAKEN("Username is taken."),
        SPECIFIC_ACCESS_ILLEGAL("The access is not legal. (current user is not customer/supplier)"),
        USERNAME_EMPTY("Username is empty");
        String message;
        Issue(String message){
            this.message = message;
        }
        public String toString(){
            return message;
        }
    }

    public WrongLoginException(Issue issue) {
        this(issue, null);
    }

    public WrongLoginException(Issue issue, UserType userType) {
        super(issue.toString() + (userType == null? "" : " User-type: " + userType));
        Objects.requireNonNull(issue);
        this.issue = issue;
        this.userType = userType;
    }

    /**
     *
     * @return Issue enum of this exception
     */
    public Issue getIssue() {
        return issue;
    }

    /**
     *
     * @return the user-type of this exception, or null
     */
    public UserType getUserType() {
        return userType;
    }
}
