package com.AGI.salvo;

import java.util.List;

public interface GameService {
	Game save(Game game);
	Game findOne(long id);
	List<Game> findAll();
	CreatedGameInfo createGame(Player player);
}
