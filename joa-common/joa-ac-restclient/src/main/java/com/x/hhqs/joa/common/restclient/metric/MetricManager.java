package com.x.hhqs.joa.common.restclient.metric;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MetricManager {
    /**
     * This method allows applications to additionally register their metrics
     *
     * @param appName
     *            Application Name (must be unique between applications)
     * @param metrics
     *            Additional Metric Providers to register
     * @return <b><code>true</code></b> if the registration is effected, <b><code>false</code></b> if the registration has no effect (all Metric
     *         Providers are already registered for this application)
     */
    boolean add(String appName, Collection<MetricProvider> metrics);

    /**
     * This method allows applications to additionally register their metrics
     *
     * @param appName
     *            Application Name (must be unique between applications)
     * @param metric
     *            Additional Metric Provider to register
     * @return <b><code>true</code></b> if the registration is effected, <b><code>false</code></b> if the registration has no effect (the Metric
     *         Provider is already registered for this application)
     */
    boolean add(String appName, MetricProvider metric);

    /**
     * This method remove all metrics registered by a specific application
     *
     * @param appName
     *            Application Name (must be unique between applications)
     * @return <b><code>true</code></b> if any metric of this application existed and removed, <b><code>false</code></b> otherwise
     *
     */
    boolean remove(String appName);

    /**
     * This method remove a specific metric registered by a specific application
     *
     * @param metric
     *            Metric Provider to remove
     * @param appName
     *            Application Name (must be unique between applications)
     * @return <b><code>true</code></b> if the metric existed and removed, <b><code>false</code></b> otherwise
     *
     */
    boolean remove(String appName, MetricProvider metric);

    /**
     * This method allows applications to register their metrics (any metrics registered previously are removed)
     *
     * @param appName
     *            Application Name (must be unique between applications)
     * @param metrics
     *            Metric Providers to register
     * @return <b><code>true</code></b> if any metric of this application existed and removed, <b><code>false</code></b> otherwise
     *
     */
    boolean replace(String appName, Collection<MetricProvider> metrics);


    /** Get all applications which currently have metrics
     * @return A Set of application names
     */
    Set<String> getApplicationNames();

    /**
     * Get all statistics values
     * @return A list of Statistics value
     */
    List<StatisticsValue> getAllStatisticsValues();

    List<MetricProvider> scanAndRegisterMetricicAnnotatedObject(String appName, Object metricContainer);

    boolean scanAndRemoveMetricicAnnotatedObject(String appName, Object metricContainer);

    boolean register(StatisticsConsumer consumer);

    boolean unregiste(StatisticsConsumer consumer);
}
