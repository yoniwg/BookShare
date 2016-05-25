package com.hgyw.bookshare.entities;

import java.io.Serializable;
import java.util.Objects;

public final class Credentials implements Serializable {

    public static final Credentials empty() { return  new Credentials("", ""); }

    private String username;
    private String password;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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
