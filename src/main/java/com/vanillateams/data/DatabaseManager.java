package com.vanillateams.data;

import com.vanillateams.VanillaTeams;
import com.vanillateams.model.Team;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    private final VanillaTeams plugin;
    private Connection connection;

    public DatabaseManager(VanillaTeams plugin) {
        this.plugin = plugin;
    }

    public synchronized void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS teams (
                        name TEXT PRIMARY KEY COLLATE NOCASE,
                        leader_uuid TEXT NOT NULL,
                        leader_name TEXT NOT NULL,
                        pvp_enabled INTEGER NOT NULL DEFAULT 1,
                        ally_team TEXT DEFAULT NULL
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS team_members (
                        uuid TEXT PRIMARY KEY,
                        username TEXT NOT NULL,
                        team_name TEXT NOT NULL COLLATE NOCASE,
                        role TEXT NOT NULL,
                        FOREIGN KEY (team_name) REFERENCES teams(name) ON DELETE CASCADE
                    );
                """);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database", e);
        }
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close database connection", e);
        }
    }

    public synchronized List<Team> loadAllTeams() {
        List<Team> teams = new ArrayList<>();
        Map<String, Team> teamMap = new LinkedHashMap<>();

        String queryTeams = "SELECT name, leader_uuid, leader_name, pvp_enabled, ally_team FROM teams;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(queryTeams)) {

            while (rs.next()) {
                String name = rs.getString("name");
                UUID leaderUuid = UUID.fromString(rs.getString("leader_uuid"));
                String leaderName = rs.getString("leader_name");
                boolean pvpEnabled = rs.getInt("pvp_enabled") == 1;
                String allyTeam = rs.getString("ally_team");

                Team team = new Team(name, leaderUuid, leaderName, pvpEnabled);
                team.setAllyTeam(allyTeam);
                teamMap.put(name.toLowerCase(), team);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load teams from database", e);
            return teams;
        }

        String queryMembers = "SELECT uuid, username, team_name FROM team_members;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(queryMembers)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String username = rs.getString("username");
                String teamName = rs.getString("team_name").toLowerCase();

                Team team = teamMap.get(teamName);
                if (team != null) {
                    team.addMember(uuid, username);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load team members from database", e);
        }

        teams.addAll(teamMap.values());
        return teams;
    }

    public synchronized void createTeam(Team team) {
        String insertTeam = """
            INSERT INTO teams (name, leader_uuid, leader_name, pvp_enabled, ally_team)
            VALUES (?, ?, ?, ?, ?);
        """;
        String insertMember = """
            INSERT INTO team_members (uuid, username, team_name, role)
            VALUES (?, ?, ?, ?);
        """;

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(insertTeam)) {
                stmt.setString(1, team.getName());
                stmt.setString(2, team.getLeader().toString());
                stmt.setString(3, team.getLeaderName());
                stmt.setInt(4, team.isPvpEnabled() ? 1 : 0);
                stmt.setString(5, team.getAllyTeam());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(insertMember)) {
                stmt.setString(1, team.getLeader().toString());
                stmt.setString(2, team.getLeaderName());
                stmt.setString(3, team.getName());
                stmt.setString(4, "LEADER");
                stmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed rollback on createTeam", ex);
            }
            plugin.getLogger().log(Level.SEVERE, "Failed to create team in database: " + team.getName(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    public synchronized void deleteTeam(String teamName) {
        String deleteMembers = "DELETE FROM team_members WHERE team_name = ? COLLATE NOCASE;";
        String deleteTeam = "DELETE FROM teams WHERE name = ? COLLATE NOCASE;";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(deleteMembers)) {
                stmt.setString(1, teamName);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(deleteTeam)) {
                stmt.setString(1, teamName);
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed rollback on deleteTeam", ex);
            }
            plugin.getLogger().log(Level.SEVERE, "Failed to delete team: " + teamName, e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    public synchronized void addOrUpdateMember(UUID uuid, String username, String teamName, String role) {
        String sql = """
            INSERT INTO team_members (uuid, username, team_name, role)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                username = excluded.username,
                team_name = excluded.team_name,
                role = excluded.role;
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, teamName);
            stmt.setString(4, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to add/update member: " + username, e);
        }
    }

    public synchronized void removeMember(UUID uuid) {
        String sql = "DELETE FROM team_members WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to remove member: " + uuid, e);
        }
    }

    public synchronized void updateLeader(Team team, UUID oldLeaderUuid) {
        String updateTeam = "UPDATE teams SET leader_uuid = ?, leader_name = ? WHERE name = ? COLLATE NOCASE;";
        String updateNewRole = "UPDATE team_members SET role = 'LEADER' WHERE uuid = ?;";
        String updateOldRole = "UPDATE team_members SET role = 'MEMBER' WHERE uuid = ?;";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(updateTeam)) {
                stmt.setString(1, team.getLeader().toString());
                stmt.setString(2, team.getLeaderName());
                stmt.setString(3, team.getName());
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(updateNewRole)) {
                stmt.setString(1, team.getLeader().toString());
                stmt.executeUpdate();
            }
            if (oldLeaderUuid != null) {
                try (PreparedStatement stmt = connection.prepareStatement(updateOldRole)) {
                    stmt.setString(1, oldLeaderUuid.toString());
                    stmt.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed rollback on updateLeader", ex);
            }
            plugin.getLogger().log(Level.SEVERE, "Failed to update leader for team: " + team.getName(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    public synchronized void updatePvp(String teamName, boolean pvpEnabled) {
        String sql = "UPDATE teams SET pvp_enabled = ? WHERE name = ? COLLATE NOCASE;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pvpEnabled ? 1 : 0);
            stmt.setString(2, teamName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update pvp for team: " + teamName, e);
        }
    }

    public synchronized void updateAlly(String teamName, String allyTeam) {
        String sql = "UPDATE teams SET ally_team = ? WHERE name = ? COLLATE NOCASE;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, allyTeam);
            stmt.setString(2, teamName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update ally for team: " + teamName, e);
        }
    }

    public synchronized void updateUsername(UUID uuid, String newUsername, boolean isLeader, String teamName) {
        String sqlMember = "UPDATE team_members SET username = ? WHERE uuid = ?;";
        String sqlLeader = "UPDATE teams SET leader_name = ? WHERE name = ? COLLATE NOCASE;";
        try {
            try (PreparedStatement stmt = connection.prepareStatement(sqlMember)) {
                stmt.setString(1, newUsername);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            if (isLeader) {
                try (PreparedStatement stmt = connection.prepareStatement(sqlLeader)) {
                    stmt.setString(1, newUsername);
                    stmt.setString(2, teamName);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update username for: " + newUsername, e);
        }
    }
}
