<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" type="flex" :gutter="10" style="max-height: 800px">
    <a-col :xl="6" :lg="24" :md="24" style="margin-bottom: 10px">
      <DepartLeftTree ref="leftTree" @select="onTreeSelect" />
    </a-col>
    <a-col :xl="18" :lg="24" :md="24" style="margin-bottom: 10px">
      <div style="height: 100%;" class="address-book">
        <!--Reference table-->
        <BasicTable @register="registerTable">
          <template #post="{ text }">
            {{
              (text || '')
                .split(',')
                .map((t) => (positionInfo[t] ? positionInfo[t] : t))
                .join(',')
            }}
          </template>
        </BasicTable>
      </div>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
  import { provide, ref, unref } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import DepartLeftTree from './components/DepartLeftTree.vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema } from './address.data';
  import { list, positionList } from './address.api';
  import { getCacheByDynKey } from "@/utils/auth";
  import { JEECG_CHAT_UID } from "@/enums/cacheEnum";

  const { prefixCls } = useDesign('address-list');
  provide('prefixCls', prefixCls);

  // Define a subcomponentrefvariable
  const leftTree = ref();

  // Currently selected departmentcode
  const orgCode = ref('');
  const positionInfo = ref({});

  // List page public parameters、method
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      columns,
      //update-begin---author:wangshuai ---date:20220629  for：[VUEN-1485]Enter system management--After the address book page，Web page command line error------------
      rowKey: 'id',
      //update-end---author:wangshuai ---date:20220629  for：[VUEN-1485]Enter system management--After the address book page，Web page command line error--------------
      showIndexColumn: true,
      formConfig: {
        schemas: searchFormSchema,
      },
      canResize: false,
      showTableSetting: false,
      // Process parameters before requesting
      beforeFetch(params) {
        params.orgCode = orgCode.value;
      },
    },
  });
  //registertabledata
  const [registerTable, { reload }] = tableContext;

  // Triggered after the left tree is selected
  function onTreeSelect(data) {
    orgCode.value = data.orgCode;
    reload();
  }

  // Query job information
  async function queryPositionInfo() {
    const result = await positionList({ pageSize: 99999 });
    if (result) {
      let obj = {};
      result.records.forEach((position) => {
        obj[position['id']] = position['name'];
      });
      positionInfo.value = obj;
    }
  }
  queryPositionInfo();
</script>

<style lang="less">
  @import './index.less';
</style>
