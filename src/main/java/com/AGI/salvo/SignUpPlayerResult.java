package com.AGI.salvo;

import java.util.Optional;

public class SignUpPlayerResult {
	private ActionResult actionResult;
	private Optional<Player> playerOptional;

	public SignUpPlayerResult(ActionResult actionResult, Optional<Player> playerOptional) {
		this.actionResult = actionResult;
		this.playerOptional = playerOptional;
	}

	public ActionResult getActionResult() {
		return actionResult;
	}

	public Optional<Player> getPlayerOptional() {
		return playerOptional;
	}
}
