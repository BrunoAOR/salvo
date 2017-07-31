package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService{

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public Player save(Player player) {
		return playerRepository.save(player);
	}

	@Override
	public Player findOne(long id) {
		return playerRepository.findOne(id);
	}

	@Override
	public List<Player> findAll() {
		return playerRepository.findAll();
	}

	@Override
	public Player findByUserName (String name) {
		return playerRepository.findByUserName(name);
	}

}
