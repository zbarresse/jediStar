package fr.jedistar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import fr.jedistar.commands.ModsCommand;

public class Main {
	
	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	//Noms des �l�ments dans le fichier de param�tres
	private static final String PARAM_MODS_JSON_URI = "modsJsonURI";
	private static final String PARAM_SHEET_ID = "equilibrageSheetID";
	private static final String PARAM_AUTH_FILE = "authFile";
	private static final String PARAM_TOKEN = "discordToken";
	private static final String PARAM_GOOGLE_API = "googleAPI";

	private static final String DEFAULT_PARAMETERS_FILE = "settings.json";
	
	public static void main(String ... args) {

		String parametersFilePath = "";
			
		//Si un argument, on l'utilise comme chemin au fichier de param�tres
		if(args.length != 0) {		
			parametersFilePath = args[0];	
		}
		//Sinon, on utilise le chemin par d�faut
		else {
			parametersFilePath = DEFAULT_PARAMETERS_FILE;	
		}

		String token = "";
		
		//Lecture du fichier Json et r�cup�ration des param�tres
		try {
			//Lecture du fichier
			byte[] encoded = Files.readAllBytes(Paths.get(parametersFilePath));
			String parametersJson = new String(encoded, "utf-8");
			
			//D�codage du json
			JSONObject parameters = new JSONObject(parametersJson);
			
			StaticVars.jsonSettings = parameters;
			
			//METTRE LA LECTURE DES PARAMETRES DU PLUS IMPORTANT AU MOINS IMPORTANT
			//Lecture du token Discord
			token = parameters.getString(PARAM_TOKEN);
			
			//URI et encodage du JSON des mods conseill�s
			String modsJsonUri = parameters.getString(PARAM_MODS_JSON_URI);
			ModsCommand.setJsonUri(modsJsonUri);
			
			
			
		}
		catch(IOException e) {
			logger.error("Cannot read the parameters file "+parametersFilePath);
			e.printStackTrace();
			return;
		}
		catch(JSONException e) {
			logger.error("JSON parameters file is incorrectly formatted");
			e.printStackTrace();
		}
		
		//Initialisation bdd
		String url = "jdbc:mysql://localhost/jedistar";
		String user = "jedistar";
		String passwd = "JeDiStArBoT";
		
		try {
			StaticVars.jdbcConnection = DriverManager.getConnection(url, user, passwd);		
		} catch (SQLException e) {
			logger.error("Error connecting to mysql database");
			e.printStackTrace();
		}
		
		logger.info("Launching bot with token -"+token+"-");

		JediStarBot bot = new JediStarBot(token);
		bot.connect();
		
	}
}
