package de.rexituz.mjoelnir.sqlite;

import com.velocitypowered.api.proxy.Player;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;

public class DatabaseHandler {
    static Connection connection = null;

    public static void connect() {
        try {
            String url = "jdbc:sqlite:./../../../local/database/gamenight.db";
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);

            if(connection == null) {
                throw new SQLException("Connection to Database could not be established!");
            }

            System.out.println("Connection to Database was successful!");
            initTables();
            System.out.println("Table creation was successful!");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initTables() throws SQLException {
        Statement statement = connection.createStatement();

        String createBanned = "CREATE TABLE IF NOT EXISTS bannedPlayers(uuid TEXT PRIMARY KEY);";
        statement.execute(createBanned);

        String creatTimeout = "CREATE TABLE IF NOT EXISTS timeoutPlayers(uuid TEXT PRIMARY KEY, until INTEGER);";
        statement.execute(creatTimeout);

        String creatUsers = "CREATE TABLE IF NOT EXISTS users(uuid TEXT PRIMARY KEY, username TEXT);";
        statement.execute(creatUsers);
    }

    public static void updateUser(Player player) {
        String sql = "REPLACE INTO users(uuid, username) VALUES (?, ?);";

        String uuid = player.getUniqueId().toString();
        String username = player.getUsername();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean banFromUsername(String name) {
        String sql = "INSERT OR IGNORE INTO bannedPlayers(uuid) VALUES (?);";
        return executeUpdateWithName(name, sql);
    }

    private static boolean executeUpdateWithName(String name, String sql) {
        String uuid = getUUID(name);

        if(uuid == null) return false;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isPlayerBanned(Player player) {
        String uuid = player.getUniqueId().toString();
        String sql = "SELECT uuid FROM bannedPlayers WHERE uuid = ?;";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            ResultSet resultSet  = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isPlayerTimeout(Player player) {
        String uuid = player.getUniqueId().toString();
        String sql = "SELECT uuid, until FROM timeoutPlayers WHERE uuid = ?;";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            ResultSet resultSet  = preparedStatement.executeQuery();

            if(!resultSet.next()) {
                return false;
            }

            long time = Instant.now().getEpochSecond();
            long until = resultSet.getLong("until");

            if(time >= until) {
                // Timeout over
                removeTimeoutFromPlayer(player);
                return false;
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static long getPlayerTimeoutUntil(String uuid) {
        String sql = "SELECT until FROM timeoutPlayers WHERE uuid = ?;";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            ResultSet resultSet  = preparedStatement.executeQuery();

            if(resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void removeTimeoutFromPlayer(Player player) {
        String uuid = player.getUniqueId().toString();
        String sql = "DELETE FROM timeoutPlayers WHERE uuid = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean removeTimeoutFromUsername(String name) {
        String sql = "DELETE FROM timeoutPlayers WHERE uuid = ?";
        return removeFromUsername(name, sql);
    }

    public static boolean removeBanFromUsername(String name) {
        String sql = "DELETE FROM bannedPlayers WHERE uuid = ?";
        return removeFromUsername(name, sql);
    }

    private static boolean removeFromUsername(String name, String sql) {
        return executeUpdateWithName(name, sql);
    }

    public static String getUUID(String name) {
        String sql = "SELECT uuid FROM users WHERE username = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                return resultSet.getString("uuid");
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static ArrayList<String> getAllKnownUsernames() {
        String sql = "SELECT username FROM users;";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet  = preparedStatement.executeQuery();

            ArrayList<String> usernames = new ArrayList<>();

            while (resultSet.next()) {
                usernames.add(resultSet.getString("username"));
            }

            return usernames;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static boolean timeoutUsername(String name, int min) {
        String sql = "REPLACE INTO timeoutPlayers(uuid, until) VALUES (?, ?);";
        String uuid = getUUID(name);

        if(uuid == null) return false;

        long until = Instant.now().plusSeconds(min* 60L).getEpochSecond();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            preparedStatement.setLong(2, until);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
