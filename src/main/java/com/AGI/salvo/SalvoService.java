package com.AGI.salvo;

import java.util.List;

public interface SalvoService {
	Salvo save(Salvo salvo);
	Salvo findOne(long id);
	List<Salvo> findAll();
	ActionResult saveSalvo(Salvo salvo, GamePlayer gamePlayer);
}
