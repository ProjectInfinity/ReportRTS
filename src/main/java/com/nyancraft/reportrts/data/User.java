package com.nyancraft.reportrts.data;

import java.util.UUID;

public class User {

    private int id;
    private String username;
    private UUID uuid;
    private boolean banned;

    public User() {
        banned = false;
        username = null;
        uuid = null;
        id = 0;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean getBanned() {
        return this.banned;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
}