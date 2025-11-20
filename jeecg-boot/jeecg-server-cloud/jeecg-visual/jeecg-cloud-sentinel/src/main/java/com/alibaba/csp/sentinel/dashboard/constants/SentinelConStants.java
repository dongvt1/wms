package com.alibaba.csp.sentinel.dashboard.constants;

/**
 * sentinelconstant configuration
 * @author zyf
 */
public class SentinelConStants {
    public static final String GROUP_ID = "SENTINEL_GROUP";

    /**
     * Flow control rules
     */
    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";
    /**
     * Hotspot parameters
     */
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "-param-rules";
    /**
     * Downgrade rules
     */
    public static final String DEGRADE_DATA_ID_POSTFIX = "-degrade-rules";
    /**
     * System rules
     */
    public static final String SYSTEM_DATA_ID_POSTFIX = "-system-rules";
    /**
     * Authorization rules
     */
    public static final String AUTHORITY_DATA_ID_POSTFIX = "-authority-rules";

    /**
     * gatewayAPI
     */
    public static final String GETEWAY_API_DATA_ID_POSTFIX = "-api-rules";
    /**
     * gatewayFlow control rules
     */
    public static final String GETEWAY_FLOW_DATA_ID_POSTFIX = "-flow-rules";
}
