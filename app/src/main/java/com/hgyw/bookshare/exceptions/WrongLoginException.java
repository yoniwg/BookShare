package hgyw.com.bookshare.exceptions;

/**
 * Exception for wrong login.
 */
public class WrongLoginException extends Exception {
    private final Issue issue;

    public enum Issue{
        WRONG_USERNAME_OR_PASSWORD("Wrong username and password"),
        USERNAME_TAKEN("Username is taken.");
        String message;
        Issue(String message){
            this.message = message;
        }
        public String toString(){
            return message;
        }
    }

    public WrongLoginException(Issue issue) {
        super(issue.toString());
        this.issue = issue;
    }
}
