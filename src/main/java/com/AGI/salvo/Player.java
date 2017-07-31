package com.AGI.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String userName;
	private String password;

	@OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
	private Set<GamePlayer> gamePlayers =  new HashSet<>();
	@OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
	private Set<Score> scores = new HashSet<>();

	public Player() {}

	public Player(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public long getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<GamePlayer> getGamePlayers() {
		return gamePlayers;
	}

	@JsonIgnore
	public List<Game> getGames() {
		return gamePlayers.stream().map(GamePlayer::getGame).collect(Collectors.toList());
	}

	public void addGamePlayer(GamePlayer gamePlayer) {
		gamePlayers.add(gamePlayer);
	}

	public void addScore(Score score) {
		scores.add(score);
	}

	public Score getScore(Game game) {
		return scores.stream().filter(score -> score.getGame() == game).findFirst().orElse(null);
	}
}
