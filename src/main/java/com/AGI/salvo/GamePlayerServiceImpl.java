package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GamePlayerServiceImpl implements GamePlayerService {

	@Autowired
	private GamePlayerRepository gamePlayerRepository;

	public GamePlayer findOne(long id) {
		return gamePlayerRepository.findOne(id);
	}
}
