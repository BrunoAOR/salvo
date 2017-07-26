package com.AGI.salvo;

public interface PlayerService {
	Player findOne(long id);
	Player findByUserName(String name);
	Player save(Player player);
}
