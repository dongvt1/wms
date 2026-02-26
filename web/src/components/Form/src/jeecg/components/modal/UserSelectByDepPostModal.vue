<!--Select users by department-->
<template>
  <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="1200px" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
    <a-row :gutter="10">
      <a-col :md="7" :sm="24">
        <a-card :style="{ minHeight: '613px', overflow: 'auto' }">
          <a-input-search placeholder="Search by job title…" style="margin-bottom: 10px" @search="onSearch" @change="handelSearchChange"/>
          <!--Organizational structure-->
          <BasicTree
            ref="treeRef"
            :style="{ minWidth: '250px' }"
            selectable
            @select="onDepSelect"
            :load-data="loadChildrenTreeData"
            :treeData="departTree"
            :selectedKeys="selectedDepIds"
            :expandedKeys="expandedKeys"
            :clickRowToExpand="false"
            :key="reloadKey"
          >
            <template #title="{ orgCategory, title }">
              <TreeIcon :orgCategory="orgCategory" :title="title"></TreeIcon>
            </template>
          </BasicTree>
        </a-card>
      </a-col>
      <a-col :md="17" :sm="24">
        <a-card :style="{ minHeight: '613px', overflow: 'auto' }">
          <!--User list-->
          <BasicTable ref="tableRef" v-bind="getBindValue" :searchInfo="searchInfo" :api="getTableList" :rowSelection="rowSelection"></BasicTable>
        </a-card>
      </a-col>
    </a-row>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, unref, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTree } from '/@/components/Tree/index';
  import { queryDepartPostUserPageList as getTableListOrigin} from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import TreeIcon from '/@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue';
  import { queryDepartAndPostTreeSync as queryDepartTreeSyncOrigin } from '/@/views/system/depart/depart.api';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';
  import {defHttp} from "@/utils/http/axios";
  export default defineComponent({
    name: 'UserSelectByDepPostModal',
    components: {
      //Asynchronous loading is required hereBasicTable
      BasicModal,
      BasicTree,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
      TreeIcon,
    },
    props: {
      ...selectProps,
      //Select box title
      modalTitle: {
        type: String,
        default: 'Department user selection',
      },
    },
    emits: ['register', 'getSelectResult'],
    setup(props, { emit, refs }) {
      const tableRef = ref();
      const treeRef = ref();
      //Registration pop-up box
      const [register, { closeModal }] = useModalInner(async (data) => {
        await queryDepartTree();
      });
      const attrs = useAttrs();
      const departTree = ref<any>([]);
      const selectedDepIds = ref([]);
      const expandedKeys = ref([]);
      const searchInfo = {};
      //tree loadedkey
      const reloadKey = ref(Math.random());
      
      /**
       *Table configuration
       */
      const tableProps = {
        columns: [
          {
            title: 'User account',
            dataIndex: 'username',
            width: 180,
          },
          {
            title: 'User name',
            dataIndex: 'realname',
            width: 180,
          },
          {
            title: 'gender',
            dataIndex: 'sex_dictText',
            width: 80,
          },
          {
            title: 'phone number',
            dataIndex: 'phone',
          },
        ],
        useSearchForm: true,
        canResize: false,
        showIndexColumn: false,
        striped: true,
        bordered: true,
        size: 'small',
        formConfig: {
          //labelWidth: 200,
          baseColProps: {
            xs: 24,
            sm: 8,
            md: 6,
            lg: 8,
            xl: 6,
            xxl: 10,
          },
          //update-begin-author:liusq date:2023-10-30 for: [issues/5514]Component page display is misaligned
          actionColOptions: {
            xs: 24,
            sm: 12,
            md: 12,
            lg: 12,
            xl: 8,
            xxl: 8,
          },
          //update-end-author:liusq date:2023-10-30 for: [issues/5514]Component page display is misaligned
          schemas: [
            {
              label: 'account',
              field: 'username',
              component: 'Input',
            },
          ],
          resetFunc: customResetFunc,
        },
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), tableProps);
      const [{ rowSelection, visibleChange, indexColumnProps, getSelectResult, reset }] = useSelectBiz(getTableList, getBindValue);

      function getTableList(params) {
        params = parseParams(params);
        return getTableListOrigin({ ...params });
      }

      function queryDepartTreeSync(params) {
        params = parseParams(params);
        return queryDepartTreeSyncOrigin({ ...params });
      }

      /**
       * Parse parameters
       * @param params
       */
      function parseParams(params) {
        if (props?.params) {
          return {
            ...params,
            ...props.params,
          };
        }
        return params;
      }

      /**
       * Load tree data
       */
      function queryDepartTree() {
        queryDepartTreeSync({}).then((res) => {
          if (res) {
            departTree.value = res;
          }
        });
      }

      /**
       * Load sub-departments
       */
      async function loadChildrenTreeData(treeNode) {
        try {
          const result = await queryDepartTreeSync({
            pid: treeNode.eventKey,
          });
          const asyncTreeAction = unref(treeRef);
          if (asyncTreeAction) {
            asyncTreeAction.updateNodeByKey(treeNode.eventKey, { children: result });
            asyncTreeAction.setExpandedKeys([treeNode.eventKey, ...asyncTreeAction.getExpandedKeys()]);
          }
        } catch (e) {
          console.error(e);
        }
        return Promise.resolve();
      }
      /**
       * Click on tree node,Filter out the corresponding users
       */
      function onDepSelect(keys) {
        if (keys[0] != null) {
          if (unref(selectedDepIds)[0] !== keys[0]) {
            selectedDepIds.value = [keys[0]];
          }
          searchInfo['departId'] = unref(selectedDepIds).join(',');
          tableRef.value.reload();
        }
      }
      /**
       * Custom reset method
       * */
      async function customResetFunc() {
        console.log('Custom query');
        //Clear tree nodes
        selectedDepIds.value = [];
        //Clear query conditions
        searchInfo['departId'] = '';
        //Clear selection
        reset();
      }
      /**
       * Confirm selection
       */
      function handleOk() {
        getSelectResult((options, values) => {
          //Return options and selected values
          emit('getSelectResult', options, values);
          //Close pop-up window
          closeModal();
        });
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
            departTree.value = result;
          } else {
            departTree.value = [];
          }
        } else {
          departTree.value = [];
          await queryDepartTree();
        }
      }

      /**
       * Search value change event
       * @param value
       */
      function handelSearchChange(value) {
        if(!value.target.value){
          reloadKey.value = Math.random();
        }
      }

      return {
        //config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getBindValue,
        rowSelection,

        departTree,
        selectedDepIds,
        expandedKeys,
        treeRef,
        tableRef,
        getTableList,
        onDepSelect,
        loadChildrenTreeData,
        onSearch,
        handelSearchChange,
        reloadKey,
      };
    },
  });
</script>

<style scoped lang="less"></style>
