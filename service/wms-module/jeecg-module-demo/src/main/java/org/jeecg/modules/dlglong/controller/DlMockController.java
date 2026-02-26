package org.jeecg.modules.dlglong.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.MatchTypeEnum;
import org.jeecg.common.system.query.QueryCondition;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.constant.VxeSocketConst;
import org.jeecg.modules.demo.mock.vxe.websocket.VxeSocket;
import org.jeecg.modules.dlglong.entity.MockEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;

/**
 * @Description: DlMockController
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/mock/dlglong")
public class DlMockController {

    /**
     * Simulate changing state
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("/change1")
    public Result mockChange1(@RequestParam("id") String id, @RequestParam("status") String status) {
        /* id for OKid（rowId），Just getrowId，Then just call VXESocket.sendMessageToAll() That’s it */

        // Encapsulate row data
        JSONObject rowData = new JSONObject();
        // This field is the row data to be changedID
        rowData.put("id", id);
        // This field is the column to be changedkeyand specific values
        rowData.put("status", status);
        // Simulate changing data
        this.mockChange(rowData);

        return Result.ok();
    }

    /**
     * Simulate changing tug status
     *
     * @param id
     * @param tugStatus
     * @return
     */
    @GetMapping("/change2")
    public Result mockChange2(@RequestParam("id") String id, @RequestParam("tug_status") String tugStatus) {
        /* id for OKid（rowId），Just getrowId，Then just call VXESocket.sendMessageToAll() That’s it */

        // Encapsulate row data
        JSONObject rowData = new JSONObject();
        // This field is the row data to be changedID
        rowData.put("id", id);
        // This field is the column to be changedkeyand specific values
        JSONObject status = JSON.parseObject(tugStatus);
        rowData.put("tug_status", status);
        // Simulate changing data
        this.mockChange(rowData);

        return Result.ok();
    }

    /**
     * Simulate changing progress bar state
     *
     * @param id
     * @param progress
     * @return
     */
    @GetMapping("/change3")
    public Result mockChange3(@RequestParam("id") String id, @RequestParam("progress") String progress) {
        /* id for OKid（rowId），Just getrowId，Then just call VXESocket.sendMessageToAll() That’s it */

        // Encapsulate row data
        JSONObject rowData = new JSONObject();
        // This field is the row data to be changedID
        rowData.put("id", id);
        // This field is the column to be changedkeyand specific values
        rowData.put("progress", progress);
        // Simulate changing data
        this.mockChange(rowData);

        return Result.ok();
    }

    private void mockChange(JSONObject rowData) {
        // Encapsulationsocketdata
        JSONObject socketData = new JSONObject();
        // Here's socketKey It must be written on the scheduling plan page. socketKey Properties remain consistent
        socketData.put("socketKey", "page-dispatch");
        // Here's args Must be an array，subscript0yes行data，subscript1yescaseId，Generally no need to pass
        socketData.put("args", new Object[]{rowData, ""});
        // Encapsulation消息字符串，Here's type 必须yes VXESocketConst.TYPE_UVT
        String message = VxeSocket.packageMessage(VxeSocketConst.TYPE_UVT, socketData);
        // call sendMessageToAll Send to all online users
        VxeSocket.sendMessageToAll(message);
    }

    /**
     * simulate changes【Big ship awaits trial】state
     *
     * @param status
     * @return
     */
    @GetMapping("/change4")
    public Result mockChange4(@RequestParam("status") String status) {
        // Encapsulationsocketdata
        JSONObject socketData = new JSONObject();
        // Here's key yes前端Note册时使用ofkey，Must be consistent
        socketData.put("key", "dispatch-dcds-status");
        // Here's args Must be an array，每一位都yesNote册方法of参数，pass in order
        socketData.put("args", new Object[]{status});

        // Encapsulation消息字符串，Here's type 必须yes VXESocketConst.TYPE_UVT
        String message = VxeSocket.packageMessage(VxeSocketConst.TYPE_CSD, socketData);
        // call sendMessageToAll Send to all online users
        VxeSocket.sendMessageToAll(message);

        return Result.ok();
    }

    /**
     * 【simulation】Instant save单行data
     *
     * @param rowData 行data，In actual use, it can be replaced by an entity class
     */
    @PutMapping("/immediateSaveRow")
    public Result mockImmediateSaveRow(@RequestBody JSONObject rowData) throws Exception {
        System.out.println("Instant save.rowData：" + rowData.toJSONString());
        // delay1.5Second，simulation网慢堵塞真实感
        Thread.sleep(500);
        return Result.ok();
    }

    /**
     * 【simulation】Instant save整个表格ofdata
     *
     * @param tableData 表格data（In actual use, it can be replaced by aListEntity class）
     */
    @PostMapping("/immediateSaveAll")
    public Result mockImmediateSaveAll(@RequestBody JSONArray tableData) throws Exception {
        // 【Note】：
        // 1、tableData里包含该页所有ofdata
        // 2、if you achieve“Instant save”，那么除了新增ofdata，其他of都yes已经保存过of了，
        //    No need to do it againupdateOperated，所以可以在前端传dataof时候就遍历判断一下，
        //    只传新增ofdata给后台insertThat’s it，Otherwise, it will cause a waste of performance。
        // 3、新增of行yes没有idof，through this，就可以判断yes否yes新增ofdata

        System.out.println("Instant save.tableData：" + tableData.toJSONString());
        // delay1.5Second，simulation网慢堵塞真实感
        Thread.sleep(1000);
        return Result.ok();
    }

    /**
     * 获取simulationdata
     *
     * @param pageNo   page number
     * @param pageSize page size
     * @param parentId fatherID，If not passed, query the top level
     * @return
     */
    @GetMapping("/getData")
    public Result getMockData(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            // father级id，根据father级idQuery children，如果for空则Query顶级
            @RequestParam(name = "parentId", required = false) String parentId
    ) {
        // simulationJSONdata路径
        String path = "classpath:org/jeecg/modules/dlglong/json/dlglong.json";
        // readJSONdata
        JSONArray dataList = readJsonData(path);
        if (dataList == null) {
            return Result.error("readdata失败！");
        }
        IPage<JSONObject> page = this.queryDataPage(dataList, parentId, pageNo, pageSize);
        return Result.ok(page);
    }

    /**
     * 获取simulation“Scheduling plan”页面ofdata
     *
     * @param pageNo   page number
     * @param pageSize page size
     * @param parentId fatherID，If not passed, query the top level
     * @return
     */
    @GetMapping("/getDdjhData")
    public Result getMockDdjhData(
            // SpringMVC 会自动将参数Note入到实体里
            MockEntity mockEntity,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            // father级id，根据father级idQuery children，如果for空则Query顶级
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam(name = "status", required = false) String status,
            // Advanced query conditions
            @RequestParam(name = "superQueryParams", required = false) String superQueryParams,
            // Advanced query mode
            @RequestParam(name = "superQueryMatchType", required = false) String superQueryMatchType,
            HttpServletRequest request
    ) {
        // Get query conditions（前台传递ofQuery参数）
        Map<String, String[]> parameterMap = request.getParameterMap();
        // Traverse output to console
        System.out.println("\ngetDdjhData - General query conditions：");
        for (String key : parameterMap.keySet()) {
            System.out.println("-- " + key + ": " + JSON.toJSONString(parameterMap.get(key)));
        }
        // Output advanced query
        try {
            System.out.println("\ngetDdjhData - Advanced query conditions：");
            // Advanced query mode
            MatchTypeEnum matchType = MatchTypeEnum.getByValue(superQueryMatchType);
            if (matchType == null) {
                System.out.println("-- Advanced query mode：Not recognized（" + superQueryMatchType + "）");
            } else {
                System.out.println("-- Advanced query mode：" + matchType.getValue());
            }
            superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
            List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
            if (conditions != null) {
                for (QueryCondition condition : conditions) {
                    System.out.println("-- " + JSON.toJSONString(condition));
                }
            } else {
                System.out.println("-- 没有传递任何Advanced query conditions");
            }
            System.out.println();
        } catch (Exception e) {
            log.error("-- Advanced query operation failed：" + superQueryParams, e);
            e.printStackTrace();
        }

        /* Note：实际使用中不用写上面那种繁琐of代码，这里只yesfor了直观of输出到控制台里而写of示例，
              It is more concise and convenient to use the following writing method */

        // Encapsulation成 MyBatisPlus 能识别of QueryWrapper，This object can be used directlySQLFilter condition splicing
        // 这个方法也会自动EncapsulationAdvanced query conditions，但yes高级Query参数名必须yessuperQueryParamsandsuperQueryMatchType
        QueryWrapper<MockEntity> queryWrapper = QueryGenerator.initQueryWrapper(mockEntity, parameterMap);
        System.out.println("queryWrapper： " + queryWrapper.getCustomSqlSegment());

        // simulationJSONdata路径
        String path = "classpath:org/jeecg/modules/dlglong/json/ddjh.json";
        String statusValue = "8";
        if (statusValue.equals(status)) {
            path = "classpath:org/jeecg/modules/dlglong/json/ddjh_s8.json";
        }
        // readJSONdata
        JSONArray dataList = readJsonData(path);
        if (dataList == null) {
            return Result.error("readdata失败！");
        }

        IPage<JSONObject> page = this.queryDataPage(dataList, parentId, pageNo, pageSize);
        // 逐行Query子表data，用于计算拖轮state
        List<JSONObject> records = page.getRecords();
        for (JSONObject record : records) {
            Map<String, Integer> tugStatusMap = new HashMap<>(5);
            String id = record.getString("id");
            // Query出主表of拖轮
            String tugMain = record.getString("tug");
            // 判断yes否有值
            if (StringUtils.isNotBlank(tugMain)) {
                // tugs split based on semicolon
                String[] tugs = tugMain.split(";");
                // Query子表data
                List<JSONObject> subRecords = this.queryDataPage(dataList, id, null, null).getRecords();
                // 遍历子表and拖轮data，找出进行计算反推拖轮state
                for (JSONObject subData : subRecords) {
                    String subTug = subData.getString("tug");
                    if (StringUtils.isNotBlank(subTug)) {
                        for (String tug : tugs) {
                            if (tug.equals(subTug)) {
                                // 计算拖轮state逻辑
                                int statusCode = 0;

                                /* If there is a departure time、Job start time、Job end time、Return time，则主表中of拖轮列中of每个拖轮背景色要即时变色 */

                                // There is a departure time，state +1
                                String departureTime = subData.getString("departure_time");
                                if (StringUtils.isNotBlank(departureTime)) {
                                    statusCode += 1;
                                }
                                // 有Job start time，state +1
                                String workBeginTime = subData.getString("work_begin_time");
                                if (StringUtils.isNotBlank(workBeginTime)) {
                                    statusCode += 1;
                                }
                                // 有Job end time，state +1
                                String workEndTime = subData.getString("work_end_time");
                                if (StringUtils.isNotBlank(workEndTime)) {
                                    statusCode += 1;
                                }
                                // 有Return time，state +1
                                String returnTime = subData.getString("return_time");
                                if (StringUtils.isNotBlank(returnTime)) {
                                    statusCode += 1;
                                }
                                // 保存拖轮state，keyyes拖轮of值，valueyesstate，前端根据不同ofstate码，显示不同of颜色，This color can also be calculated in the background and returned to the front end for direct use.
                                tugStatusMap.put(tug, statusCode);
                                break;
                            }
                        }
                    }
                }
            }
            // 新加一个字段用于保存拖轮state，不要直接覆盖原来of，这个字段可以不保存到data库里
            record.put("tug_status", tugStatusMap);
        }
        page.setRecords(records);
        return Result.ok(page);
    }

    /**
     * simulationQuerydata，可以根据fatherIDQuery，Can be paginated
     *
     * @param dataList data列表
     * @param parentId fatherID
     * @param pageNo   page number
     * @param pageSize page size
     * @return
     */
    private IPage<JSONObject> queryDataPage(JSONArray dataList, String parentId, Integer pageNo, Integer pageSize) {
        // 根据father级idQuery children
        JSONArray dataDb = dataList;
        if (StringUtils.isNotBlank(parentId)) {
            JSONArray results = new JSONArray();
            List<String> parentIds = Arrays.asList(parentId.split(","));
            this.queryByParentId(dataDb, parentIds, results);
            dataDb = results;
        }
        // simulation分页（Practical applicationSQL自带of分页）
        List<JSONObject> records = new ArrayList<>();
        IPage<JSONObject> page;
        long beginIndex, endIndex;
        // 如果任意一个参数fornull，no paging
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

    private void queryByParentId(JSONArray dataList, List<String> parentIds, JSONArray results) {
        for (int i = 0; i < dataList.size(); i++) {
            JSONObject data = dataList.getJSONObject(i);
            JSONArray children = data.getJSONArray("children");
            // 找到了该father级
            if (parentIds.contains(data.getString("id"))) {
                if (children != null) {
                    // addAll of目ofyes将多个子表ofdata合并在一起
                    results.addAll(children);
                }
            } else {
                if (children != null) {
                    queryByParentId(children, parentIds, results);
                }
            }
        }
        results.addAll(new JSONArray());
    }

    private JSONArray readJsonData(String path) {
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""));
            if (stream != null) {
                String json = IOUtils.toString(stream, "UTF-8");
                return JSON.parseArray(json);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get the last location of the vehicle
     *
     * @return
     */
    @PostMapping("/findLatestCarLngLat")
    public List findLatestCarLngLat() {
        // simulationJSONdata路径
        String path = "classpath:org/jeecg/modules/dlglong/json/CarLngLat.json";
        // readJSONdata
        return readJsonData(path);
    }

    /**
     * Get the last location of the vehicle
     *
     * @return
     */
    @PostMapping("/findCarTrace")
    public List findCarTrace() {
        // simulationJSONdata路径
        String path = "classpath:org/jeecg/modules/dlglong/json/CarTrace.json";
        // readJSONdata
        return readJsonData(path);
    }

}
