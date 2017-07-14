var $gamesList;
var $leaderBoard;

$(function () {
	$gamesList = $('#games-list');
	$leaderBoard = $('#leaderboard');
	getJSON('/api/games', onDataReady, onRequestFailed);
})

function onDataReady(games) {
	// Build Games List
	for (var i = 0; i < games.length; ++i) {
		$gamesList.append(getGameRow(games[i]));
	}

	// Build Leaderboard
	var sortedLeaderBoardArray = getLeaderBoardArray(games);
	console.log(sortedLeaderBoardArray);
	for (var i = 0; i < sortedLeaderBoardArray.length; ++i) {
		$leaderBoard.append(getLeaderBoardRow(sortedLeaderBoardArray[i]));
	}
}

function onRequestFailed(status) {
	console.log("Error: " + status);
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
