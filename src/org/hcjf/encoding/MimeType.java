package org.hcjf.encoding;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class MimeType {

    private static final Map<String, MimeType> types = new HashMap<>();

    /**
     * application/json
     */
    public static final MimeType APPLICATION_JSON = new MimeType(
            TopLevelType.APPLICATION, "json", Tree.STANDARD, null);

    /**
     * application/octet-stream
     */
    public static final MimeType APPLICATION_OCTET_STREAM = new MimeType(
            TopLevelType.APPLICATION, "octet-stream", Tree.STANDARD, null);

    private static final String TYPES_SEPARATOR = "/";
    private static final String SUFFIX_START = "+";

    private final TopLevelType type;
    private final String subTypeName;
    private final Tree tree;
    private final String suffix;

    protected MimeType(TopLevelType type, String subTypeName, Tree tree, String suffix) {
        this.type = type;
        this.subTypeName = subTypeName;
        this.tree = tree;
        this.suffix = suffix;
        types.put(toString(), this);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return type + TYPES_SEPARATOR + tree + subTypeName +
                (suffix == null ? "" : (SUFFIX_START + suffix));
    }

    /**
     *
     * @param mimeType
     * @return
     */
    public static MimeType fromString(String mimeType) {
        return types.get(mimeType);
    }

    /**
     *
     */
    protected enum TopLevelType {

        APPLICATION("application"),

        AUDIO("audio"),

        EXAMPLE("example"),

        IMAGE("image"),

        MESSAGE("message"),

        MODEL("model"),

        MULTIPART("multipart"),

        TEXT("text"),

        VIDEO("video");

        private final String name;

        TopLevelType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     *
     */
    protected enum Tree {

        STANDARD(""),

        VENDOR("vnd."),

        PERSONAL("prs."),

        UNREGISTERED("x.");

        private final String name;

        Tree(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

}
