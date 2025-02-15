package com.mackuntu.poker.ui;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Evaluator.HandEvaluator;
import java.util.ArrayList;

public class PokerUI {
    private static final int CARD_WIDTH = 84;
    private static final int CARD_HEIGHT = 118;
    private static final float CARD_SCALE = 0.8f;  // Reduced scale for more players
    private static final int TABLE_COLOR = 0xFF228B22; // Forest green with full alpha
    private static final int ANALYSIS_HEIGHT = 120;
    private static final int MAX_VISIBLE_LINES = 5;
    private static final int LINE_HEIGHT = 20;
    private static final int MAX_PLAYERS = 12;

    private final PApplet applet;
    private final PImage[] deckImages;
    private final PImage cardBack;
    private final int[] playerX;
    private final int[] playerY;
    
    public PokerUI(PApplet applet, PImage[] deckImages, PImage cardBack, PFont font) {
        this.applet = applet;
        this.deckImages = deckImages;
        this.cardBack = cardBack;
        this.playerX = new int[MAX_PLAYERS];
        this.playerY = new int[MAX_PLAYERS];
        
        // Set default text properties
        applet.textFont(font);
        applet.textAlign(PApplet.CENTER, PApplet.CENTER);
        
        // Calculate player positions in an elliptical arrangement
        calculatePlayerPositions();
    }

    private void calculatePlayerPositions() {
        int centerX = applet.width / 2;
        int centerY = applet.height / 2;
        float tableRadiusX = applet.width * 0.4f;  // Adjusted for more players
        float tableRadiusY = applet.height * 0.35f; // Adjusted for more players
        
        for (int i = 0; i < MAX_PLAYERS; i++) {
            // Calculate angle for each player, offset by -90 degrees to start from top
            double angle = (i * 2 * Math.PI / MAX_PLAYERS) - Math.PI/2;
            
            // Calculate position with elliptical adjustment
            playerX[i] = (int)(centerX + tableRadiusX * Math.cos(angle) - (CARD_WIDTH * CARD_SCALE) / 2);
            playerY[i] = (int)(centerY + tableRadiusY * Math.sin(angle) - (CARD_HEIGHT * CARD_SCALE) / 2);
        }
    }

    public void drawTable(int width, int height) {
        // Draw table background
        applet.background(0);
        applet.noStroke();
        
        // Draw table outline
        applet.fill(TABLE_COLOR);
        applet.ellipse(width/2, height/2, width * 0.85f, height * 0.75f);  // Adjusted table size
    }

    public void drawCommunityCards(ArrayList<Card> deck, int width, int height) {
        if (deck == null || deck.isEmpty()) return;
        
        int totalWidth = (int)(deck.size() * CARD_WIDTH * CARD_SCALE);
        int startX = width/2 - totalWidth/2;
        
        for(int i = 0; i < deck.size(); i++) {
            applet.image(deckImages[deck.get(i).hashCode()],
                startX + i * (CARD_WIDTH * CARD_SCALE), 
                height/2 - (CARD_HEIGHT * CARD_SCALE)/2,
                CARD_WIDTH * CARD_SCALE,
                CARD_HEIGHT * CARD_SCALE);
        }
    }

    public void drawPotInfo(int pot, int smallBlind, int bigBlind, int roundCount, int width, int height) {
        applet.fill(255);
        applet.textSize(20);  // Slightly smaller text for more players
        applet.text("Pot: $" + pot, width/2, height/2 + CARD_HEIGHT * CARD_SCALE/2 + 20);
        applet.textSize(16);
        applet.text("Blinds: $" + smallBlind + "/$" + bigBlind + " (Round " + roundCount + ")", 
            width/2, height/2 + CARD_HEIGHT * CARD_SCALE/2 + 45);
    }

