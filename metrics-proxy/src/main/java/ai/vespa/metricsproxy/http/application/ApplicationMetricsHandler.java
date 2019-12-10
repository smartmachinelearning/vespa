/*
 * Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package ai.vespa.metricsproxy.http.application;

import ai.vespa.metricsproxy.http.ErrorResponse;
import ai.vespa.metricsproxy.http.HttpHandlerBase;
import ai.vespa.metricsproxy.http.JsonResponse;
import ai.vespa.metricsproxy.metric.model.MetricsPacket;
import ai.vespa.metricsproxy.metric.model.ServiceId;
import com.google.inject.Inject;
import com.yahoo.container.jdisc.HttpResponse;
import com.yahoo.restapi.Path;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import static ai.vespa.metricsproxy.metric.model.json.GenericJsonUtil.toGenericJsonModel;
import static com.yahoo.jdisc.Response.Status.INTERNAL_SERVER_ERROR;
import static com.yahoo.jdisc.Response.Status.OK;

/**
 * Http handler that returns metrics for all nodes in the Vespa application.
 *
 * @author gjoranv
 */
public class ApplicationMetricsHandler extends HttpHandlerBase {

    public static final String V1_PATH = "/applicationmetrics/v1";
    static final String VALUES_PATH = V1_PATH + "/values";

    private final ApplicationMetricsRetriever metricsRetriever;

    @Inject
    public ApplicationMetricsHandler(Executor executor,
                                     ApplicationMetricsRetriever metricsRetriever) {
        super(executor);
        this.metricsRetriever = metricsRetriever;
    }

    @Override
    public Optional<HttpResponse> doHandle(URI requestUri, Path apiPath, String consumer) {
        if (apiPath.matches(V1_PATH)) return Optional.of(resourceListResponse(requestUri, List.of(VALUES_PATH)));
        if (apiPath.matches(VALUES_PATH)) return Optional.of(applicationMetricsResponse(consumer));
        return Optional.empty();
    }

    private JsonResponse applicationMetricsResponse(String consumer) {
        try {
            List<MetricsPacket> metrics =  Collections.singletonList(new MetricsPacket.Builder(ServiceId.toServiceId("foo-service")).build());
            return new JsonResponse(OK, toGenericJsonModel(metrics).serialize());
        } catch (Exception e) {
            log.log(Level.WARNING, "Got exception when rendering metrics:", e);
            return new ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


}
