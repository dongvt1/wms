package org.jeecg.modules.airag.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * airagKnowledge base documentation
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
public interface IAiragKnowledgeDocService extends IService<AiragKnowledgeDoc> {

    /**
     * Rebuild document
     *
     * @param docIds
     * @return
     * @author chenrui
     * @date 2025/2/18 11:14
     */
    Result<?> rebuildDocument(String docIds);

    /**
     * Add document
     *
     * @param airagKnowledgeDoc
     * @return
     * @author chenrui
     * @date 2025/2/18 15:30
     */
    Result<?> editDocument(AiragKnowledgeDoc airagKnowledgeDoc);


    /**
     * via knowledge baseidRebuild document
     *
     * @param knowId
     * @return
     * @author chenrui
     * @date 2025/2/18 18:54
     */
    Result<?> rebuildDocumentByKnowId(String knowId);


    /**
     * via knowledge baseidDelete document
     *
     * @param knowIds
     * @return
     * @author chenrui
     * @date 2025/2/18 18:59
     */
    Result<?> removeByKnowIds(List<String> knowIds);

    /**
     * via documentationid批量Delete document
     *
     * @param docIds
     * @return
     * @author chenrui
     * @date 2025/2/18 19:16
     */
    Result<?> removeDocByIds(List<String> docIds);

    /**
     * via knowledge baseidDelete all documents
     *
     * @param knowId
     * @return
     */
    Result<?> deleteAllByKnowId(String knowId);

    /**
     * fromzipPackage import documentation
     * @param knowId
     * @param file
     * @return
     * @author chenrui
     * @date 2025/3/20 13:50
     */
    Result<?> importDocumentFromZip(String knowId, MultipartFile file);
}
