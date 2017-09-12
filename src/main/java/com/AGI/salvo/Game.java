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
	@OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
	private Set<GamePlayer> gamePlayers = new HashSet<>();
	@OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
	private Set<Score> scores = new HashSet<>();

	public Game() {
		this.creationDate = new Date();
	}

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
		return gamePlayers.stream().map(GamePlayer::getPlayer).collect(Collectors.toList());
	}

	public void addGamePlayer(GamePlayer gamePlayer) {
		gamePlayers.add(gamePlayer);
	}

	public void addScore(Score score) {
		this.scores.add(score);
	}

	public Score getScore (Player player) {
		return scores.stream().filter(score -> score.getPlayer() == player).findFirst().orElse(null);
	}
}
