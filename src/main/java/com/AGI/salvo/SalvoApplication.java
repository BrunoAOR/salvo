package com.AGI.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
			final Player p1 = new Player("j.bauer@ctu.gov", "24");
			final Player p2 = new Player("c.obrian@ctu.gov", "42");
			final Player p3 = new Player("kim_bauer@gmail.com", "kb");
			final Player p4 = new Player("t.almeida@ctu.gov", "mole");

			playerRepository.save(p1);
			playerRepository.save(p2);
			playerRepository.save(p3);
			playerRepository.save(p4);

			// Games
			final Date date = new Date();

			int gameIndex = 1;
			final Game g1 = new Game(date);
			final Game g2 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			final Game g3 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			final Game g4 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			final Game g5 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			final Game g6 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			final Game g7 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex++ * 3600)));
			final Game g8 = new Game(Date.from(date.toInstant().plusSeconds(gameIndex * 3600)));

			gameRepository.save(g1);
			gameRepository.save(g2);
			gameRepository.save(g3);
			gameRepository.save(g4);
			gameRepository.save(g5);
			gameRepository.save(g6);
			gameRepository.save(g7);
			gameRepository.save(g8);

			// GamePlayers
			final GamePlayer gp1 = new GamePlayer(g1, p1, Date.from(g1.getCreationDate().toInstant().plusSeconds(15)));
			final GamePlayer gp2 = new GamePlayer(g1, p2, Date.from(g1.getCreationDate().toInstant().plusSeconds(30)));

			final GamePlayer gp3 = new GamePlayer(g2, p1, Date.from(g2.getCreationDate().toInstant().plusSeconds(15)));
			final GamePlayer gp4 = new GamePlayer(g2, p2, Date.from(g2.getCreationDate().toInstant().plusSeconds(30)));

			final GamePlayer gp5 = new GamePlayer(g3, p2, Date.from(g3.getCreationDate().toInstant().plusSeconds(15)));
			final GamePlayer gp6 = new GamePlayer(g3, p4, Date.from(g3.getCreationDate().toInstant().plusSeconds(30)));

			final GamePlayer gp7 = new GamePlayer(g4, p2, Date.from(g4.getCreationDate().toInstant().plusSeconds(15)));
			final GamePlayer gp8 = new GamePlayer(g4, p1, Date.from(g4.getCreationDate().toInstant().plusSeconds(30)));

			final GamePlayer gp9 = new GamePlayer(g5, p4, Date.from(g5.getCreationDate().toInstant().plusSeconds(15)));
			final GamePlayer gp10 = new GamePlayer(g5, p1, Date.from(g5.getCreationDate().toInstant().plusSeconds(30)));

			final GamePlayer gp11 = new GamePlayer(g6, p3, Date.from(g6.getCreationDate().toInstant().plusSeconds(15)));
			//final GamePlayer gp12 = new GamePlayer(g6, PLAYER NOT YET PRESENT, Date.from(g6.getCreationDate().toInstant().plusSeconds(30)));

			final GamePlayer gp13 = new GamePlayer(g7, p4, Date.from(g7.getCreationDate().toInstant().plusSeconds(15)));
			//final GamePlayer gp14 = new GamePlayer(g7, PLAYER NOT YET PRESENT, Date.from(g7.getCreationDate().toInstant().plusSeconds(30)));

			final GamePlayer gp15 = new GamePlayer(g8, p3, Date.from(g8.getCreationDate().toInstant().plusSeconds(15)));
			final GamePlayer gp16 = new GamePlayer(g8, p4, Date.from(g8.getCreationDate().toInstant().plusSeconds(30)));

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
			final ArrayList<Ship> ships = new ArrayList<>();

			ships.add(new Ship(gp1, Ship.ShipType.DESTROYER, Arrays.asList("H2", "H3", "H4")));
			ships.add(new Ship(gp1, Ship.ShipType.SUBMARINE, Arrays.asList("E1", "F1", "G1")));
			ships.add(new Ship(gp1, Ship.ShipType.PATROL_BOAT, Arrays.asList("B4", "B5")));
			ships.add(new Ship(gp2, Ship.ShipType.DESTROYER, Arrays.asList("B5", "C5", "D5")));
			ships.add(new Ship(gp2, Ship.ShipType.PATROL_BOAT, Arrays.asList("F1", "F2")));

			ships.add(new Ship(gp3, Ship.ShipType.DESTROYER, Arrays.asList("B5", "C5", "D5")));
			ships.add(new Ship(gp3, Ship.ShipType.PATROL_BOAT, Arrays.asList("C6", "C7")));
			ships.add(new Ship(gp4, Ship.ShipType.SUBMARINE, Arrays.asList("A2", "A3", "A4")));
			ships.add(new Ship(gp4, Ship.ShipType.PATROL_BOAT, Arrays.asList("G6", "H6")));

			ships.add(new Ship(gp5, Ship.ShipType.DESTROYER, Arrays.asList("B5", "C5", "D5")));
			ships.add(new Ship(gp5, Ship.ShipType.PATROL_BOAT, Arrays.asList("C6", "C7")));
			ships.add(new Ship(gp6, Ship.ShipType.SUBMARINE, Arrays.asList("A2", "A3", "A4")));
			ships.add(new Ship(gp6, Ship.ShipType.PATROL_BOAT, Arrays.asList("G6", "H6")));

			ships.add(new Ship(gp7, Ship.ShipType.DESTROYER, Arrays.asList("B5", "C5", "D5")));
			ships.add(new Ship(gp7, Ship.ShipType.PATROL_BOAT, Arrays.asList("C6", "C7")));
			ships.add(new Ship(gp8, Ship.ShipType.SUBMARINE, Arrays.asList("A2", "A3", "A4")));
			ships.add(new Ship(gp8, Ship.ShipType.PATROL_BOAT, Arrays.asList("G6", "H6")));

			ships.add(new Ship(gp9, Ship.ShipType.DESTROYER, Arrays.asList("B5", "C5", "D5")));
			ships.add(new Ship(gp9, Ship.ShipType.PATROL_BOAT, Arrays.asList("C6", "C7")));
			ships.add(new Ship(gp10, Ship.ShipType.SUBMARINE, Arrays.asList("A2", "A3", "A4")));
			ships.add(new Ship(gp10, Ship.ShipType.PATROL_BOAT, Arrays.asList("G6", "H6")));

			ships.add(new Ship(gp11, Ship.ShipType.DESTROYER, Arrays.asList("B5", "C5", "D5")));
			ships.add(new Ship(gp11, Ship.ShipType.PATROL_BOAT, Arrays.asList("C6", "C7")));

			ships.add(new Ship(gp15, Ship.ShipType.DESTROYER, Arrays.asList("B5", "C5", "D5")));
			ships.add(new Ship(gp15, Ship.ShipType.PATROL_BOAT, Arrays.asList("C6", "C7")));
			ships.add(new Ship(gp16, Ship.ShipType.SUBMARINE, Arrays.asList("A2", "A3", "A4")));
			ships.add(new Ship(gp16, Ship.ShipType.PATROL_BOAT, Arrays.asList("G6", "H6")));

			for (Ship ship : ships) {
				shipRepository.save(ship);
			}

			// Salvos
			final ArrayList<Salvo> salvoes = new ArrayList<>();

			salvoes.add(new Salvo(gp1, 1, Arrays.asList("B5", "C5", "F1")));
			salvoes.add(new Salvo(gp2, 1, Arrays.asList("B4", "B5", "B6")));
			salvoes.add(new Salvo(gp1, 2, Arrays.asList("F2", "D5")));
			salvoes.add(new Salvo(gp2, 2, Arrays.asList("E1", "H3", "A2")));

			salvoes.add(new Salvo(gp3, 1, Arrays.asList("A2", "A4", "G6")));
			salvoes.add(new Salvo(gp4, 1, Arrays.asList("B5", "D5", "C7")));
			salvoes.add(new Salvo(gp3, 2, Arrays.asList("A3", "H6")));
			salvoes.add(new Salvo(gp4, 2, Arrays.asList("C5", "C6")));

			salvoes.add(new Salvo(gp5, 1, Arrays.asList("G6", "H6", "A4")));
			salvoes.add(new Salvo(gp6, 1, Arrays.asList("H1", "H2", "H3")));
			salvoes.add(new Salvo(gp5, 2, Arrays.asList("A2", "A3", "D8")));
			salvoes.add(new Salvo(gp6, 2, Arrays.asList("E1", "F2", "G3")));

			salvoes.add(new Salvo(gp7, 1, Arrays.asList("A3", "A4", "F7")));
			salvoes.add(new Salvo(gp8, 1, Arrays.asList("B5", "C6", "H1")));
			salvoes.add(new Salvo(gp7, 2, Arrays.asList("A2", "G6", "H6")));
			salvoes.add(new Salvo(gp8, 2, Arrays.asList("C5", "C7", "D5")));

			salvoes.add(new Salvo(gp9, 1, Arrays.asList("A1", "A2", "A3")));
			salvoes.add(new Salvo(gp10, 1, Arrays.asList("B5", "B6", "C7")));
			salvoes.add(new Salvo(gp9, 2, Arrays.asList("G6", "G5", "G8")));
			salvoes.add(new Salvo(gp10, 2, Arrays.asList("C6", "D6", "E6")));
			salvoes.add(new Salvo(gp10, 3, Arrays.asList("H1", "H8")));

			for (Salvo salvo : salvoes) {
				salvoRepository.save(salvo);
			}

			// Scores
			double scoreWin = 1;
			double scoreTie = 0.5;
			double scoreLoss = 0;

			final List<Score> scores = new ArrayList<>();

			scores.add(new Score(g1, p1, scoreWin, Date.from(g1.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g1, p2, scoreLoss, Date.from(g1.getCreationDate().toInstant().plusSeconds(1800))));

			scores.add(new Score(g2, p1, scoreTie, Date.from(g2.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g2, p2, scoreTie, Date.from(g2.getCreationDate().toInstant().plusSeconds(1800))));

			scores.add(new Score(g3, p2, scoreWin, Date.from(g3.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g3, p4, scoreLoss, Date.from(g3.getCreationDate().toInstant().plusSeconds(1800))));

			scores.add(new Score(g4, p2, scoreTie, Date.from(g4.getCreationDate().toInstant().plusSeconds(1800))));
			scores.add(new Score(g4, p1, scoreTie, Date.from(g4.getCreationDate().toInstant().plusSeconds(1800))));

			for (Score score : scores) {
				scoreRepository.save(score);
			}

		};
	}

}

