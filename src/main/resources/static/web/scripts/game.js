let data;
let gridSize;
let gridOffset;
let $headerOutlet;
let $infoOutlet;
let $salvoBtn;
let $historyTBody;
let shipGrid;
let salvoGrid;
let previousGameState = "";
let uppercaseAsciiStart = 65;
let dataRefreshRate = 3000;


$(function () {
	$headerOutlet = $('#app-header-outlet');
	$infoOutlet = $('#app-ship-grid-info');
	$salvoBtn = $('#app-salvo-btn-container').find('button');
	$historyTBody = $('#app-history-tbody');
	$salvoBtn.on('click', SalvoPlacer.onSalvoBtnClick);

	gridSize = new Point(10, 10);
	gridOffset = new Point(1, 1);

	refreshData();
});

class Point {
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}
}

function refreshData() {
	let gamePlayerId = getUrlSearchObject().gp;

	if (gamePlayerId != null) {
		getJSON("/api/game_view/" + gamePlayerId, onDataReady, onRequestFailed);
	}
}

function onDataReady(response) {
	data = response;
	
	// Check if the previous game state has changed
	if (previousGameState == data.state && previousGameState.includes("WAIT")) {
		// Then only refreshData again later
		setTimeout(refreshData, dataRefreshRate);
		return;
	} else {
		previousGameState = data.state;
	}
	
	// Clear info-outlet and history table
	displayMessage("");
	$historyTBody.empty();
	
	shipGrid = getNewGrid(gridSize, gridOffset, 'app-ship-cell');
	salvoGrid = getNewGrid(gridSize, gridOffset, 'app-salvo-cell');

	$('#app-ship-grid-outlet').empty().append(shipGrid);
	$('#app-salvo-grid-outlet').empty().append(salvoGrid);

	// Default to hiding the firing button
	$salvoBtn.hide(0);

	// Setup header
	displayHeader(data.currentGamePlayer, data.hasOwnProperty("otherGamePlayer") ? data.otherGamePlayer : null);
	
	let otherGamePlayerId = data.hasOwnProperty("otherGamePlayer") ? data.otherGamePlayer.id : null;

	// Check state
	switch (data.state) {
		case "PLACE_SHIPS":
			$('#app-salvo-grid-container').hide(0);
			ShipsInfo.setupShipPlacing();
			break;
			
		case "WAIT_FOR_PLAYER":
			if (otherGamePlayerId == null) {
				displayMessage("Waiting for another player to join!");	
			} else {
				displayMessage("Waiting for other player to place ships!");
			}
			displayShips(data.ships);
			setTimeout(refreshData, dataRefreshRate);
			
			break;
			
		case "FIRE":
			$('#app-salvo-grid-container').show(0);
			$salvoBtn.show(0);
			displayShips(data.ships);
			displayHistoryTable(data.history);
			
			// Setup salvoes
			displaySalvoes(data.salvoes, data.currentGamePlayer.id, otherGamePlayerId);
			
			SalvoPlacer.setupSalvoPlacing();
			break;
			
		case "WAIT_FOR_TURN":
			displayMessage("Waiting for the other player to launch its salvo!");
			displayShips(data.ships);
			displayHistoryTable(data.history);
			
			// Setup salvoes
			displaySalvoes(data.salvoes, data.currentGamePlayer.id, otherGamePlayerId);
			
			setTimeout(refreshData, dataRefreshRate);
			
			break;
			
		case "WON":
		case "LOST":
		case "TIED":
			gameOver(data.state);
			displayShips(data.ships);
			displayHistoryTable(data.history);
			
			// Setup salvoes
			displaySalvoes(data.salvoes, data.currentGamePlayer.id, otherGamePlayerId);
			break;
	}

	
}

function onRequestFailed(response) {
	let responseBody = JSON.parse(response.responseText);
	$headerOutlet.text("Error " + response.status + ": " + responseBody.error);
	$('div').filter('.app-flex').hide();
}

function displayHeader(currentGamePlayer, otherGamePlayer) {
	let output = currentGamePlayer.player.email + "(you) ";
	if (otherGamePlayer != null) {
		output += "vs. " + otherGamePlayer.player.email;
	} else {
		output += "waiting for contender...";
	}
	$headerOutlet.text(output);
}

function gameOver(result) {
	switch (result) {
		case "WON":
			displayMessage("CONGRATULATIONS!<br>YOU WON!");
			break;
		case "TIED":
			displayMessage("Well, that wasn't so bad...<br>Tied game!");
			break;
		case "LOST":
			displayMessage("OOPS SORRY!<br>You lost!");
			break;
	}
}

