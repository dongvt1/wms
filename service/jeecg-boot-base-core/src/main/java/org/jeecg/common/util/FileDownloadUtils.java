package org.jeecg.common.util;

import jakarta.servlet.http.HttpServletResponse;
import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @program: file
 * @description: File download
 * @author: chenrui
 * @date: 2019-05-24 16:34
 **/
@Slf4j
public class FileDownloadUtils {

    /**
     * 单File download
     *
     * @param response
     * @param storePath Download file storage address
     * @param fileName  File name
     * @author: chenrui
     * @date: 2019/5/24 17:10
     */
    public static void downloadFile(HttpServletResponse response, String storePath, String fileName) {
        response.setCharacterEncoding("UTF-8");
        File file = new File(storePath);
        if (!file.exists()) {
            throw new NullPointerException("Specified file not found");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new NullPointerException("The file name can not null");
        }
        // 配置File download
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        // The downloaded file can display Chinese normally
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        // 实现File download
        byte[] buffer = new byte[1024];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);) {
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 多File download
     *
     * @param filesPath   Download file collection
     * @param zipFileName Multiple file names
     * @author: chenrui
     * @date: 2019/5/24 17:48
     */
    public static void downloadFileMulti(HttpServletResponse response, List<String> filesPath, String zipFileName) throws IOException {
        //Set the name of the compressed package
        String downloadName = zipFileName + ".zip";
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(downloadName, "UTF-8"));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        log.info("Start compressing files:" + filesPath);
        //Set up compressed stream：write directlyresponse，Achieve downloading while compressing
        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
             DataOutputStream os = new DataOutputStream(zipOut);) {
            //Set compression method
            zipOut.setMethod(ZipOutputStream.DEFLATED);
            for (String filePath : filesPath) {
                //Loop to write files to compressed stream
                File file = new File(filePath);
                if (file.exists()) {
                    //Add toZipEntry，andZipEntryWriting to a file stream is to push the file intozipfile directory
                    String fileName = file.getName();
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    //Format output stream file

                    InputStream is = Files.newInputStream(file.toPath());
                    byte[] b = new byte[1024];
                    int length;
                    while ((length = is.read(b)) != -1) {
                        os.write(b, 0, length);
                    }
                    is.close();
                    zipOut.closeEntry();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException(e);
        }
    }

    /**
     * Download network resources to disk
     *
     * @param fileUrl
     * @param storePath
     * @author chenrui
     * @date 2024/1/19 10:09
     */
    public static String download2DiskFromNet(String fileUrl, String storePath) {
        try {
            URL url = new URL(fileUrl);
            URLConnection conn = url.openConnection();
            // Set the timeout to3Second
            conn.setConnectTimeout(3 * 1000);
            // Prevent blocking programs
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            // Make sure the directory exists
            File file = ensureDestFileDir(storePath);
            try (InputStream inStream = conn.getInputStream();
                 FileOutputStream fs = new FileOutputStream(file);) {
                int byteread;
                byte[] buffer = new byte[1204];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                return storePath;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new JeecgBootException(e);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException(e);
        }
    }


    /**
     * Get files with non-duplicate names
     *
     * @param file
     * @return
     * @author chenrui
     * @date 2017Year5moon24afternoon6:29:13
     * @version v0.0.1
     */
    public static File getUniqueFile(final File file) {
        if (!file.exists()) {
            return file;
        }

        File tmpFile = new File(file.getAbsolutePath());
        File parentDir = tmpFile.getParentFile();
        int count = 1;
        String extension = FilenameUtils.getExtension(tmpFile.getName());
        String baseName = FilenameUtils.getBaseName(tmpFile.getName());
        do {
            tmpFile = new File(parentDir, baseName + "(" + count++ + ")." + extension);
        } while (tmpFile.exists());
        return tmpFile;
    }

    /**
     * Make sure the output file directory
     *
     * @param destFilePath
     * @return
     * @author: chenrui
     * @date: 2019-05-21 16:49
     */
    private static File ensureDestFileDir(String destFilePath) {
        File destFile = new File(destFilePath);
        FileDownloadUtils.checkDirAndCreate(destFile.getParentFile());
        return destFile;
    }

    /**
     * Verify folder exists and create directory
     *
     * @param dir
     * @author chenrui
     * @date 2017Year5moon24afternoon6:29:24
     * @version v0.0.1
     */
    public static void checkDirAndCreate(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    /**
     * Download a single file toZIPflow
     * Core functions：获取documentflow，writeZIPentry
     * @param fileUrl documentURL（can beHTTP URLor local path）
     * @param fileName ZIP内的document名
     * @param zous ZIP输出flow
     */
    public static void downLoadSingleFile(String fileUrl, String fileName, String uploadUrl,ZipArchiveOutputStream zous) {
        InputStream inputStream = null;
        try {
            // createZIPentry：每个document在ZIP中都是一个独立entry
            ZipArchiveEntry entry = new ZipArchiveEntry(fileName);
            zous.putArchiveEntry(entry);

            // 获取document输入flow：区分普通document和快捷方式
            if (fileUrl.endsWith(".url")) {
                // Handle shortcuts：generate.urldocument内容
                inputStream = FileDownloadUtils.createInternetShortcut(fileName, fileUrl, "");
            } else {
                // 普通File download：fromURLor local path获取flow
                inputStream = getDownInputStream(fileUrl,uploadUrl);
            }

            if (inputStream != null) {
                // 将documentflowwriteZIP
                IOUtils.copy(inputStream, zous);
            }
            // Close currentZIPentry
            zous.closeArchiveEntry();
        } catch (IOException e) {
            log.error("File download失败: {}",  e);
        } finally {
            // 确保输入flow关闭
            IoUtil.close(inputStream);
        }
    }

    /**
     * 获取下载document输入flow
     * Function：according toURLtype（HTTPor local）获取documentflow
     * @param fileUrl documentURL（supportHTTPand local path）
     * @return document输入flow，Return on failurenull
     */
    public static InputStream getDownInputStream(String fileUrl, String uploadUrl) {
        try {
            // deal withHTTP URL：Download via the Internet
            if (oConvertUtils.isNotEmpty(fileUrl) && fileUrl.startsWith(CommonConstant.STR_HTTP)) {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000); // Connection timeout5Second
                connection.setReadTimeout(30000);  // Read timeout30Second
                return connection.getInputStream();
            } else {
                // deal with本地document：直接读取document系统
                String downloadFilePath = uploadUrl + File.separator + fileUrl;
                // security check：防止下载危险documenttype
                SsrfFileTypeFilter.checkDownloadFileType(downloadFilePath);
                return new BufferedInputStream(new FileInputStream(downloadFilePath));
            }
        } catch (IOException e) {
            // Return on exceptionnull，上层会deal with空flow情况
            return null;
        }
    }

    /**
     * 获取document扩展名
     * Function：fromdocument名中提取扩展名
     * @param fileName document名
     * @return document扩展名（Does not include dots），like"txt"、"png"
     */
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    /**
     * create快捷方式（.urldocument内容）
     * Function：generateInternet快捷方式document内容
     * @param name shortcut name
     * @param url TargetURLaddress
     * @param icon icon path（Optional）
     * @return Include.urldocument内容的输入flow
     */
    public static InputStream createInternetShortcut(String name, String url, String icon) {
        StringWriter sw = new StringWriter();
        try {
            // according toWindows快捷方式格式write内容
            sw.write("[InternetShortcut]\n");
            sw.write("URL=" + url + "\n");
            if (oConvertUtils.isNotEmpty(icon)) {
                sw.write("IconFile=" + icon + "\n");
            }
            // 将字符串内容转换为输入flow
            return new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
        } finally {
            IoUtil.close(sw);
        }
    }
    /**
     * fromURL中提取document名
     * Function：fromHTTP URLor local path中提取纯document名
     * @param fileUrl documentURL
     * @return document名（without path）
     */
    public static String getFileNameFromUrl(String fileUrl) {
        try {
            // deal withHTTP URL：from路径部分提取document名
            if (fileUrl.startsWith(CommonConstant.STR_HTTP)) {
                URL url = new URL(fileUrl);
                String path = url.getPath();
                return path.substring(path.lastIndexOf('/') + 1);
            }

            // deal with本地document路径：fromdocument路径提取document名
            return fileUrl.substring(fileUrl.lastIndexOf(File.separator) + 1);
        } catch (Exception e) {
            // like果解析失败，使用时间戳作为document名
            return "file_" + System.currentTimeMillis();
        }
    }
    /**
     * generateZIP中的document名
     * Function：避免document名冲突，为多个documentAdd to序号
     * @param fileUrl documentURL（用于提取原始document名）
     * @param index document序号（from0start）
     * @param total document总数
     * @return deal with后的document名（With serial number）
     */
    public static String generateFileName(String fileUrl, int index, int total) {
        // fromURL中提取原始document名
        String originalFileName = getFileNameFromUrl(fileUrl);

        // like果只有一个document，直接使用原始document名
        if (total == 1) {
            return originalFileName;
        }

        // 多个document时，Use serial number+原始document名
        String extension = getFileExtension(originalFileName);
        String nameWithoutExtension = originalFileName.replace("." + extension, "");

        return String.format("%s_%d.%s", nameWithoutExtension, index + 1, extension);
    }
}
