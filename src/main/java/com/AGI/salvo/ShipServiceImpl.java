package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {

	@Autowired
	ShipRepository shipRepository;

	@Override
	public Ship save(Ship ship) {
		return shipRepository.save(ship);
	}

	@Override
	public Ship findOne(long id) {
		return shipRepository.findOne(id);
	}

	@Override
	public List<Ship> findAll() {
		return shipRepository.findAll();
	}

	@Override
	public ActionResult saveShips(List<Ship> ships, GamePlayer gamePlayer) {
		if (gamePlayer == null) {
			return ActionResult.UNAUTHORIZED;
		}
		if (gamePlayer.getShips().size() != 0){
			return ActionResult.FORBIDDEN;
		}
		if (!ShipUtils.areShipsValid(ships)) {
			return ActionResult.CONFLICT;
		}

		// So, if no errors were found, we actually finish setting up the ships and save them
		// Note: the ships should only be missing the gamePlayer
		ships.forEach(ship -> {
			ship.setGamePlayer(gamePlayer);
			gamePlayer.addShip(ship);
			save(ship);
		});

		return ActionResult.CREATED;
	}
}
