var $gamesTbody;
var $leaderboardTbody;

$(function () {
	$gamesTbody = $('#games-tbody');
	$leaderboardTbody = $('#leaderboard-tbody');
	$('#app-btn-show-sign-in').on('click', showSignInForm);
	$('#app-btn-sign-out').on('click', signOut);
	$('#app-btn-sign-in').on('click', signIn);
	$('#app-btn-sign-up').on('click', signUp);
	$('#app-btn-sign-cancel').on('click', hideSignInForm);

	refreshData();

})

function refreshData() {
	getJSON('/api/games', onDataReady, onRequestFailed);
}

function onDataReady(data) {
	// Setup and sign in/out buttons
	if (data.hasOwnProperty("player")) {
		$('#app-greeting-div').text("Hi " + data.player.email.substring(0, data.player.email.indexOf('@')) + "!");
		$('#app-btn-sign-out').show(0);
		$('#app-btn-show-sign-in').hide(0);
	} else {
		$('#app-greeting-div').text("");
		$('#app-btn-sign-out').hide(0);
		$('#app-btn-show-sign-in').show(0);
	}

	// Build Games List
	buildGameList(data);

	// Build Leaderboard
	buildLeaderboard(data.games);
}

function onRequestFailed(status) {
	console.log("Error: " + status);
}

function buildGameList(data) {
	var player = data.hasOwnProperty("player") ? data.player : null;
	var games = data.games;

	$gamesTbody.empty();
	var row;
	var cell;
	var game;
	var button;
	for (var i = -1; i < games.length; ++i) {

		if (i == -1) {
			if (player != null) {
				row = document.createElement("tr");
				cell = appendElementWithTextContent(row, "td", "");
				cell.setAttribute("colspan", "4");
				cell.className = "text-center";
				button = appendElementWithTextContent(cell, "button", "Create game");
				button.className = "btn btn-success";
				button.addEventListener("click", function () {
					tryCreateGame(onCreateGame, onCreateGameFailed);
				});
				$gamesTbody.append(row);
			}
			continue;
		}

		game = games[i];
		row = document.createElement("tr");
		row.className = "app-table-row";

		// Player 1
		cell = appendElementWithTextContent(row, "td", game.gamePlayers[0].player.email);
		cell.className = "text-center app-cell-player";

		// Vs
		cell = appendElementWithTextContent(row, "td", "VS.");
		cell.className = "text-center app-cell-vs"

		// Player 2
		cell = appendElementWithTextContent(row, "td", game.gamePlayers.length == 2 ? game.gamePlayers[1].player.email : "");
		cell.className = "text-center app-cell-player";

		// Button
		if (player != null) {
			cell = appendElementWithTextContent(row, "td", "");
			cell.className = "text-center app-cell-btn";
			// No text content for now...
			button = appendElementWithTextContent(cell, "button", "");
			button.className = "btn app-btn-sm";

			if (isPlayerInGame(player.id, game)) {
				button.textContent = "Play on";
				button.classList.add("btn-success");
				var gpId = getGamePlayerIdForPlayerIdInGameObj(player.id, game);
				gpId = gpId != null ? gpId : -1;
				button.setAttribute("data-gp-id", gpId);
				button.addEventListener("click", function () {
					playGame(this.getAttribute("data-gp-id"));
				});
			} else if (isGameJoinable(game)) {
				button.textContent = "Join";
				button.classList.add("btn-success");
				button.setAttribute("data-game-id", game.id);
				button.addEventListener("click", function() {
					tryJoinGame(this.getAttribute("data-game-id"), onJoinGame,onJoinGameFailed);
				});
			} else {
				button.textContent = "Watch";
				button.classList.add("btn-primary");
			}


		}


		$gamesTbody.append(row);
	}


}

function appendElementWithTextContent(parent, elementName, content) {
	var element = document.createElement(elementName);
	element.textContent = content;
	parent.appendChild(element);
	return element;
}

function isPlayerInGame(playerId, gameObj) {
	for (var gp of gameObj.gamePlayers) {
		if (gp.player.id == playerId) {
			return true;
		}
	}
	return false;
}

function isGameJoinable(gameObj) {
	return gameObj.gamePlayers.length == 1;
}

function getGamePlayerIdForPlayerIdInGameObj(playerId, gameObj) {
	for (var gp of gameObj.gamePlayers) {
		if (gp.player.id == playerId) {
			return gp.id;
		}
	}
	return null;
}

