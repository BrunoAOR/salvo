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
	private GameService gameService;
	@Autowired
	private GamePlayerService gamePlayerService;

	@RequestMapping("/games")
	public List<Object> getGamesDTO() {
		List<Game> games = gameService.findAll();
		return ApiUtils.getGamesDTO(games);
	}

	@RequestMapping("/game_view/{gamePlayerId}")
	public Map<String, Object> getGameViewDTO(@PathVariable long gamePlayerId) {
		GamePlayer currentGamePlayer = gamePlayerService.findOne(gamePlayerId);
		return ApiUtils.getGameViewDTO(currentGamePlayer);
	}



}
