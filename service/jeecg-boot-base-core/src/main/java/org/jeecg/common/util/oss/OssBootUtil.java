package org.jeecg.common.util.oss;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItemStream;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.filter.StrAttackFilter;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;

/**
 * @Description: Alibaba Cloud oss Upload tool class(High dependency version)
 * @Date: 2019/5/10
 * @author: jeecg-boot
 */
@Slf4j
public class OssBootUtil {

    private static String endPoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String bucketName;
    private static String staticDomain;

    public static void setEndPoint(String endPoint) {
        OssBootUtil.endPoint = endPoint;
    }

    public static void setAccessKeyId(String accessKeyId) {
        OssBootUtil.accessKeyId = accessKeyId;
    }

    public static void setAccessKeySecret(String accessKeySecret) {
        OssBootUtil.accessKeySecret = accessKeySecret;
    }

    public static void setBucketName(String bucketName) {
        OssBootUtil.bucketName = bucketName;
    }

    public static void setStaticDomain(String staticDomain) {
        OssBootUtil.staticDomain = staticDomain;
    }

    public static String getStaticDomain() {
        return staticDomain;
    }

    public static String getEndPoint() {
        return endPoint;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }

    public static String getBucketName() {
        return bucketName;
    }

    public static OSSClient getOssClient() {
        return ossClient;
    }

    /**
     * oss Tool client
     */
    private static OSSClient ossClient = null;

