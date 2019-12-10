/*
 * Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package ai.vespa.metricsproxy.http.application;

import ai.vespa.metricsproxy.http.HttpHandlerTestBase;
import ai.vespa.metricsproxy.metric.model.json.GenericJsonModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.container.jdisc.RequestHandlerTestDriver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Executors;

import static ai.vespa.metricsproxy.http.application.ApplicationMetricsHandler.V1_PATH;
import static ai.vespa.metricsproxy.http.application.ApplicationMetricsHandler.VALUES_PATH;
import static ai.vespa.metricsproxy.metric.model.json.JacksonUtil.createObjectMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author gjoranv
 */
public class ApplicationMetricsHandlerTest extends HttpHandlerTestBase {

    private static final String V1_URI = URI_BASE + V1_PATH;
    private static final String VALUES_URI = URI_BASE + VALUES_PATH;

    @BeforeClass
    public static void setup() {
        ApplicationMetricsHandler handler = new ApplicationMetricsHandler(Executors.newSingleThreadExecutor(),
                                                                          getMetricsManager(),
                                                                          vespaServices,
                                                                          getMetricsConsumers());
        testDriver = new RequestHandlerTestDriver(handler);
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

}
