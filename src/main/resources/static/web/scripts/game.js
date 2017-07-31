let data;
let gridSize;
let gridOffset;
let $headerOutlet;
let $infoOutlet;
let shipGrid;
let salvoGrid;
let uppercaseAsciiStart = 65;

$(function () {
	$headerOutlet = $('#app-header-outlet');
	$infoOutlet = $('#app-ship-grid-info');
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

	gridSize = new Point(10, 10);
	gridOffset = new Point(1, 1);

	shipGrid = getNewGrid(gridSize, gridOffset);
	salvoGrid = getNewGrid(gridSize, gridOffset);

	$('#app-ship-grid-outlet').empty().append(shipGrid);
	$('#app-salvo-grid-outlet').empty().append(salvoGrid);

	// Setup header
	displayHeader(data.currentGamePlayer, data.hasOwnProperty("otherGamePlayer") ? data.otherGamePlayer : null);

	// Setup ships
	if (data.ships.length > 0) {
		$('#app-salvo-grid-container').show();
		displayShips(data.ships);
	} else {
		$('#app-salvo-grid-container').hide();
		ShipsInfo.setupShipPlacing();
	}

	// Setup salvoes
	let otherGamePlayerId = data.hasOwnProperty("otherGamePlayer") ? data.otherGamePlayer.id : null;
	displaySalvoes(data.salvoes, data.currentGamePlayer.id, otherGamePlayerId);
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
	return false;
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

function getNewGrid(gridSize, gridOffset) {
	if (gridSize.x <= 0 || gridSize.y <= 0) {
		return null;
	}

	let grid = document.createElement('div');
	grid.classList.add('app-grid');

	// Starting at -1 to add the grid headers
	let start = -gridOffset.x;
	// Added +1 for the outer background frame
	let xEnd = gridSize.x + 1;
	let yEnd = gridSize.y + 1;

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
	let chars = cellName.split('');
	let rowIdx = chars[0].charCodeAt() - uppercaseAsciiStart + 1;
	let colIdx = chars[1];
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

function displayTemporaryMessage(htmlMessage, waitDuration) {
	// If no temporary message is being displayed, we store the current message
	if (displayTemporaryMessage.timeoutID == undefined || displayTemporaryMessage.timeoutID == 0) {
		displayTemporaryMessage.previousMessage = $infoOutlet.html();
	} else {
		// If there was a temporary message, we'll stop its timeout
		clearTimeout(displayTemporaryMessage.timeoutID);
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

class ShipsInfo {

	// ENTRY POINT for ship placement
	static setupShipPlacing() {
		$infoOutlet.html("Time to place your ships!<br>Use WASD to move the ship around the grid, Q&E to rotate it around the bow (front of the ship) and 'Enter' to place the ship.");
		setTimeout(function() {
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
			$infoOutlet.html("Placing ship " + (ShipsInfo.shipIndex + 1) + " out of " + ShipsInfo.shipTypesReadable.length + " (" + ShipsInfo.shipTypesReadable[ShipsInfo.shipIndex] + ")!<br>Use WASD to move the ship around the grid and Q&E to rotate it around the bow (front of the ship) and 'Enter' to place the ship.");
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
			console.log("Accepted!");
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
		$infoOutlet.html("All ships placed!");
		console.log("Finished ship placement!");
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

}

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
		console.log();
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
