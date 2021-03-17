package org.hcjf.layers.scripting;

import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.errors.HCJFServiceTimeoutException;
import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

public class JavaCodeEvaluator extends Layer implements CodeEvaluator {

    private static final String NAME = "java";

    private static final String CLASS_PATH_PROPERTY = "java.class.path";
    private static final String[] imports = {
            "import java.util.*;",
            "import org.hcjf.utils.*;",
            "import org.hcjf.bson.*;"
    };

    private static final String METHOD_WRAPPER = "public Object method_%s(Map<String,Object> parameters) throws Exception {%s}";
    private static final String CREATE_PARAMETERS = "Map<String,Object> var_%s = BsonDecoder.decode(Strings.hexToBytes(\"%s\")).toMap();";
    private static final String CALL_METHOD = "var_%s.put(\"_result\", method_%s(var_%s));";
    private static final String CREATE_BSON_RESULT = "String _var_%s = Strings.bytesToHex(BsonEncoder.encode(new BsonDocument(var_%s)));";
    private static final String BSON_RESULT = "_var_%s";
    private static final String RESULT = "_result";

    private static final String OUT_FIELD = "_out";
    private static final String ERR_FIELD = "_error";
    private static final String DIAGNOSTICS_FIELD = "_diagnostics";
    private static final String START_DIAGNOSTIC_ERROR = "-->";
    private static final String END_DIAGNOSTIC_ERROR = "<--";
    private static final String WAITING_VM_TIME_FIELD = "_waitingVmTime";
    private static final String EVAL_TIME_FIELD = "_evalTime";
    private static final Integer DEFAULT_CACHE_SIZE = 10;
    private static final Long DEFAULT_EVAL_TIMEOUT = 5000L;

    private final Queue<JShellInstance> cache;

    public JavaCodeEvaluator() {
        this.cache = new LinkedList<>();
        Integer size = SystemProperties.getInteger(SystemProperties.CodeEvaluator.Java.JAVA_CACHE_SIZE, DEFAULT_CACHE_SIZE);
        for (int i = 0; i < size; i++) {
            cache.offer(new JShellInstance());
        }
    }

    @Override
    public String getImplName() {
        return NAME;
    }

