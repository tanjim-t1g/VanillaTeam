package com.vanillateams;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamValidationTest {

    private boolean isValidTeamName(String name, int min, int max) {
        if (name == null) return false;
        if (name.length() < min || name.length() > max) return false;
        return name.matches("^[a-zA-Z0-9_]+$");
    }

    @Test
    @DisplayName("Valid team names should pass regex and length checks")
    void testValidTeamNames() {
        assertTrue(isValidTeamName("Red_Dragons", 2, 16));
        assertTrue(isValidTeamName("Alpha", 2, 16));
        assertTrue(isValidTeamName("team123", 2, 16));
        assertTrue(isValidTeamName("AB", 2, 16));
        assertTrue(isValidTeamName("1234567890123456", 2, 16));
    }

    @Test
    @DisplayName("Names with spaces or special characters should be rejected")
    void testInvalidCharacters() {
        assertFalse(isValidTeamName("Team One", 2, 16));
        assertFalse(isValidTeamName("Team-1", 2, 16));
        assertFalse(isValidTeamName("Team!", 2, 16));
        assertFalse(isValidTeamName("@team", 2, 16));
    }

    @Test
    @DisplayName("Names exceeding or subceeding length limits should be rejected")
    void testLengthLimits() {
        assertFalse(isValidTeamName("A", 2, 16));
        assertFalse(isValidTeamName("ThisTeamNameIsWayTooLong", 2, 16));
    }
}
