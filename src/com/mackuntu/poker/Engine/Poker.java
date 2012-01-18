package com.mackuntu.poker.engine;

import java.util.ArrayList;

import com.mackuntu.poker.action.Action;
import com.mackuntu.poker.card.Card;
import com.mackuntu.poker.dealer.Dealer;
import com.mackuntu.poker.evaluator.HandEvaluator;
import com.mackuntu.poker.player.Player;


import processing.core.*;


public class Poker extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6687524534858185583L;
	private String[] names = {"Martin", "Weipeng", "Carlos", "Kevin", "Andrew", "Jian", "Gordon", "Yifei"};
	public ArrayList<Card> cards = new ArrayList<Card>(54);
	ArrayList<Card> deck = new ArrayList<Card>(5);
	ArrayList<Card> burn = new ArrayList<Card>(3);
	Player[] players;
	PImage[] deckImage = new PImage[54];
	PImage back;
	HandEvaluator hand = null;
	PFont myfont;
	int numPlayers = 3;
	int [] freq = new int[52];
	int maxfreq;
    /* BEGIN:   DO NOT EDIT */
    /* Initialization variables */
	int xoff = 84;
	int yoff = 118;
	Dealer dealer;
	
	private int pot, raiseAmount, playerIndex, numFolded;
	private int dealerPlayer;
	private enum GameState{
		start, flop, turn, river, finish
	}
	private GameState gameState;
	/* END:     DO NOT EDIT */
	public Poker()
	{
		super();
	}
	
	public void setup()
	{
		//size(1176,473+40);
		size(screen.width,screen.height);
		populateDeck();
		myfont = createFont("FFScala", 32);
		players = new Player[numPlayers];
		for(int i = 0; i < numPlayers; i++){
			players[i] = new Player();
			players[i].setMoney(1000);
			players[i].setName(names[i]);
			players[i].setX((i%4)*4*xoff);
			players[i].setY((i/4)*(height-yoff-20));
		}
		deal();
		fill(255);
		textAlign(CENTER, CENTER);
		textFont(myfont, 20);
		dealerPlayer = 0;
		//imageMode(CENTER);
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
		dealer = new Dealer();
		deck = new ArrayList<Card>(5);
		burn = new ArrayList<Card>(3);
		pot = 0;
		raiseAmount = 0;
		numFolded = 0;
		gameState = GameState.start;
		dealerPlayer=(dealerPlayer+1)%numPlayers;
		playerIndex = dealerPlayer;
		for(int i = 0; i < numPlayers; i++){
			if(players[i].getMoney() == 0)
				continue;
			players[i].reInit();
		}
		for(int i = 0; i < numPlayers*2; i++){
			int tmpIndex = (i+dealerPlayer+1)%numPlayers;
			if(players[tmpIndex].getMoney() == 0)
				continue;
			players[tmpIndex].addCard(cards.get(dealer.getCard()));
		}
		for(int i = 0; i < numPlayers; i++)
		{
			if(players[i].getMoney() == 0)
				continue;
			players[i].initEval();
		}
		
	}
	
	private void dealFlop()
	{
		burn.add(cards.get(dealer.getCard()));
		for(int i = 0; i < 3; i++)
		{
			Card tmp = cards.get(dealer.getCard());
			for(int j = 0; j < numPlayers; j++)
			{
				if(players[j].isFolded())
					continue;
				players[j].addCard(tmp);
			}
			deck.add(tmp);
		}
	}
	
	private void dealTurnOrRiver()
	{
		burn.add(cards.get(dealer.getCard()));
		Card tmp = cards.get(dealer.getCard());
		deck.add(tmp);
		for(int j = 0; j < numPlayers; j++)
		{
			if(players[j].isFolded())
				continue;
			players[j].addCard(tmp);
		}
	}

	public void draw()
	{
		fill(0);
		rect(0,0,width,height);
		for(int i = 0; i < deck.size(); i++)
		{
			image(deckImage[deck.get(i).hashCode()],(int)(width/2-2.5*xoff+i*xoff), height/2);
		}
		for(int i = 0; i < numPlayers; i++)
		{
			ArrayList<Card> playerHand = players[i].getCards();
			if(playerHand == null){
				image(back,players[i].x,players[i].y);
				image(back,players[i].x+xoff,players[i].y);
			}
			else{
				image(deckImage[playerHand.get(0).hashCode()],players[i].x,players[i].y);
				image(deckImage[playerHand.get(1).hashCode()],players[i].x+xoff,players[i].y);
			}
		}
		fill(255);
		textAlign(CORNER,CORNER);
		text("pot = $"+pot, width/2, height-60);
		//text("stack = $"+stack, width/2, height-80);
		switch(gameState)
		{
		case start:
			if(askAll())
			{
				gameState = GameState.flop;
				dealFlop();
				newRound();
			}
			break;
		case flop:
			if(askAll())
			{
				gameState = GameState.turn;
				dealTurnOrRiver();
				newRound();
			}
			break;
		case turn:
			if(askAll())
			{
				gameState = GameState.river;
				dealTurnOrRiver();
				newRound();
			}
			break;
		case river:
			if(askAll())
			{
				gameState = GameState.finish;
				whoWon();
			}
			break;
		case finish:
			deal();
			gameState = GameState.start;
			break;
		}
		
		noLoop();
	}
	
	private void newRound()
	{
		for(int i = 0; i < numPlayers; i++)
		{
			players[i].setReady(false);
		}
		raiseAmount = 0;
	}
	
	private boolean askAll()
	{
		int numReady = 0;
		for(int i = 0; i < numPlayers; i++)
		{
			if(numFolded == numPlayers-1)
			{
				gameState = GameState.finish;
				break;
			}
			if(players[(playerIndex+1)%numPlayers].isReady())
			{
				//text(""+players[i].getAction(), players[i].x,players[i].y+yoff+20);
				playerIndex++;
				numReady++;
			}
			else
			{
				int tmp = (++playerIndex)%numPlayers;
				if(players[tmp].isFolded())
					continue;
				Action playerAct = players[tmp].getAction(raiseAmount);
				switch (playerAct)
				{
				case FOLD:
					players[tmp].setFolded();
					numFolded++;
					numReady++;
					break;
				case CALL:
					if(players[tmp].commit(raiseAmount))
					{
						numReady++;
					}
					break;
				case RAISE:
					if(players[tmp].commit(playerAct.getAmount() + raiseAmount))
					{
						raiseAmount += playerAct.getAmount();
						for(int j = 0; j < numPlayers; j++)
						{
							if(!players[j].isFolded())
								players[j].setReady(false);
						}
						players[tmp].setReady(true);
						numReady = 1;
					}
					break;
				case CHECK:
					if(raiseAmount == players[i].getCommited())
					{
						players[tmp].setReady(true);
						numReady++;
					}
					break;
				}
				text(""+playerAct, players[tmp].x,players[tmp].y+yoff+20);
				text("$"+players[tmp].getMoney(),players[tmp].x,players[tmp].y+yoff+40);
				text("commited = $"+players[tmp].getCommited(),players[tmp].x,players[tmp].y+yoff+60);
				break;
			}
		}
		
		if(numFolded == numPlayers-1)
		{
			gameState = GameState.finish;
			for(int i = 0; i < numPlayers; i++)
			{
				pot += players[i].getCommited();
				players[i].clearCommited();
			}
			whoWon();
			return false;
		}
		if(numReady == numPlayers)
		{
			for(int i = 0; i < numPlayers; i++)
			{
				pot += players[i].getCommited();
				players[i].clearCommited();
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void whoWon()
	{
		int []winner = new int[numPlayers];
		int highRank = -1, numWinner = 0, winnings = 0;
		
		for(int i = 0; i < numPlayers; i++)
		{
			if(players[i].isFolded())
				continue;
			if(players[i].eval() > highRank)
			{
				highRank = players[i].eval();
				numWinner = 0;
				winner[numWinner++] = i;
			}
			else if(players[i].eval() == highRank)
			{
				winner[numWinner++] = i;
			}
		}
		winnings = pot/numWinner;
		
		String winningStr="Winner is Player ";
		for(int i = 0; i < numWinner; i++){
			winningStr += winner[i] + " ";
			players[winner[i]].addMoney(winnings);
		}
		text(winningStr, width/2, height-40);
		text(players[winner[0]].getString(),width/2,height-20);
	}
	
	private void drawCardWall()
	{
		for(int i = 0; i < cards.size();i++)
		{
			Card tmp = cards.get(i);
			image(deckImage[tmp.hashCode()],(i%13)*xoff,i/13*yoff);
			if(!deck.contains(tmp))
			{
				fill(0,120);
				rect((i%13)*xoff,i/13*yoff,xoff,yoff);
			}
		}
		fill(255);
		textAlign(CENTER, CENTER);
		textFont(myfont, 20);
		text(hand.getString(),width/2,height-20);
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
		loop();
	}
	
	public void mouseClicked()
	{
		/*
		int num = (mouseX/xoff) + 13*(mouseY/yoff);
		Card tmp = cards.get(num);
		if(deck.contains(tmp)){
			deck.remove(tmp);
			hand.removeCard(tmp);
		}
		else{
			deck.add(tmp);
			hand.addCard(tmp);
		}
		*/
		/*
		int tmp = hand.getRanking();
		if(tmp>0)
			println("Wow, handraking:" + hand.getRanking()+ " tmp: "+tmp);
		*/
	}

}
