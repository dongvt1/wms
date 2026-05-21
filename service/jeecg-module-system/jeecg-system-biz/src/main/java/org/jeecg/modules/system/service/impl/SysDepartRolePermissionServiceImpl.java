package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepartRolePermission;
import org.jeecg.modules.system.mapper.SysDepartRolePermissionMapper;
import org.jeecg.modules.system.service.ISysDepartRolePermissionService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: Department role permissions
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Service
public class SysDepartRolePermissionServiceImpl extends ServiceImpl<SysDepartRolePermissionMapper, SysDepartRolePermission> implements ISysDepartRolePermissionService {

    @Override
    public void saveDeptRolePermission(String roleId, String permissionIds, String lastPermissionIds) {
        String ip = "";
        try {
            //Getrequest
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            //GetIPaddress
            ip = IpUtils.getIpAddr(request);
        } catch (Exception e) {
            ip = "127.0.0.1";
        }
        List<String> add = getDiff(lastPermissionIds,permissionIds);
        if(add!=null && add.size()>0) {
            List<SysDepartRolePermission> list = new ArrayList<SysDepartRolePermission>();
            for (String p : add) {
                if(oConvertUtils.isNotEmpty(p)) {
                    SysDepartRolePermission rolepms = new SysDepartRolePermission(roleId, p);
                    rolepms.setOperateDate(new Date());
                    rolepms.setOperateIp(ip);
                    list.add(rolepms);
                }
            }
            this.saveBatch(list);
        }

        List<String> delete = getDiff(permissionIds,lastPermissionIds);
        if(delete!=null && delete.size()>0) {
            for (String permissionId : delete) {
                this.remove(new QueryWrapper<SysDepartRolePermission>().lambda().eq(SysDepartRolePermission::getRoleId, roleId).eq(SysDepartRolePermission::getPermissionId, permissionId));
            }
        }
    }

    /**
     * fromdiffFind out inmainElements not found in
     * @param main
     * @param diff
     * @return
     */
    private List<String> getDiff(String main, String diff){
        if(oConvertUtils.isEmpty(diff)) {
            return null;
        }
        if(oConvertUtils.isEmpty(main)) {
            return Arrays.asList(diff.split(","));
        }

        String[] mainArr = main.split(",");
        String[] diffArr = diff.split(",");
        Map<String, Integer> map = new HashMap(5);
        for (String string : mainArr) {
            map.put(string, 1);
        }
        List<String> res = new ArrayList<String>();
        for (String key : diffArr) {
            if(oConvertUtils.isNotEmpty(key) && !map.containsKey(key)) {
                res.add(key);
            }
        }
        return res;
    }
}
