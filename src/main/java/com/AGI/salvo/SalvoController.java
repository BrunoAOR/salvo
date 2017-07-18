package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

	@Autowired
	private GameRepository gameRepository;
	@Autowired
	private GamePlayerRepository gamePlayerRepository;

	@RequestMapping("/games")
	public List<Object> getGamesDTO() {
		return gameRepository.findAll().stream().map(game -> getGameDTO(game)).collect(Collectors.toList());
	}

	@RequestMapping("/game_view/{gamePlayerId}")
	public Map<String, Object> getGameViewDTO(@PathVariable long gamePlayerId) {
		final Map<String, Object> dto = new LinkedHashMap<>();

		// Get gamePlayer with the id
		GamePlayer currentGamePlayer = gamePlayerRepository.findOne(gamePlayerId);
		// Get the OTHER gameplayer
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
				.map(ship -> getShipDTO(ship))
				.collect(Collectors.toList())
		);


		Map<String, Object> allSalvoes = new HashMap<>();
		allSalvoes.put(Long.toString(gamePlayerId), getGamePlayerSalvoesDTO(currentGamePlayer));

		if (optionalOtherGamePlayer.isPresent()) {
			GamePlayer otherGamePlayer = optionalOtherGamePlayer.get();
			allSalvoes.put(Long.toString(otherGamePlayer.getId()), getGamePlayerSalvoesDTO(otherGamePlayer));
		}
		dto.put("salvoes", allSalvoes);
		return dto;
	}

	private Map<String, Object> getGamePlayerSalvoesDTO(GamePlayer gamePlayer) {
		Map<String, Object> dto = new LinkedHashMap<>();
		gamePlayer.getSalvoes()
				.stream()
				.sorted(Comparator.comparingInt(Salvo::getTurn))
				.forEach(salvo -> dto.put(Integer.toString(salvo.getTurn()), salvo.getLocations()));
		return dto;
	}

	private Map<String, Object> getGameDTO(Game game) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", game.getId());
		dto.put("created", game.getCreationDate());
		dto.put("gamePlayers",
				game.getGamePlayers()
						.stream()
						.map(gamePlayer -> getGamePlayerDTO(gamePlayer))
						.collect(Collectors.toList())
		);
		return dto;
	}

	private Map<String, Object> getGamePlayerDTO(GamePlayer gamePlayer) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", gamePlayer.getId());
		dto.put("player", getPlayerDTO(gamePlayer.getPlayer()));
		if (gamePlayer.getScore() != null) {
			dto.put("score", gamePlayer.getScore().getPoints());
		}
		return dto;
	}

	private Map<String, Object> getPlayerDTO(Player player) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", player.getId());
		dto.put("email", player.getUserName());
		return dto;
	}

	private Map<String, Object> getShipDTO(Ship ship) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("type", ship.getType());
		dto.put("locations", ship.getLocations());
		return dto;
	}

}
