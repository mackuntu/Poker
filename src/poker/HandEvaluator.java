package poker;
import java.util.ArrayList;
import java.util.Collections;
public class HandEvaluator {
	ArrayList<Card> stack = null;
	private int size = 5, samenumcount = 1, pairs = 0, rank = 0, straightRank = -1;
	private boolean ranked = false, flush = false, straight = false;
	String [] strings = {"high card","pair", "two pair", "triplets", "straight", "flush", "full house", "four of a kind", "straight flush", "royal flush"};
	private int [] rankings;
	private int [] suites;
	
	public HandEvaluator(ArrayList<Card> stack)
	{
		this.stack = stack;
		//Collections.sort(this.stack);
		size = stack.size();
		samenumcount = 1;
		pairs = 0;
		rank = 0;
		this.rankings = new int [Card.RANKS];
		this.suites = new int [Card.SUITES];
		calcStat();
	}
	
	public HandEvaluator()
	{
		this.stack = new ArrayList<Card>(size);
	}
	
	public void calcStat()
	{
		for(Card c: stack)
		{
			rankings[c.getNum()]++;
			suites[c.getSuite()]++;
		}
	}
	
	public void addCard(Card card)
	{
		if(stack.size()<size)
		{
			stack.add(card);
			size++;
		}
		//Collections.sort(this.stack);
	}
	
	
	
	public int getRanking()
	{
		if(ranked)
			return rank;
		if(isRoyalFlush())
		{
			ranked = true;
			rank = 9;
			return 9;
		}
		else if(isStraightFlush())
		{
			ranked = true;
			rank = 8;
			return 8;
		}
		else if(isFourofaKind())
		{
			ranked = true;
			rank = 7;
			return 7;
		}
		else if(isFullHouse())
		{
			ranked = true;
			rank = 6;
			return 6;
		}
		else if(isFlush())
		{
			ranked = true;
			rank = 5;
			return 5;
		}
		else if(isStraight())
		{
			ranked = true;
			rank = 4;
			return 4;
		}
		else if(isTriple())
		{
			ranked = true;
			rank = 3;
			return 3;
		}
		else if(isTwoPair())
		{
			ranked = true;
			rank = 2;
			return 2;
		}
		else if(isPair())
		{
			ranked = true;
			rank = 1;
			return 1;
		}
		else
		{
			ranked = true;
			rank = 0;
			return 0;
		}
	}
	
	public String getString()
	{
		return strings[getRanking()];
	}
	
	private boolean isRoyalFlush()
	{
		if(isStraightFlush())
		{
			return(straightRank == 14);
		}
		return false;
	}
	
	private boolean isFullHouse()
	{
		if(isTriple() && isPair())
			return true;
		else
			return false;
	}
	
	private boolean isPair()
	{
		if(pairs == 1)
			return true;
		else
			return false;
	}
	
	private boolean isStraightFlush()
	{
		if(isFlush() && isStraight())
			return true;
		else
			return false;
	}
	
	private boolean isFlush()
	{
		for (int i = 0; i < suites.length; i++)
		{
			if(suites[i] == 5)
				flush = true;
		}
		return flush;
	}
	
	private boolean isStraight()
	{
		int count = 0;
		boolean instraight = false;
		for(int i = 0; i< rankings.length; i++)
		{
			if(rankings[i] == 0) // break straight
			{
				instraight = false;
			}
			else
			{
				if (!instraight)
				{
					instraight = true;
					rank = i;
				}
				count ++;
				if( count >= 5)
				{
					straightRank = rank;
				}
			}
		}
		return (straightRank!=-1);
	}
	
	private boolean isFourofaKind()
	{
		countSameNum();
		return samenumcount == 4;
	}
	
	private boolean isTriple()
	{
		return samenumcount == 3;
	}
	
	private boolean isTwoPair()
	{
		return pairs >= 2;
	}
	
	private void countSameNum()
	{
		int count = 1;
		int highcount = count;
		int num = stack.get(0).getNum();
		boolean set = false;
		for(int i = 1; i<stack.size();i++)
		{
			if(stack.get(i).getNum()==num)
			{
				count++;
				set = false;
			}
			else
			{
				num = stack.get(i).getNum();
				if(count >= highcount)
				{
					highcount = count;
					if(highcount == 2 && !set)
					{
						pairs++;
						set = true;
					}
					count = 1;
				}
				
			}
		}
		if(count == 2 && !set)
			pairs++;

		samenumcount = (count>=highcount)? count: highcount;
	}
	

}
