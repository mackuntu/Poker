/**
 * Represents a player in the poker game.
 * Manages player state, cards, money, and decision-making logic.
 */
package com.mackuntu.poker.Player;

import java.util.ArrayList;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Card.Card;

public class Player {
	private final String name;
	private final PlayerStrategy strategy;
	private PlayerState state;
	private int money;
	private ArrayList<Card> cards;
	private int committed;  // Total amount committed to current hand
	private String lastAction;
	private int position;  // Table position (0-5 in 6-max)
	
	public Player(String name, PlayerStrategy strategy) {
		this.name = name;
		this.strategy = strategy;
		this.money = 0;
		this.cards = new ArrayList<>();
		this.state = PlayerState.ACTIVE;
		this.committed = 0;
	}
	
	public Action getAction(int currentBet, ArrayList<Card> communityCards, int potSize) {
		if (!canAct()) return null;
		
		GameContext context = new GameContext.Builder()
			.holeCards(cards)
			.communityCards(communityCards)
			.currentBet(currentBet)
			.playerMoney(money)
			.committed(committed)
			.position(position)
			.potSize(potSize)
			.build();
		
		return strategy.decideAction(context);
	}
	
	/**
	 * Remove money from player's stack and add to their committed amount.
	 * This is a simple money movement method - poker rules are enforced at game level.
	 * @param amount Amount to bet
	 * @return true if player had enough money, false otherwise
	 */
	public boolean bet(int amount) {
		if (amount < 0 || amount > money) {
			return false;
		}
		
		money -= amount;
		committed += amount;
		
		if (money <= 0) {
			state = PlayerState.OUT_OF_MONEY;
		}
		
		return true;
	}
	
	/**
	 * Return committed money to player's stack.
	 * Used when collecting antes/bets back on canceled hands.
	 * @param amount Amount to return
	 */
	public void returnMoney(int amount) {
		if (amount < 0 || amount > committed) {
			throw new IllegalArgumentException("Invalid return amount");
		}
		money += amount;
		committed -= amount;
		if (money > 0) {
			state = PlayerState.ACTIVE;
		}
	}
	
	public void fold() {
		state = PlayerState.FOLDED;
		cards = new ArrayList<>();
		lastAction = "FOLD";
	}
	
	/**
	 * Reset player state for new hand.
	 * Clears cards and committed amount, resets state based on money.
	 */
	public void reInit() {
		cards = new ArrayList<>();
		state = money > 0 ? PlayerState.ACTIVE : PlayerState.OUT_OF_MONEY;
		committed = 0;
		lastAction = null;
	}
	
	public void addCard(Card card) {
		cards.add(card);
	}
	
	/**
	 * Adjust player's available money (e.g., for adding chips or taking away antes).
	 * @param amount Amount to adjust (positive or negative)
	 */
	public void adjustMoney(int amount) {
		if (amount < 0 && Math.abs(amount) > money) {
			throw new IllegalArgumentException("Not enough money");
		}
		money += amount;
		if (money <= 0) {
			state = PlayerState.OUT_OF_MONEY;
		}
	}
	
	// Simple getters/setters
	public String getName() { return name; }
	public int getMoney() { return money; }
	public void setMoney(int money) { 
		this.money = money;
		this.state = money > 0 ? PlayerState.ACTIVE : PlayerState.OUT_OF_MONEY;
	}
	public ArrayList<Card> getCards() { return new ArrayList<>(cards); }
	public int getCommitted() { return committed; }
	public void clearCommitted() { committed = 0; }
	public String getLastAction() { return lastAction; }
	public void setLastAction(String action) { this.lastAction = action; }
	public int getPosition() { return position; }
	public void setPosition(int position) { this.position = position; }
	public PlayerState getState() { return state; }
	public void setState(PlayerState state) { this.state = state; }
	
	// State checks
	public boolean isActive() { return state == PlayerState.ACTIVE; }
	public boolean isFolded() { return state == PlayerState.FOLDED; }
	public boolean hasNoMoney() { return state == PlayerState.OUT_OF_MONEY; }
	public boolean canAct() { return state == PlayerState.ACTIVE && money > 0; }
}