function displayShips(ships) {
	for (let i = 0; i < ships.length; ++i) {
		placeShip(ships[i]);
	}
}

function placeShip(shipObj) {
	let ship = new Ship(
		getLocationPoint(shipObj.locations[0]),
		getShipFaceDirection(shipObj),
		ShipsInfo.shipsLengths[ShipsInfo.shipTypesObj.indexOf(shipObj.type)],
		shipGrid,
		gridSize
	);

	//	for (let i = 0; i < shipObj.locations.length; ++i) {
	//		let shipPiece = document.createElement('div');
	//		shipPiece.classList.add('app-ship');
	//
	//		let targetCell = getCellByName(shipGrid, shipObj.locations[i]);
	//		targetCell.appendChild(shipPiece);
	//	}
}

function getShipFaceDirection(shipObj) {
	if (shipObj.locations.length <= 1) {
		return null;
	}

	let front = shipObj.locations[0];
	let next = shipObj.locations[1];
	// Reminder: Letter is the row | Number is the column
	let frontLetter = front.substring(0, 1);
	let frontNumber = front.substring(1);
	let nextLetter = next.substring(0, 1);
	let nextNumber = next.substring(1);

	if (nextNumber > frontNumber) {
		// Growing to the right ==> Looking left
		return ShipsInfo.MoveDirection.LEFT;
	} else if (nextNumber < frontNumber) {
		// Growing to the left ==> Looking right
		return ShipsInfo.MoveDirection.RIGHT;
	} else if (nextLetter > frontLetter) {
		// Growing downwards ==> Looking up
		return ShipsInfo.MoveDirection.UP;
	} else {
		// Growing upwards ==> Looking down
		return ShipsInfo.MoveDirection.DOWN;
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
	for (let key in salvoes) {
		for (let i = 0; i < salvoes[key].length; ++i) {
			placeShot(salvoes[key][i], key, targetGrid, checkHitFunction);
		}
	}
}

function placeShot(locationStr, turn, targetGrid, checkHitFunction) {
	let shot = document.createElement('div');
	shot.classList.add('app-shot');
	if (checkHitFunction(locationStr)) {
		shot.classList.add('app-shot-hit');
	} else {
		shot.classList.add('app-shot-failed');
	}
	shot.textContent = turn;

	let targetCell = getCellByName(targetGrid, locationStr);
	targetCell.appendChild(shot);
}

function ownShotHit(location) {
	return data.history.current.allHits.includes(location);
}

function enemyShotHit(location) {
	let locations = getAllShipLocations(data.ships);
	return locations.includes(location);
}

function getAllShipLocations(ships) {
	let locations = [];
	for (let shipKey in ships) {
		for (let locationKey in ships[shipKey].locations) {
			locations.push(ships[shipKey].locations[locationKey]);
		}
	}
	return locations;
}

function displayHistoryTable(historyObj) {
	$historyTBody.empty();
	let currentLeftToSink = ShipsInfo.shipTypesObj.length;
	let otherLeftToSink = currentLeftToSink;

	let currentEventIndex = 0;
	let otherEventIndex = 0;

	for (let turn = 1; turn <= historyObj.turn; ++turn) {
		let tr = document.createElement('tr');

		// Turn number
		appendElementWithTextContent(tr, 'th', turn);

		let actionMessage;
		let events;
		// YOU columns
		// Actions
		actionMessage = "";
		events = historyObj.current.events;
		for (let i = currentEventIndex; i < events.length; i++) {
			let event = events[i];
			if (event.turn == turn) {
				if (actionMessage.length > 0) {
					actionMessage += '<br>';
				}

				if (event.type == 'sunk') {
					actionMessage += '<span class= "app-history-highlight">'
				}

				actionMessage += ShipsInfo.getReadableName(event.ship);
				actionMessage += ' ' + event.type;
				if (event.type == 'hit' && event.count > 1) {
					actionMessage += ' ' + event.count + ' times';
				}
				actionMessage += '!';

				if (event.type == 'sunk') {
					actionMessage += '</span>';
					--currentLeftToSink;
				}

				++currentEventIndex;
			} else {
				break;
			}
		}

		if (actionMessage == "") {
			actionMessage = "Did nothing...";
		}
		appendElementWithHtmlContent(tr, 'td', actionMessage);

		// Left to sink
		appendElementWithTextContent(tr, 'td', currentLeftToSink);

		// ENEMY columns
		// Actions
		actionMessage = "";
		events = historyObj.other.events;
		for (let i = otherEventIndex; i < events.length; i++) {
			let event = events[i];
			if (event.turn == turn) {
				if (actionMessage.length > 0) {
					actionMessage += '<br>';
				}

				if (event.type == 'sunk') {
					actionMessage += '<span class= "app-history-highlight">'
				}

				actionMessage += ShipsInfo.getReadableName(event.ship);
				actionMessage += " " + event.type;
				if (event.type == "hit" && event.count > 1) {
					actionMessage += " " + event.count + " times";
				}
				actionMessage += "!";

				if (event.type == "sunk") {
					actionMessage += '</span>';
					--otherLeftToSink;
				}

				++otherEventIndex;
			} else {
				break;
			}
		}
		if (actionMessage == "") {
			actionMessage = "Did nothing...";
		}
		appendElementWithHtmlContent(tr, 'td', actionMessage);

		// Left to sink
		appendElementWithTextContent(tr, 'td', otherLeftToSink);

		$historyTBody.append(tr);
	}

}

function appendElementWithTextContent(parent, elementName, content) {
	var element = document.createElement(elementName);
	element.textContent = content;
	parent.appendChild(element);
	return element;
}

function appendElementWithHtmlContent(parent, elementName, content) {
	var element = document.createElement(elementName);
	element.innerHTML = content;
	parent.appendChild(element);
	return element;
}

function getNewGrid(gridSize, gridOffset, extraGridClass) {
	if (gridSize.x <= 0 || gridSize.y <= 0) {
		return null;
	}

	let grid = document.createElement('div');
	grid.classList.add('app-grid');

	// Starting at -1 to add the grid headers
	let start = -gridOffset.x;
	// Added +1 for the outer background frame
	let xEnd = gridSize.x + gridOffset.x;
	let yEnd = gridSize.y + gridOffset.y;

	let row;
	let cell;
	for (let y = start; y < yEnd; ++y) {
		row = document.createElement('tr');
		row.classList.add('app-grid-row');

		for (let x = start; x < xEnd; ++x) {
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

			// Add extraGridClass to internal cells
			if (extraGridClass != undefined && (typeof extraGridClass) == 'string' && x >= 0 && x < gridSize.x && y >= 0 && y < gridSize.y) {
				cell.classList.add(extraGridClass);
				cell.setAttribute('data-address', getCellAddress(x, y));
			}

			// Add the frame (outer cells) class
			if (x == -1 || x == xEnd - 1 || y == -1 || y == yEnd - 1) {
				cell.classList.add('app-grid-frame');
			}

			// Add the background class
			let classSuffix = getTileBackgroundClassSuffix(x, start, xEnd - 1, y, start, yEnd - 1);
			cell.classList.add('app-grid-terrain-' + classSuffix);

			row.appendChild(cell);
		}

		grid.appendChild(row);
	}

	return grid;

}

function getCellAddress(x, y) {
	return "" + String.fromCharCode(y + uppercaseAsciiStart) + (x + 1);
}

function getTileBackgroundClassSuffix(x, xStart, xEnd, y, yStart, yEnd) {
	let suffix = "";
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
	let rowIdx = cellName.slice(0, 1).charCodeAt() - uppercaseAsciiStart + 1;
	let colIdx = cellName.slice(1);;
	return getCell(grid, rowIdx, colIdx);
}

function getCell(grid, rowIdx, colIdx) {
	return grid.children[rowIdx].children[colIdx];
}

function getCellName(locationPoint) {
	return String.fromCharCode(locationPoint.y + gridOffset.y + uppercaseAsciiStart - 1) + (locationPoint.x + gridOffset.x);
}

function getLocationPoint(cellName) {
	// Reminder: Letter is the row | Number is the column
	let letterIdx = (cellName.substring(0, 1)).charCodeAt() - uppercaseAsciiStart;
	let numberIdx = cellName.substring(1) - gridOffset.x;
	return new Point(numberIdx, letterIdx);

}

function getUrlSearchObject() {
	let obj = {};

	if (window.location.search != "") {
		// Remove the "?" using substring 1 and then split each query term
		let terms = window.location.search.substr(1).split('&');
		let kvPair;
		for (let i = 0; i < terms.length; ++i) {
			// Split each term at the "=" to get key-value-pairs
			kvPair = terms[i].split('=');
			obj[kvPair[0]] = kvPair[1];
		}
	}

	return obj;
}

function getJSON(url, successCallback, failureCallback) {
	let request = new XMLHttpRequest();
	request.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 200) {
				successCallback(JSON.parse(this.responseText));
			} else {
				failureCallback(this);
			}
		}

	}
	request.open("GET", url, true);
	request.send();
}

