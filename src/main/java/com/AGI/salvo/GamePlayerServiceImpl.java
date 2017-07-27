package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
