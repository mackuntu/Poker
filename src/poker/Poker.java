package poker;

import java.util.ArrayList;
import processing.core.*;


public class Poker extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6687524534858185583L;
	public ArrayList<Card> cards = new ArrayList<Card>(54);
	ArrayList<Card> deck = new ArrayList<Card>(5);
	ArrayList<int[]> test = new ArrayList<int[]>();
	PImage[] deckImage = new PImage[54];
	PImage back;
	//int[] dealer = new int[54];
	int [] freq = new int[52];
	int maxfreq;
	HandEvaluator hand = null;
	PFont myfont;
	int numplayers = 3;
	ArrayList<Player> players;

	int xoff = 84;
	int yoff = 118;
	int globaloffset = 0;
	
	public Poker()
	{
		super();
	}
	
	public void setup()
	{
		size(1176,473+40);
		populateDeck();
		/*for(int i = 0; i<cards.size()*3; i++)
		{
			int shuf = (int)random(0,cards.size());
			Card tmp = (Card)cards.remove(shuf);
			cards.add(tmp);
		}*/

//		test.add(new int[]{1,2 ,3, 17, 5});
//		test.add(new int[]{0,13 ,26, 39, 5});
//		test.add(new int[]{13,26 ,2, 15, 9});
//		test.add(new int[]{0,13 ,26, 5, 18});
//		test.add(new int[]{0,1 ,2, 3, 4});
//		test.add(new int[]{1,2 ,3, 5, 4});
		test.add(new int[]{12,11 ,10, 9, 0});
//		test.add(new int[]{1,32 ,21, 45, 2});
//		test.add(new int[]{0,12 ,16, 3, 45});
		myfont = createFont("FFScala", 32);
		//testdeal();
		deal();
	}
	
	public void populateDeck()
	{
		PImage allcards = loadImage("cards.png");

		for(int i = 1; i < 5; i++)
		{
			for(int j = 1; j<14; j++)
			{
				Card newCard = new Card(j,i);
				cards.add(newCard);
				deckImage[newCard.hashCode()]=allcards.get((j-1)*xoff,(i-1)*yoff,xoff,yoff);
			}
		}
		back = allcards.get((13)*xoff,(3)*yoff,xoff,yoff);
	}
	
	private void testdeal()
	{
		deck.removeAll(deck); 
		if(globaloffset>=test.size())
		{
			globaloffset = 0;
		}
		for(int j = 0; j < test.get(globaloffset).length; j++)
		{
			deck.add(cards.get(test.get(globaloffset)[j]));
		}

		hand = new HandEvaluator(deck);
		globaloffset++;
	}

	private void deal()
	{
		//println("Dealt");
		Dealer d = new Dealer();
		deck.removeAll(deck); 
		for(int i=0; i < 5; i++)
		{
			int tmp = d.getCard();
			if(++freq[tmp] > maxfreq)
			{
				maxfreq = freq[tmp];
			}
			deck.add(cards.get(tmp));
			if(deck.size()==5)
				break;
		}
		hand = new HandEvaluator(deck);
	}

	public void draw()
	{
		fill(0);
		rect(0,0,width,height);
		
		for(int i = 0; i < deck.size();i++)
		{
			Card tmp = deck.get(i);
			//image(deckImage[tmp.hashCode()],tmp.x,tmp.y);
			//deck.get(i).display(i);
		}
		fill(255);
		textAlign(CENTER, CENTER);
		textFont(myfont, 20);
		text(hand.getString(),width/2,height-20);
		if(hand.getRanking() < 7)
			deal();
		graphFreq();
		
		/*
		for(int i = 0; i<cards.size(); i++)
		{
			//println(cards.get(i));
			cards.get(i).display();

		}
		for(int i = 0; i<cards.size(); i++)
		{
			//println(cards.get(i));
			cards.get(i).display(i);

		}*/
		/*
		testdeal();
		*/

	}
	
	private void graphFreq()
	{
		fill(0,125);
		rect(0,0,1176,473);
		stroke(255);
		strokeWeight(5);
		float tmp; 
		int xtmp;
		for(int i = 0; i < freq.length; i++)
		{
			tmp = ((float)freq[i])/maxfreq * 100;
			xtmp = i*(width-20)/freq.length+20;
			line(xtmp, height/2, xtmp, height/2-tmp);
		}
		strokeWeight(1);
		stroke(0);
	}
	
	public void keyPressed()
	{
		testdeal();
	}
	
	public void mouseClicked()
	{
		deal();
		/*
		int tmp = hand.getRanking();
		if(tmp>0)
			println("Wow, handraking:" + hand.getRanking()+ " tmp: "+tmp);
		*/
	}

}
