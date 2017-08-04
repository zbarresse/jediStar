package fr.jedistar;

public class Main {

	public static void main(String ... args) {

		if(args.length != 0) {
			System.out.println("Lancement du bot avec le token -"+args[0]+"-");
			JediStarBot bot = new JediStarBot(args[0]);
			bot.connect();
		}
		else {
			System.out.println("Aucun token pass� en param�tre");
		}

	}
}
