/**
 * Represents the dealer in a poker game.
 * Manages the deck of cards and handles card distribution.
 */
package com.mackuntu.poker.Dealer;

import java.util.ArrayList;
import java.util.Random;

public class Dealer {
	/** The deck of cards represented as indices (0-51) */
	ArrayList<Integer> dealer;

	/**
	 * Creates a new dealer with a fresh deck of 52 cards.
	 * Cards are represented as integers from 0 to 51.
	 */
	public Dealer()
	{
		this.dealer = new ArrayList<Integer>(52);
		for(int i = 0; i < 52; i++)
		{
			dealer.add(i);
		}
	}
	
	/**
	 * Deals a single card from the deck.
	 * Randomly selects a card from the remaining cards.
	 * @return The index of the dealt card (0-51)
	 */
	public int getCard()
	{
		Random r = new Random();
		int card = r.nextInt(dealer.size());
		return dealer.remove(card);
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
		Random r = new Random();
		for (int i = dealer.size() - 1; i > 0; i--) {
			int j = r.nextInt(i + 1);
			// Swap elements
			int temp = dealer.get(i);
			dealer.set(i, dealer.get(j));
			dealer.set(j, temp);
		}
	}
}
