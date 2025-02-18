import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import java.util.List;

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
            TestStrategy strategy = new TestStrategy();
            Action callAction = Action.CALL;
            callAction.setAmount(game.getBigBlind());
            strategy.setForcedAction(callAction);
            players[i] = new Player("Player " + i, strategy);
            players[i].setMoney(1000);
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
            players[i] = new Player("Player " + i, strategy);
            players[i].setMoney(1000);
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
        
        // Set all players to check for the flop round
        for (int i = 0; i < players.length; i++) {
            if (players[i].isActive()) {
                TestStrategy strategy = new TestStrategy();
                strategy.setForcedAction(Action.CHECK);
                players[i] = new Player("Player " + i, strategy);
                players[i].setMoney(1000);
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
        
        // Make all players fold except the last one
        for (int i = 0; i < numPlayers - 1; i++) {
            int playerIndex = (initialPlayer + i) % numPlayers;
            TestStrategy strategy = new TestStrategy();
            strategy.setForcedAction(Action.FOLD);
            players[playerIndex] = new Player("Player " + playerIndex, strategy);
            players[playerIndex].setMoney(1000);
            game.processNextAction();
        }
        
        // Last player should win
        int lastPlayer = (initialPlayer + numPlayers - 1) % numPlayers;
        assertEquals(lastPlayer, game.getCurrentPlayer());
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
        // Set all players to call pre-flop and check post-flop
        for (int i = 0; i < players.length; i++) {
            TestStrategy strategy = new TestStrategy();
            Action callAction = Action.CALL;
            callAction.setAmount(game.getBigBlind());
            strategy.setForcedAction(callAction);
            players[i] = new Player("Player " + i, strategy);
            players[i].setMoney(1000);
        }
        
        // Start a new hand
        game.startNewHand();
        
        // Verify initial state
        assertTrue(game.getPot() > 0, "Pot should contain blinds");
        assertEquals(GameState.START, game.getGameState());
        
        // Run the game until completion
        int maxIterations = 100;  // Prevent infinite loops
        int iterations = 0;
        GameState currentState = game.getGameState();
        
        while (game.getGameState() != GameState.FINISH && iterations < maxIterations) {
            // Process the next action
            boolean actionProcessed = game.processNextAction();
            
            // If the game state changed, update player strategies for the new street
            if (game.getGameState() != currentState) {
                // Update strategies to CHECK for post-flop play
                for (int i = 0; i < players.length; i++) {
                    if (players[i].isActive()) {
                        TestStrategy strategy = new TestStrategy();
                        strategy.setForcedAction(Action.CHECK);
                        players[i].setLastAction(null);  // Reset last action
                    }
                }
                currentState = game.getGameState();
            }
            
            // Verify basic game invariants
            if (game.getGameState() != GameState.FINISH) {
                assertTrue(game.getPot() > 0, "Pot should never be zero during active play");
            }
            assertTrue(game.getCurrentPlayer() >= 0 && game.getCurrentPlayer() < players.length, 
                "Current player should be valid");
            
            iterations++;
        }
        
        // Verify game completed successfully
        assertTrue(iterations < maxIterations, "Game should complete within iteration limit");
        assertEquals(GameState.FINISH, game.getGameState());
        assertEquals(5, game.getCardManager().getCommunityCards().size(), 
            "Should have all community cards at end");
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPotNeverZero() {
        // Start new hand should collect blinds
        game.startNewHand();
        assertTrue(game.getPot() > 0, "Pot should not be zero after blinds are posted");
        assertEquals(INITIAL_SMALL_BLIND + INITIAL_SMALL_BLIND * 2, game.getPot(), 
            "Pot should equal small blind + big blind");
        
        // Make all players fold except one
        for (int i = 0; i < players.length - 1; i++) {
            if (game.getCurrentPlayer() != game.getDealerIndex()) {  // Skip dealer
                TestStrategy strategy = new TestStrategy();
                strategy.setForcedAction(Action.FOLD);
                players[game.getCurrentPlayer()].setLastAction("FOLD");
                game.processNextAction();
            }
        }
        
        // Even after all folds, pot should still have blind money
        assertTrue(game.getPot() > 0, "Pot should not be zero after folds");
        assertEquals(INITIAL_SMALL_BLIND + INITIAL_SMALL_BLIND * 2, game.getPot(), 
            "Pot should still contain blind amounts");
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPotAfterWinnerDetermined() {
        // Start new hand
        game.startNewHand();
        int initialPot = game.getPot();
        assertTrue(initialPot > 0, "Initial pot should not be zero");
        
        // Record dealer's initial money
        int dealerIndex = game.getDealerIndex();
        int dealerInitialMoney = players[dealerIndex].getMoney();
        
        // Track total money committed to pot
        int totalPotSize = initialPot;
        
        // Process actions - have players raise and call
        int maxIterations = players.length * 10; // More than enough iterations
        int iterations = 0;
        
        while (game.getGameState() != GameState.FINISH && iterations < maxIterations) {
            int currentPlayer = game.getCurrentPlayer();
            
            if (currentPlayer == dealerIndex) {
                // Dealer raises
                Action raiseAction = Action.RAISE;
                raiseAction.setAmount(200); // Raise to 200
                TestStrategy dealerStrategy = new TestStrategy();
                dealerStrategy.setForcedAction(raiseAction);
                players[currentPlayer] = new Player(players[currentPlayer].getName(), dealerStrategy);
                players[currentPlayer].setMoney(players[currentPlayer].getMoney());
                totalPotSize += 200 - players[currentPlayer].getCommitted();
            } else {
                // Others call the dealer's raise
                Action callAction = Action.CALL;
                TestStrategy callStrategy = new TestStrategy();
                callStrategy.setForcedAction(callAction);
                players[currentPlayer] = new Player(players[currentPlayer].getName(), callStrategy);
                players[currentPlayer].setMoney(players[currentPlayer].getMoney());
                totalPotSize += 200 - players[currentPlayer].getCommitted();
            }
            
            game.processNextAction();
            iterations++;
        }
        
        // Verify we didn't hit the iteration limit
        assertTrue(iterations < maxIterations, "Game should complete before iteration limit");
        
        // Verify pot size matches total committed money
        assertEquals(totalPotSize, game.getPot(), "Pot should match total committed money");
        
        // Process final action to trigger winner determination
        game.processNextAction();
        
        // Verify dealer won the pot
        assertEquals(dealerInitialMoney + totalPotSize, players[dealerIndex].getMoney(), 
            "Dealer should receive the correct pot amount");
        assertEquals(0, game.getPot(), "Pot should be zero after being awarded");
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPotWithAllInPlayers() {
        // Set one player to only have enough for small blind
        int smallBlindPos = (game.getDealerIndex() + 1) % players.length;
        players[smallBlindPos].setMoney(INITIAL_SMALL_BLIND);
        
        // Start new hand
        game.startNewHand();
        
        // Verify pot contains the all-in amount plus big blind
        assertEquals(INITIAL_SMALL_BLIND + INITIAL_SMALL_BLIND * 2, game.getPot(), 
            "Pot should contain small blind all-in plus big blind");
        assertEquals(0, players[smallBlindPos].getMoney(), 
            "Small blind player should be all-in");
        
        // Verify player is removed from active players after going all-in
        assertFalse(game.isPlayerActive(smallBlindPos), 
            "Player should not be active after going all-in");
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