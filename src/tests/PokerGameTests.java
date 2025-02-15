import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.game.GameState;
import com.mackuntu.poker.game.PokerGame;
import com.mackuntu.poker.Player.TestStrategy;

public class PokerGameTests {
    private PokerGame game;
    private static final int NUM_PLAYERS = 6;
    private static final int INITIAL_SMALL_BLIND = 20;
    private Player[] players;

    @BeforeEach
    public void setUp() {
        // Create test players
        players = new Player[NUM_PLAYERS];
        for (int i = 0; i < players.length; i++) {
            TestStrategy strategy = new TestStrategy();
            players[i] = new Player("Player " + i, strategy);
            players[i].setMoney(1000);
        }

        // Initialize game with test mode
        game = new PokerGame(players, true);
        
        // Start the first hand
        game.startNewHand();
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerIndicatorPersistence() {
        int initialPlayer = game.getCurrentPlayer();
        int numPlayers = players.length;
        
        // Set all players to call
        for (int i = 0; i < numPlayers; i++) {
            int playerPos = (initialPlayer + i) % numPlayers;
            TestStrategy strategy = new TestStrategy();
            Action callAction = Action.CALL;
            callAction.setAmount(game.getBigBlind());
            strategy.setForcedAction(callAction);
            Player oldPlayer = players[playerPos];
            Player newPlayer = new Player(oldPlayer.getName(), strategy);
            newPlayer.setMoney(oldPlayer.getMoney());
            for (Card card : oldPlayer.getCards()) {
                newPlayer.addCard(card);
            }
            players[playerPos] = newPlayer;
        }
        
        // Process actions for all players except the last one
        for (int i = 0; i < numPlayers - 1; i++) {
            int expectedPlayer = (initialPlayer + i) % numPlayers;
            assertEquals(expectedPlayer, game.getCurrentPlayer(), 
                "Player indicator should be at position " + expectedPlayer);
            game.processNextAction();
        }
        
        // Process last player's action - this should complete the round
        boolean roundComplete = game.processNextAction();
        assertTrue(roundComplete, "Round should be complete after all players act");
        
        // After round completion, we should be in FLOP state with small blind as first to act
        assertEquals(GameState.FLOP, game.getGameState(), 
            "Game should move to FLOP after pre-flop round");
        int expectedFirstPlayer = (game.getDealerIndex() + 1) % numPlayers;  // Small blind position
        assertEquals(expectedFirstPlayer, game.getCurrentPlayer(),
            "First player in flop should be small blind (1 after dealer)");
        assertEquals(3, game.getCardManager().getCommunityCards().size(),
            "Flop should have 3 community cards");
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    public void testPlayerIndicatorAfterRoundTransition() {
        int numPlayers = players.length;
        
        // Set all players to call the big blind
        for (int i = 0; i < players.length; i++) {
            TestStrategy strategy = new TestStrategy();
            Action callAction = Action.CALL;
            callAction.setAmount(game.getBigBlind());
            strategy.setForcedAction(callAction);
            Player oldPlayer = players[i];
            Player newPlayer = new Player(oldPlayer.getName(), strategy);
            newPlayer.setMoney(oldPlayer.getMoney());
            for (Card card : oldPlayer.getCards()) {
                newPlayer.addCard(card);
            }
            players[i] = newPlayer;
        }
        
        // Complete pre-flop round
        int maxIterations = 100;  // Prevent infinite loops
        int iterations = 0;
        while (!game.processNextAction() && iterations < maxIterations) {
            iterations++;
        }
        assertTrue(iterations < maxIterations, "Game stuck in pre-flop round");
        
        // Verify we're in flop state
        assertEquals(GameState.FLOP, game.getGameState());
        
        // Verify indicator is still visible and at correct position
        assertTrue(game.getCurrentPlayer() >= 0);
        assertTrue(game.getCurrentPlayer() < numPlayers);
        
        // Set all players to call again for the flop round
        for (int i = 0; i < players.length; i++) {
            if (players[i].isActive()) {
                TestStrategy strategy = new TestStrategy();
                Action callAction = Action.CALL;
                strategy.setForcedAction(callAction);
                Player oldPlayer = players[i];
                Player newPlayer = new Player(oldPlayer.getName(), strategy);
                newPlayer.setMoney(oldPlayer.getMoney());
                for (Card card : oldPlayer.getCards()) {
                    newPlayer.addCard(card);
                }
                players[i] = newPlayer;
            }
        }
        
        // Complete flop round
        iterations = 0;
        while (!game.processNextAction() && iterations < maxIterations) {
            iterations++;
        }
        assertTrue(iterations < maxIterations, "Game stuck in flop round");
        
        // Verify we're in turn state
        assertEquals(GameState.TURN, game.getGameState());
        
        // Verify indicator is still visible and at correct position
        assertTrue(game.getCurrentPlayer() >= 0);
        assertTrue(game.getCurrentPlayer() < numPlayers);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerIndicatorWithAllFolds() {
        int numPlayers = players.length;
        int initialPlayer = game.getCurrentPlayer();
        int lastActivePlayer = -1;
        
        // Find all active players first
        int[] activePlayers = new int[numPlayers];
        int activeCount = 0;
        for (int i = 0; i < numPlayers; i++) {
            int playerIndex = (initialPlayer + i) % numPlayers;
            if (!players[playerIndex].isFolded()) {
                activePlayers[activeCount++] = playerIndex;
            }
        }
        
        // Last active player will be the last one in our list
        lastActivePlayer = activePlayers[activeCount - 1];
        
        // Make all players fold except the last one
        for (int i = 0; i < activeCount - 1; i++) {
            TestStrategy strategy = new TestStrategy();
            strategy.setForcedAction(Action.FOLD);
            Player oldPlayer = players[activePlayers[i]];
            Player newPlayer = new Player(oldPlayer.getName(), strategy);
            newPlayer.setMoney(oldPlayer.getMoney());
            for (Card card : oldPlayer.getCards()) {
                newPlayer.addCard(card);
            }
            players[activePlayers[i]] = newPlayer;
            game.processNextAction();
        }
        
        // Verify last player is still indicated
        assertEquals(lastActivePlayer, game.getCurrentPlayer());
        assertEquals(GameState.FINISH, game.getGameState());
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testInitialGameState() {
        assertEquals(GameState.START, game.getGameState());
        assertEquals(INITIAL_SMALL_BLIND, game.getSmallBlind());
        assertEquals(INITIAL_SMALL_BLIND * 2, game.getBigBlind());
        assertEquals(0, game.getCardManager().getCommunityCards().size());
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerTurnProgression() {
        int numPlayers = players.length;
        
        // First active player should be after big blind (dealer + 3)
        int dealerPos = game.getDealerIndex();
        int expectedFirstPlayer = (dealerPos + 3) % numPlayers;  // After big blind
        assertEquals(expectedFirstPlayer, game.getCurrentPlayer(), 
            "First player should be 3 positions after dealer");

        // Simulate a player action and verify next player
        game.processNextAction();
        assertEquals((expectedFirstPlayer + 1) % numPlayers, game.getCurrentPlayer(), 
            "Next player should be 1 position after first player");
        
        // Verify we can make a full round
        for (int i = 2; i < numPlayers; i++) {
            game.processNextAction();
            int expectedPlayer = (expectedFirstPlayer + i) % numPlayers;
            assertEquals(expectedPlayer, game.getCurrentPlayer(), 
                "Player " + i + " should be " + i + " positions after first player");
        }
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    public void testNewBettingRound() {
        int numPlayers = players.length;
        
        // Set all players to call the big blind
        for (int i = 0; i < players.length; i++) {
            TestStrategy strategy = new TestStrategy();
            Action callAction = Action.CALL;
            callAction.setAmount(game.getBigBlind());
            strategy.setForcedAction(callAction);
            Player oldPlayer = players[i];
            Player newPlayer = new Player(oldPlayer.getName(), strategy);
            newPlayer.setMoney(oldPlayer.getMoney());
            for (Card card : oldPlayer.getCards()) {
                newPlayer.addCard(card);
            }
            players[i] = newPlayer;
        }
        
        // Complete the pre-flop round
        int maxIterations = 100;  // Prevent infinite loops
        int iterations = 0;
        while (!game.processNextAction() && iterations < maxIterations) {
            iterations++;
        }
        assertTrue(iterations < maxIterations, "Game stuck in pre-flop round");

        // Verify we're in FLOP state with small blind as first to act
        assertEquals(GameState.FLOP, game.getGameState(), 
            "Game should move to FLOP after pre-flop round");
        int dealerPos = game.getDealerIndex();
        int expectedFirstPlayer = (dealerPos + 1) % numPlayers;  // Small blind position
        assertEquals(expectedFirstPlayer, game.getCurrentPlayer(),
            "First player after flop should be small blind (1 after dealer)");
        assertEquals(3, game.getCardManager().getCommunityCards().size(), "Flop should have 3 cards");
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testSkipFoldedPlayers() {
        int numPlayers = players.length;
        
        // Get the current player
        int initialPlayer = game.getCurrentPlayer();
        
        // Force next player to fold
        TestStrategy checkStrategy = new TestStrategy();
        checkStrategy.setForcedAction(Action.CHECK);
        Player oldPlayer = players[initialPlayer];
        Player newPlayer = new Player(oldPlayer.getName(), checkStrategy);
        newPlayer.setMoney(oldPlayer.getMoney());
        for (Card card : oldPlayer.getCards()) {
            newPlayer.addCard(card);
        }
        players[initialPlayer] = newPlayer;
        game.processNextAction();  // Current player checks
        
        TestStrategy foldStrategy = new TestStrategy();
        foldStrategy.setForcedAction(Action.FOLD);
        oldPlayer = players[(initialPlayer + 1) % numPlayers];
        newPlayer = new Player(oldPlayer.getName(), foldStrategy);
        newPlayer.setMoney(oldPlayer.getMoney());
        for (Card card : oldPlayer.getCards()) {
            newPlayer.addCard(card);
        }
        players[(initialPlayer + 1) % numPlayers] = newPlayer;
        game.processNextAction();  // Next player folds
        
        // Verify we skip the folded player and move to the next active one
        assertTrue(game.getCurrentPlayer() >= 0, "Current player should be valid");
        assertTrue(game.getCurrentPlayer() < numPlayers, "Current player should be within bounds");
        assertFalse(players[game.getCurrentPlayer()].isFolded(), "Current player should not be folded");
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    public void testCompleteGame() {
        // Set all players to call
        for (int i = 0; i < players.length; i++) {
            TestStrategy strategy = new TestStrategy();
            Action callAction = Action.CALL;
            strategy.setForcedAction(callAction);
            Player oldPlayer = players[i];
            Player newPlayer = new Player(oldPlayer.getName(), strategy);
            newPlayer.setMoney(oldPlayer.getMoney());
            for (Card card : oldPlayer.getCards()) {
                newPlayer.addCard(card);
            }
            players[i] = newPlayer;
        }
        
        int maxIterations = 100;  // Prevent infinite loops
        int iterations = 0;
        
        // Complete pre-flop round
        while (!game.processNextAction() && iterations < maxIterations) {
            iterations++;
        }
        assertTrue(iterations < maxIterations, "Game stuck in pre-flop round");
        
        // Complete flop round
        iterations = 0;
        while (!game.processNextAction() && iterations < maxIterations) {
            iterations++;
        }
        assertTrue(iterations < maxIterations, "Game stuck in flop round");
        
        // Complete turn round
        iterations = 0;
        while (!game.processNextAction() && iterations < maxIterations) {
            iterations++;
        }
        assertTrue(iterations < maxIterations, "Game stuck in turn round");
        
        // Complete river round
        iterations = 0;
        while (!game.processNextAction() && iterations < maxIterations) {
            iterations++;
        }
        assertTrue(iterations < maxIterations, "Game stuck in river round");
        
        // Verify final state
        assertEquals(GameState.FINISH, game.getGameState());
        assertEquals(5, game.getCardManager().getCommunityCards().size());
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testBlinds() {
        int numPlayers = players.length;
        int dealerPos = game.getDealerIndex();
        
        // Calculate blind positions
        int smallBlindPos = (dealerPos + 1) % numPlayers;
        int bigBlindPos = (dealerPos + 2) % numPlayers;
        
        // Verify blind amounts
        assertEquals(INITIAL_SMALL_BLIND, players[smallBlindPos].getCommitted(), 
            "Small blind should be posted");
        assertEquals(INITIAL_SMALL_BLIND * 2, players[bigBlindPos].getCommitted(), 
            "Big blind should be posted");
        
        // Verify other players haven't posted blinds
        for (int i = 0; i < numPlayers; i++) {
            if (i != smallBlindPos && i != bigBlindPos) {
                assertEquals(0, players[i].getCommitted(), 
                    "Player " + i + " should not have posted any blinds");
            }
        }
    }
} 