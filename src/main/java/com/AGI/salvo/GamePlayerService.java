package com.AGI.salvo;

import java.util.List;

public interface GamePlayerService {
	GamePlayer save (GamePlayer gamePlayer);
	GamePlayer findOne(long id);
	List<GamePlayer> findAll();
	JoinGameResult joinGame(Game game, Player player);
}