function playGame(gamePlayerId) {
	location.href = "/web/game.html?gp=" + gamePlayerId;
}

function tryCreateGame(successCallback, failureCallback) {
	if (tryCreateGame.processing == true) {
		return;
	}
	
	tryCreateGame.processing = true;
	
	var url = "/api/games";
	tryPostJson(url, null, successCallback, failureCallback);
}

function onCreateGame(response) {
	tryCreateGame.processing = false;
	var responseObj = JSON.parse(response.responseText);
	if (responseObj.hasOwnProperty("gpId")) {
		playGame(responseObj.gpId);
	}
}

function onCreateGameFailed(response) {
	tryCreateGame.processing = false;
	var message = "Failed to create game!\n";
	var responseObj = JSON.parse(response.responseText);
	if (responseObj.hasOwnProperty("error")) {
		message += responseObj.error;
	}
	alert(message);
}

function tryJoinGame(gameId, successCallback, failureCallback) {
	if (tryJoinGame.processing == true) {
		return;
	}
	
	tryJoinGame.processing = true;
	
	var url= "/api/games/" + gameId + "/players";
	tryPostJson(url, null, successCallback, failureCallback);
}

function onJoinGame(response) {
	tryJoinGame.processing = false;
	var responseObj = JSON.parse(response.responseText);
	if (responseObj.hasOwnProperty("gpId")) {
		playGame(responseObj.gpId);
	}
}

function onJoinGameFailed(response) {
	tryJoinGame.processing = false;
	var message = "Failed to join game!\n";
	var responseObj = JSON.parse(response.responseText);
	if (responseObj.hasOwnProperty("error")) {
		message += responseObj.error;
	}
	alert(message);
}

function buildLeaderboard(games) {
	$leaderboardTbody.empty();
	var sortedLeaderBoardArray = getLeaderBoardArray(games);
	for (var i = 0; i < sortedLeaderBoardArray.length; ++i) {
		$leaderboardTbody.append(getLeaderBoardRow(sortedLeaderBoardArray[i]));
	}
}

function getLeaderBoardArray(gamesObj) {
	var obj = {};
	var sortedArray = [];

	var players;
	var player;
	var objKey;
	for (var i = 0; i < gamesObj.length; ++i) {
		gamePlayers = gamesObj[i].gamePlayers;
		for (var p = 0; p < gamePlayers.length; ++p) {
			gamePlayer = gamePlayers[p];
			objKey = gamePlayer.player.email;
			if (gamePlayer.hasOwnProperty("score")) {
				if (obj.hasOwnProperty(objKey)) {
					obj[objKey].Total += gamePlayer.score;
					switch (gamePlayer.score) {
						case 1:
							++obj[objKey].Won;
							break;
						case 0:
							++obj[objKey].Lost;
							break;
						case 0.5:
							++obj[objKey].Tied;
							break;
					}
				} else {
					obj[objKey] = {
						"Name": objKey,
						"Total": gamePlayer.score,
						"Won": gamePlayer.score == 1 ? 1 : 0,
						"Lost": gamePlayer.score == 0 ? 1 : 0,
						"Tied": gamePlayer.score == 0.5 ? 1 : 0
					}
				}
			}
		}
	}
	for (var key in obj) {
		sortedArray.push(obj[key]);
	}

	sortedArray.sort((a, b) => {
		a.Total - b.Total != 0 ? a.Total - b.Total : a.Won - b.Won;
	});

	return sortedArray;
}

function getLeaderBoardRow(playerInfo) {
	var row = document.createElement('tr');
	var cell;

	for (var key in playerInfo) {
		cell = document.createElement('td');
		cell.textContent = playerInfo[key];
		row.appendChild(cell);
	}
	return row;
}

function getJSON(url, successCallback, failCallback) {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 200) {
				successCallback(JSON.parse(this.responseText));
			} else {
				failCallback(this.status);
			}
		}

	}
	request.open("GET", url, true);
	request.send();
}


// ******************************	//
// 				SIGN IN RELATED					//
// ******************************	//


function trySignIn(dataObj, successCallback, failureCallback) {
	var url = "/api/login";
	
	var data = encondeObjToUrlEncodedFormat(dataObj);
	var request = new XMLHttpRequest();
	request.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 200) {
				successCallback(this);
			} else {
				failureCallback(this);
			}
		}

	}

	request.open("POST", url, true);
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
	request.send(data);
}


