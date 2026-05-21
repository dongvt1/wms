package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysGatewayRoute;
import org.jeecg.modules.system.service.ISysGatewayRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: gatewayRoute management
 * @Author: jeecg-boot
 * @Date: 2020-05-26
 * @Version: V1.0
 */
@Tag(name = "gatewayRoute management")
@RestController
@RequestMapping("/sys/gatewayRoute")
@Slf4j
public class SysGatewayRouteController extends JeecgController<SysGatewayRoute, ISysGatewayRouteService> {

	@Autowired
	private ISysGatewayRouteService sysGatewayRouteService;

    @PostMapping(value = "/updateAll")
    public Result<?> updateAll(@RequestBody JSONObject json) {
        sysGatewayRouteService.updateAll(json);
        return Result.ok("Operation successful！");
    }

	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysGatewayRoute sysGatewayRoute) {
		LambdaQueryWrapper<SysGatewayRoute> query = new LambdaQueryWrapper<>();
		List<SysGatewayRoute> ls = sysGatewayRouteService.list(query);
		JSONArray array = new JSONArray();
		for(SysGatewayRoute rt: ls){
			JSONObject obj = (JSONObject) JSONObject.toJSON(rt);
			if(oConvertUtils.isNotEmpty(rt.getPredicates())){
				obj.put("predicates", JSONArray.parseArray(rt.getPredicates()));
			}
			if(oConvertUtils.isNotEmpty(rt.getFilters())){
				obj.put("filters", JSONArray.parseArray(rt.getFilters()));
			}
			array.add(obj);
		}
		return Result.ok(array);
	}

	@GetMapping(value = "/clearRedis")
	public Result<?> clearRedis() {
		sysGatewayRouteService.clearRedis();
		return Result.ok("Clear successfully！");
	}

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @RequiresPermissions("system:getway:delete")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysGatewayRouteService.deleteById(id);
        return Result.ok("deleterouting成功");
    }

	/**
	 * 查询被delete的列表
	 * @return
	 */
	@RequestMapping(value = "/deleteList", method = RequestMethod.GET)
	public Result<List<SysGatewayRoute>> deleteList(HttpServletRequest request) {
		Result<List<SysGatewayRoute>> result = new Result<>();
		List<SysGatewayRoute> list = sysGatewayRouteService.getDeletelist();
		result.setSuccess(true);
		result.setResult(list);
		return result;
	}

	/**
	 * 还原被逻辑delete的routing
	 *
	 * @param jsonObject
	 * @return
	 */
	@RequiresPermissions("system:gateway:putRecycleBin")
	@RequestMapping(value = "/putRecycleBin", method = RequestMethod.PUT)
	public Result putRecycleBin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
		try {
			String ids = jsonObject.getString("ids");
			if (StringUtils.isNotBlank(ids)) {
				sysGatewayRouteService.revertLogicDeleted(Arrays.asList(ids.split(",")));
				return Result.ok("Operation successful!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Operation failed!");
		}
		return Result.ok("Restore successful");
	}
	/**
	 * 彻底deleterouting
	 *
	 * @param ids 被delete的routingID，MultipleidSeparate with half-width commas
	 * @return
	 */
	@RequiresPermissions("system:gateway:deleteRecycleBin")
	@RequestMapping(value = "/deleteRecycleBin", method = RequestMethod.DELETE)
	public Result deleteRecycleBin(@RequestParam("ids") String ids) {
		try {
			if (StringUtils.isNotBlank(ids)) {
				sysGatewayRouteService.deleteLogicDeleted(Arrays.asList(ids.split(",")));
			}
			return Result.ok("delete成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("delete失败!");
		}
	}
	/**
	 * Copy route
	 *
	 * @param id routingid
	 * @return
	 */
	@RequiresPermissions("system:gateway:copyRoute")
	@RequestMapping(value = "/copyRoute", method = RequestMethod.GET)
	public Result<SysGatewayRoute> copyRoute(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
		Result<SysGatewayRoute> result = new Result<>();
		SysGatewayRoute sysGatewayRoute= sysGatewayRouteService.copyRoute(id);
		result.setResult(sysGatewayRoute);
		result.setSuccess(true);
		return result;
	}
}
