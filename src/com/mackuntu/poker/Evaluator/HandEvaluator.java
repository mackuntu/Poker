package com.mackuntu.poker.evaluator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.mackuntu.poker.card.Card;

public class HandEvaluator {
	ArrayList<Card> deck = null;
	private int pairs, bigRank, rankCode, highRankCtr;
	private int [] pairRanks;
	private int tripRank,straightRank,quadRank;
    private int [] flushRank;
    private int [] highRank;
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
		bigRank = 0;
		straightRank = -1;
		tripRank = -1;
		quadRank = -1;
		ranked = false;
		stringed = false;
		rankCode = 0;
		pairRanks = new int[2];
        flushRank = new int[5];
		highRank = new int[7];
        highRankCtr = 0;
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
			return bigRank;
		if(isRoyalFlush())
		{
			ranked = true;
			bigRank = 9;
		}
		else if(isStraightFlush())
		{
			ranked = true;
			bigRank = 8;
            rankCode = straightRank << 16;
		}
		else if(isFourofaKind())
		{
			ranked = true;
			bigRank = 7;
            rankCode = quadRank << 16;
		}
		else if(isFullHouse())
		{
			ranked = true;
			bigRank = 6;
            rankCode = tripRank << 16 | pairRanks[0] << 12;
		}
		else if(isFlush())
		{
			ranked = true;
			bigRank = 5;
            for(int i = 0; i < 5; i ++)
            {
                rankCode |= flushRank[i] << ((4-i)*4);
            }
		}
		else if(isStraight())
		{
			ranked = true;
			bigRank = 4;
            rankCode = straightRank << 16;
		}
		else if(isTriple())
		{
			ranked = true;
			bigRank = 3;
            rankCode = tripRank << 16 | highRank[0] << 12 | highRank[1] << 8;
		}
		else if(isTwoPair())
		{
			ranked = true;
			bigRank = 2;
            rankCode = pairRanks[0] << 16 | pairRanks[1] << 12 | highRank[0] << 8;
		}
		else if(isPair())
		{
			ranked = true;
			bigRank = 1;
            rankCode = pairRanks[0] << 16 | highRank[0] << 12 | highRank[1] << 8 | highRank[2] << 4;
		}
		else
		{
            ranked = true;
            for(int i = 0; i < 5; i++)
            {
                rankCode |= highRank[i] << ((4-i)*4);
            }
		}
        rankCode |= bigRank << 20;
        return bigRank;
	}
	
	private boolean isRoyalFlush()
	{
		if(isStraightFlush())
		{
			return(straightRank == 13);
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
				for(int j = 13, k = 0; j>0 && k < 5; j--)
				{
					if(suiteRankings[i][j%13]>0)
					{
						flushRank[k++] = j;
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
					straightRank = i;
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
				quadRank = i;
			}
			else if(rankings[tmp] == 3 && tripRank == -1)
			{
				tripRank = i;
			}
			else if(rankings[tmp] == 2 && pairs<2)
			{
				pairRanks[pairs++] = i;
			}
			else if(rankings[tmp]>0 && highRankCtr<7)
			{
				highRank[highRankCtr++] = i;
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
	
	public int getRankCode()
	{
		if(!ranked){
			evaluatorInit();
			getRanking();
		}
		return rankCode;
	}
	
	public String getString()
	{
		if(deck.size() == 0)
		{
			handDesc = "null";
			stringed = true;
		}
		if(!stringed){
			handDesc = strings[bigRank];
			switch(bigRank)
			{
			case 0:
				handDesc += " of " + Card.RANK_NAME[highRank[0]];
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
				handDesc = Card.RANK_NAME[flushRank[0]] + " high " + handDesc;
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
