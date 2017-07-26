package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl implements PlayerService{

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public Player findOne(long id) {
		return null;
	}

	@Override
	public Player findByUserName (String name) {
		return playerRepository.findByUserName(name);
	}

	@Override
	public Player save(Player player) {
		return playerRepository.save(player);
	}
}
