package org.jeecg.modules.airag.llm.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.airag.llm.entity.AiragModel;

/**
 * @Description: AiRagModel configuration
 * @Author: jeecg-boot
 * @Date:   2025-02-14
 * @Version: V1.0
 */
public interface AiragModelMapper extends BaseMapper<AiragModel> {

    /**
     * according toIDQuery model information(Ignore tenant)
     * for [QQYUN-12113]Chat after sharing，application、Model、知识库不according to租户查询
     * @param id
     * @return
     * @author chenrui
     * @date 2025/4/21 15:24
     */
    @InterceptorIgnore(tenantLine = "true")
    AiragModel getByIdIgnoreTenant(String id);
}
