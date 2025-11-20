package org.jeecg.modules.airag.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;

/**
 * @Description: airagKnowledge base documentation
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
public interface AiragKnowledgeDocMapper extends BaseMapper<AiragKnowledgeDoc> {

	/**
	 * through main tableidDelete subtable data
	 *
	 * @param mainId main tableid
	 * @return boolean
	 */
	public boolean deleteByMainId(@Param("mainId") String mainId);

}
