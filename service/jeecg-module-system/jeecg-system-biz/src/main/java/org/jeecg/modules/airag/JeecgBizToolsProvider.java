package org.jeecg.modules.airag;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.llm.handler.JeecgToolsProvider;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.controller.SysUserController;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * for [QQYUN-13565]【AIassistant】Added tool extensions for creating users and querying users
 * @Description: jeecg llmtool provider
 * @Author: chenrui
 * @Date: 2025/8/26 18:06
 */
@Component
public class JeecgBizToolsProvider implements JeecgToolsProvider {

    @Autowired
    SysUserController sysUserController;

    @Autowired
    SysUserMapper userMapper;

    @Autowired
    private BaseCommonService baseCommonService;

    @Autowired
    private org.jeecg.modules.system.service.ISysRoleService sysRoleService;

    @Autowired
    private org.jeecg.modules.system.service.ISysUserRoleService sysUserRoleService;

    @Autowired
    private org.jeecg.modules.system.service.ISysUserService sysUserService;

    public Map<ToolSpecification, ToolExecutor> getDefaultTools(){
        Map<ToolSpecification, ToolExecutor> tools = new HashMap<>();
        JeecgLlmTools userTool = queryUserTool();
        tools.put(userTool.getToolSpecification(), userTool.getToolExecutor());
        JeecgLlmTools addUser = addUserTool();
        tools.put(addUser.getToolSpecification(), addUser.getToolExecutor());
        // New：Query all roles
        JeecgLlmTools queryRoles = queryAllRolesTool();
        tools.put(queryRoles.getToolSpecification(), queryRoles.getToolExecutor());
        // New：Grant roles to users
        JeecgLlmTools grantRoles = grantUserRolesTool();
        tools.put(grantRoles.getToolSpecification(), grantRoles.getToolExecutor());
        return tools;
    }

