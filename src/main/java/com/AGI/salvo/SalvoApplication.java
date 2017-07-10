package com.AGI.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
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
			gamePlayerRepository.save(new GamePlayer(g1, p1, Date.from(g1.getCreationDate().toInstant().plusSeconds(15))));
			gamePlayerRepository.save(new GamePlayer(g1, p2, Date.from(g1.getCreationDate().toInstant().plusSeconds(30))));
			gamePlayerRepository.save(new GamePlayer(g2, p2, Date.from(g2.getCreationDate().toInstant().plusSeconds(15))));
			gamePlayerRepository.save(new GamePlayer(g2, p4, Date.from(g2.getCreationDate().toInstant().plusSeconds(30))));
			gamePlayerRepository.save(new GamePlayer(g3, p4, Date.from(g3.getCreationDate().toInstant().plusSeconds(15))));
			gamePlayerRepository.save(new GamePlayer(g3, p1, Date.from(g3.getCreationDate().toInstant().plusSeconds(30))));
		};
	}
}
