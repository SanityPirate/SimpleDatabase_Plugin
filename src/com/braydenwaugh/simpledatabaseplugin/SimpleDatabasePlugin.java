package com.braydenwaugh.simpledatabaseplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SimpleDatabasePlugin for Minecraft Spigot Server
**/

public class SimpleDatabasePlugin extends JavaPlugin {

    // Database connection variables
    private Connection connection;
    private String host, database, username, password;
    private int port;

    // Variables used for SQL statements and player commands
    private Player player;
    private String playerName;
    private String homeName;
    private int coordinateX;
    private int coordinateY;
    private int coordinateZ;

    // Used to store results from list home query
    private List<String> playersHomes = new ArrayList<>();

    // Used to store results from finding specific home query
    private int destinationX;
    private int destinationY;
    private int destinationZ;

    // Check that load commands have been run before allowing data retrieval commands
    private boolean isListHomeLoaded = false;
    private boolean isFindHomeLoaded = false;

    // Config File
    private FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        // Used for database connection - configure config.yml.
        host = config.getString("host");
        port = config.getInt("port");
        database = config.getString("database");
        username = config.getString("username");
        password = config.getString("password");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Test command to make sure plugin is loaded/functioning - console broadcasts a message telling the player hi
        if (command.getName().equalsIgnoreCase("sayhi")) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "Hi, " + sender.getName() + "!");
            return true;
        }
        // /sethome - Gets the player's name, the name of the home, their current coordinates, and sends them to the DB
        if (command.getName().equalsIgnoreCase("sethome")) {
            if (args.length == 1) {
                player = Bukkit.getPlayer(sender.getName());
                playerName = sender.getName();
                homeName = args[0];
                Location currentLocation = player.getLocation();
                coordinateX = currentLocation.getBlockX();
                coordinateY = currentLocation.getBlockY();
                coordinateZ = currentLocation.getBlockZ();
                sender.sendMessage(ChatColor.GOLD + "Home " + homeName + " at X: " + coordinateX + " Y: " + coordinateY + " Z: " + coordinateZ + " set successfully!");

                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            openConnection();
                            Statement statement = connection.createStatement();
                            statement.executeUpdate("INSERT INTO Homes (PlayerName, HomeName, Coordinate_X, Coordinate_Y, Coordinate_Z) VALUES ('" + playerName + "', '" + homeName + "', " + coordinateX + ", " + coordinateY + ", " + coordinateZ + ");");

                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }
                };

                runnable.runTaskAsynchronously(this);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Please specify a name for the home.");
                return false;
            }
        }
        // /findhome - Lists all of the homes the player has created.
        if (command.getName().equalsIgnoreCase("findhome")) {
            if (args.length == 0) {
                if (isListHomeLoaded) {
                    sender.sendMessage(ChatColor.GOLD + "" + Arrays.toString(playersHomes.toArray()));
                    playersHomes.clear();
                    isListHomeLoaded = false;
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Please use '/findhome load' before using this command!");
                    return false;
                }
            } else if (args.length == 1) {
                // /findhome load - Gets the player's homes from the DB and puts them in an ArrayList for access.
                if (args[0].equalsIgnoreCase("load")) {
                    playerName = sender.getName();
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                openConnection();
                                Statement statement = connection.createStatement();
                                ResultSet result = statement.executeQuery("SELECT * FROM Homes WHERE PlayerName ='" + playerName + "';");
                                while (result.next()) {
                                    String name = result.getString("HomeName");
                                    playersHomes.add(name);
                                }
                            } catch (ClassNotFoundException | SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    runnable.runTaskAsynchronously(this);
                    isListHomeLoaded = true;
                    sender.sendMessage(ChatColor.GOLD + "List of Homes has been loaded.");
                } else {
                    // /findhome [HomeName] - Returns the coordinates of a home a player has set.
                    if (isFindHomeLoaded) {
                        sender.sendMessage(ChatColor.GREEN + "" + homeName + ChatColor.WHITE + " is located at " + ChatColor.GREEN + "X: " + destinationX + " Y: " + destinationY + " Z: " + destinationZ);
                        isFindHomeLoaded = false;
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Please use '/findhome [HomeName]' load before using this command!");
                        return false;
                    }
                }
                return true;

            } else if (args.length == 2) {
                // /findhome load - Gets the coordinates of the specified home from the DB and stores in variables for access.
                if (args[1].equalsIgnoreCase("load")) {
                    homeName = args[0];
                    playerName = sender.getName();

                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                openConnection();
                                Statement statement = connection.createStatement();
                                ResultSet result = statement.executeQuery("SELECT * FROM Homes WHERE HomeName = '" + homeName + "' AND PlayerName = '" + playerName + "';");
                                result.next();
                                destinationX = result.getInt("Coordinate_X");
                                destinationY = result.getInt("Coordinate_Y");
                                destinationZ = result.getInt("Coordinate_Z");
                            } catch (ClassNotFoundException | SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    runnable.runTaskAsynchronously(this);
                    isFindHomeLoaded = true;
                    sender.sendMessage(ChatColor.GOLD + "Coordinates have been loaded.");
                    return true;
                }
            }
        }
        // /delhome [HomeName] - Deletes the specified home from the database
        if (command.getName().equalsIgnoreCase("delhome")) {
            if (args.length == 1) {
                    homeName = args[0];
                    playerName = sender.getName();

                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            openConnection();
                            Statement statement = connection.createStatement();
                            statement.executeUpdate("DELETE FROM Homes WHERE HomeName = '" + homeName + "' AND PlayerName = '" + playerName + "';");
                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }
                };

                runnable.runTaskAsynchronously(this);

                sender.sendMessage(ChatColor.RED + "Deleted " + ChatColor.WHITE + homeName + ".");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Please specify the home to be deleted.");
                return false;
            }
        }
        return false;
    }
    // Used for DB connection
    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password + "?autoReconnect=true");
        }
    }
}