package poker;

public class Card implements Comparable<Card>
{
	public static final int RANKS = 14;
	
	public static final int SUITES = 4;
	
	public static final String [] RANK_NAME = {
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
	
	private final int num, suite;
	
	public Card(int num, int suite)
	{
		this.num = 14-num;
		this.suite = suite-1;

		
	}

	public int hashCode(){
		return suite*13+num;
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

	public int compareTo(Card another) {
		// TODO Auto-generated method stub
		return this.hashCode() - another.hashCode();
	}
}