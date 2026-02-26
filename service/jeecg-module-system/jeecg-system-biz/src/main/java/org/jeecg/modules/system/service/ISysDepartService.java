package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.SysPositionSelectTreeVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * Department table Service implementation class
 * <p>
 * 
 * @Author:Steve
 * @Since：   2019-01-22
 */
public interface ISysDepartService extends IService<SysDepart>{

    /**
     * Query my department information,and displayed by nodes.
     * @param departIds departmentid
     * @return
     */
    List<SysDepartTreeModel> queryMyDeptTreeList(String departIds);

    /**
     * Query所有departmentinformation,and displayed by nodes.
     * @return
     */
    List<SysDepartTreeModel> queryTreeList();


    /**
     * Query所有departmentinformation,and displayed by nodes.
     * @param ids 多个departmentid
     * @return
     */
    List<SysDepartTreeModel> queryTreeList(String ids);

    /**
     * Query所有departmentDepartIdinformation,and displayed by nodes.
     * @return
     */
    public List<DepartIdModel> queryDepartIdTreeList();

    /**
     * 保存departmentdata
     * @param sysDepart
     * @param username username
     */
    void saveDepartData(SysDepart sysDepart,String username);

    /**
     * renewdepartdata
     * @param sysDepart
     * @param username username
     * @return
     */
    Boolean updateDepartDataById(SysDepart sysDepart,String username);
    
    /**
     * deletedepartdata
     * @param id
     * @return
     */
	/* boolean removeDepartDataById(String id); */
    
    /**
     * according to关键字搜索相关的departmentdata
     *
     * @param keyWord
     * @param myDeptSearch
     * @param departIds    多个departmentid
     * @param orgCategory
     * @param depIds
     * @return
     */
    List<SysDepartTreeModel> searchByKeyWord(String keyWord, String myDeptSearch, String departIds, String orgCategory, String depIds);
    
    /**
     * according todepartmentiddelete并delete其可能存在的子级department
     * @param id
     * @return
     */
    boolean delete(String id);
    
    /**
     * QuerySysDepartgather
     * @param userId
     * @return
     */
	public List<SysDepart> queryUserDeparts(String userId);

    /**
     * according tousernameQuerydepartment
     *
     * @param username
     * @return
     */
    List<SysDepart> queryDepartsByUsername(String username);
    
    /**
     * According to userIDQuerydepartment
     *
     * @param userId
     * @return
     */
    List<String> queryDepartsByUserId(String userId);

	 /**
     * according todepartmentid批量delete并delete其可能存在的子级department
     * @param ids 多个departmentid
     * @return
     */
	void deleteBatchWithChildren(List<String> ids);

    /**
     *  according todepartmentIdQuery,当前和下级所有departmentIDS
     * @param departId
     * @return
     */
    List<String> getSubDepIdsByDepId(String departId);

    /**
     * 获取我的department下级所有departmentIDS
     * @param departIds 多个departmentid
     * @return
     */
    List<String> getMySubDepIdsByDepId(String departIds);
    /**
     * according to关键字获取departmentinformation（Address book）
     * @param keyWord search term
     * @return
     */
    List<SysDepartTreeModel> queryTreeByKeyWord(String keyWord);
    /**
     * 获取我的department下级所有department
     * @param parentId fatherid
     * @param ids 多个departmentid
     * @param primaryKey primary key field（idororgCode）
     * @return
     */
    List<SysDepartTreeModel> queryTreeListByPid(String parentId,String ids, String primaryKey);

    /**
     * 获取某个department的所有father级department的ID
     *
     * @param departId according todepartIdcheck
     * @return JSONObject
     */
    JSONObject queryAllParentIdByDepartId(String departId);

    /**
     * 获取某个department的所有father级department的ID
     *
     * @param orgCode according toorgCodecheck
     * @return JSONObject
     */
    JSONObject queryAllParentIdByOrgCode(String orgCode);
    /**
     * 获取公司information
     * @param orgCode department编码
     * @return
     */
    SysDepart queryCompByOrgCode(String orgCode);
    /**
     * 获取下级department
     * @param pid
     * @return
     */
    List<SysDepart> queryDeptByPid(String pid);

    /**
     * 获取我的department已加入的公司
     * @return
     */
    List<SysDepart> getMyDepartList();

    /**
     * deletedepartment
     * @param id
     */
    void deleteDepart(String id);

    /**
     * Address book通过租户idQuerydepartmentdata
     * @param parentId
     * @param tenantId
     * @param departName
     * @return
     */
    List<SysDepartTreeModel> queryBookDepTreeSync(String parentId, Integer tenantId, String departName);

    /**
     * according toidQuerydepartmentinformation
     * @param parentId
     * @return
     */
    SysDepart getDepartById(String parentId);

    /**
     * according toidQuerydepartmentinformation
     * @param parentId
     * @return
     */
    IPage<SysDepart> getMaxCodeDepart(Page<SysDepart> page, String parentId);

    /**
     * renew叶子节点
     * @param id
     * @param izLeaf
     */
    void updateIzLeaf(String id, Integer izLeaf);

    /**
     * 获取导出department的data
     * @param tenantId
     * @return
     */
    List<ExportDepartVo> getExcelDepart(int tenantId);

    void importExcel(List<ExportDepartVo> listSysDeparts, List<String> errorMessageList);

    /**
     * according to租户id导出department
     * @param tenantId
     * @param idList
     * @return
     */
    List<SysDepartExportVo> getExportDepart(Integer tenantId, List<String> idList);

    /**
     * 导出系统departmentexcel
     * @param listSysDeparts
     * @param errorMessageList
     */
    void importSysDepart(List<SysDepartExportVo> listSysDeparts, List<String> errorMessageList);

    /**
     * according todepartmentidand rankid获取岗位information
     *
     * @param parentId
     * @param departId
     * @param positionId
     */
    List<SysPositionSelectTreeVo> getPositionByDepartId(String parentId, String departId, String positionId);

    /**
     * Get rank relationship
     * @param departId
     * @return
     */
    List<SysPositionSelectTreeVo> getRankRelation(String departId);

    /**
     * 异步Querydepartment和岗位接口
     *
     * @param parentId
     * @param ids
     * @param primaryKey
     * @param departIds
     * @return
     */
    List<SysDepartTreeModel> queryDepartAndPostTreeSync(String parentId, String ids, String primaryKey, String departIds,String orgName);

    /**
     * according todepartmentcode获取当前和上级department名称
     *
     * @param orgCode
     * @param depId
     * @return
     */
    String getDepartPathNameByOrgCode(String orgCode, String depId);
}
