package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GamePlayerServiceImpl implements GamePlayerService {

	@Autowired
	private GamePlayerRepository gamePlayerRepository;

	@Override
	public GamePlayer save(GamePlayer gamePlayer) {
		return gamePlayerRepository.save(gamePlayer);
	}

	@Override
	public GamePlayer findOne(long id) {
		return gamePlayerRepository.findOne(id);
	}

	@Override
	public List<GamePlayer> findAll() {
		return gamePlayerRepository.findAll();
	}

	@Override
	public JoinGameResult joinGame(Game game, Player player) {
		final ActionResult actionResult;
		GamePlayer gamePlayer = null;

		if (player == null) {
			actionResult = ActionResult.UNAUTHORIZED;
		} else {
			if (game == null || game.getGamePlayers().size() == 2) {
				actionResult = ActionResult.FORBIDDEN;
			} else {
				gamePlayer = new GamePlayer(game, player);
				save(gamePlayer);
				actionResult = ActionResult.CREATED;
			}
		}
		return new JoinGameResult(actionResult, Optional.of(gamePlayer));
	}

}
