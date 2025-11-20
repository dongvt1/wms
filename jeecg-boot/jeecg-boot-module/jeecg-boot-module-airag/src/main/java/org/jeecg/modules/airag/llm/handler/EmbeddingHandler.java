package org.jeecg.modules.airag.llm.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.parser.AutoDetectParser;
import org.jeecg.ai.factory.AiModelFactory;
import org.jeecg.ai.factory.AiModelOptions;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.*;
import org.jeecg.modules.airag.common.handler.IEmbeddingHandler;
import org.jeecg.modules.airag.common.vo.knowledge.KnowledgeSearchResult;
import org.jeecg.modules.airag.llm.config.EmbedStoreConfigBean;
import org.jeecg.modules.airag.llm.config.KnowConfigBean;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.document.TikaDocumentParser;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.mapper.AiragKnowledgeMapper;
import org.jeecg.modules.airag.llm.mapper.AiragModelMapper;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
import static org.jeecg.modules.airag.llm.consts.LLMConsts.KNOWLEDGE_DOC_TYPE_FILE;
import static org.jeecg.modules.airag.llm.consts.LLMConsts.KNOWLEDGE_DOC_TYPE_WEB;

/**
 * Vector tool class
 *
 * @Author: chenrui
 * @Date: 2025/2/18 14:31
 */
@Slf4j
@Component
public class EmbeddingHandler implements IEmbeddingHandler {

    @Autowired
    EmbedStoreConfigBean embedStoreConfigBean;

    @Autowired
    private AiragModelMapper airagModelMapper;

    @Autowired
    @Lazy
    private IAiragKnowledgeService airagKnowledgeService;

    @Autowired
    private AiragKnowledgeMapper airagKnowledgeMapper;

    @Value(value = "${jeecg.path.upload:}")
    private String uploadpath;

    @Autowired
    KnowConfigBean knowConfigBean;

    /**
     * Default segment length
     */
    private static final int DEFAULT_SEGMENT_SIZE = 1000;

    /**
     * Default segment overlap length
     */
    private static final int DEFAULT_OVERLAP_SIZE = 50;

    /**
     * Vector storage metadata:knowledgeId
     */
    public static final String EMBED_STORE_METADATA_KNOWLEDGEID = "knowledgeId";

    /**
     * Vector storage metadata:docId
     */
    public static final String EMBED_STORE_METADATA_DOCID = "docId";

    /**
     * Vector storage metadata:docName
     */
    public static final String EMBED_STORE_METADATA_DOCNAME = "docName";

    /**
     * Vector storage cache
     */
    private static final ConcurrentHashMap<String, EmbeddingStore<TextSegment>> EMBED_STORE_CACHE = new ConcurrentHashMap<>();


    /**
     * Regular match: mdpicture
     * "!\\[(.*?)]\\((.*?)(\\s*=\\d+)?\\)"
     */
    private static final Pattern PATTERN_MD_IMAGE = Pattern.compile("!\\[(.*?)]\\((.*?)\\)");

