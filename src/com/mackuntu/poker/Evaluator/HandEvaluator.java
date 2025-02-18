/**
 * Evaluates poker hands to determine their ranking and type.
 * Supports standard poker hand rankings from high card to royal flush.
 */
package com.mackuntu.poker.Evaluator;

import java.util.*;
import com.mackuntu.poker.Card.Card;

public class HandEvaluator {
	private final List<Card> cards;
	private HandRank handRank;
	private List<Integer> tieBreakers;
	private String description;

	// Enum to represent hand rankings in poker
	private enum HandRank {
		HIGH_CARD(0, "high card"),
		ONE_PAIR(1, "pair"),
		TWO_PAIR(2, "two pair"),
		THREE_OF_KIND(3, "three of a kind"),
		STRAIGHT(4, "straight"),
		FLUSH(5, "flush"),
		FULL_HOUSE(6, "full house"),
		FOUR_OF_KIND(7, "four of a kind"),
		STRAIGHT_FLUSH(8, "straight flush"),
		ROYAL_FLUSH(9, "royal flush");

		final int value;
		final String description;

		HandRank(int value, String description) {
			this.value = value;
			this.description = description;
		}
	}

	public HandEvaluator(List<Card> cards) {
		this.cards = new ArrayList<>(cards);
		evaluate();
	}

	public HandEvaluator() {
		this.cards = new ArrayList<>();
	}

	public void addCard(Card card) {
		cards.add(card);
		evaluate();
	}

	public void removeCard(Card card) {
		cards.remove(card);
		evaluate();
	}

	public int getRanking() {
		return handRank != null ? handRank.value : 0;
	}

	public String getString() {
		return description;
	}

	private void evaluate() {
		if (cards.isEmpty()) {
			handRank = HandRank.HIGH_CARD;
			tieBreakers = Collections.emptyList();
			description = "no cards";
			return;
		}

		// Sort cards by rank (high to low)
		List<Card> sortedCards = new ArrayList<>(cards);
		sortedCards.sort((a, b) -> Integer.compare(b.getNum(), a.getNum()));

		// Group cards by suit and rank
		Map<Integer, List<Card>> bySuit = new HashMap<>();
		Map<Integer, List<Card>> byRank = new HashMap<>();
		
		for (Card card : sortedCards) {
			bySuit.computeIfAbsent(card.getSuite(), k -> new ArrayList<>()).add(card);
			byRank.computeIfAbsent(card.getNum(), k -> new ArrayList<>()).add(card);
		}

		// Check for flush
		Optional<List<Card>> flush = bySuit.values().stream()
			.filter(suited -> suited.size() >= 5)
			.findFirst();

		// Check for straight and straight flush
		List<Card> straightCards = findStraight(sortedCards);
		List<Card> straightFlushCards = flush.map(this::findStraightInFlush).orElse(null);

		// Evaluate hand from best to worst
		if (straightFlushCards != null) {
			if (straightFlushCards.get(0).getNum() == 14) { // Ace high
				handRank = HandRank.ROYAL_FLUSH;
				tieBreakers = Collections.emptyList();
				description = "royal flush";
			} else {
				handRank = HandRank.STRAIGHT_FLUSH;
				tieBreakers = Collections.singletonList(straightFlushCards.get(0).getNum());
				description = String.format("%s high straight flush", 
					getCardName(straightFlushCards.get(0).getNum()));
			}
			return;
		}

		// Group ranks by frequency
		Map<Integer, Integer> rankFreq = new HashMap<>();
		for (Map.Entry<Integer, List<Card>> entry : byRank.entrySet()) {
			rankFreq.put(entry.getKey(), entry.getValue().size());
		}

		// Find four of a kind
		Optional<Map.Entry<Integer, Integer>> fourOfKind = rankFreq.entrySet().stream()
			.filter(e -> e.getValue() == 4)
			.findFirst();

		if (fourOfKind.isPresent()) {
			int quadRank = fourOfKind.get().getKey();
			int kicker = sortedCards.stream()
				.filter(c -> c.getNum() != quadRank)
				.findFirst()
				.map(Card::getNum)
				.orElse(0);
			handRank = HandRank.FOUR_OF_KIND;
			tieBreakers = Arrays.asList(quadRank, kicker);
			description = String.format("four %ss", getCardName(quadRank));
			return;
		}

		// Find three of a kind and pairs
		List<Integer> threeOfKinds = rankFreq.entrySet().stream()
			.filter(e -> e.getValue() == 3)
			.map(Map.Entry::getKey)
			.sorted(Comparator.reverseOrder())
			.toList();

		List<Integer> pairs = rankFreq.entrySet().stream()
			.filter(e -> e.getValue() == 2)
			.map(Map.Entry::getKey)
			.sorted(Comparator.reverseOrder())
			.toList();

		// Check for full house
		if (!threeOfKinds.isEmpty() && !pairs.isEmpty()) {
			handRank = HandRank.FULL_HOUSE;
			tieBreakers = Arrays.asList(threeOfKinds.get(0), pairs.get(0));
			description = String.format("full house: %ss full of %ss",
				getCardName(threeOfKinds.get(0)), getCardName(pairs.get(0)));
			return;
		}

		// Check for flush
		if (flush.isPresent()) {
			List<Card> flushCards = flush.get().subList(0, 5);
			handRank = HandRank.FLUSH;
			tieBreakers = flushCards.stream()
				.map(Card::getNum)
				.toList();
			description = String.format("%s high flush", 
				getCardName(flushCards.get(0).getNum()));
			return;
		}

		// Check for straight
		if (straightCards != null) {
			handRank = HandRank.STRAIGHT;
			tieBreakers = Collections.singletonList(straightCards.get(0).getNum());
			description = String.format("%s high straight",
				getCardName(straightCards.get(0).getNum()));
			return;
		}

		// Check for three of a kind
		if (!threeOfKinds.isEmpty()) {
			List<Integer> kickers = sortedCards.stream()
				.map(Card::getNum)
				.filter(r -> r != threeOfKinds.get(0))
				.limit(2)
				.toList();
			handRank = HandRank.THREE_OF_KIND;
			tieBreakers = new ArrayList<>();
			tieBreakers.add(threeOfKinds.get(0));
			tieBreakers.addAll(kickers);
			description = String.format("three %ss", getCardName(threeOfKinds.get(0)));
			return;
		}

		// Check for two pair
		if (pairs.size() >= 2) {
			int kicker = sortedCards.stream()
				.map(Card::getNum)
				.filter(r -> r != pairs.get(0) && r != pairs.get(1))
				.findFirst()
				.orElse(0);
			handRank = HandRank.TWO_PAIR;
			tieBreakers = Arrays.asList(pairs.get(0), pairs.get(1), kicker);
			description = String.format("two pair: %ss and %ss",
				getCardName(pairs.get(0)), getCardName(pairs.get(1)));
			return;
		}

		// Check for one pair
		if (pairs.size() == 1) {
			List<Integer> kickers = sortedCards.stream()
				.map(Card::getNum)
				.filter(r -> r != pairs.get(0))
				.limit(3)
				.toList();
			handRank = HandRank.ONE_PAIR;
			tieBreakers = new ArrayList<>();
			tieBreakers.add(pairs.get(0));
			tieBreakers.addAll(kickers);
			description = String.format("pair of %ss", getCardName(pairs.get(0)));
			return;
		}

		// High card
		handRank = HandRank.HIGH_CARD;
		tieBreakers = sortedCards.stream()
			.limit(5)
			.map(Card::getNum)
			.toList();
		description = String.format("%s high", getCardName(tieBreakers.get(0)));
	}

