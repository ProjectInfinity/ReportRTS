package com.nyancraft.reportrts.persistence;

import com.nyancraft.reportrts.ReportRTS;

public class QueryGen {

    public static String createRequestTable(){
        if(ReportRTS.getPlugin().useMySQL){
            return "CREATE TABLE `reportrts_request` (" +
            "`id` INT(10) UNSIGNED NULL AUTO_INCREMENT," +
            "`user_id` INT(10) UNSIGNED NULL DEFAULT '0'," +
            "`mod_id` INT(10) UNSIGNED NULL DEFAULT '0'," +
            "`mod_timestamp` INT(10) UNSIGNED NULL DEFAULT NULL," +
            "`mod_comment` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
            "`tstamp` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
            "`world` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8_general_ci'," +
            "`x` INT(10) NOT NULL DEFAULT '0'," +
            "`y` INT(10) NOT NULL DEFAULT '0'," +
            "`z` INT(10) NOT NULL DEFAULT '0'," +
            "`yaw` SMALLINT(6) NOT NULL DEFAULT '0'," +
            "`pitch` SMALLINT(6) NOT NULL DEFAULT '0'," +
            "`text` VARCHAR(255) NOT NULL COLLATE 'utf8_general_ci'," +
            "`status` TINYINT(1) UNSIGNED NULL DEFAULT '0'," +
            "`notified_of_completion` TINYINT(1) UNSIGNED NULL DEFAULT '0'," +
            "PRIMARY KEY (`id`))";
        }else{
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
    }
    public static String createTemporaryRequestTable(){
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
    public static String createUserTable(){
        if(ReportRTS.getPlugin().useMySQL){
            return "CREATE TABLE `reportrts_user` (" +
                    "`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "`name` VARCHAR(255) NOT NULL COLLATE 'utf8_general_ci'," +
                    "`banned` TINYINT(1) UNSIGNED NULL DEFAULT NULL," +
                    "PRIMARY KEY (`id`))";
        }else{
            return "CREATE TABLE reportrts_user (id integer primary key," +
                   " name varchar(255) not null," +
                   " banned integer)";
        }
    }
    public static String getColumns(String table){
        if(ReportRTS.getPlugin().useMySQL){
            return "show columns from `" + table + "`";
        }else{
            return "PRAGMA table_info(`" + table + "`)";
        }
    }
    public static String getAllOpenAndClaimedRequests(){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE `status` < 2";
    }
    public static String getUserId(){
        return "SELECT `id` FROM `reportrts_user` WHERE `name` = ?";
    }
    public static String getUserName(int userId){
        return "SELECT `name` FROM `reportrts_user` WHERE `id` = '" + userId + "'";
    }
    public static String createUser(){
        return "INSERT INTO `reportrts_user` (`name`, `banned`) VALUES (?, '0')";
    }
    public static String createExactUser(){
        return "INSERT INTO `reportrts_user` (`id`,`name`, `banned`) VALUES (?,?,?)";
    }
    public static String createRequest(){
        return "INSERT INTO `reportrts_request` (`user_id`, `tstamp`, `world`, `x`, `y`, `z`, `yaw`, `pitch`," +
                " `text`, `status`, `notified_of_completion`) VALUES" +
                " (?, ?, ?, ?, ?, ?, ?, ?, ?, '0', '0')";
    }
    public static String getUserStatus(int userId){
        return "SELECT `banned` FROM reportrts_user WHERE `id` = '" + userId + "'";
    }
    public static String setUserStatus(int status){
        return "UPDATE reportrts_user SET `banned` = '" + status + "' WHERE `id` = ?";
    }
    public static String getLatestTicketIdByUser(int userId){
        return "SELECT `id` FROM `reportrts_request` WHERE `user_id` = '" + userId + "' ORDER BY `tstamp` DESC LIMIT 1";
    }
    public static String getHeldRequests(int from, int limit){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.status = '2' AND request.id > '" + from + "' LIMIT " + limit;
    }
    public static String getClosedRequests(int from, int limit){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.status = '3' ORDER BY request.mod_timestamp DESC LIMIT " + from + ", " + limit;
    }
    public static String getTicketById(int id){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.id = '" + id + "'";
    }
    public static String getHeldByTicketId(int id){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.mod_id = user.id WHERE request.id = '" + id + "'";
    }
    public static String getTicketStatusById(int id){
        return "SELECT `status` FROM reportrts_request WHERE `id` = " + id;
    }
    public static String getLocationById(int id){
        return "SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world` FROM reportrts_request WHERE `id` = '" + id + "' LIMIT 1";
    }
    public static String getAllFromTable(String table){
        return "SELECT * FROM `" + table + "`";
    }
    public static String deleteRequestOlderThan(String table, int lessThanThis){
        return "DELETE FROM " + table + " WHERE tstamp < " + lessThanThis;
    }
    public static String deleteEntryById(String table, int id){
        return "DELETE FROM `" + table + "` WHERE `id` = '" + id + "'";
    }
    public static String getUnnotifiedUsers(){
        return "SELECT * FROM reportrts_request AS request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE `status` = 3 AND notified_of_completion = 0";
    }
    public static String setNotificationStatus(int id, int status){
        return "UPDATE reportrts_request SET `notified_of_completion` = " + status + " WHERE `id` = " + id;
    }
    public static String getHandledBy(){
        return "SELECT * FROM reportrts_request WHERE mod_id = ?";
    }
    public static String countRequests(int status){
        return "SELECT COUNT(`id`) FROM reportrts_request WHERE `status` = '" + status + "'";
    }
}
