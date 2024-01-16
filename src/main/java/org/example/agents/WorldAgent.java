package org.example.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldAgent extends Agent {
    static Tile[][] cave = new Tile[4][4];

    private int playerX = 0;
    private int playerY = 3;

    protected void setup() {
        Object[] args = { this }; // Pass reference to this WorldAgent
        try {
            AgentController player = getContainerController().createNewAgent("PlayerAgent", PlayerAgent.class.getName(),
                    args);
            player.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        initializeCave();
        printCave();
        addBehaviour(new CheckPositionBehaviour());
        addBehaviour(new HandlePlayerMovements());

    }

    private class CheckPositionBehaviour extends CyclicBehaviour {

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String content = msg.getContent();
                if (content.matches("(?i).*\\bhelp\\b.*")) {
                    String[] surroundings = checkSurroundings();
                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    reply.addReceiver(msg.getSender());
                    reply.setContent(String.join(",", surroundings));
                    send(reply);
                }
            } else {
                block();
            }
        }
    }

    // Method to check surroundings and return information
    private String[] checkSurroundings() {
        String[] surroundings = new String[3];
        Arrays.fill(surroundings, "none"); // Default: no indication

        // Get player's current tile
        Tile currentPlayerTile = cave[playerY][playerX];

        // Logic to check the player's current tile for specific statuses
        if (currentPlayerTile.hasStatus("Breeze")) {
            surroundings[0] = "breeze";
        }
        if (currentPlayerTile.hasStatus("Stench")) {
            surroundings[1] = "stench";
        }
        if (currentPlayerTile.hasStatus("Gold")) {
            surroundings[2] = "glitter";
        }

        return surroundings;
    }

    // Sample methods to get player position (replace with your actual player
    // tracking logic)
    public int getPlayerX() {
        // Return player's X position
        // Implement your actual player position tracking logic here
        return playerX; // Sample: player X position is 0
    }

    public int getPlayerY() {
        // Return player's Y position
        // Implement your actual player position tracking logic here
        return playerY; // Sample: player Y position is 0
    }

    // Set player's position in the cave
    public void setPlayerPosition(String playerName, int x, int y) {
        // Logic to set player's position in the cave
        // For instance, update cave[x][y] to represent the player
        // Here, you could use a specific value (e.g., 3) to represent the player
        playerY = y;
        playerX = x;
    }

    private void addPit(int x, int y) {
        cave[y][x].addStatus("Pit"); // Setting the pit at coordinates (x, y)

        // Logic to add breeze to adjacent tiles
        if (x > 0) {
            cave[y][x - 1].addStatus("Breeze");
        }
        if (x < 3) {
            cave[y][x + 1].addStatus("Breeze");
        }
        if (y > 0) {
            cave[y - 1][x].addStatus("Breeze");
        }
        if (y < 3) {
            cave[y + 1][x].addStatus("Breeze");
        }
    }

    private void addGold(int x, int y) {
        cave[y][x].addStatus("Gold");
        cave[y][x].setGoldPresent(true);

    }

    private void addWumpus(int x, int y) {
        cave[y][x].addStatus("Wumpus"); // Setting the Wumpus at coordinates (x, y)

        // Logic to add stench to adjacent tiles
        if (x > 0) {
            cave[y][x - 1].addStatus("Stench");
        }
        if (x < 3) {
            cave[y][x + 1].addStatus("Stench");
        }
        if (y > 0) {
            cave[y - 1][x].addStatus("Stench");
        }
        if (y < 3) {
            cave[y + 1][x].addStatus("Stench");
        }
    }

    // Initialize the 4x4 cave with default values
    private void initializeCave() {
        // Initialize the cave with empty tiles
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cave[i][j] = new Tile();
            }
        }
        addPit(2, 3);
        addPit(3, 1);
        addPit(0, 0);
        addWumpus(3, 3);
        addGold(2, 2);
    }

    // Print the cave structure (for demonstration purposes)
    private void printCave() {
        System.out.println("Cave Structure:");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == playerY && j == playerX) {
                    System.out.print("A "); // Player
                } else if (cave[i][j].getStatuses().contains("Pit")) {
                    System.out.print("P "); // Pit
                } else if (cave[i][j].getStatuses().contains("Wumpus")) {
                    System.out.print("W "); // Wumpus
                } else if (cave[i][j].getStatuses().contains("Gold")) {
                    System.out.print("G "); // Gold
                } else {
                    System.out.print("- "); // Empty cell
                }
            }
            System.out.println();
        }
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 4 && y >= 0 && y < 4;
    }

    private List<String> checkTileStatus(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return cave[y][x].getStatuses();
        } else {
            // Handle out-of-bounds coordinates, return an empty list
            return Collections.emptyList(); // Returning an empty list
        }
    }

    private void setPlayerLocation(int newX, int newY) {
        if (isValidCoordinate(newX, newY)) {
            playerX = newX;
            playerY = newY;
        } else {
            // Handle invalid coordinates, if needed
            // For example, throw an IllegalArgumentException or handle it based on your
            // game logic
            System.out.println("Invalid coordinates for setting player location.");
        }
    }

    private void doExit() {
        stopAgent("PlayerAgent");
        stopAgent("NavigatorAgent");
        stopAgent("WorldAgent");
        System.exit(0);
    }

    private void stopAgent(String agentName) {
        try {
            AgentController agentController = getContainerController().getAgent(agentName);
            if (agentController != null) {
                agentController.kill();
            }
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private void movePlayer(int newX, int newY) {
        if (isValidCoordinate(newX, newY)) {
            if (checkTileStatus(newX, newY).contains("Pit")) {
                System.out.println("Game Over! You fell into a pit!");
                doDelete(); // Terminate the player agent
                // Optionally: handle other game-over actions or exit the game
                doExit(); // Exit the game
            } else if (checkTileStatus(newX, newY).contains("Wumpus")) {
                System.out.println("Game Over! You encountered the Wumpus!");
                doDelete(); // Terminate the player agent
                // Optionally: handle other game-over actions or exit the game
                doExit(); // Exit the game
            } 
             else {
                // Update the player's position
                setPlayerLocation(newX, newY);
                // Additional actions when the player moves to a safe tile

            }
        } else {
            System.out.println("Invalid move! Out of bounds.");
        }
    }

    private class HandlePlayerMovements extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = receive(mt);
            if (msg != null) {
                if (msg.getContent().matches("(?i).*up.*")) {
                    movePlayer(getPlayerX(), getPlayerY() - 1);
                    System.out.println(getLocalName() + ": Mr.Navigator,I moved him up");// Move the player up
                }
                if (msg.getContent().matches("(?i).*down.*")) {
                    movePlayer(getPlayerX(), getPlayerY() + 1); // Move the player down
                    System.out.println(getLocalName() + ": Mr.Navigator,I moved him down");
                }
                if (msg.getContent().matches("(?i).*right.*")) {
                    movePlayer(getPlayerX() + 1, getPlayerY());
                    System.out.println(getLocalName() + ": Mr.Navigator,I moved him to the right");// Move the player
                                                                                                   // right
                }
                if (msg.getContent().matches("(?i).*left.*")) {
                    movePlayer(getPlayerX() - 1, getPlayerY());
                    System.out.println(getLocalName() + ": Mr.Navigator,He went left");// Move the player left
                }

                if (msg.getContent().matches("(?i).*gold.*")) {
                    cave[playerX][playerY].removeStatus("Gold");
                    System.out.println("I'VE GOT MY GOLD. WIN !!!");
                    doDelete();
                    doExit();
                }
                if (msg.getContent().matches("(?i).*randomly.*")) {
                    boolean moved = false;
                    while (!moved) {
                        Random random = new Random();
                        int randomNumber = random.nextInt(4); // Generate a random number between 0 and 3

                        // Store the current player position
                        int currentX = getPlayerX();
                        int currentY = getPlayerY();

                        // Move randomly based on the generated number without hitting walls
                        if (randomNumber == 0 && currentY > 0) {
                            movePlayer(currentX, currentY - 1); // Move up
                            System.out.println(getLocalName() + ": Mr.Navigator,I moved him up");
                            moved = true;
                        } else if (randomNumber == 1 && currentY < 3) {
                            movePlayer(currentX, currentY + 1); // Move down
                            System.out.println(getLocalName() + ": Mr.Navigator,I moved him down");
                            moved = true;
                        } else if (randomNumber == 2 && currentX < 3) {
                            movePlayer(currentX + 1, currentY); // Move right
                            System.out.println(getLocalName() + ": Mr.Navigator,I moved him to the right");
                            moved = true;
                        } else if (randomNumber == 3 && currentX > 0) {
                            movePlayer(currentX - 1, currentY); // Move left
                            System.out.println(getLocalName() + ": Mr.Navigator,He went left");
                            moved = true;
                        }
                    }
                }
                ACLMessage moveMsg = new ACLMessage(ACLMessage.CONFIRM);
                moveMsg.addReceiver(new AID("PlayerAgent", AID.ISLOCALNAME));
                moveMsg.setContent("OK");
                send(moveMsg);
                printCave();
            } else {
                block(); // Wait for a message before executing the behavior again
            }

        }
    }
}