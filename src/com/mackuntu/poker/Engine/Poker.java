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
	private static final int NUM_PLAYERS = 6;
	private static final int INITIAL_SMALL_BLIND = 10;
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
	
	public void draw() {
		background(34, 139, 34);  // Forest green background
		
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
		
		// Process next action if not in test mode and key conditions are met
		if (!game.isTestMode()) {
			if (game.getGameState() == GameState.FINISH) {
				if (!showingWinner) {
					// Just entered FINISH state, set flags
					showingWinner = true;
					waitingForNextKey = false;  // Allow next key press
				} else if (keyPressed && !waitingForNextKey) {
					// Key pressed while showing winner, start new hand
					game.startNewHand();
					showingWinner = false;
					waitingForNextKey = true;  // Wait for key release
				}
			} else if (keyPressed && !waitingForNextKey) {
				// Process next action in the game
				boolean actionProcessed = game.processNextAction();
				waitingForNextKey = true;  // Wait for key release
				
				if (actionProcessed && game.getGameState() == GameState.FINISH) {
					showingWinner = true;
					waitingForNextKey = false;  // Allow immediate key press for next hand
				}
			}
		} else {
			// In test mode, process actions without waiting
			if (game.getGameState() != GameState.FINISH && game.processNextAction()) {
				if (game.getGameState() == GameState.FINISH) {
					delay(2000);
					game.startNewHand();
				}
			}
		}
	}
	
	public void keyPressed() {
		keyPressed = true;
	}
	
	public void keyReleased() {
		keyPressed = false;
		waitingForNextKey = false;  // Allow next action after key release
	}
}
