package com.github.netty.springboot;

import com.github.netty.core.ProtocolHandler;
import com.github.netty.core.ServerListener;
import com.github.netty.protocol.DynamicProtocolChannelHandler;
import com.github.netty.protocol.HttpServletProtocol;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * The netty container is automatically configured
 * @author wangzihao
 */
@Configuration
@AutoConfigureAfter(NettyProperties.class)
@EnableConfigurationProperties(NettyProperties.class)
public class NettyEmbeddedAutoConfiguration {
    private final NettyProperties nettyProperties;
    public NettyEmbeddedAutoConfiguration(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    /**
     * Add a TCP service factory
     * @param protocolHandlers protocolHandlers
     * @param serverListeners serverListeners
     * @param beanFactory beanFactory
     * @return NettyTcpServerFactory
     */
    @Bean("nettyServerFactory")
    @ConditionalOnMissingBean(NettyTcpServerFactory.class)
    public NettyTcpServerFactory nettyTcpServerFactory(Collection<ProtocolHandler> protocolHandlers,
                                                       Collection<ServerListener> serverListeners,
                                                       BeanFactory beanFactory){
        Supplier<DynamicProtocolChannelHandler> handlerSupplier = ()->{
            Class<?extends DynamicProtocolChannelHandler> type = nettyProperties.getChannelHandler();
            return type == DynamicProtocolChannelHandler.class?
                    new DynamicProtocolChannelHandler() : beanFactory.getBean(type);
        };
        NettyTcpServerFactory tcpServerFactory = new NettyTcpServerFactory(nettyProperties,handlerSupplier);
        tcpServerFactory.getProtocolHandlers().addAll(protocolHandlers);
        tcpServerFactory.getServerListeners().addAll(serverListeners);
        return tcpServerFactory;
    }

    /**
     * Add the HTTP protocol registry
     * @param factory factory
     * @param resourceLoader resourceLoader
     * @return HttpServletProtocol
     */
    @Bean("httpServletProtocol")
    @ConditionalOnMissingBean(HttpServletProtocol.class)
    public HttpServletProtocol httpServletProtocol(ConfigurableBeanFactory factory, ResourceLoader resourceLoader) {
        Class<? extends Executor> serverHandlerExecutorClass = nettyProperties.getHttpServlet().getServerHandlerExecutor();
        Supplier<Executor> serverHandlerExecutor = null;
        if(serverHandlerExecutorClass != null){
            serverHandlerExecutor = () -> factory.getBean(serverHandlerExecutorClass);
        }

        HttpServletProtocolSpringAdapter protocol = new HttpServletProtocolSpringAdapter(nettyProperties,serverHandlerExecutor,resourceLoader.getClassLoader());
        NettyProperties.HttpServlet http = nettyProperties.getHttpServlet();
        protocol.setMaxInitialLineLength(http.getRequestMaxHeaderLineSize());
        protocol.setMaxHeaderSize(http.getRequestMaxHeaderSize());
        protocol.setMaxContentLength(http.getRequestMaxContentSize());
        protocol.setMaxChunkSize(http.getRequestMaxChunkSize());
        protocol.setMaxBufferBytes(http.getResponseMaxBufferSize());

        factory.addBeanPostProcessor(protocol);
        return protocol;
    }


}
