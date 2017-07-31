package com.AGI.salvo;

import java.util.List;
import java.util.Optional;

public interface GameService {
	Game save(Game game);
	Game findOne(long id);
	List<Game> findAll();
	Optional<GamePlayer> createGame(Player player);
}
