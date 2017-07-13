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
			SalvoRepository salvoRepository
	) {
		return args -> {

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
			Game g2 = new Game(Date.from(date.toInstant().plusSeconds(3600)));
			Game g3 = new Game(Date.from(date.toInstant().plusSeconds(7200)));

			gameRepository.save(g1);
			gameRepository.save(g2);
			gameRepository.save(g3);

			// GamePlayers
			GamePlayer gp1 = new GamePlayer(g1, p1, Date.from(g1.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp2 = new GamePlayer(g1, p2, Date.from(g1.getCreationDate().toInstant().plusSeconds(30)));
			GamePlayer gp3 = new GamePlayer(g2, p2, Date.from(g2.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp4 = new GamePlayer(g2, p4, Date.from(g2.getCreationDate().toInstant().plusSeconds(30)));
			GamePlayer gp5 = new GamePlayer(g3, p4, Date.from(g3.getCreationDate().toInstant().plusSeconds(15)));
			GamePlayer gp6 = new GamePlayer(g3, p1, Date.from(g3.getCreationDate().toInstant().plusSeconds(30)));

			gamePlayerRepository.save(gp1);
			gamePlayerRepository.save(gp2);
			gamePlayerRepository.save(gp3);
			gamePlayerRepository.save(gp4);
			gamePlayerRepository.save(gp5);
			gamePlayerRepository.save(gp6);

			// Ships
			Ship s1 = new Ship(gp1, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("H2", "H3", "H4")));
			Ship s2 = new Ship(gp1, Ship.ShipType.SUBMARINE, new ArrayList<>(Arrays.asList("E1", "F1", "G1")));
			Ship s3 = new Ship(gp1, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("B4", "B5")));
			Ship s4 = new Ship(gp2, Ship.ShipType.DESTROYER, new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
			Ship s5 = new Ship(gp2, Ship.ShipType.PATROL_BOAT, new ArrayList<>(Arrays.asList("F1", "F2")));

			shipRepository.save(s1);
			shipRepository.save(s2);
			shipRepository.save(s3);
			shipRepository.save(s4);
			shipRepository.save(s5);

			// Salvos
			Salvo salvo1 = new Salvo(gp1, 1, new ArrayList<>(Arrays.asList("B5", "C5", "F1")));
			Salvo salvo2 = new Salvo(gp2, 1, new ArrayList<>((Arrays.asList("B4", "B5", "B6"))));
			Salvo salvo3 = new Salvo(gp1, 2, new ArrayList<>(Arrays.asList("F2", "F5")));
			Salvo salvo4 = new Salvo(gp2, 2, new ArrayList<>(Arrays.asList("E1", "H3", "A2")));
			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
		};
	}
}
