package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

	@Autowired
	private GameRepository gameRepository;

	@Autowired
	private GamePlayerService gamePlayerService;

	@Override
	public Game save (Game game) {
		return gameRepository.save(game);
	}

	@Override
	public Game findOne(long id) {
		return gameRepository.findOne(id);
	}

	@Override
	public List<Game> findAll() {
		return gameRepository.findAll();
	}

	@Override
	public CreatedGameInfo createGame(Player player) {
		if (player == null) {
			return null;
		}
		Game game = new Game();
		save(game);
		GamePlayer gamePlayer = new GamePlayer(game, player);
		gamePlayerService.save(gamePlayer);
		return new CreatedGameInfo(game, gamePlayer);
	}
}
