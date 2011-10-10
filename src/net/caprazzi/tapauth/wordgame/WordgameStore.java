package net.caprazzi.tapauth.wordgame;

import java.util.Date;

import net.caprazzi.tapauth.Misc;
import net.caprazzi.tapauth.wordgame.Player.Status;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;

public class WordgameStore {
	
	private static final String GAME_ENTITY = "WordGame";
	private static final String MATCH_ENTITY = "Match";
	private static final String PLAYER_ENTITY = "Player";
	
	private static final long OPEN_MATCH_LIFE = 1000 * 60 * 3; // 3 minutes

	/**
	 * Find or create an open match and join it
	 */
	public static Match joinMatch(String gameId, String playerId) {
		long ts = new Date().getTime();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = ds.beginTransaction();
		try {						
			Key gameKey = KeyFactory.createKey(GAME_ENTITY, gameId);			
			
			// find or create an open match
			Entity matchEntity = findOpenMatch(ts, ds, gameKey);
			Entity opponent = null;
			if (matchEntity == null) {
				String matchId = Misc.randomString();
				matchEntity = createOpenMatch(ts, gameKey, matchId);
			}
			else {				
				matchEntity.setProperty("status", "active");
				opponent = ds.prepare(
						new Query(PLAYER_ENTITY, matchEntity.getKey()))
						.asSingleEntity();
			}
									
			// store a new player for this match
			Entity player = new Entity(PLAYER_ENTITY, playerId, matchEntity.getKey());
			player.setProperty("STATUS", Status.JOINED.toString());
			
			ds.put(matchEntity);
			ds.put(player);			
			
			txn.commit();
			
			return Match.fromEntities(matchEntity, player, opponent);
		}				
		finally {
			if (txn.isActive())
				txn.rollback();			
		}
	}
	
	public static Match createReplayMatch(String gameId, String matchId, Player player, Player opponent) {
		long ts = new Date().getTime();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = ds.beginTransaction();
		try {	
			Key gameKey = KeyFactory.createKey(GAME_ENTITY, gameId);
			Entity matchEntity = createOpenMatch(ts, gameKey, matchId);
			
			matchEntity.setProperty("status", "active");
			
			Entity newPlayerEn = new Entity(PLAYER_ENTITY, player.getId(), matchEntity.getKey());
			newPlayerEn.setProperty("STATUS", Status.JOINED.toString());
			
			Entity newOpponentEn = new Entity(PLAYER_ENTITY, opponent.getId(), matchEntity.getKey());
			newOpponentEn.setProperty("STATUS", Status.JOINED.toString());
			
			ds.put(matchEntity);
			ds.put(newPlayerEn);
			ds.put(newOpponentEn);
			
			txn.commit();
			
			return Match.fromEntities(matchEntity, newPlayerEn, newOpponentEn);
		}
		finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	/**
	 * find the match 
	 * @param gameId
	 * @param matchId
	 * @param playerId
	 * @return
	 */
	public static Match getMatch(String gameId, String matchId, String playerId) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = ds.beginTransaction();
		try {						
			Key gameKey = KeyFactory.createKey(GAME_ENTITY, gameId);		
			Key matchKey = KeyFactory.createKey(gameKey, MATCH_ENTITY, matchId);
			
			// get match
			Entity matchEntity = tryGetEntity(ds, matchKey);
			if (matchEntity == null)
				return null;
			
			Player player = null;
			Player opponent = null;
			
			// get players
			Query playersQuery = new Query(PLAYER_ENTITY, matchKey);
			Iterable<Entity> players = ds.prepare(playersQuery).asIterable();
			for (Entity playerEntity : players) {
				Player p = Player.fromEntity(playerEntity);
				if (p.getId().equals(playerId)) {
					player = p;
				} else {
					opponent = p;
				}
			}
			
			txn.commit();
			return new Match(matchEntity, player, opponent);
		}
		finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	private static Entity tryGetEntity(DatastoreService datastore, Key key) {
		try {
			return datastore.get(key);
		}
		catch(EntityNotFoundException ex) {
			return null;
		}
	}

	// creates an open match
	private static Entity createOpenMatch(long timestamp, Key gameKey, String matchId) {
		Entity openMatch;
		openMatch = new Entity(MATCH_ENTITY, matchId, gameKey);
		openMatch.setProperty("status", "open");
		openMatch.setProperty("timestamp", timestamp);
		openMatch.setProperty("WORDS", WordsProvider.getWords());
		return openMatch;
	}

	// find the newest non-expired open match for this game
	private static Entity findOpenMatch(long timestamp, DatastoreService ds, Key gameKey) { 
		Query q = new Query(MATCH_ENTITY, gameKey)
			.addFilter("status", FilterOperator.EQUAL, "open")
			.addFilter("timestamp", FilterOperator.GREATER_THAN, timestamp - OPEN_MATCH_LIFE)
			.addSort("timestamp", SortDirection.DESCENDING);			
		
		Entity openMatch = null;
		for (Entity match : ds.prepare(q).asIterable()) {
			openMatch = match;
			break;
		}
		return openMatch;
	}
	
	private Entity getEntity(DatastoreService datastore, Key gameKey) {
		try {
			return datastore.get(gameKey);
		}
		catch(EntityNotFoundException ex) {
			throw new RuntimeException("Game entity not found for " + gameKey, ex);
		}
	}
	
	public static void removePlayer(String gameId, String matchId, Player player) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = ds.beginTransaction();
		try {						
			Key gameKey = KeyFactory.createKey(GAME_ENTITY, gameId);
			Key matchKey = KeyFactory.createKey(gameKey, MATCH_ENTITY, matchId);
			Key playerKey = KeyFactory.createKey(matchKey, PLAYER_ENTITY, player.getId());
			ds.delete(playerKey);
			txn.commit();
		}
		finally {
			if (txn.isActive())
				txn.rollback();
		}
	}


	public static void storePlayer(String gameId, String matchId, Player player) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = ds.beginTransaction();
		try {						
			Key gameKey = KeyFactory.createKey(GAME_ENTITY, gameId);
			Key matchKey = KeyFactory.createKey(gameKey, MATCH_ENTITY, matchId);
				
			Entity entity = new Entity(PLAYER_ENTITY, player.getId(), matchKey);			
			entity.setProperty("STATUS", player.getStatus().toString());
			
			ds.put(entity);
			
			txn.commit();
		}
		finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	
}