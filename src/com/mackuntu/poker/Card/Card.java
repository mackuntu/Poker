/**
 * Represents a playing card in a standard poker deck.
 * Each card has a rank (2-10, J, Q, K, A) and a suite (Spades, Hearts, Diamonds, Clubs).
 */
package com.mackuntu.poker.Card;

public class Card
{
	/** Total number of ranks in a standard deck */
	public static final int RANKS = 13;
	
	/** Total number of suites in a standard deck */
	public static final int SUITES = 4;
	
	/** String representations of card ranks */
	public static final String [] RANK_NAME = {
		"A",  // Ace can be high or low
        "2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
		"9",
		"10",
		"J",
		"Q",
		"K",
        "A"   // Duplicate for high ace
	};
	
	/** String representations of card suites */
	public static final String [] SUITE_NAME = {
		"Spade",
		"Heart",
		"Diamond",
		"Club"
	};
	
	/** The card's rank (0-12), suite (0-3), and location in the deck */
	private final int num, suite, loc;
	
	/**
	 * Creates a new card with the specified rank and suite.
	 * @param num The rank of the card (1-13, where 1 is Ace)
	 * @param suite The suite of the card (0-3)
	 */
	public Card(int num, int suite)
	{
		this.num = num-1;
		this.suite = suite;
		this.loc = 0;
	}

	/**
	 * Generates a unique hash code for this card.
	 * @return A unique integer representing this card
	 */
	public int hashCode(){
		return suite*13+num;
	}

	/**
	 * Gets the card's location in the deck.
	 * @return The card's position
	 */
	public int getLoc() {
		return loc;
	}

	/**
	 * Gets the card's rank.
	 * @return The card's rank (0-12)
	 */
	public int getNum() {
		return num;
	}

	/**
	 * Gets the card's suite.
	 * @return The card's suite (0-3)
	 */
	public int getSuite() {
		return suite;
	}

	/**
	 * Returns a string representation of the card.
	 * @return A string in the format "Rank of Suite"
	 */
	public String toString()
	{
		return Card.RANK_NAME[num]+" of " + Card.SUITE_NAME[suite];
	}
}
