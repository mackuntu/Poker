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
    private static final float CARD_SCALE = 1.0f;  // Increased from 0.8f
    private static final int TABLE_COLOR = 0xFF228B22; // Forest green with full alpha
    private static final int ANALYSIS_HEIGHT = 120;  // Increased from 80
    private static final int MAX_VISIBLE_LINES = 5;
    private static final int LINE_HEIGHT = 20;

    private final PApplet applet;
    private final PImage[] deckImages;
    private final PImage cardBack;
    private final int[] playerX;
    private final int[] playerY;
    
    public PokerUI(PApplet applet, PImage[] deckImages, PImage cardBack, PFont font) {
        this.applet = applet;
        this.deckImages = deckImages;
        this.cardBack = cardBack;
        this.playerX = new int[6];  // 6-max poker
        this.playerY = new int[6];
        
        // Set default text properties
        applet.textFont(font);
        applet.textAlign(PApplet.CENTER, PApplet.CENTER);
        
        // Calculate player positions
        for (int i = 0; i < 6; i++) {
            double angle = (i * 2 * Math.PI / 6) - Math.PI/2;
            int centerX = applet.width / 2;
            int centerY = applet.height / 2;
            int tableRadiusX = applet.width / 2 - 150;
            int tableRadiusY = applet.height / 2 - 100;
            playerX[i] = (int)(centerX + tableRadiusX * Math.cos(angle) - 84);
            playerY[i] = (int)(centerY + tableRadiusY * Math.sin(angle) - 59);
        }
    }

    public void drawTable(int width, int height) {
        // Draw table background
        applet.background(0);
        applet.noStroke();
        
        // Draw table outline
        applet.fill(TABLE_COLOR);
        applet.ellipse(width/2, height/2, width/1.2f, height/1.2f);  // Increased table size
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
        applet.textSize(24);  // Increased text size
        applet.text("Pot: $" + pot, width/2, height/2 + CARD_HEIGHT/2 + 20);
        applet.textSize(18);  // Increased text size
        applet.text("Blinds: $" + smallBlind + "/$" + bigBlind + " (Round " + roundCount + ")", 
            width/2, height/2 + CARD_HEIGHT/2 + 50);
    }

    public void drawPlayer(Player player, int currentPlayerIndex, int playerIndex, float cardScale) {
        ArrayList<Card> playerHand = player.getCards();
        cardScale = CARD_SCALE;  // Use the constant scale
        
        // Get stored coordinates for this player position
        int x = playerX[playerIndex];
        int y = playerY[playerIndex];
        
        // Highlight current player's turn
        if (playerIndex == currentPlayerIndex) {
            applet.stroke(255, 255, 0);  // Yellow highlight
            applet.strokeWeight(3);
            applet.noFill();
            applet.rect(x - 5, y - 25, 
                CARD_WIDTH * 2 * cardScale + 10, CARD_HEIGHT * cardScale + 90);
            applet.strokeWeight(1);
            applet.noStroke();
        }
        
        // Draw cards
        if(playerHand == null || playerHand.isEmpty()) {
            applet.image(cardBack, x, y, 
                CARD_WIDTH * cardScale, CARD_HEIGHT * cardScale);
            applet.image(cardBack, x + CARD_WIDTH * cardScale, y, 
                CARD_WIDTH * cardScale, CARD_HEIGHT * cardScale);
        } else if (playerHand.size() >= 2) {
            applet.image(deckImages[playerHand.get(0).hashCode()], x, y, 
                CARD_WIDTH * cardScale, CARD_HEIGHT * cardScale);
            applet.image(deckImages[playerHand.get(1).hashCode()], 
                x + CARD_WIDTH * cardScale, y, 
                CARD_WIDTH * cardScale, CARD_HEIGHT * cardScale);
        }
        
        // Draw player info
        drawPlayerInfo(player, x, y, cardScale);
    }

    private void drawPlayerInfo(Player player, int x, int y, float cardScale) {
        applet.fill(255);
        applet.textSize(18);  // Increased text size
        
        // Draw name
        applet.text(player.getName(), 
            x + CARD_WIDTH * cardScale, y - 25);
        
        if (!player.isFolded()) {
            // Draw action
            if (player.getLastAction() != null) {
                applet.text(player.getLastAction(), 
                    x + CARD_WIDTH * cardScale, 
                    y + CARD_HEIGHT * cardScale + 25);
            }
            
            // Draw stack and bet
            String moneyInfo = "$" + player.getMoney();
            if (player.getCommitted() > 0) {
                moneyInfo += " (Bet: $" + player.getCommitted() + ")";
            }
            applet.text(moneyInfo, 
                x + CARD_WIDTH * cardScale, 
                y + CARD_HEIGHT * cardScale + 50);
        } else {
            applet.text("FOLD", 
                x + CARD_WIDTH * cardScale, 
                y + CARD_HEIGHT * cardScale + 25);
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
        applet.textSize(18);
        
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