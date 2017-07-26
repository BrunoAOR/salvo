var $gamesList;
var $leaderboardTbody;

$(function () {
	$gamesList = $('#games-list');
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
	buildGameList(data.games);

	// Build Leaderboard
	buildLeaderboard(data.games);
}

function onRequestFailed(status) {
	console.log("Error: " + status);
}

function buildGameList(games) {
	$gamesList.empty();
	for (var i = 0; i < games.length; ++i) {
		$gamesList.append(getGameRow(games[i]));
	}

}

function buildLeaderboard(games) {
	$leaderboardTbody.empty();
	var sortedLeaderBoardArray = getLeaderBoardArray(games);
	for (var i = 0; i < sortedLeaderBoardArray.length; ++i) {
		$leaderboardTbody.append(getLeaderBoardRow(sortedLeaderBoardArray[i]));
	}
}

function getGameRow(game) {
	var $li = $(document.createElement("li"));
	var content = "";
	content += new Date(game.created).toLocaleString() + ": ";
	for (var i = 0; i < game.gamePlayers.length; ++i) {
		if (i > 0) {
			content += "  -  ";
		}
		content += game.gamePlayers[i].player.email;
	}
	$li.text(content);
	return $li;
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
	url = "/api/login";
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


function trySignUp(dataObj, successCallback, failureCallback) {
	url = "/api/players";
	// dataObj = {userName: "j.bauer@ctu.gov", password: "24"}

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
	request.send(JSON.stringify(dataObj));
}


function trySignOut(successCallback, failureCallback) {
	url = "/api/logout";
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
	$('app-sign-in-info').text("");
	$('#app-sign-in-div').slideDown(500);
}


function hideSignInForm() {
	$('#app-sign-in-div').slideUp(500);
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


function onSignIn(response) {
	signIn.processing = false;
	$('#app-sign-in-info').text("Signed in successfully!");
	refreshData()
	$('#app-btn-show-sign-in').hide(0);
	$('#app-btn-sign-out').show(0);
	hideSignInForm();
}


function onSignInFail(response) {
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
