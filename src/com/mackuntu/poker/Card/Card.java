package com.mackuntu.poker.card;

public class Card
{
	public static final int RANKS = 13;
	
	public static final int SUITES = 4;
	
	public static final String [] RANK_NAME = {
		"A",
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
        "A"
	};
	
	public static final String [] SUITE_NAME = {
		"Spade",
		"Heart",
		"Diamond",
		"Club"
	};
	
	private final int num, suite, loc;
	
	public Card(int num, int suite)
	{
		this.num = num-1;
		this.suite = suite;
		this.loc = 0;
	}

	public int hashCode(){
		return suite*13+num;
	}
	public int getLoc() {
		return loc;
	}

	public int getNum() {
		return num;
	}

	public int getSuite() {
		return suite;
	}

	public String toString()
	{
		return Card.RANK_NAME[num]+" of " + Card.SUITE_NAME[suite];
	}
}
