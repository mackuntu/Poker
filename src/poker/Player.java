package poker;

import java.util.ArrayList;

public class Player {
	private String name;
	private int money;
	private ArrayList<Card> cards;
	private HandEvaluator eval;
	private boolean ready;
	private boolean folded;
	private int commited;
	
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
	public Action getAction()
	{
		Action newAct = Action.RAISE;
		newAct.setAmount(10);
		return newAct;
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
		return eval.getRankCode();
	}
	
	public void setMoney(int money) {
		this.money = money;
	}
	
	public void addMoney(int money) {
		this.money += money;
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
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isFolded() {
		return folded;
	}

	public void setFolded() {
		this.folded = true;
		this.ready = true;
	}

	public int getCommited() {
		return commited;
	}

	public boolean commit(int raiseAmount) {
		if(money >= raiseAmount - commited)
		{
			money -= raiseAmount - commited;
			commited = raiseAmount;
			ready = true;
		}
		return this.ready;
	}
}
