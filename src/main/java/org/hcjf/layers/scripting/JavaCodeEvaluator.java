package org.hcjf.layers.scripting;

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
import org.hcjf.utils.LruMap;
import org.hcjf.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

public class JavaCodeEvaluator extends Layer implements CodeEvaluator {

    private static final String CLASS_PATH_PROPERTY = "java.class.path";
    private static final String[] IMPORTS = {
            "java.util.*",
            "org.hcjf.utils.*",
            "org.hcjf.bson.*"
    };
    private static final String IMPORT_TEMPLATE = "import %s;";

    private static final String METHOD_WRAPPER = "public Object method_%s(Map<String,Object> parameters) throws Exception {%s}";
    private static final String CREATE_PARAMETERS = "Map<String,Object> var_%s = BsonDecoder.decode(Strings.hexToBytes(bson)).toMap();";
    private static final String CALL_METHOD = "var_%s.put(\"_result\", method_%s(var_%s));";
    private static final String CREATE_BSON_RESULT = "return Strings.bytesToHex(BsonEncoder.encode(new BsonDocument(var_%s)));";
    private static final String CREATE_RESULT_METHOD = "public String createResult_%s(String bson) throws Exception {%s\r\n%s\r\n%s\r\n}";
    private static final String CALL_RESULT_METHOD = "System.out.print(\">>>>\" + createResult_%s(\"%s\"));";
    private static final String RESULT = "_result";
    private static final String BSON_POINTER = ">>>>";

    private static final String OUT_FIELD = "_out";
    private static final String ERR_FIELD = "_error";
    private static final String DIAGNOSTICS_FIELD = "_diagnostics";
    private static final String START_DIAGNOSTIC_ERROR = "-->";
    private static final String END_DIAGNOSTIC_ERROR = "<--";
    private static final String WAITING_VM_TIME_FIELD = "_waitingVmTime";
    private static final String EVAL_TIME_FIELD = "_evalTime";

    private final Queue<JShellInstance> pool;

    public JavaCodeEvaluator() {
        this.pool = new LinkedList<>();
        Integer size = SystemProperties.getInteger(SystemProperties.CodeEvaluator.Java.J_SHELL_POOL_SIZE);
        for (int i = 0; i < size; i++) {
            pool.offer(new JShellInstance());
        }
    }

    @Override
    public String getImplName() {
        return SystemProperties.get(SystemProperties.CodeEvaluator.Java.IMPL_NAME);
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
        synchronized (pool) {
            while (pool.isEmpty()) {
                try {
                    pool.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
            jShellInstance = pool.remove();
            waitingVmTime = System.currentTimeMillis() - waitingVmTime;
        }

        Boolean killShell = false;
        Long timeout = SystemProperties.getLong(SystemProperties.CodeEvaluator.Java.J_SHELL_INSTANCE_TIMEOUT);
        try {
            ExecutionResult result = Service.call(() -> jShellInstance.evaluate(script, parameters),
                    ServiceSession.getCurrentIdentity(), timeout);
            result.getResultState().put(WAITING_VM_TIME_FIELD, waitingVmTime);
            return result;
        } catch (HCJFServiceTimeoutException ex) {
            killShell = true;
            throw ex;
        } finally {
            synchronized (pool) {
                if(killShell) {
                    jShellInstance.kill();
                    pool.offer(new JShellInstance());
                } else {
                    pool.offer(jShellInstance);
                }
                pool.notifyAll();
            }
        }
    }

    private static class JShellScript {

        private final String id;
        private final String script;
        private final List<SnippetEvent> snippetEvents;

        public JShellScript(String id, String script, List<SnippetEvent> snippetEvents) {
            this.id = id;
            this.script = script;
            this.snippetEvents = snippetEvents;
        }

        public String getId() {
            return id;
        }

        public String getScript() {
            return script;
        }

        public List<SnippetEvent> getSnippetEvents() {
            return snippetEvents;
        }
    }

    private static class JShellInstance implements LruMap.RemoveOverflowListener {

        private JShell jShell;
        private ByteArrayOutputStream outStream;
        private ByteArrayOutputStream errorStream;
        private PrintStream out;
        private PrintStream error;
        private LruMap<String,JShellScript> scriptCache;

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
            for(String i : IMPORTS) {
                jShell.eval(String.format(IMPORT_TEMPLATE, i));
            }

            List<String> customImports = SystemProperties.getList(SystemProperties.CodeEvaluator.Java.IMPORTS);
            for (String i : customImports) {
                jShell.eval(String.format(IMPORT_TEMPLATE, i));
            }

            scriptCache = new LruMap<>(SystemProperties.getInteger(SystemProperties.CodeEvaluator.Java.SCRIPT_CACHE_SIZE));
        }

        private List<SnippetEvent> createMethod(String scriptId, String script) {
            String method = String.format(METHOD_WRAPPER, scriptId, script);
            return jShell.eval(method);
        }

        private List<SnippetEvent> createResultMethod(String scriptId) {
            String resultMethod = String.format(CREATE_RESULT_METHOD, scriptId,
                    String.format(CREATE_PARAMETERS, scriptId),
                    String.format(CALL_METHOD, scriptId, scriptId, scriptId),
                    String.format(CREATE_BSON_RESULT, scriptId));
            return jShell.eval(resultMethod);
        }

        private List<SnippetEvent> call(String scriptId, String bson) {
            String call = String.format(CALL_RESULT_METHOD, scriptId, bson);
            return jShell.eval(call);
        }

        public ExecutionResult evaluate(String script, Map<String, Object> parameters) {
            Boolean fail = false;
            Long time = System.currentTimeMillis();
            String bson = Strings.bytesToHex(BsonEncoder.encode(new BsonDocument(parameters)));
            List<String> diagnosticsList = new ArrayList<>();
            try {
                JShellScript jShellScript = scriptCache.get(script);
                String scriptId;
                List<SnippetEvent> scriptSnippets = new ArrayList<>();
                if(jShellScript == null) {
                    scriptId = UUID.randomUUID().toString().replace("-", "_");
                    scriptSnippets.addAll(createMethod(scriptId, script));
                    for (SnippetEvent snippetEvent : scriptSnippets) {
                        if (!snippetEvent.status().equals(Snippet.Status.VALID)) {
                            fail = true;
                            jShell.diagnostics(snippetEvent.snippet()).forEach(D -> {
                                StringBuilder sourceBuilder = new StringBuilder();
                                scriptSnippets.forEach(S -> sourceBuilder.append(S.snippet().source()));
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
                    if(!fail) {
                        scriptSnippets.addAll(createResultMethod(scriptId));
                        scriptCache.put(script, new JShellScript(scriptId, script, scriptSnippets));
                    }
                } else {
                    scriptId = jShellScript.getId();
                }

                Map<String,Object> resultParameters = new HashMap<>();
                Map<String, Object> resultState = new HashMap<>();
                if (!fail) {
                    call(scriptId, bson);
                    String bsonResult = outStream.toString();
                    Integer lastIndex = bsonResult.lastIndexOf(BSON_POINTER);
                    bsonResult = bsonResult.substring(lastIndex + BSON_POINTER.length());
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
                outStream.reset();
                errorStream.reset();
            }
        }

        @Override
        public synchronized void onRemove(Object key, Object value) {
            JShellScript jShellScript = (JShellScript) value;
            jShellScript.getSnippetEvents().forEach(E -> jShell.drop(E.snippet()));
        }

        public void kill() {
            jShell.close();
        }

    }

}
