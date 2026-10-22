package com.vanillateams;

import com.vanillateams.manager.VoteManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoteManagerLogicTest {

    @Test
    @DisplayName("Vote passes when yes votes exceed no votes")
    void testVotePassed() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();

        VoteManager.PvpVote vote = new VoteManager.PvpVote("TestTeam", true, p1, Set.of(p1, p2, p3));
        vote.voteYes(p1);
        vote.voteYes(p2);
        vote.voteNo(p3);

        assertTrue(vote.allVoted());
        assertTrue(vote.passed());
        assertEquals(2, vote.yesCount());
        assertEquals(1, vote.noCount());
    }

    @Test
    @DisplayName("Vote fails on tie or when no votes exceed yes votes")
    void testVoteFailed() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        VoteManager.PvpVote vote = new VoteManager.PvpVote("TestTeam", false, p1, Set.of(p1, p2));
        vote.voteYes(p1);
        vote.voteNo(p2);

        assertTrue(vote.allVoted());
        assertFalse(vote.passed()); // ties should fail
    }

    @Test
    @DisplayName("Tracks eligible and cast votes properly")
    void testVoteEligibilityAndDuplicates() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();

        VoteManager.PvpVote vote = new VoteManager.PvpVote("TestTeam", true, p1, Set.of(p1, p2));

        assertTrue(vote.isEligible(p1));
        assertTrue(vote.isEligible(p2));
        assertFalse(vote.isEligible(outsider));

        assertFalse(vote.hasVoted(p1));
        vote.voteYes(p1);
        assertTrue(vote.hasVoted(p1));
        assertFalse(vote.allVoted());

        vote.voteNo(p2);
        assertTrue(vote.allVoted());
    }
}
