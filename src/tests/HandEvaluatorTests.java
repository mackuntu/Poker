import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Evaluator.HandEvaluator;
import java.util.ArrayList;

public class HandEvaluatorTests {
    private HandEvaluator evaluator;
    private ArrayList<Card> cards;

    @BeforeEach
    public void setUp() {
        cards = new ArrayList<>();
    }

    @Test
    public void testHighCard() {
        // Ace high
        cards.add(new Card(14, 0)); // Ace of Spades
        cards.add(new Card(10, 1)); // 10 of Hearts
        cards.add(new Card(8, 2));  // 8 of Diamonds
        cards.add(new Card(6, 3));  // 6 of Clubs
        cards.add(new Card(4, 0));  // 4 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(0, evaluator.getRanking(), "Should be high card");
        assertTrue(evaluator.getString().startsWith("A high"), "Should be Ace high");
    }

    @Test
    public void testOnePair() {
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(10, 1)); // 10 of Hearts
        cards.add(new Card(8, 2));  // 8 of Diamonds
        cards.add(new Card(6, 3));  // 6 of Clubs
        cards.add(new Card(4, 0));  // 4 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(1, evaluator.getRanking(), "Should be one pair");
        assertTrue(evaluator.getString().contains("pair of 10"), "Should be pair of tens");
    }

    @Test
    public void testTwoPair() {
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(10, 1)); // 10 of Hearts
        cards.add(new Card(8, 2));  // 8 of Diamonds
        cards.add(new Card(8, 3));  // 8 of Clubs
        cards.add(new Card(4, 0));  // 4 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(2, evaluator.getRanking(), "Should be two pair");
        assertTrue(evaluator.getString().contains("10s and 8s"), "Should be tens and eights");
    }

    @Test
    public void testThreeOfAKind() {
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(10, 1)); // 10 of Hearts
        cards.add(new Card(10, 2)); // 10 of Diamonds
        cards.add(new Card(8, 3));  // 8 of Clubs
        cards.add(new Card(4, 0));  // 4 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(3, evaluator.getRanking(), "Should be three of a kind");
        assertTrue(evaluator.getString().contains("three 10s"), "Should be three tens");
    }

    @Test
    public void testStraight() {
        // Regular straight
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(9, 1));  // 9 of Hearts
        cards.add(new Card(8, 2));  // 8 of Diamonds
        cards.add(new Card(7, 3));  // 7 of Clubs
        cards.add(new Card(6, 0));  // 6 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(4, evaluator.getRanking(), "Should be a straight");
        assertTrue(evaluator.getString().contains("10 high straight"), "Should be ten-high straight");
    }

    @Test
    public void testAceLowStraight() {
        // A-2-3-4-5 straight
        cards.add(new Card(14, 0)); // Ace of Spades
        cards.add(new Card(2, 1));  // 2 of Hearts
        cards.add(new Card(3, 2));  // 3 of Diamonds
        cards.add(new Card(4, 3));  // 4 of Clubs
        cards.add(new Card(5, 0));  // 5 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(4, evaluator.getRanking(), "Should be a straight");
        assertTrue(evaluator.getString().contains("5 high straight"), "Should be five-high straight");
    }

    @Test
    public void testFlush() {
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(8, 0));  // 8 of Spades
        cards.add(new Card(6, 0));  // 6 of Spades
        cards.add(new Card(4, 0));  // 4 of Spades
        cards.add(new Card(2, 0));  // 2 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(5, evaluator.getRanking(), "Should be a flush");
        assertTrue(evaluator.getString().contains("10 high flush"), "Should be ten-high flush");
    }

    @Test
    public void testFullHouse() {
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(10, 1)); // 10 of Hearts
        cards.add(new Card(10, 2)); // 10 of Diamonds
        cards.add(new Card(4, 3));  // 4 of Clubs
        cards.add(new Card(4, 0));  // 4 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(6, evaluator.getRanking(), "Should be a full house");
        assertTrue(evaluator.getString().contains("full house: 10s full of 4s"), "Should be tens full of fours");
    }

    @Test
    public void testFourOfAKind() {
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(10, 1)); // 10 of Hearts
        cards.add(new Card(10, 2)); // 10 of Diamonds
        cards.add(new Card(10, 3)); // 10 of Clubs
        cards.add(new Card(4, 0));  // 4 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(7, evaluator.getRanking(), "Should be four of a kind");
        assertTrue(evaluator.getString().contains("four 10s"), "Should be four tens");
    }

    @Test
    public void testStraightFlush() {
        // Regular straight flush
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(9, 0));  // 9 of Spades
        cards.add(new Card(8, 0));  // 8 of Spades
        cards.add(new Card(7, 0));  // 7 of Spades
        cards.add(new Card(6, 0));  // 6 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(8, evaluator.getRanking(), "Should be a straight flush");
        assertTrue(evaluator.getString().contains("10 high straight flush"), "Should be ten-high straight flush");
    }

    @Test
    public void testNotAStraightFlush() {
        // Flush and straight present but not a straight flush
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(9, 1));  // 9 of Hearts
        cards.add(new Card(8, 0));  // 8 of Spades
        cards.add(new Card(7, 0));  // 7 of Spades
        cards.add(new Card(6, 0));  // 6 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(4, evaluator.getRanking(), "Should be just a straight, not a straight flush");
        assertTrue(evaluator.getString().contains("10 high straight"), "Should be ten-high straight");
    }

    @Test
    public void testRoyalFlush() {
        cards.add(new Card(14, 0)); // Ace of Spades
        cards.add(new Card(13, 0)); // King of Spades
        cards.add(new Card(12, 0)); // Queen of Spades
        cards.add(new Card(11, 0)); // Jack of Spades
        cards.add(new Card(10, 0)); // 10 of Spades

        evaluator = new HandEvaluator(cards);
        assertEquals(9, evaluator.getRanking(), "Should be a royal flush");
        assertTrue(evaluator.getString().contains("royal flush"), "Should be royal flush");
    }

    @Test
    public void testSevenCardHand() {
        // Test with 7 cards (like in Texas Hold'em)
        cards.add(new Card(14, 0)); // Ace of Spades
        cards.add(new Card(13, 0)); // King of Spades
        cards.add(new Card(12, 0)); // Queen of Spades
        cards.add(new Card(11, 0)); // Jack of Spades
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(2, 1));  // 2 of Hearts
        cards.add(new Card(3, 2));  // 3 of Diamonds

        evaluator = new HandEvaluator(cards);
        assertEquals(9, evaluator.getRanking(), "Should find royal flush in 7 cards");
    }

    @Test
    public void testStraightFlushWithExtraCards() {
        // Test straight flush detection with extra cards
        cards.add(new Card(10, 0)); // 10 of Spades
        cards.add(new Card(9, 0));  // 9 of Spades
        cards.add(new Card(8, 0));  // 8 of Spades
        cards.add(new Card(7, 0));  // 7 of Spades
        cards.add(new Card(6, 0));  // 6 of Spades
        cards.add(new Card(10, 1)); // 10 of Hearts
        cards.add(new Card(10, 2)); // 10 of Diamonds

        evaluator = new HandEvaluator(cards);
        assertEquals(8, evaluator.getRanking(), "Should find straight flush despite extra cards");
    }
} 