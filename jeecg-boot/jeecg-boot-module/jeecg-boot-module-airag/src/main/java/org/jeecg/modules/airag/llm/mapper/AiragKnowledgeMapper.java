package org.jeecg.modules.airag.llm.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;

/**
 * @Description: AIRagknowledge base
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
public interface AiragKnowledgeMapper extends BaseMapper<AiragKnowledge> {

    /**
     * according toID查询knowledge base信息(Ignore tenant)
     * for [QQYUN-12113]Chat after sharing，application、Model、knowledge base不according to租户查询
     * @param id
     * @return
     * @author chenrui
     * @date 2025/4/21 15:24
     */
    @InterceptorIgnore(tenantLine = "true")
    AiragKnowledge getByIdIgnoreTenant(String id);
}
