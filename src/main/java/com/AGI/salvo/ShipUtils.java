package com.AGI.salvo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShipUtils {

	// Private constructor to avoid instantiation of the class
	private ShipUtils() {}

	public static boolean areShipsValid(List<Ship> ships) {
		Set<String> seenLocations = new HashSet<>();
		for (Ship ship : ships) {
			if (ship.getType().getLength() != ship.getLocations().size()) {
				return false;
			}
			if (hasOverlappedShipLocations(seenLocations, ship)) {
				return false;
			}

			if (!hasLinkedShipLocations(ship)) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasOverlappedShipLocations(Set<String> takenLocations, Ship ship) {
		boolean hasOverlaps = false;
		for (String location : ship.getLocations()) {
			if (takenLocations.contains(location)) {
				hasOverlaps = true;
			}
			takenLocations.add(location);
		}
		return hasOverlaps;
	}

	public static boolean hasLinkedShipLocations(Ship ship) {
		boolean horizontalOK = true;
		boolean verticalOK = true;
		List<String> locations = ship.getLocations();
		char currentLetter;
		int currentNumber;
		char nextLetter;
		int nextNumber;
		int horizontalDirection = 0;
		int verticalDirection = 0;
		// Looping only till the second to last element, because the loop compares each element with the next one
		for (int i = 0; i < locations.size() - 1 && (horizontalOK || verticalOK); ++i) {
			currentLetter = locations.get(i).charAt(0);
			currentNumber = Integer.parseInt(locations.get(i).substring(1));
			nextLetter = locations.get(i+1).charAt(0);
			nextNumber = Integer.parseInt(locations.get(i+1).substring(1));
			// Checking horizontal (same letter)
			if (currentLetter != nextLetter) {
				horizontalOK = false;
			} else {
				// So, if they have the same letter, check if the numbers are adjacent
				if (horizontalDirection == 0) {
					if (Math.abs(nextNumber - currentNumber) != 1) {
						horizontalOK = false;
					} else {
						horizontalDirection = nextNumber - currentNumber;
					}
				} else {
					// So, if horizontalDirection was defined previously
					if (nextNumber - currentNumber != horizontalDirection) {
						horizontalOK = false;
					}
				}
			}
			// Checking vertical (same number)
			if (currentNumber != nextNumber) {
				verticalOK = false;
			} else {
				// So, if they have the same number, check if the letters are adjacent
				if (verticalDirection == 0) {
					if (Math.abs(nextLetter - currentLetter) != 1) {
						verticalOK = false;
					} else {
						verticalDirection = nextLetter - currentLetter;
					}
				} else {
					// So, if verticalDirection was defined previously
					if (nextLetter - currentLetter != verticalDirection) {
						verticalOK = false;
					}
				}
			}
		}
		return horizontalOK || verticalOK;
	}
}
