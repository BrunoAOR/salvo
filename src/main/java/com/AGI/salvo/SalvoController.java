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
		Map<String, Object> dto = new LinkedHashMap<>();

		// Get gamePlayer with the id
		GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);

		// Let's get the game info
		dto.put("id", gamePlayer.getGame().getId());
		dto.put("created", gamePlayer.getGame().getCreationDate());
		dto.put("gamePlayers",
				gamePlayer.getGame().getGamePlayers()
						.stream()
						.map(gp -> getGamePlayerDTO(gp))
						.collect(Collectors.toList())
		);
		dto.put("ships", gamePlayer.getShips()
				.stream()
				.map(ship -> getShipDTO(ship))
				.collect(Collectors.toList())
		);
		// Get the OTHER gameplayer
		GamePlayer otherGamePlayer = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp != gamePlayer).findAny().orElse(null);

		Map<String, Object> allSalvoes = new HashMap<>();
		allSalvoes.put(Long.toString(gamePlayerId), getGamePlayerSalvoesDTO(gamePlayer));
		allSalvoes.put(Long.toString(otherGamePlayer.getId()), getGamePlayerSalvoesDTO(otherGamePlayer));

		dto.put("salvoes", allSalvoes);
		return dto;
	}

	private Map<String, Object> getGamePlayerSalvoesDTO(GamePlayer gamePlayer) {
		Map<String, Object> dto = new LinkedHashMap<>();
		List<Salvo> sortedSalvos = gamePlayer.getSalvoes()
				.stream()
				.sorted((salvo1, salvo2) -> salvo1.getTurn() - salvo2.getTurn())
				.collect(Collectors.toList());
		for (Salvo salvo : sortedSalvos) {
			dto.put(Integer.toString(salvo.getTurn()), salvo.getLocations());
		}
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
