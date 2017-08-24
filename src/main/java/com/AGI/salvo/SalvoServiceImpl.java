package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvoServiceImpl implements SalvoService {

	@Autowired
	SalvoRepository salvoRepository;

	@Override
	public Salvo save(Salvo salvo) {
		return salvoRepository.save(salvo);
	}

	@Override
	public Salvo findOne(long id) {
		return salvoRepository.findOne(id);
	}

	@Override
	public List<Salvo> findAll() {
		return salvoRepository.findAll();
	}

	@Override
	public ActionResult saveSalvo(Salvo salvo, GamePlayer gamePlayer) {
		if (gamePlayer == null) {
			return ActionResult.UNAUTHORIZED;
		}

		if (!SalvoUtils.isSalvoValid(salvo, gamePlayer)) {
			return ActionResult.CONFLICT;
		}

		salvo.setTurn(gamePlayer.getSalvoes().size() + 1);

		// So, if no errors were found, we actually create and save the salvo
		// Note: the salvo should only be missing the gamePlayer
		salvo.setGamePlayer(gamePlayer);
		gamePlayer.addSalvo(salvo);

		save(salvo);

		return ActionResult.CREATED;
	}
}
