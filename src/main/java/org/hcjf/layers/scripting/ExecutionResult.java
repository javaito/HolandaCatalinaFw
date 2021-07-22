package org.hcjf.layers.scripting;

import java.util.Map;

public class ExecutionResult {

    private final State state;
    private final Map<String,Object> resultState;
    private final Map<String,Object> resultParameters;
    private final Object result;

    public ExecutionResult(State state, Map<String,Object> resultState, Map<String,Object> resultParameters, Object result) {
        this.state = state;
        this.resultState = resultState;
        this.resultParameters = resultParameters;
        this.result = result;
    }

    public final State getState() {
        return state;
    }

    public final Map<String, Object> getResultState() {
        return resultState;
    }

    public final Boolean isExecutionFailed() {
        return state.equals(State.FAIL);
    }

    public <O extends Object> O getResult() {
        return (O) result;
    }

    public Map<String, Object> getResultParameters() {
        return resultParameters;
    }

    public enum State {
        FAIL,SUCCESS
    }

}
