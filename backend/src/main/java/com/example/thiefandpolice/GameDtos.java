package com.example.thiefandpolice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class GameDtos {

    // ব্রাউজার থেকে যা আসবে
    public record CreateGameRequest(int totalRounds) {}

    public record JoinRequest(
            @NotBlank @Size(max = 20) String name) {}

    // ব্রাউজারে যা যাবে
    public record CreateGameResponse(String code, String hostToken) {}

    public record JoinResponse(String code, String playerId, String name, String token) {}

    public record PlayerView(String id, String name, int score) {}

    public record GameStateView(
            String code,
            Phase phase,
            int currentRound,
            int totalRounds,
            int playersNeeded,
            List<PlayerView> players,
            String viewerType,   // "HOST" বা "PLAYER"
            PlayerView me,       // হোস্টের ক্ষেত্রে null
            Role myRole          // শুধু নিজের role; হোস্ট বা start-এর আগে null
    ) {}
}