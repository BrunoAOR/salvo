package com.AGI.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String userName;
	@OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
	private Set<GamePlayer> gamePlayers;

	public Player() {}

	public Player(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Set<GamePlayer> getGamePlayers() {
		return gamePlayers;
	}

	@JsonIgnore
	public List<Game> getGames() {
		return gamePlayers.stream().map(gamePlayer -> gamePlayer.getGame()).collect(Collectors.toList());
	}
}
