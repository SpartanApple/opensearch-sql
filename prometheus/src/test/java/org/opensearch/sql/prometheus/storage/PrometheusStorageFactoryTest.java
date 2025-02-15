/*
 *
 *  * Copyright OpenSearch Contributors
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package org.opensearch.sql.prometheus.storage;

import java.util.HashMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.sql.datasource.model.DataSource;
import org.opensearch.sql.datasource.model.DataSourceMetadata;
import org.opensearch.sql.datasource.model.DataSourceType;
import org.opensearch.sql.storage.StorageEngine;

@ExtendWith(MockitoExtension.class)
public class PrometheusStorageFactoryTest {

  @Test
  void testGetConnectorType() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    Assertions.assertEquals(
        DataSourceType.PROMETHEUS, prometheusStorageFactory.getDataSourceType());
  }

  @Test
  @SneakyThrows
  void testGetStorageEngineWithBasicAuth() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.uri", "http://dummyprometheus:9090");
    properties.put("prometheus.auth.type", "basicauth");
    properties.put("prometheus.auth.username", "admin");
    properties.put("prometheus.auth.password", "admin");
    StorageEngine storageEngine
        = prometheusStorageFactory.getStorageEngine("my_prometheus", properties);
    Assertions.assertTrue(storageEngine instanceof PrometheusStorageEngine);
  }

  @Test
  @SneakyThrows
  void testGetStorageEngineWithAWSSigV4Auth() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.uri", "http://dummyprometheus:9090");
    properties.put("prometheus.auth.type", "awssigv4");
    properties.put("prometheus.auth.region", "us-east-1");
    properties.put("prometheus.auth.secret_key", "accessKey");
    properties.put("prometheus.auth.access_key", "secretKey");
    StorageEngine storageEngine
        = prometheusStorageFactory.getStorageEngine("my_prometheus", properties);
    Assertions.assertTrue(storageEngine instanceof PrometheusStorageEngine);
  }


  @Test
  @SneakyThrows
  void testGetStorageEngineWithMissingURI() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.auth.type", "awssigv4");
    properties.put("prometheus.auth.region", "us-east-1");
    properties.put("prometheus.auth.secret_key", "accessKey");
    properties.put("prometheus.auth.access_key", "secretKey");
    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
        () -> prometheusStorageFactory.getStorageEngine("my_prometheus", properties));
    Assertions.assertEquals("Missing [prometheus.uri] fields "
            + "in the Prometheus connector properties.",
        exception.getMessage());
  }

  @Test
  @SneakyThrows
  void testGetStorageEngineWithMissingRegionInAWS() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.uri", "http://dummyprometheus:9090");
    properties.put("prometheus.auth.type", "awssigv4");
    properties.put("prometheus.auth.secret_key", "accessKey");
    properties.put("prometheus.auth.access_key", "secretKey");
    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
        () -> prometheusStorageFactory.getStorageEngine("my_prometheus", properties));
    Assertions.assertEquals("Missing [prometheus.auth.region] fields in the "
            + "Prometheus connector properties.",
        exception.getMessage());
  }

  @Test
  @SneakyThrows
  void testGetStorageEngineWithWrongAuthType() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.uri", "https://test.com");
    properties.put("prometheus.auth.type", "random");
    properties.put("prometheus.auth.region", "us-east-1");
    properties.put("prometheus.auth.secret_key", "accessKey");
    properties.put("prometheus.auth.access_key", "secretKey");
    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
        () -> prometheusStorageFactory.getStorageEngine("my_prometheus", properties));
    Assertions.assertEquals("AUTH Type : random is not supported with Prometheus Connector",
        exception.getMessage());
  }


  @Test
  @SneakyThrows
  void testGetStorageEngineWithNONEAuthType() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.uri", "https://test.com");
    StorageEngine storageEngine
        = prometheusStorageFactory.getStorageEngine("my_prometheus", properties);
    Assertions.assertTrue(storageEngine instanceof PrometheusStorageEngine);
  }

  @Test
  @SneakyThrows
  void testGetStorageEngineWithInvalidURISyntax() {
    PrometheusStorageFactory prometheusStorageFactory = new PrometheusStorageFactory();
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.uri", "http://dummyprometheus:9090? param");
    properties.put("prometheus.auth.type", "basicauth");
    properties.put("prometheus.auth.username", "admin");
    properties.put("prometheus.auth.password", "admin");
    RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
        () -> prometheusStorageFactory.getStorageEngine("my_prometheus", properties));
    Assertions.assertTrue(
        exception.getMessage().contains("Prometheus Client creation failed due to:"));
  }

  @Test
  void createDataSourceSuccess() {
    HashMap<String, String> properties = new HashMap<>();
    properties.put("prometheus.uri", "http://dummyprometheus:9090");
    properties.put("prometheus.auth.type", "basicauth");
    properties.put("prometheus.auth.username", "admin");
    properties.put("prometheus.auth.password", "admin");

    DataSourceMetadata metadata = new DataSourceMetadata();
    metadata.setName("prometheus");
    metadata.setConnector(DataSourceType.PROMETHEUS);
    metadata.setProperties(properties);

    DataSource dataSource = new PrometheusStorageFactory().createDataSource(metadata);
    Assertions.assertTrue(dataSource.getStorageEngine() instanceof PrometheusStorageEngine);
  }
}

