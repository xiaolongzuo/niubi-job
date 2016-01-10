/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zuoxiaolong.niubi.job.cluster.node;

import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.config.Configuration;
import com.zuoxiaolong.niubi.job.core.container.Container;
import com.zuoxiaolong.niubi.job.core.container.DefaultContainer;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集群节点的curator实现,可以保证同一时间有且仅有一个节点在运行job
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 14:43
 */
public class MasterSlaveNode implements Node {

    public static final String MASTER_SELECTOR_PATH = "/masterslaveleaderpath/masterselector";

    public static final String NODE_EPHEMERAL_PATH = "/masterslaveleaderpath/nodeephemeralpath";

    public static final String NODE_PERSISTENT_PATH = "/masterslaveleaderpath/nodepersistentpath";

    public static final String COUNTER_PATH = "/masterslaveleaderpath/counterpath";

    public static final String LOCK_PATH = "/masterslaveleaderpath/lockpath";

    private DistributedAtomicInteger distributedAtomicInteger;

    private InterProcessMutex interProcessMutex;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 6);

    private CuratorFramework client;

    private String name;

    private Container container;

    private LeaderSelector leaderSelector;

    private Integer nodeSequenceNumber;

    private String nodeEphemeralPath;

    private String nodePersistentPath;

    private PathChildrenCache persistentPathChildrenCache;

    private PathChildrenCache ephemeralNodeCache;

    public MasterSlaveNode(final Configuration configuration, String connectString) {
        createContainer(configuration);
        createClient(connectString);
        createDistributedProperties();
        createNodePath();
        createLeaderSelector();
        createNodeCache();
    }

    private void createContainer(Configuration configuration) {
        try {
            this.name = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            //ignored
        }
        this.container = new DefaultContainer(configuration);
    }

    private void createClient(String connectString) {
        this.client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        this.client.start();
    }

    private void createDistributedProperties() {
        this.distributedAtomicInteger = new DistributedAtomicInteger(client, COUNTER_PATH, retryPolicy);
        try {
            AtomicValue<Integer> value = this.distributedAtomicInteger.increment();
            if (value.succeeded()) {
                this.nodeSequenceNumber = value.postValue();
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new NiubiException(e);
        }
        this.interProcessMutex = new InterProcessMutex(client, LOCK_PATH);
    }

    private void createNodePath() {
        try {
            this.nodePersistentPath = this.client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT).forPath(ZKPaths.makePath(NODE_PERSISTENT_PATH, String.valueOf(nodeSequenceNumber)));
            this.nodeEphemeralPath = this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZKPaths.makePath(NODE_EPHEMERAL_PATH, this.nodePersistentPath.substring(this.nodePersistentPath.lastIndexOf("/") + 1)));
            this.persistentPathChildrenCache = new PathChildrenCache(client, this.nodePersistentPath, true);
            this.persistentPathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    if (event != null && event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                        container.getScheduleManager().startup(event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1));
                    } else if (event != null && event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                        container.getScheduleManager().shutdown(event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1));
                    }
                }
            });
            this.persistentPathChildrenCache.start();
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    private void createLeaderSelector() {

        final LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {

            private Object mutex = new Object();

            public void takeLeadership(CuratorFramework client) throws Exception {
                LoggerHelper.info(name + " has been leadership.");
                synchronized (mutex) {
                    intervalTakeLeadership(client);
                    mutex.wait();
                    LoggerHelper.info(name + " lost leadership.");
                }
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LoggerHelper.info(name + " state change [" + newState + "]");
                if (!newState.isConnected()) {
                    synchronized (mutex) {
                        mutex.notify();
                    }
                }
            }

        };

        this.leaderSelector = new LeaderSelector(client, MASTER_SELECTOR_PATH, listener);
        this.leaderSelector.autoRequeue();
    }

    private void createNodeCache() {
        ephemeralNodeCache = new PathChildrenCache(client, NODE_EPHEMERAL_PATH, true);
        ephemeralNodeCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                boolean isNotSelfNode = event != null && !event.getData().getPath().equals(nodeEphemeralPath);
                boolean isAddOrRemovedEvent = isNotSelfNode && (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED);
                if (leaderSelector.hasLeadership() && isAddOrRemovedEvent) {
                    intervalTakeLeadership(client);
                }
            }
        });
        try {
            ephemeralNodeCache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void intervalTakeLeadership(CuratorFramework client) throws Exception {
        try {
            //获取锁直到成功
            boolean acquire = false;
            while (!acquire) {
                try {
                    interProcessMutex.acquire();
                    acquire = true;
                } catch (Exception e) {
                    LoggerHelper.warn("acquire failed, do loop.", e);
                }
            }

            List<String> allGroupList = container.getScheduleManager().getGroupList();
            List<String> ephemeralNodeNameList = client.getChildren().forPath(NODE_EPHEMERAL_PATH);
            List<String> persistentNodeNameList = client.getChildren().forPath(NODE_PERSISTENT_PATH);

            clearInvalidPersistentNode(ephemeralNodeNameList, persistentNodeNameList);
            LoggerHelper.info("clear invalid persistent node(s) successfully.");

            Map<String, Integer> nodeScheduledGroupSizeMap = new HashMap<String, Integer>();
            Map<String, List<String>> nodeScheduledGroupListMap = new HashMap<String, List<String>>();

            fillNodeScheduledGroupCondition(ephemeralNodeNameList, nodeScheduledGroupSizeMap, nodeScheduledGroupListMap);
            LoggerHelper.info("fill node scheduled group condition successfully.");

            List<String> sortedEphemeralNodeNameList = sort(ephemeralNodeNameList, nodeScheduledGroupSizeMap);
            LoggerHelper.info("sort ephemeral node name list successfully.");

            Map<String, List<String>[]> needToAddOrDeleteGroupListMap = getNeedToAddOrDeleteGroupList(sortedEphemeralNodeNameList, allGroupList, nodeScheduledGroupSizeMap, nodeScheduledGroupListMap);

            for (String ephemeralNodeName : needToAddOrDeleteGroupListMap.keySet()) {
                List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(ephemeralNodeName);
                List<String> createList = needToAddOrDeleteGroupList[0];
                List<String> deleteList = needToAddOrDeleteGroupList[1];
                if (createList != null) {
                    for (String group : createList) {
                        try {
                            client.create().withMode(CreateMode.EPHEMERAL).forPath(ZKPaths.makePath(NODE_PERSISTENT_PATH + "/" + ephemeralNodeName, group));
                        } catch (Exception e) {
                            LoggerHelper.warn(e.getMessage());
                        }
                    }
                }
                if (deleteList != null) {
                    for (String group : deleteList) {
                        try {
                            client.delete().forPath(ZKPaths.makePath(NODE_PERSISTENT_PATH + "/" + ephemeralNodeName, group));
                        } catch (Exception e) {
                            LoggerHelper.warn(e.getMessage());
                        }
                    }
                }
            }
        } finally {
            interProcessMutex.release();
        }
    }

    private void clearInvalidPersistentNode(List<String> ephemeralNodeNameList, List<String> persistentNodeNameList) throws Exception {
        for (String persistentNodeName : persistentNodeNameList) {
            if (!ephemeralNodeNameList.contains(persistentNodeName)) {
                //TODO  改为异步
                client.delete().forPath(ZKPaths.makePath(NODE_PERSISTENT_PATH, persistentNodeName));
            }
        }
    }

    private void fillNodeScheduledGroupCondition(List<String> ephemeralNodeNameList, Map<String, Integer> nodeScheduledGroupSizeMap, Map<String, List<String>> nodeScheduledGroupListMap) {
        for (String ephemeralNodeName : ephemeralNodeNameList) {
            List<String> nodeGroupList;
            String persistentNodePath = ZKPaths.makePath(NODE_PERSISTENT_PATH, ephemeralNodeName);
            try {
                nodeGroupList = client.getChildren().forPath(persistentNodePath);
            } catch (Exception e) {
                LoggerHelper.error("get [" + persistentNodePath +"] children failed.", e);
                throw new NiubiException(e);
            }
            nodeScheduledGroupSizeMap.put(ephemeralNodeName, nodeGroupList.size());
            nodeScheduledGroupListMap.put(ephemeralNodeName, nodeGroupList);
        }
    }

    private List<String> sort(List<String> ephemeralNodeNameList, Map<String, Integer> nodeGroupSizeMap) {
        List<String> sortedEphemeralNodeNameList = new ArrayList<String>();
        for (int i = 0;i < ephemeralNodeNameList.size(); i++) {
            Integer currentSize = nodeGroupSizeMap.get(ephemeralNodeNameList.get(i));
            if (sortedEphemeralNodeNameList.size() == 0) {
                sortedEphemeralNodeNameList.add(ephemeralNodeNameList.get(i));
            } else {
                List<String> sortedChildrenCopy = new ArrayList<String>(sortedEphemeralNodeNameList);
                for (int j = 0;j < sortedChildrenCopy.size(); j++) {
                    if (currentSize < nodeGroupSizeMap.get(sortedChildrenCopy.get(j))) {
                        sortedEphemeralNodeNameList.add(j, ephemeralNodeNameList.get(i));
                        break;
                    } else if (j == sortedChildrenCopy.size() - 1) {
                        sortedEphemeralNodeNameList.add(ephemeralNodeNameList.get(i));
                    }
                }
            }
        }
        return sortedEphemeralNodeNameList;
    }

    private Map<String, List<String>[]> getNeedToAddOrDeleteGroupList(List<String> sortedEphemeralNodeNameList, List<String> allGroupList
            , Map<String, Integer> nodeScheduledGroupSizeMap, Map<String, List<String>> nodeScheduledGroupListMap) {
        List<String> allScheduledGroupList = new ArrayList<String>();
        for (List<String> nodeScheduledGroupList : nodeScheduledGroupListMap.values()) {
            allScheduledGroupList.addAll(nodeScheduledGroupList);
        }

        List<String> idleGroupList = new ArrayList<String>();
        for (String group : allGroupList) {
            if (!allScheduledGroupList.contains(group)) {
                idleGroupList.add(group);
            }
        }
        int groupSize = allGroupList.size();
        int nodeSize = sortedEphemeralNodeNameList.size();
        int averageGroupCount = (groupSize % nodeSize == 0) ? (groupSize / nodeSize) : (groupSize / nodeSize + 1);

        //index为0则是create列表,index为1则是delete列表
        Map<String, List<String>[]> needToAddOrDeleteGroupListMap = new HashMap<String, List<String>[]>();

        for (int i = 0;i < idleGroupList.size(); i++) {
            int index = i % nodeSize;
            List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(sortedEphemeralNodeNameList.get(index));
            if (needToAddOrDeleteGroupList == null) {
                needToAddOrDeleteGroupList = new List[2];
            }
            if (needToAddOrDeleteGroupList[0] == null) {
                needToAddOrDeleteGroupList[0] = new ArrayList<String>();
            }
            needToAddOrDeleteGroupList[0].add(idleGroupList.get(i));
            needToAddOrDeleteGroupListMap.put(sortedEphemeralNodeNameList.get(index), needToAddOrDeleteGroupList);
        }

        String exceedAverageGroupCountNode;
        while ((exceedAverageGroupCountNode = getExceedAverageGroupCountNode(sortedEphemeralNodeNameList, nodeScheduledGroupSizeMap, averageGroupCount)) != null) {
            List<String> nodeScheduledGroupList = nodeScheduledGroupListMap.get(exceedAverageGroupCountNode);
            int exceedCount = nodeScheduledGroupList.size() - averageGroupCount;
            for (int i = 0;i < exceedCount; i++) {
                //从列表头部开始清除
                String purgeGroup = nodeScheduledGroupList.get(0);
                nodeScheduledGroupList.remove(0);
                nodeScheduledGroupListMap.put(exceedAverageGroupCountNode, nodeScheduledGroupList);

                int index = i % nodeSize;
                String lessGroupCountNode = sortedEphemeralNodeNameList.get(index);
                if (lessGroupCountNode.equals(exceedAverageGroupCountNode)) {
                    lessGroupCountNode = sortedEphemeralNodeNameList.get(index - 1);
                }
                List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(lessGroupCountNode);
                if (needToAddOrDeleteGroupList == null) {
                    needToAddOrDeleteGroupList = new List[2];
                }
                if (needToAddOrDeleteGroupList[0] == null) {
                    needToAddOrDeleteGroupList[0] = new ArrayList<String>();
                }
                needToAddOrDeleteGroupList[0].add(purgeGroup);
                nodeScheduledGroupSizeMap.put(lessGroupCountNode, nodeScheduledGroupSizeMap.get(lessGroupCountNode) + 1);
                needToAddOrDeleteGroupListMap.put(lessGroupCountNode, needToAddOrDeleteGroupList);

                needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(exceedAverageGroupCountNode);
                if (needToAddOrDeleteGroupList == null) {
                    needToAddOrDeleteGroupList = new List[2];
                }
                if (needToAddOrDeleteGroupList[1] == null) {
                    needToAddOrDeleteGroupList[1] = new ArrayList<String>();
                }
                needToAddOrDeleteGroupList[1].add(purgeGroup);
                nodeScheduledGroupSizeMap.put(exceedAverageGroupCountNode, nodeScheduledGroupSizeMap.get(exceedAverageGroupCountNode) - 1);
                needToAddOrDeleteGroupListMap.put(exceedAverageGroupCountNode, needToAddOrDeleteGroupList);
            }
        }

        //将重复的删除和创建列表清除
        Map<String, List<String>[]> needToAddOrDeleteGroupListMapCopy = new HashMap<String, List<String>[]>(needToAddOrDeleteGroupListMap);
        for (String ephemeralNodeName : needToAddOrDeleteGroupListMapCopy.keySet()) {
            List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMapCopy.get(ephemeralNodeName);
            List<String> needToCreateGroupList = needToAddOrDeleteGroupList[0];
            List<String> needToDeleteGroupList = needToAddOrDeleteGroupList[1];
            if (needToCreateGroupList == null || needToDeleteGroupList == null) {
                continue;
            }
            List<String> intersectionList = new ArrayList<String>(needToCreateGroupList);
            intersectionList.retainAll(needToDeleteGroupList);
            needToCreateGroupList.removeAll(intersectionList);
            needToDeleteGroupList.removeAll(intersectionList);
            needToAddOrDeleteGroupList[0] = needToCreateGroupList;
            needToAddOrDeleteGroupList[1] = needToDeleteGroupList;
            needToAddOrDeleteGroupListMap.put(ephemeralNodeName, needToAddOrDeleteGroupList);
        }
        return needToAddOrDeleteGroupListMap;
    }

    private String getExceedAverageGroupCountNode(List<String> sortedEphemeralNodeNameList, Map<String, Integer> nodeScheduledGroupSizeMap, int averageGroupCount) {
        for (int i = sortedEphemeralNodeNameList.size() - 1;i >= 0;i--) {
            if (nodeScheduledGroupSizeMap.get(sortedEphemeralNodeNameList.get(i)) > averageGroupCount) {
                return sortedEphemeralNodeNameList.get(i);
            }
        }
        return null;
    }

    public Container getContainer() {
        return container;
    }

    public String getName() {
        return name;
    }

    public synchronized void join() {
        this.leaderSelector.start();
    }

    public synchronized void exit() {
        this.leaderSelector.close();
        this.client.close();
    }

}
