package com.nyancraft.reportrts.persistence;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Comment;
import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.data.User;

import com.nyancraft.reportrts.util.BungeeCord;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class MySQLDataProvider implements DataProvider {

    private ReportRTS plugin;

    private Connection db;

    private boolean connected;

    private User console;

    private int taskId;

    private HashMap<UUID, User> userCache;

    public MySQLDataProvider(ReportRTS plugin) {
        this.plugin = plugin;
        this.userCache = new HashMap<>();
    }

    private boolean initialize() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet query(String query) throws SQLException {

        if(this.db == null) {
            throw new IllegalStateException("Connection is null!");
        }
        Statement statement = this.db.createStatement();
        if(statement.execute(query)) {
            statement.executeQuery(query);
            return statement.getResultSet();
        }
        return null;

    }

    @Override
    public boolean isLoaded() {
        return this.connected;
    }

    @Override
    public void close() {
        this.connected = false;
        plugin.getServer().getScheduler().cancelTask(taskId);
        if(db != null) {
            try {
                db.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        } else
            plugin.getLogger().warning("Connection is null! Cannot close it.");
    }

    @Override
    public void reset() {

        try(Statement stmt = db.createStatement()) {

            stmt.addBatch("TRUNCATE TABLE `" + plugin.storagePrefix + "reportrts_user`");
            stmt.addBatch("TRUNCATE TABLE `" + plugin.storagePrefix + "reportrts_ticket`");

            stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean load() {

        if(isLoaded()) {
            // Server is already loaded, the plugin is more than likely trying to reload.
            return loadData();
        }

        // Check if MySQL driver exists on the system.
        if(!initialize()) {
            plugin.getLogger().severe("Unable to initialize because MySQL driver is missing!");
            return false;
        }

        try {
            this.db = DriverManager.getConnection("jdbc:mysql://" + plugin.storageHostname + ":" + plugin.storagePort + "/" + plugin.storageDatabase + "?autoReconnect=true", plugin.storageUsername, plugin.storagePassword);
            this.connected = true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }

        if(this.db == null) {
            plugin.getLogger().warning("Unable to load ReportRTS because the connection to the database failed.");
            return false;
        }

        if(!checkStructure()) {
            plugin.getLogger().warning("[MySQL] Structure is outdated or missing and was not created or modified correctly.");
            return false;
        }

        // Enable a refresh timer if it is needed to prevent interruption in the data-provider.
        if(plugin.storageRefreshTime > 0) {
            taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                public void run() {
                    try {
                        query("SELECT 1");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, 4000L, plugin.storageRefreshTime * 20);
        }

        return loadData();
    }

    private boolean checkStructure() {

        // The user table doesn't exist, we need to create it.
        if(!tableExists(plugin.storagePrefix + "reportrts_user")) {

            try(Statement stmt = db.createStatement()) {

                if(stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + plugin.storagePrefix + "reportrts_user` (" +
                        "`uid`  int(10) UNSIGNED NULL AUTO_INCREMENT ," +
                        "`name`  varchar(255) NOT NULL DEFAULT '' ," +
                        "`uuid`  char(36) NULL DEFAULT NULL ," +
                        "`banned`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 ," +
                        "PRIMARY KEY (`uid`))" +
                        "DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;") > 0) {

                    plugin.getLogger().warning("[MySQL] Failed to create the user table!");
                    return false;

                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            plugin.getLogger().info("[MySQL] Created the user table.");

        } else {

            // Table exists! We have to ensure the structure is up to date.
            ArrayList<String> columns = new ArrayList<>();

            try(ResultSet rs = query("show columns from `" + plugin.storagePrefix + "reportrts_user`")) {

                while(rs.next()) {
                    columns.add(rs.getString("Field"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            // If UID does not exist then chances are that the user has not migrated from ID to UID.
            if(!columns.contains("uid")) {

                if(columns.contains("id")) {
                    try(Statement stmt = db.createStatement()) {

                        stmt.execute("ALTER TABLE `" + plugin.storagePrefix + "reportrts_user` CHANGE COLUMN `id` `uid` int(10) "
                                + "UNSIGNED NOT NULL AUTO_INCREMENT FIRST , DROP PRIMARY KEY, ADD PRIMARY KEY (`uid`)");

                        plugin.getLogger().info("Migrated primary key of user table from `id` to `uid`.");

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }

        }

        // The ticket table doesn't exist, we need to create it.
        if(!tableExists(plugin.storagePrefix + "reportrts_ticket")) {

            if(tableExists(plugin.storagePrefix + "reportrts_request")) {

                // The old table exists, alter the table to make it compatible.
                try(Statement stmt = db.createStatement()) {

                    stmt.addBatch("RENAME TABLE `" + plugin.storagePrefix + "reportrts_request` TO `" + plugin.storagePrefix + "reportrts_ticket`;");

                    plugin.getLogger().info("Renamed request table to tickets.");

                    // Ensure that there are no null data in the table where the structure is changing.
                    stmt.addBatch("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `user_id` = '0' WHERE `user_id` IS NULL;");
                    stmt.addBatch("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `mod_id` = '0' WHERE `mod_id` IS NULL;");
                    stmt.addBatch("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `mod_timestamp` = '0' WHERE `mod_timestamp` IS NULL;");
                    stmt.addBatch("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `notified_of_completion` = '0' WHERE `notified_of_completion` IS NULL;");

                    stmt.addBatch("ALTER TABLE `" + plugin.storagePrefix + "reportrts_ticket` " +
                            "CHANGE COLUMN `user_id` `userId`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `id`, " +
                            "CHANGE COLUMN `mod_id` `staffId`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `userId`, " +
                            "CHANGE COLUMN `mod_timestamp` `staffTime`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `staffId`, " +
                            "CHANGE COLUMN `mod_comment` `comment`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `staffTime`, " +
                            "CHANGE COLUMN `tstamp` `timestamp`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `comment`, " +
                            "CHANGE COLUMN `bc_server` `server`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' AFTER `world`, " +
                            "CHANGE COLUMN `notified_of_completion` `notified`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 AFTER `status`;");

                    // Ensure that we remove the comment column in favor of the comment table.
                    stmt.addBatch("ALTER TABLE `" + plugin.storagePrefix + "reportrts_ticket` DROP COLUMN `comment`, MODIFY COLUMN `timestamp` int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `staffTime`;");

                    plugin.getLogger().info("Migrated ticket data to the new table structure.");

                    stmt.executeBatch();

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }

            } else {

                // Old table does not exist, create a new one.
                try(Statement stmt = db.createStatement()) {

                    if(stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + plugin.storagePrefix + "reportrts_ticket` (" +
                            "`id`  int(10) UNSIGNED NULL AUTO_INCREMENT ," +
                            "`userId`  int(10) UNSIGNED NOT NULL DEFAULT 0 ," +
                            "`staffId`  int(10) UNSIGNED NOT NULL DEFAULT 0 ," +
                            "`staffTime`  int(10) UNSIGNED NOT NULL DEFAULT 0 ," +
                            "`timestamp`  int(10) UNSIGNED NOT NULL DEFAULT 0 ," +
                            "`world`  varchar(255) NOT NULL DEFAULT '' ," +
                            "`server`  varchar(255) NOT NULL DEFAULT '' ," +
                            "`x`  int(10) NOT NULL DEFAULT 0 ," +
                            "`y`  int(10) NOT NULL DEFAULT 0 ," +
                            "`z`  int(10) NOT NULL DEFAULT 0 ," +
                            "`yaw`  smallint(6) NOT NULL DEFAULT 0 ," +
                            "`pitch`  smallint(6) NOT NULL DEFAULT 0 ," +
                            "`text`  varchar(255) NOT NULL DEFAULT '' ," +
                            "`status`  tinyint(1) UNSIGNED NULL ," +
                            "`notified`  tinyint(1) UNSIGNED NULL DEFAULT 0 ," +
                            "PRIMARY KEY (`id`))" +
                            "DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;") > 0) {

                        plugin.getLogger().warning("[MySQL] Failed to create the ticket table!");
                        return false;

                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }

            }

            plugin.getLogger().info("[MySQL] Created the ticket table.");

        } else {

            // Table exists! We have to ensure the structure is up to date.
            ArrayList<String> columns = new ArrayList<>();

            try(ResultSet rs = query("show columns from `" + plugin.storagePrefix + "reportrts_user`")) {

                while(rs.next()) {
                    columns.add(rs.getString("Field"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            if(!columns.contains("uuid")) {
                plugin.getLogger().severe("The UUID field is missing, your data is probably very old. Please run a older build of ReportRTS to migrate the data.");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return false;
            }

        }

        // Comment table doesn't exist, let's create it.
        if(!tableExists(plugin.storagePrefix + "reportrts_comment")) {

            try(Statement stmt = db.createStatement()) {

                if(stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + plugin.storagePrefix + "reportrts_comment` (" +
                        "`cid`int(11) UNSIGNED NOT NULL AUTO_INCREMENT, " +
                        "`name` varchar(255) NOT NULL, " +
                        "`timestamp` int(11) UNSIGNED NOT NULL, " +
                        "`comment`  varchar(255) NOT NULL, " +
                        "`ticket`  int(11) UNSIGNED NOT NULL, " +
                        "PRIMARY KEY (`id`))" +
                        "DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;") > 0) {

                    plugin.getLogger().warning("[MySQL] Failed to create the ticket table!");
                    return false;

                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            plugin.getLogger().info("[MySQL] Created the comment table.");

        }

        return true;
    }

    private boolean tableExists(String table) {

        try(Statement stmt = db.createStatement()) {
            stmt.executeQuery("SELECT * FROM " + table);
            return true;
        } catch(SQLException e) {
            return false;
        }

    }

    private boolean loadData() {

        Map<Integer, TreeSet<Comment>> comments = new HashMap<>();

        try(ResultSet rs = query("SELECT " +
                "`" + plugin.storagePrefix + "reportrts_comment`.ticket, " +
                        plugin.storagePrefix + "reportrts_comment.cid, " +
                        plugin.storagePrefix + "reportrts_comment.`name`, " +
                        plugin.storagePrefix + "reportrts_comment.`timestamp`, " +
                        plugin.storagePrefix + "reportrts_comment.`comment`, " +
                        plugin.storagePrefix + "reportrts_ticket.`status`, " +
                        plugin.storagePrefix + "reportrts_ticket.id FROM " +
                        plugin.storagePrefix + "reportrts_comment " +
                        "INNER JOIN " + plugin.storagePrefix + "reportrts_ticket ON " +
                        plugin.storagePrefix + "reportrts_comment.ticket = " +
                        plugin.storagePrefix + "reportrts_ticket.id WHERE " +
                        plugin.storagePrefix + "reportrts_ticket.`status` < 2 ORDER BY " +
                        plugin.storagePrefix + "reportrts_comment.`timestamp` ASC")) {

            while(rs.next()) {
                if(!comments.containsKey(rs.getInt(1))) comments.put(rs.getInt(1), new TreeSet<Comment>());
                TreeSet<Comment> commentSet = comments.get(rs.getInt(1));
                commentSet.add(new Comment(rs.getLong("timestamp"), rs.getInt("ticket"), rs.getInt("cid"), rs.getString("name"), rs.getString("comment")));
                comments.put(rs.getInt(1), commentSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // MySQL connected fine. Load tickets from database.
        try(ResultSet rs = query("SELECT * FROM " + plugin.storagePrefix + "reportrts_ticket as ticket INNER JOIN " +
                plugin.storagePrefix + "reportrts_user as user ON ticket.userId = user.uid WHERE ticket.status < 2")) {

            while(rs.next()) {

                Ticket ticket = new Ticket(
                        rs.getString("name"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt(1),
                        rs.getLong("timestamp"),
                        rs.getString("text"),
                        rs.getInt("status"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("yaw"),
                        rs.getInt("pitch"),
                        rs.getString("world"),
                        rs.getString("server")
                );

                // Attach comments if there are any.
                if(comments.containsKey(rs.getInt(1))) ticket.setComments(comments.get(rs.getInt(1)));

                if(rs.getInt("status") > 0) {
                    User staff = getUser(null, rs.getInt("staffId"), false);
                    ticket.setStaffName(staff.getUsername());
                    ticket.setStaffTime(rs.getLong("staffTime"));
                    ticket.setStaffUuid(staff.getUuid());
                    ticket.setNotified(rs.getBoolean("notified"));
                }
                plugin.tickets.put(rs.getInt(1), ticket);

            }

        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Load pending notifications.
        try(ResultSet rs = query("SELECT * FROM " + plugin.storagePrefix + "reportrts_ticket as ticket INNER JOIN " +
                plugin.storagePrefix + "reportrts_user as user ON ticket.userId = user.uid WHERE ticket.status = 3 AND ticket.notified = 0")) {

            while(rs.next()) {
                plugin.notifications.put(rs.getInt("id"), UUID.fromString(rs.getString("uuid")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean resetNotifications() {

        try(Statement stmt = db.createStatement()) {

            return stmt.executeUpdate("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `notified` = 1 WHERE `notified` = 0 AND `status` = 3") > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int createUser(String username) {
        /** This method is only used for creating the Console user at this time. **/

        if(!connected) return 0;
        int id;

        try(PreparedStatement stmt = db.prepareStatement("INSERT INTO `" + plugin.storagePrefix + "reportrts_user` " +
                "(`name`, `uuid`, `banned`) VALUES (?, ?, '0')", Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, UUID.randomUUID().toString());
            // Statement didn't run, return 0.
            if(stmt.executeUpdate() < 1) return 0;
            ResultSet rs = stmt.getGeneratedKeys();
            rs.first();
            id = rs.getInt(1);
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        return id;
    }

    @Override
    public int createUser(UUID uuid) {

        if(!connected) return 0;
        int id;

        Player player = plugin.getServer().getPlayer(uuid);

        // User is not online. Simply return 0.
        if(player == null) return 0;

        try(PreparedStatement stmt = db.prepareStatement("INSERT INTO `" + plugin.storagePrefix +
                        "reportrts_user` (`name`, `uuid`, `banned`) VALUES (?, ?, '0')",
                        Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, player.getName());
            stmt.setString(2, uuid.toString());
            // Statement didn't run. Return 0.
            if(stmt.executeUpdate() < 1) return 0;
            ResultSet rs = stmt.getGeneratedKeys();
            rs.first();
            id = rs.getInt(1);
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        return id;
    }

    @Override
    public int createComment(String name, long timestamp, String comment, int ticketId) {

        if(!isLoaded()) return 0;

        long current = System.currentTimeMillis() / 1000;

        if(timestamp > current || (current - 120) > timestamp || ticketId < 1) return 0;

        try(PreparedStatement ps = db.prepareStatement("INSERT INTO `" + plugin.storagePrefix + "reportrts_comment` (`name`, `timestamp`, `comment`, `ticket`) " +
        "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setLong(2, timestamp);
            ps.setString(3, comment);
            ps.setInt(4, ticketId);

            int result = ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if(!rs.first()) return 0;

            int keys = rs.getInt(1);

            return result < 1 ? -1 : keys;

        } catch(SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int createTicket(User user, Location location, String message) {

        if(!isLoaded()) return 0;

        // User does not exist, so we need to create it.
        if(user.getId() == 0) user = getUser(user.getUuid(), 0, true);

        try(PreparedStatement ps = db.prepareStatement("INSERT INTO `" + plugin.storagePrefix + "reportrts_ticket` (`userId`, `timestamp`, " +
                "`world`, `x`, `y`, `z`, `yaw`, `pitch`, `text`, `status`, `notified`, `server`) VALUES" +
                " (?, ?, ?, ?, ?, ?, ?, ?, ?, '0', '0', ?)", Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, user.getId());
            ps.setLong(2, System.currentTimeMillis() / 1000);
            ps.setString(3, location.getWorld().getName());
            ps.setDouble(4, location.getX());
            ps.setDouble(5, location.getY());
            ps.setDouble(6, location.getZ());
            ps.setDouble(7, location.getYaw());
            ps.setDouble(8, location.getPitch());
            ps.setString(9, message);
            ps.setString(10, BungeeCord.getServer());

            int result = ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if(!rs.first()) return 0;

            int keys = rs.getInt(1);

            rs.close();

            return result < 1 ? -1 : keys;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int countTickets(int status) {

        int total = 0;

        try(ResultSet rs = query("SELECT COUNT(`id`) FROM `" + plugin.storagePrefix + "reportrts_ticket` WHERE `status` = '" + status + "'")) {

            if(!rs.next()) {
                plugin.getLogger().warning("Failed to count tickets of status " + status);
                return 0;
            }

            total = rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    @Override
    public User getUser(UUID uuid, int id, boolean create) {

        // Check if the user exists in the UserCache and return that instead.
        if(uuid != null && userCache.containsKey(uuid)) return userCache.get(uuid);

        User user = new User();

        if(uuid == null && id > 0) {
            try(ResultSet rs = query("SELECT * FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `uid` = " + id)) {

                // No hits and we can't create a user because there is no UUID.
                if(!rs.next()) return null;

                user.setId(id);
                user.setUsername(rs.getString("name"));
                user.setUuid(UUID.fromString(rs.getString("uuid")));
                user.setBanned(rs.getBoolean("banned"));

                uuid = user.getUuid();

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        // Ensure that a UUID exists before we attempt to run a query.
        if(uuid == null) return null;

        user.setUuid(uuid);

        try(PreparedStatement stmt = db.prepareStatement("SELECT * FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `uuid` = ?")) {

            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            // No hits!
            if(!rs.next()) {
                // Check if we want to create the user or not.
                if(!create) return null;
                rs.close();
                // Store the ID of the created user.
                int userId = createUser(uuid);
                // User was not created if the ID is 0.
                if(userId == 0) return null;
                Statement statement = db.createStatement();
                rs = statement.executeQuery("SELECT * FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `uid` = " + userId);
                // Check if there is any result.
                if(!rs.next()) return null;
            }

            user.setUsername(rs.getString("name"));
            user.setBanned(rs.getBoolean("banned"));
            user.setId(rs.getInt("uid"));

            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // Store user in UserCache to save future queries. TODO: Remember to add expiry.
        if(!userCache.containsKey(uuid)) userCache.put(uuid, user);

        return user;
    }

    @Override
    public User getUnsafeUser(String name) {

        User user = new User();

        if(name == null || name.length() < 1) return null;

        // Check if name is console.
        if(name.equalsIgnoreCase("CONSOLE")) return getConsole();

        try(PreparedStatement stmt = db.prepareStatement("SELECT * FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `name` = ?")) {

            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) return null;

            user.setId(rs.getInt("uid"));
            user.setUsername(name);
            user.setUuid(UUID.fromString(rs.getString("uuid")));
            user.setBanned(rs.getBoolean("banned"));

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // Store the user in the UserCache for next time.
        if(!userCache.containsKey(user.getUuid())) userCache.put(user.getUuid(), user);

        return user;
    }

    @Override
    public User getConsole() {

        // The console User class is not loaded yet.
        if(this.console == null) {

            User console = new User();

            try(ResultSet rs = query("SELECT * FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `name` = '" + plugin.getServer().getConsoleSender().getName() + "'")) {

                // No hits!
                if(!rs.next()) {
                    // Create console entry.
                    createUser(plugin.getServer().getConsoleSender().getName());
                    ResultSet rs1 = query("SELECT * FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `name` = '" + plugin.getServer().getConsoleSender().getName() + "'");
                    if(!rs1.next()) {
                        // Creation have failed. Log this and return null.
                        plugin.getLogger().severe("Failed to create a entry for Console in the RTS user table.");
                        return null;
                    }
                    console.setId(rs1.getInt("uid"));
                    console.setUsername(plugin.getServer().getConsoleSender().getName());
                    console.setBanned(false);
                    console.setUuid(UUID.fromString(rs1.getString("uuid")));

                    rs1.close();

                } else {

                    console.setId(rs.getInt("uid"));
                    console.setUsername(plugin.getServer().getConsoleSender().getName());
                    console.setBanned(false);
                    console.setUuid(UUID.fromString(rs.getString("uuid")));

                }

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
            this.console = console;
        }
        // Console has been initialized so we can return it's User.
        return console;
    }

    @Override
    public TreeSet<Comment> getComments(int ticketId) {

        if(ticketId < 1) return null;

        TreeSet<Comment> comments = new TreeSet<>();

        try(ResultSet rs = query("SELECT * FROM " + plugin.storagePrefix + "reportrts_comment WHERE `ticket` = " + ticketId)) {

            while(rs.next()) {
                comments.add(new Comment(rs.getLong("timestamp"), rs.getInt("ticket"), rs.getInt("cid"), rs.getString("name"), rs.getString("comment")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return comments;
    }

    @Override
    public HashMap<Integer, TreeSet<Comment>> getAllComments(int status) {

        if(status < 0 || status > 3) return null;

        HashMap<Integer, TreeSet<Comment>> comments = new HashMap<>();

        try(ResultSet rs = query("SELECT " +
                "`" + plugin.storagePrefix + "reportrts_comment`.ticket, " +
                plugin.storagePrefix + "reportrts_comment.cid, " +
                plugin.storagePrefix + "reportrts_comment.`name`, " +
                plugin.storagePrefix + "reportrts_comment.`timestamp`, " +
                plugin.storagePrefix + "reportrts_comment.`comment`, " +
                plugin.storagePrefix + "reportrts_ticket.`status`, " +
                plugin.storagePrefix + "reportrts_ticket.id FROM " +
                plugin.storagePrefix + "reportrts_comment " +
                "INNER JOIN " + plugin.storagePrefix + "reportrts_ticket ON " +
                plugin.storagePrefix + "reportrts_comment.ticket = " +
                plugin.storagePrefix + "reportrts_ticket.id WHERE " +
                plugin.storagePrefix + "reportrts_ticket.`status` < " + status + " ORDER BY " +
                plugin.storagePrefix + "reportrts_comment.`timestamp` ASC")) {

            while(rs.next()) {
                if(!comments.containsKey(rs.getInt(1))) comments.put(rs.getInt(1), new TreeSet<Comment>());
                TreeSet<Comment> commentSet = comments.get(rs.getInt(1));
                commentSet.add(new Comment(rs.getLong("timestamp"), rs.getInt("ticket"), rs.getInt("cid"), rs.getString("name"), rs.getString("comment")));
                comments.put(rs.getInt(1), commentSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return comments;
    }

    @Override
    public LinkedHashMap<Integer, Ticket> getTickets(int status, int cursor, int limit) {

        if(status < 1 || cursor < 0 || limit < 0) return null;

        LinkedHashMap<Integer, Ticket> tickets = new LinkedHashMap<>();

        try(ResultSet rs = query("SELECT * FROM " + plugin.storagePrefix + "reportrts_ticket as ticket INNER JOIN " +
                plugin.storagePrefix + "reportrts_user as user ON ticket.userId = user.uid WHERE ticket.status = " + status + " ORDER BY ticket.id " + (status == 3 ? "ASC" : "DESC") + " LIMIT " + cursor + ", " + limit)) {

            while(rs.next()) {

                Ticket ticket = new Ticket(
                        rs.getString("name"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt(1),
                        rs.getLong("timestamp"),
                        rs.getString("text"),
                        rs.getInt("status"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("yaw"),
                        rs.getInt("pitch"),
                        rs.getString("world"),
                        rs.getString("server")
                );

                if(rs.getInt("status") > 0) {
                    User staff = getUser(null, rs.getInt("staffId"), false);
                    ticket.setStaffName(staff.getUsername());
                    ticket.setStaffTime(rs.getLong("staffTime"));
                    ticket.setStaffUuid(staff.getUuid());
                    ticket.setNotified(rs.getBoolean("notified"));
                }

                tickets.put(rs.getInt(1), ticket);

            }

        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
        return tickets;
    }

    @Override
    public LinkedHashMap<Integer, Ticket> getTickets(int status) {

        if(status < 0) return null;

        LinkedHashMap<Integer, Ticket> tickets = new LinkedHashMap<>();

        try(ResultSet rs = query("SELECT * FROM " + plugin.storagePrefix + "reportrts_ticket as ticket INNER JOIN " +
                plugin.storagePrefix + "reportrts_user as user ON ticket.userId = user.uid WHERE ticket.status = " + status + " ORDER BY ticket.id " + (status == 3 ? "ASC" : "DESC"))) {

            while(rs.next()) {

                Ticket ticket = new Ticket(
                        rs.getString("name"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt(1),
                        rs.getLong("timestamp"),
                        rs.getString("text"),
                        rs.getInt("status"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("yaw"),
                        rs.getInt("pitch"),
                        rs.getString("world"),
                        rs.getString("server")
                );

                if(rs.getInt("status") > 0) {
                    User staff = getUser(null, rs.getInt("staffId"), false);
                    ticket.setStaffName(staff.getUsername());
                    ticket.setStaffTime(rs.getLong("staffTime"));
                    ticket.setStaffUuid(staff.getUuid());
                    ticket.setNotified(rs.getBoolean("notified"));
                }

                tickets.put(rs.getInt(1), ticket);

            }

        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
        return tickets;
    }

    @Override
    public Ticket getTicket(int id) {

        TreeSet<Comment> comments = getComments(id);

        try(ResultSet rs = query("SELECT * FROM `" + plugin.storagePrefix + "reportrts_ticket` as ticket INNER JOIN `" + plugin.storagePrefix
        + "reportrts_user` as user ON ticket.userId = user.uid WHERE ticket.id = '" + id + "'")) {

            if(!rs.next()) return null;

            Ticket ticket = new Ticket(
                    rs.getString("name"),
                    UUID.fromString(rs.getString("uuid")),
                    rs.getInt("id"),
                    rs.getLong("timestamp"),
                    rs.getString("text"),
                    rs.getInt("status"),
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"),
                    rs.getFloat("yaw"),
                    rs.getFloat("pitch"),
                    rs.getString("world"),
                    rs.getString("server")
            );

            if(comments.size() > 0) ticket.setComments(comments);

            if(rs.getInt("notified") > 0) ticket.setNotified(true);

            return ticket;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public LinkedHashMap<Integer, Ticket> getHandledBy(UUID uuid, int cursor, int limit) {
        return this.getTicketsBy(uuid, cursor, limit, false);
    }

    @Override
    public LinkedHashMap<Integer, Ticket> getOpenedBy(UUID uuid, int cursor, int limit) {
        return this.getTicketsBy(uuid, cursor, limit, true);
    }

    private LinkedHashMap<Integer, Ticket> getTicketsBy(UUID uuid, int cursor, int limit, boolean creator) {

        // Limit has to be 1 or above.
        if(limit < 1) return null;

        User user = getUser(uuid, 0, false);

        if(user == null) return null;

        LinkedHashMap<Integer, Ticket> tickets = new LinkedHashMap<>();

        if(creator) {

            // Get tickets opened by a player.
            try(PreparedStatement ps = db.prepareStatement("SELECT * FROM `" + plugin.storagePrefix + "reportrts_ticket` as ticket INNER JOIN `" + plugin.storagePrefix + "reportrts_user` as user " +
                    "ON ticket.userId = user.uid WHERE ticket.userId = ? ORDER BY ticket.timestamp DESC LIMIT ?, ?")) {

                ps.setInt(1, user.getId());
                ps.setInt(2, cursor);
                ps.setInt(3, limit);

                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    Ticket ticket = new Ticket(
                            rs.getString("name"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getInt("id"),
                            rs.getLong("timestamp"),
                            rs.getString("text"),
                            rs.getInt("status"),
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch"),
                            rs.getString("world"),
                            rs.getString("server")
                    );

                    if(rs.getInt("notified") > 0) ticket.setNotified(true);

                    tickets.put(ticket.getId(), ticket);
                }

                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else {

            // Get tickets handled by a player.
            try(PreparedStatement ps = db.prepareStatement("SELECT * FROM `" + plugin.storagePrefix + "reportrts_ticket` as ticket INNER JOIN `" + plugin.storagePrefix + "reportrts_user` as user " +
                    "ON ticket.userId = user.uid WHERE ticket.staffId = ? ORDER BY ticket.staffTime DESC LIMIT ?, ?")) {

                ps.setInt(1, user.getId());
                ps.setInt(2, cursor);
                ps.setInt(3, limit);

                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    Ticket ticket = new Ticket(
                            rs.getString("name"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getInt("id"),
                            rs.getLong("timestamp"),
                            rs.getString("text"),
                            rs.getInt("status"),
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch"),
                            rs.getString("world"),
                            rs.getString("server")
                    );

                    if(rs.getInt("notified") > 0) ticket.setNotified(true);

                    tickets.put(ticket.getId(), ticket);
                }

                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        }
        return tickets;
    }

    @Override
    public LinkedHashMap<String, Integer> getTop(int limit) {

        // Limit has to be 1 or above.
        if(limit < 1) return null;

        LinkedHashMap<String, Integer> results = new LinkedHashMap<>();

        try(ResultSet rs = query("SELECT `reportrts_user`.name, COUNT(`reportrts_ticket`.staffId) AS tickets FROM `reportrts_ticket` " +
                "LEFT JOIN `reportrts_user` ON `reportrts_ticket`.staffId = `reportrts_user`.uid WHERE `reportrts_ticket`.status = 3 " +
                    "GROUP BY `name` ORDER BY tickets DESC LIMIT " + limit)) {

            while(rs.next()) {
                results.put(rs.getString("name"), rs.getInt("tickets"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }

    @Override
    public int setTicketStatus(int id, UUID uuid, String username, int status, String comment, boolean notified, long timestamp) {

        if(!isLoaded()) return 0;

        Ticket ticket;

        if(!plugin.tickets.containsKey(id)) {
            ticket = getTicket(id);
        } else {
            ticket = plugin.tickets.get(id);
        }

        // Store the staff performing the command and create him/her if he/she does not exist.
        User staff = getUser(uuid, 0, true);
        if(staff == null) return -1;

        if(ticket == null) return -3;

        // Make sure tickets do not clash.
        if(ticket.getStatus() == status || (status == 2 && ticket.getStatus() == 3)) return -2;

        try(PreparedStatement ps = db.prepareStatement("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `status` = ?, staffId = ?, staffTime = ?, comment = ?, notified = ? WHERE `id` = ?")) {

            ps.setInt(1, status);
            ps.setInt(2, staff.getId());
            ps.setLong(3, timestamp);
            ps.setString(4, comment);
            ps.setInt(5, notified ? 1 : 0);
            ps.setInt(6, id);

            // Check if any rows were affected.
            if(ps.executeUpdate() < 1) return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int setNotificationStatus(int ticketId, boolean status) {

        if(!isLoaded()) return 0;

        try(Statement stmt = db.createStatement()) {

            // Return affected rows.
            return stmt.executeUpdate("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `notified` = '" + (status ? 1 : 0) + "' WHERE `id` = '" + ticketId + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int setUserStatus(UUID uuid, boolean status) {

        if(!isLoaded()) return 0;

        try(Statement stmt = db.createStatement()) {

            int result = stmt.executeUpdate("UPDATE `" + plugin.storagePrefix + "reportrts_user` SET `banned` = '" + (status ? 1 : 0) + "' WHERE `uuid` = '" + uuid.toString() + "'");

            if(result > 0) {

                if(userCache.containsKey(uuid)) {
                    User user = userCache.get(uuid);
                    user.setBanned(status);

                    userCache.put(uuid, user);
                }

            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void deleteTicket(int ticketId) {

        if(!isLoaded()) return;

        try(ResultSet rs = query("DELETE FROM `" + plugin.storagePrefix + "` WHERE `id` = '" + ticketId + "'")) {

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}