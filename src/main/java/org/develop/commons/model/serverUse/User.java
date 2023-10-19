package org.develop.commons.model.serverUse;

public record User(long id, String username, String password, Role role) {
    public enum Role{
        ADMIN, USER
    }
}
