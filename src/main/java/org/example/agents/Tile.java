package org.example.agents;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private boolean visited;

    public boolean isGoldPresent() {
        return goldPresent;
    }

    public void setGoldPresent(boolean goldPresent) {
        this.goldPresent = goldPresent;
    }

    private boolean goldPresent;

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isSafe() {
        return safe;
    }

    private boolean safe;

    private List<String> statuses;

    public Tile() {
        this.statuses = new ArrayList<>();
        this.visited = false;
        this.safe = true;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public void addStatus(String status) {
        statuses.add(status);
    }

    public void removeStatus(String status) {
        statuses.remove(status);
    }

    public boolean hasStatus(String status) {
        return statuses.contains(status);
    }

    public List<String> getStatuses() {
        return statuses;
    }
}
