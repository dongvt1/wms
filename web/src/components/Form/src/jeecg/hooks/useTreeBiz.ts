import type { Ref } from 'vue';
import { inject, reactive, ref, computed, unref, watch, nextTick } from 'vue';
import { TreeActionType } from '/@/components/Tree';
import { listToTree } from '/@/utils/common/compUtils';
import { isEqual } from 'lodash-es';
import { defHttp } from "@/utils/http/axios";
import { queryAllParentId } from "/@/api/common/api";

export function useTreeBiz(treeRef, getList, props, realProps, emit) {
  //Receive drop down box options
  const selectOptions = inject('selectOptions', ref<Array<object>>([]));
  //Receive selected value
  const selectValues = <object>inject('selectValues', reactive({}));
  // Whether the echo is loading
  const loadingEcho = inject<Ref<boolean>>('loadingEcho', ref(false));
  //Dataset
  const treeData = ref<Array<object>>([]);
  //Selected value
  const checkedKeys = ref<Array<string | number>>([]);
  //Selected row record
  const selectRows = ref<Array<object>>([]);
  //Whether to open pop-up mode
  const openModal = ref(false);
  // Whether to enable parent-child association，If multiple selection is not possible，Always cancel the parent-child association
  const getCheckStrictly = computed(() => (realProps.multiple ? props.checkStrictly : true));
  // Whether it is the first time to load the echo，Only first load，will be displayed loading
  let isFirstLoadEcho = true;
  let prevSelectValues = [];
  // The parent node that needs to be expandedIDlist
  const expandedKeys = ref<Array<string | number>>([]);
  // Whether to enable automatic expansion（can passpropscontrol）
  const enableAutoExpand = props.enableAutoExpand !== false;
  /**
   * monitorselectValueschange
   */
  watch(
    selectValues,
    ({ value: values }: Recordable) => {
      if(!values){
        return;
      }
      // update-begin--author:liaozhiyang---date:20250604---for：【issues/8232】Code settingsJSelectDeptComponent values ​​are not translated
      if (values.length > 0) {
        // Prevent multiple requests
        if (isEqual(values, prevSelectValues)) return;
        prevSelectValues = values;
        loadingEcho.value = isFirstLoadEcho;
        isFirstLoadEcho = false;
        onLoadData(null, values.join(',')).finally(() => {
          loadingEcho.value = false;
        });
        // update-end--author:liaozhiyang---date:20250604---for：【issues/8232】Code settingsJSelectDeptComponent values ​​are not translated
      }
    },
    { immediate: true }
  );

  /**
   * Get tree instance
   */
  function getTree() {
    const tree = unref(treeRef);
    if (!tree) {
      //throw new Error('tree is null!');
      return null;
    }
    return tree;
  }

  /**
   * 获取The parent node that needs to be expandedID
   */
  async function getParentIdsToExpand(selectedIds) {
    if (!selectedIds || selectedIds.length === 0) return [];
    
    try {
      const result = await queryAllParentId({ 
        departId: selectedIds.join(','),
        orgCode: props.params?.orgCode 
      });
      
      if (result) {
        const allParentIds = [];
        // deal with Map or Object structure
        const valuesToProcess = result instanceof Map 
          ? Array.from(result.values()) 
          : Object.values(result);
        
        // Traverse the parent nodes of all selected nodes
        valuesToProcess.forEach((nodeData: any) => {
          if (nodeData && nodeData.parentIds && Array.isArray(nodeData.parentIds)) {
            // Add parent nodeID（Does not include the selected node itself）
            const parentIds = nodeData.parentIds.filter(id => !selectedIds.includes(id));
            allParentIds.push(...parentIds);
          }
        });
        
        return [...new Set(allParentIds)]; // Remove duplicates
      }
      return [];
    } catch (error) {
      console.warn('Get parent nodeIDfail:', error);
      return [];
    }
  }

  /**
   * Set tree expansion level
   */
  function expandTree() {
    nextTick(() => {
      if (props.defaultExpandLevel && props.defaultExpandLevel > 0) {
        getTree().filterByLevel(props.defaultExpandLevel);
      }
      //设置list默认选中
      checkedKeys.value = selectValues['value'];
      
      // 如果有The parent node that needs to be expanded，then expand them
      if (expandedKeys.value.length > 0) {
        getTree().setExpandedKeys(expandedKeys.value);
      }
    }).then(() => {
      // Again make sure to expand，Because the tree may not be fully rendered yet
      if (expandedKeys.value.length > 0) {
        setTimeout(() => {
          getTree().setExpandedKeys(expandedKeys.value);
        }, 100);
      }
    });
  }

  /**
   * Tree node selection
   */
  function onSelect(keys, info) {
    if (props.checkable == false) {
      checkedKeys.value = props.checkStrictly ? keys.checked : keys;
      const { selectedNodes } = info;
      let rows = <any[]>[];
      selectedNodes.forEach((item) => {
        rows.push(item);
      });
      selectRows.value = rows;
    }
  }

  /**
   * Tree node selection
   */
  function onCheck(keys, info) {
    if (props.checkable == true) {
      // If multiple selections are not possible，Just keep the last selected one
      if (!realProps.multiple) {
        if (info.checked) {
          //update-begin-author:taoyan date:20220408 for: In single selection mode，set uprowKey，Unable to select data-
          checkedKeys.value = [info.node.eventKey];
          let rowKey = props.rowKey;
          let temp = info.checkedNodes.find((n) => n[rowKey] === info.node.eventKey);
          selectRows.value = [temp];
          //update-end-author:taoyan date:20220408 for: In single selection mode，set uprowKey，Unable to select data-
        } else {
          checkedKeys.value = [];
          selectRows.value = [];
        }
        return;
      }
      checkedKeys.value = props.checkStrictly ? keys.checked : keys;
      const { checkedNodes } = info;
      let rows = <any[]>[];
      checkedNodes.forEach((item) => {
        rows.push(item);
      });
      selectRows.value = rows;
    }
  }

  /**
   * Check all
   */
  async function checkALL(checkAll) {
    getTree().checkAll(checkAll);
    //update-begin---author:wangshuai ---date:20230403  for：【issues/394】It will not take effect if all operations in the department tree are checked./【issues/4646】After all departments are checked，Click the confirm button，Department information is lost------------
    await nextTick();
    checkedKeys.value = getTree().getCheckedKeys();
    if(checkAll){
      getTreeRow();
    }else{
      selectRows.value = [];
    }
    //update-end---author:wangshuai ---date:20230403  for：【issues/394】It will not take effect if all operations in the department tree are checked./【issues/4646】After all departments are checked，Click the confirm button，Department information is lost------------
  }

  /**
   * 获取数list
   * @param res
   */
  function getTreeRow() {
    let ids = "";
    if(unref(checkedKeys).length>0){
      ids = checkedKeys.value.join(",");
    }
    getList({ids:ids}).then((res) =>{
      selectRows.value = res;
    })
  }

  /**
   * Expand all
   */
  function expandAll(expandAll) {
    getTree().expandAll(expandAll);
  }

  /**
   * Load tree data
   */
  async function onLoadData(treeNode, ids) {
    let params = {};
    let startPid = '';
    if (treeNode) {
      startPid = treeNode.eventKey;
      //update-begin---author:wangshuai ---date:20220407  for：rowkeyNot set toid，syncWhen turning on asynchronous，Click on the upper and lower levels to not display them------------
      params['pid'] = treeNode.value;
      //update-end---author:wangshuai ---date:20220407  for：rowkeyNot set toid，syncWhen turning on asynchronous，Click on the upper and lower levels to not display them------------
    }
    if (ids) {
      startPid = '';
      params['ids'] = ids;
    }

    if(props.params?.departIds){
      params['departIds'] = props.params.departIds;
    }
    let record = await getList(params);
    let optionData = record;
    //Only show company information（company+子company）
    if(props.onlyShowCompany){
      record = getCompanyData(record)
    }
    //Whether to select only department positions
    if (props.izOnlySelectDepartPost) {
      setCompanyDepartCheckable(record);
    }
    if (!props.serverTreeData) {
      //前端deal withdata为treestructure
      record = listToTree(record, props, startPid);
      if (record.length == 0 && treeNode) {
        checkHasChild(startPid, treeData.value);
      }
    }

    if (openModal.value == true) {
      //Load all data in pop-up mode
      if (!treeNode) {
        treeData.value = record;
      } else {
        return new Promise((resolve: (value?: unknown) => void) => {
          if (!treeNode.children) {
            resolve();
            return;
          }
          const asyncTreeAction: TreeActionType | null = unref(treeRef);
          if (asyncTreeAction) {
            asyncTreeAction.updateNodeByKey(treeNode.eventKey, { children: record });
            asyncTreeAction.setExpandedKeys([treeNode.eventKey, ...asyncTreeAction.getExpandedKeys()]);
          }
          resolve();
          return;
        });
      }
      expandTree();
    } else {
      const options = <any[]>[];
      optionData.forEach((item) => {
        //update-begin-author:taoyan date:2022-7-4 for: issues/I5F3P4 onlineEdit after configuration department selection，Viewing the data should show the department name，Not a department code
        options.push({ label: item[props.labelKey], value: item[props.rowKey] });
        //update-end-author:taoyan date:2022-7-4 for: issues/I5F3P4 onlineEdit after configuration department selection，Viewing the data should show the department name，Not a department code
      });
      selectOptions.value = options;
    }
  }

  /**
   * 获取到company/子companydata
   * @param record
   */
  function getCompanyData(record){
    const companyData = record.filter(item=>item.orgCategory && ['1','4'].includes(item.orgCategory));
    return companyData
  }
  /**
   * Detect whether there are subordinate nodes during asynchronous loading
   * @param pid parent node
   * @param treeArray  treedata
   */
  function checkHasChild(pid, treeArray) {
    if (treeArray && treeArray.length > 0) {
      for (let item of treeArray) {
        if (item.key == pid) {
          if (!item.child) {
            item.isLeaf = true;
          }
          break;
        } else {
          checkHasChild(pid, item.children);
        }
      }
    }
  }

  /**
   * 获取已选择data
   */
  function getSelectTreeData(success) {
    const options = <any[]>[];
    const values = <any[]>[];
    selectRows.value.forEach((item) => {
      options.push({ label: item[props.labelKey], value: item[props.rowKey] });
    });
    checkedKeys.value.forEach((item) => {
      values.push(item);
    });
    selectOptions.value = options;
    success && success(options, values);
  }

  /**
   * Pop-up box shows hidden trigger event
   */
  async function visibleChange(visible) {
    if (visible) {
      //弹出框打开时加载全部data
      openModal.value = true;
      await onLoadData(null, null);
      
      // 在data加载完成后，If there is a selected value and auto-expand is enabled，则展开parent node
      if (enableAutoExpand && selectValues.value && selectValues.value.length > 0) {
        try {
          const selectedIds = selectValues.value;
          const parentIds = await getParentIdsToExpand(selectedIds);
          
          if (parentIds.length > 0) {
            expandedKeys.value = parentIds;
            
            // Delayed expansion，Make sure the tree has been rendered
            nextTick(() => {
              try {
                const tree = getTree();
                if (tree) {
                  tree.setExpandedKeys(parentIds);

                  // Again make sure to expand
                  setTimeout(() => {
                    try {
                      const tree = getTree();
                      if (tree) {
                        tree.setExpandedKeys(parentIds);
                        console.log('parent node已展开:', parentIds);
                        // Ensure expansion for the third time，Use a longer delay
                        setTimeout(() => {
                          try {
                            const tree = getTree();
                            if (tree) {
                              tree.setExpandedKeys(parentIds);
                            }
                          } catch (error) {
                            console.warn('展开parent nodefail:', error);
                          }
                        }, 500);
                      }
                    } catch (error) {
                      console.warn('展开parent nodefail:', error);
                    }
                  }, 200);
                }
              } catch (error) {
                console.warn('展开parent nodefail:', error);
              }
            });
            
          }
        } catch (error) {
          console.warn('Get parent nodeIDfail:', error);
        }
      }
    } else {
      openModal.value = false;
      // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-414】Department sets default value，Query reset becomes empty(synchronousJSelectUserModification of components)
      emit?.('close');
      // update-end--author:liaozhiyang---date:20240527---for：【TV360X-414】Department sets default value，Query reset becomes empty(synchronousJSelectUserModification of components)
    }
  }

  /**
   * 设置company部门复选框显示
   * @param record
   */
  function setCompanyDepartCheckable(record) {
    if (record && record.length > 0) {
      for (const item of record) {
        if (item.orgCategory !== '3') {
          item.checkable = false;
          item.selectable = false;
        } else {
          item.checkable = true;
          item.selectable = true;
        }
        if (item.isLeaf) {
          setCompanyDepartCheckable(item.children);
        }
      }
    }
  }

  /**
   * Job search
   *
   * @param value
   */
  async function onSearch(value) {
    if(value){
      let result = await defHttp.get({ url: "/sys/sysDepart/searchBy", params: { keyWord: value, orgCategory: "3",...props.params } });
      if (Array.isArray(result)) {
        treeData.value = result;
      } else {
        treeData.value = [];
      }
    } else {
      treeData.value = [];
      await onLoadData(null, null)
    }
  }

  return [
    {
      visibleChange,
      selectOptions,
      selectValues,
      onLoadData,
      onCheck,
      onSelect,
      checkALL,
      expandAll,
      checkedKeys,
      selectRows,
      treeData,
      getCheckStrictly,
      getSelectTreeData,
      onSearch,
      expandedKeys,
    },
  ];
}