    /**
     * Add user
     * @return
     * @author chenrui
     * @date 2025/8/27 09:51
     */
    private JeecgLlmTools addUserTool(){
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("add_user")
                .description("Add user,Return to add results;" +
                        "\n\n - When required fields are missing,Please ask the user." +
                        "\n\n - You should determine in advance whether the user’s input is legal,For example, does the username comply with the specifications?,Are the mobile phone number and email address correct?." +
                        "\n\n - Use the username to check whether the user exists in advance,Cannot be added if it exists." +
                        "\n\n - Return success message after successful addition,If it fails, return the reason for failure." +
                        "\n\n - username,Job number,Mail,Mobile phone numbers are required to be unique,Confirm uniqueness in advance through query user tool." )
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("username", "username,Required,Only letters allowed、number、Underline，and must start with a letter,only")
                                .addStringProperty("password", "User password,Required")
                                .addStringProperty("realname", "real name,Required")
                                .addStringProperty("workNo", "Job number,Required,only")
                                .addStringProperty("email", "Mail,Required,only")
                                .addStringProperty("phone", "Phone number,Required,only")
                                .required("username","password","realname","workNo","email","phone")
                                .build()
                )
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            JSONObject arguments = JSONObject.parseObject(toolExecutionRequest.arguments());
            arguments.put("confirmPassword",arguments.get("password"));
            arguments.put("userIdentity",1);
            arguments.put("activitiSync",1);
            arguments.put("departIds","");
            String selectedRoles = arguments.getString("selectedroles");
            String selectedDeparts = arguments.getString("selecteddeparts");
            String msg = "Add userfail";
            try {
                SysUser user = JSON.parseObject(arguments.toJSONString(), SysUser.class);
                user.setCreateTime(new Date());//Set creation time
                String salt = oConvertUtils.randomGen(8);
                user.setSalt(salt);
                String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
                user.setPassword(passwordEncode);
                user.setStatus(1);
                user.setDelFlag(CommonConstant.DEL_FLAG_0);
                //User table fieldsorg_codeHis value cannot be set here
                user.setOrgCode(null);
                // Save user go oneservice Guaranteed transaction
                //Get tenantsids
                String relTenantIds = arguments.getString("relTenantIds");
                sysUserService.saveUser(user, selectedRoles, selectedDeparts, relTenantIds, false);
                baseCommonService.addLog("Add user，username： " +user.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
                msg = "Add user成功";
                // User change，Trigger sync workflow
            } catch (Exception e) {
                msg = "Add userfail";
            }
            return msg;
        };
        return new JeecgLlmTools(toolSpecification,toolExecutor);
    }

    /**
     * Query user information
     *
     * @return User listJSONstring
     * @author chenrui
     * @date 2025/8/26 18:52
     */
    private JeecgLlmTools queryUserTool() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("query_user_by_name")
                .description("Query user details，returnjsonarray。支持username、real name、Mail、Phone number、Job number多字段组合查询，username、real name、Mail、Phone number均为模糊查询，Job number为精确查询。无条件则return全部user。")
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("username", "username")
                                .addStringProperty("realname", "real name")
                                .addStringProperty("email", "e-mail")
                                .addStringProperty("phone", "Phone number")
                                .addStringProperty("workNo", "Job number")
                                .build()
                )
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            SysUser args = JSONObject.parseObject(toolExecutionRequest.arguments(), SysUser.class);
            QueryWrapper<SysUser> qw = new QueryWrapper<>();
            if (StringUtils.isNotBlank(args.getUsername())) {
                qw.like("username", args.getUsername());
            }
            if (StringUtils.isNotBlank(args.getRealname())) {
                qw.like("realname", args.getRealname());
            }
            if (StringUtils.isNotBlank(args.getEmail())) {
                qw.like("email", args.getEmail());
            }
            if (StringUtils.isNotBlank(args.getPhone())) {
                qw.like("phone", args.getPhone());
            }
            if (StringUtils.isNotBlank(args.getWorkNo())) {
                qw.eq("work_no", args.getWorkNo());
            }
            qw.eq("del_flag", 0);
            List<SysUser> users = userMapper.selectList(qw);
            users.forEach(u -> { u.setPassword(null); u.setSalt(null); });
            return JSONObject.toJSONString(users);
        };
        return new JeecgLlmTools(toolSpecification, toolExecutor);
    }

    /**
     * Query all roles
     * @return
     * @author chenrui
     * @date 2025/8/27 09:52
     */
    private JeecgLlmTools queryAllRolesTool() {
        ToolSpecification spec = ToolSpecification.builder()
                .name("query_all_roles")
                .description("Query all roles，returnjsonarray。Contains fields：id、roleName、roleCode；Default by creation time/Sorting number rules are determined by the backend。")
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("roleName", "Character name")
                                .addStringProperty("roleCode", "role coding")
                                .build()
                )
                .build();
        ToolExecutor exec = (toolExecutionRequest, memoryId) -> {
            // Do tenant isolation queries（If turned on）
            SysRole sysRole = JSONObject.parseObject(toolExecutionRequest.arguments(), SysRole.class);
            QueryWrapper<SysRole> qw = Wrappers.query();
            if (StringUtils.isNotBlank(sysRole.getRoleName())) {
                qw.like("role_name", sysRole.getRoleName());
            }
            if (StringUtils.isNotBlank(sysRole.getRoleCode())) {
                qw.like("role_code", sysRole.getRoleCode());
            }
            // not deleted
            List<SysRole> roles = sysRoleService.list(qw);
            // 仅return核心字段
            JSONArray arr = new JSONArray();
            for (SysRole r : roles) {
                JSONObject o = new JSONObject();
                o.put("id", r.getId());
                o.put("roleName", r.getRoleName());
                o.put("roleCode", r.getRoleCode());
                arr.add(o);
            }
            return arr.toJSONString();
        };
        return new JeecgLlmTools(spec, exec);
    }

    /**
     * Grant roles to users
     * @return
     * @author chenrui
     * @date 2025/8/27 09:52
     */
    private JeecgLlmTools grantUserRolesTool() {
        ToolSpecification spec = ToolSpecification.builder()
                .name("grant_user_roles")
                .description("Grant roles to users，Supports granting multiple roles at one time；Skip if relationship already exists。return授予结果统计。")
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("userId", "userID，Required")
                                .addStringProperty("roleIds", "RoleIDlist，Required，Use commas to separate")
                                .required("userId","roleIds")
                                .build()
                )
                .build();
        ToolExecutor exec = (toolExecutionRequest, memoryId) -> {
            JSONObject args = JSONObject.parseObject(toolExecutionRequest.arguments());
            String userId = args.getString("userId");
            String roleIdsStr = args.getString("roleIds");
            if (StringUtils.isAnyBlank(userId, roleIdsStr)) {
                return "Parameter missing：userId or roleIds";
            }
            SysUser user = sysUserService.getById(userId);
            if (user == null) {
                return "user不存在：" + userId;
            }
            String[] roleIds = roleIdsStr.split(",");
            int added = 0, existed = 0, invalid = 0;
            for (String roleId : roleIds) {
                roleId = roleId.trim();
                if (roleId.isEmpty()) continue;
                SysRole role = sysRoleService.getById(roleId);
                if (role == null) { invalid++; continue; }
                QueryWrapper<org.jeecg.modules.system.entity.SysUserRole> q = new QueryWrapper<>();
                q.eq("role_id", roleId).eq("user_id", userId);
                org.jeecg.modules.system.entity.SysUserRole one = sysUserRoleService.getOne(q);
                if (one == null) {
                    org.jeecg.modules.system.entity.SysUserRole rel = new org.jeecg.modules.system.entity.SysUserRole(userId, roleId);
                    boolean ok = sysUserRoleService.save(rel);
                    if (ok) { added++; } else { invalid++; }
                } else {
                    existed++;
                }
            }
            return String.format("Grant completed：New%d，Already exists%d，invalid/fail%d", added, existed, invalid);
        };
        return new JeecgLlmTools(spec, exec);
    }
}
