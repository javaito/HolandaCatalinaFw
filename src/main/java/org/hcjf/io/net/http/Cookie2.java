package org.hcjf.io.net.http;

import org.hcjf.utils.Strings;

/**
 * @author javaito
 */
public class Cookie2 extends Cookie {

    public static final String COMMENT_URL = "CommentURL";
    public static final String PORT = "PORT";
    public static final String DISCARD = "Discard";

    private String commentUrl;
    private Integer port;
    private boolean discard;

    public Cookie2(String name, String value) {
        super(name, value);
    }

    public String getCommentUrl() {
        return commentUrl;
    }

    public void setCommentUrl(String commentUrl) {
        this.commentUrl = commentUrl;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isDiscard() {
        return discard;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    @Override
    public String toString() {
        Strings.Builder result = new Strings.Builder();
        result.append(super.toString());

        if(getCommentUrl() != null) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(COMMENT_URL).append(Strings.ASSIGNATION).append(getCommentUrl());
        }

        if(getPort() != null) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(PORT).append(Strings.ASSIGNATION).append(getPort());
        }

        if(isDiscard()) {
            result.append(Strings.ARGUMENT_SEPARATOR_2).append(Strings.WHITE_SPACE);
            result.append(DISCARD);
        }

        return result.toString();
    }

}
