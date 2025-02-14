/**
 * Test suite for the Dealer and HandEvaluator classes.
 * Tests core functionality of deck management and hand evaluation.
 */
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Dealer.Dealer;
import com.mackuntu.poker.Evaluator.HandEvaluator;
import java.util.ArrayList;

public class DeckTests {
	/** The dealer instance used for testing */
	private Dealer dealer;
	
	/** The hand evaluator instance used for testing */
	private HandEvaluator hand;

	/**
	 * Sets up a fresh dealer and hand evaluator before each test.
	 */
	@BeforeEach
	public void setUp() {
		dealer = new Dealer();
		hand = new HandEvaluator();
	}

	/**
	 * Tests that the dealer is initialized with a full deck of 52 cards.
	 */
	@Test
	public void testDealerInit() {
		assertEquals(52, dealer.getSize());
	}

	/**
	 * Tests that dealing a card reduces the deck size by one.
	 */
	@Test
	public void testDealerDeal() {
		int initialSize = dealer.getSize();
		dealer.getCard();
		assertEquals(initialSize - 1, dealer.getSize());
	}

	/**
	 * Tests hand evaluation with a straight.
	 * Creates a straight (6-7-8-9-10) and verifies it's evaluated correctly.
	 */
	@Test
	public void testHandEvaluation() {
		// Create a new hand evaluator with an empty list
		ArrayList<Card> cards = new ArrayList<>();
		
		// Add cards for a straight
		cards.add(new Card(10, 0));
		cards.add(new Card(9, 1));
		cards.add(new Card(8, 2));
		cards.add(new Card(7, 3));
		cards.add(new Card(6, 0));
		
		// Initialize evaluator with cards
		hand = new HandEvaluator(cards);
		
		assertEquals(4, hand.getRanking(), "Should be a straight (rank 4)");
	}

	/**
	 * Tests that the shuffle operation completes without errors.
	 * Note: This is a basic test that only verifies the operation doesn't throw exceptions.
	 * A more comprehensive test would verify the randomness of the shuffle.
	 */
	@Test
	public void testDealerShuffle() {
		// Just verify no exceptions are thrown during shuffle
		dealer.shuffle();
		assertTrue(true, "Shuffle should complete without errors");
	}
}
