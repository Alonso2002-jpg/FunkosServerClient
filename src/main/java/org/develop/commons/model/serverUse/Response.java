package org.develop.commons.model.serverUse;

public record Response(Status status, String content, String createdAt) {
    public enum Status {
        OK, ERROR, BYE, TOKEN
    }
}
