package org.jeecg.common.util;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.filter.StrAttackFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;

/**
 * minioFile upload tool class
 * @author: jeecg-boot
 */
@Slf4j
public class MinioUtil {
    private static String minioUrl;
    private static String minioName;
    private static String minioPass;
    private static String bucketName;

    public static void setMinioUrl(String minioUrl) {
        MinioUtil.minioUrl = minioUrl;
    }

    public static void setMinioName(String minioName) {
        MinioUtil.minioName = minioName;
    }

    public static void setMinioPass(String minioPass) {
        MinioUtil.minioPass = minioPass;
    }

    public static void setBucketName(String bucketName) {
        MinioUtil.bucketName = bucketName;
    }

    public static String getMinioUrl() {
        return minioUrl;
    }

    public static String getBucketName() {
        return bucketName;
    }

    private static MinioClient minioClient = null;

    /**
     * Upload files
     * @param file
     * @return
     */
    public static String upload(MultipartFile file, String bizPath, String customBucket) throws Exception {
        String fileUrl = "";
        // Business path filtering，Prevent attacks
        bizPath = StrAttackFilter.filter(bizPath);

        // File security check，Prevent vulnerable files from being uploaded
        SsrfFileTypeFilter.checkUploadFileType(file, bizPath);

        String newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(customBucket)){
            newBucket = customBucket;
        }
        try {
            initMinio(minioUrl, minioName,minioPass);
            // Check if the bucket already exists
            if(minioClient.bucketExists(BucketExistsArgs.builder().bucket(newBucket).build())) {
                log.info("Bucket already exists.");
            } else {
                // Create a file calledotabucket
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(newBucket).build());
                log.info("create a new bucket.");
            }
            InputStream stream = file.getInputStream();
            // Get file name
            String orgName = file.getOriginalFilename();
            if("".equals(orgName)){
                orgName=file.getName();
            }
            orgName = CommonUtils.getFileName(orgName);
            String objectName = bizPath+"/"
                                +( orgName.indexOf(".")==-1
                                   ?orgName + "_" + System.currentTimeMillis()
                                   :orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."))
                                 );

            // useputObjectUpload a local file to the bucket。
            if(objectName.startsWith(SymbolConstant.SINGLE_SLASH)){
                objectName = objectName.substring(1);
            }
            PutObjectArgs objectArgs = PutObjectArgs.builder().object(objectName)
                    .bucket(newBucket)
                    .contentType("application/octet-stream")
                    .stream(stream,stream.available(),-1).build();
            minioClient.putObject(objectArgs);
            stream.close();
            fileUrl = minioUrl+newBucket+"/"+objectName;
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return fileUrl;
    }

    /**
     * File upload
     * @param file
     * @param bizPath
     * @return
     */
    public static String upload(MultipartFile file, String bizPath) throws Exception {
        return upload(file,bizPath,null);
    }

    /**
     * Get file stream
     * @param bucketName
     * @param objectName
     * @return
     */
    public static InputStream getMinioFile(String bucketName,String objectName){
        InputStream inputStream = null;
        try {
            initMinio(minioUrl, minioName, minioPass);
            GetObjectArgs objectArgs = GetObjectArgs.builder().object(objectName)
                    .bucket(bucketName).build();
            inputStream = minioClient.getObject(objectArgs);
        } catch (Exception e) {
            log.info("File retrieval failed" + e.getMessage());
        }
        return inputStream;
    }

    /**
     * Delete files
     * @param bucketName
     * @param objectName
     * @throws Exception
     */
    public static void removeObject(String bucketName, String objectName) {
        try {
            initMinio(minioUrl, minioName,minioPass);
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().object(objectName)
                    .bucket(bucketName).build();
            minioClient.removeObject(objectArgs);
        }catch (Exception e){
            log.info("File deletion failed" + e.getMessage());
        }
    }

    /**
     * Get file external link
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    public static String getObjectUrl(String bucketName, String objectName, Integer expires) {
        initMinio(minioUrl, minioName,minioPass);
        try{
            //update-begin---author:liusq  Date:20220121  for：Get file external link报错提示methodcannot be empty，Causes file download and preview to fail----
            GetPresignedObjectUrlArgs objectArgs = GetPresignedObjectUrlArgs.builder().object(objectName)
                    .bucket(bucketName)
                    .expiry(expires).method(Method.GET).build();
            //update-begin---author:liusq  Date:20220121  for：Get file external link报错提示methodcannot be empty，Causes file download and preview to fail----
            String url = minioClient.getPresignedObjectUrl(objectArgs);
            return URLDecoder.decode(url,"UTF-8");
        }catch (Exception e){
            log.info("Failed to obtain file path" + e.getMessage());
        }
        return null;
    }

    /**
     * Initialize client
     * @param minioUrl
     * @param minioName
     * @param minioPass
     * @return
     */
    private static MinioClient initMinio(String minioUrl, String minioName,String minioPass) {
        if (minioClient == null) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint(minioUrl)
                        .credentials(minioName, minioPass)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return minioClient;
    }

    /**
     * Upload files到minio
     * @param stream
     * @param relativePath
     * @return
     */
    public static String upload(InputStream stream,String relativePath) throws Exception {
        initMinio(minioUrl, minioName,minioPass);
        if(minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.info("Bucket already exists.");
        } else {
            // Create a file calledotabucket
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("create a new bucket.");
        }
        PutObjectArgs objectArgs = PutObjectArgs.builder().object(relativePath)
                .bucket(bucketName)
                .contentType("application/octet-stream")
                .stream(stream,stream.available(),-1).build();
        minioClient.putObject(objectArgs);
        stream.close();
        return minioUrl+bucketName+"/"+relativePath;
    }

}
