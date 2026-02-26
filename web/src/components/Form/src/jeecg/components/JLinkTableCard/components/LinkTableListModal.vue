<template>
  <BasicModal
    @register="registerModal"
    :width="popModalFixedWidth"
    :dialogStyle="{ top: '70px' }"
    :bodyStyle="popBodyStyle"
    :title="modalTitle"
    wrapClassName="jeecg-online-pop-list-modal"
  >
    <template #footer>
      <a-button key="back" @click="handleCancel">closure</a-button>
      <a-button :disabled="submitDisabled" key="submit" type="primary" @click="handleSubmit" :loading="submitLoading">Sure</a-button>
    </template>

    <BasicTable ref="tableRef" @register="registerTable" :rowSelection="rowSelection">
      <!-- update-begin-author:taoyan date:2023-7-11 for: issues/4992 onlineform development The field control type is associated record Can I add a query to the selection list when adding it? -->
      <template #tableTitle>
        <a-input-search v-model:value="searchText" @search="onSearch" placeholder="Please enter keywords，Press Enter to search" style="width: 240px" />
      </template>
      <!-- update-end-author:taoyan date:2023-7-11 for: issues/4992 onlineform development The field control type is associated record Can I add a query to the selection list when adding it? -->

      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)"> </TableAction>
      </template>

      <template #fileSlot="{ text }">
        <span v-if="!text" style="font-size: 12px; font-style: italic">No file</span>
        <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download" size="small" @click="downloadRowFile(text)"> download </a-button>
      </template>

      <template #imgSlot="{ text }">
        <span v-if="!text" style="font-size: 12px; font-style: italic">No pictures</span>
        <img v-else :src="getImgView(text)" alt="Picture does not exist" class="online-cell-image" @click="viewOnlineCellImage(text)" />
      </template>

      <template #htmlSlot="{ text }">
        <div v-html="text"></div>
      </template>

      <template #pcaSlot="{ text }">
        <div :title="getPcaText(text)">{{ getPcaText(text) }}</div>
      </template>

      <template #dateSlot="{ text, column }">
        <span>{{ getFormatDate(text, column) }}</span>
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, watch, ref, toRaw, computed, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { defHttp } from '/@/utils/http/axios';
  import { useTableColumns } from '../hooks/useTableColumns';
  import { createAsyncComponent } from '@/utils/factory/createAsyncComponent';
  import { useFixedHeightModal } from '../hooks/useLinkTable';

  export default defineComponent({
    name: 'LinkTableListModal',
    props: {
      /**Can be a table name can beID*/
      id: {
        type: String,
        default: '',
      },
      multi: {
        type: Boolean,
        default: false,
      },
      addAuth: {
        type: Boolean,
        default: true,
      },
    },
    components: {
      BasicModal,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), { loading: true, delay: 1000 }),
      TableAction: createAsyncComponent(() => import('/@/components/Table/src/components/TableAction.vue'), { loading: true, delay: 1000 }),
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { createMessage: $message } = useMessage();
      const tableRef = ref(null);
      // Pop-up window height control
      const { popModalFixedWidth, resetBodyStyle, popBodyStyle } = useFixedHeightModal();
      const searchText = ref('');
      const modalWidth = ref(800);
      //useModalInner
      const [registerModal, { closeModal }] = useModalInner((data) => {
        searchText.value = '';
        selectedRowKeys.value = data.selectedRowKeys;
        selectedRows.value = data.selectedRows;
        setTimeout(async () => {
          await setPagination({ current: 1 });
          await reload(); // Wait for form to load
          resetBodyStyle();
        }, 100);
      });

      function handleCancel() {
        closeModal();
      }
      const submitDisabled = computed(() => {
        const arr = selectedRowKeys.value;
        if (arr && arr.length > 0) {
          return false;
        }
        return true;
      });
      const submitLoading = ref(false);
      function handleSubmit() {
        submitLoading.value = true;
        let arr = toRaw(selectedRows.value);
        if (arr && arr.length > 0) {
          emit('success', arr);
          closeModal();
        }
        setTimeout(() => {
          submitLoading.value = false;
        }, 200);
      }

      //---------------------list------------------------
      function queryTableData(params) {
        const url = '/online/cgform/api/getData/' + props.id;
        return defHttp.get({ url, params });
      }

      function list(params) {
        params['column'] = 'id';
        return new Promise(async (resolve, _reject) => {
          const aa = await queryTableData(params);
          resolve(aa);
        });
      }

      const onlineTableContext = {
        isPopList: true,
        reloadTable() {
          console.log('reloadTable');
        },
        isTree() {
          return false;
        },
      };
      const extConfigJson = ref<any>({});

      // deal with BasicTable configuration
      const { columns, downloadRowFile, getImgView, getPcaText, getFormatDate, handleColumnResult, hrefComponent, viewOnlineCellImage } =
        useTableColumns(onlineTableContext, extConfigJson);

      /**
       * QuerytableColumn information and other configurations
       */
      function getColumnList() {
        const url = '/online/cgform/api/getColumns/' + props.id;
        return new Promise((resolve, reject) => {
          defHttp.get({ url }, { isTransformResponse: false }).then((res) => {
            if (res.success) {
              resolve(res.result);
            } else {
              $message.warning(res.message);
              reject();
            }
          });
        });
      }

      const modalTitle = ref('');
      watch(
        () => props.id,
        async () => {
          let columnResult: any = await getColumnList();
          handleColumnResult(columnResult);
          modalTitle.value = columnResult.description;
        },
        { immediate: true }
      );

      const { tableContext } = useListPage({
        designScope: 'process-design',
        pagination: true,
        tableProps: {
          title: '',
          api: list,
          clickToRowSelect: true,
          columns: columns,
          showTableSetting: false,
          immediate: false,
          //showIndexColumn: true,
          canResize: false,
          showActionColumn: false,
          actionColumn: {
            dataIndex: 'action',
            slots: { customRender: 'action' },
          },
          useSearchForm: false,
          beforeFetch: (params) => {
            return addQueryParams(params);
          },
        },
      });
      const [registerTable, { reload, setPagination }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
      watch(
        () => props.multi,
        (val) => {
          if (val == true) {
            rowSelection.type = 'checkbox';
          } else {
            rowSelection.type = 'radio';
          }
        },
        { immediate: true }
      );

      /**
       * Action bar
       */
      function getTableAction(record) {
        return [
          {
            label: 'edit',
            onClick: handleUpdate.bind(null, record),
          },
        ];
      }

      function handleUpdate(record) {
        console.log('handleUpdate', record);
      }

      function onSearch() {
        reload();
      }
      const eqConditonTypes = ['int', 'double', 'Date', 'Datetime', 'BigDecimal'];
      function addQueryParams(params) {
        let text = searchText.value;
        if (!text) {
          params['superQueryMatchType'] = 'or';
          params['superQueryParams'] = '';
          return params;
        }
        let arr = columns.value;
        let conditions: any[] = [];
        if (arr && arr.length > 0) {
          for (let item of arr) {
            if (item.dbType) {
              if (item.dbType == 'string') {
                conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'like', val: text });
              } else if (item.dbType == 'Date') {
                if (text.length == '2020-10-10'.length) {
                  conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'eq', val: text });
                }
              } else if (item.dbType == 'Datetime') {
                if (text.length == '2020-10-10 10:10:10'.length) {
                  conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'eq', val: text });
                }
              } else if (eqConditonTypes.indexOf(item.dbType)) {
                conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'eq', val: text });
              } else {
                //text blob不做deal with
              }
            }
          }
        }
        params['superQueryMatchType'] = 'or';
        params['superQueryParams'] = encodeURI(JSON.stringify(conditions));
        return params;
      }

      // modalData addition completed 直接closurelist，Bring new data back to the form
      function handleDataSave(data) {
        console.log('handleDateSave', data);
        // update-begin--author:liaozhiyang---date:20250429---for：【issues/8163】New associated records are lost
        let arr = [data, ...selectedRows.value];
        // update-end--author:liaozhiyang---date:20250429---for：【issues/8163】New associated records are lost
        emit('success', arr);
        closeModal();
        //reload();
      }

      return {
        registerModal,
        modalWidth,
        handleCancel,
        submitDisabled,
        submitLoading,
        handleSubmit,

        registerTable,
        getTableAction,
        searchText,
        onSearch,

        downloadRowFile,
        getImgView,
        getPcaText,
        getFormatDate,
        hrefComponent,
        viewOnlineCellImage,
        rowSelection,
        modalTitle,

        reload,

        popModalFixedWidth,
        popBodyStyle,
        handleDataSave,

        tableRef,
      };
    },
  });
</script>

<style scoped></style>
