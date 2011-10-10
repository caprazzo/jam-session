package net.caprazzi.tapauth.wordgame;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.Misc;
import net.caprazzi.tapauth.SkimpyTemplate;
import net.caprazzi.tapauth.wordgame.Player.Status;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class WordgameServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String[] parts = req.getRequestURI().split("/");		
		String baseUrl = "http://" + req.getServerName() + ":" + req.getServerPort();
				
		if (parts[2].equals("create")) {
			String gameId = Misc.randomString();
			resp.sendRedirect("/wordgame/view/" + gameId);
			return;
		}
		
		if (parts[2].equals("view")) {
			String gameId = parts[3];
			
			String joinUrl = baseUrl + "/wordgame/join/" + gameId;		
			// TODO: use shortened url to produce smaller QR codes
			String qrUrl = "https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=" + joinUrl;
			
			resp.setContentType("text/html");
			new SkimpyTemplate(getServletContext().getResourceAsStream("/wordgame-res/view.html"))
				.add("JOIN_URL", joinUrl)
				.add("QR_CODE_URL", qrUrl)
				.write(resp.getWriter());
			
			return;
		}	
		
		// on join, a playerId is assigned and suitable match is joined
		if (parts[2].equals("join")) {
			String gameId = parts[3];
			String playerId = Misc.randomString();
			Match match = WordgameStore.joinMatch(gameId, playerId);
			Player player = match.getPlayer();
			String channelId = GameChannelId.getChannelId(gameId, match, player);
			resp.sendRedirect("/wordgame/play/" + channelId);
			return;
		}
		
		if (parts[2].equals("play")) {						 
			GameChannelId channelId = GameChannelId.parse(parts[3]);
			
			// load match data
			Match match = WordgameStore.getMatch(
					channelId.getGameId(),
					channelId.getMatchId(), 
					channelId.getPlayerId());
			
			// open channel
			ChannelService channelService = ChannelServiceFactory.getChannelService();		
			String token = channelService.createChannel(parts[3]);				        	       						
			
			String postUrl = "/wordgame/post/" + parts[3];
			
			// send application page
			resp.setContentType("text/html");
			new SkimpyTemplate(getServletContext().getResourceAsStream("/wordgame-res/wordgame.html"))
				.add("POST_URL", postUrl)
				.add("CHANNEL_TOKEN", token)
				.add("GAME_WORDS", match.getWords())
				.write(resp.getWriter());
			return;
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String cmd = req.getParameter("cmd");		
		String[] parts = req.getRequestURI().split("/");
		String playerChannel = parts[3];
		GameChannelId channelId = GameChannelId.parse(playerChannel);
		String gameId = channelId.getGameId();
		Match match = WordgameStore.getMatch(
				gameId, 
				channelId.getMatchId(), 
				channelId.getPlayerId());
		Player player = match.getPlayer();
		Player opponent = match.getOpponent();
		
		String opponentChannel = GameChannelId.getChannelId(
				gameId,
				channelId.getMatchId(),
				opponent);
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		// start: user pressed start button
		if (cmd.equals("start")) {						
			if (opponent.getStatus() != Status.NOT_CONNECTED) {
				String startedMsg = WordgameProtocol.notifyOpponentStarted();
				channelService.sendMessage(new ChannelMessage(opponentChannel, startedMsg));
			}						
		}
		
		if (cmd.equals("player-completed")) {
			handlePlayerCompleted(playerChannel, match, opponent, opponentChannel, channelService);
			return;
		}
		
		if (cmd.equals("player-update-progress")) {
			handlePlayerUpdateProgress(req, opponent, opponentChannel,
					channelService);
			return;
		}
		
		if (cmd.equals("player-request-replay")) {
			if (opponent.getStatus() != Status.NOT_CONNECTED) {
				String msg = WordgameProtocol.notifyOpponentRequestReplay();
				channelService.sendMessage(new ChannelMessage(opponentChannel, msg));
			}
			return;
		}
		
		if (cmd.equals("player-accept-replay")) {
			// create new match and join both players
			Match newMatch = WordgameStore.createReplayMatch(gameId, match.getId(), player, opponent);
			Player newPlayer = newMatch.getPlayer();
			Player newOpponent = newMatch.getOpponent();
			String newPlayerChannelId = GameChannelId.getChannelId(gameId, newMatch, newPlayer);
			String newOpponentChannelId = GameChannelId.getChannelId(gameId, newMatch, newOpponent);
			
			// redirect both to play
			if (opponent.getStatus() != Status.NOT_CONNECTED) {
				String msg = WordgameProtocol.notifyRedirect("/wordgame/play/" + newOpponentChannelId);
				channelService.sendMessage(new ChannelMessage(opponentChannel, msg));
			}
			
			String msg = WordgameProtocol.notifyRedirect("/wordgame/play/" + newPlayerChannelId);
			channelService.sendMessage(new ChannelMessage(playerChannel, msg));
			
		}
	}

	private void handlePlayerUpdateProgress(HttpServletRequest req,
			Player opponent, String opponentChannel,
			ChannelService channelService) {
		String progress = req.getParameter("progress");
		if (opponent.getStatus() != Status.NOT_CONNECTED) {
			String msg = WordgameProtocol.notifyOpponentProgress(progress);
			channelService.sendMessage(new ChannelMessage(opponentChannel, msg));
		}
	}

	private void handlePlayerCompleted(String channelStr, Match match,
			Player opponent, String opponentChannel,
			ChannelService channelService) {
		
		
		// send win to opponent
		if (opponent.getStatus() != Status.NOT_CONNECTED) {
			String msg = WordgameProtocol.notifyOpponentWin();
			channelService.sendMessage(new ChannelMessage(opponentChannel, msg));
		}
		
		// send win to self
		// notify this player about the opponent
		String msg = WordgameProtocol.notifyPlayerWin();
		channelService.sendMessage(new ChannelMessage(channelStr, msg));
	}
}
