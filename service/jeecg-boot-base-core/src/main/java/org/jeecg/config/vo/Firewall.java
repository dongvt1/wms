package org.jeecg.config.vo;

/**
 * Platform security configuration
 *
 * @author: scott
 * @date: 2023Year09moon05day 9:25
 */
public class Firewall {
    /**
     * Data source security (After opening，OnlineData sources for reports and charts are required)
     */
    private Boolean dataSourceSafe = false;
    /**
     * Is use prohibited? * Query all fields
     */
    private Boolean disableSelectAll = false;
    /**
     * low code mode（dev:development mode，prod:Release mode——Close all online development and configuration capabilities）
     */
    private String lowCodeMode;
//    /**
//     * table dictionary security mode（white:whitelist——配置了whitelist的表才能通过表字典方式访问，black:blacklist——配置了blacklist的表不允许表字典方式访问）
//     */
//    private String tableDictMode;

    public Boolean getDataSourceSafe() {
        return dataSourceSafe;
    }

    public void setDataSourceSafe(Boolean dataSourceSafe) {
        this.dataSourceSafe = dataSourceSafe;
    }

    public String getLowCodeMode() {
        return lowCodeMode;
    }

    public void setLowCodeMode(String lowCodeMode) {
        this.lowCodeMode = lowCodeMode;
    }

    public Boolean getDisableSelectAll() {
        return disableSelectAll;
    }

    public void setDisableSelectAll(Boolean disableSelectAll) {
        this.disableSelectAll = disableSelectAll;
    }
}