@Configuration
class WebAuthenticationConfig extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Bean
	public UserDetailsService createUserDetailsService() {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				Player player = playerRepository.findByUserName(username);
				if (player != null)
				{
					return new User(player.getUserName(), player.getPassword(), AuthorityUtils.createAuthorityList("USER"));
				}
				else
				{
					throw new UsernameNotFoundException("Username " + username + "not found!");
				}
			}
		};
	}
}

@EnableWebSecurity
@Configuration
class WebAccessConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {

		// How log in and logout works
		httpSecurity.formLogin()
				.usernameParameter("userName")
				.passwordParameter("password")
				.loginPage("/api/login");
		httpSecurity.logout().logoutUrl("/api/logout");

		// Who can see what
		httpSecurity.authorizeRequests()
				.antMatchers("/web/game.html").hasAuthority("USER");

		// turn off checking for CSRF tokens
		httpSecurity.csrf().disable();

		// If user is not authenticated, send an authentication failure response
		httpSecurity.exceptionHandling().authenticationEntryPoint((request, response, authentication) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// If login is successful, just clear the flags asking for authentication
		httpSecurity.formLogin().successHandler((request, response, authentication) -> clearAuthenticationAttributes(request));

		// If login fails, just send an authentication failure response
		httpSecurity.formLogin().failureHandler((request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// If logout is successful, just send a success response
		httpSecurity.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			httpSession.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}