package net.caprazzi.tapauth.wordgame;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class WordsProvider {

	private static EnumMap<Levels, List<String>> byLevel 
		= new EnumMap<Levels, List<String>>(Levels.class);
	
	static
	{
		try {
			URL rs = WordsProvider.class.getClassLoader().getResource("top-1000-english-words.txt");
			FileInputStream is = new FileInputStream(rs.getFile());
			System.out.println(is);
			List<String> list = loadFromFile(is);

		
		byLevel.put(Levels.EASY, new ArrayList<String>());
		byLevel.put(Levels.MEDIUM, new ArrayList<String>());
		byLevel.put(Levels.HARD, new ArrayList<String>());
		byLevel.put(Levels.MASTER, new ArrayList<String>());
		byLevel.put(Levels.BEYOND, new ArrayList<String>());
		
		for (String word : list.toArray(new String[]{})) {
			byLevel.get(Levels.fromLength(word.length())).add(word);
		}
		
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private enum Levels {
		EASY, MEDIUM, HARD, MASTER, BEYOND;
		
		public static Levels fromLength(int length) {
			if (length <= 2) return Levels.EASY;
			if (length <= 4) return Levels.MEDIUM;
			if (length <= 6) return Levels.HARD;
			if (length <= 8) return Levels.MASTER;
			return Levels.BEYOND;
		}
	}

	private static List<String> loadFromFile(InputStream is) {
		List<String> list = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String word;
		try {
			while((word = reader.readLine()) != null) {
				list.add(word.toLowerCase().trim());
			}
			is.close();
			return list;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Random random = new Random();
	private static String getWordAtLevel(Levels lvl) {
		List<String> list = byLevel.get(lvl);
		return list.get(random.nextInt(list.size()));
	}
	
	public static String getWords() {
		return new StringBuilder()
			.append(getWordAtLevel(Levels.EASY))
			.append(",")
			.append(getWordAtLevel(Levels.MEDIUM))
			.append(",")
			.append(getWordAtLevel(Levels.HARD))
			.append(",")
			.append(getWordAtLevel(Levels.MASTER))
			.toString();
			
	}

}
