package org.jeecg.common.constant;

/**
 * @Description: TenantConstant
 * @author: scott
 * @date: 2022Year08moon29day 15:29
 */
public interface TenantConstant {
    /*------【Low-code application parameters】----------------------------------------------*/
    /**
     * headeroflowAppIdlogo
     */
    String X_LOW_APP_ID = "X-Low-App-ID";
    /**
     * applicationID——Entity fields
     */
    String FIELD_LOW_APP_ID = "lowAppId";
    /**
     * applicationID——table fields
     */
    String DB_FIELD_LOW_APP_ID = "low_app_id";
    /*------【Low-code application parameters】---------------------------------------------*/

    /*--------【Tenant parameters】-----------------------------------------------*/
    /**
     * tenantID（Entity fields名 and urlParameter name）
     */
    String TENANT_ID = "tenantId";
    /**
     * tenantID Database field name
     */
    String TENANT_ID_TABLE = "tenant_id";
    /*-------【Tenant parameters】-----------------------------------------------*/

    /**
     * super administrator
     */
    String SUPER_ADMIN = "superAdmin";

    /**
     * Organization Account Administrator
     */
    String ACCOUNT_ADMIN = "accountAdmin";

    /**
     * 组织application管理员
     */
    String APP_ADMIN = "appAdmin";

}
