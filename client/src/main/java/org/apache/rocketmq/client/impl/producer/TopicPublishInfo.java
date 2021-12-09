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
package org.apache.rocketmq.client.impl.producer;

import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.client.common.ThreadLocalIndex;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;

/**
 * @author itlemon
 */
public class TopicPublishInfo {
    /**
     * 是否是顺序消息
     */
    private boolean orderTopic = false;

    /**
     * 该topic是否是否含有路由信息
     */
    private boolean haveTopicRouterInfo = false;

    /**
     * 消息队列列表
     */
    private List<MessageQueue> messageQueueList = new ArrayList<>();

    /**
     * 用于消息队列选择的索引标识，每次选择一次队列，该值会增加1，当超过Integer.MAX_VALUE后，重置为0，重头开始
     */
    private volatile ThreadLocalIndex sendWhichQueue = new ThreadLocalIndex();

    /**
     * Topic路由数据，具体包含：QueueDataList、BrokerDataList以及Broker上的服务过滤器的地址列表等
     */
    private TopicRouteData topicRouteData;

    public boolean isOrderTopic() {
        return orderTopic;
    }

    public void setOrderTopic(boolean orderTopic) {
        this.orderTopic = orderTopic;
    }

    public boolean ok() {
        return null != this.messageQueueList && !this.messageQueueList.isEmpty();
    }

    public List<MessageQueue> getMessageQueueList() {
        return messageQueueList;
    }

    public void setMessageQueueList(List<MessageQueue> messageQueueList) {
        this.messageQueueList = messageQueueList;
    }

    public ThreadLocalIndex getSendWhichQueue() {
        return sendWhichQueue;
    }

    public void setSendWhichQueue(ThreadLocalIndex sendWhichQueue) {
        this.sendWhichQueue = sendWhichQueue;
    }

    public boolean isHaveTopicRouterInfo() {
        return haveTopicRouterInfo;
    }

    public void setHaveTopicRouterInfo(boolean haveTopicRouterInfo) {
        this.haveTopicRouterInfo = haveTopicRouterInfo;
    }

    /**
     * 选择一个消息队列
     *
     * @param lastBrokerName 上一次发送失败的Broker名称
     * @return 消息队列
     */
    public MessageQueue selectOneMessageQueue(final String lastBrokerName) {
        if (lastBrokerName == null) {
            // 直接按照轮询的形式选择队列
            return selectOneMessageQueue();
        } else {
            // 如果上一次发送消息失败，那么在这类将规避掉失败的Broker上的消息队列，因为上一次失败，本次失败的概率还是很大的
            int index = this.sendWhichQueue.getAndIncrement();
            for (int i = 0; i < this.messageQueueList.size(); i++) {
                int pos = Math.abs(index++) % this.messageQueueList.size();
                if (pos < 0) {
                    pos = 0;
                }
                MessageQueue mq = this.messageQueueList.get(pos);
                // 在这里进行了合理的规避，只要选择的MessageQueue的BrokerName和上一次失败的一样，那么就继续看下一个MessageQueue
                if (!mq.getBrokerName().equals(lastBrokerName)) {
                    return mq;
                }
            }
            // 上面的规避还是有可能获取不到合适的MessageQueue的，比如当只有一个Broker的时候，那么MessageQueueList中全部都是
            // 这个Broker上的MessageQueue，所以这里还会继续兜底，按照普通轮询的方式选择一个消息队列。
            return selectOneMessageQueue();
        }
    }

    /**
     * 从列表中获取一个MessageQueue
     *
     * @return MessageQueue
     */
    public MessageQueue selectOneMessageQueue() {
        // 这里的index，每获取一次，将内部递增一次，为了是保证在选择队列的时候是以轮询的形式进行
        int index = this.sendWhichQueue.getAndIncrement();
        int pos = Math.abs(index) % this.messageQueueList.size();
        if (pos < 0) {
            pos = 0;
        }
        return this.messageQueueList.get(pos);
    }

    public int getQueueIdByBroker(final String brokerName) {
        for (int i = 0; i < topicRouteData.getQueueDatas().size(); i++) {
            final QueueData queueData = this.topicRouteData.getQueueDatas().get(i);
            if (queueData.getBrokerName().equals(brokerName)) {
                return queueData.getWriteQueueNums();
            }
        }

        return -1;
    }

    @Override
    public String toString() {
        return "TopicPublishInfo [orderTopic=" + orderTopic + ", messageQueueList=" + messageQueueList
                + ", sendWhichQueue=" + sendWhichQueue + ", haveTopicRouterInfo=" + haveTopicRouterInfo + "]";
    }

    public TopicRouteData getTopicRouteData() {
        return topicRouteData;
    }

    public void setTopicRouteData(final TopicRouteData topicRouteData) {
        this.topicRouteData = topicRouteData;
    }
}
