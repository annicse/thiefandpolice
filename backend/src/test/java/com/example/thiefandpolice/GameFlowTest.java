package com.example.thiefandpolice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.thiefandpolice.GameDtos.GameStateView;
import com.example.thiefandpolice.GameDtos.PlayerView;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GameFlowTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = newFullGame(10);
    }

    // ---------- helper ----------

    private Game newFullGame(int totalRounds) {
        Game g = new Game("ABCD", totalRounds);
        for (int i = 1; i <= Game.MIN_PLAYERS; i++) {
            g.addPlayer("P" + i);
        }
        return g;
    }

    private Game gameWithPlayers(int count) {
        Game g = new Game("ABCD", 10);
        for (int i = 1; i <= count; i++) {
            g.addPlayer("P" + i);
        }
        return g;
    }

    private Player byRole(Game g, Role role) {
        return g.getPlayers().stream()
                .filter(p -> p.getRole() == role)
                .findFirst()
                .orElseThrow();
    }

    private Player byRole(Role role) {
        return game.getPlayers().stream()
                .filter(p -> p.getRole() == role)
                .findFirst()
                .orElseThrow();
    }

    private void assertStatus(HttpStatus expected, Executable action) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, action);
        assertEquals(expected, ex.getStatusCode());
    }

    private void advanceToGuessing() {
        game.start(game.getHostToken());
        game.revealKing(byRole(Role.KING).getToken());
        game.revealPolice(byRole(Role.POLICE).getToken());
    }

    // KING_REVEAL থেকে শুরু করে king ও police reveal করে, police ঠিক thief ধরে, RESULT-এ পৌঁছায়
    // (start এখানে ডাকা হয় না, কারণ নতুন রাউন্ডে next-round নিজেই KING_REVEAL-এ এনে দেয়)
    private void finishRoundWithCorrectGuess() {
        game.revealKing(byRole(Role.KING).getToken());
        game.revealPolice(byRole(Role.POLICE).getToken());
        game.guess(byRole(Role.POLICE).getToken(), byRole(Role.THIEF).getId());
    }

    // start করে, তারপর প্রথম রাউন্ড শেষ করে
    private void playRoundWithCorrectGuess() {
        game.start(game.getHostToken());
        finishRoundWithCorrectGuess();
    }

    // ---------- start ----------

    @Test
    void start_dealsEachRoleExactlyOnce() {
        game.start(game.getHostToken());

        Set<Role> roles = game.getPlayers().stream()
                .map(Player::getRole)
                .collect(Collectors.toSet());

        assertEquals(EnumSet.copyOf(Role.rolesFor(Game.MIN_PLAYERS)), roles);
        assertEquals(Phase.KING_REVEAL, game.getPhase());
        assertEquals(1, game.getCurrentRound());
    }

    @Test
    void start_rejectsNonHost() {
        String playerToken = game.getPlayers().get(0).getToken();

        assertStatus(HttpStatus.FORBIDDEN, () -> game.start(playerToken));
        assertEquals(Phase.LOBBY, game.getPhase());
    }

    @Test
    void start_rejectsWhenNotEnoughPlayers() {
        Game small = new Game("WXYZ", 10);
        small.addPlayer("Only");

        assertStatus(HttpStatus.CONFLICT, () -> small.start(small.getHostToken()));
    }

    @Test
    void start_rejectsThreePlayers_butAcceptsFour() {
        Game three = new Game("WXYZ", 10);
        for (int i = 1; i <= 3; i++) {
            three.addPlayer("P" + i);
        }
        assertStatus(HttpStatus.CONFLICT, () -> three.start(three.getHostToken()));
        assertEquals(Phase.LOBBY, three.getPhase());

        three.addPlayer("P4");
        three.start(three.getHostToken());
        assertEquals(Phase.KING_REVEAL, three.getPhase());
    }

    @Test
    void start_dealsTheRightRolesForEveryPlayerCount() {
        for (int n = Game.MIN_PLAYERS; n <= Game.MAX_PLAYERS; n++) {
            Game g = gameWithPlayers(n);
            g.start(g.getHostToken());

            Set<Role> dealt = g.getPlayers().stream()
                    .map(Player::getRole)
                    .collect(Collectors.toSet());

            assertEquals(n, dealt.size(), "every player has a different role, players: " + n);
            assertEquals(EnumSet.copyOf(Role.rolesFor(n)), dealt, "players: " + n);
        }
    }

    @Test
    void guess_withSixPlayers_caughtOutlawGetsNothing_otherOutlawsKeepLoot() {
        Game g = gameWithPlayers(6);
        g.start(g.getHostToken());
        Player king = byRole(g, Role.KING);
        Player police = byRole(g, Role.POLICE);
        g.revealKing(king.getToken());
        g.revealPolice(police.getToken());
        Player caught = g.getPlayers().stream()
                .filter(p -> p.getRole() == Role.BURGLAR)
                .findFirst().orElseThrow();

        g.guess(police.getToken(), caught.getId());

        assertEquals(700, police.getMoney());          // Burglar-এর loot
        assertEquals(0, caught.getMoney());
        assertEquals(500, byRole(g, Role.THIEF).getMoney()); // ধরা পড়েনি
        assertEquals(800, byRole(g, Role.SMUGGLER).getMoney());
        assertEquals(1000, king.getMoney());
        assertEquals(200, byRole(g, Role.VILLAGER).getMoney());
    }

    @Test
    void addPlayer_rejectsEleventhPlayer() {
        Game g = gameWithPlayers(Game.MAX_PLAYERS);

        assertStatus(HttpStatus.CONFLICT, () -> g.addPlayer("Extra"));
    }

    @Test
    void start_rejectsSecondStart() {
        game.start(game.getHostToken());

        assertStatus(HttpStatus.CONFLICT, () -> game.start(game.getHostToken()));
    }

    @Test
    void start_dealsEveryRoleAboutEquallyOftenToTheFirstPlayer() {
        int deals = 4000;
        Map<Role, Integer> counts = new EnumMap<>(Role.class);

        for (int i = 0; i < deals; i++) {
            Game g = newFullGame(10);
            g.start(g.getHostToken());
            Role first = g.getPlayers().get(0).getRole(); // প্রথমে join করা প্লেয়ার
            counts.merge(first, 1, Integer::sum);
        }

        // প্রত্যাশা: প্রতিটা role ~1000 বার। 850..1150 অনেক বড় ফাঁক রাখা হয়েছে, তাই শুধু আসল পক্ষপাতেই ফেল করবে
        for (Role role : Role.rolesFor(Game.MIN_PLAYERS)) {
            int count = counts.getOrDefault(role, 0);
            assertTrue(count > 850 && count < 1150,
                    role + " was dealt " + count + " times out of " + deals);
        }
    }

    // ---------- reveal-king / reveal-police ----------

    @Test
    void revealKing_onlyKingCanDoIt() {
        game.start(game.getHostToken());

        assertStatus(HttpStatus.FORBIDDEN, () -> game.revealKing(byRole(Role.VILLAGER).getToken()));
        assertStatus(HttpStatus.FORBIDDEN, () -> game.revealKing(game.getHostToken()));
        assertEquals(Phase.KING_REVEAL, game.getPhase());

        game.revealKing(byRole(Role.KING).getToken());
        assertEquals(Phase.POLICE_REVEAL, game.getPhase());
    }

    @Test
    void revealKing_rejectedInWrongPhase() {
        assertStatus(HttpStatus.CONFLICT, () -> game.revealKing(game.getPlayers().get(0).getToken()));
    }

    @Test
    void revealPolice_onlyPoliceCanDoIt_andNotBeforeKingReveal() {
        game.start(game.getHostToken());
        String policeToken = byRole(Role.POLICE).getToken();

        assertStatus(HttpStatus.CONFLICT, () -> game.revealPolice(policeToken));

        game.revealKing(byRole(Role.KING).getToken());
        assertStatus(HttpStatus.FORBIDDEN, () -> game.revealPolice(byRole(Role.THIEF).getToken()));

        game.revealPolice(policeToken);
        assertEquals(Phase.GUESSING, game.getPhase());
    }

    // ---------- guess + scoring ----------

    @Test
    void guess_correct_paysMoneyAsPerRules() {
        playRoundWithCorrectGuess();

        assertEquals(Phase.RESULT, game.getPhase());
        assertEquals(1000, byRole(Role.KING).getMoney());
        assertEquals(200, byRole(Role.VILLAGER).getMoney());
        assertEquals(500, byRole(Role.POLICE).getMoney());
        assertEquals(0, byRole(Role.THIEF).getMoney());
    }

    @Test
    void guess_wrong_policeGetsNothingAndThiefKeepsLoot() {
        advanceToGuessing();

        game.guess(byRole(Role.POLICE).getToken(), byRole(Role.KING).getId());

        assertEquals(Phase.RESULT, game.getPhase());
        assertEquals(1000, byRole(Role.KING).getMoney());
        assertEquals(200, byRole(Role.VILLAGER).getMoney());
        assertEquals(0, byRole(Role.POLICE).getMoney());
        assertEquals(500, byRole(Role.THIEF).getMoney());
    }

    @Test
    void guess_villager_policePaysFineToVillager() {
        advanceToGuessing();
        Player police = byRole(Role.POLICE);
        police.addMoney(1000); // আগের রাউন্ডের জমানো টাকা ধরে নিই

        game.guess(police.getToken(), byRole(Role.VILLAGER).getId());

        assertEquals(1000 - Game.VILLAGER_FINE, police.getMoney());
        assertEquals(200 + Game.VILLAGER_FINE, byRole(Role.VILLAGER).getMoney());
        assertEquals(500, byRole(Role.THIEF).getMoney()); // ধরা পড়েনি, তাই loot রাখে
        assertEquals(1000, byRole(Role.KING).getMoney());
    }

    @Test
    void guess_villager_fineIsLimitedByWhatPoliceHas() {
        advanceToGuessing();
        Player police = byRole(Role.POLICE);
        police.addMoney(100); // জরিমানার চেয়ে কম

        game.guess(police.getToken(), byRole(Role.VILLAGER).getId());

        assertEquals(0, police.getMoney()); // ০-এর নিচে নামে না
        assertEquals(200 + 100, byRole(Role.VILLAGER).getMoney()); // যা পেল তাই
    }

    @Test
    void guess_villager_policeWithNoMoneyPaysNothing() {
        advanceToGuessing();

        game.guess(byRole(Role.POLICE).getToken(), byRole(Role.VILLAGER).getId());

        assertEquals(0, byRole(Role.POLICE).getMoney());
        assertEquals(200, byRole(Role.VILLAGER).getMoney());
    }

    @Test
    void guess_rejectsNonPolice() {
        advanceToGuessing();
        String thiefId = byRole(Role.THIEF).getId();

        assertStatus(HttpStatus.FORBIDDEN, () -> game.guess(byRole(Role.KING).getToken(), thiefId));
        assertStatus(HttpStatus.FORBIDDEN, () -> game.guess(game.getHostToken(), thiefId));
        assertEquals(Phase.GUESSING, game.getPhase());
    }

    @Test
    void guess_rejectsSelfAndUnknownPlayer() {
        advanceToGuessing();
        Player police = byRole(Role.POLICE);

        assertStatus(HttpStatus.BAD_REQUEST, () -> game.guess(police.getToken(), police.getId()));
        assertStatus(HttpStatus.BAD_REQUEST, () -> game.guess(police.getToken(), "no-such-id"));
        assertEquals(Phase.GUESSING, game.getPhase());
    }

    @Test
    void guess_rejectedInWrongPhase_andNotPaidTwice() {
        game.start(game.getHostToken());
        String policeToken = byRole(Role.POLICE).getToken();
        String thiefId = byRole(Role.THIEF).getId();

        assertStatus(HttpStatus.CONFLICT, () -> game.guess(policeToken, thiefId));

        game.revealKing(byRole(Role.KING).getToken());
        game.revealPolice(policeToken);
        game.guess(policeToken, thiefId);

        assertStatus(HttpStatus.CONFLICT, () -> game.guess(policeToken, thiefId));
        assertEquals(1000, byRole(Role.KING).getMoney());
    }

    // ---------- next-round + GAME_OVER ----------

    @Test
    void nextRound_rejectsNonHost_andWrongPhase() {
        game.start(game.getHostToken());
        assertStatus(HttpStatus.CONFLICT, () -> game.nextRound(game.getHostToken()));

        game.revealKing(byRole(Role.KING).getToken());
        game.revealPolice(byRole(Role.POLICE).getToken());
        game.guess(byRole(Role.POLICE).getToken(), byRole(Role.THIEF).getId());

        assertStatus(HttpStatus.FORBIDDEN, () -> game.nextRound(byRole(Role.KING).getToken()));
        assertEquals(Phase.RESULT, game.getPhase());
    }

    @Test
    void nextRound_startsNewRound_keepsMoney_andRedealsAllRoles() {
        playRoundWithCorrectGuess();

        game.nextRound(game.getHostToken());

        assertEquals(Phase.KING_REVEAL, game.getPhase());
        assertEquals(2, game.getCurrentRound());

        Set<Role> roles = game.getPlayers().stream()
                .map(Player::getRole)
                .collect(Collectors.toSet());
        assertEquals(EnumSet.copyOf(Role.rolesFor(Game.MIN_PLAYERS)), roles);

        int totalMoney = game.getPlayers().stream().mapToInt(Player::getMoney).sum();
        assertEquals(1000 + 200 + 500, totalMoney);
    }

    @Test
    void nextRound_afterLastRound_goesToGameOver_thenRejectsFurtherCalls() {
        game.start(game.getHostToken());

        for (int round = 1; round <= 10; round++) {
            assertEquals(round, game.getCurrentRound());
            assertEquals(Phase.KING_REVEAL, game.getPhase());
            finishRoundWithCorrectGuess();
            assertEquals(Phase.RESULT, game.getPhase());
            game.nextRound(game.getHostToken());
        }

        assertEquals(Phase.GAME_OVER, game.getPhase());
        assertEquals(10, game.getCurrentRound());
        assertEquals(10 * (1000 + 200 + 500),
                game.getPlayers().stream().mapToInt(Player::getMoney).sum());

        assertStatus(HttpStatus.CONFLICT, () -> game.nextRound(game.getHostToken()));
    }

    // ---------- secrets (viewFor) ----------

    @Test
    void view_inLobby_hasNoRolesOrIds() {
        GameStateView hostView = game.viewFor(game.getHostToken());

        assertNull(hostView.myRole());
        assertNull(hostView.me());
        assertNull(hostView.kingId());
        assertNull(hostView.policeId());
    }

    @Test
    void view_playerSeesOnlyOwnRole_beforeResult() {
        game.start(game.getHostToken());

        for (Player p : game.getPlayers()) {
            GameStateView view = game.viewFor(p.getToken());

            assertEquals(p.getRole(), view.myRole());
            for (PlayerView pv : view.players()) {
                assertNull(pv.role(), "role must stay hidden before RESULT");
            }
        }

        GameStateView hostView = game.viewFor(game.getHostToken());
        assertNull(hostView.myRole());
        for (PlayerView pv : hostView.players()) {
            assertNull(pv.role());
        }
    }

    @Test
    void view_kingAndPoliceIds_appearOnlyAfterTheirReveal() {
        game.start(game.getHostToken());
        String observer = byRole(Role.THIEF).getToken();

        assertNull(game.viewFor(observer).kingId());
        assertNull(game.viewFor(observer).policeId());

        game.revealKing(byRole(Role.KING).getToken());
        assertEquals(byRole(Role.KING).getId(), game.viewFor(observer).kingId());
        assertEquals(byRole(Role.KING).getId(), game.viewFor(game.getHostToken()).kingId());
        assertNull(game.viewFor(observer).policeId());

        game.revealPolice(byRole(Role.POLICE).getToken());
        assertEquals(byRole(Role.POLICE).getId(), game.viewFor(observer).policeId());
    }

    @Test
    void view_allRolesVisibleInResult_andHiddenAgainInNextRound() {
        playRoundWithCorrectGuess();

        for (PlayerView pv : game.viewFor(game.getHostToken()).players()) {
            assertNotNull(pv.role());
        }

        game.nextRound(game.getHostToken());

        GameStateView view = game.viewFor(byRole(Role.VILLAGER).getToken());
        assertNull(view.kingId());
        assertNull(view.policeId());
        for (PlayerView pv : view.players()) {
            assertNull(pv.role());
        }
    }

    @Test
    void view_guessedId_visibleOnlyInResult_andClearedInNextRound() {
        game.start(game.getHostToken());
        game.revealKing(byRole(Role.KING).getToken());
        game.revealPolice(byRole(Role.POLICE).getToken());
        String observer = byRole(Role.VILLAGER).getToken();

        assertNull(game.viewFor(observer).guessedId());

        String thiefId = byRole(Role.THIEF).getId();
        game.guess(byRole(Role.POLICE).getToken(), thiefId);

        assertEquals(thiefId, game.viewFor(observer).guessedId());
        assertEquals(thiefId, game.viewFor(game.getHostToken()).guessedId());

        game.nextRound(game.getHostToken());
        assertNull(game.viewFor(observer).guessedId());
    }

    @Test
    void view_rejectsUnknownToken() {
        assertStatus(HttpStatus.FORBIDDEN, () -> game.viewFor("bad-token"));
    }
}
