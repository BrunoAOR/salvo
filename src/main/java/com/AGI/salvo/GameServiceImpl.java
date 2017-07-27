package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

	@Autowired
	private GameRepository gameRepository;

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

}
