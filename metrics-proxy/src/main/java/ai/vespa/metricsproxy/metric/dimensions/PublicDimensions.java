package ai.vespa.metricsproxy.metric.dimensions;

/**
 * The names of all dimensions that are publicly available, in addition to some dimensions that
 * are used in the process of composing these public dimensions.
 *
 * 'INTERNAL' in this context means non-public.
 *
 * @author gjoranv
 */
public final class PublicDimensions {
    private PublicDimensions() { }

    public static final String APPLICATION_ID = "applicationId";  // tenant.app.instance
    public static final String ZONE = "zone";

    // The public dimension is composed from the two non-public dimensions.
    // Node-specific.
    public static final String INTERNAL_CLUSTER_TYPE = "clustertype";
    public static final String INTERNAL_CLUSTER_ID = "clusterid";
    public static final String CLUSTER_ID = INTERNAL_CLUSTER_TYPE + "/" + INTERNAL_CLUSTER_ID;

    // Internal name (instance) is confusing, so renamed to 'serviceId' for public use.
    // This is added by the metrics-proxy.
    public static final String INTERNAL_SERVICE_ID = "instance";
    public static final String SERVICE_ID = "serviceId";

    // From host-admin
    public static final String HOSTNAME = "host";

}