    /**
     * Evaluate the script with a set of parameter and store the result into the result object. This particular
     * implementation evaluate java code.
     * @param script     Script to evaluate.
     * @param parameters Parameters object.
     * @return Model as result of the script evaluation.
     */
    @Override
    public ExecutionResult evaluate(String script, Map<String, Object> parameters) {
        JShellInstance jShellInstance;
        Long waitingVmTime = System.currentTimeMillis();
        synchronized (cache) {
            while (cache.isEmpty()) {
                try {
                    cache.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
            jShellInstance = cache.remove();
            waitingVmTime = System.currentTimeMillis() - waitingVmTime;
        }

        Boolean killShell = false;
        Long timeout = SystemProperties.getLong(SystemProperties.CodeEvaluator.Java.JAVA_CACHE_TIMEOUT, DEFAULT_EVAL_TIMEOUT);
        try {
            ExecutionResult result = Service.call(() -> jShellInstance.evaluate(script, parameters),
                    ServiceSession.getCurrentIdentity(), timeout);
            result.getResultState().put(WAITING_VM_TIME_FIELD, waitingVmTime);
            return result;
        } catch (HCJFServiceTimeoutException ex) {
            killShell = true;
            throw ex;
        } finally {
            synchronized (cache) {
                if(killShell) {
                    jShellInstance.kill();
                    cache.offer(new JShellInstance());
                } else {
                    cache.offer(jShellInstance);
                }
                cache.notifyAll();
            }
        }
    }

    private static class JShellInstance {

        private JShell jShell;
        private ByteArrayOutputStream outStream;
        private ByteArrayOutputStream errorStream;
        private PrintStream out;
        private PrintStream error;

        public JShellInstance() {
            init();
        }

        public void init() {
            outStream = new ByteArrayOutputStream();
            out = new PrintStream(outStream);
            errorStream = new ByteArrayOutputStream();
            error = new PrintStream(errorStream);
            jShell = JShell.builder().out(out).err(error).build();
            jShell.addToClasspath(System.getProperty(CLASS_PATH_PROPERTY));
            for(String i : imports) {
                jShell.eval(i);
            }
        }

        private List<SnippetEvent> createMethod(String executionId, String script) {
            String method = String.format(METHOD_WRAPPER, executionId, script);
            return jShell.eval(method);
        }

        private List<SnippetEvent> createParameters(String executionId, String bson) {
            String parameters = String.format(CREATE_PARAMETERS, executionId, bson);
            return jShell.eval(parameters);
        }

        private List<SnippetEvent> call(String executionId) {
            String call = String.format(CALL_METHOD, executionId, executionId, executionId);
            return jShell.eval(call);
        }

        private List<SnippetEvent> createBsonResult(String executionId) {
            String call = String.format(CREATE_BSON_RESULT, executionId, executionId, executionId);
            return jShell.eval(call);
        }

        public ExecutionResult evaluate(String script, Map<String, Object> parameters) {
            Boolean fail = false;
            Long time = System.currentTimeMillis();
            String bson = Strings.bytesToHex(BsonEncoder.encode(new BsonDocument(parameters)));
            List<String> diagnosticsList = new ArrayList<>();
            List<SnippetEvent> snippets = new ArrayList<>();
            try {
                String executionId = UUID.randomUUID().toString().replace("-", "_");
                snippets.addAll(createMethod(executionId, script));
                snippets.addAll(createParameters(executionId, bson));
                snippets.addAll(call(executionId));
                snippets.addAll(createBsonResult(executionId));

                for (SnippetEvent snippetEvent : snippets) {
                    if (!snippetEvent.status().equals(Snippet.Status.VALID)) {
                        fail = true;
                        jShell.diagnostics(snippetEvent.snippet()).forEach(D -> {
                            StringBuilder sourceBuilder = new StringBuilder();
                            snippets.stream().forEach(S -> sourceBuilder.append(S.snippet().source()));
                            StringBuilder builder = new StringBuilder();
                            String source = sourceBuilder.toString();
                            builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
                            builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
                            builder.append(snippetEvent.status().toString());
                            builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
                            builder.append(D.getMessage(Locale.getDefault()));
                            builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
                            builder.append(source, 0, (int) D.getStartPosition());
                            builder.append(START_DIAGNOSTIC_ERROR);
                            builder.append(source, (int) D.getStartPosition(), (int) D.getEndPosition());
                            builder.append(END_DIAGNOSTIC_ERROR);
                            builder.append(source.substring((int) D.getEndPosition()));
                            diagnosticsList.add(builder.toString());
                        });
                    }
                }
                Map<String,Object> resultParameters = new HashMap<>();
                Map<String, Object> resultState = new HashMap<>();
                if (!fail) {
                    String bsonResult = jShell.varValue(jShell.variables().filter(V -> V.name().equals(String.format(BSON_RESULT, executionId))).findFirst().get());
                    bsonResult = bsonResult.replace("\"", Strings.EMPTY_STRING);
                    resultParameters.putAll(BsonDecoder.decode(Strings.hexToBytes(bsonResult)).toMap());
                }
                resultState.put(OUT_FIELD, outStream.toString());
                resultState.put(ERR_FIELD, errorStream.toString());
                resultState.put(DIAGNOSTICS_FIELD, diagnosticsList);
                resultState.put(EVAL_TIME_FIELD, System.currentTimeMillis() - time);
                ExecutionResult executionResult = new ExecutionResult(
                        fail ? ExecutionResult.State.FAIL : ExecutionResult.State.SUCCESS,
                        resultState, resultParameters, resultParameters.remove(RESULT));
                return executionResult;
            } finally {
                jShell.snippets().forEach(S -> {
                    if(!(S instanceof ImportSnippet)) {
                        jShell.drop(S);
                    }
                });
                outStream.reset();
                errorStream.reset();
            }
        }

        public void kill() {
            jShell.close();
        }

    }

}
