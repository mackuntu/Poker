package com.mackuntu.poker.Dealer;

import java.util.ArrayList;
import java.util.Random;

public class Dealer {
	ArrayList<Integer> dealer;
	public Dealer()
	{
		this.dealer = new ArrayList<Integer>(52);
		for(int i = 0; i < 52; i++)
		{
			dealer.add(i);
		}
	}
	
	public int getCard()
	{
		Random r = new Random();
		int card = r.nextInt(dealer.size());
		return dealer.remove(card);
	}

}
