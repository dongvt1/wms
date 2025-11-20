package org.jeecg.modules.system.service;

import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;

import java.util.List;

/**
 * third partyAppdocking
 * @author: jeecg-boot
 */
public interface IThirdAppService {

    /**
     * GetAccessToken
     * @return String
     */
    String getAccessToken();

    /**
     * 将local部门同步到third partyApp<br>
     * Sync direction：local --> third partyAPP
     * Synchronization logic：<br>
     * 1. First check whether it has been synchronized，Modify if any，Create if none；<br>
     * 2. local没有但third partyApp里有则删除third partyAppinside。
     * @param ids
     * @return Return successfullytrue
     */
    SyncInfoVo syncLocalDepartmentToThirdApp(String ids);

//    /**
//     * 将third partyApp部门同步到local<br>
//     * Sync direction：third partyAPP --> local
//     * Synchronization logic：<br>
//     * 1. First check whether it has been synchronized，Modify if any，Create if none；<br>
//     * 2. local没有但third partyApp里有则删除third partyAppinside。
//     * @param ids
//     * @return Return successfullytrue
//     */
//    SyncInfoVo syncThirdAppDepartmentToLocal(String ids);

    /**
     * 将local用户同步到third partyApp<br>
     * Sync direction：local --> third partyAPP <br>
     * Synchronization logic：First check whether it has been synchronized，Modify if any、Create if none<br>
     * Notice：Sync people's status，Such as resignation、Disable、Logical deletion etc.。
     * (Special point：1、The current logic is specifically designed not to delete users.，Prevent corporate WeChat from going online in advance，User already exists，But there is no such user on the platform。
     *  企业微信支持Disable账号；DingTalk does not support
     *  2、In corporate WeChat, mobile phone number is activated，Can only be changed by the user himself，Not allowed to change through the interface)
     * @param ids
     * @return Return successfully空数组，Failure returns error message
     */
    SyncInfoVo syncLocalUserToThirdApp(String ids);

//    /**
//     * 将third partyApp用户同步到local<br>
//     * Sync direction：third partyAPP --> local <br>
//     * Synchronization logic：First check whether it has been synchronized，Modify if any、Create if none<br>
//     * Notice：Sync people's status，Such as resignation、Disable、Logical deletion etc.。
//     *
//     * @return Return successfully空数组，Failure returns error message
//     */
//    SyncInfoVo syncThirdAppUserToLocal();

    /**
     * 根据local用户ID，删除third partyAPPof users
     *
     * @param userIdList local用户IDlist
     * @return 0indicates success，Other values ​​indicate failure
     */
    int removeThirdAppUser(List<String> userIdList);

    /**
     * Send message
     *
     * @param message
     * @param verifyConfig Whether to verify the configuration（Not enabledAPPWill refuse to send）
     * @return
     */
    boolean sendMessage(MessageDTO message, boolean verifyConfig);

    /**
     * Send message
     * @param message
     * @return boolean
     */
    boolean sendMessage(MessageDTO message);

}
