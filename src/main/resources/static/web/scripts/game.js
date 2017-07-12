var $headerOutlet;
var grid;
var uppercaseASCIIstart = 65;

$(function () {
	$headerOutlet = $('#app-header-outlet');

	grid = getNewGrid(10, 10, true);
	$('#app-grid-outlet').html(grid);

	var gamePlayerIndex = getUrlSearchObject().gp;

	if (gamePlayerIndex != null) {
		getJSON("/api/game_view/" + gamePlayerIndex, onDataReady, onRequestFailed);
		//$.getJSON("/api/game_view/" + gamePlayerIndex).done(onDataReady).fail(onRequestFailed);
	}
});

function onDataReady(data) {
	console.log("Data retrieved:",data);

	// Setup header
	displayHeader(data.gamePlayers, getUrlSearchObject().gp);

	// Setup ships
	displayShips(data.ships);
}

function onRequestFailed(status) {
	console.log("Error: " + status);
}

function displayHeader(gamePlayers, currentGamePlayerIndex) {
	var email0 = gamePlayers[0].player.email;
	var email1 = gamePlayers[1].player.email;


	if (gamePlayers[0].id == currentGamePlayerIndex) {
		$headerOutlet.text(email0 + "(you) vs. " + email1);
	} else {
		$headerOutlet.text(email1 + "(you) vs. " + email0);
	}
}

function displayShips(ships) {
	for (var i = 0; i < ships.length; ++i) {
		placeShip(ships[i]);
	}
}

function placeShip(ship) {
	for (var i = 0; i < ship.locations.length; ++i) {
		var cell = getCellByName(grid, ship.locations[i]);
		cell.textContent = "S";
	}
}

function getJSON(url, successCallback, failCallback) {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 200) {
				successCallback(JSON.parse(this.responseText));
			} else if (this.status >= 400 && this.status < 600) {
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
						cell.textContent = String.fromCharCode(y + uppercaseASCIIstart);
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
	var rowIdx = chars[0].charCodeAt() - uppercaseASCIIstart + 1;
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
