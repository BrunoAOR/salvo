package com.AGI.salvo;

import java.util.List;

public interface PlayerService {
	Player save(Player player);
	Player findOne(long id);
	List<Player> findAll();
	Player findByUserName(String name);
}
