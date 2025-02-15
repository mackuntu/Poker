/**
 * Represents possible poker actions that a player can take during their turn.
 * This enum defines the standard poker actions and includes functionality
 * for handling raise amounts.
 */
package com.mackuntu.poker.Action;

public enum Action {
	/** Player folds their hand and exits the current round */
	FOLD,
	/** Player raises the bet by a specified amount */
	RAISE,
	/** Player matches the current bet amount */
	CALL,
	/** Player passes their turn when no bet is required */
	CHECK;

	/** The amount associated with a raise action */
	private int amount;

	/**
	 * Sets the amount for a raise action.
	 * @param amount The amount to raise, will be set to 0 if negative
	 */
	public void setAmount(int amount)
	{
		// Prevent negative raise amounts
		this.amount = Math.max(0, amount);
	}

	/**
	 * Gets the current raise amount.
	 * @return The amount set for this action
	 */
	public int getAmount()
	{
		return amount;
	}
}
