package com.AGI.salvo;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class GamePlayer {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private Date joinDate;

	@JoinColumn(name = "player_id")
	@ManyToOne(fetch = FetchType.EAGER)
	private Player player;

	@JoinColumn(name = "game_id")
	@ManyToOne(fetch = FetchType.EAGER)
	private Game game;

	@OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
	private Set<Ship> ships = new HashSet<>();

	@OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
	private Set<Salvo> salvoes = new HashSet<>();

	public GamePlayer() {}

	public GamePlayer(Game game, Player player) {
		this.game = game;
		game.addGamePlayer(this);
		this.player = player;
		player.addGamePlayer(this);
		this.joinDate = new Date();
	}

	public GamePlayer(Game game, Player player, Date joinDate) {
		this.game = game;
		game.addGamePlayer(this);
		this.player = player;
		player.addGamePlayer(this);
		this.joinDate = joinDate;

	}

	public long getId() {
		return id;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Set<Ship> getShips() {
		return ships;
	}

	public void setShips(Set<Ship> ships) {
		this.ships = ships;
	}

	public Set<Salvo> getSalvoes() {
		return salvoes;
	}

	public void setSalvoes(Set<Salvo> salvoes) {
		this.salvoes = salvoes;
	}

	public void addShip(Ship ship) {
		ships.add(ship);
	}

	public void addSalvo (Salvo salvo) {
		salvoes.add(salvo);
	}

	public Score getScore() {
		return player.getScore(game);
	}
}
