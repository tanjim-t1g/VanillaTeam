package com.vanillateams.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Team {
    private final String name;
    private UUID leader;
    private String leaderName;
    private final Map<UUID, String> members = new LinkedHashMap<>();
    private boolean pvpEnabled;
    private String allyTeam;

    public Team(String name, UUID leader, String leaderName, boolean pvpEnabled) {
        this.name = name;
        this.leader = leader;
        this.leaderName = leaderName;
        this.pvpEnabled = pvpEnabled;
        this.members.put(leader, leaderName);
    }

    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
        String name = members.get(leader);
        if (name != null) {
            this.leaderName = name;
        }
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public String getAllyTeam() {
        return allyTeam;
    }

    public void setAllyTeam(String allyTeam) {
        this.allyTeam = allyTeam;
    }

    public boolean hasAlly() {
        return allyTeam != null && !allyTeam.isEmpty();
    }

    public Map<UUID, String> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    public Set<UUID> getMemberIds() {
        return Collections.unmodifiableSet(members.keySet());
    }

    public void addMember(UUID uuid, String username) {
        members.put(uuid, username);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public String getMemberName(UUID uuid) {
        return members.get(uuid);
    }

    public UUID findMemberByName(String name) {
        for (var entry : members.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void updateMemberName(UUID uuid, String newName) {
        if (members.containsKey(uuid)) {
            members.put(uuid, newName);
            if (leader.equals(uuid)) {
                this.leaderName = newName;
            }
        }
    }

    public int getSize() {
        return members.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return name.equalsIgnoreCase(team.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}
