package org.flypiggy.operate.log.spring.boot.starter.context;

public class LogOperatorContext {

    private static final ThreadLocal<String> REQUEST_CONTEXT = new ThreadLocal<>();

    private LogOperatorContext() {
    }

    public static void set(String context) {
        REQUEST_CONTEXT.set(context);
    }

    public static String get() {
        return REQUEST_CONTEXT.get();
    }

    public static void remove() {
        REQUEST_CONTEXT.remove();
    }

}
