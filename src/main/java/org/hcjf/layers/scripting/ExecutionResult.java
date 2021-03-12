package org.hcjf.layers.scripting;

import java.util.Map;

public class ExecutionResult {

    private final State state;
    private Map<String,Object> result;

    public ExecutionResult(State state) {
        this.state = state;
    }

    public final State getState() {
        return state;
    }

    public final Map<String, Object> getResult() {
        return result;
    }

    public final void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public final Boolean isExecutionFailed() {
        return state.equals(State.FAIL);
    }

    public enum State {
        FAIL,SUCCESS
    }

}
