package com.example.thiefandpolice;

import com.example.thiefandpolice.GameDtos.ConfigResponse;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Public game limits for the frontend, so the numbers are defined only once (in GameConfig)
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @GetMapping
    public ConfigResponse config() {
        Map<Role, Integer> payouts = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            payouts.put(role, role.getPayout());
        }
        return new ConfigResponse(
                GameConfig.MIN_ROUNDS,
                GameConfig.MAX_ROUNDS,
                GameConfig.DEFAULT_ROUNDS,
                GameConfig.MIN_PLAYERS,
                GameConfig.MAX_PLAYERS,
                GameConfig.VILLAGER_FINE,
                payouts);
    }
}
