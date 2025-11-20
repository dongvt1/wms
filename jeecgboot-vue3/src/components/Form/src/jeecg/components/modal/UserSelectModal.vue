<!--User selection box-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      :width="showSelected ? '1200px' : '900px'"
      wrapClassName="j-user-select-modal"
      @ok="handleOk"
      @cancel="handleCancel"
      :maxHeight="maxHeight"
      :centered="true"
      destroyOnClose
      @visible-change="visibleChange"
      
    >
      <a-row>
        <a-col :span="showSelected ? 18 : 24">
          <BasicTable
            ref="tableRef"
            :columns="columns"
            :scroll="tableScroll"
            v-bind="getBindValue"
            :useSearchForm="true"
            :formConfig="formConfig"
            :api="hasCustomApi ? customListApi : getUserList"
            :searchInfo="searchInfo"
            :rowSelection="rowSelection"
            :indexColumnProps="indexColumnProps"
            :afterFetch="afterFetch"
            :beforeFetch="beforeFetch"
          >
            <!-- update-begin-author:taoyan date:2022-5-25 for: VUEN-1112one to many User selection Number of selections not shown，and clear -->
            <template #tableTitle></template>
            <!-- update-end-author:taoyan date:2022-5-25 for: VUEN-1112one to many User selection Number of selections not shown，and clear -->
          </BasicTable>
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
  import { defineComponent, unref, ref, watch, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getUserList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';

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
        default: 'Select user',
      },
      //update-begin---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------
      //Exclude usersidcollection of
      excludeUserIdList: {
        type: Array,
        default: [],
      },
      //update-end---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------

      // QuerytableCustom interface for data usage
      customListApi: {type: Function},
      // 自定义接口的Querystrip件是否使用 JInput
      customApiJInput: {type: Boolean, default: true},
    },
    emits: ['register', 'getSelectResult', 'close'],
    setup(props, { emit, refs }) {
      // update-begin-author:taoyan date:2022-5-24 for: VUEN-1086 【Mobile terminal】User selection Query按钮 The effect is not good List display without scroll bars
      const tableScroll = ref<any>({ x: false });
      const tableRef = ref();
      const maxHeight = ref(600);

      const hasCustomApi = computed(() => typeof props.customListApi === 'function');

      //Registration pop-up box
      const [register, { closeModal }] = useModalInner(() => {
        if (window.innerWidth < 900) {
          tableScroll.value = { x: 900 };
        } else {
          tableScroll.value = { x: false };
        }
        //update-begin-author:taoyan date:2022-6-2 for: VUEN-1112 one to many User selection Number of selections not shown，and clear
        setTimeout(() => {
          if (tableRef.value) {
            tableRef.value.setSelectedRowKeys(selectValues['value'] || []);
          }
        }, 800);
        //update-end-author:taoyan date:2022-6-2 for: VUEN-1112 one to many User selection Number of selections not shown，and clear
      });
      // update-end-author:taoyan date:2022-5-24 for: VUEN-1086 【Mobile terminal】User selection Query按钮 The effect is not good List display without scroll bars
      const attrs = useAttrs();
      //Table configuration
      const config = {
        canResize: false,
        bordered: true,
        size: 'small',
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, visibleChange, selectValues, indexColumnProps, getSelectResult, handleDeleteSelected, selectRows }] = useSelectBiz(
        getUserList,
        getBindValue,
        emit
      );
      const searchInfo = ref(props.params);
      // update-begin--author:liaozhiyang---date:20230811---for：【issues/657】Deleting the selected list on the right is invalid
      watch(rowSelection.selectedRowKeys, (newVal) => {
        //update-begin---author:wangshuai ---date: 20230829  for：nullPointer exception causes the console error page not to be displayed------------
        if(tableRef.value){
          tableRef.value.setSelectedRowKeys(newVal);
        }
        //update-end---author:wangshuai ---date: 20230829 for：nullPointer exception causes the console error page not to be displayed------------
      });
      // update-end--author:liaozhiyang---date:20230811---for：【issues/657】Deleting the selected list on the right is invalid
      //Queryform
      const formConfig = {
        baseColProps: {
          xs: 24,
          sm: 8,
          md: 6,
          lg: 8,
          xl: 6,
          xxl: 6,
        },
        //update-begin-author:taoyan date:2022-5-24 for: VUEN-1086 【Mobile terminal】User selection Query按钮 The effect is not good List display without scroll bars---Query表单按钮的栅格布局和表单的保持一致
        actionColOptions: {
          xs: 24,
          sm: 8,
          md: 8,
          lg: 8,
          xl: 8,
          xxl: 8,
        },
        //update-end-author:taoyan date:2022-5-24 for: VUEN-1086 【Mobile terminal】User selection Query按钮 The effect is not good List display without scroll bars---Query表单按钮的栅格布局和表单的保持一致
        schemas: [
          {
            label: 'account',
            field: 'username',
            component: (hasCustomApi.value && !props.customApiJInput) ? 'Input' : 'JInput',
          },
          {
            label: 'Name',
            field: 'realname',
            component: (hasCustomApi.value && !props.customApiJInput) ? 'Input' : 'JInput',
          },
        ],
        autoSubmitOnEnter: true
      };
      //Define table columns
      const columns = [
        {
          title: '用户account',
          dataIndex: 'username',
          width: 120,
          align: 'left',
        },
        {
          title: '用户Name',
          dataIndex: 'realname',
          width: 120,
        },
        {
          title: 'gender',
          dataIndex: 'sex_dictText',
          width: 50,
        },
        {
          title: 'phone number',
          dataIndex: 'phone',
          width: 120,
        },
        {
          title: 'Mail',
          dataIndex: 'email',
          // width: 40,
        },
        {
          title: 'state',
          dataIndex: 'status_dictText',
          width: 80,
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
            title: '用户Name',
            dataIndex: 'realname',
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
      
      //update-begin---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------
      /**
       * 用户返回结果逻辑Query
       */
      function afterFetch(record) {
        let excludeList = props.excludeUserIdList;
        if(!excludeList){
          return record;
        }
        let arr:any[] = [];
        //If there is a filter useridgather，And the data returned by the background is not empty
        if(excludeList.length>0 && record && record.length>0){
          for(let item of record){
            if(excludeList.indexOf(item.id)<0){
              arr.push({...item})
            }
          }
          return arr;
        }
        return record;
      }
      // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9366】User selection组件取消和关闭会把选择数据带入
      const handleCancel = () => {
        emit('close');
      };
      // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9366】User selection组件取消和关闭会把选择数据带入
      //update-end---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------

      // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-305】small screen display10strip
      const clientHeight = document.documentElement.clientHeight * 200;
      maxHeight.value = clientHeight > 600 ? 600 : clientHeight;
      // update-end--author:liaozhiyang---date:20240607---for：【TV360X-305】small screen display10strip

      //update-begin---author:wangshuai---date:2024-07-03---for:【TV360X-1629】User selection组件不是根据创建时间正序排序的---
      /**
       * Sort by creation time before requesting
       *
       * @param params
       */
      function beforeFetch(params) {
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      }
      //update-end---author:wangshuai---date:2024-07-03---for:【TV360X-1629】User selection组件不是根据创建时间正序排序的---

      return {
        //config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getBindValue,
        getUserList,
        formConfig,
        columns,
        rowSelection,
        selectRows,
        selectedTable,
        handleDeleteSelected,
        tableScroll,
        tableRef,
        afterFetch,
        handleCancel,
        maxHeight,
        beforeFetch,
        hasCustomApi,
      };
    },
  });
</script>