    public void drawPlayer(Player player, int currentPlayerIndex, int playerIndex, float cardScale) {
        if (playerIndex >= MAX_PLAYERS) return;
        
        // Get stored coordinates for this player position
        int x = playerX[playerIndex];
        int y = playerY[playerIndex];
        
        // Highlight current player's turn
        if (playerIndex == currentPlayerIndex) {
            applet.stroke(255, 255, 0);  // Yellow highlight
            applet.strokeWeight(3);
            applet.noFill();
            applet.rect(x - 5, y - 20, 
                CARD_WIDTH * 2 * CARD_SCALE + 10, CARD_HEIGHT * CARD_SCALE + 70);
            applet.strokeWeight(1);
            applet.noStroke();
        }
        
        // Draw cards
        ArrayList<Card> playerHand = player.getCards();
        if(playerHand == null || playerHand.isEmpty()) {
            applet.image(cardBack, x, y, 
                CARD_WIDTH * CARD_SCALE, CARD_HEIGHT * CARD_SCALE);
            applet.image(cardBack, x + CARD_WIDTH * CARD_SCALE, y, 
                CARD_WIDTH * CARD_SCALE, CARD_HEIGHT * CARD_SCALE);
        } else if (playerHand.size() >= 2) {
            applet.image(deckImages[playerHand.get(0).hashCode()], x, y, 
                CARD_WIDTH * CARD_SCALE, CARD_HEIGHT * CARD_SCALE);
            applet.image(deckImages[playerHand.get(1).hashCode()], 
                x + CARD_WIDTH * CARD_SCALE, y, 
                CARD_WIDTH * CARD_SCALE, CARD_HEIGHT * CARD_SCALE);
        }
        
        // Draw player info with adjusted text sizes
        drawPlayerInfo(player, x, y);
    }

    private void drawPlayerInfo(Player player, int x, int y) {
        applet.fill(255);
        applet.textSize(14);  // Smaller text size for more players
        
        // Draw name
        applet.text(player.getName(), 
            x + CARD_WIDTH * CARD_SCALE, y - 20);
        
        if (!player.isFolded()) {
            // Draw action
            if (player.getLastAction() != null) {
                applet.text(player.getLastAction(), 
                    x + CARD_WIDTH * CARD_SCALE, 
                    y + CARD_HEIGHT * CARD_SCALE + 20);
            }
            
            // Draw stack and bet
            String moneyInfo = "$" + player.getMoney();
            if (player.getCommitted() > 0) {
                moneyInfo += " (Bet: $" + player.getCommitted() + ")";
            }
            applet.text(moneyInfo, 
                x + CARD_WIDTH * CARD_SCALE, 
                y + CARD_HEIGHT * CARD_SCALE + 40);
        } else {
            applet.text("FOLD", 
                x + CARD_WIDTH * CARD_SCALE, 
                y + CARD_HEIGHT * CARD_SCALE + 20);
        }
    }

    public void drawHandAnalysis(ArrayList<String> handAnalysis, int currentPlayerIndex, 
            Player[] players, ArrayList<Card> communityCards, int width, int height) {
        // Draw background
        applet.fill(0, 100);
        applet.noStroke();
        applet.rect(10, height - ANALYSIS_HEIGHT - 10, width - 20, ANALYSIS_HEIGHT);
        
        // Draw analysis text
        applet.fill(255);
        applet.textAlign(PApplet.LEFT, PApplet.TOP);
        applet.textSize(16);  // Slightly smaller text
        
        ArrayList<String> allLines = new ArrayList<>();
        
        // Add current hand info
        if (currentPlayerIndex >= 0 && currentPlayerIndex < players.length 
                && !players[currentPlayerIndex].isFolded()) {
            ArrayList<Card> playerCards = players[currentPlayerIndex].getCards();
            if (playerCards != null) {
                ArrayList<Card> allCards = new ArrayList<>(playerCards);
                if (communityCards != null) {
                    allCards.addAll(communityCards);
                }
                HandEvaluator currentHand = new HandEvaluator(allCards);
                String handDesc = currentHand.getString();
                allLines.add("Current Hand: " + handDesc);
            }
        }
        
        // Add action history
        if (handAnalysis != null) {
            allLines.addAll(handAnalysis);
        }
        
        // Calculate scroll position to show last MAX_VISIBLE_LINES lines
        int startIndex = Math.max(0, allLines.size() - MAX_VISIBLE_LINES);
        float y = height - ANALYSIS_HEIGHT - 5;
        
        // Draw visible lines
        for (int i = startIndex; i < allLines.size(); i++) {
            applet.text(allLines.get(i), 20, y);
            y += LINE_HEIGHT;
        }
        
        // Reset text alignment
        applet.textAlign(PApplet.CENTER, PApplet.CENTER);
    }
} 