function tryPostJson(url, dataObj, successCallback, failureCallback) {
	let request = new XMLHttpRequest();
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


// ****************************** //
//					INFO MESSAGES					//
// ****************************** //

function displayMessage(htmlMessage) {
	// Clear any temporary message timeout if it exists
	if (displayTemporaryMessage.timeoutID != undefined && displayTemporaryMessage.timeoutID != 0) {
		clearTimeout(displayTemporaryMessage.timeoutID);
		displayTemporaryMessage.timeoutID = 0;
	}
	$infoOutlet.html(htmlMessage);
}

function displayTemporaryMessage(htmlMessage, waitDuration) {
	// If no temporary message is being displayed, we store the current message
	if (displayTemporaryMessage.timeoutID == undefined || displayTemporaryMessage.timeoutID == 0) {
		displayTemporaryMessage.previousMessage = $infoOutlet.html();
	} else {
		// If there was a temporary message, we'll stop its timeout
		clearTimeout(displayTemporaryMessage.timeoutID);
		displayTemporaryMessage.timeoutID = 0;
	}
	$infoOutlet.html(htmlMessage);
	displayTemporaryMessage.timeoutID = setTimeout(function () {
		displayPreviousMessage();
		displayTemporaryMessage.timeoutID = 0;
	}, waitDuration);
}

function displayPreviousMessage() {
	$infoOutlet.html(displayTemporaryMessage.previousMessage);
}


// ****************************** //
//					PLACING SHIPS					//
// ****************************** //

function trySubmitShips(data) {
	let gamePlayerId = getUrlSearchObject().gp;
	tryPostJson("/api/games/players/" + gamePlayerId + "/ships", data, onSubmitShips, onSubmitShipsFail);
}

function onSubmitShips(response) {
	refreshData();
}

function onSubmitShipsFail(response) {
	var responseObj = JSON.parse(response.responseText);
	alert("ERROR!\n" + responseObj.error);
	refreshData();
}

class ShipsInfo {

	// ENTRY POINT for ship placement
	static setupShipPlacing() {
		ShipsInfo.reset();
		displayMessage("Time to place your ships!<br>Use WASD to move the ship around the grid, Q&E to rotate it around the bow (front of the ship) and 'Enter' to place the ship.");
		setTimeout(function () {
			document.addEventListener("keydown", ShipsInfo.handleKeyDownEvent);
			ShipsInfo.requestNextShip();
		}, ShipsInfo.messageDisplayTime * 1.5);
	}

	static handleKeyDownEvent(event) {
		switch (event.keyCode) {
			case 87: //W
				ShipsInfo.moveShip(ShipsInfo.MoveDirection.UP);
				break;
			case 83: //S
				ShipsInfo.moveShip(ShipsInfo.MoveDirection.DOWN);
				break;
			case 65: //A
				ShipsInfo.moveShip(ShipsInfo.MoveDirection.LEFT);
				break;
			case 68: //D
				ShipsInfo.moveShip(ShipsInfo.MoveDirection.RIGHT);
				break;
			case 81: //Q
				ShipsInfo.rotateShip(ShipsInfo.RotationDirection.CCW);
				break;
			case 69: //E
				ShipsInfo.rotateShip(ShipsInfo.RotationDirection.CW);
				break;
			case 13: // Enter key
				ShipsInfo.saveShip();
				break;
		}
	}

	static getShipTypeReadable(index) {
		if (index >= 0 && index < ShipsInfo.shipTypesReadable.length) {
			return ShipsInfo.shipTypesReadable[index];
		} else {
			return null;
		}
	}

	static getShipTypeObj(index) {
		if (index >= 0 && index < ShipsInfo.shipTypesObj.length) {
			return ShipsInfo.shipTypesObj[index];
		} else {
			return null;
		}
	}

	static isLocationTaken(point) {
		for (let i = 0; i < ShipsInfo.takenLocations.length; ++i) {
			let currLocation = ShipsInfo.takenLocations[i];
			if (currLocation.x == point.x && currLocation.y == point.y) {
				return true;
			}
		}
		return false;
	}

	static moveShip(moveDirection) {
		ShipsInfo.currentShip.move(moveDirection);
	}

	static rotateShip(rotationDirection) {
		ShipsInfo.currentShip.rotate(rotationDirection);
	}

	static requestNextShip() {
		if (ShipsInfo.shipIndex < ShipsInfo.shipTypesReadable.length) {
			displayMessage("Placing ship " + (ShipsInfo.shipIndex + 1) + " out of " + ShipsInfo.shipTypesReadable.length + " (" + ShipsInfo.shipTypesReadable[ShipsInfo.shipIndex] + ")!<br>Use WASD to move the ship around the grid and Q&E to rotate it around the bow (front of the ship) and 'Enter' to place the ship.");
			if (ShipsInfo.currentShip == null) {
				ShipsInfo.currentShip = new Ship(
					new Point(0, 0),
					ShipsInfo.MoveDirection.UP,
					ShipsInfo.shipsLengths[ShipsInfo.shipIndex],
					shipGrid,
					gridSize
				);
			} else {
				ShipsInfo.currentShip = new Ship(
					new Point(ShipsInfo.currentShip.pos.x, ShipsInfo.currentShip.pos.y),
					ShipsInfo.currentShip.faceDirection,
					ShipsInfo.shipsLengths[ShipsInfo.shipIndex],
					shipGrid,
					gridSize
				);
			}
			ShipsInfo.currentShip.shipFront.classList.add("app-ship-active");
		} else {
			ShipsInfo.endShipPlacing();
		}
	}

	static saveShip() {
		if (ShipsInfo.currentShip.isValid) {
			ShipsInfo.placedShips.push(ShipsInfo.currentShip);
			let shipLocations = ShipsInfo.currentShip.getLocations();
			for (let i = 0; i < shipLocations.length; ++i) {
				ShipsInfo.takenLocations.push(shipLocations[i]);
			}
			++ShipsInfo.shipIndex;
			ShipsInfo.currentShip.shipFront.classList.remove("app-ship-active");
			ShipsInfo.requestNextShip();
			displayTemporaryMessage("Ship placed", ShipsInfo.messageDisplayTime / 2);
		} else {
			displayTemporaryMessage("Hey! You can't place a ship on top of another one!", ShipsInfo.messageDisplayTime);
		}
	}

	// EXIT POINT for ship placement
	static endShipPlacing() {
		document.removeEventListener("keydown", ShipsInfo.handleKeyDownEvent);
		// Build ships object
		let shipsData = ShipsInfo.getShipsDataArray();
		// Submit to server
		trySubmitShips(shipsData);
		ShipsInfo.reset();
		displayMessage("All ships placed!");
	}

	static getShipsDataArray() {
		let dataArray = [];
		for (let i = 0; i < ShipsInfo.placedShips.length; ++i) {
			let currentShip = ShipsInfo.placedShips[i];
			let locationsArray = [];
			let shipLocations = currentShip.getLocations();
			for (let j = 0; j < shipLocations.length; ++j) {
				locationsArray.push(getCellName(shipLocations[j]));
			}
			dataArray.push({
				type: ShipsInfo.shipTypesObj[i],
				locations: locationsArray
			});
		}
		return dataArray;
	}

	static reset() {
		ShipsInfo.placedShips = [];
		ShipsInfo.shipIndex = 0;
		ShipsInfo.currentShip = null;
		ShipsInfo.takenLocations = [];
	}

	static getReadableName(shipType) {
		let index = ShipsInfo.shipTypesObj.indexOf(shipType);
		if (index == -1) {
			return "";
		}
		return ShipsInfo.shipTypesReadable[index];
	}

}

// ShipsInfo constants
ShipsInfo.shipTypesReadable = ["Carrier", "Battleship", "Submarine", "Destroyer", "Patrol Boat"];
ShipsInfo.shipTypesObj = ["CARRIER", "BATTLESHIP", "SUBMARINE", "DESTROYER", "PATROL_BOAT"];
ShipsInfo.shipsLengths = [5, 4, 3, 3, 2];
ShipsInfo.MoveDirection = {
	UP: 0,
	RIGHT: 1,
	DOWN: 2,
	LEFT: 3
}
ShipsInfo.RotationDirection = {
	CW: 0,
	CCW: 1
}
ShipsInfo.messageDisplayTime = 1000;

// ShipsInfo variables
ShipsInfo.placedShips = [];
ShipsInfo.shipIndex = 0;
ShipsInfo.currentShip = null;
ShipsInfo.takenLocations = [];


class Ship {
	constructor(position, faceDirection, length, grid, gridSize) {
		this.pos = position;
		this.length = length;
		this.grid = grid;
		this.gridSize = gridSize;
		this.faceDirection = faceDirection;;
		this.isValid = false;

		// Create html elements
		this.shipParts = [];

		this.shipFront = document.createElement('div');
		this.shipFront.classList.add('app-ship');
		this.shipFront.classList.add('app-ship-front');
		switch (this.faceDirection) {
			case ShipsInfo.MoveDirection.UP:
				this.shipFront.classList.add('app-ship-up');
				break;
			case ShipsInfo.MoveDirection.DOWN:
				this.shipFront.classList.add('app-ship-down');
				break;
			case ShipsInfo.MoveDirection.LEFT:
				this.shipFront.classList.add('app-ship-left');
				break;
			case ShipsInfo.MoveDirection.RIGHT:
				this.shipFront.classList.add('app-ship-right');
				break;
		}


		this.shipParts.push(this.shipFront);

		let previousShipPart = this.shipFront;
		let newShipPart;
		for (let i = 1; i < this.length; ++i) {
			newShipPart = document.createElement('div');
			newShipPart.classList.add('app-ship-part');
			if (i == this.length - 1) {
				// End of ship
				newShipPart.classList.add('app-ship-back');
			} else {
				newShipPart.classList.add('app-ship-mid');
			}

			this.shipParts.push(newShipPart);

			previousShipPart.appendChild(newShipPart);
			previousShipPart = newShipPart;
		}

		this.render();
	}

	move(direction) {
		let invalidMove = true;
		switch (direction) {
			case ShipsInfo.MoveDirection.UP:
				if (this.canChangeStateTo(new Point(this.pos.x, this.pos.y - 1), this.faceDirection)) {
					--this.pos.y;
					invalidMove = false;
				}
				break;
			case ShipsInfo.MoveDirection.DOWN:
				if (this.canChangeStateTo(new Point(this.pos.x, this.pos.y + 1), this.faceDirection)) {
					++this.pos.y;
					invalidMove = false;
				}
				break;
			case ShipsInfo.MoveDirection.LEFT:
				if (this.canChangeStateTo(new Point(this.pos.x - 1, this.pos.y), this.faceDirection)) {
					--this.pos.x;
					invalidMove = false;
				}
				break;
			case ShipsInfo.MoveDirection.RIGHT:
				if (this.canChangeStateTo(new Point(this.pos.x + 1, this.pos.y), this.faceDirection)) {
					++this.pos.x;
					invalidMove = false;
				}
				break;
		}
		if (invalidMove) {
			displayTemporaryMessage("Invalid move!<br>You can't move outside the grid!", ShipsInfo.messageDisplayTime);
		}
		this.render();
	}

	rotate(direction) {
		let invalidMove = true;
		switch (this.faceDirection) {
			case ShipsInfo.MoveDirection.UP:
				if (direction == ShipsInfo.RotationDirection.CW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.RIGHT)) {
					this.shipFront.classList.remove("app-ship-up");
					this.shipFront.classList.add("app-ship-right");
					this.faceDirection = ShipsInfo.MoveDirection.RIGHT;
					invalidMove = false;
				} else if (direction == ShipsInfo.RotationDirection.CCW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.LEFT)) {
					this.shipFront.classList.remove("app-ship-up");
					this.shipFront.classList.add("app-ship-left");
					this.faceDirection = ShipsInfo.MoveDirection.LEFT;
					invalidMove = false;
				}
				break;
			case ShipsInfo.MoveDirection.DOWN:
				if (direction == ShipsInfo.RotationDirection.CW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.LEFT)) {
					this.shipFront.classList.remove("app-ship-down");
					this.shipFront.classList.add("app-ship-left");
					this.faceDirection = ShipsInfo.MoveDirection.LEFT;
					invalidMove = false;
				} else if (direction == ShipsInfo.RotationDirection.CCW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.RIGHT)) {
					this.shipFront.classList.remove("app-ship-down");
					this.shipFront.classList.add("app-ship-right");
					this.faceDirection = ShipsInfo.MoveDirection.RIGHT;
					invalidMove = false;
				}
				break;
			case ShipsInfo.MoveDirection.LEFT:
				if (direction == ShipsInfo.RotationDirection.CW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.UP)) {
					this.shipFront.classList.remove("app-ship-left");
					this.shipFront.classList.add("app-ship-up");
					this.faceDirection = ShipsInfo.MoveDirection.UP;
					invalidMove = false;
				} else if (direction == ShipsInfo.RotationDirection.CCW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.DOWN)) {
					this.shipFront.classList.remove("app-ship-left");
					this.shipFront.classList.add("app-ship-down");
					this.faceDirection = ShipsInfo.MoveDirection.DOWN;
					invalidMove = false;
				}
				break;
			case ShipsInfo.MoveDirection.RIGHT:
				if (direction == ShipsInfo.RotationDirection.CW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.DOWN)) {
					this.shipFront.classList.remove("app-ship-right");
					this.shipFront.classList.add("app-ship-down");
					this.faceDirection = ShipsInfo.MoveDirection.DOWN;
					invalidMove = false;
				} else if (direction == ShipsInfo.RotationDirection.CCW && this.canChangeStateTo(this.pos, ShipsInfo.MoveDirection.UP)) {
					this.shipFront.classList.remove("app-ship-right");
					this.shipFront.classList.add("app-ship-up");
					this.faceDirection = ShipsInfo.MoveDirection.UP;
					invalidMove = false;
				}
				break;
		}
		if (invalidMove) {
			displayTemporaryMessage("Invalid rotation!<br>You can't rotate the ship because it would land outside of the grid!", ShipsInfo.messageDisplayTime);
		}
		this.render();
	}

	canChangeStateTo(targetPos, targetRotation) {
		switch (targetRotation) {
			case ShipsInfo.MoveDirection.UP:
				return !(
					targetPos.x < 0 ||
					targetPos.x > this.gridSize.x - 1 ||
					targetPos.y < 0 ||
					targetPos.y > this.gridSize.y - this.length
				);
				break;
			case ShipsInfo.MoveDirection.DOWN:
				return !(
					targetPos.x < 0 ||
					targetPos.x > this.gridSize.x - 1 ||
					targetPos.y < this.length - 1 ||
					targetPos.y > this.gridSize.y - 1
				);
				break;
			case ShipsInfo.MoveDirection.LEFT:
				return !(
					targetPos.x < 0 ||
					targetPos.x > this.gridSize.x - this.length ||
					targetPos.y < 0 ||
					targetPos.y > this.gridSize.y - 1
				);
				break;
			case ShipsInfo.MoveDirection.RIGHT:
				return !(
					targetPos.x < this.length - 1 ||
					targetPos.x > this.gridSize.x - 1 ||
					targetPos.y < 0 ||
					targetPos.y > this.gridSize.y - 1
				);
				break;
		}
	}

	render() {
		// Change parent cell
		let targetCell = getCell(this.grid, this.pos.y + gridOffset.y, this.pos.x + gridOffset.x);
		targetCell.appendChild(this.shipFront);

		// Set proper backgrounds and record ship validity
		this.isValid = true;
		for (let i = 0; i < this.length; ++i) {
			this.shipParts[i].classList.remove("app-ship-forbidden");
			let partPos = this.getLocation(i);

			if (ShipsInfo.isLocationTaken(partPos) || !this.isLocationInGrid(partPos)) {
				this.shipParts[i].classList.add("app-ship-forbidden");
				this.isValid = false;
			}
		}
	}

	isLocationInGrid(point) {
		if (point.x < 0 || point.x >= this.gridSize.x || point.y < 0 || point.y >= this.gridSize.y) {
			return false;
		}
		return true;
	}

	getLocation(partIndex) {
		let partPos = new Point(this.pos.x, this.pos.y);
		switch (this.faceDirection) {
			case ShipsInfo.MoveDirection.UP:
				partPos.y += partIndex;
				break;
			case ShipsInfo.MoveDirection.DOWN:
				partPos.y -= partIndex;
				break;
			case ShipsInfo.MoveDirection.LEFT:
				partPos.x += partIndex;
				break;
			case ShipsInfo.MoveDirection.RIGHT:
				partPos.x -= partIndex;
				break;
		}
		return partPos;
	}

	getLocations() {
		let locations = [];
		for (let i = 0; i < this.length; ++i) {
			locations.push(this.getLocation(i));
		}
		return locations;
	}
}


