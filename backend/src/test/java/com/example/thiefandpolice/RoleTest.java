package com.example.thiefandpolice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void fourPlayers_haveKingPoliceVillagerAndOneThief() {
        assertEquals(
                List.of(Role.KING, Role.POLICE, Role.VILLAGER, Role.THIEF),
                Role.rolesFor(4));
    }

    @Test
    void everyPlayerCount_hasOneRolePerPlayer_onlyVillagersRepeat() {
        for (int n = GameConfig.MIN_PLAYERS; n <= GameConfig.MAX_PLAYERS; n++) {
            List<Role> roles = Role.rolesFor(n);
            assertEquals(n, roles.size(), "players: " + n);
            long nonVillagers = roles.stream().filter(r -> r != Role.VILLAGER).count();
            long distinctNonVillagers = roles.stream()
                    .filter(r -> r != Role.VILLAGER).distinct().count();
            assertEquals(nonVillagers, distinctNonVillagers, "players: " + n);
        }
    }

    @Test
    void villagerCount_growsWithThePlayerCount() {
        assertEquals(1, Role.villagersFor(4));
        assertEquals(1, Role.villagersFor(10));
        assertEquals(2, Role.villagersFor(11));
        assertEquals(2, Role.villagersFor(15));
        assertEquals(3, Role.villagersFor(16));
        assertEquals(3, Role.villagersFor(20));
    }

    @Test
    void elevenPlayers_haveTwoVillagersAndTheSameSevenOutlawsAsTen() {
        List<Role> ten = Role.rolesFor(10);
        List<Role> eleven = Role.rolesFor(11);

        assertEquals(2, eleven.stream().filter(r -> r == Role.VILLAGER).count());
        assertEquals(
                ten.stream().filter(Role::isOutlaw).toList(),
                eleven.stream().filter(Role::isOutlaw).toList());
    }

    @Test
    void everyPlayerCount_hasOneKingOnePolice_theRightVillagers_restAreOutlaws() {
        for (int n = GameConfig.MIN_PLAYERS; n <= GameConfig.MAX_PLAYERS; n++) {
            List<Role> roles = Role.rolesFor(n);
            assertEquals(1, roles.stream().filter(r -> r == Role.KING).count());
            assertEquals(1, roles.stream().filter(r -> r == Role.POLICE).count());
            assertEquals(Role.villagersFor(n), roles.stream().filter(r -> r == Role.VILLAGER).count());
            assertEquals(n - 2 - Role.villagersFor(n), roles.stream().filter(Role::isOutlaw).count());
        }
    }

    @Test
    void twentyPlayers_useEveryRole() {
        assertEquals(
                Set.copyOf(List.of(Role.values())),
                Set.copyOf(Role.rolesFor(20)));
    }

    @Test
    void rejectsPlayerCountsOutsideTheLimits() {
        assertThrows(IllegalArgumentException.class, () -> Role.rolesFor(3));
        assertThrows(IllegalArgumentException.class, () -> Role.rolesFor(21));
    }

    @Test
    void payoutsFollowTheRules() {
        assertEquals(GameConfig.KING_SALARY, Role.KING.getPayout());
        assertEquals(GameConfig.VILLAGER_SALARY, Role.VILLAGER.getPayout());
        assertEquals(GameConfig.POLICE_SALARY, Role.POLICE.getPayout());
        assertEquals(GameConfig.THIEF_LOOT, Role.THIEF.getPayout());
        assertEquals(GameConfig.BURGLAR_LOOT, Role.BURGLAR.getPayout());
        assertEquals(GameConfig.SMUGGLER_LOOT, Role.SMUGGLER.getPayout());
        assertEquals(GameConfig.PICKPOCKET_LOOT, Role.PICKPOCKET.getPayout());
        assertEquals(GameConfig.GANGSTER_LOOT, Role.GANGSTER.getPayout());
        assertEquals(GameConfig.DAKAT_LOOT, Role.DAKAT.getPayout());
        assertEquals(GameConfig.KIDNAPPER_LOOT, Role.KIDNAPPER.getPayout());
        assertEquals(GameConfig.TOKAI_LOOT, Role.TOKAI.getPayout());
        assertEquals(GameConfig.DALAL_LOOT, Role.DALAL.getPayout());
        assertEquals(GameConfig.JAAL_NOTE_LOOT, Role.JAAL_NOTE.getPayout());
        assertEquals(GameConfig.CHANDABAZ_LOOT, Role.CHANDABAZ.getPayout());
        assertEquals(GameConfig.MASTAN_LOOT, Role.MASTAN.getPayout());
        assertEquals(GameConfig.HIJACKER_LOOT, Role.HIJACKER.getPayout());
        assertEquals(GameConfig.HITMAN_LOOT, Role.HITMAN.getPayout());
        assertEquals(GameConfig.DON_LOOT, Role.DON.getPayout());
        assertTrue(Role.DAKAT.isOutlaw());
        assertFalse(Role.VILLAGER.isOutlaw());
    }
}
