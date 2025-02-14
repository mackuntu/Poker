/**
 * Represents a player in the poker game.
 * Manages player state, cards, money, and decision-making logic.
 */
package com.mackuntu.poker.Player;

import java.util.ArrayList;
import java.util.Random;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Card.Card;
import com.mackuntu.poker.Evaluator.HandEvaluator;

public class Player {
	/** Player's name */
	private String name;
	
	/** Player's current money/chips */
	private int money;
	
	/** Player's current hand of cards */
	private ArrayList<Card> cards;
	
	/** Evaluator for the player's hand */
	private HandEvaluator eval;
	
	/** Whether the player has acted in the current round */
	private boolean ready;
	
	/** Whether the player has folded their hand */
	private boolean folded;
	
	/** Amount of money committed to the current pot */
	private int commited;
	
	/** The last action taken by the player */
	private String lastAction;
	
	/** Player's position on the table (x,y coordinates) */
	public int x, y;
	
	/**
	 * Creates a new player with the specified name.
	 * @param name The player's name
	 */
	public Player(String name)
	{
		this.name = name;
		this.money = 0;
		this.cards = new ArrayList<>();
		this.eval = new HandEvaluator();
		this.ready = false;
		this.folded = false;
		this.commited = 0;
	}

	/**
	 * Creates a new player with default values.
	 */
	public Player()
	{
		super();
	}

	/**
	 * Determines the player's action based on current game state.
	 * Uses a combination of hand strength, position, and pot odds to make decisions.
	 * @param raiseAmount The current bet amount to match
	 * @return The chosen action (FOLD, CALL, RAISE, or CHECK)
	 */
	public Action getAction(int raiseAmount)
	{
		Random r = new Random();
		Action newAct;
		
		// Get hand strength (0-9)
		int handStrength = eval.getRanking();
		
		// Calculate pot odds and implied odds
		double potOdds = (double)raiseAmount / (commited + raiseAmount);
		
		// Adjust hand strength based on game stage
		if (cards.size() <= 2) {  // Pre-flop
			// Premium starting hands get a boost
			if (isPremiumStartingHand()) {
				handStrength += 2;
			}
			// Increase aggression in late position
			if (isLatePosition()) {
				handStrength += 1;
			}
		}
		
		// Calculate betting amount based on pot size and hand strength
		int maxBet = calculateMaxBet(handStrength);
		
		// Bluffing logic - more likely to bluff with weak hands in late position
		boolean shouldBluff = (r.nextDouble() < getBluffProbability());
		
		if (shouldBluff) {
			if (isLatePosition() && raiseAmount <= money/4) {
				newAct = Action.RAISE;
				newAct.setAmount(Math.min(money/3, maxBet));
			} else if (raiseAmount == commited) {
				newAct = Action.CHECK;
			} else if (raiseAmount <= money/5) {
				newAct = Action.CALL;
			} else {
				newAct = Action.FOLD;
			}
		} else {
			// Normal play based on hand strength
			if (handStrength >= 7) {  // Very strong hands (four of a kind or better)
				newAct = Action.RAISE;
				newAct.setAmount(Math.min(money, maxBet));
			}
			else if (handStrength >= 5) {  // Strong hands (flush or better)
				if (raiseAmount > money/2) {
					newAct = Action.CALL;
				} else {
					newAct = Action.RAISE;
					newAct.setAmount(Math.min(money/2, maxBet));
				}
			}
			else if (handStrength >= 3) {  // Medium hands (three of a kind or better)
				if (raiseAmount > money/3) {
					newAct = Action.FOLD;
				} else if (potOdds < 0.2) {
					newAct = Action.CALL;
				} else {
					newAct = Action.RAISE;
					newAct.setAmount(Math.min(money/4, maxBet));
				}
			}
			else if (handStrength >= 1) {  // Weak hands (pair)
				if (raiseAmount > money/4) {
					newAct = Action.FOLD;
				} else if (raiseAmount == commited) {
					newAct = Action.CHECK;
				} else if (potOdds < 0.15) {
					newAct = Action.CALL;
				} else {
					newAct = Action.FOLD;
				}
			}
			else {  // Very weak hands
				if (raiseAmount == commited) {
					newAct = Action.CHECK;
				} else if (potOdds < 0.1 && isLatePosition()) {
					newAct = Action.CALL;
				} else {
					newAct = Action.FOLD;
				}
			}
		}
		
		return newAct;
	}
	