// ****************************** //
//				LAUNCHING SALVOES				//
// ****************************** //

function trySubmitSalvo(data) {
	let gamePlayerId = getUrlSearchObject().gp;
	tryPostJson("/api/games/players/" + gamePlayerId + "/salvos", data, onSubmitSalvo, onSubmitSalvoFail);
}

function onSubmitSalvo(response) {
	refreshData();
}

function onSubmitSalvoFail(response) {
	var responseObj = JSON.parse(response.responseText);
	alert("ERROR!\n" + responseObj.error);
	refreshData();
}

class SalvoPlacer {

	// ENTRY POINT for salvo placing
	static setupSalvoPlacing() {
		SalvoPlacer.reset();
		SalvoPlacer.addEvents();
		SalvoPlacer.updateMessage();
	}

	static addEvents() {
		$('.app-salvo-cell').on('click', SalvoPlacer.handleClick);
	}

	static removeEvents() {
		$('.app-salvo-cell').off('click', SalvoPlacer.handleClick);
	}

	static handleClick() {
		if (SalvoPlacer.cellHasPreviousShot(this)) {
			displayTemporaryMessage('A previous shot has already been made to that cell!<br>Choose another cell!', SalvoPlacer.messageDisplayTime);
		} else if (SalvoPlacer.cellHasCurrentShot(this)) {
			SalvoPlacer.removeShot(this);
			SalvoPlacer.updateMessage();
		} else if (SalvoPlacer.canAddShot()) {
			SalvoPlacer.addShot(this);
			SalvoPlacer.updateMessage();
		} else {
			displayTemporaryMessage('You have already reached the max amount of shots for this salvo!<br>You may launch the salvo or deselect a shot to select a new position!', SalvoPlacer.messageDisplayTime);
		}
		SalvoPlacer.updateButton();

	}

