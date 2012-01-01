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
	ArrayList<Player> players;
	PImage[] deckImage = new PImage[54];
	PImage back;
	HandEvaluator hand = null;
	PFont myfont;
	int numplayers = 3;
	int [] freq = new int[52];
	int maxfreq;
    /* BEGIN:   DO NOT EDIT */
    /* Initialization variables */
	int xoff = 84;
	int yoff = 118;
	/* END:     DO NOT EDIT */
	public Poker()
	{
		super();
	}
	
	public void setup()
	{
		size(1176,473+40);
		populateDeck();
		myfont = createFont("FFScala", 32);
		deal();
	}
	
	public void populateDeck()
	{
		PImage allcards = loadImage("cards.png");

		for(int i = 0; i < 4; i++)
		{
			for(int j = 1; j<14; j++)
			{
				Card newCard = new Card(j,i);
				cards.add(newCard);
                if(j == 1){
				    deckImage[newCard.hashCode()] = allcards.get(0, i*yoff, xoff, yoff);
                }
                else
                {
                    deckImage[newCard.hashCode()] = allcards.get((14-j)*xoff, i*yoff, xoff, yoff);
                }
			}
		}
		back = allcards.get((13)*xoff,(2)*yoff,xoff,yoff);
	}
	
	private void deal()
	{
		Dealer d = new Dealer();
		deck.removeAll(deck); 
		for(int i = 0; i < 7; i++)
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
		}
		fill(255);
		textAlign(CENTER, CENTER);
		textFont(myfont, 20);
		text(hand.getString(),width/2,height-20);
		if(hand.getRanking() < 7)
			deal();
		graphFreq();
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
