package org.jeecg.modules.test.constant;

/**
 * Microservice unit test constant definition
 * @author: zyf
 * @date: 2022/04/21
 */
public interface CloudConstant {

    /**
     * MQTest queue name
     */
    public final static String MQ_JEECG_PLACE_ORDER = "jeecg_place_order";

    /**
     * MQTest message bus
     */
    public final static String MQ_DEMO_BUS_EVENT = "demoBusEvent";

    /**
     * Distributed locklock key
     */
    public final static String REDISSON_DEMO_LOCK_KEY1 = "demoLockKey1";
    public final static String REDISSON_DEMO_LOCK_KEY2 = "demoLockKey2";

}
