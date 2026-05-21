package org.jeecg.modules.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Title: DuplicateCheckAction
 * @Description: Repeat verification tool
 * @Author Zhang Daihao
 * @Date 2019-03-25
 * @Version V1.0
 */
@Slf4j
@RestController
@RequestMapping("/sys/duplicate")
@Tag(name="Repeat check")
public class DuplicateCheckController {

	@Autowired
	ISysDictService sysDictService;

	/**
	 * Verify whether the data exists in the system
	 * 
	 * @return
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET)
	@Operation(summary="Repeat check接口")
	public Result<String> doDuplicateCheck(DuplicateCheckVo duplicateCheckVo, HttpServletRequest request) {
		log.debug("----duplicate check------："+ duplicateCheckVo.toString());
		
		// 1.Fill value is blank，Return directly
		if(StringUtils.isEmpty(duplicateCheckVo.getFieldVal())){
			Result rs = new Result();
			rs.setCode(500);
			rs.setSuccess(true);
			rs.setMessage("Data is empty,No processing！");
			return rs;
		}
		
		// 2.Return results
		if (sysDictService.duplicateCheckData(duplicateCheckVo)) {
			// This value is available
			return Result.ok("This value is available！");
		} else {
			// The value is not available
			log.info("The value is not available，already exists in the system！");
			return Result.error("The value is not available，already exists in the system！");
		}
	}


}
