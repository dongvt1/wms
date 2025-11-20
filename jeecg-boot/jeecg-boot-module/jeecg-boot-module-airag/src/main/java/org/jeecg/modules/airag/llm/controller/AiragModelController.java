package org.jeecg.modules.airag.llm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.ai.factory.AiModelFactory;
import org.jeecg.ai.factory.AiModelOptions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.handler.AIChatHandler;
import org.jeecg.modules.airag.llm.handler.EmbeddingHandler;
import org.jeecg.modules.airag.llm.service.IAiragModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;

/**
 * @Description: AiRagModel configuration
 * @Author: jeecg-boot
 * @Date: 2025-02-14
 * @Version: V1.0
 */
@Tag(name = "AiRagModel configuration")
@RestController
@RequestMapping("/airag/airagModel")
@Slf4j
public class AiragModelController extends JeecgController<AiragModel, IAiragModelService> {
    @Autowired
    private IAiragModelService airagModelService;

    @Autowired
    AIChatHandler aiChatHandler;

    /**
     * Paginated list query
     *
     * @param airagModel
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<IPage<AiragModel>> queryPageList(AiragModel airagModel, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<AiragModel> queryWrapper = QueryGenerator.initQueryWrapper(airagModel, req.getParameterMap());
        Page<AiragModel> page = new Page<AiragModel>(pageNo, pageSize);
        IPage<AiragModel> pageList = airagModelService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add to
     *
     * @param airagModel
     * @return
     */
    @PostMapping(value = "/add")
    @RequiresPermissions("airag:model:add")
    public Result<String> add(@RequestBody AiragModel airagModel) {
        // verify Model name/Model type/base model
        AssertUtils.assertNotEmpty("Model name不能为空", airagModel.getName());
        AssertUtils.assertNotEmpty("Model type不能为空", airagModel.getModelType());
        AssertUtils.assertNotEmpty("base model不能为空", airagModel.getModelName());
        // Not activated by default
        if(oConvertUtils.isObjectEmpty(airagModel.getActivateFlag())){
            airagModel.setActivateFlag(0);
        }
        airagModel.setActivateFlag(0);
        airagModelService.save(airagModel);
        return Result.OK("Add to成功！");
    }

    /**
     * edit
     *
     * @param airagModel
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("airag:model:edit")
    public Result<String> edit(@RequestBody AiragModel airagModel) {
        airagModelService.updateById(airagModel);
        return Result.OK("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("airag:model:delete")
    public Result<String> delete(HttpServletRequest request, @RequestParam(name = "id", required = true) String id) {
        //update-begin---author:chenrui ---date:20250606  for：[issues/8337]aboutaiData permission issues for worklists #8337------------
        //in the case ofsaasin isolation，Determine the current tenantidIs it under the current tenant?
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            AiragModel model = airagModelService.getById(id);
            //Get current tenant
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (null == model || !model.getTenantId().equals(currentTenantId)) {
                return Result.error("deleteAIModel failed，不能delete其他租户的AIModel！");
            }
        }
        //update-end---author:chenrui ---date:20250606  for：[issues/8337]aboutaiData permission issues for worklists #8337------------
        airagModelService.removeById(id);
        return Result.OK("delete成功!");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<AiragModel> queryById(@RequestParam(name = "id", required = true) String id) {
        AiragModel airagModel = airagModelService.getById(id);
        if (airagModel == null) {
            return Result.error("No corresponding data found");
        }
        return Result.OK(airagModel);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param airagModel
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AiragModel airagModel) {
        return super.exportXls(request, airagModel, AiragModel.class, "AiRagModel configuration");
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
        return super.importExcel(request, response, AiragModel.class);
    }

    @PostMapping(value = "/test")
    public Result<?> test(@RequestBody AiragModel airagModel) {
        // verify Model name/Model type/base model
        AssertUtils.assertNotEmpty("Model name不能为空", airagModel.getName());
        AssertUtils.assertNotEmpty("Model type不能为空", airagModel.getModelType());
        AssertUtils.assertNotEmpty("base model不能为空", airagModel.getModelName());
        try {
            if(LLMConsts.MODEL_TYPE_LLM.equals(airagModel.getModelType())){
                aiChatHandler.completions(airagModel, Collections.singletonList(UserMessage.from("To test whether it can be successfully called, simply return success")), null);
            }else{
                AiModelOptions aiModelOptions = EmbeddingHandler.buildModelOptions(airagModel);
                EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(aiModelOptions);
                embeddingModel.embed("test text");
            }
        }catch (Exception e){
            log.error("测试Model连接失败", e);
            return Result.error("测试Model连接失败：" + e.getMessage());
        }
        // Test successfully activate data
        airagModel.setActivateFlag(1);
        airagModelService.updateById(airagModel);
        return Result.OK("");
    }

}
