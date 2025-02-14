package com.mackuntu.poker.Engine;

import java.util.ArrayList;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Dealer.Dealer;
import com.mackuntu.poker.Evaluator.HandEvaluator;
import com.mackuntu.poker.Player.Player;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;


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
	int numPlayers = 6;
	int [] freq = new int[52];
	int maxfreq;
    /* BEGIN:   DO NOT EDIT */
    /* Initialization variables */
	int xoff = 84;
	int yoff = 118;
	Dealer dealer;
	
	private int pot, raiseAmount, playerIndex;
	private int dealerPlayer;
	private int smallBlind = 20;  // Starting small blind
    private int bigBlind = 40;    // Starting big blind
	private int roundCount = 0;   // Track number of rounds
	private static final int ROUNDS_TO_INCREASE_BLINDS = 10;  // Increase blinds every 10 rounds
	private enum GameState{
		start, flop, turn, river, finish
	}
	private GameState gameState;
	/* END:     DO NOT EDIT */
	private ArrayList<String> handAnalysis = new ArrayList<>();
	private static final int MAX_ANALYSIS_LINES = 5;  // Number of lines to show in rolling window
	
	public Poker()
	{
		super();
	}
	
	public void settings() {
        size(800, 600);
    }

	public void setup()
	{
		size(800, 600);
		populateDeck();
		myfont = createFont("FFScala", 32);
		players = new Player[numPlayers];
		
		// Calculate positions for 6 players around an oval table
		int centerX = width / 2;
		int centerY = height / 2;
		int tableRadiusX = width / 3;
		int tableRadiusY = height / 3;
		
		for(int i = 0; i < numPlayers; i++){
			players[i] = new Player();
			players[i].setMoney(1000);
			players[i].setName(names[i]);
			
			// Calculate position on oval
			double angle = (i * 2 * Math.PI / numPlayers) - Math.PI/2; // Start from top
			players[i].setX((int)(centerX + tableRadiusX * Math.cos(angle) - xoff));
			players[i].setY((int)(centerY + tableRadiusY * Math.sin(angle) - yoff/2));
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
		PImage allcards = loadImage("data/cards.png");

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
		gameState = GameState.start;
		dealerPlayer=(dealerPlayer+1)%numPlayers;
		playerIndex = dealerPlayer;
		
		// Increment round count and adjust blinds if needed
		roundCount++;
		if (roundCount % ROUNDS_TO_INCREASE_BLINDS == 0) {
			smallBlind *= 2;
			bigBlind *= 2;
			addHandAnalysis("Blinds increased to " + smallBlind + "/" + bigBlind);
		}
		
		// Post blinds
		int smallBlindPlayer = (dealerPlayer + 1) % numPlayers;
		int bigBlindPlayer = (dealerPlayer + 2) % numPlayers;
		
		// Post small blind
		if (players[smallBlindPlayer].getMoney() >= smallBlind) {
			players[smallBlindPlayer].deductMoney(smallBlind);
			pot += smallBlind;
			addHandAnalysis(players[smallBlindPlayer].getName() + " posts small blind $" + smallBlind);
		}
		
		// Post big blind
		if (players[bigBlindPlayer].getMoney() >= bigBlind) {
			players[bigBlindPlayer].deductMoney(bigBlind);
			pot += bigBlind;
			addHandAnalysis(players[bigBlindPlayer].getName() + " posts big blind $" + bigBlind);
		}
		
		// Initialize players
		for(int i = 0; i < numPlayers; i++){
			if(players[i].getMoney() == 0)
				continue;
			players[i].reInit();
		}
		
		// Deal cards
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
		
		// Draw table outline
		fill(34, 139, 34); // Forest green
		ellipse(width/2, height/2, width/1.5f, height/1.5f);
		
		// Draw community cards
		for(int i = 0; i < deck.size(); i++)
		{
			image(deckImage[deck.get(i).hashCode()],
				(int)(width/2 - (deck.size()*xoff)/2 + i*xoff), 
				height/2 - yoff/2,
				xoff * 0.8f,  // Make cards slightly smaller
				yoff * 0.8f);
		}
		
		// Draw pot and blind info
		fill(255);
		textAlign(CENTER, CENTER);
		textSize(20);
		// Calculate current pot including all committed money
		int currentPot = pot;
		for(int i = 0; i < numPlayers; i++) {
			currentPot += players[i].getCommited();
		}
		text("Pot: $" + currentPot, width/2, height/2 + yoff/2);  // Position just below community cards
		textSize(14);
		text("Blinds: $" + smallBlind + "/$" + bigBlind + " (Round " + roundCount + ")", width/2, height/2 + yoff/2 + 25);
		
		// Draw player cards
		for(int i = 0; i < numPlayers; i++)
		{
			ArrayList<Card> playerHand = players[i].getCards();
			float cardScale = 0.8f; // Make player cards slightly smaller
			
			if(playerHand == null){
				image(back, players[i].x, players[i].y, xoff * cardScale, yoff * cardScale);
				image(back, players[i].x + xoff * cardScale, players[i].y, xoff * cardScale, yoff * cardScale);
			}
			else{
				image(deckImage[playerHand.get(0).hashCode()], players[i].x, players[i].y, xoff * cardScale, yoff * cardScale);
				image(deckImage[playerHand.get(1).hashCode()], players[i].x + xoff * cardScale, players[i].y, xoff * cardScale, yoff * cardScale);
			}
			
			// Draw player info
			fill(255);
			textAlign(CENTER, CENTER);
			textSize(14);
			
			// Add blind indicators to player names
			String playerInfo = players[i].getName();
			if (i == (dealerPlayer + 1) % numPlayers) {
				playerInfo += " (SB)";
			} else if (i == (dealerPlayer + 2) % numPlayers) {
				playerInfo += " (BB)";
			} else if (i == dealerPlayer) {
				playerInfo += " (D)";
			}
			
			text(playerInfo, players[i].x + xoff * cardScale, players[i].y - 20);
			
			if (!players[i].isFolded()) {
				// Draw action above stack amount
				if (players[i].getLastAction() != null) {
					text(players[i].getLastAction(), players[i].x + xoff * cardScale, players[i].y + yoff * cardScale + 20);
				}
				
				// Draw stack and bet amount
				String moneyInfo = "$" + players[i].getMoney();
				if (players[i].getCommited() > 0) {
					moneyInfo += " (Bet: $" + players[i].getCommited() + ")";
				}
				text(moneyInfo, players[i].x + xoff * cardScale, players[i].y + yoff * cardScale + 40);
			} else {
				text("FOLD", players[i].x + xoff * cardScale, players[i].y + yoff * cardScale + 20);
			}
		}
		
		// Draw hand analysis window
		drawHandAnalysis();
		
		switch(gameState)
		{
		case start:
			if(askAll())
			{
				gameState = GameState.flop;
				dealFlop();
				newRound();
				addHandAnalysis("Dealing flop");
			}
			break;
		case flop:
			if(askAll())
			{
				gameState = GameState.turn;
				dealTurnOrRiver();
				newRound();
				addHandAnalysis("Dealing turn");
			}
			break;
		case turn:
			if(askAll())
			{
				gameState = GameState.river;
				dealTurnOrRiver();
				newRound();
				addHandAnalysis("Dealing river");
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
			addHandAnalysis("Starting new hand");
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
	
	private void drawHandAnalysis() {
		// Draw background for analysis window
		fill(0, 100);  // Semi-transparent black
		rect(10, height - 100, width - 20, 80);
		
		// Draw analysis text
		fill(255);
		textAlign(LEFT, TOP);
		textSize(14);
		float y = height - 95;
		for (String analysis : handAnalysis) {
			text(analysis, 20, y);
			y += 16;
		}
	}
	
	private void addHandAnalysis(String analysis) {
		handAnalysis.add(0, analysis);  // Add to beginning
		while (handAnalysis.size() > MAX_ANALYSIS_LINES) {
			handAnalysis.remove(handAnalysis.size() - 1);  // Remove oldest
		}
	}
	
	private boolean askAll() {
		int numReady = 0;
		int activePlayers = 0;
		
		// Count active players first
		for(int i = 0; i < numPlayers; i++) {
			if(!players[i].isFolded() && players[i].getMoney() > 0) {
				activePlayers++;
			}
		}
		
		// If only one active player, move to finish
		if(activePlayers <= 1) {
			gameState = GameState.finish;
			for(int i = 0; i < numPlayers; i++) {
				pot += players[i].getCommited();
				players[i].clearCommited();
			}
			return false;
		}
		
		// Process player actions
		for(int i = 0; i < numPlayers; i++) {
			if(players[(playerIndex+1)%numPlayers].isReady()) {
				playerIndex++;
				numReady++;
				continue;
			}
			
			int tmp = (++playerIndex)%numPlayers;
			if(players[tmp].isFolded() || players[tmp].getMoney() == 0) {
				numReady++;
				continue;
			}
			
			Action playerAct = players[tmp].getAction(raiseAmount);
			switch (playerAct) {
			case FOLD:
				players[tmp].setFolded();
					numReady++;
				addHandAnalysis(players[tmp].getName() + " folds");
				break;
			case CALL:
				if(players[tmp].commit(raiseAmount)) {
					numReady++;
					addHandAnalysis(players[tmp].getName() + " calls $" + raiseAmount);
				}
				break;
			case RAISE:
				if(players[tmp].commit(playerAct.getAmount() + raiseAmount)) {
					raiseAmount += playerAct.getAmount();
					for(int j = 0; j < numPlayers; j++) {
						if(!players[j].isFolded() && players[j].getMoney() > 0) {
							players[j].setReady(false);
						}
					}
					players[tmp].setReady(true);
					numReady = 1;
					addHandAnalysis(players[tmp].getName() + " raises to $" + players[tmp].getCommited());
				}
				break;
			case CHECK:
				if(raiseAmount == players[tmp].getCommited()) {
					players[tmp].setReady(true);
					numReady++;
					addHandAnalysis(players[tmp].getName() + " checks");
				}
				break;
			}
		}
		
		// Check if round is complete
		if(numReady >= activePlayers) {
			for(int i = 0; i < numPlayers; i++) {
				pot += players[i].getCommited();
				players[i].clearCommited();
			}
			return true;
		}
		
		return false;
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
