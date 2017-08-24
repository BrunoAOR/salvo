package com.AGI.salvo;

import java.util.List;

public interface ScoreService {
	Score save(Score score);
	Score findOne(long id);
	List<Score> findAll();
	List<Score> saveScores(Game game);
}
