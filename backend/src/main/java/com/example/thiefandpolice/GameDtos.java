package com.example.thiefandpolice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class GameDtos {

    // ব্রাউজার থেকে যা আসবে
    public record CreateGameRequest(int totalRounds) {}

    public record JoinRequest(
            @NotBlank @Size(max = 20) String name) {}

    public record GuessRequest(@NotBlank String playerId) {}

    // ব্রাউজারে যা যাবে
    public record CreateGameResponse(String code, String hostToken) {}

    public record JoinResponse(String code, String playerId, String name, String token) {}

    // role এখানে সবার কাছে যায়, কিন্তু RESULT/GAME_OVER-এর আগে সবসময় null
    public record PlayerView(String id, String name, int score, Role role) {}

    public record GameStateView(
            String code,
            Phase phase,
            int currentRound,
            int totalRounds,
            int playersNeeded,
            List<PlayerView> players,
            String viewerType,   // "HOST" বা "PLAYER"
            PlayerView me,       // হোস্টের ক্ষেত্রে null
            Role myRole,         // শুধু নিজের role; হোস্ট বা start-এর আগে null
            String kingId,       // king reveal করার আগে null, পরে সবার কাছে king-এর playerId
            String policeId      // police reveal করার আগে null, পরে সবার কাছে police-এর playerId
    ) {}
}