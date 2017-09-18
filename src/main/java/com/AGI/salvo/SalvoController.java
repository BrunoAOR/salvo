package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


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
	@Autowired
	private SalvoService salvoService;
	@Autowired
	private ScoreService scoreService;

	@Autowired
	private DBCache dbCache;

	@RequestMapping(path = "/games", method = RequestMethod.GET)
	public Map<String, Object> getGamesDTO(Authentication auth) {
		String playerUserName = ApiUtils.getPlayerUserNameFromAuthenticationObject(auth);
		if (playerUserName != null) {
			if (!dbCache.hasPlayerDTO(playerUserName)) {
				dbCache.addPlayerDTO(playerUserName, ApiUtils.getPlayerDTO(playerService.findByUserName(playerUserName)));
			}
		}

		if (!dbCache.gamesDTOValid()) {
			List<Game> games = gameService.findAll();
			dbCache.updateGamesDTO(ApiUtils.getGamesDTO(games));
		}

		return ApiUtils.getApiGamesDTO(dbCache.getPlayerDTO(playerUserName), dbCache.getGamesDTO());
	}

	@RequestMapping(path = "/games", method = RequestMethod.POST)
	public ResponseEntity<Object> createGame(Authentication auth) {

		Player currentPlayer = getPlayerFromAuthenticationObject(auth);
		Optional<GamePlayer> createdGamePlayer = gameService.createGame(currentPlayer);
		ResponseEntity<Object> response = ApiUtils.getCreatedGameResponse(createdGamePlayer);
		if (response.getStatusCode() == HttpStatus.CREATED) {
			dbCache.gamesDTOValid(false);
		}
		return response;
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
		JoinGameResult joinGameResult = gamePlayerService.joinGame(game, authenticatedPlayer);
		ResponseEntity<Object> response = ApiUtils.getJoinGameResponse(joinGameResult);
		if (response.getStatusCode() == HttpStatus.CREATED) {
			dbCache.gamesDTOValid(false);
		}
		return response;
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
		if (!ApiUtils.isRequestAuthorized(authenticatedPlayer, gamePlayer)) {
			actionResult = ActionResult.UNAUTHORIZED;
		} else {
			actionResult = shipService.saveShips(receivedShipList, gamePlayer);
		}
		return ApiUtils.getSaveShipsResponse(actionResult);
	}

	@RequestMapping(path = "/games/players/{gamePlayerId}/salvos", method = RequestMethod.GET)
	public ResponseEntity<Object> getListOfSalvoes(@PathVariable long gamePlayerId, Authentication auth) {
		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		GamePlayer gamePlayer = gamePlayerService.findOne(gamePlayerId);
		return ApiUtils.getListOfSalvoesResponse(gamePlayer, authenticatedPlayer);
	}

	@RequestMapping(path = "/games/players/{gamePlayerId}/salvos", method = RequestMethod.POST)
	public ResponseEntity<Object> saveSalvo(@PathVariable long gamePlayerId, @RequestBody Salvo receivedSalvo, Authentication auth) {

		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		GamePlayer gamePlayer = authenticatedPlayer == null ? null : gamePlayerService.findOne(gamePlayerId);
		ActionResult actionResult;
		if (!ApiUtils.isRequestAuthorized(authenticatedPlayer, gamePlayer)) {
			actionResult = ActionResult.UNAUTHORIZED;
		} else {
			actionResult = salvoService.saveSalvo(receivedSalvo, gamePlayer);
		}

		if (actionResult == ActionResult.CREATED) {
			if(ApiUtils.isGameOver(gamePlayer.getGame())) {
				scoreService.saveScores(gamePlayer.getGame());
				dbCache.gamesDTOValid(false);
			}
		}

		return ApiUtils.getSaveSalvoResponse(actionResult);
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
		if (ApiUtils.isGuest(auth)){
			return null;
		}
		return playerService.findByUserName(auth.getName());
	}


}
