package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.util.internal.StringUtil;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author scott
 * @Date 2019/12/9 11:33
 * @Description: Organization code generation rules
 */
public class OrgCodeRule implements IFillRuleHandler {

    @Override
    public Object execute(JSONObject params, JSONObject formData) {
        ISysDepartService sysDepartService = (ISysDepartService) SpringContextUtils.getBean("sysDepartServiceImpl");

        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        LambdaQueryWrapper<SysDepart> query1 = new LambdaQueryWrapper<SysDepart>();
        // Create aListgather,Stores all returned by the querySysDepartobject
        List<SysDepart> departList = new ArrayList<>();
        String[] strArray = new String[2];
        //Define department type
        String orgType = "";
        // Define new encoding string
        String newOrgCode = "";
        // Define old encoding string
        String oldOrgCode = "";

        String parentId = null;
        if (formData != null && formData.size() > 0) {
            Object obj = formData.get("parentId");
            if (obj != null) {
                parentId = obj.toString();
            }
        } else {
            if (params != null) {
                Object obj = params.get("parentId");
                if (obj != null) {
                    parentId = obj.toString();
                }
            }
        }

        //If it is the highest level,Then query the same levelorg_code, Call the tool class to generate the code and return
        if (StringUtil.isNullOrEmpty(parentId)) {
            // Line to determine whether the table in the database is empty,If empty, the initial code will be returned directly.
            //Get the maximum valuecodedepartment information
            //update-begin---author:wangshuai ---date:20230211  for：[QQYUN-4209]New departments cannot be built under tenant isolation------------
            Page<SysDepart> page = new Page<>(1,1);
            IPage<SysDepart> pageList = sysDepartService.getMaxCodeDepart(page,"");
            List<SysDepart> records = pageList.getRecords();
            if (null==records || records.size()==0) {
            //update-end---author:wangshuai ---date:20230211  for：[QQYUN-4209]New departments cannot be built under tenant isolation------------
                strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
                strArray[1] = "1";
                return strArray;
            } else {
                SysDepart depart = records.get(0);
                oldOrgCode = depart.getOrgCode();
                orgType = depart.getOrgType();
                newOrgCode = YouBianCodeUtil.getNextYouBianCode(oldOrgCode);
            }
        } else {//Otherwise, query all departments at the same level.,There are two situations after getting the results,With or without siblings
            //Get the maximum value of your departmentorgCodeDepartment information
            //update-begin---author:wangshuai ---date:20230211  for：[QQYUN-4209]New departments cannot be built under tenant isolation------------
            Page<SysDepart> page = new Page<>(1,1);
            IPage<SysDepart> pageList = sysDepartService.getMaxCodeDepart(page,parentId);
            List<SysDepart> records = pageList.getRecords();
            // Query the parent department
            SysDepart depart = sysDepartService.getDepartById(parentId);
            //update-end---author:wangshuai ---date:20230211  for：[QQYUN-4209]New departments cannot be built under tenant isolation------------
            // Get the parent departmentCode
            String parentCode = depart.getOrgCode();
            // Calculate the type of the current department based on the parent department type
            orgType = String.valueOf(Integer.valueOf(depart.getOrgType()) + 1);
            // The department handling the same level isnullsituation
            if (null == records || records.size()==0) {
                // Directly generate the current department code and return
                newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, null);
            } else { //处理有同级部门situation
                // Get the code of the same level department,Utilize tools
                String subCode = records.get(0).getOrgCode();
                // Returns the generated current department code
                newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, subCode);
            }
        }
        // Returns an array that finally encapsulates the department code and department type
        strArray[0] = newOrgCode;
        strArray[1] = orgType;
        return strArray;
    }
}
