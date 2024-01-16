package org.example;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.example.agents.NavigatorAgent;
import org.example.agents.PlayerAgent;
import org.example.agents.WorldAgent;

public class WumpusGameRunner {
    public static void main(String[] args) {
        jade.core.Runtime rt = jade.core.Runtime.instance();
        Profile p = new ProfileImpl();
        AgentContainer ac = rt.createMainContainer(p);

        try {
            // Create WorldAgent
            AgentController worldAgentController = ac.createNewAgent("WorldAgent", WorldAgent.class.getName(), null);
            worldAgentController.start();


            AgentController navigatorAgentController = ac.createNewAgent("NavigatorAgent", NavigatorAgent.class.getName(), null);
            navigatorAgentController.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
