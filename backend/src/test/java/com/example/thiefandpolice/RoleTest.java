package com.example.thiefandpolice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void fourPlayers_haveKingPoliceVillagerAndOneThief() {
        assertEquals(
                List.of(Role.KING, Role.POLICE, Role.VILLAGER, Role.THIEF),
                Role.rolesFor(4));
    }

    @Test
    void everyPlayerCount_hasOneRolePerPlayer_allDifferent() {
        for (int n = Role.MIN_PLAYERS; n <= Role.MAX_PLAYERS; n++) {
            List<Role> roles = Role.rolesFor(n);
            assertEquals(n, roles.size(), "players: " + n);
            assertEquals(n, new HashSet<>(roles).size(), "roles must be distinct, players: " + n);
        }
    }

    @Test
    void everyPlayerCount_hasExactlyOneKingPoliceAndVillager_restAreOutlaws() {
        for (int n = Role.MIN_PLAYERS; n <= Role.MAX_PLAYERS; n++) {
            List<Role> roles = Role.rolesFor(n);
            assertEquals(1, roles.stream().filter(r -> r == Role.KING).count());
            assertEquals(1, roles.stream().filter(r -> r == Role.POLICE).count());
            assertEquals(1, roles.stream().filter(r -> r == Role.VILLAGER).count());
            assertEquals(n - 3, roles.stream().filter(Role::isOutlaw).count());
        }
    }

    @Test
    void tenPlayers_useEveryRole() {
        assertEquals(List.of(Role.values()), Role.rolesFor(10));
    }

    @Test
    void rejectsPlayerCountsOutsideTheLimits() {
        assertThrows(IllegalArgumentException.class, () -> Role.rolesFor(3));
        assertThrows(IllegalArgumentException.class, () -> Role.rolesFor(11));
    }

    @Test
    void payoutsFollowTheRules() {
        assertEquals(1000, Role.KING.getPayout());
        assertEquals(200, Role.VILLAGER.getPayout());
        assertEquals(0, Role.POLICE.getPayout());
        assertEquals(500, Role.THIEF.getPayout());
        assertEquals(1100, Role.KIDNAPPER.getPayout());
        assertTrue(Role.DAKAT.isOutlaw());
        assertFalse(Role.VILLAGER.isOutlaw());
    }
}
