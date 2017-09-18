package com.AGI.salvo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBCache {

	// Cache for the api/games endpoint
	private List<Object> gamesDTO;
	private boolean gamesDTOValid = false;

	// Cache for constant playerDTOs
	private Map<String, Map<String, Object>> players = new HashMap<>();

	// Cache for the api/games endpoint
	public List<Object> getGamesDTO() {
		return gamesDTO;
	}

	public void updateGamesDTO(List<Object> newGamesDTO) {
		gamesDTO = newGamesDTO;
		gamesDTOValid = true;
	}

	public void gamesDTOValid (boolean isValid) {
		gamesDTOValid = isValid;
	}

	public boolean gamesDTOValid () {
		return gamesDTOValid;
	}

	// Cache for constant playerDTOs
	public Map<String, Object> getPlayerDTO (String userName) {
		return players.get(userName);
	}

	public void addPlayerDTO(String userName, Map<String, Object> newPlayerDTO) {
		players.put(userName, newPlayerDTO);
	}

	public boolean hasPlayerDTO (String userName) {
		return players.containsKey(userName);
	}
}
