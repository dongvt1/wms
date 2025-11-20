<!--Character selection box-->
<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="800px" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
      <BasicTable
        :columns="columns"
        v-bind="config"
        :useSearchForm="true"
        :formConfig="formConfig"
        :api="getRoleList"
        :searchInfo="searchInfo"
        :rowSelection="rowSelection"
        :indexColumnProps="indexColumnProps"
      ></BasicTable>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getRoleList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';
  import { useAttrs } from '/@/hooks/core/useAttrs';

  export default defineComponent({
    name: 'UserSelectModal',
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
        default: 'Character selection',
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
        rowKey: unref(props).rowKey,
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, indexColumnProps, visibleChange, getSelectResult }] = useSelectBiz(getRoleList, getBindValue);
      const searchInfo = ref(props.params);
      //Queryform
      const formConfig = {
        //labelWidth: 220,
        baseColProps: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 14,
          xl: 14,
          xxl: 14,
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
            label: 'Character name',
            field: 'roleName',
            component: 'Input',
          },
        ],
      };
      //Define table columns
      const columns = [
        {
          title: 'Character name',
          dataIndex: 'roleName',
          width: 240,
          align: 'left',
        },
        {
          title: 'role coding',
          dataIndex: 'roleCode',
          // width: 40,
        },
      ];

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
        config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getRoleList,
        formConfig,
        getBindValue,
        columns,
        rowSelection,
      };
    },
  });
</script>
