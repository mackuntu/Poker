package tests;

import java.util.ArrayList;
import java.util.HashSet;

import com.mackuntu.poker.card.Card;
import com.mackuntu.poker.engine.Poker;
import com.mackuntu.poker.evaluator.HandEvaluator;


import junit.framework.*;

public class DeckTests extends TestCase{
	Poker p;
	public DeckTests(String name)
	{
		super(name);
		p = new Poker();
		p.populateDeck();
	}
	public void testStraight()
	{
		for(int i = 0; i < 13; i++)
		{
			for(int j = 0; j < 4; j++)
			{
	            HandEvaluator hand;
	            ArrayList<Card> deck = new ArrayList<Card>(7);
				deck.removeAll(deck);
				System.out.println("\n==========\nNow testing: ");
				for(int k = 0; k < 7; k++)
				{
					Card tmp = new Card((i+k)%13+1,(j+k)%4);
					System.out.println(tmp);
					deck.add(p.cards.get(tmp.hashCode()));
				}
				hand = new HandEvaluator(deck);
				assertTrue(hand.getRanking() == 4);
			}
		}
	}

	public void testFullHouse()
	{
		assertTrue(true);
	}
}
