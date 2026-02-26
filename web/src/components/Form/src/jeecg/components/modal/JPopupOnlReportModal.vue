<template>
  <!--popupselection box-->
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="title"
      :width="1200"
      @ok="handleSubmit"
      @cancel="handleCancel"
      cancelText="closure"
      wrapClassName="j-popup-modal"
      @visible-change="visibleChange"
    >
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" v-if="showSearchFlag" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" @keyup.enter.native="searchQuery">
          <a-row :gutter="24">
            <template v-for="(item, index) in queryInfo">
              <template v-if="item.hidden === '1'">
                <a-col :md="8" :sm="24" :key="'query' + index" v-show="toggleSearchStatus">
                  <SearchFormItem :formElRef="formRef" :queryParam="queryParam" :item="item" :dictOptions="dictOptions"></SearchFormItem>
                </a-col>
              </template>
              <template v-else>
                <a-col :md="8" :sm="24" :key="'query' + index">
                  <SearchFormItem :formElRef="formRef" :queryParam="queryParam" :item="item" :dictOptions="dictOptions"></SearchFormItem>
                </a-col>
              </template>
            </template>

            <a-col :md="8" :sm="8" v-if="showAdvancedButton">
              <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
                <a-col :lg="6">
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">Query</a-button>
                  <a-button preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">reset</a-button>
                  <a @click="handleToggleSearch" style="margin-left: 8px">
                    {{ toggleSearchStatus ? 'close' : 'Expand' }}
                    <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                  </a>
                </a-col>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <BasicTable
        ref="tableRef"
        :canResize="false"
        :bordered="true"
        :loading="loading"
        :rowKey="rowkey ? rowkey : combineRowKey"
        :columns="columns"
        :showIndexColumn="false"
        :dataSource="dataSource"
        :pagination="pagination"
        :rowSelection="rowSelection"
        @row-click="clickThenCheck"
        @change="handleChangeInTable"
      >
        <template #tableTitle></template>
         <template #bodyCell="{text, column}">
          <template v-if="column.fieldType === 'Image'">
            <span v-if="!text" style="font-size: 12px; font-style: italic">No pictures</span>
            <img v-else :src="getImgView(text)" alt="Picture does not exist" class="cellIamge" @click="viewOnlineCellImage($event, text)" />
          </template>
        </template>
      </BasicTable>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { defineComponent, unref, ref, watch, watchEffect, reactive, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { usePopBiz } from '/@/components/jeecg/OnLine/hooks/usePopBiz';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { createImgPreview } from '/@/components/Preview/index';

  export default defineComponent({
    name: 'JPopupOnlReportModal',
    components: {
      //Asynchronous loading is required hereBasicTable
      BasicModal,
      SearchFormItem: createAsyncComponent(() => import('/@/components/jeecg/OnLine/SearchFormItem.vue'), { loading: false }),
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: ['multi', 'code', 'sorter', 'groupId', 'param','showAdvancedButton', 'getFormValues', 'selected', 'rowkey'],
    emits: ['ok', 'register'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const labelCol = reactive({
        xs: { span: 24 },
        sm: { span: 6 },
      });
      const wrapperCol = reactive({
        xs: { span: 24 },
        sm: { span: 18 },
      });
      //Registration pop-up box
      const [register, { closeModal }] = useModalInner();
      const formRef = ref();
      const tableRef = ref();
      const toggleSearchStatus = ref(false);
      const attrs = useAttrs();
      const tableScroll = ref({ x: true });
      // update-begin--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
      const getBindValue = computed(() => {
        return Object.assign({}, unref(props), unref(attrs));
      });
      // update-end--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
      const [
        {
          visibleChange,
          loadColumnsInfo,
          dynamicParamHandler,
          loadData,
          handleChangeInTable,
          combineRowKey,
          clickThenCheck,
          filterUnuseSelect,
          getOkSelectRows,
        },
        {
          visible,
          rowSelection,
          checkedKeys,
          selectRows,
          pagination,
          dataSource,
          columns,
          loading,
          title,
          iSorter,
          queryInfo,
          queryParam,
          dictOptions,
        },
      ] = usePopBiz(getBindValue,tableRef);

      const showSearchFlag = computed(() => unref(queryInfo) && unref(queryInfo).length > 0);
      /**
       *monitorcode
       */
      watch(
        () => props.code,
        () => {
          loadColumnsInfo();
        }
      );
      /**
       *monitorpopupdynamic parameters Support system variable syntax
       */
      watch(
        () => props.param,
        () => {
          // update-begin--author:liaozhiyang---date:20231213---for：【issues/901】JPopupComponent configurationparamException after parameter
          if (visible.value) {
            dynamicParamHandler();
            loadData();
          }
          // update-end--author:liaozhiyang---date:20231213---for：【issues/901】JPopupComponent configurationparamException after parameter
        }
      );
      /**
       *monitorsortersort field
       */
      watchEffect(() => {
        if (props.sorter) {
          let arr = props.sorter.split('=');
          if (arr.length === 2 && ['asc', 'desc'].includes(arr[1].toLowerCase())) {
            iSorter.value = { column: arr[0], order: arr[1].toLowerCase() };
            // sort field受控
            unref(columns).forEach((col) => {
              if (col.dataIndex === unref(iSorter).column) {
                col['sortOrder'] = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
              } else {
                col['sortOrder'] = false;
              }
            });
          } else {
            console.warn('【JPopup】sorterParameter is illegal');
          }
        }
      });

      //update-begin-author:taoyan date:2022-5-31 for: VUEN-1156 popup When multiple data has paging，Select other pages，closurepopup Click it again，Pagination still selects the last clicked pagination，But the data is the data on the first page Not refreshed
      watch(
        () => pagination.current,
        (current) => {
          if (current) {
            tableRef.value.setPagination({
              current: current,
            });
          }
        }
      );
      //update-end-author:taoyan date:2022-5-31 for: VUEN-1156 popup When multiple data has paging，Select other pages，closurepopup Click it again，Pagination still selects the last clicked pagination，But the data is the data on the first page Not refreshed

      function handleToggleSearch() {
        toggleSearchStatus.value = !unref(toggleSearchStatus);
      }
      /**
       * Cancel/closure
       */
      function handleCancel() {
        closeModal();
        checkedKeys.value = [];
        selectRows.value = [];
        // update-begin--author:liaozhiyang---date:20230908---for：【issues/742】Delete default still exists after selection
        tableRef.value.clearSelectedRowKeys();
        // update-end--author:liaozhiyang---date:20230908---for：【issues/742】Delete default still exists after selection
      }

      /**
       *Confirm submission
       */
      function handleSubmit() {
        filterUnuseSelect();
        if (!props.multi && unref(selectRows) && unref(selectRows).length > 1) {
          createMessage.warning('Only one record can be selected');
          return false;
        }
        if (!unref(selectRows) || unref(selectRows).length == 0) {
          createMessage.warning('Select at least one record');
          return false;
        }
        //update-begin-author:taoyan date:2022-5-31 for: VUEN-1155 popup When selecting data，Multiple duplicates of data will be selected
        let rows = getOkSelectRows!();
        emit('ok', rows);
        //update-end-author:taoyan date:2022-5-31 for: VUEN-1155 popup When selecting data，Multiple duplicates of data will be selected
        handleCancel();
      }

      /**
       * Query
       */
      function searchQuery() {
        loadData(1);
      }
      /**
       * reset
       */
      function searchReset() {
        queryParam.value = {};
        loadData(1);
      }

      /**
       * 2024-07-24
       * liaozhiyang
       * 【TV360X-1756】Add image type to report
       * picture
       * @param text
       */
      function getImgView(text) {
        if (text && text.indexOf(',') > 0) {
          text = text.substring(0, text.indexOf(','));
        }
        return getFileAccessHttpUrl(text);
      }
      /**
       * 2024-07-24
       * liaozhiyang
       * 【TV360X-1756】Add image type to report
       * Preview list cell picture
       * @param text
       */
      function viewOnlineCellImage(e, text) {
        e.stopPropagation();
        if (text) {
          let imgList: any = [];
          let arr = text.split(',');
          for (let str of arr) {
            if (str) {
              imgList.push(getFileAccessHttpUrl(str));
            }
          }
          createImgPreview({ imageList: imgList });
        }
      }
      // update-begin--author:liaozhiyang---date:20250415--for：【issues/3656】popupdictecho
      watchEffect(() => {
        if (props.selected && props.rowkey) {
          const selected = props.multi ? props.selected : [props.selected];
          checkedKeys!.value = selected.map((item) => item[props.rowkey]);
          selectRows!.value = selected;
        }
      });
      // update-end--author:liaozhiyang---date:20250415--for：【issues/3656】popupdictecho
      return {
        attrs,
        register,
        tableScroll,
        dataSource,
        pagination,
        columns,
        rowSelection,
        checkedKeys,
        loading,
        title,
        handleCancel,
        handleSubmit,
        clickThenCheck,
        loadData,
        combineRowKey,
        handleChangeInTable,
        visibleChange,
        queryInfo,
        queryParam,
        tableRef,
        formRef,
        labelCol,
        wrapperCol,
        dictOptions,
        showSearchFlag,
        toggleSearchStatus,
        handleToggleSearch,
        searchQuery,
        searchReset,
        getImgView,
        viewOnlineCellImage,
      };
    },
  });
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 5px;

    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 0;
      white-space: nowrap;
    }
  }
  :deep(.jeecg-basic-table .ant-table-wrapper .ant-table-title){
    min-height: 0;
  }
  .cellIamge {
    height: 25px !important;
    margin: 0 auto;
    max-width: 80px;
    font-size: 12px;
    font-style: italic;
    cursor: pointer;
  }
</style>
