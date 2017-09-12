package com.AGI.salvo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Salvo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(name = "gamePlayer_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private GamePlayer gamePlayer;

	private int turn;

	@ElementCollection
	@Column(name = "locations")
	private List<String> locations = new ArrayList<>();

	public Salvo() {}

	public Salvo(GamePlayer gamePlayer, int turn, List<String> locations) {
		this.gamePlayer = gamePlayer;
		gamePlayer.addSalvo(this);
		this.turn = turn;
		this.locations = locations;
	}

	public long getId() {
		return id;
	}

	public GamePlayer getGamePlayer() {
		return gamePlayer;
	}

	public void setGamePlayer(GamePlayer gamePlayer) {
		this.gamePlayer = gamePlayer;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public List<String> getLocations() {
		return locations;
	}

	public void setLocations(List<String> locations) {
		this.locations = locations;
	}
}
