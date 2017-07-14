package com.AGI.salvo;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Score {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private double points;
	private Date finishDate;

	@JoinColumn(name = "game_id")
	@ManyToOne(fetch = FetchType.EAGER)
	private Game game;

	@JoinColumn(name = "player_id")
	@ManyToOne(fetch = FetchType.EAGER)
	private Player player;

	public Score() {}

	public Score(Game game, Player player, double points, Date finishDate) {
		this.game = game;
		game.addScore(this);
		this.player = player;
		player.addScore(this);
		this.points = points;
		this.finishDate = finishDate;
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double points) {
		this.points = points;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}