    /**
     * 上传文件至Alibaba Cloud OSS
     * File uploaded successfully,Returns the complete access path of the file
     * File upload failed,return null
     *
     * @param file    Files to be uploaded
     * @param fileDir File saving directory
     * @return oss Relative file path in
     */
    public static String upload(MultipartFile file, String fileDir,String customBucket) throws Exception {
        // File security check，Prevent vulnerable files from being uploaded
        SsrfFileTypeFilter.checkUploadFileType(file);

        String filePath = null;
        initOss(endPoint, accessKeyId, accessKeySecret);
        StringBuilder fileUrl = new StringBuilder();
        String newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(customBucket)){
            newBucket = customBucket;
        }
        try {
            //Determine whether the bucket exists,Create a bucket if it does not exist
            if(!ossClient.doesBucketExist(newBucket)){
                ossClient.createBucket(newBucket);
            }
            // Get file name
            String orgName = file.getOriginalFilename();
            if("" == orgName){
              orgName=file.getName();
            }
            orgName = CommonUtils.getFileName(orgName);
            String fileName = orgName.indexOf(".")==-1
                              ?orgName + "_" + System.currentTimeMillis()
                              :orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."));
            if (!fileDir.endsWith(SymbolConstant.SINGLE_SLASH)) {
                fileDir = fileDir.concat(SymbolConstant.SINGLE_SLASH);
            }
            //update-begin-author:wangshuai date:20201012 for: Filter uploaded folder names with special characters，Prevent attacks
            fileDir=StrAttackFilter.filter(fileDir);
            //update-end-author:wangshuai date:20201012 for: Filter uploaded folder names with special characters，Prevent attacks
            fileUrl = fileUrl.append(fileDir + fileName);

            if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
                filePath = staticDomain + SymbolConstant.SINGLE_SLASH + fileUrl;
            } else {
                filePath = "https://" + newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH + fileUrl;
            }
            PutObjectResult result = ossClient.putObject(newBucket, fileUrl.toString(), file.getInputStream());
            // Set permissions(public reading)
//            ossClient.setBucketAcl(newBucket, CannedAccessControlList.PublicRead);
            if (result != null) {
                log.info("------OSSFile uploaded successfully------" + fileUrl);
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            return null;
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        }
        return filePath;
    }

    /**
     * get originalURL
    * @param url: originalURL
    * @Return: java.lang.String
    */
    public static String getOriginalUrl(String url) {
        String originalDomain = "https://" + bucketName + "." + endPoint;
        if(oConvertUtils.isNotEmpty(staticDomain) && url.indexOf(staticDomain)!=-1){
            url = url.replace(staticDomain,originalDomain);
        }
        return url;
    }

    /**
     * File upload
     * @param file
     * @param fileDir
     * @return
     */
    public static String upload(MultipartFile file, String fileDir) throws Exception {
        return upload(file, fileDir,null);
    }

    /**
     * 上传文件至Alibaba Cloud OSS
     * File uploaded successfully,Returns the complete access path of the file
     * File upload failed,return null
     *
     * @param file    Files to be uploaded
     * @param fileDir File saving directory
     * @return oss Relative file path in
     */
    public static String upload(FileItemStream file, String fileDir) {
        String filePath = null;
        initOss(endPoint, accessKeyId, accessKeySecret);
        StringBuilder fileUrl = new StringBuilder();
        try {
            String suffix = file.getName().substring(file.getName().lastIndexOf('.'));
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            if (!fileDir.endsWith(SymbolConstant.SINGLE_SLASH)) {
                fileDir = fileDir.concat(SymbolConstant.SINGLE_SLASH);
            }
            fileDir = StrAttackFilter.filter(fileDir);
            fileUrl = fileUrl.append(fileDir + fileName);
            if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
                filePath = staticDomain + SymbolConstant.SINGLE_SLASH + fileUrl;
            } else {
                filePath = "https://" + bucketName + "." + endPoint + SymbolConstant.SINGLE_SLASH + fileUrl;
            }
            PutObjectResult result = ossClient.putObject(bucketName, fileUrl.toString(), file.openStream());
            // Set permissions(public reading)
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
            if (result != null) {
                log.info("------OSSFile uploaded successfully------" + fileUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return filePath;
    }

    /**
     * Delete files
     * @param url
     */
    public static void deleteUrl(String url) {
        deleteUrl(url,null);
    }

    /**
     * Delete files
     * @param url
     */
    public static void deleteUrl(String url,String bucket) {
        String newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(bucket)){
            newBucket = bucket;
        }
        String bucketUrl = "";
        if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
            bucketUrl = staticDomain + SymbolConstant.SINGLE_SLASH ;
        } else {
            bucketUrl = "https://" + newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH;
        }
        //TODO Deletion of cloud storage files is temporarily not allowed
        //initOss(endPoint, accessKeyId, accessKeySecret);
        url = url.replace(bucketUrl,"");
        ossClient.deleteObject(newBucket, url);
    }

    /**
     * Delete files
     * @param fileName
     */
    public static void delete(String fileName) {
        ossClient.deleteObject(bucketName, fileName);
    }

    /**
     * Get file stream
     * @param objectName
     * @param bucket
     * @return
     */
    public static InputStream getOssFile(String objectName,String bucket){
        InputStream inputStream = null;
        try{
            String newBucket = bucketName;
            if(oConvertUtils.isNotEmpty(bucket)){
                newBucket = bucket;
            }
            initOss(endPoint, accessKeyId, accessKeySecret);
            //update-begin---author:liusq  Date:20220120  for：replaceobjectNameprefix，preventkeyInconsistency results in failure to obtain files----
            objectName = OssBootUtil.replacePrefix(objectName,bucket);
            //update-end---author:liusq  Date:20220120  for：replaceobjectNameprefix，preventkeyInconsistency results in failure to obtain files----
            OSSObject ossObject = ossClient.getObject(newBucket,objectName);
            inputStream = new BufferedInputStream(ossObject.getObjectContent());
        }catch (Exception e){
            log.info("File retrieval failed" + e.getMessage());
        }
        return inputStream;
    }

    ///**
    // * Get file stream
    // * @param objectName
    // * @return
    // */
    //public static InputStream getOssFile(String objectName){
    //    return getOssFile(objectName,null);
    //}

    /**
     * Get file external link
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    public static String getObjectUrl(String bucketName, String objectName, Date expires) {
        initOss(endPoint, accessKeyId, accessKeySecret);
        try{
            //update-begin---author:liusq  Date:20220120  for：replaceobjectNameprefix，preventkeyInconsistency results in failure to obtain files----
            objectName = OssBootUtil.replacePrefix(objectName,bucketName);
            //update-end---author:liusq  Date:20220120  for：replaceobjectNameprefix，preventkeyInconsistency results in failure to obtain files----
            if(ossClient.doesObjectExist(bucketName,objectName)){
                URL url = ossClient.generatePresignedUrl(bucketName,objectName,expires);
                //log.info("originalurl : {}", url.toString());
                //log.info("decode url : {}", URLDecoder.decode(url.toString(), "UTF-8"));
                //【issues/4023】question ossAfter external links are transcoded，Partially invalid，About one-third；无需转编码直接return即可 #4023
                return url.toString();
            }
        }catch (Exception e){
            log.info("Failed to obtain file path" + e.getMessage()); 
        }
        return null;
    }

    /**
     * initialization oss client
     *
     * @return
     */
    private static OSSClient initOss(String endpoint, String accessKeyId, String accessKeySecret) {
        if (ossClient == null) {
            ossClient = new OSSClient(endpoint,
                    new DefaultCredentialProvider(accessKeyId, accessKeySecret),
                    new ClientConfiguration());
        }
        return ossClient;
    }


    /**
     * Upload files tooss
     * @param stream
     * @param relativePath
     * @return
     */
    public static String upload(InputStream stream, String relativePath) {
        String filePath = null;
        String fileUrl = relativePath;
        initOss(endPoint, accessKeyId, accessKeySecret);
        if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
            filePath = staticDomain + SymbolConstant.SINGLE_SLASH + relativePath;
        } else {
            filePath = "https://" + bucketName + "." + endPoint + SymbolConstant.SINGLE_SLASH + fileUrl;
        }
        PutObjectResult result = ossClient.putObject(bucketName, fileUrl.toString(),stream);
        // Set permissions(public reading)
        ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        if (result != null) {
            log.info("------OSSFile uploaded successfully------" + fileUrl);
        }
        return filePath;
    }

    /**
     * replaceprefix，preventkeyInconsistency results in failure to obtain files
     * @param objectName File upload路径 key
     * @param customBucket Custom bucket
     * @date 2022-01-20
     * @author lsq
     * @return
     */
    private static String replacePrefix(String objectName,String customBucket){
        log.info("------replacePrefix---replace前---objectName:{}",objectName);
        if(oConvertUtils.isNotEmpty(staticDomain)){
            objectName= objectName.replace(staticDomain+SymbolConstant.SINGLE_SLASH,"");
        }else{
            String newBucket = bucketName;
            if(oConvertUtils.isNotEmpty(customBucket)){
                newBucket = customBucket;
            }
            String path ="https://" + newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH;
            objectName = objectName.replace(path,"");
        }
        log.info("------replacePrefix---replace后---objectName:{}",objectName);
        return objectName;
    }
}