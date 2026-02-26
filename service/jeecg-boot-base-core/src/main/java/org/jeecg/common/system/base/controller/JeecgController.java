package org.jeecg.common.system.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecgframework.poi.handler.inter.IExcelExportServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @Description: Controllerbase class
 * @Author: dangzhenghui@163.com
 * @Date: 2019-4-21 8:13
 * @Version: 1.0
 */
@Slf4j
public class JeecgController<T, S extends IService<T>> {
    /**issues/2933 JeecgControllerinjectionserviceUse insteadprotectedModify，to avoid repeated citationsservice*/
    @Autowired
    protected S service;
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;
    
    /**
     * Exportexcel
     *
     * @param request
     */
    protected ModelAndView exportXls(HttpServletRequest request, T object, Class<T> clazz, String title) {
        // Step.1 Assemble query conditions
        QueryWrapper<T> queryWrapper = QueryGenerator.initQueryWrapper(object, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // Filter selected data
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id",selectionList);
        }
        // Step.2 获取Exportdata
        List<T> exportList = service.list(queryWrapper);

        // Step.3 AutoPoi ExportExcel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //set herefilenameinvalid ,The front end will be re-updated and set up.
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(NormalExcelConstants.CLASS, clazz);
        //update-begin--Author:liusq  Date:20210126 for：图片Export报错，ImageBasePathnot set--------------------
        ExportParams exportParams=new ExportParams(title + "Report", "Export人:" + sysUser.getRealname(), title);
        exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
        //update-end--Author:liusq  Date:20210126 for：图片Export报错，ImageBasePathnot set----------------------
        mv.addObject(NormalExcelConstants.PARAMS,exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
        return mv;
    }
    /**
     * According to each pagesheet数量Exportmanysheet
     *
     * @param request
     * @param object Entity class
     * @param clazz Entity classclass
     * @param title title
     * @param exportFields Export字段自definition
     * @param pageNum eachsheetnumber of data items
     * @param request
     */
    protected ModelAndView exportXlsSheet(HttpServletRequest request, T object, Class<T> clazz, String title,String exportFields,Integer pageNum) {
        // Step.1 Assemble query conditions
        QueryWrapper<T> queryWrapper = QueryGenerator.initQueryWrapper(object, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // Step.2 Calculate paginationsheetdata
        double total = service.count();
        int count = (int)Math.ceil(total/pageNum);
        //update-begin-author:liusq---date:20220629--for: manysheetExport根据选择Export写法调整 ---
        // Step.3  Filter selected data
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id",selectionList);
        }
        //update-end-author:liusq---date:20220629--for: manysheetExport根据选择Export写法调整 ---
        // Step.4 manysheetdeal with
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <=count ; i++) {
            Page<T> page = new Page<T>(i, pageNum);
            IPage<T> pageList = service.page(page, queryWrapper);
            List<T> exportList = pageList.getRecords();
            Map<String, Object> map = new HashMap<>(5);
            ExportParams exportParams=new ExportParams(title + "Report", "Export人:" + sysUser.getRealname(), title+i,jeecgBaseConfig.getPath().getUpload());
            exportParams.setType(ExcelType.XSSF);
            //map.put("title",exportParams);
            //sheetTitle
            map.put(NormalExcelConstants.PARAMS,exportParams);
            //sheet对应实体
            map.put(NormalExcelConstants.CLASS,clazz);
            //data集合
            map.put(NormalExcelConstants.DATA_LIST, exportList);
            listMap.add(map);
        }
        // Step.4 AutoPoi ExportExcel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //set herefilenameinvalid ,The front end will be re-updated and set up.
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(NormalExcelConstants.MAP_LIST, listMap);
        return mv;
    }

    /**
     * 大dataExport
     * @param request
     * @param object
     * @param clazz
     * @param title
     * @param pageSize 每次查询的data量
     * @return
     * @author chenrui
     * @date 2025/8/11 16:11
     */
    protected ModelAndView exportXlsForBigData(HttpServletRequest request, T object, Class<T> clazz, String title,Integer pageSize) {
        // Assemble query conditions
        QueryWrapper<T> queryWrapper = QueryGenerator.initQueryWrapper(object, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // Calculate pagination数
        double total = service.count();
        int count = (int) Math.ceil(total / pageSize);
        // Filter selected data
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }

        // definitionIExcelExportServer
        IExcelExportServer excelExportServer = (queryParams, pageNum) -> {
            if (pageNum > count) {
                return null;
            }
            Page<T> page = new Page<T>(pageNum, pageSize);
            IPage<T> pageList = service.page(page, (QueryWrapper<T>) queryParams);
            return new ArrayList<>(pageList.getRecords());
        };

        // AutoPoi ExportExcel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //set herefilenameinvalid ,The front end will be re-updated and set up.
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(NormalExcelConstants.CLASS, clazz);
        ExportParams exportParams = new ExportParams(title + "Report", "Export人:" + sysUser.getRealname(), title, jeecgBaseConfig.getPath().getUpload());
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.EXPORT_SERVER, excelExportServer);
        mv.addObject(NormalExcelConstants.QUERY_PARAMS, queryWrapper);
        return mv;
    }


    /**
     * 根据权限Exportexcel，传入Export字段参数
     *
     * @param request
     */
    protected ModelAndView exportXls(HttpServletRequest request, T object, Class<T> clazz, String title,String exportFields) {
        ModelAndView mv = this.exportXls(request,object,clazz,title);
        mv.addObject(NormalExcelConstants.EXPORT_FIELDS,exportFields);
        return mv;
    }

    /**
     * Get objectID
     *
     * @return
     */
    private String getId(T item) {
        try {
            return PropertyUtils.getProperty(item, "id").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * passexcel导入data
     *
     * @param request
     * @param response
     * @return
     */
    protected Result<?> importExcel(HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Get the uploaded file object
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<T> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
                //update-begin-author:taoyan date:20190528 for:批量插入data
                long start = System.currentTimeMillis();
                service.saveBatch(list);
                //400strip saveBatchTime consuming1592millisecond  循环插入Time consuming1947millisecond
                //1200strip  saveBatchTime consuming3687millisecond 循环插入Time consuming5212millisecond
                log.info("Time consuming" + (System.currentTimeMillis() - start) + "millisecond");
                //update-end-author:taoyan date:20190528 for:批量插入data
                return Result.ok("File imported successfully！data行数：" + list.size());
            } catch (Exception e) {
                //update-begin-author:taoyan date:20211124 for: 导入data重复增加提示
                String msg = e.getMessage();
                log.error(msg, e);
                if(msg!=null && msg.indexOf("Duplicate entry")>=0){
                    return Result.error("File import failed:有重复data！");
                }else{
                    return Result.error("File import failed:" + e.getMessage());
                }
                //update-end-author:taoyan date:20211124 for: 导入data重复增加提示
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("File import failed！");
    }
}
