package io.github.flypiggy.stack.operate.log.spring.boot.starter.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Log {
}
