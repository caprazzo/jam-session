package net.caprazzi.tapauth.simplechat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import net.caprazzi.tapauth.ChannelId;
import net.caprazzi.tapauth.Misc;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class SimpleChatChannelsManager {

	private static Cache getCache() {
		try {
			return CacheManager.getInstance().getCacheFactory()
					.createCache(Collections.emptyMap());
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
	}
	

	public static Iterable<String> getChannelIds(String chatId) {
		String cacheKey = "_chat-" + chatId;
		Cache cache = getCache();
		String ids = (String) cache.get(cacheKey);
		if (ids != null)
			return Arrays.asList(ids.split("/"));

		Key dataKey = KeyFactory.createKey("Chat", chatId);
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("Chat", dataKey);
		Entity entity = datastore.prepare(query).asSingleEntity();
		if (entity == null)
			return Collections.emptyList();

		ids = (String) entity.getProperty("_channels");
		// store channel list in cache
		if (Misc.isEmpty(ids))
			return Collections.emptyList();

		cache.put(cacheKey, ids);
		return Arrays.asList(ids.split("/"));
	}

	public static void addChannelId(ChannelId channelId) {
		Key dataKey = KeyFactory.createKey("Chat", channelId.getChatId());
		String cacheKey = "_chat-" + channelId.getChatId();

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("Chat", dataKey);
		Entity entity = datastore.prepare(query).asSingleEntity();

		String channels;
		if (entity == null) {
			channels = channelId.toString() + "/";
			entity = new Entity(dataKey);
			entity.setProperty("_channels", channels);
			datastore.put(entity);
		} else {
			channels = entity.getProperty("_channels").toString();
			if (!Misc.isEmpty(channels)
					&& channels.indexOf(channelId.toString()) == -1) {
				channels = new StringBuilder(channels)
						.append(channelId.toString()).append("/").toString();
				entity.setProperty("_channels", channels);
				datastore.put(entity);
			}
		}

		getCache().put(cacheKey, channels);
	}

	public static void removeChannel(String chatId, String channelId) {
		Key dataKey = KeyFactory.createKey("Chat", chatId);
		String cacheKey = "_chat-" + chatId;

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("Chat", dataKey);
		Entity entity = datastore.prepare(query).asSingleEntity();

		if (entity == null)
			return;

		String channels = entity.getProperty("_channels").toString();
		if (!Misc.isEmpty(channels)) {
			channels = channels.replace(channelId + "/", "");

			// drop chat record if no more channels
			if (Misc.isEmpty(channels)) {
				datastore.delete(dataKey);
				getCache().remove(cacheKey);
				return;
			}

			// update store
			entity.setProperty("_channels", channels);
			datastore.put(entity);

			// update cache
			Cache cache = getCache();
			cache.put(cacheKey, channels);
		}

	}

	public static ChannelId createChannel(String chatId) {
		return new ChannelId(chatId, Misc.randomString(), getScreenName());
	}

	private static final String[] colors = { "pink", "orange", "yellow", "red",
			"blue", "green", "violet", "purple", "gray" };

	private static int screenNameCount = 0;

	private static String getScreenName() {
		return colors[new Random().nextInt(colors.length)];
	}

}
