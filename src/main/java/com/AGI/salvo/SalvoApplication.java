package com.AGI.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(
			PlayerRepository playerRepository,
			GameRepository gameRepository,
			GamePlayerRepository gamePlayerRepository,
			ShipRepository shipRepository,
			SalvoRepository salvoRepository,
			ScoreRepository scoreRepository
	) {
		return (String... args) -> {

			// Players
			Player p1 = new Player("j.bauer@ctu.gov");
			Player p2 = new Player("c.obrian@ctu.gov");
			Player p3 = new Player("kim_bauer@gmail.com");
			Player p4 = new Player("t.almeida@ctu.gov");

			playerRepository.save(p1);
			playerRepository.save(p2);
			playerRepository.save(p3);
			playerRepository.save(p4);

			// Games
			Date date = new Date();

			Game g1 = new Game(date);
			int gameIndex = 1;
			Game g2 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			Game g3 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			Game g4 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			Game g5 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			Game g6 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			Game g7 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			Game g8 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));

			gameRepository.save(g1);
			gameRepository.save(g2);
			gameRepository.save(g3);
			gameRepository.save(g4);
			gameRepository.save(g5);
			gameRepository.save(g6);
			gameRepository.save(g7);
			gameRepository.save(g8);

			// GamePlayers
			GamePlayer gp1 = new GamePlayer(g1, p1, Date.from(g1.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp2 = new GamePlayer(g1, p2, Date.from(g1.getCreationDate().toInstant().plusSeconds(30)));

			GamePlayer gp3 = new GamePlayer(g2, p1, Date.from(g2.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp4 = new GamePlayer(g2, p2, Date.from(g2.getCreationDate().toInstant().plusSeconds(30)));

			GamePlayer gp5 = new GamePlayer(g3, p2, Date.from(g3.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp6 = new GamePlayer(g3, p4, Date.from(g3.getCreationDate().toInstant().plusSeconds(30)));

			GamePlayer gp7 = new GamePlayer(g4, p2, Date.from(g4.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp8 = new GamePlayer(g4, p1, Date.from(g4.getCreationDate().toInstant().plusSeconds(30)));

			GamePlayer gp9 = new GamePlayer(g5, p4, Date.from(g5.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp10 = new GamePlayer(g5, p1, Date.from(g5.getCreationDate().toInstant().plusSeconds(30)));

			GamePlayer gp11 = new GamePlayer(g6, p3, Date.from(g6.getCreationDate().toInstant().plusSeconds(15)));
			//GamePlayer gp12 = new GamePlayer(g6, PLAYER NOT YET PRESENT, Date.from(g6.getCreationDate().toInstant().plusSeconds(30)));

			GamePlayer gp13 = new GamePlayer(g7, p4, Date.from(g7.getCreationDate().toInstant().plusSeconds(15)));
			//GamePlayer gp14 = new GamePlayer(g7, PLAYER NOT YET PRESENT, Date.from(g7.getCreationDate().toInstant().plusSeconds(30)));

			GamePlayer gp15 = new GamePlayer(g8, p3, Date.from(g8.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp16 = new GamePlayer(g8, p4, Date.from(g8.getCreationDate().toInstant().plusSeconds(30)));

			gamePlayerRepository.save(gp1);
			gamePlayerRepository.save(gp2);
			gamePlayerRepository.save(gp3);
			gamePlayerRepository.save(gp4);
			gamePlayerRepository.save(gp5);
			gamePlayerRepository.save(gp6);
			gamePlayerRepository.save(gp7);
			gamePlayerRepository.save(gp8);
			gamePlayerRepository.save(gp9);
			gamePlayerRepository.save(gp10);
			gamePlayerRepository.save(gp11);
			//gamePlayerRepository.save(gp12);
			gamePlayerRepository.save(gp13);
			//gamePlayerRepository.save(gp14);
			gamePlayerRepository.save(gp15);
			gamePlayerRepository.save(gp16);


			// Ships
			ArrayList<Ship> ships = new ArrayList<>();

			ships.add(new Ship(gp1, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("H2", "H3", "H4"))));
			ships.add(new Ship(gp1, Ship.ShipType.SUBMARINE, new ArrayList<>(Arrays.asList("E1", "F1", "G1"))));
			ships.add(new Ship(gp1, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("B4", "B5"))));
			ships.add(new Ship(gp2, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5"))));
			ships.add(new Ship(gp2, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("F1", "F2"))));

			ships.add(new Ship(gp3, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5"))));
			ships.add(new Ship(gp3, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("C6", "C7"))));
			ships.add(new Ship(gp4, Ship.ShipType.SUBMARINE, new ArrayList<>(Arrays.asList("A2", "A3", "A4"))));
			ships.add(new Ship(gp4, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("G6", "H6"))));

			ships.add(new Ship(gp5, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5"))));
			ships.add(new Ship(gp5, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("C6", "C7"))));
			ships.add(new Ship(gp6, Ship.ShipType.SUBMARINE, new ArrayList<>(Arrays.asList("A2", "A3", "A4"))));
			ships.add(new Ship(gp6, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("G6", "H6"))));

			ships.add(new Ship(gp7, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5"))));
			ships.add(new Ship(gp7, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("C6", "C7"))));
			ships.add(new Ship(gp8, Ship.ShipType.SUBMARINE, new ArrayList<>(Arrays.asList("A2", "A3", "A4"))));
			ships.add(new Ship(gp8, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("G6", "H6"))));

			ships.add(new Ship(gp9, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5"))));
			ships.add(new Ship(gp9, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("C6", "C7"))));
			ships.add(new Ship(gp10, Ship.ShipType.SUBMARINE, new ArrayList<>(Arrays.asList("A2", "A3", "A4"))));
			ships.add(new Ship(gp10, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("G6", "H6"))));

			ships.add(new Ship(gp11, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5"))));
			ships.add(new Ship(gp11, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("C6", "C7"))));

			ships.add(new Ship(gp15, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5"))));
			ships.add(new Ship(gp15, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("C6", "C7"))));
			ships.add(new Ship(gp16, Ship.ShipType.SUBMARINE, new ArrayList<>(Arrays.asList("A2", "A3", "A4"))));
			ships.add(new Ship(gp16, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("G6", "H6"))));

			for (Ship ship : ships) {
				shipRepository.save(ship);
			}

			// Salvos
			ArrayList<Salvo> salvoes = new ArrayList<>();

			salvoes.add(new Salvo(gp1, 1, new ArrayList<>(Arrays.asList("B5", "C5", "F1"))));
			salvoes.add(new Salvo(gp2, 1, new ArrayList<>(Arrays.asList("B4", "B5", "B6"))));
			salvoes.add(new Salvo(gp1, 2, new ArrayList<>(Arrays.asList("F2", "F5"))));
			salvoes.add(new Salvo(gp2, 2, new ArrayList<>(Arrays.asList("E1", "H3", "A2"))));

			salvoes.add(new Salvo(gp3, 1, new ArrayList<>(Arrays.asList("A2", "A4", "G6"))));
			salvoes.add(new Salvo(gp4, 1, new ArrayList<>(Arrays.asList("B5", "D5", "C7"))));
			salvoes.add(new Salvo(gp3, 2, new ArrayList<>(Arrays.asList("A3", "H6"))));
			salvoes.add(new Salvo(gp4, 2, new ArrayList<>(Arrays.asList("C5", "C6"))));

			salvoes.add(new Salvo(gp5, 1, new ArrayList<>(Arrays.asList("G6", "H6", "A4"))));
			salvoes.add(new Salvo(gp6, 1, new ArrayList<>(Arrays.asList("H1", "H2", "H3"))));
			salvoes.add(new Salvo(gp5, 2, new ArrayList<>(Arrays.asList("A2", "A3", "D8"))));
			salvoes.add(new Salvo(gp6, 2, new ArrayList<>(Arrays.asList("E1", "F2", "G3"))));

			salvoes.add(new Salvo(gp7, 1, new ArrayList<>(Arrays.asList("A3", "A4", "F7"))));
			salvoes.add(new Salvo(gp8, 1, new ArrayList<>(Arrays.asList("B5", "C6", "H1"))));
			salvoes.add(new Salvo(gp7, 2, new ArrayList<>(Arrays.asList("A2", "G6", "H6"))));
			salvoes.add(new Salvo(gp8, 2, new ArrayList<>(Arrays.asList("C5", "C7", "D5"))));

			salvoes.add(new Salvo(gp9, 1, new ArrayList<>(Arrays.asList("A1", "A2", "A3"))));
			salvoes.add(new Salvo(gp10, 1, new ArrayList<>(Arrays.asList("B5", "B6", "C7"))));
			salvoes.add(new Salvo(gp9, 2, new ArrayList<>(Arrays.asList("G6", "G5", "G8"))));
			salvoes.add(new Salvo(gp10, 2, new ArrayList<>(Arrays.asList("C6", "D6", "E6"))));
			salvoes.add(new Salvo(gp10, 3, new ArrayList<>(Arrays.asList("H1", "H8"))));

			for (Salvo salvo : salvoes) {
				salvoRepository.save(salvo);
			}

			// Scores
			double scoreWin = 1;
			double scoreTie = 0.5;
			double scoreLoss = 0;
			ArrayList<Score> scores = new ArrayList<>();

			scores.add(new Score(g1, p1, scoreWin, Date.from(g1.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g1, p2, scoreLoss, Date.from(g1.getCreationDate().toInstant().plusSeconds(1800))));

			scores.add(new Score(g2, p1, scoreTie, Date.from(g2.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g2, p2, scoreTie, Date.from(g2.getCreationDate().toInstant().plusSeconds(1800))));

			scores.add(new Score(g3, p2, scoreWin, Date.from(g3.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g3, p4, scoreLoss, Date.from(g3.getCreationDate().toInstant().plusSeconds(1800))));

			scores.add(new Score(g4, p2, scoreTie, Date.from(g4.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g4, p4, scoreTie, Date.from(g4.getCreationDate().toInstant().plusSeconds(1800))));

			for (Score score : scores) {
				scoreRepository.save(score);
			}
		};
	}
}
