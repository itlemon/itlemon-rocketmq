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
package org.apache.rocketmq.common.message;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * RocketMQ消息结构
 *
 * @author itlemon
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 8445773977080406428L;

    /**
     * 主题名字，可以通过RocketMQ Console创建
     */
    private String topic;

    /**
     * 未知用途
     */
    private int flag;

    /**
     * 该字段为一个HashMap，存储了Message其余各项参数，比如tag、keys等关键的消息属性。RocketMQ
     * 预定义了一组内置属性，除了内置属性之外，还可以设置任意自定义属性。当然属性的数量也是有限的，
     * 消息序列化之后的大小不能超过预设的最大消息大小。
     */
    private Map<String, String> properties;

    /**
     * 消息体，字节数组，注意一点：生产者和消费者必须保持一致的编码，比如UTF-8。
     * Producer要发送的实际消息内容，以字节数组形式进行存储。Message消息有一定大小限制。
     */
    private byte[] body;

    /**
     * RocketMQ 4.3.0引入的事务消息相关的事务编号。
     */
    private String transactionId;

    public Message() {
    }

    public Message(String topic, byte[] body) {
        this(topic, "", "", 0, body, true);
    }

    public Message(String topic, String tags, String keys, int flag, byte[] body, boolean waitStoreMsgOK) {
        this.topic = topic;
        this.flag = flag;
        this.body = body;

        if (tags != null && tags.length() > 0) {
            // 设置消息标签，方便后期消费者订阅特定标签
            this.setTags(tags);
        }

        if (keys != null && keys.length() > 0) {
            this.setKeys(keys);
        }

        this.setWaitStoreMsgOK(waitStoreMsgOK);
    }

    public Message(String topic, String tags, byte[] body) {
        this(topic, tags, "", 0, body, true);
    }

    public Message(String topic, String tags, String keys, byte[] body) {
        this(topic, tags, keys, 0, body, true);
    }

    /**
     * 可以设置业务相关标识，用于消费处理判定，或消息追踪查询
     * 多个key可以使用{@link MessageConst#KEY_SEPARATOR} 空格隔开，或者使用{@link Message#setKeys(Collection)}
     * PS：当Broker中messageIndexEnable=true，则key用于创建消息的Hash索引，帮助用户快速查询消息。
     */
    public void setKeys(String keys) {
        this.putProperty(MessageConst.PROPERTY_KEYS, keys);
    }

    /**
     * 向properties中存储键值对
     *
     * @param name 键
     * @param value 值
     */
    void putProperty(final String name, final String value) {
        if (null == this.properties) {
            // 第一次存储属性时候初始化，不存储的时候，Message类中properties为null
            this.properties = new HashMap<>();
        }

        this.properties.put(name, value);
    }

    /**
     * 指定清除properties中存储键值对
     *
     * @param name 键
     */
    void clearProperty(final String name) {
        if (null != this.properties) {
            this.properties.remove(name);
        }
    }

    /**
     * 向properties中存储用户自定义键值对。
     * 要求：键不能是{@link MessageConst#STRING_HASH_SET}中包含的键，且键和值不都不能为空
     *
     * @param name 键
     * @param value 值
     */
    public void putUserProperty(final String name, final String value) {
        if (MessageConst.STRING_HASH_SET.contains(name)) {
            throw new RuntimeException(String.format(
                    "The Property<%s> is used by system, input another please", name));
        }

        if (value == null || value.trim().isEmpty()
                || name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "The name or value of property can not be null or blank string!"
            );
        }

        this.putProperty(name, value);
    }

    /**
     * 获取该Message的用户自定义属性值
     *
     * @param name 键
     * @return 属性值
     */
    public String getUserProperty(final String name) {
        return this.getProperty(name);
    }

    public String getProperty(final String name) {
        // 只要触及properties。都要检查其是否初始化，如果没有，则进行初始化
        if (null == this.properties) {
            this.properties = new HashMap<>();
        }

        return this.properties.get(name);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 获取消息中的标签
     *
     * @return 消息标签
     */
    public String getTags() {
        return this.getProperty(MessageConst.PROPERTY_TAGS);
    }

    /**
     * 设置当前消息的标签，在消费消息时可以通过tag进行消息过滤判定
     */
    public void setTags(String tags) {
        this.putProperty(MessageConst.PROPERTY_TAGS, tags);
    }

    public String getKeys() {
        return this.getProperty(MessageConst.PROPERTY_KEYS);
    }

    /**
     * {@link Message#setKeys(String)} 的重载方法，用于设置多个key，用空格分隔
     * PS：当Broker中messageIndexEnable=true，则key用于创建消息的Hash索引，帮助用户快速查询消息。
     */
    public void setKeys(Collection<String> keys) {
        StringBuffer sb = new StringBuffer();
        for (String k : keys) {
            sb.append(k).append(MessageConst.KEY_SEPARATOR);
        }

        this.setKeys(sb.toString().trim());
    }

    /**
     * 获取延迟时间级别，默认为0
     *
     * @return 延迟级别
     */
    public int getDelayTimeLevel() {
        String t = this.getProperty(MessageConst.PROPERTY_DELAY_TIME_LEVEL);
        if (t != null) {
            return Integer.parseInt(t);
        }

        return 0;
    }

    /**
     * 消息延迟处理级别，不同级别对应不同延迟时间
     *
     * @param level 消息延迟级别
     */
    public void setDelayTimeLevel(int level) {
        this.putProperty(MessageConst.PROPERTY_DELAY_TIME_LEVEL, String.valueOf(level));
    }

    /**
     * 在同步刷盘情况下是否需要等待数据落地才认为消息发送成功，默认为true
     *
     * @return Boolean
     */
    public boolean isWaitStoreMsgOK() {
        String result = this.getProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK);
        if (null == result) {
            return true;
        }

        return Boolean.parseBoolean(result);
    }

    /**
     * 在同步刷盘情况下是否需要等待数据落地才认为消息发送成功
     *
     * @param waitStoreMsgOK boolean
     */
    public void setWaitStoreMsgOK(boolean waitStoreMsgOK) {
        this.putProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK, Boolean.toString(waitStoreMsgOK));
    }

    public void setInstanceId(String instanceId) {
        this.putProperty(MessageConst.PROPERTY_INSTANCE_ID, instanceId);
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getBuyerId() {
        return getProperty(MessageConst.PROPERTY_BUYER_ID);
    }

    public void setBuyerId(String buyerId) {
        putProperty(MessageConst.PROPERTY_BUYER_ID, buyerId);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "topic='" + topic + '\'' +
                ", flag=" + flag +
                ", properties=" + properties +
                ", body=" + Arrays.toString(body) +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
