package org.flypiggy.operate.log.spring.boot.starter.exception;

public class OperateLogException extends RuntimeException {

    public OperateLogException() {
        super();
    }

    public OperateLogException(String msg) {
        super(msg);
    }
}
