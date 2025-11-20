package org.jeecg.common.util.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @Description: Check file sensitive suffix
 * @author: lsq
 * @date: 2023Year09moon12day 15:29
 */
@Slf4j
public class SsrfFileTypeFilter {

    /**
     * Whitelist of file types allowed to be manipulated
     */
    private final static List<String> FILE_TYPE_WHITE_LIST = new ArrayList<>();
    /**Initialize file header type，Replenish what is not enough by yourself*/
    final static HashMap<String, String> FILE_TYPE_MAP = new HashMap<>();
    static {
        //Image files
        FILE_TYPE_WHITE_LIST.add("jpg");
        FILE_TYPE_WHITE_LIST.add("jpeg");
        FILE_TYPE_WHITE_LIST.add("png");
        FILE_TYPE_WHITE_LIST.add("gif");
        FILE_TYPE_WHITE_LIST.add("bmp");
        FILE_TYPE_WHITE_LIST.add("svg");
        FILE_TYPE_WHITE_LIST.add("ico");
        FILE_TYPE_WHITE_LIST.add("heic");

        //text file
        FILE_TYPE_WHITE_LIST.add("txt");
        FILE_TYPE_WHITE_LIST.add("doc");
        FILE_TYPE_WHITE_LIST.add("docx");
        FILE_TYPE_WHITE_LIST.add("pdf");
        FILE_TYPE_WHITE_LIST.add("csv");
//        FILE_TYPE_WHITE_LIST.add("xml");
        FILE_TYPE_WHITE_LIST.add("md");

        //Audio and video files
        FILE_TYPE_WHITE_LIST.add("mp4");
        FILE_TYPE_WHITE_LIST.add("avi");
        FILE_TYPE_WHITE_LIST.add("mov");
        FILE_TYPE_WHITE_LIST.add("wmv");
        FILE_TYPE_WHITE_LIST.add("mp3");
        FILE_TYPE_WHITE_LIST.add("wav");

        //form file
        FILE_TYPE_WHITE_LIST.add("xls");
        FILE_TYPE_WHITE_LIST.add("xlsx");

        //compressed file
        FILE_TYPE_WHITE_LIST.add("zip");
        FILE_TYPE_WHITE_LIST.add("rar");
        FILE_TYPE_WHITE_LIST.add("7z");
        FILE_TYPE_WHITE_LIST.add("tar");

        //appfile suffix
        FILE_TYPE_WHITE_LIST.add("apk");
        FILE_TYPE_WHITE_LIST.add("wgt");

        //幻灯片file suffix
        FILE_TYPE_WHITE_LIST.add("ppt");
        FILE_TYPE_WHITE_LIST.add("pptx");

        //Set header tags for prohibited files
        FILE_TYPE_MAP.put("3c25402070616765206c", "jsp");
        FILE_TYPE_MAP.put("3c3f7068700a0a2f2a2a0a202a205048", "php");
        FILE_TYPE_MAP.put("cafebabe0000002e0041", "class");
        FILE_TYPE_MAP.put("494e5345525420494e54", "sql");
       /* fileTypeMap.put("ffd8ffe000104a464946", "jpg");
        fileTypeMap.put("89504e470d0a1a0a0000", "png");
        fileTypeMap.put("47494638396126026f01", "gif");
        fileTypeMap.put("49492a00227105008037", "tif");
        fileTypeMap.put("424d228c010000000000", "bmp");
        fileTypeMap.put("424d8240090000000000", "bmp");
        fileTypeMap.put("424d8e1b030000000000", "bmp");
        fileTypeMap.put("41433130313500000000", "dwg");
        fileTypeMap.put("3c21444f435459504520", "html");
        fileTypeMap.put("3c21646f637479706520", "htm");
        fileTypeMap.put("48544d4c207b0d0a0942", "css");
        fileTypeMap.put("696b2e71623d696b2e71", "js");
        fileTypeMap.put("7b5c727466315c616e73", "rtf");
        fileTypeMap.put("38425053000100000000", "psd");
        fileTypeMap.put("46726f6d3a203d3f6762", "eml");
        fileTypeMap.put("d0cf11e0a1b11ae10000", "doc");
        fileTypeMap.put("5374616E64617264204A", "mdb");
        fileTypeMap.put("252150532D41646F6265", "ps");
        fileTypeMap.put("255044462d312e350d0a", "pdf");
        fileTypeMap.put("2e524d46000000120001", "rmvb");
        fileTypeMap.put("464c5601050000000900", "flv");
        fileTypeMap.put("00000020667479706d70", "mp4");
        fileTypeMap.put("49443303000000002176", "mp3");
        fileTypeMap.put("000001ba210001000180", "mpg");
        fileTypeMap.put("3026b2758e66cf11a6d9", "wmv");
        fileTypeMap.put("52494646e27807005741", "wav");
        fileTypeMap.put("52494646d07d60074156", "avi");
        fileTypeMap.put("4d546864000000060001", "mid");
        fileTypeMap.put("504b0304140000000800", "zip");
        fileTypeMap.put("526172211a0700cf9073", "rar");
        fileTypeMap.put("235468697320636f6e66", "ini");
        fileTypeMap.put("504b03040a0000000000", "jar");
        fileTypeMap.put("4d5a9000030000000400", "exe");
        fileTypeMap.put("3c25402070616765206c", "jsp");
        fileTypeMap.put("4d616e69666573742d56", "mf");
        fileTypeMap.put("3c3f786d6c2076657273", "xml");
        fileTypeMap.put("494e5345525420494e54", "sql");
        fileTypeMap.put("7061636b616765207765", "java");
        fileTypeMap.put("406563686f206f66660d", "bat");
        fileTypeMap.put("1f8b0800000000000000", "gz");
        fileTypeMap.put("6c6f67346a2e726f6f74", "properties");
        fileTypeMap.put("cafebabe0000002e0041", "class");
        fileTypeMap.put("49545346030000006000", "chm");
        fileTypeMap.put("04000000010000001300", "mxp");
        fileTypeMap.put("504b0304140006000800", "docx");
        fileTypeMap.put("6431303a637265617465", "torrent");
        fileTypeMap.put("6D6F6F76", "mov");
        fileTypeMap.put("FF575043", "wpd");
        fileTypeMap.put("CFAD12FEC5FD746F", "dbx");
        fileTypeMap.put("2142444E", "pst");
        fileTypeMap.put("AC9EBD8F", "qdf");
        fileTypeMap.put("E3828596", "pwl");
        fileTypeMap.put("2E7261FD", "ram");*/
    }

