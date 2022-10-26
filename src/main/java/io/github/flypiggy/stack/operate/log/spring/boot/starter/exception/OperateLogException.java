package io.github.flypiggy.stack.operate.log.spring.boot.starter.exception;

public class OperateLogException extends RuntimeException {

    public OperateLogException() {
        super();
    }

    public OperateLogException(String msg) {
        super(msg);
    }

    public OperateLogException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
