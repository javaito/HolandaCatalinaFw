package org.hcjf.layers.query.functions;

public class JShellQueryFunction extends BaseQueryFunctionLayer {

    private static final class Functions {
        private static final String SHELL = "jsh";
        private static final String NAMED_VALUE = "$";
    }

    private static final String NAME = "jShell";

    public JShellQueryFunction() {
        super(NAME);
        addFunctionName(Functions.SHELL);
        addFunctionName(Functions.NAMED_VALUE);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        return null;
    }

}