function tryPostJson(url, dataObj, successCallback, failureCallback) {	
	var request = new XMLHttpRequest();
	request.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 201) {
				successCallback(this);
			} else {
				failureCallback(this);
			}
		}

	}

	request.open("POST", url, true);
	request.setRequestHeader("Content-type", "application/json");
	if (dataObj != null) {
		request.send(JSON.stringify(dataObj));
	} else {
		request.send();
	}
}

function trySignUp(dataObj, successCallback, failureCallback) {
	// dataObj = {userName: "j.bauer@ctu.gov", password: "24"}
	var url = "/api/players";
	tryPostJson(url, dataObj, successCallback, failureCallback);
}


function trySignOut(successCallback, failureCallback) {
	var url = "/api/logout";
	var request = new XMLHttpRequest();
	request.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 200) {
				successCallback(this);
			} else {
				failureCallback(this);
			}
		}

	}

	request.open("POST", url, true);
	request.send();
}


function encondeObjToUrlEncodedFormat(obj) {
	var enconded = [];
	for (var key in obj) {
		enconded.push(encodeURIComponent(key) + "=" + encodeURIComponent(obj[key]));
	}
	return enconded.join("&");
}


function showSignInForm() {
	$('#app-sign-in-info').text("");
	$('#app-sign-in-div').slideDown(500);
}


function hideSignInForm() {
	$('#app-sign-in-div').slideUp(500, clearSignInForm);
}


function signIn() {
	if (signIn.processing == true) {
		return;
	}

	var dataObj = getValidatedUserDataObject();
	if (dataObj != null) {
		signIn.processing = true;
		trySignIn(dataObj, onSignIn, onSignInFail);
	}
}


function signUp() {
	if (signUp.processing == true) {
		return;
	}

	var dataObj = getValidatedUserDataObject();
	if (dataObj != null) {
		signUp.processing = true;
		trySignUp(dataObj, onSignUp, onSignUpFail);
	}
}


function signOut() {
	if (signOut.processing == true) {
		return;
	}

	signOut.processing = true;
	trySignOut(onSignOut);
}


function getValidatedUserDataObject(getLast) {
	if (getLast == true && getValidatedUserDataObject.last != 'undefined' && getValidatedUserDataObject.last != null) {
		return getValidatedUserDataObject.last;
	}

	var email = $('#app-input-email').val();
	var password = $('#app-input-password').val();

	var emailRegEx = /^\S+@\S+\.\S+$/;
	if (!emailRegEx.test(email)) {
		$('#app-sign-in-info').text("Invalid email");
		return null;
	}

	//	if (password.length < 8 || !/[A-Z]+/.test(password) || !/[a-z]+/.test(password) || !/[0-9]+/.test(password)) {
	//		console.log("Invalid password");
	//		return null;
	//	}

	if (password.length == 0 || /\s+/.test(password)) {
		$('#app-sign-in-info').text("Invalid password");
		return null;
	}
	getValidatedUserDataObject.last = {
		userName: email,
		password: password
	};
	return getValidatedUserDataObject.last;
}

function clearSignInForm() {
	$('#app-input-email').val("");
	$('#app-input-password').val("");
}


function onSignIn(response) {
	signIn.processing = false;
	$('#app-sign-in-info').text("Signed in successfully!");
	refreshData()
	$('#app-btn-show-sign-in').hide(0);
	$('#app-btn-sign-out').show(0);
	hideSignInForm();
}


function onSignInFail(response) {
	console.log(response);
	signIn.processing = false;
	var info = $('#app-sign-in-info');
	if (response.status == 401) {
		info.text("Wrong user name or password.");
	} else {
		info.text("An error occured. Try again.")
	}
}


function onSignUp(response) {
	signUp.processing = false;
	$('#app-sign-in-info').text("Signed up successfully!");
	trySignIn(getValidatedUserDataObject(true), onSignIn, onSignInFail);
}


function onSignUpFail(response) {
	signUp.processing = false;
	var info = $('#app-sign-in-info');
	if (response.status == 403) {
		var obj = JSON.parse(response.responseText);
		info.text("Error: " + (obj.hasOwnProperty("error") ? obj["error"] : response.status));
	} else {
		info.text("Error:" + response.status);
	}
}


function onSignOut(response) {
	signOut.processing = false;
	refreshData();
	$('#app-btn-sign-out').hide(0);
	$('#app-btn-show-sign-in').show(0);

	console.log("Successful sign out!");
}


function onSignOutFail(response) {
	signOut.processing = false;
	console.log("Error:" + response.status);

}
