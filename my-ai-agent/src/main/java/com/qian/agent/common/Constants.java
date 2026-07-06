package com.qian.agent.common;

public final class Constants {

    private Constants() {
    }

    public static final String REDIS_SESSION_PREFIX = "agent:session:";
    public static final String REDIS_RATE_LIMIT_PREFIX = "agent:ratelimit:";

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_TOOL = "tool";

    public static final String DEFAULT_SESSION_TITLE = "新对话";
    public static final String DEFAULT_MODEL = "deepseek-chat";
}
