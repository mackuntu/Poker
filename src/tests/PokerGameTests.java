import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;

import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.game.GameState;
import com.mackuntu.poker.game.PokerGame;
import com.mackuntu.poker.Player.TestStrategy;
import com.mackuntu.poker.Evaluator.HandEvaluator;
import com.mackuntu.poker.game.CardManager;

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
        
        // Make all players fold except the last one
        for (int i = 0; i < players.length - 1; i++) {
            TestStrategy strategy = (TestStrategy) players[i].getStrategy();
            strategy.setForcedAction(Action.FOLD);
        }
        
        // Process actions until hand completes (should be quick since all but one fold)
        while (game.getGameState() != GameState.FINISH) {
            game.processNextAction();
        }
        
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

    @Test
    public void testCommunityCardDealing() {
        game.startNewHand();
        
        // Initially there should be no community cards
        assertTrue(game.getCardManager().getCommunityCards().isEmpty(), 
            "No community cards at start of hand");
        
        // Set all players to call to progress the game
        for (int i = 0; i < players.length; i++) {
            TestStrategy strategy = (TestStrategy) players[i].getStrategy();
            Action callAction = Action.CALL;
            callAction.setAmount(game.getBigBlind());
            strategy.setForcedAction(callAction);
        }
        
        // Process until flop
        while (game.getGameState() == GameState.START) {
            game.processNextAction();
        }
        
        // Check flop (should be 3 cards)
        assertEquals(3, game.getCardManager().getCommunityCards().size(), 
            "Flop should have 3 cards");
        
        // Process until turn
        while (game.getGameState() == GameState.FLOP) {
            game.processNextAction();
        }
        
        // Check turn (should be 4 cards)
        assertEquals(4, game.getCardManager().getCommunityCards().size(), 
            "Turn should have 4 cards");
        
        // Process until river
        while (game.getGameState() == GameState.TURN) {
            game.processNextAction();
        }
        
        // Check river (should be 5 cards)
        assertEquals(5, game.getCardManager().getCommunityCards().size(), 
            "River should have 5 cards");
        
        // Verify all cards are unique
        List<Card> communityCards = game.getCardManager().getCommunityCards();
        Set<Card> uniqueCards = new HashSet<>(communityCards);
        assertEquals(communityCards.size(), uniqueCards.size(), 
            "All community cards should be unique");
    }

    @Test
    public void testHandEvaluationWithCommunityCards() {
        game.startNewHand();
        CardManager cardManager = game.getCardManager();
        
        // Set up a scenario where community cards make a flush
        Card[] communityFlush = {
            new Card(2, 0),  // 2 of Spades
            new Card(5, 0),  // 5 of Spades
            new Card(7, 0),  // 7 of Spades
            new Card(9, 0),  // 9 of Spades
            new Card(13, 1)  // King of Hearts
        };
        
        // Give player 0 two spades for a flush
        Card[] playerCards = {
            new Card(3, 0),  // 3 of Spades
            new Card(4, 0)   // 4 of Spades
        };
        
        // Force these cards in test mode
        for (Card card : playerCards) {
            players[0].addCard(card);
        }
        
        // Add community cards one by one
        for (Card card : communityFlush) {
            cardManager.getCommunityCards().add(card);
        }
        
        // Evaluate the hand
        ArrayList<Card> allCards = new ArrayList<>();
        allCards.addAll(Arrays.asList(playerCards));
        allCards.addAll(Arrays.asList(communityFlush));
        
        HandEvaluator evaluator = new HandEvaluator(allCards);
        
        // Should be a flush
        assertEquals(5, evaluator.getRanking(), "Should be a flush");
        assertTrue(evaluator.getString().contains("flush"), "Hand should be evaluated as a flush");
    }
} 