package org.jeecg.modules.demo.test.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Order air ticket
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
public interface JeecgOrderTicketMapper extends BaseMapper<JeecgOrderTicket> {

	/**
	 *  Delete customers in batches through foreign keys in the main table
	 * @param mainId
	 * @return
	 */
    @Delete("DELETE FROM JEECG_ORDER_TICKET WHERE ORDER_ID = #{mainId}")
	public boolean deleteTicketsByMainId(String mainId);

    /**
     * 通过主表Order外键查询Order air ticket
     * @param mainId Orderid
     * @return 返回Order air ticket集合
     */
    @Select("SELECT * FROM JEECG_ORDER_TICKET WHERE ORDER_ID = #{mainId}")
	public List<JeecgOrderTicket> selectTicketsByMainId(String mainId);
}
