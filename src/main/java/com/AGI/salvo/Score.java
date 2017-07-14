package com.AGI.salvo;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Score {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private double score;
	private Date finishDate;

	@JoinColumn(name = "game_id")
	@ManyToOne(fetch = FetchType.EAGER)
	private Game game;

	@JoinColumn(name = "player_id")
	@ManyToOne(fetch = FetchType.EAGER)
	private Player player;

	public Score() {}

	public Score(Game game, Player player, double score, Date finishDate) {
		this.game = game;
		game.addScore(this);
		this.player = player;
		player.addScore(this);
		this.score = score;
		this.finishDate = finishDate;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
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
