package org.hcjf.io.net.http;

import org.hcjf.utils.Strings;

/**
 * @author javaito
 */
public class Cookie {

    public static final String COMMENT = "Comment";
    public static final String DOMAIN = "Domain";
    public static final String MAX_AGE = "Max-Age";
    public static final String PATH = "Path";
    public static final String SECURE = "Secure";
    public static final String VERSION = "Version";

    private final String name;
    private final String value;
    private String comment;
    private String domain;
    private String path;
    private Integer maxAge;
    private Integer version;
    private boolean secure;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof Cookie) {
            result = this.getName().equals(((Cookie) obj).getName());
        }
        return result;
    }

    @Override
    public String toString() {
        Strings.Builder result = new Strings.Builder();
        result.append(getName()).append(Strings.ASSIGNATION).append(getValue());

        if(getComment() != null) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(COMMENT).append(Strings.ASSIGNATION).append(getComment());
        }

        if(getDomain() != null) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(DOMAIN).append(Strings.ASSIGNATION).append(getDomain());
        }

        if(getMaxAge() != null) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(MAX_AGE).append(Strings.ASSIGNATION).append(getMaxAge());
        }

        if(getPath() != null) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(PATH).append(Strings.ASSIGNATION).append(getPath());
        }

        if(isSecure()) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(SECURE);
        }

        if(getVersion() != null) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(VERSION).append(Strings.ASSIGNATION).append(getVersion());
        }

        return result.toString();
    }

}
