package com.AGI.salvo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ApiUtils {

	// Private constructor to avoid instantiation of the class
	private ApiUtils() {}

	public static Map<String, Object> getGamesDTO (Player authenticatedPlayer, List<Game> games) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		if (authenticatedPlayer != null) {
			mapDTO.put("player", getPlayerDTO(authenticatedPlayer));
		}
		mapDTO.put("games", getGamesDTO(games));
		return mapDTO;
	}

	public static List<Object> getGamesDTO(List<Game> games) {
		return games.stream().map(ApiUtils::getGameDTO).collect(Collectors.toList());
	}

	public static ResponseEntity<Object> getGameViewResponse(GamePlayer gamePlayer, Player authenticatedPlayer) {

		Map<String, Object> mapDTO = new LinkedHashMap<>();
		final HttpStatus httpStatus;
		if (authenticatedPlayer == null) {
			mapDTO.put("error", "User must log in!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else if (gamePlayer.getPlayer() != authenticatedPlayer) {
			// The signed in player is requesting for a game_view for a gamePlayer that is not his
			mapDTO.put("error", "Unauthorized to view game info from this player's perspective!");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			// The signed in player is requesting for a game_view for one of his gamePlayers
			mapDTO = ApiUtils.getGameViewDTO(gamePlayer);
			httpStatus = HttpStatus.OK;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}

	public static Map<String, Object> getGameViewDTO(GamePlayer currentGamePlayer) {

		final Map<String, Object> dto = new LinkedHashMap<>();

		// Get the OTHER gamePlayer
		Optional<GamePlayer> optionalOtherGamePlayer = currentGamePlayer.getGame().getGamePlayers()
				.stream()
				.filter(gp -> gp != currentGamePlayer)
				.findFirst();

		// Let's get the game info
		dto.put("id", currentGamePlayer.getGame().getId());
		dto.put("created", currentGamePlayer.getGame().getCreationDate());

		dto.put("currentGamePlayer", getGamePlayerDTO(currentGamePlayer));

		optionalOtherGamePlayer.ifPresent(gamePlayer -> dto.put("otherGamePlayer", getGamePlayerDTO(gamePlayer)));

		dto.put("ships", currentGamePlayer.getShips()
				.stream()
				.map(ApiUtils::getShipDTO)
				.collect(Collectors.toList())
		);


		final Map<String, Object> allSalvoes = new LinkedHashMap<>();
		allSalvoes.put(Long.toString(currentGamePlayer.getId()), getGamePlayerSalvoesDTO(currentGamePlayer));

		optionalOtherGamePlayer.ifPresent(gamePlayer ->	allSalvoes.put(Long.toString(gamePlayer.getId()), getGamePlayerSalvoesDTO(gamePlayer)));

		dto.put("salvoes", allSalvoes);

		dto.put("history", getHistoryDTO(currentGamePlayer, optionalOtherGamePlayer.orElse(null)));

		return dto;
	}

	public static Map<String, Object> getHistoryDTO (GamePlayer currentGamePlayer, GamePlayer otherGamePlayer) {
		final Map<String, Object> dto = new LinkedHashMap<>();

		if (otherGamePlayer != null) {
			dto.put("turn", getCurrentTurn(currentGamePlayer.getGame()));
			dto.put("current", getGamePlayerHistoryDTO(currentGamePlayer, otherGamePlayer));
			dto.put("other", getGamePlayerHistoryDTO(otherGamePlayer, currentGamePlayer));
		}

		return dto;
	}

	private static int getCurrentTurn (Game game) {
		final IntWrapper turn = new IntWrapper(0);

		game.getGamePlayers().forEach(gamePlayer -> gamePlayer.getSalvoes().forEach(salvo -> {
			if (salvo.getTurn() > turn.get()) {
				turn.set(salvo.getTurn());
			}
		}));

		return turn.get();
	}

	public static Map<String, Object> getGamePlayerHistoryDTO (GamePlayer currentGamePlayer, GamePlayer otherGamePlayer) {
		final Map<String, Object> gamePlayerHistoryDTO = new LinkedHashMap<>();

		final List<String> allHitsList = new ArrayList<>();
		final List<Map<String, Object>> eventsList = new ArrayList<>();



		// Get a map of all of the enemies initial ship sizes
		final Map<Ship, Integer> shipsRemainingSizes = new LinkedHashMap<>();
		otherGamePlayer.getShips().forEach(ship -> shipsRemainingSizes.put(ship, ship.getType().getLength()));

		// Now, check if the location of each ship (from the otherGamePlayer) got hit by any shot (location) of each salvo of the currentGamePlayer
		currentGamePlayer.getSalvoes().stream().sorted(Comparator.comparingInt(Salvo::getTurn)).forEach(salvo -> {
			salvo.getLocations().forEach(shot -> {
				otherGamePlayer.getShips().forEach(ship -> {
					ship.getLocations().forEach(shipLocation -> {
						if (Objects.equals(shot, shipLocation)) {
							// Update the allHitsList
							allHitsList.add(shot);
							// Update the eventsList with a new event of type hit
							updateEventsList(eventsList, salvo.getTurn(), ship.getType().toString(), "hit");
							//eventsList.add(getEventDTO(salvo.getTurn(), ship.getType().toString(), "hit", 1));
							// Update the eventsList with a new event of type sunk if applicable
							shipsRemainingSizes.put(ship, shipsRemainingSizes.get(ship) - 1);
							if (shipsRemainingSizes.get(ship) <= 0) {
								updateEventsList(eventsList, salvo.getTurn(), ship.getType().toString(), "sunk");
								// eventsList.add(getEventDTO(salvo.getTurn(), ship.getType().toString(), "sunk", 0));
							}
						}
					});
				});
			});
		});

		gamePlayerHistoryDTO.put("allHits", allHitsList);

		// sort the eventsList according to turn, then ship and then type (alphabetically will do because hit comes before sunk)
		eventsList.sort((a,b) -> {
			int currentCompare = (int)a.get("turn") - (int)b.get("turn");
			if (currentCompare != 0) {
				return currentCompare;
			}
			currentCompare = ((String)a.get("ship")).compareTo((String)b.get("ship"));
			if (currentCompare != 0) {
				return currentCompare;
			}
			currentCompare = ((String)a.get("type")).compareTo((String)b.get("type"));
			return currentCompare;
		});

		gamePlayerHistoryDTO.put("events", eventsList);
		return gamePlayerHistoryDTO;
	}

	private static void updateEventsList (List<Map<String, Object>> eventsList, int turn, String shipType, String eventType) {
		if (Objects.equals(eventType, "sunk")) {
			eventsList.add(getEventDTO(turn, shipType, "sunk", 0));
		} else if (Objects.equals(eventType, "hit")) {
			// Check if a hit already happened on the same turn and ship
			Optional<Map<String, Object>> optionalEvent = eventsList.stream()
					.filter(map -> (int)map.get("turn") == turn && Objects.equals(map.get("ship"), shipType) && Objects.equals(map.get("type"), eventType))
					.findFirst();
			if (optionalEvent.isPresent()) {
				optionalEvent.get().put("count", (int)optionalEvent.get().get("count") + 1);
			} else {
				eventsList.add(getEventDTO(turn, shipType, "hit", 1));
			}
		}
	}

	public static Map<String, Object> getEventDTO (int turn, String shipType, String eventType, int count) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		mapDTO.put("turn", turn);
		mapDTO.put("ship", shipType);
		mapDTO.put("type", eventType);
		if (count > 0) {
			mapDTO.put("count", count);
		}
		return mapDTO;
	}

	public static ResponseEntity<Object> getSignUpPlayerResponse (SignUpPlayerResult signUpPlayerResult) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		final HttpStatus httpStatus;

		switch (signUpPlayerResult.getActionResult()) {
			case FORBIDDEN:
				mapDTO.put("error", "Forbidden: Email already in use!");
				httpStatus = HttpStatus.FORBIDDEN;
				break;
			case CREATED:
				mapDTO.put("id", signUpPlayerResult.getPlayerOptional().get().getId());
				mapDTO.put("email", signUpPlayerResult.getPlayerOptional().get().getUserName());
				httpStatus = HttpStatus.CREATED;
				break;
			default:
				mapDTO.put("error", "Error: Something went wrong, but we have no idea what...");
				httpStatus = HttpStatus.NOT_FOUND;
				break;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}

	public static Map<String, Object> getGamePlayerSalvoesDTO(GamePlayer gamePlayer) {
		final Map<String, Object> dto = new LinkedHashMap<>();
		gamePlayer.getSalvoes()
				.stream()
				.sorted(Comparator.comparingInt(Salvo::getTurn))
				.forEach(salvo -> dto.put(Integer.toString(salvo.getTurn()), salvo.getLocations()));
		return dto;
	}

	public static Map<String, Object> getGameDTO(Game game) {
		final Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", game.getId());
		dto.put("created", game.getCreationDate());
		dto.put("gamePlayers",
				game.getGamePlayers()
						.stream()
						.map(ApiUtils::getGamePlayerDTO)
						.collect(Collectors.toList())
		);
		return dto;
	}


	public static Map<String, Object> getGamePlayerDTO(GamePlayer gamePlayer) {
		final Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", gamePlayer.getId());
		dto.put("player", getPlayerDTO(gamePlayer.getPlayer()));
		if (gamePlayer.getScore() != null) {
			dto.put("score", gamePlayer.getScore().getPoints());
		}
		return dto;
	}

	public static Map<String, Object> getPlayerDTO(Player player) {
		final Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", player.getId());
		dto.put("email", player.getUserName());
		return dto;
	}

	public static Map<String, Object> getShipDTO(Ship ship) {
		final Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("type", ship.getType());
		dto.put("locations", ship.getLocations());
		return dto;
	}

	public static ResponseEntity<Object> getCreatedGameResponse (Optional<GamePlayer> createdGamePlayer) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		final HttpStatus httpStatus;
		if (!createdGamePlayer.isPresent()) {
			mapDTO.put("error", "Unauthorized");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			mapDTO.put("gpId", createdGamePlayer.get().getId());
			httpStatus = HttpStatus.CREATED;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}

	public static ResponseEntity<Object> getListOfShipsResponse(GamePlayer gamePlayer, Player authenticatedPlayer) {
		final List<Object> ships;
		final HttpStatus httpStatus;

		if (authenticatedPlayer == null || gamePlayer == null || gamePlayer.getPlayer() != authenticatedPlayer) {
			ships = new ArrayList<>();
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			ships = gamePlayer.getShips().stream().map(ApiUtils::getShipDTO).collect(Collectors.toList());
			httpStatus = HttpStatus.OK;
		}
		return new ResponseEntity<>(ships, httpStatus);
	}

	public static ResponseEntity<Object> getListOfPlayerInGameResponse(Game game) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		final HttpStatus httpStatus;
		if (game == null) {
			mapDTO.put("error", "Forbidden: No such game!");
			httpStatus = HttpStatus.FORBIDDEN;
		} else {
			mapDTO.put("players", game.getGamePlayers().stream().map(gp -> getPlayerDTO(gp.getPlayer())).collect(Collectors.toList()));
			httpStatus = HttpStatus.OK;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}

	public static ResponseEntity<Object> getListOfSalvoesResponse(GamePlayer gamePlayer, Player authenticatedPlayer) {
		final Map<String, Object> salvoes;
		final HttpStatus httpStatus;

		if (authenticatedPlayer == null || gamePlayer == null || gamePlayer.getPlayer() != authenticatedPlayer) {
			salvoes = new LinkedHashMap<>();
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			salvoes = getGamePlayerSalvoesDTO(gamePlayer);;
			httpStatus = HttpStatus.OK;
		}
		return new ResponseEntity<>(salvoes, httpStatus);
	}

	public static ResponseEntity<Object> getJoinGameResponse (JoinGameResult joinGameResult) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		final HttpStatus httpStatus;

		switch (joinGameResult.getActionResult()) {
			case UNAUTHORIZED:
				mapDTO.put("error", "Unauthorized: User needs to sign in before joining a game!");
				httpStatus = HttpStatus.UNAUTHORIZED;
				break;
			case FORBIDDEN:
				mapDTO.put("error", "Forbidden: Action is not allowed!");
				httpStatus = HttpStatus.FORBIDDEN;
				break;
			case CREATED:
				mapDTO.put("gpId", joinGameResult.getGamePlayerOptional().get().getId());
				httpStatus = HttpStatus.CREATED;
				break;
			default:
				mapDTO.put("error", "Error: Something went wrong, but we have no idea what...");
				httpStatus = HttpStatus.NOT_FOUND;
				break;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}

	public static ResponseEntity<Object> getSaveShipsResponse (ActionResult actionResult) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		final HttpStatus httpStatus;

		switch (actionResult) {
			case UNAUTHORIZED:
				mapDTO.put("error", "Unauthorized: User can't add ships with current credentials!");
				httpStatus = HttpStatus.UNAUTHORIZED;
				break;
			case FORBIDDEN:
				mapDTO.put("error", "Forbidden: User may not add further ships!");
				httpStatus = HttpStatus.FORBIDDEN;
				break;
			case CONFLICT:
				mapDTO.put("error", "Conflict: Ships are not properly set up!");
				httpStatus = HttpStatus.CONFLICT;
				break;
			case CREATED:
				// Nothing gets added to the mapDTO object
				httpStatus = HttpStatus.CREATED;
				break;
			default:
				mapDTO.put("error", "Error: Something went wrong, but we have no idea what...");
				httpStatus = HttpStatus.NOT_FOUND;
				break;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}

	public static ResponseEntity<Object> getSaveSalvoResponse (ActionResult actionResult) {
		final Map<String, Object> mapDTO = new LinkedHashMap<>();
		final HttpStatus httpStatus;

		switch (actionResult) {
			case UNAUTHORIZED:
				mapDTO.put("error", "Unauthorized: User can't add ships with current credentials!");
				httpStatus = HttpStatus.UNAUTHORIZED;
				break;
			case FORBIDDEN:
				mapDTO.put("error", "Forbidden: User may not add further salvo for specified turn!");
				httpStatus = HttpStatus.FORBIDDEN;
				break;
			case CONFLICT:
				mapDTO.put("error", "Conflict: Salvo locations overlap with previous shots!");
				httpStatus = HttpStatus.CONFLICT;
				break;
			case CREATED:
				// Nothing gets added to the mapDTO object
				httpStatus = HttpStatus.CREATED;
				break;
			default:
				mapDTO.put("error", "Error: Something went wrong, but we have no idea what...");
				httpStatus = HttpStatus.NOT_FOUND;
				break;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}
}
