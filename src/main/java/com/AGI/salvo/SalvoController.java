package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

	@Autowired
	private GameRepository gameRepository;

	@RequestMapping("/games")
	public List<Object> getGamesDTO() {
		return gameRepository.findAll().stream().map(game -> getGameDTO(game)).collect(Collectors.toList());
	}

	private Map<String, Object> getGameDTO(Game game) {
		Map<String, Object> gameDTO = new LinkedHashMap<>();
		gameDTO.put("id", game.getId());
		gameDTO.put("created", game.getCreationDate());
		gameDTO.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> getGamePlayerDTO(gamePlayer)).collect(Collectors.toList()));
		return gameDTO;
	}

	private Map<String, Object> getGamePlayerDTO(GamePlayer gamePlayer) {
		Map<String, Object> gamePlayerDTO = new LinkedHashMap<>();
		gamePlayerDTO.put("id", gamePlayer.getId());
		gamePlayerDTO.put("player", getPlayerDTO(gamePlayer.getPlayer()));
		return gamePlayerDTO;
	}

	private Map<String, Object> getPlayerDTO(Player player) {
		Map<String, Object> playerDTO = new LinkedHashMap<>();
		playerDTO.put("id", player.getId());
		playerDTO.put("email", player.getUserName());
		return playerDTO;
	}

}
