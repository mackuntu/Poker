/**
 * Represents the dealer in a poker game.
 * Manages the deck of cards and handles card distribution.
 */
package com.mackuntu.poker.Dealer;

import java.util.ArrayList;
import java.util.Random;

public class Dealer {
	/** The deck of cards represented as indices (0-51) */
	private final ArrayList<Integer> dealer;
	private final Random random;

	/**
	 * Creates a new dealer with a fresh deck of 52 cards.
	 * Cards are represented as integers from 0 to 51.
	 */
	public Dealer() {
		this(false);
	}

	/**
	 * Creates a new dealer with a fresh deck of 52 cards.
	 * @param testMode If true, uses a fixed seed for random number generation
	 */
	public Dealer(boolean testMode) {
		this.dealer = new ArrayList<>(52);
		this.random = testMode ? new Random(42) : new Random();
		
		for(int i = 0; i < 52; i++) {
			dealer.add(i);
		}
		
		if (testMode) {
			shuffle(); // Initial shuffle with fixed seed
		}
	}
	
	/**
	 * Deals a single card from the deck.
	 * Randomly selects a card from the remaining cards.
	 * @return The index of the dealt card (0-51)
	 */
	public int getCard() {
		if (dealer.isEmpty()) {
			throw new IllegalStateException("No cards left in deck");
		}
		int card = random.nextInt(dealer.size());
		int cardValue = dealer.remove(card);
		if (cardValue < 0 || cardValue > 51) {
			throw new IllegalStateException("Invalid card value: " + cardValue);
		}
		return cardValue;
	}

	/**
	 * Gets the number of cards remaining in the deck.
	 * @return The current size of the deck
	 */
	public int getSize() {
		return dealer.size();
	}

	/**
	 * Shuffles the remaining cards in the deck.
	 * Uses the Fisher-Yates shuffle algorithm.
	 */
	public void shuffle() {
		for (int i = dealer.size() - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			// Swap elements
			int temp = dealer.get(i);
			dealer.set(i, dealer.get(j));
			dealer.set(j, temp);
		}
	}
}
