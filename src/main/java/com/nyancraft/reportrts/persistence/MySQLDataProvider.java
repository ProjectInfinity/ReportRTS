package com.nyancraft.reportrts.persistence;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.data.User;

import com.nyancraft.reportrts.util.BungeeCord;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.UUID;

public class MySQLDataProvider implements DataProvider {

    private ReportRTS plugin;

    private Connection db;

    private boolean connected;

    private User console;

    private int taskId;

    public MySQLDataProvider(ReportRTS plugin) {
        this.plugin = plugin;
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

        // TODO: Check tables.  ALSO mod_comment => comment.

        return loadData();
    }

    private boolean loadData() {

        // MySQL connected fine. Load tickets from database.
        try(ResultSet rs = query("SELECT * FROM " + plugin.storagePrefix + "reportrts_request as request INNER JOIN " +
                plugin.storagePrefix + "reportrts_user as user ON request.user_id = user.id WHERE `status` < 2")) {

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
                        rs.getString("bc_server"),
                        rs.getString("comment")
                );

                if(rs.getInt("status") > 0) {
                    User staff = getUser(null, rs.getInt("staffId"), false);
                    ticket.setModName(staff.getUsername());
                    ticket.setModTimestamp(rs.getLong("staffTime"));
                    ticket.setModUUID(staff.getUuid());
                    ticket.setModComment(rs.getString("comment"));
                    ticket.setNotified(rs.getBoolean("notified"));
                    if(ticket.getStatus() == 3 && !ticket.isNotified()) {
                        plugin.notifications.put(ticket.getId(), ticket.getUUID());
                    }
                }
                plugin.tickets.put(rs.getInt(1), ticket);

            }

        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean resetNotifications() {

        try(Statement stmt = db.createStatement()) {

            return stmt.executeUpdate("UPDATE `" + plugin.storagePrefix + "reportrts_ticket` SET `notified` = 1 WHERE `notified` = 0") > 0;

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
            id = stmt.getGeneratedKeys().getInt(1);

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
            id = stmt.getGeneratedKeys().getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        return id;
    }

    @Override
    public int createTicket(User user, Location location, String message) {

        if(!isLoaded()) return 0;

        // User does not exist, so we need to create it.
        // TODO: Figure out if this need a workaround for console.
        if(user.getId() == 0) user = getUser(user.getUuid(), 0, true);

        // TODO: Ensure that database fields are correct.
        try(PreparedStatement ps = db.prepareStatement("INSERT INTO `" + plugin.storagePrefix + "reportrts_ticket` (`userId`, `timestamp`, " +
                "`world`, `x`, `y`, `z`, `yaw`, `pitch`, `text`, `status`, `notified`, `bc_server`) VALUES" +
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

            return ps.executeUpdate() < 1 ? -1 : ps.getGeneratedKeys().getInt(1);

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

        User user = new User();

        if(uuid == null && id > 0) {
            try(ResultSet rs = query("SELECT * FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `id` = " + id)) {

                // No hits and we can't create a user because there is no UUID.
                if(!rs.next()) return null;

                user.setId(id);
                user.setUsername(rs.getString("name"));
                user.setUuid(UUID.fromString(rs.getString("uuid")));
                user.setBanned(rs.getBoolean("banned"));

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
                // TODO: Figure out if Console needs workarounds.
                createUser(uuid);
            }

            user.setUsername(rs.getString("name"));
            user.setBanned(rs.getBoolean("banned"));
            user.setId(rs.getInt("id"));

            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
                    console.setId(rs.getInt("id"));
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
    public LinkedHashMap<Integer, Ticket> getTickets(int status, int cursor, int limit) {

        if(status < 1 || cursor < 0 || limit < 0) return null;

        LinkedHashMap<Integer, Ticket> tickets = new LinkedHashMap<>();

        try(ResultSet rs = query("SELECT * FROM " + plugin.storagePrefix + "reportrts_ticket as ticket INNER JOIN " +
                plugin.storagePrefix + "reportrts_user as user ON ticket.user_id = user.id WHERE ticket.status = " + status + " ORDER BY ticket.id " + (status == 3 ? "ASC" : "DESC") + " LIMIT " + cursor + ", " + limit)) {

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
                        rs.getString("bc_server"),
                        rs.getString("comment")
                );

                if(rs.getInt("status") > 0) {
                    User staff = getUser(null, rs.getInt("staffId"), false);
                    ticket.setModName(staff.getUsername());
                    ticket.setModTimestamp(rs.getLong("staffTime"));
                    ticket.setModUUID(staff.getUuid());
                    ticket.setModComment(rs.getString("comment"));
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
                plugin.storagePrefix + "reportrts_user as user ON ticket.user_id = user.id WHERE ticket.status = " + status + " ORDER BY ticket.id " + (status == 3 ? "ASC" : "DESC"))) {

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
                        rs.getString("bc_server"),
                        rs.getString("comment")
                );

                if(rs.getInt("status") > 0) {
                    User staff = getUser(null, rs.getInt("staffId"), false);
                    ticket.setModName(staff.getUsername());
                    ticket.setModTimestamp(rs.getLong("staffTime"));
                    ticket.setModUUID(staff.getUuid());
                    ticket.setModComment(rs.getString("comment"));
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

        try(ResultSet rs = query("SELECT * FROM `" + plugin.storagePrefix + "reportrts_ticket` as ticket INNER JOIN `" + plugin.storagePrefix
        + "reportrts_user` as user ON ticket.userId = user.id WHERE ticket.id = '" + id + "'")) {

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
                    rs.getString("bc_server"),
                    rs.getString("comment")
            );

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
            try(PreparedStatement ps = db.prepareStatement("SELECT * FROM `" + plugin.storagePrefix + "reportrts_ticket` as ticket INNER JOIN `" + plugin.storagePrefix + "reportrts_user as user " +
                    "ON ticket.userId = user.id WHERE ticket.userId = ? ORDER BY ticket.timestamp DESC LIMIT ?, ?")) {

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
                            rs.getString("bc_server"),
                            rs.getString("comment")
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
            try(PreparedStatement ps = db.prepareStatement("SELECT * FROM `" + plugin.storagePrefix + "reportrts_ticket` as ticket INNER JOIN `" + plugin.storagePrefix + "reportrts_user as user " +
                    "ON ticket.userId = user.id WHERE ticket.staffId = ? ORDER BY ticket.staffTime DESC LIMIT ?, ?")) {

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
                            rs.getString("bc_server"),
                            rs.getString("comment")
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

            results.put(rs.getString("name"), rs.getInt("tickets"));

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

            return stmt.executeUpdate("UPDATE `" + plugin.storagePrefix + "reportrts_user` SET `notified` = '" + (status ? 1 : 0) + "' WHERE `uuid` = '" + uuid.toString() + "'");

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