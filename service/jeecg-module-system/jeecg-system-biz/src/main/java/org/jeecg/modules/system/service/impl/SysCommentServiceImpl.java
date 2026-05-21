package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.FileTypeEnum;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.SysFilesModel;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.entity.SysFormFile;
import org.jeecg.modules.system.mapper.SysCommentMapper;
import org.jeecg.modules.system.mapper.SysFormFileMapper;
import org.jeecg.modules.system.service.ISysCommentService;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;
import org.jeecg.modules.system.vo.UserAvatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: System comment reply form
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Service
public class SysCommentServiceImpl extends ServiceImpl<SysCommentMapper, SysComment> implements ISysCommentService {

    @Autowired
    private ISysBaseAPI sysBaseApi;

    @Autowired
    private SysFormFileMapper sysFormFileMapper;

//    @Autowired
//    private IEasyOaBaseApi easyOaBseApi;

    @Autowired
    private RedisUtil redisUtil;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    @Value(value = "${jeecg.uploadType}")
    private String uploadType;

    /**
     * sysFormFiletable name in
     */
    private static final String SYS_FORM_FILE_TABLE_NAME = "sys_comment";

    @Override
    public List<SysCommentVO> queryFormCommentInfo(SysComment sysComment) {
        String tableName = sysComment.getTableName();
        String dataId = sysComment.getTableDataId();
        //Get comment information
        List<SysCommentVO> list = this.baseMapper.queryCommentList(tableName, dataId);
        // Get relevant people for comments
        Set<String> personSet = new HashSet<>();
        if(list!=null && list.size()>0){
            for(SysCommentVO vo: list){
                if(oConvertUtils.isNotEmpty(vo.getFromUserId())){
                    personSet.add(vo.getFromUserId());    
                }
                if(oConvertUtils.isNotEmpty(vo.getToUserId())){
                    personSet.add(vo.getToUserId());
                }
            }
        }
        if(personSet.size()>0){
            //Get user information
            Map<String, UserAvatar> userAvatarMap = queryUserAvatar(personSet);
            for(SysCommentVO vo: list){
                String formId = vo.getFromUserId();
                String toId = vo.getToUserId();
                // Set avatar、username
                if(oConvertUtils.isNotEmpty(formId)){
                    UserAvatar fromUser = userAvatarMap.get(formId);
                    if(fromUser!=null){
                        vo.setFromUserId_dictText(fromUser.getRealname());
                        vo.setFromUserAvatar(fromUser.getAvatar());
                    }
                }
                if(oConvertUtils.isNotEmpty(toId)){
                    UserAvatar toUser = userAvatarMap.get(toId);
                    if(toUser!=null){
                        vo.setToUserId_dictText(toUser.getRealname());
                        vo.setToUserAvatar(toUser.getAvatar());
                    }
                }
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOneFileComment(HttpServletRequest request) {
        String existFileId = request.getParameter("fileId");
        if(oConvertUtils.isEmpty(existFileId)){
            String savePath = "";
            // Get business path
            String bizPath = request.getParameter("biz");
            // Get the uploaded file object
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("file");

            // File security check，Prevent vulnerable files from being uploaded
            try {
                SsrfFileTypeFilter.checkUploadFileType(file, bizPath);
            } catch (Exception e) {
                throw new JeecgBootException(e);
            }

            if (oConvertUtils.isEmpty(bizPath)) {
                bizPath = CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType) ? "upload" : "";
            }
            if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
                savePath = this.uploadLocal(file, bizPath);
            } else {
                savePath = CommonUtils.upload(file, bizPath, uploadType);
            }

            String orgName = file.getOriginalFilename();
            // Get file name
            orgName = CommonUtils.getFileName(orgName);
            //file size
            long size = file.getSize();
            //File type
            String type = orgName.substring(orgName.lastIndexOf("."), orgName.length());
            FileTypeEnum fileType = FileTypeEnum.getByType(type);

            //Save to SysFiles
            SysFilesModel sysFiles = new SysFilesModel();
            sysFiles.setFileName(orgName);
            sysFiles.setUrl(savePath);
            sysFiles.setFileType(fileType.getValue());
            sysFiles.setStoreType("temp");
            if (size > 0) {
                sysFiles.setFileSize(Double.parseDouble(String.valueOf(size)));
            }
            String fileId = String.valueOf(IdWorker.getId());
            sysFiles.setId(fileId);
            String tenantId = oConvertUtils.getString(TenantContext.getTenant());
            sysFiles.setTenantId(tenantId);
//            //update-begin---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Knowledge base backend migration---
//            easyOaBseApi.addSysFiles(sysFiles);
//            //update-end---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Knowledge base backend migration---

            //Save to SysFormFile
            String tableName = SYS_FORM_FILE_TABLE_NAME;
            String tableDataId = request.getParameter("commentId");
            SysFormFile sysFormFile = new SysFormFile();
            sysFormFile.setTableName(tableName);
            sysFormFile.setFileType(fileType.getValue());
            sysFormFile.setTableDataId(tableDataId);
            sysFormFile.setFileId(fileId);
            sysFormFileMapper.insert(sysFormFile);

        }else{
//            //update-begin---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Knowledge base backend migration---
//            SysFilesModel sysFiles = easyOaBseApi.getFileById(existFileId);
//            //update-end---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Knowledge base backend migration---
//            if(sysFiles!=null){
                //Save to SysFormFile
                String tableName = SYS_FORM_FILE_TABLE_NAME;
                String tableDataId = request.getParameter("commentId");
                SysFormFile sysFormFile = new SysFormFile();
                sysFormFile.setTableName(tableName);
                sysFormFile.setFileType("");
                sysFormFile.setTableDataId(tableDataId);
                sysFormFile.setFileId(existFileId);
                sysFormFileMapper.insert(sysFormFile);
//            }
        }
        //update-end-author:taoyan date:2023-6-12 for: QQYUN-4310【document】从document库选择document功能未做
    }

    /**
     * app端回复评论保存document
     * @param request
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void appSaveOneFileComment(HttpServletRequest request) {

        String orgName = request.getParameter("fileName");
        String fileSize = request.getParameter("fileSize");
        String savePath = request.getParameter("savePath");
        // Get file name
        orgName = CommonUtils.getFileName(orgName);
        //file size
        long size = Long.valueOf(fileSize);
        //File type
        String type = orgName.substring(orgName.lastIndexOf("."), orgName.length());
        FileTypeEnum fileType = FileTypeEnum.getByType(type);

        //Save to SysFiles
        SysFilesModel sysFiles = new SysFilesModel();
        sysFiles.setFileName(orgName);
        sysFiles.setUrl(savePath);
        sysFiles.setFileType(fileType.getValue());
        sysFiles.setStoreType("temp");
        if (size > 0) {
            sysFiles.setFileSize(Double.parseDouble(String.valueOf(size)));
        }
        String defaultValue = "0";
        String fileId = String.valueOf(IdWorker.getId());
        sysFiles.setId(fileId);
        String tenantId = oConvertUtils.getString(TenantContext.getTenant());
        sysFiles.setTenantId(tenantId);
//        //update-begin---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Knowledge base backend migration---
//        easyOaBseApi.addSysFiles(sysFiles);
//        //update-end---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Knowledge base backend migration---
        //Save to SysFormFile
        String tableName = SYS_FORM_FILE_TABLE_NAME;
        String tableDataId = request.getParameter("commentId");
        SysFormFile sysFormFile = new SysFormFile();
        sysFormFile.setTableName(tableName);
        sysFormFile.setFileType(fileType.getValue());
        sysFormFile.setTableDataId(tableDataId);
        sysFormFile.setFileId(fileId);
        sysFormFileMapper.insert(sysFormFile);

    }

    @Override
    public List<SysCommentFileVo> queryFormFileList(String tableName, String formDataId) {
        List<SysCommentFileVo> list = baseMapper.queryFormFileList(tableName, formDataId);
        return list;
    }

    @Override
    public String saveOne(SysComment sysComment) {
        this.save(sysComment);
        //Send system message
        String content = sysComment.getCommentContent();
        if (content.indexOf("@") >= 0) {
            Set<String> set = getCommentUsername(content);
            if (set.size() > 0) {
                String users = String.join(",", set);
                MessageDTO md = new MessageDTO();
                md.setTitle("Someone mentioned you in a form comment");
                md.setContent(content);
                md.setToAll(false);
                md.setToUser(users);
                md.setFromUser("system");
                md.setType(MessageTypeEnum.XT.getType());

                // update-begin-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，The other party cannot see which form it is.@of，no hyperlink
                String tableName = sysComment.getTableName();
                String prefix = "desform:";
                if (tableName != null) {
                    // form designer
                    if (tableName.startsWith(prefix)) {
                        Map<String, Object> data = new HashMap<>();
                        data.put(CommonConstant.NOTICE_MSG_BUS_TYPE, "comment");
                        JSONObject params = new JSONObject();
                        params.put("code", tableName.substring(prefix.length()));
                        params.put("dataId", sysComment.getTableDataId());
                        params.put("type", "designForm");
                        data.put(CommonConstant.NOTICE_MSG_SUMMARY, params);
                        md.setData(data);
                    }
                    // Onlineform，Determine whether to carryid
                    else if (oConvertUtils.isNotEmpty(sysComment.getTableId())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put(CommonConstant.NOTICE_MSG_BUS_TYPE, "comment");
                        JSONObject params = new JSONObject();
                        params.put("code", tableName);
                        params.put("formId", sysComment.getTableId());
                        params.put("dataId", sysComment.getTableDataId());
                        params.put("type", "cgform");
                        data.put(CommonConstant.NOTICE_MSG_SUMMARY, params);
                        md.setData(data);
                    }
                }
                // update-end-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，The other party cannot see which form it is.@of，no hyperlink
                
                sysBaseApi.sendTemplateMessage(md);
            }
        }
        return sysComment.getId();
    }

    @Override
    public void deleteOne(String id) {
        this.removeById(id);
        //还要删除关联document
        LambdaQueryWrapper<SysFormFile> query = new LambdaQueryWrapper<SysFormFile>()
                .eq(SysFormFile::getTableDataId, id)
                .eq(SysFormFile::getTableName, SYS_FORM_FILE_TABLE_NAME);
        this.sysFormFileMapper.delete(query);
    }

    /**
     * 通过正则获取评论中of用户账号
     *
     * @return
     */
    private Set<String> getCommentUsername(String content) {
        Set<String> set = new HashSet<String>(3);
        String reg = "(@(.*?)\\[(.*?)\\])";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        while (m.find()) {
            if (m.groupCount() == 3) {
                String username = m.group(3);
                set.add(username);
            }
        }
        return set;
    }


    /**
     * 本地document上传
     *
     * @param mf      document
     * @param bizPath custom path
     * @return
     */
    private String uploadLocal(MultipartFile mf, String bizPath) {
        try {
            // File security check，Prevent vulnerable files from being uploaded
            SsrfFileTypeFilter.checkUploadFileType(mf, bizPath);
        } catch (Exception e) {
            throw new JeecgBootException(e);
        }
        
        try {
            String ctxPath = uploadpath;
            String fileName = null;
            File file = new File(ctxPath + File.separator + bizPath + File.separator);
            if (!file.exists()) {
                file.mkdirs();// 创建document根目录
            }
            String orgName = mf.getOriginalFilename();// Get file name
            orgName = CommonUtils.getFileName(orgName);
            if (orgName.indexOf(".") != -1) {
                fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
            } else {
                fileName = orgName + "_" + System.currentTimeMillis();
            }
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath = null;
            if (oConvertUtils.isNotEmpty(bizPath)) {
                dbpath = bizPath + File.separator + fileName;
            } else {
                dbpath = fileName;
            }
            if (dbpath.contains("\\")) {
                dbpath = dbpath.replace("\\", "/");
            }
            return dbpath;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * Query user information
     * @param idSet
     * @return
     */
    private Map<String, UserAvatar> queryUserAvatar(Set<String> idSet){
        List<UserAvatar> list = this.baseMapper.queryUserAvatarList(idSet);
        Map<String, UserAvatar> map = new HashMap<>();
        if(list!=null && list.size()>0){
            for(UserAvatar user: list){
                map.put(user.getId(), user);
            }
        }
        return map;
    }
    
}
