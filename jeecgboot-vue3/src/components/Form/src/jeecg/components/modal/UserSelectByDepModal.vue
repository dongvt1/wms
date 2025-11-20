<!--Select users by department-->
<template>
  <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="1200px" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
    <a-row :gutter="10">
      <a-col :md="7" :sm="24" style="height: 613px;overflow: auto ">
        <a-card :style="{ minHeight: '613px', overflow: 'auto' }">
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
  import { queryTreeList, getTableList as getTableListOrigin } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { queryDepartTreeSync as queryDepartTreeSyncOrigin } from '/@/views/system/depart/depart.api';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';
  import TreeIcon from '@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue';
  export default defineComponent({
    name: 'UserSelectByDepModal',
    components: {
      TreeIcon,
      //Asynchronous loading is required hereBasicTable
      BasicModal,
      BasicTree,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
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
      const departTree = ref([]);
      const selectedDepIds = ref([]);
      const expandedKeys = ref([]);
      const searchInfo = {};
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
            // width: 50,
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
            xxl: 8,
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
            {
              label: 'Name',
              field: 'realname',
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
        return getTableListOrigin({...params});
      }

      function queryDepartTreeSync(params) {
        params = parseParams(params);
        return queryDepartTreeSyncOrigin({...params});
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
          }
        }
        return params;
      }

      /**
       * Load tree data
       */
      function queryDepartTree() {
        queryDepartTreeSync().then((res) => {
          if (res) {
            departTree.value = res;
            // Expand parent node by default
            //expandedKeys.value = unref(departTree).map(item => item.id)
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
      };
    },
  });
</script>

<style scoped lang="less"></style>
