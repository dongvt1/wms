<template>
  <div>
    <!--Reference table-->
    <BasicTable
      @register="registerTable"
      :rowSelection="rowSelection"
      :expandedRowKeys="expandedRowKeys"
      @expand="handleExpand"
      @fetch-success="onFetchSuccess"
    >
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> New</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!--Dictionary popup-->
    <CategoryModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="system-category" setup>
  //tsgrammar
  import { ref, computed, unref, toRaw, nextTick } from 'vue';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { useDrawer } from '/src/components/Drawer';
  import CategoryModal from './components/CategoryModal.vue';
  import { useModal } from '/src/components/Modal';
  import { useMethods } from '/src/hooks/system/useMethods';
  import { columns, searchFormSchema } from './category.data';
  import { list, deleteCategory, batchDeleteCategory, getExportUrl, getImportUrl, getChildList, getChildListBatch } from './category.api';
  import { useListPage } from '/@/hooks/system/useListPage';

  const expandedRowKeys = ref([]);
  const { handleExportXls, handleImportXls } = useMethods();
  //dictionarymodel
  const [registerModal, { openModal }] = useModal();
  // List page public parameters、method
  const { prefixCls, onExportXls, onImportXls, tableContext } = useListPage({
    designScope: 'category-template',
    tableProps: {
      title: '分类dictionary',
      api: list,
      columns: columns,
      actionColumn: {
        width: 180,
      },
      formConfig: {
        schemas: searchFormSchema,
      },
      isTreeTable: true,
    },
    exportConfig: {
      name: '分类dictionary列表',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //registertabledata
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] =
    tableContext;

  /**
   * New事件
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Edit event
   */
  async function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Details
   */
  async function handleDetail(record) {
    openModal(true, {
      record,
      isUpdate: true,
      hideFooter: true,
    });
  }

  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteCategory({ id: record.id }, importSuccess);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    const ids = selectedRowKeys.value.filter((item) => !item.includes('loading'));
    await batchDeleteCategory({ ids: ids }, importSuccess);
  }
  /**
   * import
   */
  function importSuccess() {
    //update-begin---author:wangshuai ---date:20220530  for：[issues/54]树dictionary，Check，然后批量delete，System error------------
    (selectedRowKeys.value = []) && reload();
    //update-end---author:wangshuai ---date:20220530  for：[issues/54]树dictionary，Check，然后批量delete，System error--------------
  }
  /**
   * Add subordinate
   */
  function handleAddSub(record) {
    openModal(true, {
      record,
      isUpdate: false,
    });
  }
  /**
   * successful callback
   */
  async function handleSuccess({ isUpdate,isSubAdd, values, expandedArr }) {
    if (isUpdate) {
      //Edit callback
      updateTableDataRecord(values.id, values);
    } else {
      if (!values['pid']) {
        //New根节点
        reload();
      } else {
        //New子集
        //update-begin-author:liusq---date:20230411--for: [issue/4550]分类dictionarydata量过多会造成data查询时间过长--- 
        if(isSubAdd){
          await expandTreeNode(values.pid);
        //update-end-author:liusq---date:20230411--for: [issue/4550]分类dictionarydata量过多会造成data查询时间过长--- 
        }else{
          //update-begin-author:wangshuai---date:20240319--for: dictionary树delete之后其他节点出现loading---
          //expandedRowKeys.value = [];
          //update-end-author:wangshuai---date:20240319--for: dictionary树delete之后其他节点出现loading--- 
          for (let key of unref(expandedArr)) {
            await expandTreeNode(key);
          }
        }
      }
    }
  }

  /**
   * Callback after successful interface request
   */
  function onFetchSuccess(result) {
    getDataByResult(result.items) && loadDataByExpandedRows();
  }
  /**
   * 根据已展开的行查询data（用于保存后刷新时异步加载子级的data）
   */
  async function loadDataByExpandedRows() {
    if (unref(expandedRowKeys).length > 0) {
      const res = await getChildListBatch({ parentIds: unref(expandedRowKeys).join(',') });
      if (res.success && res.result.records.length > 0) {
        //已展开的data批量子节点
        let records = res.result.records;
        const listMap = new Map();
        for (let item of records) {
          let pid = item['pid'];
          if (unref(expandedRowKeys).includes(pid)) {
            let mapList = listMap.get(pid);
            if (mapList == null) {
              mapList = [];
            }
            mapList.push(item);
            listMap.set(pid, mapList);
          }
        }
        let childrenMap = listMap;
        let fn = (list) => {
          if (list) {
            list.forEach((data) => {
              if (unref(expandedRowKeys).includes(data.id)) {
                data.children = getDataByResult(childrenMap.get(data.id));
                fn(data.children);
              }
            });
          }
        };
        fn(getDataSource());
      }
    }
  }
  /**
   * 处理data集
   */
  function getDataByResult(result) {
    if (result && result.length > 0) {
      return result.map((item) => {
        //Determine whether a node with child nodes is marked
        if (item['hasChild'] == '1') {
          let loadChild = { id: item.id + '_loadChild', name: 'loading...', isLoading: true };
          item.children = [loadChild];
        }
        return item;
      });
    }
  }
  /**
   *Expand and merge tree nodes
   * */
  async function handleExpand(expanded, record) {
    // Determine whether it is expanded state，expanded state(expanded)and there exists a subset(children)and has not been loaded(isLoading)的就去查询子节点data
    if (expanded) {
      expandedRowKeys.value.push(record.id);
      if (record.children.length > 0 && !!record.children[0].isLoading) {
        let result = await getChildList({ pid: record.id });
        if (result && result.length > 0) {
          record.children = getDataByResult(result);
        } else {
          record.children = null;
          record.hasChild = '0';
        }
      }
    } else {
      let keyIndex = expandedRowKeys.value.indexOf(record.id);
      if (keyIndex >= 0) {
        expandedRowKeys.value.splice(keyIndex, 1);
      }
    }
  }
  /**
   *操作表格后处理Expand and merge tree nodes
   * */
  async function expandTreeNode(key) {
    let record:any = findTableDataRecord(key);
    //update-begin-author:liusq---date:20230411--for: [issue/4550]分类dictionarydata量过多会造成data查询时间过长，show“Interface request timeout,Please refresh the page and try again!”--- 
    if(!expandedRowKeys.value.includes(key)){
      expandedRowKeys.value.push(key);
    }
    //update-end-author:liusq---date:20230411--for: [issue/4550]分类dictionarydata量过多会造成data查询时间过长，show“Interface request timeout,Please refresh the page and try again!”--- 
    let result = await getChildList({ pid: key });
    if (result && result.length > 0) {
      record.children = getDataByResult(result);
    } else {
      record.children = null;
      record.hasChild = '0';
    }
    updateTableDataRecord(key, record);
  }
  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'delete',
        popConfirm: {
          title: '确定delete吗?',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'Add subordinate',
        onClick: handleAddSub.bind(null, { pid: record.id }),
      },
    ];
  }
</script>

<style scoped></style>
