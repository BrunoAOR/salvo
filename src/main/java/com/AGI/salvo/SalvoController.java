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
		CreatedGameInfo createdGameInfo = null;
		if (currentPlayer != null) {
			// Create game and gamePlayer for said game
			createdGameInfo = gameService.createGame(currentPlayer);
		}
		return ApiUtils.getCreatedGameResponse(createdGameInfo);
	}

	@RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.GET)
	public ResponseEntity<Object> getListOfPlayerInGameDTO (@PathVariable long gameId) {
		Game game = gameService.findOne(gameId);
		return ApiUtils.getListOfPlayerInGameDTO(game);
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
		response = new ResponseEntity<>(mapDTO, httpStatus);
		return response;
	}

	@RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.GET)
	public ResponseEntity<Object> getListOfShips(@PathVariable long gamePlayerId, Authentication auth) {
		ResponseEntity<Object> response;
		List<Object> ships = new ArrayList<>();
		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		GamePlayer gamePlayer = gamePlayerService.findOne(gamePlayerId);
		HttpStatus httpStatus;

		if (authenticatedPlayer == null || gamePlayer == null || gamePlayer.getPlayer().getId() != authenticatedPlayer.getId()) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			gamePlayer.getShips().forEach(ship -> ships.add(ApiUtils.getShipDTO(ship)));
			httpStatus = HttpStatus.OK;
		}

		response = new ResponseEntity<>(ships, httpStatus);
		return response;
	}

	@RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
	public ResponseEntity<Object> saveListOfShips(@PathVariable long gamePlayerId, @RequestBody List<Ship> receivedShipList, Authentication auth) {
		ResponseEntity<Object> response;
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);
		GamePlayer gamePlayer = gamePlayerService.findOne(gamePlayerId);
		HttpStatus httpStatus;

		if (authenticatedPlayer == null)  {
			mapDTO.put("error", "Unauthorized: User needs to sign in before trying to add ships!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else if (gamePlayer == null) {
			mapDTO.put("error", "Unauthorized: No such game player exists!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else if (gamePlayer.getPlayer().getId() != authenticatedPlayer.getId()) {
			mapDTO.put("error", "Unauthorized: Player is attempting to add ships for another player!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else if (gamePlayer.getShips().size() != 0){
			mapDTO.put("error", "Forbidden: The player has already added ships for this game!");
			httpStatus = HttpStatus.FORBIDDEN;
		} else {
			boolean lengthsOK = true;
			boolean overlapOK = true;
			boolean linkedOk = true;
			Set<String> seenLocations = new HashSet<>();
			for (Ship ship : receivedShipList) {
				if (ship.getType().getLength() != ship.getLocations().size()) {
					lengthsOK = false;
					break;
				}
				overlapOK = !ApiUtils.hasOverlappedShipLocations(seenLocations, ship);
				if (!overlapOK) {
					break;
				}

				linkedOk = ApiUtils.hasLinkedShipLocations(ship);
				if (!linkedOk) {
					break;
				}
			}
			if (!lengthsOK) {
				mapDTO.put("error", "Forbidden: The ships' locations' length are inconsistent with their types!");
				httpStatus = HttpStatus.FORBIDDEN;
			} else if (!overlapOK) {
				mapDTO.put("error", "Forbidden: The ships' locations overlap with each other!");
				httpStatus = HttpStatus.FORBIDDEN;
			} else if (!linkedOk) {
				mapDTO.put("error", "Forbidden: Each ship's locations must be in a straight line!");
				httpStatus = HttpStatus.FORBIDDEN;
			} else {
				// So, no errors...
				// Save ships in database
				receivedShipList.forEach(ship -> {
					ship.setGamePlayer(gamePlayer);
					gamePlayer.addShip(ship);
					shipService.save(ship);
				});
				// Nothing is added to the response map.
				httpStatus = HttpStatus.CREATED;
			}
		}
		response = new ResponseEntity<>(mapDTO, httpStatus);
		return response;
	}

	@RequestMapping(path = "/game_view/{gamePlayerId}", method = RequestMethod.GET)
	public ResponseEntity<Object> getGameViewDTO(@PathVariable long gamePlayerId, Authentication auth) {
		ResponseEntity<Object> response;

		GamePlayer requestedGamePlayer = gamePlayerService.findOne(gamePlayerId);
		Player authenticatedPlayer = getPlayerFromAuthenticationObject(auth);

		Map<String, Object> mapDTO = new LinkedHashMap<>();
		HttpStatus httpStatus;
		if (authenticatedPlayer == null) {
			mapDTO.put("error", "User must log in!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else if (requestedGamePlayer.getPlayer().getId() != authenticatedPlayer.getId()) {
			// The signed in player is requesting for a game_view for a gamePlayer that is not his
			mapDTO.put("error", "Unauthorized to view game info from this player's perspective!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			// The signed in player is requesting for a game_view for one of his gamePlayers
			mapDTO = ApiUtils.getGameViewDTO(requestedGamePlayer);
			httpStatus = HttpStatus.OK;
		}

		response = new ResponseEntity<>(mapDTO, httpStatus);
		return response;
	}

	@RequestMapping(path = "/players", method = RequestMethod.POST)
	public ResponseEntity<Object> SignUpPlayer(@RequestBody Player player) {
		ResponseEntity<Object> response;

		boolean userNameTaken = playerService.findByUserName(player.getUserName()) != null;

		Map<String, Object> map = new LinkedHashMap<>();
		HttpStatus httpStatus;

		if (userNameTaken) {
			map.put("error", "Name in use");
			httpStatus = HttpStatus.FORBIDDEN;
		} else {
			playerService.save(player);
			map.put("id", player.getId());
			map.put("email", player.getUserName());
			httpStatus = HttpStatus.CREATED;
		}
		response = new ResponseEntity<>(map, httpStatus);
		return response;
	}

	private Player getPlayerFromAuthenticationObject (Authentication auth) {
		return isGuest(auth) ? null : playerService.findByUserName(auth.getName());
	}

	private boolean isGuest(Authentication auth) {
		return auth == null || auth instanceof AnonymousAuthenticationToken;
	}

}
