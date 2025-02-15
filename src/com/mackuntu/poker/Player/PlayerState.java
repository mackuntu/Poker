package com.mackuntu.poker.Player;

public enum PlayerState {
    ACTIVE,         // Player is in the current hand and has money
    FOLDED,         // Player folded this hand but has money for next hand
    OUT_OF_MONEY,   // Player has no money left
    SITTING_OUT     // Player is temporarily sitting out
} 