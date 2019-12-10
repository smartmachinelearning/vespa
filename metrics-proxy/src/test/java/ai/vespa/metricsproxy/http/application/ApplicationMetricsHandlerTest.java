/*
 * Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package ai.vespa.metricsproxy.http.application;

import ai.vespa.metricsproxy.core.ConsumersConfig;
import ai.vespa.metricsproxy.core.MetricsConsumers;
import ai.vespa.metricsproxy.http.HttpHandlerTestBase;
import ai.vespa.metricsproxy.http.MetricsHandler;
import ai.vespa.metricsproxy.metric.model.json.GenericJsonModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.yahoo.container.jdisc.RequestHandlerTestDriver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;

import static ai.vespa.metricsproxy.TestUtil.getFileContents;
import static ai.vespa.metricsproxy.http.ValuesFetcher.DEFAULT_PUBLIC_CONSUMER_ID;
import static ai.vespa.metricsproxy.http.application.ApplicationMetricsHandler.V1_PATH;
import static ai.vespa.metricsproxy.http.application.ApplicationMetricsHandler.VALUES_PATH;
import static ai.vespa.metricsproxy.metric.model.json.JacksonUtil.createObjectMapper;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author gjoranv
 */
@SuppressWarnings("UnstableApiUsage")
public class ApplicationMetricsHandlerTest extends HttpHandlerTestBase {

    private static final String V1_URI = URI_BASE + V1_PATH;
    private static final String VALUES_URI = URI_BASE + VALUES_PATH;

    private static final String TEST_FILE = "generic-sample.json";
    private static final String RESPONSE = getFileContents(TEST_FILE);
    private static final String HOST = "localhost";

    private static Node node;
    private static int port;

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options().dynamicPort());

    @BeforeClass
    public static void setupClass() {
        ApplicationMetricsRetriever applicationMetricsRetriever = new ApplicationMetricsRetriever(
                nodesConfig("/node0"));

        ApplicationMetricsHandler handler = new ApplicationMetricsHandler(Executors.newSingleThreadExecutor(),
                                                                          applicationMetricsRetriever,
                                                                          emptyMetricsConsumers());
        testDriver = new RequestHandlerTestDriver(handler);
        setupWireMock();
    }

    private static void setupWireMock() {
        port = wireMockRule.port();
        node = new Node("id", "localhost", port, MetricsHandler.VALUES_PATH);
        wireMockRule.stubFor(get(urlEqualTo(node.metricsUri(DEFAULT_PUBLIC_CONSUMER_ID).getPath()))
                                     .willReturn(aResponse().withBody(RESPONSE)));
    }

    private GenericJsonModel getResponseAsJsonModel(String consumer) {
        String response = testDriver.sendRequest(VALUES_URI + "?consumer=" + consumer).readAll();
        try {
            return createObjectMapper().readValue(response, GenericJsonModel.class);
        } catch (IOException e) {
            fail("Failed to create json model: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void v1_response_contains_values_uri() throws Exception {
        String response = testDriver.sendRequest(V1_URI).readAll();
        JSONObject root = new JSONObject(response);
        assertTrue(root.has("resources"));

        JSONArray resources = root.getJSONArray("resources");
        assertEquals(1, resources.length());

        JSONObject valuesUrl = resources.getJSONObject(0);
        assertEquals(VALUES_URI, valuesUrl.getString("url"));
    }

    @Ignore
    @Test
    public void visually_inspect_values_response() throws Exception {
        String response = testDriver.sendRequest(VALUES_URI).readAll();
        ObjectMapper mapper = createObjectMapper();
        var jsonModel = mapper.readValue(response, GenericJsonModel.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonModel));
    }

    @Test
    public void response_contains_node_metrics() {
        GenericJsonModel jsonModel = getResponseAsJsonModel(DEFAULT_CONSUMER);

        // assertNotNull(jsonModel.nodes);
    }

    private static VespaNodesConfig nodesConfig(String... paths) {
        var nodes = Arrays.stream(paths)
                .map(ApplicationMetricsHandlerTest::nodeConfig)
                .collect(toList());
        return new VespaNodesConfig.Builder()
                .node(nodes)
                .build();
    }

    private static VespaNodesConfig.Node.Builder nodeConfig(String path) {
        return new VespaNodesConfig.Node.Builder()
                .configId(path)
                .hostname(HOST)
                .path(path)
                .port(port);
    }

    private static MetricsConsumers emptyMetricsConsumers() {
        return new MetricsConsumers(new ConsumersConfig.Builder().build());
    }
}
