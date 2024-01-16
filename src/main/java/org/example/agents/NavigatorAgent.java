package org.example.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class NavigatorAgent extends Agent {

    Player player = new Player();
    Tile[][] worldGrid = new Tile[4][4];
    WorldAgent worldAgent = new WorldAgent();
    protected void setup() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                worldGrid[i][j] = new Tile();
            }
        }
        addBehaviour(new GuidePlayerBehaviour());
        // Initialize a grid representing the world (4x4 in this example)

    }

    private void updateNearbyTiles(int currentX, int currentY, String surroundings) {
        // Update the current tile as visited
        worldGrid[currentX][currentY].setVisited(true);

        // Split the surroundings into an array of words
        String[] perceptions = surroundings.split("\\s+");

        // Update nearby tiles based on the perceived surroundings
        // Assuming the agent perceives surroundings in North, East, South, West directions
        for (String perception : perceptions) {
            if (perception.matches("(?i).*\\bbreeze\\b.*") || perception.matches("(?i).*\\bstinks\\b.*") || perception.matches("(?i).*\\bstench\\b.*")) {
                // Mark nearby tiles as unsafe (potential pits)
                if (currentY > 0) {
                    worldGrid[currentX][currentY - 1].setSafe(false); // North
                }
                if (currentY < 3) {
                    worldGrid[currentX][currentY + 1].setSafe(false); // South
                }
                if (currentX < 3) {
                    worldGrid[currentX + 1][currentY].setSafe(false); // East
                }
                if (currentX > 0) {
                    worldGrid[currentX - 1][currentY].setSafe(false); // West
                }
            }
            if (perception.matches("(?i).*\\bgold\\b.*")) {
                worldGrid[currentX][currentY].setGoldPresent(true);
            }

        }
    }
    private class GuidePlayerBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getSender().getLocalName().equals("PlayerAgent") && msg.getContent().length()>2) {
                String surroundings = msg.getContent();
                updateNearbyTiles(worldAgent.getPlayerX(), worldAgent.getPlayerY(), surroundings);
                String nextMove = decideNextMove(worldAgent.getPlayerX(), worldAgent.getPlayerY());
                ACLMessage moveMsg = new ACLMessage(ACLMessage.PROPOSE);
                moveMsg.addReceiver(new AID("PlayerAgent", AID.ISLOCALNAME));
                moveMsg.setContent(nextMove);
                send(moveMsg);

            } else {
                block();
            }
        }

        private String decideNextMove(int currentX, int currentY) {
            // Logic to check nearby safe and unvisited tiles
            if (currentY > 0 && !worldGrid[currentX][currentY - 1].isVisited() && worldGrid[currentX][currentY - 1].isSafe()) {
                player.storeMovement("I think you need to go up"); // Store the movement
                return "I think you need to go up";
            }
            if (currentY < 3 && !worldGrid[currentX][currentY + 1].isVisited() && worldGrid[currentX][currentY + 1].isSafe()) {
                player.storeMovement("I guess you need to go down"); // Store the movement
                return "I guess you need to go down";
            }
            if (currentX < 3 && !worldGrid[currentX + 1][currentY].isVisited() && worldGrid[currentX + 1][currentY].isSafe()) {
                player.storeMovement("Right is the right way! You get it? Right! Ahaha"); // Store the movement
                return "Right is the right way! You get it? Right! Ahaha";
            }
            if (currentX > 0 && !worldGrid[currentX - 1][currentY].isVisited() && worldGrid[currentX - 1][currentY].isSafe()) {
                player.storeMovement("Go left, there is only one way"); // Store the movement
                return "Go left, there is only one way";
            }

            if (worldGrid[currentX][currentY].isGoldPresent()){
                System.out.println(worldGrid[currentX][currentY].isGoldPresent());
                return "Get gold";
            }

            // Backtracking logic if no safe unvisited tiles
            String lastMove = player.getLastMovement(); // Retrieve the last stored movement
            if (lastMove != null) {
                player.backtrack(); // Backtrack by removing the last movement

                // Determine the opposite movement for backtracking
                switch (lastMove) {
                    case "I think you need to go up":
                        return "I guess you need to go down";
                    case "I guess you need to go down":
                        return "I think you need to go up";
                    case "Right is the right way! You get it? Right! Ahaha":
                        return "Go left, there is only one way";
                    case "Go left, there is only one way":
                        return "Right is the right way! You get it? Right! Ahaha"; default: return "Move randomly";
                }
            }

            // Default action if no specific move is determined
            return "Then, I don't know, just move randomly";
        }
    }
}