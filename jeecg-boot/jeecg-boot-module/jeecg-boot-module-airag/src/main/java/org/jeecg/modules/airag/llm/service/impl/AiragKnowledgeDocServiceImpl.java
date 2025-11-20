package org.jeecg.modules.airag.llm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.*;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.jeecg.modules.airag.llm.handler.EmbeddingHandler;
import org.jeecg.modules.airag.llm.mapper.AiragKnowledgeDocMapper;
import org.jeecg.modules.airag.llm.mapper.AiragKnowledgeMapper;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jeecg.modules.airag.llm.consts.LLMConsts.*;

/**
 * @Description: airagKnowledge base documentation
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@Slf4j
@Service
public class AiragKnowledgeDocServiceImpl extends ServiceImpl<AiragKnowledgeDocMapper, AiragKnowledgeDoc> implements IAiragKnowledgeDocService {

    @Autowired
    private AiragKnowledgeDocMapper airagKnowledgeDocMapper;

    @Autowired
    private AiragKnowledgeMapper airagKnowledgeMapper;

    @Autowired
    EmbeddingHandler embeddingHandler;


    @Value(value = "${jeecg.path.upload:}")
    private String uploadpath;

    /**
     * Supported document types
     */
    private static final List<String> SUPPORT_DOC_TYPE = Arrays.asList("txt", "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "md");

    /**
     * Vectorized thread pool size
     */
    private static final int THREAD_POOL_SIZE = 10;

    /**
     * Vectorized document thread pool
     */
    private static final ExecutorService buildDocExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    // Unzip the file:Maximum single file150MB
    private static final long MAX_FILE_SIZE = 150 * 1024 * 1024;
    // Unzip the file:Total decompressed size1024MB
    private static final long MAX_TOTAL_SIZE = 1024 * 1024 * 1024;
    // Unzip the file:Maximum decompression10000indivualEntry
    private static final int MAX_ENTRY_COUNT = 10000;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Result<?> editDocument(AiragKnowledgeDoc airagKnowledgeDoc) {
        AssertUtils.assertNotEmpty("Document cannot be empty", airagKnowledgeDoc);
        AssertUtils.assertNotEmpty("The knowledge base cannot be empty", airagKnowledgeDoc.getKnowledgeId());
        AssertUtils.assertNotEmpty("Document title cannot be empty", airagKnowledgeDoc.getTitle());
        AssertUtils.assertNotEmpty("Document type cannot be empty", airagKnowledgeDoc.getType());
        if (KNOWLEDGE_DOC_TYPE_TEXT.equals(airagKnowledgeDoc.getType())) {
            AssertUtils.assertNotEmpty("Document content cannot be empty", airagKnowledgeDoc.getContent());
        }

        airagKnowledgeDoc.setStatus(KNOWLEDGE_DOC_STATUS_DRAFT);
        // Save to database
        if (this.saveOrUpdate(airagKnowledgeDoc)) {
            // Reconstruction vector
            return this.rebuildDocument(airagKnowledgeDoc.getId());
        } else {
            return Result.error("Save failed");
        }
    }

    @Override
    public Result<?> rebuildDocumentByKnowId(String knowId) {
        AssertUtils.assertNotEmpty("knowledge baseidcannot be empty", knowId);
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectList(Wrappers.lambdaQuery(AiragKnowledgeDoc.class).eq(AiragKnowledgeDoc::getKnowledgeId, knowId));
        if (oConvertUtils.isObjectEmpty(docList)) {
            return Result.OK();
        }
        String docIds = docList.stream().map(AiragKnowledgeDoc::getId).collect(Collectors.joining(","));
        return rebuildDocument(docIds);
    }

    @Transactional(rollbackFor = {java.lang.Exception.class})
    @Override
    public Result<?> rebuildDocument(String docIds) {
        AssertUtils.assertNotEmpty("Please select a document to rebuild", docIds);
        List<String> docIdList = Arrays.asList(docIds.split(","));
        // Query data
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectBatchIds(docIdList);
        AssertUtils.assertNotEmpty("Document does not exist", docList);

        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        // check status
        List<AiragKnowledgeDoc> knowledgeDocs = docList.stream()
                .filter(doc -> {
                    //update-begin---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】aiknowledge base Document uploaded It always shows that it is under construction？------------
                    if(KNOWLEDGE_DOC_STATUS_BUILDING.equalsIgnoreCase(doc.getStatus())){
                        Date updateTime = doc.getUpdateTime();
                        if (updateTime != null) {
                            // Vectorization exceeds5minute,Revectorize
                            long timeDifference = System.currentTimeMillis() - updateTime.getTime();
                            return timeDifference > 5 * 60 * 1000;
                        }else{
                            return true;
                        }
                    } else {
                        return true;
                    }
                    //update-end---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】aiknowledge base Document uploaded It always shows that it is under construction？------------
                })
                .peek(doc -> {
                    doc.setStatus(KNOWLEDGE_DOC_STATUS_BUILDING);
                })
                .collect(Collectors.toList());
        if (oConvertUtils.isObjectEmpty(knowledgeDocs)) {
            return Result.ok("Operation successful");
        }
        if (oConvertUtils.isObjectEmpty(knowledgeDocs)) {
            return Result.ok("Operation successful");
        }
        // update status
        this.updateBatchById(knowledgeDocs);
        // Asynchronously rebuild documents
        String tenantId = TenantContext.getTenant();
        String token = TokenUtils.getTokenByRequest();
        knowledgeDocs.forEach((doc) -> {
            CompletableFuture.runAsync(() -> {
                UserTokenContext.setToken(token);
                TenantContext.setTenant(tenantId);
                String knowId = doc.getKnowledgeId();
                log.info("Start rebuilding the document, knowledge baseid: {}, documentid: {}", knowId, doc.getId());
                doc.setStatus(KNOWLEDGE_DOC_STATUS_BUILDING);
                this.updateById(doc);
                //update-begin---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】aiknowledge base Document uploaded It always shows that it is under construction？------------
                try {
                    Map<String, Object> metadata = embeddingHandler.embeddingDocument(knowId, doc);
                    // Update data date:2025/2/18
                    if (null != metadata) {
                        doc.setStatus(KNOWLEDGE_DOC_STATUS_COMPLETE);
                        this.updateById(doc);
                        log.info("重建documentsuccess, knowledge baseid: {}, documentid: {}", knowId, doc.getId());
                    } else {
                        this.handleDocBuildFailed(doc, "Vectorization failed");
                        log.info("重建document失败, knowledge baseid: {}, documentid: {}", knowId, doc.getId());
                    }
                }catch (Throwable t){
                    this.handleDocBuildFailed(doc, t.getMessage());
                    log.error("重建document失败:" + t.getMessage() + ", knowledge baseid: " + knowId + ", documentid: " + doc.getId(), t);
                }
                //update-end---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】aiknowledge base Document uploaded It always shows that it is under construction？------------
            }, buildDocExecutorService);
        });
        log.info("返回Operation successful");
        return Result.ok("Operation successful");
    }

    /**
     * 处理document构建失败
     */
    private void handleDocBuildFailed(AiragKnowledgeDoc doc, String failedReason) {
        doc.setStatus(KNOWLEDGE_DOC_STATUS_FAILED);

        String metadataStr = doc.getMetadata();
        JSONObject metadata;
        if (oConvertUtils.isEmpty(metadataStr)) {
            metadata = new JSONObject();
        } else {
            metadata = JSONObject.parseObject(metadataStr);
        }
        metadata.put("failedReason", failedReason);
        doc.setMetadata(metadata.toJSONString());

        this.updateById(doc);
    }

    @Override
    public Result<?> removeByKnowIds(List<String> knowIds) {
        AssertUtils.assertNotEmpty("选择knowledge base", knowIds);
        for (String knowId : knowIds) {
            AiragKnowledge airagKnowledge = airagKnowledgeMapper.selectById(knowId);
            AssertUtils.assertNotEmpty("knowledge base不存在", airagKnowledge);
            AssertUtils.assertNotEmpty("请先为knowledge base配置向量模型库", airagKnowledge.getEmbedId());
            // Asynchronously delete vector data
            final String embedId = airagKnowledge.getEmbedId();
            final String finalKnowId = knowId;
            CompletableFuture.runAsync(() -> {
                try {
                    embeddingHandler.deleteEmbedDocsByKnowId(finalKnowId, embedId);
                } catch (Throwable ignore) {
                }
            });
            // Delete data
            airagKnowledgeDocMapper.deleteByMainId(knowId);
        }
        return Result.OK();
    }

    @Override
    public Result<?> removeDocByIds(List<String> docIds) {
        AssertUtils.assertNotEmpty("请选择要删除的document", docIds);
        // Query data
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectBatchIds(docIds);
        AssertUtils.assertNotEmpty("Document does not exist", docList);
        // Organize data
        Map<String, List<String>> knowledgeDocs = docList.stream().collect(Collectors.groupingBy(
                AiragKnowledgeDoc::getKnowledgeId,
                Collectors.mapping(AiragKnowledgeDoc::getId, Collectors.toList())
        ));
        if (oConvertUtils.isObjectEmpty(knowledgeDocs)) {
            return Result.ok("success");
        }
        knowledgeDocs.forEach((knowId, groupedDocIds) -> {
            AiragKnowledge airagKnowledge = airagKnowledgeMapper.selectById(knowId);
            AssertUtils.assertNotEmpty("knowledge base不存在", airagKnowledge);
            AssertUtils.assertNotEmpty("请先为knowledge base配置向量模型库", airagKnowledge.getEmbedId());
            // Asynchronously delete vector data
            final String embedId = airagKnowledge.getEmbedId();
            final List<String> docIdsToDelete = new ArrayList<>(groupedDocIds);
            CompletableFuture.runAsync(() -> {
                try {
                    embeddingHandler.deleteEmbedDocsByDocIds(docIdsToDelete, embedId);
                } catch (Throwable ignore) {
                }
            });
            // Delete data
            airagKnowledgeDocMapper.deleteBatchIds(groupedDocIds);
        });
        return Result.ok("success");
    }

    @Override
    public Result<?> deleteAllByKnowId(String knowId) {
        if (oConvertUtils.isEmpty(knowId)) {
            return Result.error("knowledge baseidcannot be empty");
        }
        LambdaQueryWrapper<AiragKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiragKnowledgeDoc::getKnowledgeId, knowId);
        //noinspection unchecked
        wrapper.select(AiragKnowledgeDoc::getId);
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectList(wrapper);
        if (docList.isEmpty()) {
            return Result.ok("暂无document");
        }
        List<String> docIds = docList.stream().map(AiragKnowledgeDoc::getId).collect(Collectors.toList());
        this.removeDocByIds(docIds);
        return Result.ok("Clearing completed");
    }

    @Transactional(rollbackFor = {java.lang.Exception.class})
    @Override
    public Result<?> importDocumentFromZip(String knowId, MultipartFile zipFile) {
        AssertUtils.assertNotEmpty("请先选择knowledge base", knowId);
        AssertUtils.assertNotEmpty("Please upload files", zipFile);
        long startTime = System.currentTimeMillis();
        log.info("开始上传Knowledge base documentation(zip), knowledge baseid: {}, file name: {}", knowId, zipFile.getOriginalFilename());

        try {
            String bizPath = knowId + File.separator + UUIDGenerator.generate();
            String workDir = uploadpath + File.separator + bizPath + File.separator;
            String sourcesPath = workDir + "files";

            SsrfFileTypeFilter.checkUploadFileType(zipFile);
            // passfilePath Check if the file is a compressed package(zip)
            String zipFileName = FilenameUtils.getBaseName(zipFile.getOriginalFilename());
            String fileExt = FilenameUtils.getExtension(zipFile.getOriginalFilename());
            if (null == fileExt || !fileExt.equalsIgnoreCase("zip")) {
                throw new JeecgBootException("Please uploadzipCompressed package");
            }
            String uploadedZipPath = CommonUtils.uploadLocal(zipFile, bizPath, uploadpath);
            // Unzip files
            List<AiragKnowledgeDoc> docList = new ArrayList<>();
            AtomicInteger fileCount = new AtomicInteger(0);
            unzipFile(uploadpath + File.separator + uploadedZipPath, sourcesPath, uploadedFile -> {
                // Only supportstxt、pdf、docx、pptx、html、mddocument
                String fileName = uploadedFile.getName();
                if (!SUPPORT_DOC_TYPE.contains(FilenameUtils.getExtension(fileName).toLowerCase())) {
                    log.warn("不支持的document类型: {}", fileName);
                    return;
                }
                String baseName = FilenameUtils.getBaseName(fileName);
                AiragKnowledgeDoc doc = new AiragKnowledgeDoc();
                doc.setKnowledgeId(knowId);
                doc.setTitle(baseName);
                doc.setType(LLMConsts.KNOWLEDGE_DOC_TYPE_FILE);
                doc.setStatus(LLMConsts.KNOWLEDGE_DOC_STATUS_DRAFT);

                String relativePath;
                if (File.separator.equals("\\")) {
                    // Windows path handling
                    String escapedPath = uploadpath.replace("//", "\\\\");
                    relativePath = uploadedFile.getPath().replaceFirst("^" + escapedPath, "");
                } else {
                    // Unix path handling
                    relativePath = uploadedFile.getPath().replaceFirst("^" + uploadpath, "");
                }
                JSONObject metadata = new JSONObject();
                metadata.put(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH, relativePath);
                metadata.put(LLMConsts.KNOWLEDGE_DOC_METADATA_SOURCES_PATH, sourcesPath);
                doc.setMetadata(metadata.toJSONString());
                docList.add(doc);
            });
            AssertUtils.assertNotEmpty("Compressed package中没有符合要求的document", docList);
            // save data
            this.saveBatch(docList);
            // 重建document
            String docIds = docList.stream().map(AiragKnowledgeDoc::getId).filter(oConvertUtils::isObjectNotEmpty).collect(Collectors.joining(","));
            rebuildDocument(docIds);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.info("上传Knowledge base documentation(zip)success, knowledge baseid: {}, file name: {}, time consuming: {}ms", knowId, zipFile.getOriginalFilename(), (System.currentTimeMillis() - startTime));
        return Result.ok("上传success");
    }

    /**
     * Unzip files
     *
     * @param zipFilePath 压缩document路径
     * @param destDir    目标document夹
     * @param afterExtract Callback after decompression is complete
     * @throws IOException
     * @author chenrui
     * @date 2025/3/20 14:37
     */
    public static void unzipFile(String zipFilePath, String destDir, Consumer<File> afterExtract) throws IOException {
        unzipFile(Paths.get(zipFilePath), Paths.get(destDir), afterExtract);
    }


    /**
     * Unzip files
     *
     * @param zipFilePath  压缩document路径
     * @param targetDir    目标document夹
     * @param afterExtract Callback after decompression is complete
     * @throws IOException
     * @author chenrui
     * @date 2025/4/28 17:02
     */
    private static void unzipFile(Path zipFilePath, Path targetDir, Consumer<File> afterExtract) throws IOException {
        long totalUnzippedSize = 0;
        int entryCount = 0;

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                entryCount++;
                if (entryCount > MAX_ENTRY_COUNT) {
                    throw new IOException("Unzip the file数量超限，may bezip bombattack");
                }

                Path newPath = safeResolve(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    try (InputStream is = zipFile.getInputStream(entry);
                         OutputStream os = Files.newOutputStream(newPath)) {

                        long bytesCopied = copyLimited(is, os, MAX_FILE_SIZE);
                        totalUnzippedSize += bytesCopied;

                        if (totalUnzippedSize > MAX_TOTAL_SIZE) {
                            throw new IOException("The total decompressed size exceeds the limit，may bezip bombattack");
                        }
                    }

                    // Callback after decompression is complete
                    if (afterExtract != null) {
                        afterExtract.accept(newPath.toFile());
                    }
                }
            }
        }
    }

    /**
     * Safe parsing path，preventZip Slipattack
     *
     * @param targetDir
     * @param entryName
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/4/28 16:46
     */
    private static Path safeResolve(Path targetDir, String entryName) throws IOException {
        Path resolvedPath = targetDir.resolve(entryName).normalize();
        if (!resolvedPath.startsWith(targetDir)) {
            throw new IOException("ZIP 路径穿越attack被阻止:" + entryName);
        }
        return resolvedPath;
    }

    /**
     * Copy input stream to output stream，and limit the maximum number of bytes
     *
     * @param in
     * @param out
     * @param maxBytes
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/4/28 17:03
     */
    private static long copyLimited(InputStream in, OutputStream out, long maxBytes) throws IOException {
        byte[] buffer = new byte[8192];
        long totalCopied = 0;
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            totalCopied += bytesRead;
            if (totalCopied > maxBytes) {
                throw new IOException("单indivualdocument解压超限，may bezip bombattack");
            }
            out.write(buffer, 0, bytesRead);
        }
        return totalCopied;
    }

}
