package poker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
public class HandEvaluator {
	ArrayList<Card> deck = null;
	private int pairs, rank;
	private int [] pairRanks;
	private int highRank,tripRank,straightRank,flushRank, quadRank;
	private boolean ranked = false, stringed = false;
	String [] strings = {"high card","pair", "two pair", "triplets", "straight", "flush", "full house", "four of a kind", "straight flush", "royal flush"};
	private int [] rankings;
	private int [] suites;
	private int [][] suiteRankings;
	private String handDesc;
	
	public HandEvaluator(ArrayList<Card> deck)
	{
		this.deck = deck;
		evaluatorInit();
		this.rankings = new int [Card.RANKS];
		this.suites = new int [Card.SUITES];
		this.suiteRankings = new int[Card.SUITES][Card.RANKS];
		calcStat();
	}
	
	public HandEvaluator()
	{
		this.deck = new ArrayList<Card>(7);
	}
	
	public void evaluatorInit()
	{
		pairs = 0;
		rank = -1;
		straightRank = -1;
		flushRank = -1;
		tripRank = -1;
		highRank = -1;
		quadRank = -1;
		ranked = false;
		stringed = false;
		pairRanks = new int[2];
	}
	public void calcStat()
	{
		for(Card c: deck)
		{
			rankings[c.getNum()]++;
			suites[c.getSuite()]++;
			suiteRankings[c.getSuite()][c.getNum()]++;
		}
	}
	
	public void addCard(Card card)
	{
		rankings[card.getNum()]++;
		suites[card.getSuite()]++;
		suiteRankings[card.getSuite()][card.getNum()]++;
		evaluatorInit();
	}
	
	public void removeCard(Card card)
	{
		rankings[card.getNum()]--;
		suites[card.getSuite()]--;
		suiteRankings[card.getSuite()][card.getNum()]--;
		evaluatorInit();
	}
	
	public int getRanking()
	{
		if(deck.size()==0)
		{
			return 0;
		}
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
	
	private boolean isRoyalFlush()
	{
		if(isStraightFlush())
		{
			return(straightRank == 0);
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
		if(pairs >= 1)
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
			if(suites[i] >= 5){
				for(int j = 13; j>0; j--)
				{
					if(suiteRankings[i][j%13]>0)
					{
						flushRank = j%13;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean isStraight()
	{
		int count = 0;
		boolean instraight = false;
		for(int i = 0; i< rankings.length+1; i++)
		{
			if(rankings[i%rankings.length] == 0) // break straight
			{
				instraight = false;
				count = 0;
			}
			else
			{
				if (!instraight)
				{
					instraight = true;
				}
				count ++;
				if( count >= 5)
				{
					straightRank = i%rankings.length;
				}
			}
		}
		return (straightRank!=-1);
	}
	
	private boolean isFourofaKind()
	{
		for(int i = 13; i>0; i--)
		{
			int tmp = i%rankings.length;
			if(rankings[tmp] == 4)
			{
				quadRank = tmp;
			}
			else if(rankings[tmp] == 3 && tripRank == -1)
			{
				tripRank = tmp;
			}
			else if(rankings[tmp] == 2 && pairs<2)
			{
				pairRanks[pairs++] = tmp;
			}
			if(rankings[tmp]>0 && highRank == -1)
			{
				highRank = tmp;
			}
		}
		return quadRank != -1;
	}
	
	private boolean isTriple()
	{
		return tripRank != -1;
	}
	
	private boolean isTwoPair()
	{
		return pairs >= 2;
	}
	
	public String getString()
	{
		if(deck.size() == 0)
		{
			handDesc = "null";
			stringed = true;
		}
		if(!stringed){
			int tmp = getRanking();
			handDesc = strings[tmp];
			switch(tmp)
			{
			case 0:
				handDesc += " of " + Card.RANK_NAME[highRank];
				break;
			case 1:
				handDesc += " of " + Card.RANK_NAME[pairRanks[0]] + "'s";
				break;
			case 2: 
				handDesc += " of " + Card.RANK_NAME[pairRanks[0]]+"'s and " + Card.RANK_NAME[pairRanks[1]] + "'s";
				break;
			case 3:
				handDesc += " of " + Card.RANK_NAME[tripRank] + "'s";
				break;
			case 4:
				handDesc = Card.RANK_NAME[straightRank] + " high " + handDesc;
				break;
			case 5: 
				handDesc = Card.RANK_NAME[flushRank] + " high " + handDesc;
				break;
			case 6:
				handDesc += " with triple " + Card.RANK_NAME[tripRank] + "'s and a pair of " + Card.RANK_NAME[pairRanks[0]] + "'s";
				break;
			case 7:
				handDesc = Card.RANK_NAME[quadRank] + " high " + handDesc;
				break;
			case 8:
				handDesc = Card.RANK_NAME[straightRank] + " high " + handDesc;
				break;
			default:
				break;
			}
		}
		return handDesc;
	}

}
