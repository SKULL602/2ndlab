package org.example.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.Random;

public class PlayerAgent extends Agent {
    private Random random = new Random();

    private String[] responsesBreeze = {
        "Ah, I feel a breeze here.", 
        "It's a breeze here.",
        "The breeze is refreshing.",
        "This breeze is unusual, but comforting.",
        "A gentle breeze is blowing through.",
        "The air is filled with a light breeze."};
    
    private String[] helpMessage = {
        "Help, I don't know where to go!", 
        "I might need some help down there!",
        "I feel lost, can someone help me?",
        "Is anyone there to help me?",
        "Help! I'm not sure about my next move."};
        
    private String[] responsesStench = {
        "Ugh, it stinks in this area.", 
        "Meh, something stinks down there.",
        "The stench is unbearable.",
        "Is that a stench? It's awful!"};
   
    private String[] responsesGlitter = {
        "Wow, I think I've found gold!", 
        "Shiny! There might be something valuable here. Gold!",
        "This sparkling sight could be gold!",
        "Could this be the glimmer of precious gold?",
        "I see a glittering reflection – potential gold!"};
   
    private String[] responsesDefault = {
        "It's so good to be safe in here.", 
        "Seems quiet and safe around.",
        "I feel safe in this spot.",
        "This place feels safe and peaceful."};

    protected void setup() {
        addBehaviour(new SequentialBehaviour());
        String playerName = "PlayerAgent"; 

        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof WorldAgent) {
            WorldAgent world = (WorldAgent) args[0];
            world.setPlayerPosition(playerName, 0, 3);
        } else {
            System.out.println("No reference to WorldAgent found.");
            doDelete();
        }
    }

    private class SequentialBehaviour extends CyclicBehaviour {
        private int state = 0;

        public void action() {
            switch (state) {
                case 0:
                    requestPosition();
                    break;
                case 1:
                    handleSurroundingsAndMove();
                    break;
                case 2:
                    handleNavigatorResponse();
                    break;
                case 3:
                    informWorldAboutMovement();
                    break;
            }
        }

        private void requestPosition() {
            ACLMessage inquirePosition = new ACLMessage(ACLMessage.REQUEST);
            inquirePosition.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
            inquirePosition.setContent(helpMessage[random.nextInt(helpMessage.length)]);
            send(inquirePosition);
            state++;
            System.out.println(getLocalName() + ": " + inquirePosition.getContent());
        }

        private void handleSurroundingsAndMove() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);

            if (msg != null) {
                String[] surroundings = msg.getContent().split(",");
                String response = generateResponse(surroundings);
                System.out.println(getLocalName() + ": " + response);

                ACLMessage navigateMsg = new ACLMessage(ACLMessage.CFP);
                navigateMsg.addReceiver(new AID("NavigatorAgent", AID.ISLOCALNAME));
                navigateMsg.setContent(response);
                send(navigateMsg);
                state++;
            } else {
                block();
            }
        }

        private String generateResponse(String[] surroundings) {
            String response;

            if (Arrays.asList(surroundings).contains("breeze") && Arrays.asList(surroundings).contains("stench")) {
                response = "Whoa, I feel a breeze and it stinks!";
            } else if (Arrays.asList(surroundings).contains("breeze") && Arrays.asList(surroundings).contains("glitter")) {
                response = "Ah, I feel a breeze and I think I've found gold nearby.";
            } else if (Arrays.asList(surroundings).contains("stench") && Arrays.asList(surroundings).contains("glitter")) {
                response = "Ugh, it stinks here, but I think I've found gold!";
            } else if (Arrays.asList(surroundings).contains("breeze") && Arrays.asList(surroundings).contains("stench") && Arrays.asList(surroundings).contains("glitter")) {
                response = "It's breeze, it stinks, and I've found gold!";
            } else if (Arrays.asList(surroundings).contains("breeze")) {
                response = responsesBreeze[random.nextInt(responsesBreeze.length)];
            } else if (Arrays.asList(surroundings).contains("stench")) {
                response = responsesStench[random.nextInt(responsesStench.length)];
            } else if (Arrays.asList(surroundings).contains("glitter")) {
                response = responsesGlitter[random.nextInt(responsesGlitter.length)];
            } else {
                response = responsesDefault[random.nextInt(responsesDefault.length)];
            }

            return response;
        }

        private void handleNavigatorResponse() {
            MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg2 = receive(mt2);

            if (msg2 != null) {
                String action = msg2.getContent();
                if (isValidAction(action)) {
                    ACLMessage moveMsg = new ACLMessage(ACLMessage.CFP);
                    moveMsg.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
                    moveMsg.setContent(action);
                    send(moveMsg);
                    System.out.println(msg2.getSender().getLocalName() + ": " + action);
                    state++;
                }
            } else {
                block();
            }
        }

        private boolean isValidAction(String action) {
            return action.matches("(?i)(.*\\bup\\b.*|.*\\bdown\\b.*|.*\\bleft\\b.*|.*\\bright\\b.*|.*\\brandomly\\b.*)")
                    || action.matches("(?i)(.*\\bgold\\b.*|)");
        }

        private void informWorldAboutMovement() {
            MessageTemplate mt3 = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            ACLMessage msg3 = receive(mt3);

            if (msg3 != null && msg3.getContent().equals("OK")) {
                state = 0;
            } else {
                block();
            }
        }
    }
}
