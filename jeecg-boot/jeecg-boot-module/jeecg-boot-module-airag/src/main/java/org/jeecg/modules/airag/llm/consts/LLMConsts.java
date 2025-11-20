package org.jeecg.modules.airag.llm.consts;

import java.util.regex.Pattern;

/**
 * @Description: airagModel constant class
 * @Author: chenrui
 * @Date: 2025/2/12 17:35
 */
public class LLMConsts {


    /**
     * regular expression:Is it a web page?
     */
    public static final Pattern WEB_PATTERN = Pattern.compile("^(http|https)://.*");

    /**
     * state:enable
     */
    public static final String STATUS_ENABLE = "enable";
    /**
     * state:Disable
     */
    public static final String STATUS_DISABLE = "disable";


    /**
     * Model type:vector
     */
    public static final String MODEL_TYPE_EMBED = "EMBED";

    /**
     * Model type:chat
     */
    public static final String MODEL_TYPE_LLM = "LLM";

    /**
     * vector模型：Default dimensions
     */
    public static final Integer EMBED_MODEL_DEFAULT_DIMENSION = 1536;

    /**
     * knowledge base:文档state:draft
     */
    public static final String KNOWLEDGE_DOC_STATUS_DRAFT = "draft";
    /**
     * knowledge base:文档state:Under construction
     */
    public static final String KNOWLEDGE_DOC_STATUS_BUILDING = "building";
    /**
     * knowledge base:文档state:Build completed
     */
    public static final String KNOWLEDGE_DOC_STATUS_COMPLETE = "complete";
    /**
     * knowledge base:文档state:Build failed
     */
    public static final String KNOWLEDGE_DOC_STATUS_FAILED = "failed";

    /**
     * knowledge base:Document type:text
     */
    public static final String KNOWLEDGE_DOC_TYPE_TEXT = "text";
    /**
     * knowledge base:Document type:document
     */
    public static final String KNOWLEDGE_DOC_TYPE_FILE = "file";
    /**
     * knowledge base:Document type:Web page
     */
    public static final String KNOWLEDGE_DOC_TYPE_WEB = "web";

    /**
     * knowledge base:Document metadata:document路径
     */
    public static final String KNOWLEDGE_DOC_METADATA_FILEPATH = "filePath";

    /**
     * knowledge base:Document metadata:Resource path
     */
    public static final String KNOWLEDGE_DOC_METADATA_SOURCES_PATH = "sourcesPath";

}
