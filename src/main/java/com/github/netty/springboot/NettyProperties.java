package com.github.netty.springboot;

import com.github.netty.core.util.ApplicationX;
import com.github.netty.protocol.DynamicProtocolChannelHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.util.ResourceLeakDetector;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * You can configure it here
 * @author wangzihao
 * 2018/8/25/025
 */
@ConfigurationProperties(prefix = "server.netty", ignoreUnknownFields = true)
public class NettyProperties implements Serializable{
    private static final long serialVersionUID = 1L;

    /**
     * 服务端 - TCP级别最大同时在线的连接数
     */
    private int maxConnections = 10000;
    /**
     * 服务端 - 是否tcp数据包日志
     */
    private boolean enableTcpPackageLog = false;
    /**
     * 服务端 - 第一个客户端包的超时时间 (毫秒)
     */
    private long firstClientPacketReadTimeoutMs = 800;
    /**
     * 服务端 - tcp数据包日志等级(需要先开启tcp数据包日志)
     */
    private LogLevel tcpPackageLogLevel = LogLevel.DEBUG;

    /**
     * 服务端-IO线程数  注: (0 = cpu核数 * 2 )
     */
    private int serverIoThreads = 50;
    /**
     * 服务端-io线程执行调度与执行io事件的百分比. 注:(100=每次只执行一次调度工作, 其他都执行io事件), 并发高的时候可以设置最大
     */
    private int serverIoRatio = 100;

    /**
     * 是否禁用Nagle算法，true=禁用Nagle算法. 即数据包立即发送出去 (在TCP_NODELAY模式下，假设有3个小包要发送，第一个小包发出后，接下来的小包需要等待之前的小包被ack，在这期间小包会合并，直到接收到之前包的ack后才会发生)
     */
    private boolean tcpNodelay = false;

    /**
     * netty的内存泄漏检测级别(调试程序的时候用). 默认禁用, 不然极其耗费性能
     */
    private ResourceLeakDetector.Level resourceLeakDetectorLevel = ResourceLeakDetector.Level.DISABLED;

    /**
     * 动态协议处理器,是在进入所有协议之前的入口- 使用者可以继承它加入自己的逻辑 比如:(处理超出最大tcp连接数时的逻辑, 处理遇到不支持的协议时的逻辑等..)
     */
    private Class<?extends DynamicProtocolChannelHandler> channelHandler = DynamicProtocolChannelHandler.class;

    /**
     * HTTP协议(Servlet实现)
     */
    @NestedConfigurationProperty
    private final HttpServlet httpServlet = new HttpServlet();

    /**
     * 全局对象(类似spring容器)
     */
    private transient final ApplicationX application = new ApplicationX();

    public NettyProperties() {}

    public ApplicationX getApplication() {
        return application;
    }

    public boolean isTcpNodelay() {
        return tcpNodelay;
    }

    public void setTcpNodelay(boolean tcpNodelay) {
        this.tcpNodelay = tcpNodelay;
    }

    public ResourceLeakDetector.Level getResourceLeakDetectorLevel() {
        return resourceLeakDetectorLevel;
    }

    public void setResourceLeakDetectorLevel(ResourceLeakDetector.Level resourceLeakDetectorLevel) {
        this.resourceLeakDetectorLevel = resourceLeakDetectorLevel;
    }

    public long getFirstClientPacketReadTimeoutMs() {
        return firstClientPacketReadTimeoutMs;
    }

    public void setFirstClientPacketReadTimeoutMs(long firstClientPacketReadTimeoutMs) {
        this.firstClientPacketReadTimeoutMs = firstClientPacketReadTimeoutMs;
    }

    public Class<?extends DynamicProtocolChannelHandler> getChannelHandler() {
        return channelHandler;
    }

    public void setChannelHandler(Class<?extends DynamicProtocolChannelHandler> channelHandler) {
        this.channelHandler = channelHandler;
    }

    public LogLevel getTcpPackageLogLevel() {
        return tcpPackageLogLevel;
    }

    public void setTcpPackageLogLevel(LogLevel tcpPackageLogLevel) {
        this.tcpPackageLogLevel = tcpPackageLogLevel;
    }

    public int getServerIoThreads() {
        return serverIoThreads;
    }

    public void setServerIoThreads(int serverIoThreads) {
        this.serverIoThreads = serverIoThreads;
    }

    public int getServerIoRatio() {
        return serverIoRatio;
    }

    public void setServerIoRatio(int serverIoRatio) {
        this.serverIoRatio = serverIoRatio;
    }

    public boolean isEnableTcpPackageLog() {
        return enableTcpPackageLog;
    }

    public void setEnableTcpPackageLog(boolean enableTcpPackageLog) {
        this.enableTcpPackageLog = enableTcpPackageLog;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }


    public HttpServlet getHttpServlet() {
        return httpServlet;
    }


