package com.example.thiefandpolice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

// আসল সার্ভার না চালিয়ে HTTP অনুরোধ নকল করে: URL, status কোড আর আসল JSON যাচাই করে।
// @SpringBootTest পুরো অ্যাপ লোড করে, @AutoConfigureMockMvc তাতে MockMvc জুড়ে দেয়।
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private record NewGame(String code, String hostToken) {}

    private record Seat(String id, String token) {}

    // ---------- create ----------

    @Test
    void create_returnsFourLetterCodeWithoutIAndO() throws Exception {
        for (int i = 0; i < 20; i++) {
            NewGame game = createGame();

            assertTrue(game.code().matches("[A-HJ-NP-Z]{4}"), "bad code: " + game.code());
            assertFalse(game.hostToken().isBlank());
        }
    }

    @Test
    void create_rejectsRoundCountsOutsideTheLimits() throws Exception {
        for (String body : List.of("{\"totalRounds\":55}", "{\"totalRounds\":0}", "{}")) {
            mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void create_acceptsRoundCountsAtBothLimits() throws Exception {
        for (int rounds : List.of(GameConfig.MIN_ROUNDS, GameConfig.MAX_ROUNDS)) {
            mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"totalRounds\":" + rounds + "}"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void create_rejectsRoundCountsJustOutsideTheLimits() throws Exception {
        for (int rounds : List.of(GameConfig.MIN_ROUNDS - 1, GameConfig.MAX_ROUNDS + 1)) {
            mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"totalRounds\":" + rounds + "}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------- config ----------

    @Test
    void config_publishesTheLimitsFromGameConfig() throws Exception {
        String body = mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(GameConfig.MIN_ROUNDS, readInt(body, "$.minRounds"));
        assertEquals(GameConfig.MAX_ROUNDS, readInt(body, "$.maxRounds"));
        assertEquals(GameConfig.DEFAULT_ROUNDS, readInt(body, "$.defaultRounds"));
        assertEquals(GameConfig.MIN_PLAYERS, readInt(body, "$.minPlayers"));
        assertEquals(GameConfig.MAX_PLAYERS, readInt(body, "$.maxPlayers"));
        assertEquals(GameConfig.VILLAGER_FINE, readInt(body, "$.villagerFine"));
        // every role has its money amount, taken from the Role enum
        for (Role role : Role.values()) {
            assertEquals(role.getPayout(), readInt(body, "$.payouts." + role.name()));
        }
    }

    // ---------- join ----------

    @Test
    void join_isCaseInsensitiveForTheGameCode() throws Exception {
        NewGame game = createGame();

        join(game.code().toLowerCase(), "Amy");
    }

    @Test
    void join_rejectsBlankOrTooLongName_andUnknownGame() throws Exception {
        NewGame game = createGame();

        joinExpecting(game.code(), "   ", status().isBadRequest());
        joinExpecting(game.code(), "x".repeat(21), status().isBadRequest());
        joinExpecting("ZZZZ", "Amy", status().isNotFound());
    }

    @Test
    void join_rejectsDuplicateNameAndPlayerOverTheMaximum() throws Exception {
        NewGame game = createGame();
        join(game.code(), "Amy");

        joinExpecting(game.code(), "amy", status().isConflict());

        // Amy সহ মোট ১০ জন পর্যন্ত ঢুকতে পারে
        for (int i = 2; i <= GameConfig.MAX_PLAYERS; i++) {
            join(game.code(), "P" + i);
        }
        joinExpecting(game.code(), "Extra", status().isConflict());
    }

    // ---------- state ----------

    @Test
    void state_rejectsMissingWrongTokenAndUnknownGame() throws Exception {
        NewGame game = createGame();

        mockMvc.perform(get("/api/games/{code}/state", game.code()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/games/{code}/state", game.code()).header("X-Token", "wrong"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/games/{code}/state", "ZZZZ").header("X-Token", game.hostToken()))
                .andExpect(status().isNotFound());
    }

    // ---------- start ----------

    @Test
    void start_rejectsNonHost_andLobbyWithTooFewPlayers() throws Exception {
        NewGame game = createGame();
        Seat amy = join(game.code(), "Amy");

        call(game.code(), amy.token(), "start", status().isForbidden());
        call(game.code(), game.hostToken(), "start", status().isConflict()); // মাত্র ১ জন, কমপক্ষে ৪ লাগে
    }

    // ---------- পুরো খেলা, শুধু HTTP দিয়ে ----------

    @Test
    void fullRoundOverHttp_keepsSecretsHidden_andPaysCorrectly() throws Exception {
        NewGame game = createGame();
        String code = game.code();
        List<Seat> seats = new ArrayList<>();
        for (String name : List.of("Amy", "Ben", "Cat", "Dan")) {
            seats.add(join(code, name));
        }

        call(code, game.hostToken(), "start", status().isNoContent());

        // প্রত্যেকে শুধু নিজের role পায়, আর কোনো state-এ অন্য কারো role বা কোনো token থাকে না
        Map<String, Seat> byRole = new HashMap<>();
        for (Seat seat : seats) {
            String body = state(code, seat.token());
            byRole.put(readString(body, "$.myRole"), seat);
            assertNoRolesVisible(body);
            assertNoTokensLeaked(body, game, seats);
        }
        String hostBody = state(code, game.hostToken());
        assertEquals("HOST", readString(hostBody, "$.viewerType"));
        assertNoRolesVisible(hostBody);
        assertNoTokensLeaked(hostBody, game, seats);
        assertEquals(Set.of("KING", "VILLAGER", "POLICE", "THIEF"), byRole.keySet());

        Seat king = byRole.get("KING");
        Seat villager = byRole.get("VILLAGER");
        Seat police = byRole.get("POLICE");
        Seat thief = byRole.get("THIEF");

        // king reveal: শুধু king পারে; এরপর সবাই (host-ও) king-কে চেনে
        call(code, villager.token(), "reveal-king", status().isForbidden());
        call(code, king.token(), "reveal-king", status().isNoContent());
        assertEquals(king.id(), readString(state(code, thief.token()), "$.kingId"));
        assertEquals(king.id(), readString(state(code, game.hostToken()), "$.kingId"));

        // police reveal: শুধু police পারে
        call(code, thief.token(), "reveal-police", status().isForbidden());
        call(code, police.token(), "reveal-police", status().isNoContent());
        assertEquals(police.id(), readString(state(code, villager.token()), "$.policeId"));

        // guess: police ছাড়া কেউ পারে না, নিজেকে বাছা যায় না, তারপর ঠিক thief
        guess(code, king.token(), thief.id(), status().isForbidden());
        guess(code, police.token(), police.id(), status().isBadRequest());
        guess(code, police.token(), thief.id(), status().isNoContent());

        // RESULT: সবার role দেখা যায়, স্কোর নিয়ম মেনে বসেছে
        String result = state(code, game.hostToken());
        assertEquals("RESULT", readString(result, "$.phase"));
        assertEquals(thief.id(), readString(result, "$.guessedId"));
        List<?> roles = JsonPath.read(result, "$.players[*].role");
        assertEquals(4, roles.size());
        assertTrue(roles.stream().noneMatch(Objects::isNull));
        assertEquals(GameConfig.KING_SALARY, moneyOf(result, king));
        assertEquals(GameConfig.VILLAGER_SALARY, moneyOf(result, villager));
        assertEquals(GameConfig.THIEF_LOOT, moneyOf(result, police));
        assertEquals(0, moneyOf(result, thief));

        // next-round: শুধু host; তারপর role আবার লুকায়, স্কোর জমা থাকে
        call(code, villager.token(), "next-round", status().isForbidden());
        call(code, game.hostToken(), "next-round", status().isNoContent());

        String round2 = state(code, game.hostToken());
        assertEquals("KING_REVEAL", readString(round2, "$.phase"));
        assertEquals(2, readInt(round2, "$.currentRound"));
        assertNoRolesVisible(round2);
        assertEquals(GameConfig.KING_SALARY, moneyOf(round2, king));
    }

    // ---------- host নিজেও খেলে ----------

    @Test
    void hostWhoPlays_isAPlayerWithTheHostToken_andNoTokenLeaksToOthers() throws Exception {
        String created = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalRounds\":10,\"hostName\":\"Hasan\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        NewGame game = new NewGame(readString(created, "$.code"), readString(created, "$.hostToken"));
        List<Seat> seats = new ArrayList<>();
        for (String name : List.of("Amy", "Ben", "Cat")) {
            seats.add(join(game.code(), name));
        }

        // host নিজে Player: সংখ্যায় ধরা হয়, তাই ৪ জন হয়ে start করা যায়
        String hostBody = state(game.code(), game.hostToken());
        assertEquals("PLAYER", readString(hostBody, "$.viewerType"));
        assertTrue(readBool(hostBody, "$.host"));
        assertEquals(4, ((List<?>) JsonPath.read(hostBody, "$.players")).size());

        call(game.code(), game.hostToken(), "start", status().isNoContent());

        // host-player নিজের role দেখে, অন্যদের কেউ সেটা (বা hostToken) দেখে না
        String afterStart = state(game.code(), game.hostToken());
        assertFalse(readString(afterStart, "$.myRole").isBlank());
        assertNoRolesVisible(afterStart);
        for (Seat seat : seats) {
            String body = state(game.code(), seat.token());
            assertFalse(readBool(body, "$.host"));
            assertNoRolesVisible(body);
            assertNoTokensLeaked(body, game, seats);
        }
    }

    @Test
    void create_rejectsBlankHostName() throws Exception {
        mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalRounds\":10,\"hostName\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalRounds\":10,\"hostName\":\"" + "x".repeat(21) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- helper ----------

    private NewGame createGame() throws Exception {
        String body = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalRounds\":10}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new NewGame(readString(body, "$.code"), readString(body, "$.hostToken"));
    }

    private Seat join(String code, String name) throws Exception {
        String body = joinRequest(code, name)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Seat(readString(body, "$.playerId"), readString(body, "$.token"));
    }

    private void joinExpecting(String code, String name, ResultMatcher expected) throws Exception {
        joinRequest(code, name).andExpect(expected);
    }

    private ResultActions joinRequest(String code, String name) throws Exception {
        return mockMvc.perform(post("/api/games/{code}/join", code)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"));
    }

    private String state(String code, String token) throws Exception {
        return mockMvc.perform(get("/api/games/{code}/state", code).header("X-Token", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private void call(String code, String token, String action, ResultMatcher expected)
            throws Exception {
        mockMvc.perform(post("/api/games/{code}/{action}", code, action).header("X-Token", token))
                .andExpect(expected);
    }

    private void guess(String code, String token, String playerId, ResultMatcher expected)
            throws Exception {
        mockMvc.perform(post("/api/games/{code}/guess", code)
                        .header("X-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"" + playerId + "\"}"))
                .andExpect(expected);
    }

    private String readString(String json, String path) {
        return JsonPath.read(json, path);
    }

    private boolean readBool(String json, String path) {
        Boolean value = JsonPath.read(json, path);
        return value;
    }

    private int readInt(String json, String path) {
        return JsonPath.read(json, path);
    }

    private int moneyOf(String json, Seat seat) {
        List<Integer> amounts = JsonPath.read(json, "$.players[?(@.id=='" + seat.id() + "')].money");
        return amounts.get(0);
    }

    // RESULT-এর আগে কোনো খেলোয়াড়ের role JSON-এ থাকে না (সব null)
    private void assertNoRolesVisible(String json) {
        List<?> roles = JsonPath.read(json, "$.players[*].role");
        assertTrue(roles.stream().allMatch(Objects::isNull), "a role leaked: " + roles);
    }

    // কাঁচা JSON-এর কোথাও কোনো token (host বা player-এর) থাকে না
    private void assertNoTokensLeaked(String json, NewGame game, List<Seat> seats) {
        assertFalse(json.contains(game.hostToken()), "host token leaked");
        for (Seat seat : seats) {
            assertFalse(json.contains(seat.token()), "a player token leaked");
        }
    }
}