    /**
     * @param fileName
     * @return String
     * @description 通过file suffix名获取File type
     */
    private static String getFileTypeBySuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }


    /**
     * Download file type filtering
     *
     * @param filePath
     */
    public static void checkDownloadFileType(String filePath) throws IOException {
        //file suffix
        String suffix = getFileTypeBySuffix(filePath);
        log.debug(" 【File download verification】file suffix suffix: {}", suffix);
        boolean isAllowExtension = FILE_TYPE_WHITE_LIST.contains(suffix.toLowerCase());
        //Whether to allow downloaded files
        if (!isAllowExtension) {
            throw new JeecgBootException("Download failed，Illegal file type exists：" + suffix);
        }
    }

    /**
     * Upload file type filtering
     *
     * @param file
     */
    public static void checkUploadFileType(MultipartFile file) throws Exception {
        checkUploadFileType(file, null);
    }
    
    /**
     * Upload file type filtering
     *
     * @param file
     */
    public static void checkUploadFileType(MultipartFile file, String customPath) throws Exception {
        //1. Path security verification
        validatePathSecurity(customPath);
        //2. 校验file suffix和头
        String suffix = getFileType(file, customPath);
        log.info("【File upload verification】file suffix suffix: {}，customPath：{}", suffix, customPath);
        boolean isAllowExtension = FILE_TYPE_WHITE_LIST.contains(suffix.toLowerCase());
        //Whether to allow downloaded files
        if (!isAllowExtension) {
            throw new JeecgBootException("Upload failed，Illegal file type exists：" + suffix);
        }
    }

    /**
     * Get the file type by reading the file header
     *
     * @param file
     * @return File type
     * @throws Exception
     */

    private static String getFileType(MultipartFile file, String customPath) throws Exception {
        //update-begin-author:liusq date:20230404 for: [issue/4672]The file caused by the method is occupied，Comment out this methodtomcatAutomatically clean up temporary files
        String fileExtendName = null;
        InputStream is = null;
        try {
            //is = new FileInputStream(file);
            is = file.getInputStream();
            byte[] b = new byte[10];
            is.read(b, 0, b.length);
            String fileTypeHex = String.valueOf(bytesToHexString(b));
            Iterator<String> keyIter = FILE_TYPE_MAP.keySet().iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                // Before verification5characters compared
                if (key.toLowerCase().startsWith(fileTypeHex.toLowerCase().substring(0, 5))
                        || fileTypeHex.toLowerCase().substring(0, 5).startsWith(key.toLowerCase())) {
                    fileExtendName = FILE_TYPE_MAP.get(key);
                    break;
                }
            }
            log.debug("-----获取到的指定File type------"+fileExtendName);
            // If it is not the above type，Then determine the extension
            if (StringUtils.isBlank(fileExtendName)) {
                String fileName = file.getOriginalFilename();
                // If no extension，will directly return the empty string
                if (-1 == fileName.indexOf(".")) {
                    return "";
                }
                // If there is an extension，then returns the extension
                return getFileTypeBySuffix(fileName);
            }
            is.close();
            return fileExtendName;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }finally {
            if (is != null) {
                is.close();
            }
        }
        //update-end-author:liusq date:20230404 for: [issue/4672]The file caused by the method is occupied，Comment out this methodtomcatAutomatically clean up temporary files
    }

    /**
     * Get file header string
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Path security verification
     */
    private static void validatePathSecurity(String customPath) throws JeecgBootException {
        if (customPath == null || customPath.trim().isEmpty()) {
            return;
        }

        // The unified delimiter is /
        String normalized = customPath.replace("\\", "/");

        // 1. Prevent path traversal attacks
        if (normalized.contains("..") || normalized.contains("~")) {
            throw new JeecgBootException("The upload service path contains illegal characters！");
        }

        // 2. Limit path depth
        int depth = normalized.split("/").length;
        if (depth > 5) {
            throw new JeecgBootException("The depth of the upload service path exceeds the limit！");
        }

        // 3. Restrict character set（Only letters allowed、number、Underline、horizontal line、slash）
        if (!normalized.matches("^[a-zA-Z0-9/_-]+$")) {
            throw new JeecgBootException("The upload service path contains illegal characters！");
        }
    }

    
}
