package com.nyancraft.reportrts.persistence.query;

public abstract class Query {
    public abstract String createRequestTable();
    public abstract String createTemporaryRequestTable();
    public abstract String createUserTable();
    public String createUser(){
        return "INSERT INTO `reportrts_user` (`name`, `banned`) VALUES (?, '0')";
    }
    public String createExactUser(){
        return "INSERT INTO `reportrts_user` (`id`,`name`, `banned`) VALUES (?,?,?)";
    }
    public String createRequest(){
        return "INSERT INTO `reportrts_request` (`user_id`, `tstamp`, `world`, `x`, `y`, `z`, `yaw`, `pitch`," +
                " `text`, `status`, `notified_of_completion`) VALUES" +
                " (?, ?, ?, ?, ?, ?, ?, ?, ?, '0', '0')";
    }

    public String setUserStatus(int status){
        return "UPDATE reportrts_user SET `banned` = '" + status + "' WHERE `id` = ?";
    }
    public String setNotificationStatus(int id, int status){
        return "UPDATE reportrts_request SET `notified_of_completion` = " + status + " WHERE `id` = " + id;
    }

    public abstract String getColumns(String table);
    public String getAllOpenAndClaimedRequests(){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE `status` < 2";
    }
    public String getUserId(){
        return "SELECT `id` FROM `reportrts_user` WHERE `name` = ?";
    }
    public String getUserName(int userId){
        return "SELECT `name` FROM `reportrts_user` WHERE `id` = '" + userId + "'";
    }
    public String getUserStatus(int userId){
        return "SELECT `banned` FROM reportrts_user WHERE `id` = '" + userId + "'";
    }
    public String getLatestTicketIdByUser(int userId){
        return "SELECT `id` FROM `reportrts_request` WHERE `user_id` = '" + userId + "' ORDER BY `tstamp` DESC LIMIT 1";
    }
    public String getHeldRequests(int from, int limit){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.status = '2' AND request.id > '" + from + "' LIMIT " + limit;
    }
    public String getClosedRequests(int from, int limit){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.status = '3' ORDER BY request.mod_timestamp DESC LIMIT " + from + ", " + limit;
    }
    public String getTicketById(int id){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.id = '" + id + "'";
    }
    public String getHeldByTicketId(int id){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.mod_id = user.id WHERE request.id = '" + id + "'";
    }
    public String getTicketStatusById(int id){
        return "SELECT `status` FROM reportrts_request WHERE `id` = " + id;
    }
    public String getLocationById(int id){
        return "SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world` FROM reportrts_request WHERE `id` = '" + id + "' LIMIT 1";
    }
    public String getAllFromTable(String table){
        return "SELECT * FROM `" + table + "`";
    }
    public String getUnnotifiedUsers(){
        return "SELECT * FROM reportrts_request AS request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE `status` = 3 AND notified_of_completion = 0";
    }
    public String getHandledBy(){
        return "SELECT * FROM reportrts_request WHERE mod_id = ?";
    }

    public String getLimitedHandledBy(){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.mod_id = ? ORDER BY request.mod_timestamp DESC LIMIT ?, ? ";
    }

    public String getLimitedCreatedBy(){
        return "SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.user_id = ? ORDER BY request.tstamp DESC LIMIT ?, ? ";
    }

    public String countRequests(int status){
        return "SELECT COUNT(`id`) FROM reportrts_request WHERE `status` = '" + status + "'";
    }

    public String deleteRequestOlderThan(String table, int lessThanThis){
        return "DELETE FROM " + table + " WHERE tstamp < " + lessThanThis;
    }
    public String deleteEntryById(String table, int id){
        return "DELETE FROM `" + table + "` WHERE `id` = '" + id + "'";
    }
}