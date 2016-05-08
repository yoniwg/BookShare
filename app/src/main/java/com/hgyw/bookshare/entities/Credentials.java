package hgyw.com.bookshare.entities;

import java.util.Objects;

/**
 * Immutable class represents username and password.
 */
public final class Credentials {

    public static final Credentials EMPTY = create("", "");

    private final String username;
    private final String password;

    private Credentials(String username, String password) {
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credentials that = (Credentials) o;
        return username.equals(that.username) &&  password.equals(that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static Credentials create(String username, String password) {
        return new Credentials(username, password);
    }
}
