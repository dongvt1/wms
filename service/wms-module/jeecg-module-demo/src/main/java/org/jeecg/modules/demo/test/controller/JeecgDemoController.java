package org.jeecg.modules.demo.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: Single table example
 * @Author: jeecg-boot
 * @Date:2018-12-29
 * @Version:V2.0
 */
@Slf4j
@Tag(name = "Single tableDEMO")
@RestController
@RequestMapping("/test/jeecgDemo")
public class JeecgDemoController extends JeecgController<JeecgDemo, IJeecgDemoService> {
    @Autowired
    private IJeecgDemoService jeecgDemoService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * Paginated list query
     *
     * @param jeecgDemo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "GetDemoData list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "system/examples/demo/index")
    public Result<?> list(JeecgDemo jeecgDemo, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<JeecgDemo> queryWrapper = QueryGenerator.initQueryWrapper(jeecgDemo, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<JeecgDemo> page = new Page<JeecgDemo>(pageNo, pageSize);

        IPage<JeecgDemo> pageList = jeecgDemoService.page(page, queryWrapper);
        log.info("Query the current page：" + pageList.getCurrent());
        log.info("Query the current page数量：" + pageList.getSize());
        log.info("Number of query results：" + pageList.getRecords().size());
        log.info("Total data：" + pageList.getTotal());
        return Result.OK(pageList);
    }

    /**
     * Add to
     *
     * @param jeecgDemo
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add totestDEMO")
    @Operation(summary = "Add toDEMO")
    public Result<?> add(@RequestBody JeecgDemo jeecgDemo) {
        jeecgDemoService.save(jeecgDemo);
        return Result.OK("Add to成功！");
    }

    /**
     * edit
     *
     * @param jeecgDemo
     * @return
     */
    @AutoLog(value = "editDEMO", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "editDEMO")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody JeecgDemo jeecgDemo) {
        jeecgDemoService.updateById(jeecgDemo);
        return Result.OK("Update successful！");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "deletetestDEMO")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "passIDdeleteDEMO")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        jeecgDemoService.removeById(id);
        return Result.OK("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "批量deleteDEMO")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.jeecgDemoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "passIDQueryDEMO")
    public Result<?> queryById(@Parameter(name = "id", description = "Exampleid", required = true) @RequestParam(name = "id", required = true) String id) {
        JeecgDemo jeecgDemo = jeecgDemoService.getById(id);
        return Result.OK(jeecgDemo);
    }

    /**
     * Exportexcel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public ModelAndView exportXls(HttpServletRequest request, JeecgDemo jeecgDemo) {
        //GetExport表格字段
        String exportFields = jeecgDemoService.getExportFields();
        //pointsheetExport表格字段
        return super.exportXlsSheet(request, jeecgDemo, JeecgDemo.class, "Single table模型",exportFields,500);
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, JeecgDemo.class);
    }

    // =====Redis Example===============================================================================================

    /**
     * redisoperate -- set
     */
    @GetMapping(value = "/redisSet")
    public void redisSet() {
        redisUtil.set("name", "Zhang San" + DateUtils.now());
    }

    /**
     * redisoperate -- get
     */
    @GetMapping(value = "/redisGet")
    public String redisGet() {
        return (String) redisUtil.get("name");
    }

    /**
     * redisoperate -- setObj
     */
    @GetMapping(value = "/redisSetObj")
    public void redisSetObj() {
        JeecgDemo p = new JeecgDemo();
        p.setAge(10);
        p.setBirthday(new Date());
        p.setContent("hello");
        p.setName("Zhang San");
        p.setSex("male");
        redisUtil.set("user-zdh", p);
    }

    /**
     * redisoperate -- setObj
     */
    @GetMapping(value = "/redisGetObj")
    public Object redisGetObj() {
        return redisUtil.get("user-zdh");
    }

    /**
     * redisoperate -- get
     */
    @GetMapping(value = "/redis/{id}")
    public JeecgDemo redisGetJeecgDemo(@PathVariable("id") String id) {
        JeecgDemo t = jeecgDemoService.getByIdCacheable(id);
        log.info(t.toString());
        return t;
    }

    // ===FreemakerExample================================================================================

    /**
     * freemakerWay 【Page path： src/main/resources/templates】
     *
     * @param modelAndView
     * @return
     */
    @IgnoreAuth
    @RequestMapping("/html")
    public ModelAndView ftl(ModelAndView modelAndView) {
        modelAndView.setViewName("demo3");
        List<String> userList = new ArrayList<String>();
        userList.add("admin");
        userList.add("user1");
        userList.add("user2");
        log.info("--------------test--------------");
        modelAndView.addObject("userList", userList);
        return modelAndView;
    }


    // ==========================================dynamic form JSONAcceptance test===========================================
    /**
     * onlineAdd new data
     */
    @PostMapping(value = "/testOnlineAdd")
    public Result<?> testOnlineAdd(@RequestBody JSONObject json) {
        log.info(json.toJSONString());
        return Result.OK("Add to成功！");
    }

    /*----------------------------------------外部Get权限Example------------------------------------*/

    /**
     * 【data权限Example - programming】mybatisPlus java类Way加载权限
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/mpList")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public Result<?> loadMpPermissonList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                         HttpServletRequest req) {
        QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
        //programmingWay，GivequeryWrapperLoad data permission rules
        QueryGenerator.installAuthMplus(queryWrapper, JeecgDemo.class);
        Page<JeecgDemo> page = new Page<JeecgDemo>(pageNo, pageSize);
        IPage<JeecgDemo> pageList = jeecgDemoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 【data权限Example - programming】mybatis xmlWay加载权限
     *
     * @param jeecgDemo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/sqlList")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public Result<?> loadSqlPermissonList(JeecgDemo jeecgDemo, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                          HttpServletRequest req) {
        IPage<JeecgDemo> pageList = jeecgDemoService.queryListWithPermission(pageSize, pageNo);
        return Result.OK(pageList);
    }
    /*----------------------------------------外部Get权限Example------------------------------------*/

    /**
     * online apiEnhance list
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaListHttp")
    public Result enhanceJavaListHttp(@RequestBody JSONObject params) {
        log.info(" =========================================================== ");
        log.info("params: " + params.toJSONString());
        log.info("params.tableName: " + params.getString("tableName"));
        log.info("params.json: " + params.getJSONObject("json").toJSONString());
        JSONArray dataList = params.getJSONArray("dataList");
        log.info("params.dataList: " + dataList.toJSONString());
        log.info(" =========================================================== ");
        return Result.OK(dataList);
    }

    /**
     * online apiEnhance form
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaFormHttp")
    public Result enhanceJavaFormHttp(@RequestBody JSONObject params) {
        log.info(" =========================================================== ");
        log.info("params: " + params.toJSONString());
        log.info("params.tableName: " + params.getString("tableName"));
        log.info("params.json: " + params.getJSONObject("json").toJSONString());
        log.info(" =========================================================== ");
        return Result.OK("1");
    }

    @GetMapping(value = "/hello")
    public String hello(HttpServletRequest req) {
        return "hello world!";
    }

    // =====Vue3 Native  原生页面Example===============================================================================================
    @GetMapping(value = "/oneNative/list")
    public Result oneNativeList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
        Object oneNative = redisUtil.get("one-native");
        JSONArray data = new JSONArray();
        if(null != oneNative){
            JSONObject nativeObject = (JSONObject) oneNative;
            data = nativeObject.getJSONArray("data");
        }
        IPage<JSONObject> objectPage = queryDataPage(data, pageNo, pageSize);
        return Result.OK(objectPage);
    }
    
    @PostMapping("/oneNative/add")
    public Result<String> oneNativeAdd(@RequestBody JSONObject jsonObject){
        Object oneNative = redisUtil.get("one-native");
        JSONObject nativeObject = new JSONObject();
        JSONArray data = new JSONArray();
        if(null != oneNative){
            nativeObject = (JSONObject) oneNative;
            data = nativeObject.getJSONArray("data");
        }
        jsonObject.put("id", UUIDGenerator.generate());
        data.add(jsonObject);
        nativeObject.put("data",data);
        redisUtil.set("one-native",nativeObject);
        return Result.OK("Add to成功");
    }
    
    @PutMapping("/oneNative/edit")
    public Result<String> oneNativeEdit(@RequestBody JSONObject jsonObject){
        JSONObject oneNative = (JSONObject)redisUtil.get("one-native");
        JSONArray data = oneNative.getJSONArray("data");
        data = getNativeById(data,jsonObject);
        oneNative.put("data", data);
        redisUtil.set("one-native", oneNative);
        return Result.OK("Modification successful");
    }

    @DeleteMapping("/oneNative/delete")
    public Result<String> oneNativeDelete(@RequestParam(name = "ids") String ids){
        Object oneNative = redisUtil.get("one-native");
        if(null != oneNative){
            JSONObject nativeObject = (JSONObject) oneNative;
            JSONArray data = nativeObject.getJSONArray("data");
            data = deleteNativeById(data,ids);
            nativeObject.put("data",data);
            redisUtil.set("one-native",nativeObject);
        }
        return Result.OK("delete成功");
    }
    
    /**
     * Getrediscorrespondiddata
     * @param data
     * @param jsonObject
     * @return
     */
    public JSONArray getNativeById(JSONArray data,JSONObject jsonObject){
        String dbId = "id";
        String id = jsonObject.getString(dbId);
        for (int i = 0; i < data.size(); i++) {
            if(id.equals(data.getJSONObject(i).getString(dbId))){
                data.set(i,jsonObject);
                break;
            }
        }
        return data;
    }

    /**
     * deleteredisincluded iniddata
     * @param data
     * @param ids
     * @return
     */
    public JSONArray deleteNativeById(JSONArray data,String ids){
        String dbId = "id";
        for (int i = 0; i < data.size(); i++) {
            //ifidContains direct clearingdata中data
            if(ids.contains(data.getJSONObject(i).getString(dbId))){
                data.fluentRemove(i);
            }
            //judgedataIs there any length left?1Bit
            if(data.size() == 1 && ids.contains(data.getJSONObject(0).getString(dbId))){
                data.fluentRemove(0);
            }
        }
        return data;
    }

    /**
     * 模拟Querydata，can be based on parentIDQuery，可以point页
     *
     * @param dataList Data list
     * @param pageNo   page number
     * @param pageSize page size
     * @return
     */
    private IPage<JSONObject> queryDataPage(JSONArray dataList, Integer pageNo, Integer pageSize) {
        // According to parentidQuery子级
        JSONArray dataDb = dataList;
        // 模拟point页（Practical applicationSQL自带的point页）
        List<JSONObject> records = new ArrayList<>();
        IPage<JSONObject> page;
        long beginIndex, endIndex;
        // if任意一个参数为null，则不point页
        if (pageNo == null || pageSize == null) {
            page = new Page<>(0, dataDb.size());
            beginIndex = 0;
            endIndex = dataDb.size();
        } else {
            page = new Page<>(pageNo, pageSize);
            beginIndex = page.offset();
            endIndex = page.offset() + page.getSize();
        }
        for (long i = beginIndex; (i < endIndex && i < dataDb.size()); i++) {
            JSONObject data = dataDb.getJSONObject((int) i);
            data = JSON.parseObject(data.toJSONString());
            // Do not return children
            data.remove("children");
            records.add(data);
        }
        page.setRecords(records);
        page.setTotal(dataDb.size());
        return page;
    }
    // =====Vue3 Native  原生页面Example===============================================================================================


    /**
     * Get创建人
     * @return
     */
    @GetMapping(value = "/groupList")
    public Result<?> groupList() {
        return Result.ok(jeecgDemoService.getCreateByList());
    }

    /**
     * testMonoobject
     * @return
     */
    @GetMapping(value ="/test")
    public Mono<String> test() {
        //solveshiroReport an errorNo SecurityManager accessible to the calling code, either bound to the org.apache.shiro
        // https://blog.csdn.net/Japhet_jiu/article/details/131177210
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        SecurityUtils.setSecurityManager(securityManager);
        
        return Mono.just("test");
    }

}
