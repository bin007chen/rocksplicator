/*
 *  Copyright 2017 Pinterest, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.pinterest.rocksplicator.controller.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.pinterest.rocksdb_admin.thrift.Admin;
import com.pinterest.rocksplicator.controller.util.AdminClientFactory;
import com.pinterest.rocksplicator.controller.util.ZKUtil;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

/**
 * @author Ang Xu (angxu@pinterest.com)
 */
public abstract class TaskBaseTest {
  protected static final String CLUSTER = "devtest";
  protected static final String CONFIG =
      "{" +
          "  \"user_pins\": {" +
          "  \"num_shards\": 3," +
          "  \"127.0.0.1:8090:us-east-1a\": [\"00000:M\", \"00001:S\", \"00002:S\"]," +
          "  \"127.0.0.1:8091:us-east-1c\": [\"00000:S\", \"00001:M\", \"00002:S\"]," +
          "  \"127.0.0.1:8092:us-east-1e\": [\"00000:S\", \"00001:S\", \"00002:M\"]" +
          "   }" +
          "}";

  protected TestingServer zkServer;
  protected CuratorFramework zkClient;
  protected Injector injector;

  @Mock protected Admin.Client client;
  @Mock protected AdminClientFactory clientFactory;


  @BeforeMethod
  public void setup() throws Exception {

    MockitoAnnotations.initMocks(this);

    zkServer = new TestingServer();
    zkClient = CuratorFrameworkFactory.newClient(
        zkServer.getConnectString(), new RetryOneTime(3000));
    zkClient.start();

    zkClient.createContainers(ZKUtil.getClusterConfigZKPath(CLUSTER));
    zkClient.setData().forPath(ZKUtil.getClusterConfigZKPath(CLUSTER), CONFIG.getBytes());

    TaskModule module = new TaskModule(zkClient, clientFactory);
    injector = Guice.createInjector(module);

    // mock client factory
    when(clientFactory.getClient(any())).thenReturn(client);
  }

  @AfterMethod
  public void tearDown() throws IOException {
    zkClient.close();
    zkServer.close();
  }

}
