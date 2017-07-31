package com.AGI.salvo;

import java.util.Optional;

public class JoinGameResult {

	private final ActionResult actionResult;
	private Optional<GamePlayer> gamePlayerOptional;

	public JoinGameResult(ActionResult actionResult, Optional<GamePlayer> gamePlayerOptional) {
		this.actionResult = actionResult;
		this.gamePlayerOptional = gamePlayerOptional;
	}

	public ActionResult getActionResult() {
		return actionResult;
	}

	public Optional<GamePlayer> getGamePlayerOptional() {
		return gamePlayerOptional;
	}
}
