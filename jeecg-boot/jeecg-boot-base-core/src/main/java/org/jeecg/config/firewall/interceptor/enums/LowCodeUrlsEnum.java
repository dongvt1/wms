package org.jeecg.config.firewall.interceptor.enums;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author: qinfeng
 * @date: 2023/09/04 11:44
 */
public enum LowCodeUrlsEnum {
    /**
     * onlineForm configuration request TODO Add, modify or delete
     */
    NEW_LOW_APP_ADD_URL("/online/cgform/api/addAll", "Add toonlineform"),
    NEW_LOW_APP_EDIT_URL("/online/cgform/api/editAll", "editonlineform"),
    ONLINE_DB_SYNC("/online/cgform/api/doDbSynch/**/**", "onlineform同步数据库"),
    ONLINE_DEL_BATCH("/online/cgform/head/deleteBatch", "onlineform批量删除"),
    ONLINE_DELETE("/online/cgform/head/delete", "onlineform删除"),
    ONLINE_REMOVE("/online/cgform/head/removeRecord", "onlineform移除"),
    ONLINE_COPY("/online/cgform/head/copyOnline", "onlineform生成视图"),
    ONLINE_TABLE("/online/cgform/head/copyOnlineTable", "onlineform复制表"),
    ONLINE_BUTTON_AI_TEST("/online/cgform/button/aitest", "onlineform自定义按钮生成数据"),
    ONLINE_BUTTON_ADD("/online/cgform/button/add", "onlineform自定义按钮新增"),
    ONLINE_BUTTON_EDIT("/online/cgform/button/edit", "onlineform自定义按钮edit"),
    ONLINE_BUTTON_DEL("/online/cgform/button/deleteBatch", "onlineform自定义按钮删除"),
    ONLINE_ENHANCE_JS("/online/cgform/head/enhanceJs/**", "onlineformJSEnhance"),
    ONLINE_ENHANCE_JAVA("/online/cgform/head/enhanceJava/**", "onlineformJAVAEnhance"),
    /**
     * onlineReport configuration request
     */
    ONLINE_CG_REPORT_ADD("/online/cgreport/head/add", "onlineReport added"),
    ONLINE_CG_REPORT_EDIT("/online/cgreport/head/editAll", "onlineReportedit"),
    ONLINE_CG_REPORT_DEL("/online/cgreport/head/delete", "onlineReport deletion"),
    ONLINE_CG_REPORT_PARSE_SQL("/online/cgreport/head/parseSql", "onlineReportSQLparse"),
    /**
     * onlineChart configuration request
     */
    ONLINE_GRAPH_REPORT_ADD("/online/graphreport/head/add", "onlineNew chart"),
    ONLINE_GRAPH_REPORT_EDIT("/online/graphreport/head/edit", "online图表edit"),
    ONLINE_GRAPH_REPORT_DEL("/online/graphreport/head/deleteBatch", "onlineChart delete"),
    ONLINE_GRAPH_REPORT_PARSE_SQL("/online/cgreport/head/parseSql", "online图表parseSQL"),

    /**
     * Large screen configuration request
     */
    BIG_SCREEN_DB_ADD("/bigscreen/bigScreenDb/add", "New large screen data source"),
    BIG_SCREEN_DB_EDIT("/bigscreen/bigScreenDb/edit", "大屏数据源edit"),
    BIG_SCREEN_DB_DEL("/bigscreen/bigScreenDb/delete", "Delete large screen data source"),
    BIG_SCREEN_DB_TEST_CONNECTION("/bigscreen/bigScreenDb/testConnection", "Large screen data source connection test"),
//    BIG_SCREEN_SAVE("/bigscreen/visual/save", "Large screen new"),
//    BIG_SCREEN_EDIT("/bigscreen/visual/update", "大屏edit"),
//    BIG_SCREEN_COPY("/bigscreen/visual/copy", "Large screen copy"),
//    BIG_SCREEN_REMOVE("/bigscreen/visual/remove", "Large screen removal"),
//    BIG_SCREEN_DEL("/bigscreen/visual/deleteById", "Delete on large screen"),

    /**
     * Dashboard configuration request
     */
    DRAG_DB_ADD("/drag/onlDragDataSource/add", "New dashboard data source"),
    DRAG_DB_TEST_CONNECTION("/drag/onlDragDataSource/testConnection", "Dashboard data source connection test"),
    DRAG_PARSE_SQL("/drag/onlDragDatasetHead/queryFieldBySql", "Dashboard datasetSQLparse"),
    DRAG_DATASET_ADD("/drag/onlDragDatasetHead/add", "Dashboard dataset新增");

    /**
     * Other configuration requests
     */

    private String url;
    private String title;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    LowCodeUrlsEnum(String url, String title) {
        this.url = url;
        this.title = title;
    }

    /**
     * according tocodeGet the available quantity
     *
     * @return
     */
    public static List<String> getLowCodeInterceptUrls() {
        return Arrays.stream(LowCodeUrlsEnum.values()).map(LowCodeUrlsEnum::getUrl).collect(Collectors.toList());
    }

}
