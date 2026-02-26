package org.jeecg.common.aspect;

/**
 * @Author scott
 * @Date 2020/1/14 13:36
 * @Description: askURLRouting with menuURLConversion rules（Ease of use of menu routingURLto configure data permission rules）
 */
public enum UrlMatchEnum {
    /**begURLRouting with menuURLConversion rules /online/cgform/api/getData/ */
    CGFORM_DATA("/online/cgform/api/getData/", "/online/cgformList/"),
    /**begURLRouting with menuURLConversion rules /online/cgform/api/exportXls/ */
    CGFORM_EXCEL_DATA("/online/cgform/api/exportXls/", "/online/cgformList/"),
    /**begURLRouting with menuURLConversion rules /online/cgform/api/getTreeData/ */
    CGFORM_TREE_DATA("/online/cgform/api/getTreeData/", "/online/cgformList/"),
    /**begURLRouting with menuURLConversion rules /online/cgreport/api/getColumnsAndData/ */
    CGREPORT_DATA("/online/cgreport/api/getColumnsAndData/", "/online/cgreport/"),
    /** begURLRouting with menuURLConversion rules/online/cgreport/api/getData/ 【vue3报表数据ask地址】 */
    CGREPORT_ONLY_DATA("/online/cgreport/api/getData/", "/online/cgreport/"),
    /**begURLRouting with menuURLConversion rules /online/cgreport/api/exportXls/ */
    CGREPORT_EXCEL_DATA("/online/cgreport/api/exportXls/", "/online/cgreport/"),
    /**begURLRouting with menuURLConversion rules /online/cgreport/api/exportManySheetXls/ */
    CGREPORT_EXCEL_DATA2("/online/cgreport/api/exportManySheetXls/", "/online/cgreport/");

    UrlMatchEnum(String url, String matchUrl) {
        this.url = url;
        this.matchUrl = matchUrl;
    }

    /**
     * Request ask URLprefix
     */
    private String url;
    /**
     * menu routing URLprefix (Corresponding menu path)
     */
    private String matchUrl;

    /**
     * according toreq url Get the menu configuration path（Front-end page routingURL）
     *
     * @param url
     * @return
     */
    public static String getMatchResultByUrl(String url) {
        //Get the enumeration
        UrlMatchEnum[] values = UrlMatchEnum.values();
        //strengthenforLoop through the traversal operation
        for (UrlMatchEnum lr : values) {
            //If traversed to obtaintypeand parameterstypeconsistent
            if (url.indexOf(lr.url) != -1) {
                //returntypeObjectdesc
                return url.replace(lr.url, lr.matchUrl);
            }
        }
        return null;
    }

    public String getMatchUrl() {
        return matchUrl;
    }
    //    public static void main(String[] args) {
//        /**
//         * for examplerequest真实askURL: /online/cgform/api/getData/81fcf7d8922d45069b0d5ba983612d3a
//         * Transform matching routeURLback（The menu path corresponding to the configuration）:/online/cgformList/81fcf7d8922d45069b0d5ba983612d3a
//         */
//        System.out.println(UrlMatchEnum.getMatchResultByUrl("/online/cgform/api/getData/81fcf7d8922d45069b0d5ba983612d3a"));
//    }
}