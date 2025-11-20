package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserPosition;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserPositionMapper;
import org.jeecg.modules.system.service.ISysUserPositionService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: User position relationship table
 * @Author: jeecg-boot
 * @Date: 2023-02-14
 * @Version: V1.0
 */
@Service
public class SysUserPositionServiceImpl extends ServiceImpl<SysUserPositionMapper, SysUserPosition> implements ISysUserPositionService {

    @Autowired
    private SysUserPositionMapper sysUserPositionMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public IPage<SysUser> getPositionUserList(Page<SysUser> page, String positionId) {
        return page.setRecords(sysUserPositionMapper.getPositionUserList(page, positionId));
    }

    @Override
    public void saveUserPosition(String userIds, String positionId) {
        String[] userIdArray = userIds.split(SymbolConstant.COMMA);
        //Existing users
        StringBuilder userBuilder = new StringBuilder();
        for (String userId : userIdArray) {
            //Get whether the member exists in the position
            Long count = sysUserPositionMapper.getUserPositionCount(userId, positionId);
            if (count == 0) {
                //插入到User position relationship table里面
                SysUserPosition userPosition = new SysUserPosition();
                userPosition.setPositionId(positionId);
                userPosition.setUserId(userId);
                sysUserPositionMapper.insert(userPosition);
            } else {
                userBuilder.append(userId).append(SymbolConstant.COMMA);
            }
        }
        //If the useridexist，说明已existUser position relationship table中,提示用户已exist
        String uIds = userBuilder.toString();
        if (oConvertUtils.isNotEmpty(uIds)) {
            //Query user list
            List<SysUser> sysUsers = userMapper.selectBatchIds(Arrays.asList(uIds.split(SymbolConstant.COMMA)));
            String realnames = sysUsers.stream().map(SysUser::getRealname).collect(Collectors.joining(SymbolConstant.COMMA));
            throw new JeecgBootException(realnames + "已exist该职位中");
        }
    }

    @Override
    public void removeByPositionId(String positionId) {
        sysUserPositionMapper.removeByPositionId(positionId);
    }

    @Override
    public void removePositionUser(String userIds, String positionId) {
        String[] userIdArray = userIds.split(SymbolConstant.COMMA);
		sysUserPositionMapper.removePositionUser(Arrays.asList(userIdArray),positionId);
    }

}
