package com.mackuntu.poker.Engine;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Player.RandomPlayerStrategy;
import com.mackuntu.poker.game.GameState;
import com.mackuntu.poker.game.PokerGame;
import com.mackuntu.poker.ui.PokerUI;

public class Poker extends PApplet {
	private static final int NUM_PLAYERS = 12;
	private static final int WINDOW_WIDTH = 1200;
	private static final int WINDOW_HEIGHT = 800;
	
	private PokerGame game;
	private PokerUI ui;
	private boolean keyPressed;
	private boolean waitingForNextKey;  // Add flag to wait for next key press
	private Player[] players;
	private PImage[] deckImages;
	private PImage cardBack;
	private PFont font;
	private boolean showingWinner;  // Add flag to track winner state
	private boolean gameOver;  // Add this field at the top with other fields
	private boolean testMode;
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "--location=0,0", "com.mackuntu.poker.Engine.Poker" });
	}
	
	public void setup() {
		size(WINDOW_WIDTH, WINDOW_HEIGHT);

		// Load resources
		font = createFont("Arial", 32);
		loadCardImages();
		
		// Create players
		players = new Player[NUM_PLAYERS];
		for (int i = 0; i < NUM_PLAYERS; i++) {
			players[i] = new Player("Player " + i, new RandomPlayerStrategy());
			players[i].setMoney(1000);
		}
		
		// Initialize game
		game = new PokerGame(players, false);
		ui = new PokerUI(this, deckImages, cardBack, font);
		
		// Start first hand
		game.startNewHand();
		waitingForNextKey = false;  // Initialize waiting flag
		showingWinner = false;  // Initialize winner state
		gameOver = false;  // Initialize game over state
	}
	
	private void loadCardImages() {
		PImage allCards = loadImage("data/cards.png");
		deckImages = new PImage[54];
		
		for (int suite = 0; suite < 4; suite++) {
			for (int rank = 1; rank < 14; rank++) {
				Card newCard = new Card(rank, suite);
				if (rank == 1) {
					deckImages[newCard.hashCode()] = allCards.get(0, suite * 118, 84, 118);
				} else {
					deckImages[newCard.hashCode()] = allCards.get((14 - rank) * 84, suite * 118, 84, 118);
				}
			}
		}
		cardBack = allCards.get(13 * 84, 2 * 118, 84, 118);
	}
	
	private boolean hasPlayersWithMoney() {
		int playersWithMoney = 0;
		for (Player player : players) {
			if (player.getMoney() > 0) {
				playersWithMoney++;
			}
		}
		return playersWithMoney >= 2;
	}
	
	public void draw() {
		background(34, 139, 34);  // Forest green background
		
		// Check if game is over (only one player has money)
		if (!hasPlayersWithMoney()) {
			gameOver = true;
		}
		
		// Draw game elements
		ui.drawTable(WINDOW_WIDTH, WINDOW_HEIGHT);
		ui.drawCommunityCards(game.getCardManager().getCommunityCards(), WINDOW_WIDTH, WINDOW_HEIGHT);
		ui.drawPotInfo(game.getPot(), game.getSmallBlind(), game.getBigBlind(), 
					  0, WINDOW_WIDTH, WINDOW_HEIGHT);  // TODO: Add round count to game
		
		// Draw players
		for (int i = 0; i < NUM_PLAYERS; i++) {
			ui.drawPlayer(players[i], game.getCurrentPlayer(), i, 0.8f);
		}
		
		// Draw hand analysis
		ArrayList<String> handAnalysis = new ArrayList<>(game.getHandAnalysis());
		ui.drawHandAnalysis(handAnalysis, game.getCurrentPlayer(), 
						  players, game.getCardManager().getCommunityCards(), WINDOW_WIDTH, WINDOW_HEIGHT);
		
		if (gameOver) {
			// Draw game over screen
			fill(0, 200);  // Semi-transparent black
			rect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
			fill(255);
			textAlign(CENTER, CENTER);
			textSize(48);
			text("GAME OVER", WINDOW_WIDTH/2, WINDOW_HEIGHT/2 - 40);
			
			// Find the winner
			String winnerName = "";
			int winnerMoney = 0;
			for (Player player : players) {
				if (player.getMoney() > winnerMoney) {
					winnerName = player.getName();
					winnerMoney = player.getMoney();
				}
			}
			
			textSize(32);
			text(winnerName + " wins with $" + winnerMoney + "!", WINDOW_WIDTH/2, WINDOW_HEIGHT/2 + 20);
			textSize(24);
			text("Press 'R' to start a new game", WINDOW_WIDTH/2, WINDOW_HEIGHT/2 + 80);
			return;
		}
		
		// Process game actions
		if (!game.isTestMode()) {
			if (keyPressed && !waitingForNextKey) {
				System.out.println("\n=== Key Pressed ===");
				System.out.println("Current Game State: " + game.getGameState());
				System.out.println("Active Players: " + game.getActivePlayerCount());
				System.out.println("Players with money: " + countPlayersWithMoney());
				System.out.println("Current Pot: $" + game.getPot());
				
				if (game.getGameState() == GameState.FINISH) {
					System.out.println("Starting new hand (in FINISH state)");
					game.startNewHand();
				} else {
					System.out.println("Processing next action for player " + game.getCurrentPlayer());
					game.processNextAction();
				}
				waitingForNextKey = true;
				
				System.out.println("After action - Game State: " + game.getGameState());
				System.out.println("==================");
			}
		} else {
			// Test mode - process actions automatically
			if (game.getGameState() == GameState.FINISH) {
				game.startNewHand();
			} else {
				game.processNextAction();
			}
		}
	}
	
	public void keyPressed() {
		System.out.println("\n=== Key Pressed ===");
		System.out.println("Current Game State: " + game.getGameState());
		System.out.println("Active Players: " + game.getActivePlayerCount());
		System.out.println("Players with money: " + countPlayersWithMoney());
		System.out.println("Current Pot: $" + game.getPot());
		
		if (gameOver) {
			System.out.println("Game is over - checking for restart");
			if (key == 'r' || key == 'R') {
				System.out.println("Restarting game");
				restartGame();
			}
			return;
		}

		if (!testMode) {
			if (game.getGameState() == GameState.FINISH) {
				System.out.println("Hand is finished - starting new hand");
				game.processNextAction();  // This will start a new hand
			} else {
				System.out.println("Processing next action");
				game.processNextAction();
			}
		}
	}
	
	public void keyReleased() {
		keyPressed = false;
		waitingForNextKey = false;  // Allow next action after key release
	}
	
	private int countPlayersWithMoney() {
		int count = 0;
		for (Player player : players) {
			if (player.getMoney() > 0) {
				count++;
			}
		}
		return count;
	}
	
	private void restartGame() {
		// Reset all players
		for (Player player : players) {
			player.setMoney(1000);
		}
		
		// Create new game instance
		game = new PokerGame(players, false);
		game.startNewHand();
		
		// Reset game state
		gameOver = false;
		showingWinner = false;
		waitingForNextKey = false;
	}
}
