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

import com.zuoxiaolong.niubi.job.api.ApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.ApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
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

import java.util.*;

/**
 * 主从模式实现
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 14:43
 */
public class MasterSlaveNode extends AbstractRemoteJobNode {

    private DistributedAtomicInteger distributedAtomicInteger;

    private InterProcessMutex interProcessMutex;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 6);

    private CuratorFramework client;

    private LeaderSelector leaderSelector;

    private Integer nodeSequenceNumber;

    private String nodeEphemeralPath;

    private String nodePersistentPath;

    private PathChildrenCache persistentPathChildrenCache;

    private PathChildrenCache ephemeralNodeCache;

    private ApiFactory apiFactory;

    public MasterSlaveNode(String zookeeperAddresses, String jarRepertoryUrl, String[] propertiesFileNames) {
        super(jarRepertoryUrl, propertiesFileNames);
        createClient(zookeeperAddresses);
        createDistributedProperties();
        createNodePath();
        createLeaderSelector();
        createNodeCache();
    }

    private void createClient(String zookeeperAddresses) {
        this.client = CuratorFrameworkFactory.newClient(zookeeperAddresses, retryPolicy);
        this.client.start();
        this.apiFactory = new ApiFactoryImpl(client);
    }

    private void createDistributedProperties() {
        this.distributedAtomicInteger = new DistributedAtomicInteger(client, apiFactory.pathApi().getMasterSlaveCounterPath(), retryPolicy);
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
        this.interProcessMutex = new InterProcessMutex(client, apiFactory.pathApi().getMasterSlaveLockPath());
    }

    private void createNodePath() {
        try {
            this.nodePersistentPath = this.client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT).forPath(ZKPaths.makePath(apiFactory.pathApi().getMasterSlavePersistentNodePath(), String.valueOf(nodeSequenceNumber)), StringHelper.getBytes(getIp()));
            this.nodeEphemeralPath = this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZKPaths.makePath(apiFactory.pathApi().getMasterSlaveEphemeralNodePath(), this.nodePersistentPath.substring(this.nodePersistentPath.lastIndexOf("/") + 1)));
            this.persistentPathChildrenCache = new PathChildrenCache(client, this.nodePersistentPath, true);
            this.persistentPathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    JobData jobData = new JobData(event.getData());
                    if (event != null && event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                        getContainer(jobData.getData().getJarFileName(), jobData.getData().getPackagesToScan(), jobData.getData().isSpring()).scheduleManager().startup(event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1));
                    } else if (event != null && event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                        getContainer(jobData.getData().getJarFileName(), jobData.getData().getPackagesToScan(), jobData.getData().isSpring()).scheduleManager().shutdown(event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1));
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
                LoggerHelper.info(getIp() + " has been leadership.");
                synchronized (mutex) {
                    intervalTakeLeadership(client);
                    mutex.wait();
                    LoggerHelper.info(getIp() + " lost leadership.");
                }
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LoggerHelper.info(getIp() + " state change [" + newState + "]");
                if (!newState.isConnected()) {
                    synchronized (mutex) {
                        mutex.notify();
                    }
                }
            }

        };

        this.leaderSelector = new LeaderSelector(client, apiFactory.pathApi().getMasterSlaveMasterSelectorPath(), listener);
        this.leaderSelector.autoRequeue();
    }

    private void createNodeCache() {
        ephemeralNodeCache = new PathChildrenCache(client, apiFactory.pathApi().getMasterSlaveEphemeralNodePath(), true);
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

            ContextManager contextManager = new ContextManager();
            LoggerHelper.info("init context manager successfully.");

            Map<String, List<String>[]> needToAddOrDeleteGroupListMap = getNeedToAddOrDeleteGroupList(contextManager);

            for (String ephemeralNodeName : needToAddOrDeleteGroupListMap.keySet()) {
                List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(ephemeralNodeName);
                List<String> createList = needToAddOrDeleteGroupList[0];
                List<String> deleteList = needToAddOrDeleteGroupList[1];
                if (createList != null) {
                    for (String group : createList) {
                        try {
                            client.create().withMode(CreateMode.EPHEMERAL).forPath(ZKPaths.makePath(apiFactory.pathApi().getMasterSlaveEphemeralNodePath() + "/" + ephemeralNodeName, group));
                        } catch (Exception e) {
                            LoggerHelper.warn(e.getMessage());
                        }
                    }
                }
                if (deleteList != null) {
                    for (String group : deleteList) {
                        try {
                            client.delete().forPath(ZKPaths.makePath(apiFactory.pathApi().getMasterSlavePersistentNodePath() + "/" + ephemeralNodeName, group));
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

    private Map<String, List<String>[]> getNeedToAddOrDeleteGroupList(ContextManager contextManager) {

        //index为0则是create列表,index为1则是delete列表
        Map<String, List<String>[]> needToAddOrDeleteGroupListMap = new HashMap<String, List<String>[]>();

        for (int i = 0;i < contextManager.getIdleGroupList().size(); i++) {
            int index = i % contextManager.getNodeSize();
            String group = contextManager.getIdleGroupList().get(i);
            String nodeName = contextManager.getSortedEphemeralNodeNameList().get(index);
            List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(nodeName);
            if (needToAddOrDeleteGroupList == null) {
                needToAddOrDeleteGroupList = new List[2];
            }
            if (needToAddOrDeleteGroupList[0] == null) {
                needToAddOrDeleteGroupList[0] = new ArrayList<String>();
            }
            needToAddOrDeleteGroupList[0].add(group);

            contextManager.addGroup(nodeName, group);
            needToAddOrDeleteGroupListMap.put(nodeName, needToAddOrDeleteGroupList);
        }

        String exceedAverageGroupCountNode;
        while ((exceedAverageGroupCountNode = contextManager.getExceedAverageGroupCountNode()) != null) {
            int currentCount = contextManager.getNodeScheduledGroupSizeMap().get(exceedAverageGroupCountNode);
            int exceedCount = currentCount - contextManager.getAverageGroupCount();
            for (int i = 0;i < exceedCount; i++) {
                //从列表头部开始清除
                String purgeGroup = contextManager.getNodeScheduledGroupListMap().get(exceedAverageGroupCountNode).get(0);

                int index = i % contextManager.getSortedEphemeralNodeNameList().indexOf(exceedAverageGroupCountNode);
                String lessGroupCountNode = contextManager.getSortedEphemeralNodeNameList().get(index);

                List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(lessGroupCountNode);
                if (needToAddOrDeleteGroupList == null) {
                    needToAddOrDeleteGroupList = new List[2];
                }
                if (needToAddOrDeleteGroupList[0] == null) {
                    needToAddOrDeleteGroupList[0] = new ArrayList<String>();
                }
                needToAddOrDeleteGroupList[0].add(purgeGroup);
                contextManager.addGroup(lessGroupCountNode, purgeGroup);
                needToAddOrDeleteGroupListMap.put(lessGroupCountNode, needToAddOrDeleteGroupList);

                needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(exceedAverageGroupCountNode);
                if (needToAddOrDeleteGroupList == null) {
                    needToAddOrDeleteGroupList = new List[2];
                }
                if (needToAddOrDeleteGroupList[1] == null) {
                    needToAddOrDeleteGroupList[1] = new ArrayList<String>();
                }
                needToAddOrDeleteGroupList[1].add(purgeGroup);
                contextManager.subGroup(exceedAverageGroupCountNode, purgeGroup);
                needToAddOrDeleteGroupListMap.put(exceedAverageGroupCountNode, needToAddOrDeleteGroupList);
            }
        }

        String lessMinimumStandardCountNode;
        while ((lessMinimumStandardCountNode = contextManager.getLessMinimumStandardCountNode()) != null) {
            //寻找一个等于平均值的节点
            String averageGroupCountNode = contextManager.getAverageGroupCountNode();
            //从列表头部开始清除
            String purgeGroup = contextManager.getNodeScheduledGroupListMap().get(averageGroupCountNode).get(0);

            List<String>[] needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(lessMinimumStandardCountNode);
            if (needToAddOrDeleteGroupList == null) {
                needToAddOrDeleteGroupList = new List[2];
            }
            if (needToAddOrDeleteGroupList[0] == null) {
                needToAddOrDeleteGroupList[0] = new ArrayList<String>();
            }
            needToAddOrDeleteGroupList[0].add(purgeGroup);
            contextManager.addGroup(lessMinimumStandardCountNode, purgeGroup);
            needToAddOrDeleteGroupListMap.put(lessMinimumStandardCountNode, needToAddOrDeleteGroupList);

            needToAddOrDeleteGroupList = needToAddOrDeleteGroupListMap.get(averageGroupCountNode);
            if (needToAddOrDeleteGroupList == null) {
                needToAddOrDeleteGroupList = new List[2];
            }
            if (needToAddOrDeleteGroupList[1] == null) {
                needToAddOrDeleteGroupList[1] = new ArrayList<String>();
            }
            needToAddOrDeleteGroupList[1].add(purgeGroup);
            contextManager.subGroup(averageGroupCountNode, purgeGroup);
            needToAddOrDeleteGroupListMap.put(averageGroupCountNode, needToAddOrDeleteGroupList);
        }

        //将重复的删除和创建列表清除
        Map<String, List<String>[]> needToAddOrDeleteGroupListMapCopy = Collections.unmodifiableMap(needToAddOrDeleteGroupListMap);
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

    public synchronized void join() {
        this.leaderSelector.start();
    }

    public synchronized void exit() {
        this.leaderSelector.close();
        this.client.close();
    }

    private class ContextManager {

        private List<String> ephemeralNodeNameList;

        private List<String> persistentNodeNameList;

        private List<String> allGroupList;

        private List<String> idleGroupList;

        private final int nodeSize;

        private final int groupSize;

        private final int averageGroupCount;

        private Map<String, Integer> nodeScheduledGroupSizeMap = new HashMap<String, Integer>();

        private Map<String, List<String>> nodeScheduledGroupListMap = new HashMap<String, List<String>>();

        private List<String> sortedEphemeralNodeNameList;

        private ContextManager() throws Exception {
            ephemeralNodeNameList = Collections.unmodifiableList(client.getChildren().forPath(apiFactory.pathApi().getMasterSlaveEphemeralNodePath()));
            persistentNodeNameList = Collections.unmodifiableList(client.getChildren().forPath(apiFactory.pathApi().getMasterSlavePersistentNodePath()));
//            allGroupList = Collections.unmodifiableList(getContainer().getScheduleManager().getGroupList());
            nodeSize = ephemeralNodeNameList.size();
            groupSize = allGroupList.size();
            averageGroupCount = (groupSize % nodeSize == 0) ? (groupSize / nodeSize) : (groupSize / nodeSize + 1);
            clearInvalidPersistentNode();
            fillNodeScheduledGroupCondition();
            sortEphemeralNodeNameList();
            findIdleGroupList();
        }

        private void clearInvalidPersistentNode() throws Exception {
            for (String persistentNodeName : persistentNodeNameList) {
                if (!ephemeralNodeNameList.contains(persistentNodeName)) {
                    client.delete().inBackground().forPath(ZKPaths.makePath(apiFactory.pathApi().getMasterSlavePersistentNodePath(), persistentNodeName));
                }
            }
        }

        private void fillNodeScheduledGroupCondition() {
            for (String ephemeralNodeName : ephemeralNodeNameList) {
                List<String> nodeGroupList;
                String persistentNodePath = ZKPaths.makePath(apiFactory.pathApi().getMasterSlavePersistentNodePath(), ephemeralNodeName);
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

        private void sortEphemeralNodeNameList() {
            sortedEphemeralNodeNameList = new ArrayList<String>();
            for (int i = 0;i < ephemeralNodeNameList.size(); i++) {
                Integer currentSize = nodeScheduledGroupSizeMap.get(ephemeralNodeNameList.get(i));
                if (sortedEphemeralNodeNameList.size() == 0) {
                    sortedEphemeralNodeNameList.add(ephemeralNodeNameList.get(i));
                } else {
                    List<String> sortedChildrenCopy = new ArrayList<String>(sortedEphemeralNodeNameList);
                    for (int j = 0;j < sortedChildrenCopy.size(); j++) {
                        if (currentSize < nodeScheduledGroupSizeMap.get(sortedChildrenCopy.get(j))) {
                            sortedEphemeralNodeNameList.add(j, ephemeralNodeNameList.get(i));
                            break;
                        } else if (j == sortedChildrenCopy.size() - 1) {
                            sortedEphemeralNodeNameList.add(ephemeralNodeNameList.get(i));
                        }
                    }
                }
            }
        }

        private void findIdleGroupList() {
            List<String> allScheduledGroupList = new ArrayList<String>();
            for (List<String> nodeScheduledGroupList : nodeScheduledGroupListMap.values()) {
                allScheduledGroupList.addAll(nodeScheduledGroupList);
            }
            idleGroupList = new ArrayList<String>();
            for (String group : allGroupList) {
                if (!allScheduledGroupList.contains(group)) {
                    idleGroupList.add(group);
                }
            }
            idleGroupList = Collections.unmodifiableList(idleGroupList);
        }

        private void addGroup(String nodeName, String group) {
            nodeScheduledGroupSizeMap.put(nodeName, nodeScheduledGroupSizeMap.get(nodeName) + 1);
            nodeScheduledGroupListMap.put(nodeName, ListHelper.add(nodeScheduledGroupListMap.get(nodeName), group));
            sortEphemeralNodeNameList();
        }

        private void subGroup(String nodeName, String group) {
            nodeScheduledGroupSizeMap.put(nodeName, nodeScheduledGroupSizeMap.get(nodeName) - 1);
            nodeScheduledGroupListMap.put(nodeName, ListHelper.sub(nodeScheduledGroupListMap.get(nodeName), group));
            sortEphemeralNodeNameList();
        }

        private String getExceedAverageGroupCountNode() {
            for (int i = sortedEphemeralNodeNameList.size() - 1;i >= 0;i--) {
                if (nodeScheduledGroupSizeMap.get(sortedEphemeralNodeNameList.get(i)) > averageGroupCount) {
                    return sortedEphemeralNodeNameList.get(i);
                }
            }
            return null;
        }

        private String getAverageGroupCountNode() {
            for (int i = sortedEphemeralNodeNameList.size() - 1;i >= 0;i--) {
                if (nodeScheduledGroupSizeMap.get(sortedEphemeralNodeNameList.get(i)) == averageGroupCount) {
                    return sortedEphemeralNodeNameList.get(i);
                }
            }
            return null;
        }

        private String getLessMinimumStandardCountNode() {
            for (int i = 0;i < sortedEphemeralNodeNameList.size();i++) {
                if (nodeScheduledGroupSizeMap.get(sortedEphemeralNodeNameList.get(i)) < (averageGroupCount - 1)) {
                    return sortedEphemeralNodeNameList.get(i);
                }
            }
            return null;
        }

        private List<String> getEphemeralNodeNameList() {
            return ephemeralNodeNameList;
        }

        private List<String> getPersistentNodeNameList() {
            return persistentNodeNameList;
        }

        private List<String> getAllGroupList() {
            return allGroupList;
        }

        public List<String> getIdleGroupList() {
            return idleGroupList;
        }

        public int getNodeSize() {
            return nodeSize;
        }

        public int getGroupSize() {
            return groupSize;
        }

        public int getAverageGroupCount() {
            return averageGroupCount;
        }

        private Map<String, Integer> getNodeScheduledGroupSizeMap() {
            return Collections.unmodifiableMap(nodeScheduledGroupSizeMap);
        }

        private Map<String, List<String>> getNodeScheduledGroupListMap() {
            return Collections.unmodifiableMap(nodeScheduledGroupListMap);
        }

        private List<String> getSortedEphemeralNodeNameList() {
            return Collections.unmodifiableList(sortedEphemeralNodeNameList);
        }

    }

}
