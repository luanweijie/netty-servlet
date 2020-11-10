package com.github.netty.springboot;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@EnableConfigurationProperties(NettyProperties.class)
@Import({NettyEmbeddedAutoConfiguration.class})
public @interface EnableNettyEmbedded {

}
