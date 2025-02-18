/**
 * Represents a playing card in a standard poker deck.
 * Uses enums to represent ranks and suits for better type safety and readability.
 */
package com.mackuntu.poker.Card;

public class Card
{
	public enum Rank {
		TWO(2, "2"),
		THREE(3, "3"),
		FOUR(4, "4"),
		FIVE(5, "5"),
		SIX(6, "6"),
		SEVEN(7, "7"),
		EIGHT(8, "8"),
		NINE(9, "9"),
		TEN(10, "10"),
		JACK(11, "J"),
		QUEEN(12, "Q"),
		KING(13, "K"),
		ACE(14, "A");  // Ace is high by default (14), can be treated as 1 in straights

		private final int value;
		private final String symbol;

		Rank(int value, String symbol) {
			this.value = value;
			this.symbol = symbol;
		}

		public int getValue() { return value; }
		public String getSymbol() { return symbol; }

		public static Rank fromValue(int value) {
			for (Rank rank : values()) {
				if (rank.value == value) return rank;
			}
			throw new IllegalArgumentException("Invalid rank value: " + value);
		}
	}

	public enum Suit {
		SPADES(0, "♠"),
		HEARTS(1, "♥"),
		DIAMONDS(2, "♦"),
		CLUBS(3, "♣");

		private final int value;
		private final String symbol;

		Suit(int value, String symbol) {
			this.value = value;
			this.symbol = symbol;
		}

		public int getValue() { return value; }
		public String getSymbol() { return symbol; }

		public static Suit fromValue(int value) {
			for (Suit suit : values()) {
				if (suit.value == value) return suit;
			}
			throw new IllegalArgumentException("Invalid suit value: " + value);
		}
	}

	private final Rank rank;
	private final Suit suit;
	
	/**
	 * Creates a new card with the specified rank and suit.
	 * @param rank The rank value (2-14, where 14 is Ace)
	 * @param suit The suit value (0-3)
	 */
	public Card(int rank, int suit)
	{
		this.rank = Rank.fromValue(rank);
		this.suit = Suit.fromValue(suit);
	}

	/**
	 * Creates a new card with the specified rank and suit enums.
	 * @param rank The rank enum value
	 * @param suit The suit enum value
	 */
	public Card(Rank rank, Suit suit)
	{
		this.rank = rank;
		this.suit = suit;
	}

	/**
	 * Gets the card's rank value.
	 * @return The rank value (2-14)
	 */
	public int getNum()
	{
		return rank.getValue();
	}

	/**
	 * Gets the card's suit value.
	 * @return The suit value (0-3)
	 */
	public int getSuite()
	{
		return suit.getValue();
	}

	/**
	 * Gets the card's rank enum.
	 * @return The Rank enum value
	 */
	public Rank getRank()
	{
		return rank;
	}

	/**
	 * Gets the card's suit enum.
	 * @return The Suit enum value
	 */
	public Suit getSuit()
	{
		return suit;
	}

	/**
	 * Generates a unique hash code for this card.
	 * @return A unique integer representing this card
	 */
	@Override
	public int hashCode()
	{
		return suit.getValue() * 13 + (rank.getValue() - 2);
	}

	/**
	 * Checks if two cards are equal.
	 * @param obj The object to compare with
	 * @return true if the cards have the same rank and suit
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof Card other)) return false;
		return this.rank == other.rank && this.suit == other.suit;
	}

	/**
	 * Returns a string representation of the card.
	 * @return A string in the format "Rank♠" (e.g., "A♠" for Ace of Spades)
	 */
	@Override
	public String toString()
	{
		return rank.getSymbol() + suit.getSymbol();
	}

	/**
	 * Returns a verbose string representation of the card.
	 * @return A string in the format "Rank of Suit" (e.g., "Ace of Spades")
	 */
	public String toLongString()
	{
		return String.format("%s of %s", 
			rank.getSymbol(), 
			suit.name().charAt(0) + suit.name().substring(1).toLowerCase());
	}

	// Static utility methods
	public static final int TOTAL_RANKS = Rank.values().length;  // 13
	public static final int TOTAL_SUITS = Suit.values().length;  // 4
}
