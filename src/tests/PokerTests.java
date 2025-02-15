/**
 * Test suite for the poker game's core functionality.
 * Tests player actions, betting mechanics, and game state progression.
 */
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

import com.mackuntu.poker.Player.Player;
import com.mackuntu.poker.Player.TestStrategy;

public class PokerTests {
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerCommit() {
        Player player = new Player("Test", new TestStrategy());
        player.setMoney(1000);
        
        // Test valid bet
        System.out.println("Before first bet - Money: " + player.getMoney() + ", Committed: " + player.getCommitted());
        assertTrue(player.bet(100));
        System.out.println("After first bet - Money: " + player.getMoney() + ", Committed: " + player.getCommitted());
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommitted());
        assertTrue(player.canAct());
        
        // Test bet with insufficient funds
        System.out.println("Before second bet - Money: " + player.getMoney() + ", Committed: " + player.getCommitted());
        assertFalse(player.bet(1000));
        System.out.println("After second bet - Money: " + player.getMoney() + ", Committed: " + player.getCommitted());
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommitted());
        
        // Test additional bet
        System.out.println("Before third bet - Money: " + player.getMoney() + ", Committed: " + player.getCommitted());
        assertTrue(player.bet(50));
        System.out.println("After third bet - Money: " + player.getMoney() + ", Committed: " + player.getCommitted());
        assertEquals(850, player.getMoney());
        assertEquals(150, player.getCommitted());
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerFold() {
        Player player = new Player("Test", new TestStrategy());
        player.setMoney(1000);
        
        // Test fold
        player.fold();
        assertTrue(player.isFolded());
        assertNotNull(player.getCards());
        assertEquals("FOLD", player.getLastAction());
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerMoneyAdjustment() {
        Player player = new Player("Test", new TestStrategy());
        player.setMoney(1000);
        
        // Test valid deduction
        player.adjustMoney(-20); // small blind
        assertEquals(980, player.getMoney());
        
        // Test invalid deduction
        assertThrows(IllegalArgumentException.class, () -> {
            player.adjustMoney(-1000);
        });
        
        // Test addition
        player.adjustMoney(100);
        assertEquals(1080, player.getMoney());
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerReinitialization() {
        Player player = new Player("Test", new TestStrategy());
        player.setMoney(1000);
        
        // Make some changes to player state
        player.bet(100);
        player.fold();
        
        // Reinitialize
        player.reInit();
        
        // Verify state is reset
        assertEquals(0, player.getCommitted());
        assertFalse(player.isFolded());
        assertNotNull(player.getCards());
        assertNull(player.getLastAction());
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testPlayerBet() {
        Player player = new Player("Test", new TestStrategy());
        player.setMoney(1000);
        
        // Test valid bet
        assertTrue(player.bet(100));
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommitted());
        assertTrue(player.canAct());
        
        // Test bet with insufficient funds
        assertFalse(player.bet(1000));
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommitted());
        
        // Test valid additional bet
        assertTrue(player.bet(50));
        assertEquals(850, player.getMoney());
        assertEquals(150, player.getCommitted());
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testReturnMoney() {
        Player player = new Player("Test", new TestStrategy());
        player.setMoney(1000);
        
        // Make a bet and verify
        assertTrue(player.bet(100));
        assertEquals(900, player.getMoney());
        assertEquals(100, player.getCommitted());
        
        // Return part of the money
        player.returnMoney(50);
        assertEquals(950, player.getMoney());
        assertEquals(50, player.getCommitted());
        
        // Return rest of the money
        player.returnMoney(50);
        assertEquals(1000, player.getMoney());
        assertEquals(0, player.getCommitted());
        
        // Test invalid return
        assertThrows(IllegalArgumentException.class, () -> {
            player.returnMoney(100);
        });
    }
} 