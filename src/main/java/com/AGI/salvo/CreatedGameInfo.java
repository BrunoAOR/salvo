package com.AGI.salvo;

public class CreatedGameInfo {

	private Game game;
	private GamePlayer gamePlayer;

	public CreatedGameInfo(Game game, GamePlayer gamePlayer) {
		this.game = game;
		this.gamePlayer = gamePlayer;
	}

	public Game getGame() {
		return game;
	}

	public GamePlayer getGamePlayer() {
		return gamePlayer;
	}
}
