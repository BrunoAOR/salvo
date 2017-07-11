package com.AGI.salvo;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Game {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private Date creationDate;
	@OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
	private Set<GamePlayer> gamePlayers = new HashSet<>();

	public Game() {}

	public Game(Date creationDate) {
		this.creationDate = creationDate;
	}

	public long getId() {
		return id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Set<GamePlayer> getGamePlayers() {
		return gamePlayers;
	}

	public List<Player> getPlayers() {
		return gamePlayers.stream().map(gamePlayer -> gamePlayer.getPlayer()).collect(Collectors.toList());
	}

	public void addGamePlayer(GamePlayer gamePlayer) {
		gamePlayers.add(gamePlayer);
	}
}
