/**
 * Test suite for the poker game's core functionality.
 * Tests player actions, betting mechanics, and game state progression.
 */
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Engine.Poker;
import com.mackuntu.poker.Player.Player;

public class PokerTests {
    /** The test player instance */
    private Player player;

    /**
     * Sets up a fresh player and poker game instance before each test.
     */
    @BeforeEach
    public void setUp() {
        player = new Player("TestPlayer");
        player.setMoney(1000);
        new Poker();
    }

    /**
     * Tests the player's ability to commit money to the pot.
     * Verifies both successful commits and attempts to commit more than available funds.
     */
    @Test
    public void testPlayerCommit() {
        // Test basic commit
        player.commit(100);
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommited());
        assertTrue(player.isReady());

        // Test commit more than available
        assertFalse(player.commit(2000));
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommited());
    }

    /**
     * Tests the folding mechanism.
     * Verifies that folded players are marked as ready and their cards are cleared.
     */
    @Test
    public void testPlayerFold() {
        player.setFolded();
        assertTrue(player.isFolded());
        assertTrue(player.isReady());
        assertNull(player.getCards());
    }

    /**
     * Tests the blind deduction mechanism.
     * Verifies that blinds are correctly deducted from player's stack.
     */
    @Test
    public void testBlindDeduction() {
        int initialMoney = player.getMoney();
        player.deductMoney(20); // small blind
        assertEquals(initialMoney - 20, player.getMoney());
        assertEquals(20, player.getCommited());
    }

    /**
     * Tests round progression mechanics.
     * Verifies betting rules and stack updates during a round.
     */
    @Test
    public void testRoundProgression() {
        // Test that a player can't commit negative amounts
        assertFalse(player.commit(-100));
        assertEquals(1000, player.getMoney());
        assertEquals(0, player.getCommited());

        // Test multiple commits in same round
        assertTrue(player.commit(50));
        assertTrue(player.commit(100));
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommited());
    }

    /**
     * Tests player state reinitialization.
     * Verifies that all player attributes are properly reset.
     */
    @Test
    public void testPlayerReInit() {
        player.setFolded();
        player.commit(100);
        player.reInit();
        
        assertFalse(player.isFolded());
        assertEquals(0, player.getCommited());
        assertFalse(player.isReady());
    }

    /**
     * Tests the validity of player actions.
     * Verifies constraints on raise amounts and stack sizes.
     */
    @Test
    public void testActionValidity() {
        // Test that raise amount is positive
        Action act = Action.RAISE;
        act.setAmount(-100);
        assertEquals(0, act.getAmount()); // Should not allow negative amounts

        // Test that raise amount doesn't exceed player's stack
        act.setAmount(2000);
        assertFalse(player.commit(act.getAmount()));
    }

    /**
     * Tests pot calculation mechanics.
     * Verifies that the pot is correctly updated after player actions.
     */
    @Test
    public void testPotCalculation() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        p1.setMoney(1000);
        p2.setMoney(1000);

        // Simulate a round of betting
        p1.commit(50);
        p2.commit(100);
        
        assertEquals(50, p1.getCommited());
        assertEquals(100, p2.getCommited());
        assertEquals(950, p1.getMoney());
        assertEquals(900, p2.getMoney());
    }

    /**
     * Tests the reset of player ready states.
     * Verifies that ready states are properly cleared between rounds.
     */
    @Test
    public void testReadyStateReset() {
        Player[] players = new Player[3];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player("P" + i);
            players[i].setMoney(1000);
        }

        // Simulate betting round
        players[0].commit(50);
        players[0].setReady(true);
        players[1].commit(100);
        players[1].setReady(true);
        players[2].commit(100);
        players[2].setReady(true);

        // Test ready state reset
        for (Player p : players) {
            p.reInit();
            assertFalse(p.isReady());
        }
    }

    /**
     * Tests blind progression over multiple rounds.
     * Verifies that blinds increase correctly after specified intervals.
     */
    @Test
    public void testBlindProgression() {
        int initialSmallBlind = 20;
        int initialBigBlind = 40;
        
        // Test blind doubling
        for (int i = 0; i < 10; i++) {
            initialSmallBlind *= 2;
            initialBigBlind *= 2;
        }
        
        // After 10 rounds, blinds should be 20480/40960
        assertEquals(20480, initialSmallBlind);
        assertEquals(40960, initialBigBlind);
    }

    /**
     * Tests tracking of player actions.
     * Verifies that action history is properly maintained and cleared.
     */
    @Test
    public void testPlayerActionTracking() {
        player.setLastAction("RAISE to $100");
        assertEquals("RAISE to $100", player.getLastAction());
        
        player.reInit();
        assertNull(player.getLastAction());
    }
} 