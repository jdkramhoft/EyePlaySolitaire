package ai.agent;

import ai.state.ActionFinder51;
import ai.state.State;
import ai.action.Action;

import java.util.List;

public class RandomAgent implements Agent {

    @Override
    public Action getAction(State state) {
        ActionFinder51 finder = new ActionFinder51();
        List<Action> actionList = finder.getActions(state);
        if(actionList.isEmpty()) return null;
        int count = actionList.size();
        int choice = (int) (Math.random() * count);
        return actionList.get(choice);
    }
}