package com.AGI.salvo;

import javax.persistence.*;
import java.util.Date;
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

	public GamePlayer() {}

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

}
