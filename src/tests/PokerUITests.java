import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Player.TestStrategy;
import com.mackuntu.poker.ui.PokerUI;
import java.util.ArrayList;

public class PokerUITests {
    private TestPApplet applet;
    private PokerUI ui;
    private PImage[] deckImages;
    private PImage cardBack;
    private Player testPlayer;
    
    // Test PApplet implementation for UI testing
    private static class TestPApplet extends PApplet {
        public int lastFillColor = 0;
        public float lastTextSize = 0;
        public String lastText = "";
        public float lastX = 0;
        public float lastY = 0;
        public ArrayList<Float> textSizes = new ArrayList<>();
        public float lastStrokeWeight = 0;
        public int lastStrokeColor = 0;
        public boolean isStroked = true;
        public boolean isFilled = true;
        public ArrayList<String> drawnText = new ArrayList<>();  // Track all text drawn
        
        @Override
        public void fill(int rgb) {
            lastFillColor = rgb;
            isFilled = true;
        }

        @Override
        public void fill(int rgb, float alpha) {
            lastFillColor = rgb;
            isFilled = true;
        }
        
        @Override
        public void textSize(float size) {
            lastTextSize = size;
            textSizes.add(size);
        }
        
        @Override
        public void text(String str, float x, float y) {
            lastText = str;
            drawnText.add(str);  // Add to list of all drawn text
            lastX = x;
            lastY = y;
        }

        @Override
        public void textFont(PFont font) {
            // No-op for testing
        }

        @Override
        public void textAlign(int alignX, int alignY) {
            // No-op for testing
        }

        @Override
        public void noStroke() {
            isStroked = false;
        }

        @Override
        public void stroke(int rgb) {
            lastStrokeColor = rgb;
            isStroked = true;
        }

        @Override
        public void stroke(float r, float g, float b) {
            lastStrokeColor = color((int)r, (int)g, (int)b);
            isStroked = true;
        }

        @Override
        public void strokeWeight(float weight) {
            lastStrokeWeight = weight;
        }

        @Override
        public void noFill() {
            isFilled = false;
        }

        @Override
        public void rect(float x, float y, float w, float h) {
            lastX = x;
            lastY = y;
        }

        @Override
        public void image(PImage img, float x, float y, float w, float h) {
            lastX = x;
            lastY = y;
        }

        @Override
        public void ellipse(float x, float y, float w, float h) {
            lastX = x;
            lastY = y;
        }

        @Override
        public void background(int rgb) {
            // No-op for testing
        }
        
        public void clearTracking() {
            lastFillColor = 0;
            lastTextSize = 0;
            lastText = "";
            lastX = 0;
            lastY = 0;
            textSizes.clear();
            drawnText.clear();  // Clear drawn text list
            lastStrokeWeight = 0;
            lastStrokeColor = 0;
            isStroked = true;
            isFilled = true;
        }
        
        // Helper method to check if any drawn text contains a substring
        public boolean anyTextContains(String substring) {
            return drawnText.stream().anyMatch(text -> text.contains(substring));
        }
        
        // Mock dimensions for testing
        public int width = 1200;
        public int height = 800;
    }
    
    @BeforeEach
    public void setUp() {
        applet = new TestPApplet();
        deckImages = new PImage[54];
        cardBack = new PImage();
        PFont font = new PFont();
        
        // Initialize test player
        testPlayer = new Player("Test Player", new TestStrategy());
        testPlayer.setMoney(1000);
        
        ui = new PokerUI(applet, deckImages, cardBack, font);
    }
    
    @Test
    public void testPotInfoDisplay() {
        int pot = 500;
        int smallBlind = 10;
        int bigBlind = 20;
        int roundCount = 1;
        
        applet.clearTracking();
        ui.drawPotInfo(pot, smallBlind, bigBlind, roundCount, applet.width, applet.height);
        
        assertEquals(255, applet.lastFillColor, "Text color should be white");
        assertEquals(24.0f, applet.textSizes.get(0), "Pot text size should be 24");
        assertEquals(18.0f, applet.textSizes.get(1), "Blinds text size should be 18");
        assertTrue(applet.anyTextContains(String.valueOf(smallBlind)), "Small blind should be displayed");
        assertTrue(applet.anyTextContains(String.valueOf(bigBlind)), "Big blind should be displayed");
    }
    
    @Test
    public void testPlayerInfoDisplay() {
        testPlayer.setMoney(1000);
        testPlayer.bet(100);  // Commit some money, reduces money to 900
        testPlayer.setLastAction("CALL");
        
        applet.clearTracking();
        ui.drawPlayer(testPlayer, 0, 0, 1.0f);
        
        // Debug output
        System.out.println("All drawn text:");
        for (String text : applet.drawnText) {
            System.out.println("- " + text);
        }
        
        // Check for money info with correct amounts after betting
        boolean foundMoney = applet.drawnText.stream()
            .anyMatch(text -> text.contains("$900"));  // Money is 900 after betting 100
        boolean foundBet = applet.drawnText.stream()
            .anyMatch(text -> text.contains("Bet: $100"));
            
        assertTrue(foundMoney, "Player money should be displayed (looking for $900 in: " + applet.drawnText + ")");
        assertTrue(foundBet, "Committed amount should be displayed (looking for Bet: $100 in: " + applet.drawnText + ")");
        
        // Also verify other expected elements
        assertTrue(applet.drawnText.stream().anyMatch(text -> text.contains(testPlayer.getName())), 
            "Player name should be displayed");
        assertTrue(applet.drawnText.stream().anyMatch(text -> text.contains("CALL")), 
            "Last action should be displayed");
    }
    
    @Test
    public void testFoldedPlayerDisplay() {
        testPlayer.fold();
        
        applet.clearTracking();
        ui.drawPlayer(testPlayer, 0, 0, 1.0f);
        
        assertTrue(applet.anyTextContains("FOLD"), "Folded status should be displayed");
    }
    
    @Test
    public void testHandAnalysisDisplay() {
        ArrayList<String> analysis = new ArrayList<>();
        analysis.add("Player 1 raises to $100");
        Player[] players = new Player[]{testPlayer};
        ArrayList<Card> communityCards = new ArrayList<>();
        
        applet.clearTracking();
        ui.drawHandAnalysis(analysis, 0, players, communityCards, applet.width, applet.height);
        
        assertTrue(applet.anyTextContains("Player 1 raises to $100"), "Hand analysis should be displayed");
    }
    
    @Test
    public void testEmptyHandDisplay() {
        // Test with empty hand
        testPlayer.reInit();  // Clear cards
        
        applet.clearTracking();
        ui.drawPlayer(testPlayer, 0, 0, 1.0f);
        
        assertTrue(applet.anyTextContains(testPlayer.getName()), "Player name should be displayed even with empty hand");
    }
} 