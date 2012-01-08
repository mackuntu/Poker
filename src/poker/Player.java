package poker;

import java.util.ArrayList;

public class Player {
	private String name;
	private int money;
	private ArrayList<Card> cards;
	private HandEvaluator eval;
	public int x,y;
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
	
	public String getString()
	{
		return eval.getString();
	}
	
	public int eval()
	{
		return eval.getRanking();
	}
	
	public void setMoney(int money) {
		this.money = money;
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
}
