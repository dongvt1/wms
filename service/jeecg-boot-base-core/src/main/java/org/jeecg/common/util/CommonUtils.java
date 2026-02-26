package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oss.OssBootUtil;
import org.jeecgframework.poi.util.PoiPublicUtil;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: general tools
 * @author: jeecg-boot
 */
@Slf4j
public class CommonUtils {

    /**
     * Chinese regular rules
     */
    private static Pattern ZHONGWEN_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");

    /**
     * file name Regular string
     * file name支持的字符串：Alphanumeric Chinese.-_()（） Characters other than this will be deleted
     */
    private static String FILE_NAME_REGEX = "[^A-Za-z\\.\\(\\)\\-（）\\_0-9\\u4e00-\\u9fa5]";

    public static String uploadOnlineImage(byte[] data,String basePath,String bizPath,String uploadType){
        String dbPath = null;
        String fileName = "image" + Math.round(Math.random() * 100000000000L);
        fileName += "." + PoiPublicUtil.getFileExtendName(data);
        try {
            if(CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)){
                File file = new File(basePath + File.separator + bizPath + File.separator );
                if (!file.exists()) {
                    file.mkdirs();// Create file root directory
                }
                String savePath = file.getPath() + File.separator + fileName;
                File savefile = new File(savePath);
                FileCopyUtils.copy(data, savefile);
                dbPath = bizPath + File.separator + fileName;
            }else {
                InputStream in = new ByteArrayInputStream(data);
                String relativePath = bizPath+"/"+fileName;
                if(CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)){
                    dbPath = MinioUtil.upload(in,relativePath);
                }else if(CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)){
                    dbPath = OssBootUtil.upload(in,relativePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbPath;
    }

    /**
     * 判断file name是否带盘符，reprocess
     * @param fileName
     * @return
     */
    public static String getFileName(String fileName){
        //Determine whether there is drive letter information
        // Check for Unix-style path
        int unixSep = fileName.lastIndexOf('/');
        // Check for Windows-style path
        int winSep = fileName.lastIndexOf('\\');
        // Cut off at latest possible point
        int pos = (winSep > unixSep ? winSep : unixSep);
        if (pos != -1)  {
            // Any sort of path separator found...
            fileName = fileName.substring(pos + 1);
        }
        //替换上传file name字的特殊字符
        fileName = fileName.replace("=","").replace(",","").replace("&","")
                .replace("#", "").replace("“", "").replace("”", "");
        //替换上传file name字中的空格
        fileName=fileName.replaceAll("\\s","");
        //update-beign-author:taoyan date:20220302 for: /issues/3381 online Online form When using the file component，上传file name中含%，Download exception
        fileName = fileName.replaceAll(FILE_NAME_REGEX, "");
        //update-end-author:taoyan date:20220302 for: /issues/3381 online Online form When using the file component，上传file name中含%，Download exception
        return fileName;
    }

    /**
     * java Determine whether the string contains Chinese characters
     * @param str
     * @return
     */
    public static boolean ifContainChinese(String str) {
        if(str.getBytes().length == str.length()){
            return false;
        }else{
            Matcher m = ZHONGWEN_PATTERN.matcher(str);
            if (m.find()) {
                return true;
            }
            return false;
        }
    }

    /**
     * Unified global upload
     * @Return: java.lang.String
     */
    public static String upload(MultipartFile file, String bizPath, String uploadType) {
        String url = "";
        try {
            if (CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)) {
                url = MinioUtil.upload(file, bizPath);
            } else {
                url = OssBootUtil.upload(file, bizPath);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException(e.getMessage());
        }
        return url;
    }
    /**
     * Local file upload
     * @param mf document
     * @param bizPath  custom path
     * @return
     */
    public static String uploadLocal(MultipartFile mf,String bizPath,String uploadpath){
        try {
            // document安全校验，防止上传漏洞document
            SsrfFileTypeFilter.checkUploadFileType(mf, bizPath);
            
            String fileName = null;
            File file = new File(uploadpath + File.separator + bizPath + File.separator );
            if (!file.exists()) {
                // Create file root directory
                file.mkdirs();
            }
            // Getfile name
            String orgName = mf.getOriginalFilename();
            // Transcoding without Chinese
            if (orgName != null && !CommonUtils.ifContainChinese(orgName)) {
                orgName = new String(orgName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            }
            orgName = CommonUtils.getFileName(orgName);
            if(orgName.indexOf(SymbolConstant.SPOT)!=-1){
                fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."));
            }else{
                fileName = orgName+ "_" + System.currentTimeMillis();
            }
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath = null;
            if(oConvertUtils.isNotEmpty(bizPath)){
                dbpath = bizPath + File.separator + fileName;
            }else{
                dbpath = fileName;
            }
            if (dbpath.contains(SymbolConstant.DOUBLE_BACKSLASH)) {
                dbpath = dbpath.replace("\\", "/");
            }
            return dbpath;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * Unified global upload With bucket
     * @Return: java.lang.String
     */
    public static String upload(MultipartFile file, String bizPath, String uploadType, String customBucket) {
        String url = "";
        try {
            if (CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)) {
                url = MinioUtil.upload(file, bizPath, customBucket);
            } else {
                url = OssBootUtil.upload(file, bizPath, customBucket);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return url;
    }

    /** Current system database type */
    private static String DB_TYPE = "";
    private static DbType dbTypeEnum = null;

    /**
     * Globally obtain the platform database type（voided）
     * @return
     */
    @Deprecated
    public static String getDatabaseType() {
        if(oConvertUtils.isNotEmpty(DB_TYPE)){
            return DB_TYPE;
        }
        DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
        try {
            return getDatabaseTypeByDataSource(dataSource);
        } catch (SQLException e) {
            //e.printStackTrace();
            log.warn(e.getMessage(),e);
            return "";
        }
    }

    /**
     * Globally obtain the platform database type（correspondmybaisPlusenumerate）
     * @return
     */
    public static DbType getDatabaseTypeEnum() {
        if (oConvertUtils.isNotEmpty(dbTypeEnum)) {
            return dbTypeEnum;
        }
        try {
            DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
            dbTypeEnum = JdbcUtils.getDbType(dataSource.getConnection().getMetaData().getURL());
            //【useSQL_SERVER2005engine】QQYUN-13298 Resolve upgrademybatisPlusbackSqlServerUse pagingOFFSET，No sort field error reporting problem
            if (dbTypeEnum == DbType.SQL_SERVER) {
                dbTypeEnum = DbType.SQL_SERVER2005;
            }
            return dbTypeEnum;
        } catch (SQLException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * According to data sourcekeyGetDataSourceProperty
     * @param sourceKey
     * @return
     */
    public static DataSourceProperty getDataSourceProperty(String sourceKey){
        DynamicDataSourceProperties prop = SpringContextUtils.getApplicationContext().getBean(DynamicDataSourceProperties.class);
        Map<String, DataSourceProperty> map = prop.getDatasource();
        DataSourceProperty db = (DataSourceProperty)map.get(sourceKey);
        return db;
    }

    /**
     * according tosourceKey Get数据源连接
     * @param sourceKey
     * @return
     * @throws SQLException
     */
    public static Connection getDataSourceConnect(String sourceKey) throws SQLException {
        if (oConvertUtils.isEmpty(sourceKey)) {
            sourceKey = "master";
        }
        DynamicDataSourceProperties prop = SpringContextUtils.getApplicationContext().getBean(DynamicDataSourceProperties.class);
        Map<String, DataSourceProperty> map = prop.getDatasource();
        DataSourceProperty db = (DataSourceProperty)map.get(sourceKey);
        if(db==null){
            return null;
        }
        DriverManagerDataSource ds = new DriverManagerDataSource ();
        ds.setDriverClassName(db.getDriverClassName());
        ds.setUrl(db.getUrl());
        ds.setUsername(db.getUsername());
        ds.setPassword(db.getPassword());
        return ds.getConnection();
    }

    /**
     * GetDatabase type
     * @param dataSource
     * @return
     * @throws SQLException
     */
    private static String getDatabaseTypeByDataSource(DataSource dataSource) throws SQLException{
        if("".equals(DB_TYPE)) {
            Connection connection = dataSource.getConnection();
            try {
                DatabaseMetaData md = connection.getMetaData();
                String dbType = md.getDatabaseProductName().toUpperCase();
                String sqlserver= "SQL SERVER";
                if(dbType.indexOf(DataBaseConstant.DB_TYPE_MYSQL)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_MYSQL;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_ORACLE)>=0 ||dbType.indexOf(DataBaseConstant.DB_TYPE_DM)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_ORACLE;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_SQLSERVER)>=0||dbType.indexOf(sqlserver)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_SQLSERVER;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_POSTGRESQL)>=0 || dbType.indexOf(DataBaseConstant.DB_TYPE_KINGBASEES)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_POSTGRESQL;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_MARIADB)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_MARIADB;
                }else {
                    log.error("Database type:[" + dbType + "]Not recognized!");
                    //throw new JeecgBootException("Database type:["+dbType+"]Not recognized!");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }finally {
                connection.close();
            }
        }
        return DB_TYPE;

    }
    /**
     * Get服务器地址
     *
     * @param request
     * @return
     */
    public static String getBaseUrl(HttpServletRequest request) {
        //1.【compatible】compatible微服务下的 base path-------
        String xGatewayBasePath = request.getHeader(ServiceNameConstants.X_GATEWAY_BASE_PATH);
        if(oConvertUtils.isNotEmpty(xGatewayBasePath)){
            log.info("x_gateway_base_path = "+ xGatewayBasePath);
            return  xGatewayBasePath;
        }
        //2.【compatible】SSL认证之back，request.getScheme()Get不到httpsquestion
        // https://blog.csdn.net/weixin_34376986/article/details/89767950
        String scheme = request.getHeader(CommonConstant.X_FORWARDED_SCHEME);
        if(oConvertUtils.isEmpty(scheme)){
            scheme = request.getScheme();
        }

        //3.Routine operation
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        //return host domain
        String baseDomainPath = null;
        //update-begin---author:wangshuai---date:2024-03-15---for:【QQYUN-8561】Enterprise WeChat login request interface setting context is inconsistent，resulting in interface404---
        int httpPort = 80;
        int httpsPort = 443;
        if(httpPort == serverPort || httpsPort == serverPort){
        //update-end---author:wangshuai---date:2024-03-15---for:【QQYUN-8561】Enterprise WeChat login request interface setting context is inconsistent，resulting in interface404---~
            baseDomainPath = scheme + "://" + serverName  + contextPath ;
        }else{
            baseDomainPath = scheme + "://" + serverName + ":" + serverPort + contextPath ;
        }
        log.debug("-----Common getBaseUrl----- : " + baseDomainPath);
        return baseDomainPath;
    }

    /**
     * Recursive merge fastJSON object
     *
     * @param target  目标object
     * @param sources 来源object，Allow multiple，Priority from left to right，The rightmost one has the highest priority
     */
    public static JSONObject mergeJSON(JSONObject target, JSONObject... sources) {
        for (JSONObject source : sources) {
            CommonUtils.mergeJSON(target, source);
        }
        return target;
    }

    /**
     * Recursive merge fastJSON object
     *
     * @param target 目标object
     * @param source 来源object
     */
    public static JSONObject mergeJSON(JSONObject target, JSONObject source) {
        for (String key : source.keySet()) {
            Object sourceItem = source.get(key);
            // whether it is JSONObject
            if (sourceItem instanceof Map) {
                // targetThis exists inkey
                if (target.containsKey(key)) {
                    // both JSONObject，continue merging
                    if (target.get(key) instanceof Map) {
                        CommonUtils.mergeJSON(target.getJSONObject(key), source.getJSONObject(key));
                        continue;
                    }
                }
            }
            // targetThis does not existkey，or not JSONObject，then cover
            target.put(key, sourceItem);
        }
        return target;
    }

    /**
     * WilllistThe collection is divided using delimiters
     * @param list      Stringcollection text of type
     * @param separator delimiter
     * @return
     */
    public static String getSplitText(List<String> list, String separator) {
        if (null != list && list.size() > 0) {
            return StringUtils.join(list, separator);
        }
        return "";
    }
 
    /**
     * passtableconditionsSQL
     *
     * @param tableSql sys_user where name = '1212'
     * @return name = '1212'
     */
    public static String getFilterSqlByTableSql(String tableSql) {
        if(oConvertUtils.isEmpty(tableSql)){
            return null;
        }
        
        if (tableSql.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE) > 0) {
            String[] arr = tableSql.split(" (?i)where ");
            if (arr != null && oConvertUtils.isNotEmpty(arr[1])) {
                return arr[1];
            }
        }
        return "";
    }

    /**
     * passtableGet表名
     *
     * @param tableSql sys_user where name = '1212'
     * @return sys_user
     */
    public static String getTableNameByTableSql(String tableSql) {
        if(oConvertUtils.isEmpty(tableSql)){
            return null;
        }
        
        if (tableSql.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE) > 0) {
            String[] arr = tableSql.split(" (?i)where ");
            return arr[0].trim();
        } else {
            return tableSql;
        }
    }

    /**
     * Determine whether two arrays intersect
     * @param set1
     * @param arr2
     * @return
     */
    public static boolean hasIntersection(Set<String> set1, String[] arr2) {
        if (set1 == null) {
            return false;
        }
        
        if(set1.size()>0){
            for (String str : arr2) {
                if (set1.contains(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * outputinfolog，Exceptions will be caught，防止因为log问题导致程序异常
     *
     * @param msg
     * @param objects
     */
    public static void logInfo(String msg, Object... objects) {
        try {
            log.info(msg, objects);
        } catch (Exception e) {
            log.warn("{} —— {}", msg, e.getMessage());
        }
    }

}