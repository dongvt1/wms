package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.ThirdLoginModel;

import java.util.List;

/**
 * @Description: Third-party login account table
 * @Author: jeecg-boot
 * @Date:   2020-11-17
 * @Version: V1.0
 */
public interface ISysThirdAccountService extends IService<SysThirdAccount> {
    /**
     * Update third-party account information
     * @param sysUser SysUserobject
     * @param thirdUserUuid third partyid
     */
    void updateThirdUserId(SysUser sysUser,String thirdUserUuid);

    /**
     * 创建third partyuser
     * @param phone Phone number
     * @param thirdUserUuid third partyid
     * @return SysUser
     */
    SysUser createUser(String phone, String thirdUserUuid, Integer tenantId);

    /**
     * According to localuserIdQuery data
     * @param sysUserId userid
     * @param thirdType third party登录类型
     * @return SysThirdAccount
     */
    SysThirdAccount getOneBySysUserId(String sysUserId, String thirdType);

    /**
     * 根据third partyuserIdQuery data
     * @param thirdUserId third partyid
     * @param thirdType third party登录类型
     * @return SysThirdAccount
     */
    SysThirdAccount getOneByThirdUserId(String thirdUserId, String thirdType);

    /**
     * pass sysUsername Collection batch query
     *
     * @param sysUsernameArr usernamegather
     * @param thirdType      third party类型
     * @return
     */
    List<SysThirdAccount> listThirdUserIdByUsername(String[] sysUsernameArr, String thirdType, Integer tenantId);

    /**
     * 创建新user
     *
     * @param tlm third party登录信息
     * @return SysThirdAccount
     * @return tenantId tenantid
     */
    SysThirdAccount saveThirdUser(ThirdLoginModel tlm, Integer tenantId);

    /**
     * 绑定third party账号(登录后根据userid绑定third party账号)
     * @param sysThirdAccount
     */
    SysThirdAccount bindThirdAppAccountByUserId(SysThirdAccount sysThirdAccount);


    /**
     * 根据third party UUID和third party类别获取third partyuser数据
     * @param unionid
     * @param thirdType
     * @param tenantId
     * @param thirdUserId
     * @return
     */
    SysThirdAccount getOneByUuidAndThirdType(String unionid, String thirdType,Integer tenantId,String thirdUserId);
}
