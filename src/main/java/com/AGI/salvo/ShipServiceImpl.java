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
}
