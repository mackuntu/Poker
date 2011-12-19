package tests;

import java.util.ArrayList;

import junit.framework.*;

import poker.*;

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
		for(int j = 0; j < 52; j++)
		{
			HandEvaluator hand;
			ArrayList<Card> deck = new ArrayList<Card>(5);
			deck.removeAll(deck);
			for(int i = 0; i < 4; i++)
			{
				int tmp = (i+j)%52;
				deck.add(p.cards.get(tmp));
			}
			deck.add(p.cards.get((4+j+13)%52));
			hand = new HandEvaluator(deck);
			assertTrue(hand.getRanking()== 4);
			if((j+4) % 13 ==0 && j != 0)
			{
				j+= 3;
			}
		}
	}
	
	public void testFullHouse()
	{
		assertTrue(true);
	}
}
