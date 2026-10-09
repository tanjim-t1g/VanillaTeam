package com.vanillateams.manager;

import com.vanillateams.VanillaTeams;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VoteManager {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;
    private final Map<String, PvpVote> activeVotes = new HashMap<>();

    public VoteManager(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    public static class PvpVote {
        private final String teamName;
        private final boolean proposedState;
        private final UUID initiator;
        private final Set<UUID> eligibleVoters;
        private final Set<UUID> votedYes = new HashSet<>();
        private final Set<UUID> votedNo = new HashSet<>();
        private BukkitTask timeoutTask;

        public PvpVote(String teamName, boolean proposedState, UUID initiator, Set<UUID> eligibleVoters) {
            this.teamName = teamName;
            this.proposedState = proposedState;
            this.initiator = initiator;
            this.eligibleVoters = eligibleVoters;
        }

        public boolean hasVoted(UUID uuid) {
            return votedYes.contains(uuid) || votedNo.contains(uuid);
        }

        public void voteYes(UUID uuid) {
            votedYes.add(uuid);
        }

        public void voteNo(UUID uuid) {
            votedNo.add(uuid);
        }

        public boolean allVoted() {
            return (votedYes.size() + votedNo.size()) >= eligibleVoters.size();
        }

        public boolean passed() {
            return votedYes.size() > votedNo.size();
        }

        public int yesCount() {
            return votedYes.size();
        }

        public int noCount() {
            return votedNo.size();
        }

        public int totalEligible() {
            return eligibleVoters.size();
        }

        public boolean isEligible(UUID uuid) {
            return eligibleVoters.contains(uuid);
        }

        public boolean isProposedState() {
            return proposedState;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTimeoutTask(BukkitTask task) {
            this.timeoutTask = task;
        }

        public void cancelTimeout() {
            if (timeoutTask != null && !timeoutTask.isCancelled()) {
                timeoutTask.cancel();
            }
        }
    }

    public boolean hasActiveVote(Team team) {
        return activeVotes.containsKey(team.getName().toLowerCase());
    }

    public PvpVote getActiveVote(Team team) {
        return activeVotes.get(team.getName().toLowerCase());
    }

    public void startVote(Team team, Player initiator, boolean proposedState) {
        Set<UUID> eligible = new HashSet<>();
        for (UUID memberId : team.getMemberIds()) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null && p.isOnline()) {
                eligible.add(memberId);
            }
        }

        if (eligible.size() <= 1) {
            teamManager.setTeamPvp(team, proposedState);
            String state = proposedState ? "&aON" : "&cOFF";
            teamManager.broadcastTeamMessage(team,
                    plugin.prefix() + plugin.msg("pvp-changed").replace("<state>", state));
            return;
        }

        PvpVote vote = new PvpVote(team.getName(), proposedState, initiator.getUniqueId(), eligible);
        activeVotes.put(team.getName().toLowerCase(), vote);
        vote.voteYes(initiator.getUniqueId());

        String state = proposedState ? "&aON" : "&cOFF";
        String msg = plugin.msg("pvp-vote-started")
                .replace("<player>", initiator.getName())
                .replace("<state>", state);
        Component voteMsg = MessageUtil.parse(plugin.prefix() + msg)
                .append(MessageUtil.yesNoButtons("/team pvp yes", "/team pvp no"));

        for (UUID memberId : team.getMemberIds()) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null && p.isOnline()) {
                p.sendMessage(voteMsg);
            }
        }

        int timeoutSeconds = plugin.getConfig().getInt("settings.pvp-vote-timeout-seconds", 30);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> concludeVote(team), timeoutSeconds * 20L);
        vote.setTimeoutTask(task);

        if (vote.allVoted()) {
            concludeVote(team);
        }
    }

    public void castVote(Team team, Player voter, boolean yes) {
        PvpVote vote = activeVotes.get(team.getName().toLowerCase());
        if (vote == null) return;

        if (yes) {
            vote.voteYes(voter.getUniqueId());
        } else {
            vote.voteNo(voter.getUniqueId());
        }

        String voteStr = yes ? "&ayes" : "&cno";
        teamManager.broadcastTeamMessage(team,
                plugin.prefix() + plugin.msg("pvp-vote-cast")
                        .replace("<player>", voter.getName())
                        .replace("<vote>", voteStr));

        if (vote.allVoted()) {
            concludeVote(team);
        }
    }

    private void concludeVote(Team team) {
        PvpVote vote = activeVotes.remove(team.getName().toLowerCase());
        if (vote == null) return;
        vote.cancelTimeout();

        if (vote.passed()) {
            teamManager.setTeamPvp(team, vote.isProposedState());
            String state = vote.isProposedState() ? "&aON" : "&cOFF";
            teamManager.broadcastTeamMessage(team,
                    plugin.prefix() + plugin.msg("pvp-vote-passed")
                            .replace("<state>", state)
                            .replace("<yes>", String.valueOf(vote.yesCount()))
                            .replace("<no>", String.valueOf(vote.noCount())));
        } else {
            String currentState = team.isPvpEnabled() ? "&aON" : "&cOFF";
            teamManager.broadcastTeamMessage(team,
                    plugin.prefix() + plugin.msg("pvp-vote-failed")
                            .replace("<state>", currentState)
                            .replace("<yes>", String.valueOf(vote.yesCount()))
                            .replace("<no>", String.valueOf(vote.noCount())));
        }
    }

    public void cancelVote(Team team) {
        PvpVote vote = activeVotes.remove(team.getName().toLowerCase());
        if (vote != null) {
            vote.cancelTimeout();
        }
    }

    public void cancelAll() {
        for (PvpVote vote : activeVotes.values()) {
            vote.cancelTimeout();
        }
        activeVotes.clear();
    }
}
