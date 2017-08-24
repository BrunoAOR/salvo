package com.AGI.salvo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SalvoUtils {

	// Private constructor to avoid instantiation of the class
	private SalvoUtils(){}

	/**
	 * This function verifies first whether the salvo locations overlap with each other and then if they overlap with previous salvoes
	 *
	 * @param salvo
	 * @param gamePlayer
	 * @return
	 */
	public static boolean isSalvoValid(Salvo salvo, GamePlayer gamePlayer) {

		List<String> currentLocations = salvo.getLocations();
		// Check if there are locations
		if (currentLocations.isEmpty()) {
			return false;
		}

		// Check if there is an overlap among this salvo's locations
		for (int i = 0; i < currentLocations.size(); ++i) {
			for (int j = i + 1; j < currentLocations.size(); ++j) {
				if (Objects.equals(currentLocations.get(i), currentLocations.get(j))) {
					// Overlap in salvo locations!
					return false;
				}
			}
		}

		// So, no overlap in this salvo's locations

		// Check if there is an overlap with any previous salvos' locations
		List<String> previousLocations = new ArrayList<>();

		// Populate previousLocations list
		for (Salvo previousSalvo : gamePlayer.getSalvoes()) {
			previousLocations.addAll(previousSalvo.getLocations());
		}

		for (int i = 0; i < currentLocations.size(); ++i) {
			for (int j = 0; j < previousLocations.size(); ++j) {
				if (Objects.equals(currentLocations.get(i), previousLocations.get(j))) {
					// Overlap in current salvo location with previous salvo location
					return false;
				}
			}
		}

		// So, no overlaps whatsoever
		return true;
	}
}
