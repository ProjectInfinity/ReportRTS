package com.nyancraft.reportrts.data;

public class Comment implements Comparable<Comment> {

    private long timestamp;

    private int ticket;
    private int id;

    private String name;
    private String comment;

    public Comment(long timestamp, int ticket, int id, String name, String comment) {
        this.timestamp = timestamp;
        this.ticket = ticket;
        this.id = id;
        this.name = name;
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTicket() {
        return ticket;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTicket(int ticket) {
        this.ticket = ticket;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int compareTo(Comment comment) {
        int id = comment.getId();
        return comment.getId() - id;
    }
}