	private List<Card> findStraight(List<Card> sortedCards) {
		// Handle special case: Ace-low straight (A-2-3-4-5)
		if (hasAceLowStraight(sortedCards)) {
			List<Card> straight = new ArrayList<>();
			// Find 5,4,3,2
			for (Card card : sortedCards) {
				if (card.getNum() >= 2 && card.getNum() <= 5) {
					straight.add(card);
				}
			}
			// Add Ace at the end (low)
			straight.add(sortedCards.stream()
				.filter(c -> c.getNum() == 14)
				.findFirst()
				.orElseThrow());
			return straight;
		}

		// Check for regular straights
		List<Card> straight = new ArrayList<>();
		int lastRank = -1;
		
		for (Card card : sortedCards) {
			int rank = card.getNum();
			if (lastRank == -1) {
				straight.add(card);
				lastRank = rank;
			} else if (rank == lastRank - 1) {
				straight.add(card);
				lastRank = rank;
				if (straight.size() == 5) {
					return straight;
				}
			} else if (rank != lastRank) {
				straight.clear();
				straight.add(card);
				lastRank = rank;
			}
		}
		
		return straight.size() == 5 ? straight : null;
	}

	private List<Card> findStraightInFlush(List<Card> flushCards) {
		return findStraight(flushCards);
	}

	private boolean hasAceLowStraight(List<Card> sortedCards) {
		boolean hasAce = false;
		boolean has2 = false;
		boolean has3 = false;
		boolean has4 = false;
		boolean has5 = false;

		for (Card card : sortedCards) {
			switch (card.getNum()) {
				case 14 -> hasAce = true;
				case 2 -> has2 = true;
				case 3 -> has3 = true;
				case 4 -> has4 = true;
				case 5 -> has5 = true;
			}
		}

		return hasAce && has2 && has3 && has4 && has5;
	}

	private String getCardName(int rank) {
		return switch (rank) {
			case 14 -> "A";
			case 13 -> "K";
			case 12 -> "Q";
			case 11 -> "J";
			case 10 -> "10";
			default -> String.valueOf(rank);
		};
	}
}
