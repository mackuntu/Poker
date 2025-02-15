package com.mackuntu.poker.game;

import com.mackuntu.poker.Action.Action;
import com.mackuntu.poker.Player.Player;

public interface ActionValidator {
    boolean isValidAction(Action action, Player player, int currentBet);
} 