package org.jeecg.modules.system.constant;

/**
 * Default home page constants
 */
public interface DefIndexConst {

    /**
     * Default home pageroleCode
     */
    String DEF_INDEX_ALL = "DEF_INDEX_ALL";

    /**
     * Default home page缓存key
     */
    String CACHE_KEY = "sys:cache:def_index";
    /**
     * 缓存Default home page类型前缀
     */
    String CACHE_TYPE = "sys:cache:home_type::";
    /**
     * Default home page type
     */
    String HOME_TYPE_SYSTEM = "system";
    String HOME_TYPE_PERSONAL = "personal";
    String HOME_TYPE_MENU = "menuHome";

    /**
     * Default home page初始值
     */
    String DEF_INDEX_NAME = "front page";
    String DEF_INDEX_URL = "/dashboard/analysis";
    String DEF_INDEX_COMPONENT = "dashboard/Analysis";

}
