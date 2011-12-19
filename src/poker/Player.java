package poker;

import java.util.ArrayList;

public class Player {
	String name;
	int money;
	ArrayList<Card> cards;
	public Player(String name)
	{
		this.name = name;
		this.money = 0;
	}

	public Player()
	{
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public ArrayList<Card> getCards() {
		return cards;
	}

	public void addCard(Card card) {
		cards.add(card);
	}

}
