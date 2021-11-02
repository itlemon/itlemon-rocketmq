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
package org.apache.rocketmq.example.quickstart;

import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * This class demonstrates how to send messages to brokers using provided {@link DefaultMQProducer}.
 *
 * @author itlemon
 */
public class Producer {
    public static void main(String[] args) throws MQClientException, InterruptedException {

        // 构建生产者实例，这里使用默认的topic：TBW102
        DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
        // 设置namesever地址
        producer.setNamesrvAddr("127.0.0.1:4444");
        // 启动生产者
        producer.start();

        // 构建消息体，发送消息
        for (int i = 0; i < 1000; i++) {
            try {
                Message msg = new Message("TopicTest", "TagA",
                        ("Hello RocketMQ " + i).getBytes(StandardCharsets.UTF_8)
                );

                SendResult sendResult = producer.send(msg);

                System.out.printf("%s%n", sendResult);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }
        // 关闭生产者
        producer.shutdown();
    }
}
