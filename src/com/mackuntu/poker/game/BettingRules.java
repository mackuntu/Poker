package com.mackuntu.poker.game;

import com.mackuntu.poker.Player.Player;

public interface BettingRules {
    boolean canCheck(Player player);
    boolean canCall(Player player);
    boolean canRaise(Player player, int amount);
    int getMinimumRaise();
    int getCurrentBet();
    void setCurrentBet(int amount);
    void resetBets();
} 