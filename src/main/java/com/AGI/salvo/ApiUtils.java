package com.AGI.salvo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

public class ApiUtils {

	// Private constructor to avoid instantiation of the class
	private ApiUtils() {}

	public static Map<String, Object> getGamesDTO (Player authenticatedPlayer, List<Game> games) {
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		if (authenticatedPlayer != null) {
			mapDTO.put("player", getPlayerDTO(authenticatedPlayer));
		}
		mapDTO.put("games", getGamesDTO(games));
		return mapDTO;
	}

	public static List<Object> getGamesDTO(List<Game> games) {
		return games.stream().map(ApiUtils::getGameDTO).collect(Collectors.toList());
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


		Map<String, Object> allSalvoes = new HashMap<>();
		allSalvoes.put(Long.toString(currentGamePlayer.getId()), getGamePlayerSalvoesDTO(currentGamePlayer));

		optionalOtherGamePlayer.ifPresent(gamePlayer ->	allSalvoes.put(Long.toString(gamePlayer.getId()), getGamePlayerSalvoesDTO(gamePlayer)));

		dto.put("salvoes", allSalvoes);
		return dto;
	}

	public static Map<String, Object> getGamePlayerSalvoesDTO(GamePlayer gamePlayer) {
		Map<String, Object> dto = new LinkedHashMap<>();
		gamePlayer.getSalvoes()
				.stream()
				.sorted(Comparator.comparingInt(Salvo::getTurn))
				.forEach(salvo -> dto.put(Integer.toString(salvo.getTurn()), salvo.getLocations()));
		return dto;
	}

	public static Map<String, Object> getGameDTO(Game game) {
		Map<String, Object> dto = new LinkedHashMap<>();
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
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", gamePlayer.getId());
		dto.put("player", getPlayerDTO(gamePlayer.getPlayer()));
		if (gamePlayer.getScore() != null) {
			dto.put("score", gamePlayer.getScore().getPoints());
		}
		return dto;
	}

	public static Map<String, Object> getPlayerDTO(Player player) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", player.getId());
		dto.put("email", player.getUserName());
		return dto;
	}

	public static Map<String, Object> getShipDTO(Ship ship) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("type", ship.getType());
		dto.put("locations", ship.getLocations());
		return dto;
	}

	public static ResponseEntity<Object> getCreatedGameResponse (CreatedGameInfo createdGameInfo) {
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		HttpStatus httpStatus;
		if (createdGameInfo == null) {
			mapDTO.put("error", "Unauthorized");
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			mapDTO.put("gpId", createdGameInfo.getGamePlayer().getId());
			httpStatus = HttpStatus.CREATED;
		}
		return new ResponseEntity<Object>(mapDTO, httpStatus);
	}

	public static ResponseEntity<Object> getListOfPlayerInGameDTO (Game game) {
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		HttpStatus httpStatus;
		if (game == null) {
			mapDTO.put("error", "Forbidden: No such game!");
			httpStatus = HttpStatus.FORBIDDEN;
		} else {
			mapDTO.put("players", game.getGamePlayers().stream().map(gp -> getPlayerDTO(gp.getPlayer())).collect(Collectors.toList()));
			httpStatus = HttpStatus.OK;
		}
		return new ResponseEntity<>(mapDTO, httpStatus);
	}

	public static boolean hasOverlappedShipLocations(Set<String> takenLocations, Ship ship) {
		boolean hasOverlaps = false;
		for (String location : ship.getLocations()) {
			if (takenLocations.contains(location)) {
				hasOverlaps = true;
			}
			takenLocations.add(location);
		}
		return hasOverlaps;
	}

	public static boolean hasLinkedShipLocations(Ship ship) {
		boolean horizontalOK = true;
		boolean verticalOK = true;
		List<String> locations = ship.getLocations();
		char currentLetter;
		int currentNumber;
		char nextLetter;
		int nextNumber;
		int horizontalDirection = 0;
		int verticalDirection = 0;
		// Looping only till the second to last element, because the loop compares each element with the next one
		for (int i = 0; i < locations.size() - 1 && (horizontalOK || verticalOK); ++i) {
			currentLetter = locations.get(i).charAt(0);
			currentNumber = Integer.parseInt(locations.get(i).substring(1));
			nextLetter = locations.get(i+1).charAt(0);
			nextNumber = Integer.parseInt(locations.get(i+1).substring(1));
			// Checking horizontal (same letter)
			if (currentLetter != nextLetter) {
				horizontalOK = false;
			} else {
				// So, if they have the same letter, check if the numbers are adjacent
				if (horizontalDirection == 0) {
					if (Math.abs(nextNumber - currentNumber) != 1) {
						horizontalOK = false;
					} else {
						horizontalDirection = nextNumber - currentNumber;
					}
				} else {
					// So, if horizontalDirection was defined previously
					if (nextNumber - currentNumber != horizontalDirection) {
						horizontalOK = false;
					}
				}
			}
			// Checking vertical (same number)
			if (currentNumber != nextNumber) {
				verticalOK = false;
			} else {
				// So, if they have the same number, check if the letters are adjacent
				if (verticalDirection == 0) {
					if (Math.abs(nextLetter - currentLetter) != 1) {
						verticalOK = false;
					} else {
						verticalDirection = nextLetter - currentLetter;
					}
				} else {
					// So, if verticalDirection was defined previously
					if (nextLetter - currentLetter != verticalDirection) {
						verticalOK = false;
					}
				}
			}
		}


		return horizontalOK || verticalOK;
	}
}
