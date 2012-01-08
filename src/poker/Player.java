package poker;

import java.util.ArrayList;

public class Player {
	private String name;
	private int money;
	private ArrayList<Card> cards;
	private HandEvaluator eval;
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
	public void reInit()
	{
		cards = new ArrayList<Card>(7);
		eval = null;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getMoney() {
		return money;
	}
	
	public void addCard(Card c)
	{
		cards.add(c);
		if(eval != null)
		{
			eval.addCard(c);
		}
	}
	
	public void initEval()
	{
		this.eval = new HandEvaluator(cards);
	}
	
	public void setMoney(int money) {
		this.money = money;
	}
}
