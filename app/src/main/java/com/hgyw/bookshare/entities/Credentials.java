package com.hgyw.bookshare.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable class represents username and password.
 */
public final class Credentials implements Serializable {

    public static final Credentials EMPTY = new Credentials("", "");

    private final String username;
    private final String password;

    public Credentials(String username, String password) {
        this.username = Objects.requireNonNull(username).trim();
        this.password = Objects.requireNonNull(password).trim();

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

}
