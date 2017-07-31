package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api")
public class SalvoController {

	@Autowired
	private GameService gameService;
	@Autowired
	private GamePlayerService gamePlayerService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private ShipService shipService;

	@RequestMapping(path = "/games", method = RequestMethod.GET)
	public Map<String, Object> getGamesDTO(Authentication auth) {

		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		List<Game> games = gameService.findAll();
		return ApiUtils.getGamesDTO(authenticatedPlayer, games);
	}

	@RequestMapping(path = "/games", method = RequestMethod.POST)
	public ResponseEntity<Object> createGame(Authentication auth) {

		Player currentPlayer = getPlayerFromAuthenticationObject(auth);
		Optional<GamePlayer> createdGamePlayer = gameService.createGame(currentPlayer);
		return ApiUtils.getCreatedGameResponse(createdGamePlayer);
	}

	@RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.GET)
	public ResponseEntity<Object> getListOfPlayerInGameDTO (@PathVariable long gameId) {

		Game game = gameService.findOne(gameId);
		return ApiUtils.getListOfPlayerInGameResponse(game);
	}

	@RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.POST)
	public ResponseEntity<Object> joinGame (@PathVariable long gameId, Authentication auth) {

		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		Game game = authenticatedPlayer == null ? null : gameService.findOne(gameId);
		JoinGameResult joinGameResult = gamePlayerService.JoinGame(game, authenticatedPlayer);
		return ApiUtils.getJoinGameResponse(joinGameResult);
	}

	@RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.GET)
	public ResponseEntity<Object> getListOfShips(@PathVariable long gamePlayerId, Authentication auth) {

		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		GamePlayer gamePlayer = gamePlayerService.findOne(gamePlayerId);
		return ApiUtils.getListOfShipsResponse(gamePlayer, authenticatedPlayer);
	}

	@RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
	public ResponseEntity<Object> saveListOfShips(@PathVariable long gamePlayerId, @RequestBody List<Ship> receivedShipList, Authentication auth) {

		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		GamePlayer gamePlayer = authenticatedPlayer == null ? null : gamePlayerService.findOne(gamePlayerId);
		ActionResult actionResult;
		if (!isShipCreationAuthorized(authenticatedPlayer, gamePlayer)) {
			actionResult = ActionResult.UNAUTHORIZED;
		} else {
			actionResult = shipService.saveShips(receivedShipList, gamePlayer);
		}
		return ApiUtils.getSaveShipsResponse(actionResult);
	}

	@RequestMapping(path = "/game_view/{gamePlayerId}", method = RequestMethod.GET)
	public ResponseEntity<Object> getGameViewDTO(@PathVariable long gamePlayerId, Authentication auth) {

		GamePlayer requestedGamePlayer = gamePlayerService.findOne(gamePlayerId);
		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		return ApiUtils.getGameViewResponse(requestedGamePlayer, authenticatedPlayer);
	}

	@RequestMapping(path = "/players", method = RequestMethod.POST)
	public ResponseEntity<Object> SignUpPlayer(@RequestBody Player player) {

		SignUpPlayerResult signUpPlayerResult = playerService.signUpPlayer(player);
		return ApiUtils.getSignUpPlayerResponse(signUpPlayerResult);
	}

	private Player getPlayerFromAuthenticationObject (Authentication auth) {
		return isGuest(auth) ? null : playerService.findByUserName(auth.getName());
	}

	private boolean isGuest(Authentication auth) {
		return auth == null || auth instanceof AnonymousAuthenticationToken;
	}

	private boolean isShipCreationAuthorized(Player authenticatedPlayer, GamePlayer gamePlayer) {
		if (authenticatedPlayer == null || gamePlayer == null || gamePlayer.getPlayer() != authenticatedPlayer)  {
			return false;
		} else {
			return true;
		}
	}
}