	/**
	 * Checks if the player has a premium starting hand.
	 * Premium hands include high pairs and high suited cards.
	 * @return true if the hand is premium
	 */
	private boolean isPremiumStartingHand() {
		if (cards.size() != 2) return false;
		Card c1 = cards.get(0);
		Card c2 = cards.get(1);
		
		// Pocket pairs
		if (c1.getNum() == c2.getNum()) {
			return c1.getNum() >= 10;  // JJ or better
		}
		
		// Suited high cards
		if (c1.getSuite() == c2.getSuite()) {
			return c1.getNum() >= 11 && c2.getNum() >= 11;  // QK suited or better
		}
		
		// High cards
		return (c1.getNum() >= 13 && c2.getNum() >= 12) ||  // AK, AQ
			   (c1.getNum() >= 12 && c2.getNum() >= 13);    // AK, AQ (reverse order)
	}
	
	/**
	 * Checks if the player is in late position.
	 * Late position provides better information about other players' actions.
	 * @return true if in late position
	 */
	private boolean isLatePosition() {
		// Consider last 2 positions as late position
		// This is a simplified version - ideally would be based on active players
		return x > 400;  // Rough estimate based on screen position
	}
	
	/**
	 * Calculates the probability of bluffing based on position and stack size.
	 * @return The probability of bluffing (0.05-0.30)
	 */
	private double getBluffProbability() {
		// Base bluff probability
		double prob = 0.1;
		
		// Increase bluff probability in late position
		if (isLatePosition()) {
			prob += 0.1;
		}
		
		// Increase bluff probability if we have a lot of money
		if (money > 2000) {
			prob += 0.1;
		}
		
		// Decrease bluff probability if we're short stacked
		if (money < 500) {
			prob -= 0.05;
		}
		
		return Math.min(0.3, Math.max(0.05, prob));  // Keep between 5% and 30%
	}
	
	/**
	 * Calculates the maximum bet based on hand strength and position.
	 * @param handStrength The strength of the current hand (0-9)
	 * @return The maximum amount to bet
	 */
	private int calculateMaxBet(int handStrength) {
		// Base bet is proportional to hand strength and current stack
		double baseBet = ((double)handStrength / 9.0) * money;
		
		// Adjust based on position
		if (isLatePosition()) {
			baseBet *= 1.2;  // 20% more aggressive in late position
		}
		
		// Minimum bet
		if (baseBet < 20) {
			baseBet = 20;
		}
		
		return (int)Math.min(money, baseBet);
	}
	
	public String getName() {
		return name;
	}
	public void reInit()
	{
		cards = new ArrayList<Card>(7);
		eval = null;
		ready = false;
		folded = false;
		commited = 0;
		lastAction = null;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getMoney() {
		return money;
	}
	
	public void addCard(Card card)
	{
		cards.add(card);
		if(eval != null)
		{
			eval.addCard(card);
		}
	}
	
	public void initEval()
	{
		this.eval = new HandEvaluator(cards);
	}
	
	public String getString()
	{
		return eval.getString();
	}
	
	public int eval()
	{
		if(eval == null)
			initEval();
		return eval.getRankCode();
	}
	
	public void setMoney(int money) {
		this.money = money;
	}
	
	public void addMoney(int amount) {
		this.money += amount;
	}

	public void deductMoney(int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Amount must be positive");
		}
		if (amount > money) {
			throw new IllegalArgumentException("Not enough money to deduct");
		}
		money -= amount;
		commited += amount;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public ArrayList<Card> getCards() {
		return cards;
	}

	public boolean isReady() {
		return ready || folded;  // Folded players are always ready
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isFolded() {
		return folded;
	}

	public void setFolded() {
		folded = true;
		ready = true;
		cards = null;
		lastAction = "FOLD";
	}

	public int getCommited() {
		return commited;
	}
	
	public void clearCommited()
	{
		commited = 0;
	}
	
	public boolean commit(int raiseAmount) {
		// Validate raise amount
		if (raiseAmount <= 0 || raiseAmount <= commited) {
			return false;
		}
		
		// Check if player can afford the raise
		int toAdd = raiseAmount - commited;
		if (money >= toAdd) {
			money -= toAdd;
			commited = raiseAmount;
			ready = true;
			return true;
		}
		
		// Can't afford the raise
		return false;
	}

	public String getLastAction() {
		return lastAction;
	}
	
	public void setLastAction(String action) {
		this.lastAction = action;
	}
}
