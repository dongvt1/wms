<template>
  <div class="p-4">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> New menu</a-button>
        <a-button type="primary" preIcon="ic:round-expand" @click="expandAll">Expand all</a-button>
        <a-button type="primary" preIcon="ic:round-compress" @click="collapseAll">Collapse all</a-button>

        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon icon="ant-design:down-outlined" />
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <MenuDrawer @register="registerDrawer" @success="handleSuccess" :showFooter="showFooter" />
    <DataRuleList @register="registerDrawer1" />
  </div>
</template>
<script lang="ts" name="system-menu" setup>
  import { nextTick, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useDrawer } from '/@/components/Drawer';
  import MenuDrawer from './MenuDrawer.vue';
  import DataRuleList from './DataRuleList.vue';
  import { columns,searchFormSchema } from './menu.data';
  import { list, deleteMenu, batchDeleteMenu } from './menu.api';
  import { useDefIndexStore } from "@/store/modules/defIndex";
  import { useI18n } from "/@/hooks/web/useI18n";

  const checkedKeys = ref<Array<string | number>>([]);
  const showFooter = ref(true);
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerDrawer1, { openDrawer: openDataRule }] = useDrawer();
  const { t } = useI18n();

  // Custom menu name column rendering
  columns[0].customRender = function ({ text, record }) {
    // date-begin--author:liaozhiyang---date:20250716---for：【issues/8317】The default homepage menu name adapts to internationalization error reporting
    let displayText = text;
    // update-begin--author:liaozhiyang---date:20240306---for：【QQYUN-8379】Menu management page menu internationalization
    // Deal with internationalization first，Avoid affecting internationalization checks after adding default homepage markup
    if (displayText && displayText.includes("t('") && t) {
      try {
        displayText = new Function('t', `return ${displayText}`)(t);
      } catch (error) {
        console.warn('Internationalization processing failed:', error);
        // 如果Internationalization processing failed，Use original text
        displayText = text;
      }
    }
    // update-end--author:liaozhiyang---date:20240306---for：【QQYUN-8379】Menu management page menu internationalization
    // After the internationalization process is completed，Add the default home page mark
    const isDefIndex = checkDefIndex(record);
    if (isDefIndex) {
      displayText += `（${t('routes.basic.defaultHomePage')}）`;
    }
    return displayText;
    // date-end--author:liaozhiyang---date:20250716---for：【issues/8317】The default homepage menu name adapts to internationalization error reporting
  };

  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      title: 'Menu list',
      api: list,
      columns: columns,
      size: 'small',
      pagination: false,
      isTreeTable: true,
      striped: true,
      useSearchForm: true,
      showTableSetting: true,
      bordered: true,
      showIndexColumn: false,
      tableSetting: { fullScreen: true },
      formConfig: {
        // update-begin--author:liaozhiyang---date:20230803---for：【QQYUN-5873】Query arealablelDefault left
        labelWidth: 74,
        rowProps: { gutter: 24 },
        // update-end--author:liaozhiyang---date:20230803---for：【QQYUN-5873】Query arealablelDefault left
        schemas: searchFormSchema,
        autoAdvancedCol: 4,
        baseColProps: { xs: 24, sm: 12, md: 6, lg: 6, xl: 6, xxl: 6 },
        actionColOptions: { xs: 24, sm: 12, md: 6, lg: 6, xl: 6, xxl: 6 },
      },
      actionColumn: {
        width: 120,
      },
    },
  });
  //registertabledata
  const [registerTable, { reload, expandAll, collapseAll }] = tableContext;

  /**
   * Select column configuration
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 30,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  /**
   * Select event
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * New
   */
  function handleCreate() {
    showFooter.value = true;
    openDrawer(true, {
      isUpdate: false,
    });
  }

  /**
   * edit
   */
  function handleEdit(record) {
    showFooter.value = true;
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Details
   */
  function handleDetail(record) {
    showFooter.value = false;
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Add subordinate
   */
  function handleAddSub(record) {
    openDrawer(true, {
      record: { parentId: record.id, menuType: 1 },
      isUpdate: false,
    });
  }
  /**
   * data权限弹窗
   */
  function handleDataRule(record) {
    openDataRule(true, { id: record.id });
  }

  /**
   * delete
   */
  async function handleDelete(record) {
    await deleteMenu({ id: record.id }, reload);
  }
  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDeleteMenu({ ids: checkedKeys.value }, () => {
      // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1662】Menu management、定时任务批量delete清空选中
      reload();
      checkedKeys.value = [];
      // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1662】Menu management、定时任务批量delete清空选中
    });
  }
  /**
   * successful callback
   */
  function handleSuccess() {
    reload();
    reloadDefIndex();
  }

  function onFetchSuccess() {
    // The demonstration expands all table items by default
    nextTick(expandAll);
  }

  // --------------- begin Default home page configuration ------------

  const defIndexStore = useDefIndexStore()

  // Set default homepage
  async function handleSetDefIndex(record: Recordable) {
    defIndexStore.update(record.url, record.component, record.route)
  }

  /**
   * Check if it is the default home page
   * @param record
   */
  function checkDefIndex(record: Recordable) {
    return defIndexStore.check(record.url)
  }

  // 重新加载Default home page configuration
  function reloadDefIndex() {
    try {
      defIndexStore.query();
    } catch (e) {
      console.error(e)
    }
  }

  reloadDefIndex()

  // --------------- end Default home page configuration ------------

  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  /**
   * 下拉Action bar
   */
  function getDropDownAction(record) {
    return [
      // {
      //   label: 'Details',
      //   onClick: handleDetail.bind(null, record),
      // },
      {
        label: 'Add subordinate',
        onClick: handleAddSub.bind(null, record),
      },
      {
        label: 'data规则',
        onClick: handleDataRule.bind(null, record),
      },
      {
        label: 'Set as default homepage',
        onClick: handleSetDefIndex.bind(null, record),
        ifShow: () => !record.internalOrExternal && record.component && !checkDefIndex(record),
      },
      {
        label: 'delete',
        color: 'error',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
