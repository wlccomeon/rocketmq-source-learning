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
package org.apache.rocketmq.example.transaction;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionListenerImpl implements TransactionListener {
    private AtomicInteger transactionIndex = new AtomicInteger(0);

    private ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();

    /**
     * 如果half消息发送成功了，就会在这里回调函数，然后执行本地事务了
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        //执行本地事务
        //接着根据本地一连串事务执行结果，去选择执行rollback或者commit
        try {
            //如果本地事务都执行成功了，则返回commit
            return LocalTransactionState.COMMIT_MESSAGE;
        }catch (Exception e){
            //如果本地事务执行失败，回滚所有一切执行过的操作
            //如果本地事务执行失败了，返回rollback，标记half消息无效
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
//        int value = transactionIndex.getAndIncrement();
//        int status = value % 3;
//        localTrans.put(msg.getTransactionId(), status);
//        return LocalTransactionState.UNKNOW;
    }

    /**
     * 如果因为各种原因，没有返回commit或者rollback
     * @param msg Check message
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        //查询本地事务，是否执行成功了
        Integer status = localTrans.get(msg.getTransactionId());
        //根据本地事务的情况，去选择执行rollback或commit
        if (null != status) {
            switch (status) {
                case 0:
                    return LocalTransactionState.UNKNOW;
                case 1:
                    return LocalTransactionState.COMMIT_MESSAGE;
                case 2:
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                default:
                    return LocalTransactionState.COMMIT_MESSAGE;
            }
        }
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
