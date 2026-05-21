<!--Job selection box-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      width="1100px"
      wrapClassName="j-user-select-modal"
      @ok="handleOk"
      destroyOnClose
      @visible-change="visibleChange"
    >
      <a-row>
        <a-col :span="showSelected ? 18 : 24">
          <BasicTable
            :columns="columns"
            :bordered="true"
            :useSearchForm="true"
            :formConfig="formConfig"
            :api="getPositionList"
            :searchInfo="searchInfo"
            :rowSelection="rowSelection"
            :indexColumnProps="indexColumnProps"
            v-bind="getBindValue"
          ></BasicTable>
        </a-col>
        <a-col :span="showSelected ? 6 : 0">
          <BasicTable
            v-bind="selectedTable"
            :dataSource="selectRows"
            :useSearchForm="true"
            :formConfig="{ showActionButtonGroup: false, baseRowStyle: { minHeight: '40px' } }"
          >
            <!--Action bar-->
            <template #action="{ record }">
              <a href="javascript:void(0)" @click="handleDeleteSelected(record)"><Icon icon="ant-design:delete-outlined"></Icon></a>
            </template>
          </BasicTable>
        </a-col>
      </a-row>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getPositionList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';

  export default defineComponent({
    name: 'PositionSelectModal',
    components: {
      //Asynchronous loading is required hereBasicTable
      BasicModal,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: {
      ...selectProps,
      //Select box title
      modalTitle: {
        type: String,
        default: 'Job selection',
      },
    },
    emits: ['register', 'getSelectResult'],
    setup(props, { emit, refs }) {
      //Registration pop-up box
      const [register, { closeModal }] = useModalInner();
      const attrs = useAttrs();
      //Table configuration
      const config = {
        canResize: false,
        bordered: true,
        size: 'small',
        //Change to readrowKey,Custom transfer parameters
        rowKey: props.rowKey,
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, visibleChange, indexColumnProps, getSelectResult, handleDeleteSelected, selectRows }] = useSelectBiz(
        getPositionList,
        getBindValue
      );
      const searchInfo = ref(props.params);
      //Queryform
      const formConfig = {
        labelCol: {
          span: 4,
        },
        baseColProps: {
          xs: 24,
          sm: 10,
          md: 10,
          lg: 10,
          xl: 10,
          xxl: 10,
        },
        //update-begin-author:liusq date:2023-10-30 for: [issues/5514]Component page display is misaligned
        actionColOptions: {
            xs: 24,
            sm: 8,
            md: 8,
            lg: 8,
            xl: 8,
            xxl: 8,
        },
        //update-end-author:liusq date:2023-10-30 for: [issues/5514]Component page display is misaligned
        schemas: [
          {
            label: 'Job title',
            field: 'name',
            component: 'JInput',
            colProps: { span: 10 },
          },
        ],
      };
      //Define table columns
      const columns = [
        {
          title: 'Job title',
          dataIndex: 'name',
          // width: 180,
        },
        {
          title: 'Job level',
          dataIndex: 'postLevel',
        },
      ];
      //selectedtableinformation
      const selectedTable = {
        pagination: false,
        showIndexColumn: false,
        scroll: { y: 390 },
        size: 'small',
        canResize: false,
        bordered: true,
        rowKey: 'id',
        columns: [
          {
            title: 'Job title',
            dataIndex: 'name',
            width: 40,
          },
          {
            title: 'operate',
            dataIndex: 'action',
            align: 'center',
            width: 40,
            slots: { customRender: 'action' },
          },
        ],
      };
      /**
       * Confirm selection
       */
      function handleOk() {
        getSelectResult((options, values) => {
          //回传选项和selected值
          emit('getSelectResult', options, values);
          //Close pop-up window
          closeModal();
        });
      }
      return {
        handleOk,
        getPositionList,
        register,
        visibleChange,
        getBindValue,
        formConfig,
        indexColumnProps,
        columns,
        rowSelection,

        selectedTable,
        selectRows,
        handleDeleteSelected,
        searchInfo,
      };
    },
  });
</script>
