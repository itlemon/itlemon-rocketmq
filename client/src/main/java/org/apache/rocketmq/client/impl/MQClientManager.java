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
package org.apache.rocketmq.client.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.remoting.RPCHook;

/**
 * @author itlemon
 */
public class MQClientManager {
    private static final InternalLogger LOG = ClientLogger.getLog();
    private static final MQClientManager INSTANCE = new MQClientManager();
    private final AtomicInteger factoryIndexGenerator = new AtomicInteger();
    private final ConcurrentMap<String/* clientId */, MQClientInstance> factoryTable =
            new ConcurrentHashMap<>();

    private MQClientManager() {

    }

    public static MQClientManager getInstance() {
        return INSTANCE;
    }

    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig) {
        return getOrCreateMQClientInstance(clientConfig, null);
    }

    /**
     * 获取或者创建一个MQClientInstance对象
     *
     * @param clientConfig 客户端配置
     * @param rpcHook RPC Hook
     * @return MQClientInstance对象
     */
    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig, RPCHook rpcHook) {
        // 构建客户端ID，格式clientIP@PID@uintName，uintName为可选部分，设置了则会拼接
        String clientId = clientConfig.buildMQClientId();
        // 首先通过clientID来获取MQClientInstance对象，获取不到将进行创建，完成创建后存储到Map中
        MQClientInstance instance = this.factoryTable.get(clientId);
        if (null == instance) {
            instance =
                    new MQClientInstance(clientConfig.cloneClientConfig(),
                            this.factoryIndexGenerator.getAndIncrement(), clientId, rpcHook);
            MQClientInstance prev = this.factoryTable.putIfAbsent(clientId, instance);
            if (prev != null) {
                instance = prev;
                LOG.warn("Returned Previous MQClientInstance for clientId:[{}]", clientId);
            } else {
                LOG.info("Created new MQClientInstance for clientId:[{}]", clientId);
            }
        }

        return instance;
    }

    public void removeClientFactory(final String clientId) {
        this.factoryTable.remove(clientId);
    }
}
