package com.mackuntu.poker.Player;

import com.mackuntu.poker.Action.Action;

public interface PlayerStrategy {
    /**
     * Determines the next action for a player based on game state.
     * @param gameState Current state of the game needed for decision making
     * @return The chosen action
     */
    Action decideAction(GameContext gameState);
} 