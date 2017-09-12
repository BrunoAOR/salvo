package com.AGI.salvo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ship {

	public enum ShipType{
		CARRIER(5), BATTLESHIP(4), SUBMARINE(3), DESTROYER(3), PATROL_BOAT(2);

		private final int length;

		ShipType(int length) {
			this.length = length;
		}

		public int getLength() {
			return length;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private ShipType type;

	@JoinColumn(name = "game_player_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private GamePlayer gamePlayer;

	@ElementCollection
	@Column(name = "locations")
	List<String> locations = new ArrayList<>();

	public Ship() {}

	public Ship(GamePlayer gamePlayer, ShipType type, List<String> locations) {
		this.gamePlayer = gamePlayer;
		gamePlayer.addShip(this);
		this.type = type;
		this.locations = locations;
	}

	public ShipType getType() {
		return type;
	}

	public void setType(ShipType type) {
		this.type = type;
	}

	public GamePlayer getGamePlayer() {
		return gamePlayer;
	}

	public void setGamePlayer(GamePlayer gamePlayer) {
		this.gamePlayer = gamePlayer;
	}

	public List<String> getLocations() {
		return locations;
	}

}