	static cellHasPreviousShot(cellElement) {
		return $(cellElement).find('.app-shot').length != 0;
	}

	static cellHasCurrentShot(cellElement) {
		return $(cellElement).find('.app-shot-selected').length != 0;
	}

	static canAddShot() {
		return SalvoPlacer.currentSalvoCount < SalvoPlacer.maxSalvoes;
	}

	static addShot(cellElement) {
		let shotDiv = document.createElement('div');
		shotDiv.classList.add('app-shot-selected');
		cellElement.appendChild(shotDiv);
		++SalvoPlacer.currentSalvoCount;
		SalvoPlacer.addShotLocation(cellElement.getAttribute('data-address'));
	}

	static addShotLocation(locationName) {
		let index = SalvoPlacer.shotLocations.indexOf(locationName);
		if (index == -1) {
			SalvoPlacer.shotLocations.push(locationName);
		}
	}

	static removeShot(cellElement) {
		$(cellElement).find('.app-shot-selected').remove();
		--SalvoPlacer.currentSalvoCount;
		SalvoPlacer.removeShotLocation(cellElement.getAttribute('data-address'));
	}

	static removeShotLocation(locationName) {
		let index = SalvoPlacer.shotLocations.indexOf(locationName);
		if (index != -1) {
			SalvoPlacer.shotLocations.splice(index, 1);
		}

	}

