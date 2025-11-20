package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;

import java.util.List;

/**
 * @Description: Third-party login account table
 * @Author: jeecg-boot
 * @Date: 2020-11-17
 * @Version: V1.0
 */
public interface SysThirdAccountMapper extends BaseMapper<SysThirdAccount> {

    /**
     * pass sysUsername Collection batch query
     *
     * @param sysUsernameArr usernamegather
     * @param thirdType       Third party type
     * @return
     */
    List<SysThirdAccount> selectThirdIdsByUsername(@Param("sysUsernameArr") String[] sysUsernameArr, @Param("thirdType") String thirdType, @Param("tenantId") Integer tenantId);
    
    /**
     * Query bound users
     * @param tenantId
     * @param thirdType
     * @return
     */
    List<JwUserDepartVo> getThirdUserBindByWechat(@Param("tenantId") int tenantId, @Param("thirdType") String thirdType);
}
