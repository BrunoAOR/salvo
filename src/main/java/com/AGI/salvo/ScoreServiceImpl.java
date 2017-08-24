package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {

	@Autowired
	ScoreRepository scoreRepository;

	@Override
	public Score save(Score score) {
		return scoreRepository.save(score);
	}

	@Override
	public Score findOne(long id) {
		return scoreRepository.findOne(id);
	}

	@Override
	public List<Score> findAll() {
		return scoreRepository.findAll();
	}

	@Override
	public List<Score> saveScores(Game game) {
		List<Score> savedScores = new ArrayList<>();

		List<GamePlayer> gamePlayers = new ArrayList<>(game.getGamePlayers());

		if (gamePlayers.size() == 2) {
			boolean isGP0Dead = false;
			boolean isGP1Dead = false;
			if (ApiUtils.getRemainingShips(gamePlayers.get(0)) == 0) {
				isGP0Dead = true;
			}
			if (ApiUtils.getRemainingShips(gamePlayers.get(1)) == 0) {
				isGP1Dead = true;
			}
			if (isGP0Dead && isGP1Dead) {
				// TIED GAME
				savedScores.add(scoreRepository.save(new Score (game, gamePlayers.get(0).getPlayer(), 0.5, new Date())));
				savedScores.add(scoreRepository.save(new Score (game, gamePlayers.get(1).getPlayer(), 0.5, new Date())));
			} else if (isGP1Dead) {
				// GP0 WON
				savedScores.add(scoreRepository.save(new Score (game, gamePlayers.get(0).getPlayer(), 1, new Date())));
				savedScores.add(scoreRepository.save(new Score (game, gamePlayers.get(1).getPlayer(), 0, new Date())));
			} else if (isGP0Dead) {
				// GP1 WON
				savedScores.add(scoreRepository.save(new Score (game, gamePlayers.get(0).getPlayer(), 0, new Date())));
				savedScores.add(scoreRepository.save(new Score (game, gamePlayers.get(1).getPlayer(), 1, new Date())));
			}
		}

		return savedScores;
	}
}
