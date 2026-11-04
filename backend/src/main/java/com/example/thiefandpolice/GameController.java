package com.example.thiefandpolice;

import com.example.thiefandpolice.GameDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public CreateGameResponse create(@Valid @RequestBody CreateGameRequest request) {
        Game game = gameService.createGame(request.totalRounds());
        return new CreateGameResponse(game.getCode(), game.getHostToken());
    }

    @PostMapping("/{code}/join")
    public JoinResponse join(@PathVariable String code,
                             @Valid @RequestBody JoinRequest request) {
        Game game = gameService.getGame(code);
        Player player = game.addPlayer(request.name().trim());
        return new JoinResponse(game.getCode(), player.getId(), player.getName(), player.getToken());
    }

    @GetMapping("/{code}/state")
    public GameStateView state(@PathVariable String code,
                               @RequestHeader("X-Token") String token) {
        return gameService.getGame(code).viewFor(token);
    }

    @PostMapping("/{code}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void start(@PathVariable String code,
                      @RequestHeader("X-Token") String token) {
        gameService.getGame(code).start(token);
    }

    @PostMapping("/{code}/reveal-king")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revealKing(@PathVariable String code,
                           @RequestHeader("X-Token") String token) {
        gameService.getGame(code).revealKing(token);
    }

    @PostMapping("/{code}/reveal-police")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revealPolice(@PathVariable String code,
                             @RequestHeader("X-Token") String token) {
        gameService.getGame(code).revealPolice(token);
    }

    @PostMapping("/{code}/guess")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guess(@PathVariable String code,
                      @RequestHeader("X-Token") String token,
                      @Valid @RequestBody GuessRequest request) {
        gameService.getGame(code).guess(token, request.playerId());
    }
}