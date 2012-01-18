package com.mackuntu.poker.Action;

public enum Action {
	FOLD,
	RAISE,
	CALL,
	CHECK;
	protected int amount;
	public void setAmount(int amount)
	{
		this.amount = amount;
	}
	public int getAmount()
	{
		return amount;
	}
}