	static updateMessage() {
		let message = "Time to place some shots!";

		if (SalvoPlacer.currentSalvoCount > 0) {
			message += " You may now launch the salvo if you so desire, but you can place more shots."
		}

		message += "<br>" + SalvoPlacer.currentSalvoCount + "/" + SalvoPlacer.maxSalvoes + " shots placed.<br>Click on a cell in your Salvoes grid to select the cell for a shot. Click it again to deselect. Once finished with all shots, click on the green button.";
		displayMessage(message);
	}

	static updateButton() {
		if (SalvoPlacer.currentSalvoCount > 0) {
			$salvoBtn.attr('disabled', false);
		} else {
			$salvoBtn.attr('disabled', true);
		}
	}

	static onSalvoBtnClick() {
		SalvoPlacer.endSalvoPlacing();
	}

	// EXIT POINT for salvo placing
	static endSalvoPlacing() {
		SalvoPlacer.removeEvents();
		// Build salvo object
		let salvoData = SalvoPlacer.getSalvoData(); // some function

		// Submit to server
		trySubmitSalvo(salvoData);
		SalvoPlacer.reset();
		displayMessage("Salvo shot!");
		$salvoBtn.attr('disabled', true);
	}

	static getSalvoData() {
		return {
			locations: SalvoPlacer.shotLocations
		};
	}

	static reset() {
		$('.app-shot-selected').remove();
		SalvoPlacer.currentSalvoCount = 0;
		SalvoPlacer.shotLocations = [];
	}
}

// SalvoPlacer constants
SalvoPlacer.messageDisplayTime = 2000;
SalvoPlacer.maxSalvoes = 5;

// SalvoPlacer variables
SalvoPlacer.currentSalvoCount = 0;
SalvoPlacer.shotLocations = [];