    public static class HttpServlet{
        /**
         * 请求体最大字节
         */
        private int requestMaxContentSize = 20 * 1024 * 1024;
        /**
         * 请求头每行最大字节
         */
        private int requestMaxHeaderLineSize = 40960;
        /**
         * 请求头最大字节
         */
        private int requestMaxHeaderSize = 81920;
        /**
         * 请求分块传输的每段上限
         */
        private int requestMaxChunkSize = 5 * 1024 * 1024;
        /**
         * 响应最大缓冲区大小（超过这个大小，会触发flush方法，发送给网络并清空缓冲区）
         */
        private int responseMaxBufferSize = 1024 * 1024;
        /**
         * 服务端 - servlet线程执行器
         */
        private Class<?extends Executor> serverHandlerExecutor = null;
        /**
         * 服务端 - servlet3异步特性。 异步dispatch的线程执行器 (默认用的是netty的IO线程) {@link #serverIoThreads}
         */
        private Class<?extends ExecutorService> asyncExecutorService = null;
        /**
         * 服务端 - servlet3的异步特性。 异步回调是否切换至新的线程执行任务, 如果没有异步嵌套异步的情况,建议开启.因为只有给前端写数据的IO损耗.
         * (设置false会减少一次线程切换, 用回调方的线程执行. 提示:tomcat是true，用新线程执行)
         */
        private boolean asyncSwitchThread = true;
        /**
         * session存储 - 是否开启本地文件存储
         */
        private boolean enablesLocalFileSession = false;

        /**
         * session存储 - session远程存储的url地址, 注: 如果不设置就不会开启
         */
        private String sessionRemoteServerAddress;

        /**
         * 每次调用servlet的 OutputStream.Writer()方法写入的最大堆字节,超出后用堆外内存
         */
        private int responseWriterChunkMaxHeapByteLength = 4096 * 5;

        /**
         * servlet文件存储的根目录。(servlet文件上传下载) 如果未指定，则使用临时目录。
         */
        private File basedir;

        /**
         * 是否开启DNS地址查询. true=开启 {@link javax.servlet.ServletRequest#getRemoteHost}
         */
        private boolean enableNsLookup = false;


        public int getResponseMaxBufferSize() {
            return responseMaxBufferSize;
        }

        public void setResponseMaxBufferSize(int responseMaxBufferSize) {
            this.responseMaxBufferSize = responseMaxBufferSize;
        }

        public boolean isAsyncSwitchThread() {
            return asyncSwitchThread;
        }

        public void setAsyncSwitchThread(boolean asyncSwitchThread) {
            this.asyncSwitchThread = asyncSwitchThread;
        }

        public Class<? extends ExecutorService> getAsyncExecutorService() {
            return asyncExecutorService;
        }

        public void setAsyncExecutorService(Class<? extends ExecutorService> asyncExecutorService) {
            this.asyncExecutorService = asyncExecutorService;
        }

        public boolean isEnableNsLookup() {
            return enableNsLookup;
        }

        public void setEnableNsLookup(boolean enableNsLookup) {
            this.enableNsLookup = enableNsLookup;
        }

        public int getRequestMaxContentSize() {
            return requestMaxContentSize;
        }

        public void setRequestMaxContentSize(int requestMaxContentSize) {
            this.requestMaxContentSize = requestMaxContentSize;
        }

        public int getRequestMaxHeaderLineSize() {
            return requestMaxHeaderLineSize;
        }

        public void setRequestMaxHeaderLineSize(int requestMaxHeaderLineSize) {
            this.requestMaxHeaderLineSize = requestMaxHeaderLineSize;
        }

        public int getRequestMaxHeaderSize() {
            return requestMaxHeaderSize;
        }

        public void setRequestMaxHeaderSize(int requestMaxHeaderSize) {
            this.requestMaxHeaderSize = requestMaxHeaderSize;
        }

        public int getRequestMaxChunkSize() {
            return requestMaxChunkSize;
        }

        public void setRequestMaxChunkSize(int requestMaxChunkSize) {
            this.requestMaxChunkSize = requestMaxChunkSize;
        }

        public Class<?extends Executor> getServerHandlerExecutor() {
            return serverHandlerExecutor;
        }

        public void setServerHandlerExecutor(Class<?extends Executor> serverHandlerExecutor) {
            this.serverHandlerExecutor = serverHandlerExecutor;
        }

        public boolean isEnablesLocalFileSession() {
            return enablesLocalFileSession;
        }

        public void setEnablesLocalFileSession(boolean enablesLocalFileSession) {
            this.enablesLocalFileSession = enablesLocalFileSession;
        }

        public String getSessionRemoteServerAddress() {
            return sessionRemoteServerAddress;
        }

        public void setSessionRemoteServerAddress(String sessionRemoteServerAddress) {
            this.sessionRemoteServerAddress = sessionRemoteServerAddress;
        }

        public int getResponseWriterChunkMaxHeapByteLength() {
            return responseWriterChunkMaxHeapByteLength;
        }

        public void setResponseWriterChunkMaxHeapByteLength(int responseWriterChunkMaxHeapByteLength) {
            this.responseWriterChunkMaxHeapByteLength = responseWriterChunkMaxHeapByteLength;
        }

        public File getBasedir() {
            return basedir;
        }

        public void setBasedir(File basedir) {
            this.basedir = basedir;
        }
    }

}
