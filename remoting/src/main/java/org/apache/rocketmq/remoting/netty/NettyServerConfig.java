/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.remoting.netty;

/**
 * Netty Server配置
 *
 * @author itlemon
 */
public class NettyServerConfig implements Cloneable {

    /**
     * Name Server监听的端口，在启动Name Server的时候会被设置为9876
     */
    private int listenPort = 8888;

    /**
     * 设置Netty的工作线程池线程数
     */
    private int serverWorkerThreads = 8;

    /**
     * Netty公共任务线程池线程个数，在RocketMQ中，根据不同的请求{@link org.apache.rocketmq.common.protocol.RequestCode}
     * 来创建不同的线程池来专门处理该类任务，比如发送消息，拉取消息等都有自己的线程池，如果某个请求类型没有自己专用的线程池，将由公共
     * 线程池来处理请求。
     */
    private int serverCallbackExecutorThreads = 0;

    /**
     * IO线程池线程个数，主要是Name Server和Broker解析请求，返回相应线程池的线程个数，该IO线程主要是用来处理网络请求，
     * 包括解析请求包数据，然后转发请求到各自的线程池处理具体逻辑，最后将处理后的结果返回给调用方完成请求处理。
     */
    private int serverSelectorThreads = 3;

    /**
     * 发送one way消息的请求并发数
     */
    private int serverOnewaySemaphoreValue = 256;

    /**
     * 发送异步消息最大并发数
     */
    private int serverAsyncSemaphoreValue = 64;

    /**
     * 网络连接最大空闲时间，超过该时间的连接将被关闭
     */
    private int serverChannelMaxIdleTimeSeconds = 120;

    /**
     * socket发送缓存区大小，可以通过-Dcom.rocketmq.remoting.socket.sndbuf.size=xxx来设置，默认值为65535B，也就是64K
     */
    private int serverSocketSndBufSize = NettySystemConfig.socketSndbufSize;

    /**
     * socket接收缓存区大小，可以通过-Dcom.rocketmq.remoting.socket.rcvbuf.size=xxx来设置，默认值为65535B，也就是64K
     */
    private int serverSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;

    /**
     * 是否开启ByteBuffer缓存，默认是开启
     */
    private boolean serverPooledByteBufAllocatorEnable = true;

    /**
     * 是否开启Epoll IO模型，在Linux环境下建议开启
     * make make install
     * <p>
     * <p>
     * ../glibc-2.10.1/configure \ --prefix=/usr \ --with-headers=/usr/include \
     * --host=x86_64-linux-gnu \ --build=x86_64-pc-linux-gnu \ --without-gd
     */
    private boolean useEpollNativeSelector = false;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getServerWorkerThreads() {
        return serverWorkerThreads;
    }

    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }

    public int getServerSelectorThreads() {
        return serverSelectorThreads;
    }

    public void setServerSelectorThreads(int serverSelectorThreads) {
        this.serverSelectorThreads = serverSelectorThreads;
    }

    public int getServerOnewaySemaphoreValue() {
        return serverOnewaySemaphoreValue;
    }

    public void setServerOnewaySemaphoreValue(int serverOnewaySemaphoreValue) {
        this.serverOnewaySemaphoreValue = serverOnewaySemaphoreValue;
    }

    public int getServerCallbackExecutorThreads() {
        return serverCallbackExecutorThreads;
    }

    public void setServerCallbackExecutorThreads(int serverCallbackExecutorThreads) {
        this.serverCallbackExecutorThreads = serverCallbackExecutorThreads;
    }

    public int getServerAsyncSemaphoreValue() {
        return serverAsyncSemaphoreValue;
    }

    public void setServerAsyncSemaphoreValue(int serverAsyncSemaphoreValue) {
        this.serverAsyncSemaphoreValue = serverAsyncSemaphoreValue;
    }

    public int getServerChannelMaxIdleTimeSeconds() {
        return serverChannelMaxIdleTimeSeconds;
    }

    public void setServerChannelMaxIdleTimeSeconds(int serverChannelMaxIdleTimeSeconds) {
        this.serverChannelMaxIdleTimeSeconds = serverChannelMaxIdleTimeSeconds;
    }

    public int getServerSocketSndBufSize() {
        return serverSocketSndBufSize;
    }

    public void setServerSocketSndBufSize(int serverSocketSndBufSize) {
        this.serverSocketSndBufSize = serverSocketSndBufSize;
    }

    public int getServerSocketRcvBufSize() {
        return serverSocketRcvBufSize;
    }

    public void setServerSocketRcvBufSize(int serverSocketRcvBufSize) {
        this.serverSocketRcvBufSize = serverSocketRcvBufSize;
    }

    public boolean isServerPooledByteBufAllocatorEnable() {
        return serverPooledByteBufAllocatorEnable;
    }

    public void setServerPooledByteBufAllocatorEnable(boolean serverPooledByteBufAllocatorEnable) {
        this.serverPooledByteBufAllocatorEnable = serverPooledByteBufAllocatorEnable;
    }

    public boolean isUseEpollNativeSelector() {
        return useEpollNativeSelector;
    }

    public void setUseEpollNativeSelector(boolean useEpollNativeSelector) {
        this.useEpollNativeSelector = useEpollNativeSelector;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (NettyServerConfig) super.clone();
    }
}
