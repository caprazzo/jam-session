package net.caprazzi.tapauth.wordgame;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.wordgame.Player.Status;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class WordgameChannelHandler extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String[] parts = req.getRequestURI().split("/");
		if (parts.length != 4) {
			resp.sendError(404);
			return;
		}
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		ChannelPresence presence = channelService.parsePresence(req);
		GameChannelId channelId = GameChannelId.parse(presence.clientId());
		
		// get match
		Match match = WordgameStore.getMatch(
				channelId.getGameId(), 
				channelId.getMatchId(), 
				channelId.getPlayerId());
		Player opponent = match.getOpponent();		
		
		if (parts[3].equals("connected")) {
			// set the state of this player as connected
			match.getPlayer().setConnected();
			WordgameStore.storePlayer(
					channelId.getGameId(),
					channelId.getMatchId(),
					match.getPlayer());
			
			if (opponent != null) {				
				
				
				if (opponent.getStatus() != Status.NOT_CONNECTED) {
					// notify the opponent about this player
					String opponentChannel = GameChannelId.getChannelId(
							channelId.getGameId(), 
							channelId.getMatchId(),
							opponent);
					
					String msg = WordgameProtocol.notifyOpponentConnected(match.getPlayer());
					channelService.sendMessage(new ChannelMessage(opponentChannel, msg));
				}
												
				// notify this player about the opponent
				String msg = WordgameProtocol.notifyOpponentConnected(opponent);
				channelService.sendMessage(new ChannelMessage(presence.clientId(), msg));				
			}
			return;
		}
		
		if (parts[3].equals("disconnected-DISABLED")) {
			
			// remove player from game
			WordgameStore.removePlayer(
					channelId.getGameId(),
					channelId.getMatchId(),
					match.getPlayer());

			// notify opponent
			if (opponent != null && opponent.getStatus() != Status.NOT_CONNECTED) {
				String opponentChannel = GameChannelId.getChannelId(
						channelId.getGameId(), 
						channelId.getMatchId(),
						opponent);
				String partedMsg = WordgameProtocol.notifyParted();
				channelService.sendMessage(new ChannelMessage(opponentChannel, partedMsg));
			}
			return;
		}
		
	}
	
}
