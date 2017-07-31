package com.AGI.salvo;

import java.util.List;

public interface ShipService {
	Ship save(Ship game);
	Ship findOne(long id);
	List<Ship> findAll();
	ActionResult saveShips(List<Ship> ship, GamePlayer gamePlayer);
}
