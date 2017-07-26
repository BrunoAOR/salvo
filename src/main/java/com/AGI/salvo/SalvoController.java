package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

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

	@RequestMapping("/games")
	public Map<String, Object> getGamesDTO(Authentication auth) {
		Map<String, Object> mapDTO = new LinkedHashMap<>();
		Player currentPlayer = getPlayerFromAuthenticationObject(auth);
		if (currentPlayer != null) {
			mapDTO.put("player", ApiUtils.getPlayerDTO(currentPlayer));
		}
		List<Game> games = gameService.findAll();
		mapDTO.put("games", ApiUtils.getGamesDTO(games));
		return mapDTO;
	}

	@RequestMapping("/game_view/{gamePlayerId}")
	public Map<String, Object> getGameViewDTO(@PathVariable long gamePlayerId) {
		GamePlayer currentGamePlayer = gamePlayerService.findOne(gamePlayerId);
		return ApiUtils.getGameViewDTO(currentGamePlayer);
	}

	@RequestMapping("/players")
	public ResponseEntity<Object> SignUpPlayer(@RequestBody Player player) {
		ResponseEntity<Object> response;

		Map<String, Object> map = new LinkedHashMap<>();

		boolean userNameTaken = playerService.findByUserName(player.getUserName()) != null;

		if (userNameTaken) {
			map.put("error", "Name in use");
			response = new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
		} else {
			playerService.save(player);
			map.put("id", player.getId());
			map.put("email", player.getUserName());
			response = new ResponseEntity<>(map, HttpStatus.CREATED);
		}

		return response;
	}

	private Player getPlayerFromAuthenticationObject (Authentication auth) {
		return isGuest(auth) ? null : playerService.findByUserName(auth.getName());
	}

	private boolean isGuest(Authentication auth) {
		return auth == null || auth instanceof AnonymousAuthenticationToken;
	}

}
