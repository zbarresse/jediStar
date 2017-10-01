package fr.jedistar.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;

public abstract class OnlineDataParser {

	private final static Logger logger = LoggerFactory.getLogger(OnlineDataParser.class);

	private final static String SQL_SELECT_CHARS_EXPIRATION = "SELECT expiration FROM characters LIMIT 1;";
	private final static String SQL_DELETE_CHARS = "DELETE FROM characters";
	private final static String SQL_INSERT_CHARS = "INSERT INTO characters VALUES (?,?,?,?,?,?,?,?);";
	
	private final static String CHARS_URI = "https://swgoh.gg/api/characters/?format=json";
	
	public static boolean parseSwgohGGCharacters() {
		
		Connection conn = StaticVars.jdbcConnection;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		BufferedReader in = null;
		
		try {
			//V�rifier si un m�j est n�cessaire
			stmt = conn.prepareStatement(SQL_SELECT_CHARS_EXPIRATION);
			
			rs = stmt.executeQuery();
			
			boolean updateNeeded = true;
			
			if(rs.next()) {
				Date expiration = rs.getTimestamp("expiration");
				if(expiration.after(new Date())) {
					updateNeeded = false;
				}
			}
			
			if(!updateNeeded) {
				return true;
			}
			
			rs.close();
			stmt.close();
			
			//Charger l'API swgoh.gg
			URL url = new URL(CHARS_URI);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String json = in.readLine();

			JSONArray charsJson = new JSONArray(json);
			
			//Vider la table
			stmt = conn.prepareStatement(SQL_DELETE_CHARS);
			stmt.executeUpdate();
			
			stmt.close();
			
			//Ins�rer les donn�es
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(SQL_INSERT_CHARS);
			
			Calendar expirationCal = Calendar.getInstance();
			expirationCal.add(Calendar.DAY_OF_MONTH, 1);
			java.sql.Timestamp expiration = new Timestamp(expirationCal.getTimeInMillis());
			
			for(int i=0;i<charsJson.length();i++) {
				JSONObject character = charsJson.getJSONObject(i);
				
				stmt.setString(1,character.getString("name"));
				stmt.setString(2, character.getString("base_id"));
				stmt.setString(3, character.getString("url"));
				stmt.setString(4, character.getString("image"));
				stmt.setInt(5, character.getInt("power"));
				stmt.setString(6, character.getString("description"));
				stmt.setInt(7, character.getInt("combat_type"));
				stmt.setTimestamp(8, expiration);
				
				stmt.addBatch();
			}
			
			stmt.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {

			try {
				if(stmt != null) {
					stmt.close();
				}
				if(rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		}
		
		return true;
	}
}
