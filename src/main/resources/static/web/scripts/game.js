var data;
var $headerOutlet;
var shipGrid;
var salvoGrid;
var uppercaseAsciiStart = 65;

$(function () {
	$headerOutlet = $('#app-header-outlet');
	shipGrid = getNewGrid(10, 10, true);
	salvoGrid = getNewGrid(10, 10, true);

	$('#app-ship-grid-outlet').append(shipGrid);
	$('#app-salvo-grid-outlet').html(salvoGrid);

	var gamePlayerIndex = getUrlSearchObject().gp;

	if (gamePlayerIndex != null) {
		getJSON("/api/game_view/" + gamePlayerIndex, onDataReady, onRequestFailed);
		//$.getJSON("/api/game_view/" + gamePlayerIndex).done(onDataReady).fail(onRequestFailed);
	}
});

function onDataReady(response) {
	console.log("Data retrieved:",response);
	
	data = response;
	
	// Setup header
	displayHeader(data.currentGamePlayer, data.hasOwnProperty("otherGamePlayer") ? data.otherGamePlayer : null);

	// Setup ships
	displayShips(data.ships);
	
	// Setup salvoes
	displaySalvoes(data.salvoes, data.currentGamePlayer.id, data.hasOwnProperty("otherGamePlayer") ? data.otherGamePlayer.id : null);
}

function onRequestFailed(status) {
	console.log("Error: " + status);
}

function displayHeader(currentGamePlayer, otherGamePlayer) {
    var output = currentGamePlayer.player.email + "(you) ";
    if (otherGamePlayer != null) {
        output += "vs. " + otherGamePlayer.player.email;
    } else {
        output += "waiting for contender...";
    }
	$headerOutlet.text(output);
}

function displayShips(ships) {
	for (var i = 0; i < ships.length; ++i) {
		placeShip(ships[i]);
	}
}

function placeShip(ship) {
	for (var i = 0; i < ship.locations.length; ++i) {
		var shipPiece = document.createElement('div');
		shipPiece.classList.add('app-ship');
		
		var targetCell = getCellByName(shipGrid, ship.locations[i]);
		targetCell.appendChild(shipPiece);
	}
}

function displaySalvoes(salvoes, currentGpId, otherGpId) {
	// Place own salvoes
	placeSalvoes(salvoes[currentGpId], salvoGrid, ownShotHit);
	
	// Place enemy hits
	if (otherGpId != null) {
		placeSalvoes(salvoes[otherGpId], shipGrid, enemyShotHit);
	}
}

function placeSalvoes(salvoes, targetGrid, checkHitFunction) {
	for (var key in salvoes) {
		for (var i = 0; i < salvoes[key].length; ++i) {
			placeShot(salvoes[key][i], key, targetGrid, checkHitFunction);
		}
	}
}

function placeShot(locationStr, turn, targetGrid, checkHitFunction) {
	var shot = document.createElement('div');
	shot.classList.add('app-shot');
	if (checkHitFunction(locationStr)) {
		shot.classList.add('app-shot-hit');
	} else {
		shot.classList.add('app-shot-failed');
	}
	shot.textContent = turn;
	
	var targetCell = getCellByName(targetGrid, locationStr);
	targetCell.appendChild(shot);
}

function ownShotHit(location) {
	return false;
}

function enemyShotHit(location) {
	var locations = getAllShipLocations(data.ships);
	return locations.includes(location);
}

function getAllShipLocations(ships) {
	var locations = [];
	for (var shipKey in ships) {
		for (var locationKey in ships[shipKey].locations) {
			locations.push(ships[shipKey].locations[locationKey]);
		}
	}
	return locations;
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

function getNewGrid(xSize = 1, ySize = 1) {
	if (xSize <= 0 || ySize <= 0) {
		return null;
	}

	var grid = document.createElement('div');
	grid.classList.add('app-grid');

	// Starting at -1 to add the grid headers
	var start = -1;
	// Added +1 for the outer background frame
	var xEnd = xSize + 1;
	var yEnd = ySize + 1;

	var row;
	var cell;
	for (var y = start; y < yEnd; ++y) {
		row = document.createElement('tr');
		row.classList.add('app-grid-row');

		for (var x = start; x < xEnd; ++x) {
			// Create the cell
			if (y == -1) {
				cell = document.createElement('th');
				if (x != xEnd - 1) {
					cell.textContent = x >= 0 ? (x + 1) : '';
				}
			} else {
				if (x == -1) {
					cell = document.createElement('th');
					if (y != yEnd - 1) {
						cell.textContent = String.fromCharCode(y + uppercaseAsciiStart);
					}
				} else {
					cell = document.createElement('td');
				}
			}
			cell.classList.add('app-grid-cell');

			// Add the frame (outer cells) class
			if (x == -1 || x == xEnd - 1 || y == -1 || y == yEnd - 1) {
				cell.classList.add('app-grid-frame');
			}

			// Add the background class
			var classSuffix = getTileBackgroundClassSuffix(x, start, xEnd - 1, y, start, yEnd - 1);
			cell.classList.add('app-grid-terrain-' + classSuffix);

			row.appendChild(cell);
		}

		grid.appendChild(row);
	}

	return grid;

}

function getTileBackgroundClassSuffix(x, xStart, xEnd, y, yStart, yEnd) {
	var suffix = "";
	if (y == yStart) {
		suffix += "T";
	} else if (y == yEnd) {
		suffix += "B";
	} else {
		suffix += "C";
	}

	if (x == xStart) {
		suffix += "L";
	} else if (x == xEnd) {
		suffix += "R";
	} else {
		suffix += "C";
	}
	return suffix;
}

function getCellByName(grid, cellName) {
	var chars = cellName.split('');
	var rowIdx = chars[0].charCodeAt() - uppercaseAsciiStart + 1;
	var colIdx = chars[1];
	return getCell(grid, rowIdx, colIdx);
}

function getCell(grid, rowIdx, colIdx) {
	return grid.children[rowIdx].children[colIdx];
}

function getUrlSearchObject() {
	var obj = {};

	if (window.location.search == "") {
		return {};
	}

	var terms = window.location.search.substr(1).split('&');
	var kvPair;
	for (var i = 0; i < terms.length; ++i) {
		kvPair = terms[i].split('=');
		obj[kvPair[0]] = kvPair[1];
	}

	return obj;
}
