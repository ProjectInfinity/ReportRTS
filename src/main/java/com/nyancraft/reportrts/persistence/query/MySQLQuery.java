package com.nyancraft.reportrts.persistence.query;

import com.nyancraft.reportrts.ReportRTS;

public class MySQLQuery extends Query{
    @Override
    public String createRequestTable() {
        return "CREATE TABLE `" + ReportRTS.getPlugin().storagePrefix + "reportrts_request` (" +
                "`id` INT(10) UNSIGNED NULL AUTO_INCREMENT," +
                "`user_id` INT(10) UNSIGNED NULL DEFAULT '0'," +
                "`mod_id` INT(10) UNSIGNED NULL DEFAULT '0'," +
                "`mod_timestamp` INT(10) UNSIGNED NULL DEFAULT NULL," +
                "`mod_comment` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                "`tstamp` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
                "`world` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8_general_ci'," +
                "`bc_server` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8_general_ci'," +
                "`x` INT(10) NOT NULL DEFAULT '0'," +
                "`y` INT(10) NOT NULL DEFAULT '0'," +
                "`z` INT(10) NOT NULL DEFAULT '0'," +
                "`yaw` SMALLINT(6) NOT NULL DEFAULT '0'," +
                "`pitch` SMALLINT(6) NOT NULL DEFAULT '0'," +
                "`text` VARCHAR(255) NOT NULL COLLATE 'utf8_general_ci'," +
                "`status` TINYINT(1) UNSIGNED NULL DEFAULT '0'," +
                "`notified_of_completion` TINYINT(1) UNSIGNED NULL DEFAULT '0'," +
                "PRIMARY KEY (`id`))";
    }

    @Override
    public String createUserTable() {
        return "CREATE TABLE `" + ReportRTS.getPlugin().storagePrefix + "reportrts_user` (" +
                "`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(255) NOT NULL COLLATE 'utf8_general_ci'," +
                "`uuid` CHAR(36) NULL DEFAULT NULL," +
                "`banned` TINYINT(1) UNSIGNED NULL DEFAULT NULL," +
                "PRIMARY KEY (`id`))";
    }

    @Override
    public String getColumns(String table) {
        return "show columns from `" + table + "`";
    }
}