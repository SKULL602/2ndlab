package org.example.agents;

import java.util.Stack;

public class Player {
    private int currentX;
    private int currentY;
    private Stack<String> movementHistory;

    public Player() {
        this.currentX = 0; // Initialize starting X coordinate
        this.currentY = 0; // Initialize starting Y coordinate
        this.movementHistory = new Stack<>();
    }

    // Method to update the current position
    public void updatePosition(int x, int y) {
        this.currentX = x;
        this.currentY = y;
    }

    // Method to store the movement in the history
    public void storeMovement(String move) {
        movementHistory.push(move);
    }

    // Method to backtrack by one step
    public String getLastMovement() {
        if (!movementHistory.isEmpty()) {
            return movementHistory.peek(); // Retrieve the last movement without removing it
        }
        return null; // Return null if no movements are stored
    }

    // Method to backtrack by removing the last movement from history
    public void backtrack() {
        if (!movementHistory.isEmpty()) {
            movementHistory.pop(); // Remove the last movement from history
        }
    }

    // Getters for current X and Y coordinates
    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }
}