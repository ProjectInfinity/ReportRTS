package com.nyancraft.reportrts.persistence.query;

public class SQLiteQuery extends Query{
    @Override
    public String createRequestTable() {
        return "CREATE TABLE reportrts_request (id integer primary key," +
                " user_id integer not null," +
                " mod_id integer," +
                " mod_timestamp bigint," +
                " mod_comment varchar(255)," +
                " tstamp bigint not null," +
                " world varchar(255) not null," +
                " x integer not null," +
                " y integer not null," +
                " z integer not null," +
                " yaw integer not null default 0," +
                " pitch integer not null default 0," +
                " text varchar(255) not null," +
                " status integer," +
                " notified_of_completion integer)";
    }

    @Override
    public String createTemporaryRequestTable() {
        return "CREATE TABLE temp_request (id integer primary key," +
                " user_id integer not null," +
                " mod_id integer," +
                " mod_timestamp bigint," +
                " mod_comment varchar(255)," +
                " tstamp bigint not null," +
                " world varchar(255) not null," +
                " x integer not null," +
                " y integer not null," +
                " z integer not null," +
                " yaw integer not null default 0," +
                " pitch integer not null default 0," +
                " text varchar(255) not null," +
                " status integer," +
                " notified_of_completion integer)";
    }

    @Override
    public String createUserTable() {
        return "CREATE TABLE reportrts_user (id integer primary key," +
                " name varchar(255) not null," +
                " banned integer)";
    }

    @Override
    public String getColumns(String table) {
        return "PRAGMA table_info(`" + table + "`)";
    }
}