    /**
     * Vectorization documentation
     *
     * @param knowId
     * @param doc
     * @return
     * @author chenrui
     * @date 2025/2/18 11:52
     */
    public Map<String, Object> embeddingDocument(String knowId, AiragKnowledgeDoc doc) {
        AiragKnowledge airagKnowledge = airagKnowledgeService.getById(knowId);
        AssertUtils.assertNotEmpty("Knowledge base does not exist", airagKnowledge);
        AssertUtils.assertNotEmpty("Please configure the vector model library for the knowledge base first", airagKnowledge.getEmbedId());
        AssertUtils.assertNotEmpty("Document cannot be empty", doc);
        // Read document
        String content = doc.getContent();
        // vectorize and store
        if (oConvertUtils.isEmpty(content)) {
            switch (doc.getType()) {
                case KNOWLEDGE_DOC_TYPE_FILE:
                    //parse file
                    if (knowConfigBean.isEnableMinerU()) {
                        parseFileByMinerU(doc);
                    }
                    content = parseFile(doc);
                    break;
                case KNOWLEDGE_DOC_TYPE_WEB:
                    // TODO author: chenrui for:Read website content date:2025/2/18
                    break;
            }
        }
        //update-begin---author:chenrui ---date:20250307  for：[QQYUN-11443]【AI】Should the title also be generated into the vector library?，Titles are generally meaningful------------
        if (oConvertUtils.isNotEmpty(doc.getTitle())) {
            content = doc.getTitle() + "\n\n" + content;
        }
        //update-end---author:chenrui ---date:20250307  for：[QQYUN-11443]【AI】Should the title also be generated into the vector library?，Titles are generally meaningful------------

        // vectorization date:2025/2/18
        AiragModel model = getEmbedModelData(airagKnowledge.getEmbedId());
        AiModelOptions modelOp = buildModelOptions(model);
        EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOp);
        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        // Delete old data
        embeddingStore.removeAll(metadataKey(EMBED_STORE_METADATA_DOCID).isEqualTo(doc.getId()));
        // segmenter
        DocumentSplitter splitter = DocumentSplitters.recursive(DEFAULT_SEGMENT_SIZE, DEFAULT_OVERLAP_SIZE);
        // segment and store
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        Metadata metadata = Metadata.metadata(EMBED_STORE_METADATA_DOCID, doc.getId())
                .put(EMBED_STORE_METADATA_KNOWLEDGEID, doc.getKnowledgeId())
                .put(EMBED_STORE_METADATA_DOCNAME, FilenameUtils.getName(doc.getTitle()));
        Document from = Document.from(content, metadata);
        ingestor.ingest(from);
        return metadata.toMap();
    }

    /**
     * vector query(Multiple knowledge bases)
     *
     * @param knowIds
     * @param queryText
     * @param topNumber
     * @param similarity
     * @return
     * @author chenrui
     * @date 2025/2/18 16:52
     */
    @Override
    public KnowledgeSearchResult embeddingSearch(List<String> knowIds, String queryText, Integer topNumber, Double similarity) {
        AssertUtils.assertNotEmpty("Please select a knowledge base", knowIds);
        AssertUtils.assertNotEmpty("Please fill in the query content", queryText);

        topNumber = oConvertUtils.getInteger(topNumber, 5);

        //hit document list
        List<Map<String, Object>> documents = new ArrayList<>(16);
        for (String knowId : knowIds) {
            List<Map<String, Object>> searchResp = searchEmbedding(knowId, queryText, topNumber, similarity);
            if (oConvertUtils.isObjectNotEmpty(searchResp)) {
                documents.addAll(searchResp);
            }
        }

        //Hit document content
        StringBuilder data = new StringBuilder();
        // rightdocumentsaccording toscoreSort in descending order and take the firsttopNumberindivual
        List<Map<String, Object>> sortedDocuments = documents.stream()
                .sorted(Comparator.comparingDouble((Map<String, Object> doc) -> (Double) doc.get("score")).reversed())
                .limit(topNumber)
                .peek(doc -> data.append(doc.get("content")).append("\n"))
                .collect(Collectors.toList());

        return new KnowledgeSearchResult(data.toString(), sortedDocuments);
    }

    /**
     * vector query
     *
     * @param knowId
     * @param queryText
     * @param topNumber
     * @param similarity
     * @return
     * @author chenrui
     * @date 2025/2/18 16:52
     */
    public List<Map<String, Object>> searchEmbedding(String knowId, String queryText, Integer topNumber, Double similarity) {
        AssertUtils.assertNotEmpty("Please select a knowledge base", knowId);
        AiragKnowledge knowledge = airagKnowledgeMapper.getByIdIgnoreTenant(knowId);
        AssertUtils.assertNotEmpty("Knowledge base does not exist", knowledge);
        AssertUtils.assertNotEmpty("Please fill in the query content", queryText);
        AiragModel model = getEmbedModelData(knowledge.getEmbedId());

        AiModelOptions modelOp = buildModelOptions(model);
        EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOp);
        Embedding queryEmbedding = embeddingModel.embed(queryText).content();

        topNumber = oConvertUtils.getInteger(topNumber, modelOp.getTopNumber());
        similarity = oConvertUtils.getDou(similarity, modelOp.getSimilarity());
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topNumber)
                .minScore(similarity)
                .filter(metadataKey(EMBED_STORE_METADATA_KNOWLEDGEID).isEqualTo(knowId))
                .build();

        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(embeddingSearchRequest).matches();
        List<Map<String, Object>> result = new ArrayList<>();
        if (oConvertUtils.isObjectNotEmpty(relevant)) {
            result = relevant.stream().map(matchRes -> {
                Map<String, Object> data = new HashMap<>();
                data.put("score", matchRes.score());
                data.put("content", matchRes.embedded().text());
                Metadata metadata = matchRes.embedded().metadata();
                data.put("chunk", metadata.getInteger("index"));
                data.put(EMBED_STORE_METADATA_DOCNAME, metadata.getString(EMBED_STORE_METADATA_DOCNAME));
                return data;
            }).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * 获取vector query路由
     *
     * @param knowIds
     * @param topNumber
     * @param similarity
     * @return
     * @author chenrui
     * @date 2025/2/20 21:03
     */
    @Override
    public QueryRouter getQueryRouter(List<String> knowIds, Integer topNumber, Double similarity) {
        AssertUtils.assertNotEmpty("Please select a knowledge base", knowIds);
        List<ContentRetriever> retrievers = Lists.newArrayList();
        for (String knowId : knowIds) {
            if (oConvertUtils.isEmpty(knowId)) {
                continue;
            }
            AiragKnowledge knowledge = airagKnowledgeMapper.getByIdIgnoreTenant(knowId);
            AssertUtils.assertNotEmpty("Knowledge base does not exist", knowledge);
            AiragModel model = getEmbedModelData(knowledge.getEmbedId());
            AiModelOptions modelOptions = buildModelOptions(model);
            EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOptions);

            EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
            topNumber = oConvertUtils.getInteger(topNumber, 5);
            similarity = oConvertUtils.getDou(similarity, 0.75);
            // 构建一indivual嵌入存储内容检索器，Used to retrieve content from embedded storage
            EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(topNumber)
                    .minScore(similarity)
                    .filter(metadataKey(EMBED_STORE_METADATA_KNOWLEDGEID).isEqualTo(knowId))
                    .build();
            retrievers.add(contentRetriever);
        }
        if (retrievers.isEmpty()) {
            return null;
        } else {
            return new DefaultQueryRouter(retrievers);
        }
    }

    /**
     * 删除Vectorization documentation
     *
     * @param knowId
     * @param modelId
     * @author chenrui
     * @date 2025/2/18 19:07
     */
    public void deleteEmbedDocsByKnowId(String knowId, String modelId) {
        AssertUtils.assertNotEmpty("Select knowledge base", knowId);
        AiragModel model = getEmbedModelData(modelId);

        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        // Delete data
        embeddingStore.removeAll(metadataKey(EMBED_STORE_METADATA_KNOWLEDGEID).isEqualTo(knowId));
    }

    /**
     * 删除Vectorization documentation
     *
     * @param docIds
     * @param modelId
     * @author chenrui
     * @date 2025/2/18 19:07
     */
    public void deleteEmbedDocsByDocIds(List<String> docIds, String modelId) {
        AssertUtils.assertNotEmpty("Select document", docIds);
        AiragModel model = getEmbedModelData(modelId);

        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        // Delete data
        embeddingStore.removeAll(metadataKey(EMBED_STORE_METADATA_DOCID).isIn(docIds));
    }

    /**
     * Query vector model data
     *
     * @param modelId
     * @return
     * @author chenrui
     * @date 2025/2/20 20:08
     */
    private AiragModel getEmbedModelData(String modelId) {
        AssertUtils.assertNotEmpty("Vector model cannot be empty", modelId);
        AiragModel model = airagModelMapper.getByIdIgnoreTenant(modelId);
        AssertUtils.assertNotEmpty("Vector model does not exist", model);
        AssertUtils.assertEquals("Only vector models are supported", LLMConsts.MODEL_TYPE_EMBED, model.getModelType());
        return model;
    }

    /**
     * Get vector storage
     *
     * @param model
     * @return
     * @author chenrui
     * @date 2025/2/18 14:56
     */
    private EmbeddingStore<TextSegment> getEmbedStore(AiragModel model) {
        AssertUtils.assertNotEmpty("Model not configured", model);
        String modelId = model.getId();
        String connectionInfo = embedStoreConfigBean.getHost() + embedStoreConfigBean.getPort() + embedStoreConfigBean.getDatabase();
        String key = modelId + connectionInfo;
        if (EMBED_STORE_CACHE.containsKey(key)) {
            return EMBED_STORE_CACHE.get(key);
        }


        AiModelOptions modelOp = buildModelOptions(model);
        EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOp);

        String tableName = embedStoreConfigBean.getTable();

        // update-begin---author:sunjianlei ---date:20250509  for：【QQYUN-12345】Vector model dimension inconsistency problem
        // If the model is not the default vector dimension
        int dimension = embeddingModel.dimension();
        if (!LLMConsts.EMBED_MODEL_DEFAULT_DIMENSION.equals(dimension)) {
            // Just add the dimension suffix，Prevent saving failure due to inconsistent dimensions
            tableName += ("_" + dimension);
        }
        // update-end-----author:sunjianlei ---date:20250509  for：【QQYUN-12345】Vector model dimension inconsistency problem

        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                // Connection and table parameters
                .host(embedStoreConfigBean.getHost())
                .port(embedStoreConfigBean.getPort())
                .database(embedStoreConfigBean.getDatabase())
                .user(embedStoreConfigBean.getUser())
                .password(embedStoreConfigBean.getPassword())
                .table(tableName)
                // Embedding dimension
                // Required: Must match the embedding model’s output dimension
                .dimension(embeddingModel.dimension())
                // Indexing and performance options
                // Enable IVFFlat index
                .useIndex(true)
                // Number of lists
                // for IVFFlat index
                .indexListSize(100)
                // Table creation options
                // Automatically create the table if it doesn’t exist
                .createTable(true)
                //Don’t drop the table first (set to true if you want a fresh start)
                .dropTableFirst(false)
                .build();
        EMBED_STORE_CACHE.put(key, embeddingStore);
        return embeddingStore;
    }

    /**
     * structureModelOptions
     *
     * @param model
     * @return
     * @author chenrui
     * @date 2025/3/11 17:45
     */
    public static AiModelOptions buildModelOptions(AiragModel model) {
        AiModelOptions.AiModelOptionsBuilder modelOpBuilder = AiModelOptions.builder()
                .provider(model.getProvider())
                .modelName(model.getModelName())
                .baseUrl(model.getBaseUrl());
        if (oConvertUtils.isObjectNotEmpty(model.getCredential())) {
            JSONObject modelCredential = JSONObject.parseObject(model.getCredential());
            modelOpBuilder.apiKey(oConvertUtils.getString(modelCredential.getString("apiKey"), null));
            modelOpBuilder.secretKey(oConvertUtils.getString(modelCredential.getString("secretKey"), null));
        }
        modelOpBuilder.topNumber(5);
        modelOpBuilder.similarity(0.75);
        return modelOpBuilder.build();
    }

    /**
     * parse file
     *
     * @param doc
     * @author chenrui
     * @date 2025/3/5 11:31
     */
    private String parseFile(AiragKnowledgeDoc doc) {
        String metadata = doc.getMetadata();
        AssertUtils.assertNotEmpty("Please upload the file first", metadata);
        JSONObject metadataJson = JSONObject.parseObject(metadata);
        if (!metadataJson.containsKey(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH)) {
            throw new JeecgBootException("Please upload the file first");
        }
        String filePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH);
        AssertUtils.assertNotEmpty("Please upload the file first", filePath);
        // Internet resources,Download to temporary directory first
        filePath = ensureFile(filePath);
        // Extract document content
        File docFile = new File(filePath);
        if (docFile.exists()) {
            Document document = new TikaDocumentParser(AutoDetectParser::new, null, null, null).parse(docFile);
            if (null != document) {
                String content = document.text();
                // Determine whethermddocument
                String fileType = FilenameUtils.getExtension(docFile.getName());
                if ("md".contains(fileType)) {
                    // in the case ofmddocument，查找所有picture语法，in the case oflocalpicture，替换成网络picture
                    String baseUrl = "#{domainURL}/sys/common/static/";
                    String sourcePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_SOURCES_PATH);
                    if(oConvertUtils.isNotEmpty(sourcePath)) {
                        String escapedPath = uploadpath;
                        //update-begin---author:wangshuai---date:2025-06-03---for:【QQYUN-12636】【AIknowledge base】document库上传 locallocal document中的picture不展示---
                        /*if (File.separator.equals("\\")){
                            escapedPath = uploadpath.replace("//", "\\\\");
                        }*/
                        //update-end---author:wangshuai---date:2025-06-03---for:【QQYUN-12636】【AIknowledge base】document库上传 locallocal document中的picture不展示---
                        sourcePath = sourcePath.replaceFirst("^" + escapedPath, "").replace("\\", "/");
                        String docFilePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH);
                        docFilePath = FilenameUtils.getPath(docFilePath);
                        docFilePath = docFilePath.replace("\\", "/");
                        StringBuffer sb = replaceImageUrl(content, baseUrl + sourcePath + "/", baseUrl + docFilePath);
                        content = sb.toString();
                    }
                }
                return content;
            }
        }
        return null;
    }

    @NotNull
    private static StringBuffer replaceImageUrl(String content, String abstractBaseUrl, String relativeBaseUrl) {
        // Regular expression matchingmddocument中的picture语法 ![alt text](image url)
        Matcher matcher = PATTERN_MD_IMAGE.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String imageUrl = matcher.group(2);
            // 检查是否是localpicture路径
            if (!imageUrl.startsWith("http")) {
                // 替换成网络picture路径
                String networkImageUrl = abstractBaseUrl + imageUrl;
                if(imageUrl.startsWith("/")) {
                    // 绝right路径
                    networkImageUrl = abstractBaseUrl + imageUrl;
                }else{
                    // 相right路径
                    networkImageUrl = relativeBaseUrl + imageUrl;
                }
                // 修改picture路径中//->/，but keephttp://andhttps://
                networkImageUrl = networkImageUrl.replaceAll("(?<!http:)(?<!https:)//", "/");
                matcher.appendReplacement(sb, "![" + matcher.group(1) + "](" + networkImageUrl + ")");
            } else {
                matcher.appendReplacement(sb, "![" + matcher.group(1) + "](" + imageUrl + ")");
            }
        }
        matcher.appendTail(sb);
        return sb;
    }

    /**
     * passMinerUparse file
     *
     * @param doc
     * @author chenrui
     * @date 2025/4/1 17:37
     */
    private void parseFileByMinerU(AiragKnowledgeDoc doc) {
        String metadata = doc.getMetadata();
        AssertUtils.assertNotEmpty("Please upload the file first", metadata);
        JSONObject metadataJson = JSONObject.parseObject(metadata);
        if (!metadataJson.containsKey(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH)) {
            throw new JeecgBootException("Please upload the file first");
        }
        String filePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH);
        AssertUtils.assertNotEmpty("Please upload the file first", filePath);
        filePath = ensureFile(filePath);

        File docFile = new File(filePath);
        String fileType = FilenameUtils.getExtension(filePath);
        if (!docFile.exists()
                || "txt".equalsIgnoreCase(fileType)
                || "md".equalsIgnoreCase(fileType)) {
            return ;
        }

        String command = "magic-pdf";
        if (oConvertUtils.isNotEmpty(knowConfigBean.getCondaEnv())) {
            command = "conda run -n " + knowConfigBean.getCondaEnv() + " " + command;
        }

        String outputPath = docFile.getParentFile().getAbsolutePath();
        String[] args = {
                "-p", docFile.getAbsolutePath(),
                "-o", outputPath,
        };

        try {
            String execLog = CommandExecUtil.execCommand(command, args);
            log.info("Execute command line:" + command + " args:" + Arrays.toString(args) + "\n log::" + execLog);
            // if successful,替换document路径and静态资源路径
            String fileBaseName = FilenameUtils.getBaseName(docFile.getName());
            String newFileDir = outputPath + File.separator + fileBaseName + File.separator + "auto" + File.separator ;
            // 先检查document是否存在,Replace only if it exists
            File convertedFile = new File(newFileDir + fileBaseName + ".md");
            if (convertedFile.exists()) {
                log.info("document转换成mdsuccess,替换document路径and静态资源路径");
                newFileDir = newFileDir.replaceFirst("^" + uploadpath, "");
                metadataJson.put(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH, newFileDir + fileBaseName + ".md");
                metadataJson.put(LLMConsts.KNOWLEDGE_DOC_METADATA_SOURCES_PATH, newFileDir);
                doc.setMetadata(metadataJson.toJSONString());
            }
        } catch (IOException e) {
            log.error("document转换mdfail,Use traditional extraction solutions{}", e.getMessage(), e);
        }
    }

    /**
     * 确保document存在
     * @param filePath
     * @return
     * @author chenrui
     * @date 2025/4/1 17:36
     */
    @NotNull
    private String ensureFile(String filePath) {
        // Internet resources,Download to temporary directory first
        Matcher matcher = LLMConsts.WEB_PATTERN.matcher(filePath);
        if (matcher.matches()) {
            log.info("Internet resources,Download to temporary directory:" + filePath);
            // 准备document
            String tempFilePath = uploadpath + File.separator + "tmp" + File.separator + UUIDGenerator.generate() + File.separator;
            String fileName = filePath;
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            fileName = FilenameUtils.getName(fileName);
            tempFilePath = tempFilePath + fileName;
            FileDownloadUtils.download2DiskFromNet(filePath, tempFilePath);
            filePath = tempFilePath;
        } else {
            //localdocument
            filePath = uploadpath + File.separator + filePath;
        }
        return filePath;
    }


}
