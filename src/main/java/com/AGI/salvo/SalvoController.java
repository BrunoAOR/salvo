package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class SalvoController {

	@Autowired
	private GameService gameService;
	@Autowired
	private GamePlayerService gamePlayerService;
	@Autowired
	private PlayerService playerService;

	@RequestMapping(path = "/games", method = RequestMethod.GET)
	public Map<String, Object> getGamesDTO(Authentication auth) {
		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		List<Game> games = gameService.findAll();
		return ApiUtils.getGamesDTO(authenticatedPlayer, games);
	}

	@RequestMapping(path = "/games", method = RequestMethod.POST)
	public ResponseEntity<Object> createGame(Authentication auth) {
		ResponseEntity<Object> response;
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		Player currentPlayer = getPlayerFromAuthenticationObject(auth);
		if (currentPlayer != null) {
			// Create game and gamePlayer for said game
			Game game = new Game();
			gameService.save(game);
			GamePlayer gamePlayer = new GamePlayer(game, currentPlayer);
			gamePlayerService.save(gamePlayer);

			mapDTO.put("gpId", gamePlayer.getId());
			response = new ResponseEntity<Object>(mapDTO, HttpStatus.CREATED);
		} else {
			mapDTO.put("error", "Unauthorized");
			response = new ResponseEntity<Object>(mapDTO, HttpStatus.UNAUTHORIZED);
		}
		return response;
	}

	@RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.GET)
	public ResponseEntity<Object> getListOfPlayerInGameDTO (@PathVariable long gameId) {
		ResponseEntity<Object> response;
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		HttpStatus httpStatus;

		Game game = gameService.findOne(gameId);

		if (game == null) {
			mapDTO.put("error", "Forbidden: No such game!");
			httpStatus = HttpStatus.FORBIDDEN;
		} else {
			mapDTO.put("players", game.getGamePlayers().stream().map(gp -> ApiUtils.getPlayerDTO(gp.getPlayer())).collect(Collectors.toList()));
			httpStatus = HttpStatus.OK;
		}
		response = new ResponseEntity<Object>(mapDTO, httpStatus);
		return response;
	}

	@RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.POST)
	public ResponseEntity<Object> joinGame (@PathVariable long gameId, Authentication auth) {
		ResponseEntity<Object> response;
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		HttpStatus httpStatus;

		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		Game game = gameService.findOne(gameId);

		if (authenticatedPlayer == null) {
			mapDTO.put("error", "Unauthorized: User needs to sign in before joining a game!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else if (game == null) {
			mapDTO.put("error", "Forbidden: No such game!");
			httpStatus = HttpStatus.HTTP_VERSION_NOT_SUPPORTED;
		} else if (game.getGamePlayers().size() == 2) {
			mapDTO.put("error", "Forbidden: Game is full!");
			httpStatus = HttpStatus.FORBIDDEN;
		} else {
			GamePlayer gamePlayer = new GamePlayer(game, authenticatedPlayer);
			gamePlayerService.save(gamePlayer);
			mapDTO.put("gpId", gamePlayer.getId());
			httpStatus = HttpStatus.CREATED;
		}
		response = new ResponseEntity<Object>(mapDTO, httpStatus);
		return response;
	}

	@RequestMapping(path = "/game_view/{gamePlayerId}", method = RequestMethod.GET)
	public ResponseEntity<Object> getGameViewDTO(@PathVariable long gamePlayerId, Authentication auth) {
		ResponseEntity<Object> response;

		GamePlayer requestedGamePlayer = gamePlayerService.findOne(gamePlayerId);
		Player authedPlayer = getPlayerFromAuthenticationObject(auth);

		Map<String, Object> mapDTO;

		if (requestedGamePlayer.getPlayer().getId() == authedPlayer.getId()) {
			// The signed in player is requesting for a game_view for one of his gamePlayers
			mapDTO = ApiUtils.getGameViewDTO(requestedGamePlayer);
			response = new ResponseEntity<Object>(mapDTO, HttpStatus.OK);
		} else {
			// The signed in player is requesting for a game_view for a gamePlayer that is not his
			mapDTO = new HashMap<>();
			mapDTO.put("error", "Unauthorized to view game info from this player's perspective!");
			response = new ResponseEntity<Object>(mapDTO, HttpStatus.UNAUTHORIZED);
		}

		return response;
	}

	@RequestMapping(path = "/players", method = RequestMethod.POST)
	public ResponseEntity<Object> SignUpPlayer(@RequestBody Player player) {
		ResponseEntity<Object> response;

		Map<String, Object> map = new LinkedHashMap<>();

		boolean userNameTaken = playerService.findByUserName(player.getUserName()) != null;

		if (userNameTaken) {
			map.put("error", "Name in use");
			response = new ResponseEntity<Object>(map, HttpStatus.FORBIDDEN);
		} else {
			playerService.save(player);
			map.put("id", player.getId());
			map.put("email", player.getUserName());
			response = new ResponseEntity<Object>(map, HttpStatus.CREATED);
		}

		return response;
	}

	private Player getPlayerFromAuthenticationObject (Authentication auth) {
		return isGuest(auth) ? null : playerService.findByUserName(auth.getName());
	}

	private boolean isGuest(Authentication auth) {
		return auth == null || auth instanceof AnonymousAuthenticationToken;
	}

}
