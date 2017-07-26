package com.AGI.salvo;

import java.util.*;
import java.util.stream.Collectors;

public class ApiUtils {

	// Private constructor to avoid instantiation of the class
	private ApiUtils() {}

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
		if (optionalOtherGamePlayer.isPresent()) {
			dto.put("otherGamePlayer", getGamePlayerDTO(optionalOtherGamePlayer.get()));
		}

		dto.put("ships", currentGamePlayer.getShips()
				.stream()
				.map(ApiUtils::getShipDTO)
				.collect(Collectors.toList())
		);


		Map<String, Object> allSalvoes = new HashMap<>();
		allSalvoes.put(Long.toString(currentGamePlayer.getId()), getGamePlayerSalvoesDTO(currentGamePlayer));

		if (optionalOtherGamePlayer.isPresent()) {
			GamePlayer otherGamePlayer = optionalOtherGamePlayer.get();
			allSalvoes.put(Long.toString(otherGamePlayer.getId()), getGamePlayerSalvoesDTO(otherGamePlayer));
		}
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
}
