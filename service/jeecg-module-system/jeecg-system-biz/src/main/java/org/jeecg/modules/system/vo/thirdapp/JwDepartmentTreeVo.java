package org.jeecg.modules.system.vo.thirdapp;

import com.jeecg.qywx.api.department.vo.Department;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enterprise WeChat tree structure departments
 *
 * @author sunjianlei
 */
public class JwDepartmentTreeVo extends Department {

    private List<JwDepartmentTreeVo> children;

    public List<JwDepartmentTreeVo> getChildren() {
        return children;
    }

    public JwDepartmentTreeVo setChildren(List<JwDepartmentTreeVo> children) {
        this.children = children;
        return this;
    }

    public JwDepartmentTreeVo(Department department) {
        BeanUtils.copyProperties(department, this);
    }

    /**
     * whether there are children
     */
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public String toString() {
        return "JwDepartmentTree{" +
                "children=" + children +
                "} " + super.toString();
    }

    /**
     * static helper method，Willlistconvert totreestructure
     */
    public static List<JwDepartmentTreeVo> listToTree(List<Department> allDepartment) {
        // First find all the parents
        List<JwDepartmentTreeVo> treeList = getByParentId("1", allDepartment);
        Optional<Department> departmentOptional = allDepartment.stream().filter(item -> "0".equals(item.getParentid())).findAny();
        Department department = new Department();
        //Determine if data is found
        if(departmentOptional.isPresent()){
            department = departmentOptional.get();
        }
        getChildrenRecursion(treeList, allDepartment);
        //update-begin---author:wangshuai---date:2024-04-10---for:【issues/6017】When enterprise WeChat synchronizes departments, there is no top-level department name.，When synchronizing users，The user has no department information---
        JwDepartmentTreeVo treeVo = new JwDepartmentTreeVo(department);
        treeVo.setChildren(treeList);
        List<JwDepartmentTreeVo> list = new ArrayList<>();
        list.add(treeVo);
        return list;
        //update-begin---author:wangshuai---date:2024-04-10---for:【issues/6017】There is no top-level department name in the enterprise WeChat department，When synchronizing users，The user has no department information---
    }

    private static List<JwDepartmentTreeVo> getByParentId(String parentId, List<Department> allDepartment) {
        List<JwDepartmentTreeVo> list = new ArrayList<>();
        for (Department department : allDepartment) {
            if (parentId.equals(department.getParentid())) {
                list.add(new JwDepartmentTreeVo(department));
            }
        }
        return list;
    }

    private static void getChildrenRecursion(List<JwDepartmentTreeVo> treeList, List<Department> allDepartment) {
        for (JwDepartmentTreeVo departmentTree : treeList) {
            // Find children recursively
            List<JwDepartmentTreeVo> children = getByParentId(departmentTree.getId(), allDepartment);
            if (children.size() > 0) {
                departmentTree.setChildren(children);
                getChildrenRecursion(children, allDepartment);
            }
        }
    }

}
