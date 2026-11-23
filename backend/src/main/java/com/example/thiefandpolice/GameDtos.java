package com.example.thiefandpolice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public class GameDtos {

    // ব্রাউজার থেকে যা আসবে
    // hostName দিলে host নিজেও খেলবে (না দিলে শুধু স্ক্রিন)
    public record CreateGameRequest(int totalRounds, @Size(max = 20) String hostName) {}

    public record JoinRequest(
            @NotBlank @Size(max = 20) String name) {}

    public record GuessRequest(@NotBlank String playerId) {}

    // Public limits and amounts from GameConfig
    public record ConfigResponse(
            int minRounds,
            int maxRounds,
            int defaultRounds,
            int minPlayers,
            int maxPlayers,
            int villagerFine,
            Map<Role, Integer> payouts) {} // money per round / loot for every role

    // ব্রাউজারে যা যাবে
    public record CreateGameResponse(String code, String hostToken) {}

    public record JoinResponse(String code, String playerId, String name, String token) {}

    // role এখানে সবার কাছে যায়, কিন্তু RESULT/GAME_OVER-এর আগে সবসময় null
    public record PlayerView(String id, String name, int money, Role role) {}

    public record GameStateView(
            String code,
            Phase phase,
            int currentRound,
            int totalRounds,
            int minPlayers,
            int maxPlayers,
            List<PlayerView> players,
            String viewerType,   // "HOST" (খেলছে না) বা "PLAYER"
            boolean host,        // true হলে এই দর্শক game-এর host (খেললেও)
            PlayerView me,       // হোস্টের ক্ষেত্রে null
            Role myRole,         // শুধু নিজের role; হোস্ট বা start-এর আগে null
            String kingId,       // king reveal করার আগে null, পরে সবার কাছে king-এর playerId
            String policeId,     // police reveal করার আগে null, পরে সবার কাছে police-এর playerId
            String guessedId     // RESULT/GAME_OVER-এ police যাকে thief বলেছে তার playerId, নইলে null
    ) {}
}