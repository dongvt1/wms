<template>
  <a-card :bordered="false">
    <!-- Operation button area -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" preIcon="ant-design:plus">New</a-button>
      <!--      <a-button type="primary" preIcon="ant-design:download" @click="handleExportExcel('Single table native list')">Export</a-button>-->
      <!--      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="handleImportExcel">import</j-upload-button>-->
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchDel">
              <Icon icon="ant-design:delete-outlined"></Icon>
              delete
            </a-menu-item>
          </a-menu>
        </template>
        <a-button
          >Batch operations
          <Icon icon="mdi:chevron-down"></Icon>
        </a-button>
      </a-dropdown>
    </div>

    <!-- tablearea-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> Selected <a style="font-weight: 600">{{ selectedRowKeys.length }}</a
        >item
        <a style="margin-left: 24px" @click="onClearSelected">Clear</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        :scroll="{ x: true }"
        bordered
        rowKey="id"
        class="j-table-force-nowrap"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex==='tupian'">
            <span v-if="!text" style="font-size: 12px; font-style: italic">No pictures</span>
            <img v-else :src="getImgView(text)" :preview="record.id" alt="" class="anty-img-wrap" />
          </template>
          <template v-else-if="column.dataIndex==='wenjian'">
            <span v-if="!text" style="font-size: 12px; font-style: italic">No file</span>
            <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download" size="small" @click="downloadFile(text)"> download </a-button>
          </template>
          <template v-else-if="column.dataIndex==='action'">
            <a @click="handleEdit(record)">edit</a>
            <a-divider type="vertical" />
            <a-dropdown>
              <!-- update-begin--author:liaozhiyang---date:20230803---for：【QQYUN-5838】Make the icon smaller and consistent -->
              <a class="ant-dropdown-link">More <Icon icon="mdi-light:chevron-down"></Icon></a>
              <!-- update-end--author:liaozhiyang---date:20230803---for：【QQYUN-5838】Make the icon smaller and consistent -->
              <template #overlay>
                <a-menu class="antd-more">
                  <a-menu-item>
                    <a @click="handleDetail(record)">Details</a>
                  </a-menu-item>
                  <a-menu-item>
                    <Popconfirm title="确定delete吗?" @confirm="() => handleDelete(record.id)" placement="left">
                      <a>delete</a>
                    </Popconfirm>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
          <!-- <template v-else-if="column.dataIndex==='htmlSlot'">
            <div v-html="text"></div>
          </template>
          <template v-else-if="column.dataIndex==='pcaSlot'">
            <div>{{ getAreaTextByCode(text) }}</div>
          </template> -->
        </template>
      </a-table>
    </div>
    <OneNativeModal ref="oneProtogenesisModal" @ok="handleSuccess"></OneNativeModal>
  </a-card>
</template>

<script lang="ts" setup>
  import '../less/TableExpand.less';
  import { onMounted, ref, reactive } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil.js';
  import { getAreaTextByCode } from '/@/components/Form/src/utils/Area';
  import OneNativeModal from './components/OneNativeModal.vue';
  import { Modal, Popconfirm } from 'ant-design-vue';
  import { JSelectUserByDept, JDictSelectTag, JSelectDept, JSearchSelect } from '/@/components/Form';
  import Icon from '/@/components/Icon/index';
  import { filterObj, getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { loadCategoryData } from '/@/api/common/api';
  import { getToken } from '/@/utils/auth';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import { initDictOptions } from '/@/utils/dict';

  const { handleExportXls, handleImportXls } = useMethods();
  const modalVisible = ref<boolean>(false);
  const queryParam = ref<any>({});
  const loading = ref<boolean>(false);
  const dictOptions = ref<any>([]);
  const oneProtogenesisModal = ref();
  const tokenHeader = { 'X-Access-Token': getToken() };
  //Header
  const columns = ref<any>([
    {
      title: 'text',
      align: 'center',
      dataIndex: 'name',
    },
    {
      title: 'Dictionary drop down',
      align: 'center',
      dataIndex: 'xiala',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['xiala'], text) : ''),
    },
    {
      title: 'Dictionary radio',
      align: 'center',
      dataIndex: 'danxuan',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['danxuan'], text) : ''),
    },
    {
      title: 'Dictionary multiple choice',
      align: 'center',
      dataIndex: 'duoxuan',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['duoxuan'], text) : ''),
    },
    {
      title: 'switch',
      align: 'center',
      dataIndex: 'kaiguan',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['kaiguan'], text) : ''),
    },
    {
      title: 'date',
      align: 'center',
      dataIndex: 'riqi',
      customRender: function ({ text }) {
        return !text ? '' : text.length > 10 ? text.substr(0, 10) : text;
      },
    },
    {
      title: 'year month day hour minute second',
      align: 'center',
      dataIndex: 'nyrsfm',
    },
    {
      title: 'time',
      align: 'center',
      dataIndex: 'shijian',
    },
    {
      title: 'document',
      align: 'center',
      dataIndex: 'wenjian',
    },
    {
      title: 'picture',
      align: 'center',
      dataIndex: 'tupian',
    },
    {
      title: 'operate',
      dataIndex: 'action',
      align: 'center',
      fixed: 'right',
      width: 147,
    },
  ]);

  const Api = reactive<any>({
    list: '/test/jeecgDemo/oneNative/list',
    delete: '/test/jeecgDemo/oneNative/delete',
    exportXls: '/test/jeecgDemo/oneNative/exportXls',
    importExcel: 'test/jeecgDemo/oneNative/importExcel',
  });

  const dataSource = ref<any>([]);
  const toggleSearchStatus = ref<boolean>(false);
  const ipagination = ref<any>({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30'],
    showTotal: (total, range) => {
      return range[0] + '-' + range[1] + ' common' + total + 'strip';
    },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
  });

  const selectedRowKeys = ref<any>([]);
  const selectionRows = ref<any>([]);
  const iSorter = ref<any>({ column: 'createTime', order: 'desc' });
  const iFilters = ref<any>({});
  const { createMessage } = useMessage();

  /**
   * checkbox selected event
   * @param rowKeys
   * @param rows
   */
  function onSelectChange(rowKeys, rows) {
    selectedRowKeys.value = rowKeys;
    selectionRows.value = rows;
  }

  /**
   * table change event
   */
  function handleTableChange({ pagination, filters, sorter }) {
    ipagination.value = pagination;
    iSorter.value = sorter;
    iFilters.value = { ...filters };
  }

  /**
   * New
   */
  function handleAdd() {
    oneProtogenesisModal.value.disableSubmit = false;
    oneProtogenesisModal.value.add();
  }

  /**
   * Clear selected rows
   */
  function onClearSelected() {
    selectedRowKeys.value = [];
    selectionRows.value = [];
  }

  /**
   * 批量delete
   */
  function batchDel() {
    Modal.confirm({
      title: 'confirmdelete',
      content: 'yesnodelete选中数据',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: () => {
        defHttp.delete({ url: Api.delete, data: { ids: selectedRowKeys.value } }, { joinParamsToUrl: true }).then(() => {
          handleSuccess();
        });
      },
    });
  }

  /**
   * Exportexcel
   */
  function handleExportExcel(title) {
    let paramsForm = getQueryParams();
    if (selectedRowKeys.value && selectedRowKeys.value.length > 0) {
      paramsForm['selections'] = selectedRowKeys.join(',');
    }
    handleExportXls(title, Api.exportXls, filterObj(paramsForm));
  }

  /**
   * importexcel
   */
  function handleImportExcel(file) {
    handleImportXls(file, Api.importExcel, '').then(() => {
      handleSuccess();
    });
  }

  /**
   * Get query parameters
   */
  function getQueryParams() {
    let params = Object.assign(queryParam.value, iSorter.value, iFilters.value);
    params.field = getQueryField();
    params.pageNo = ipagination.value.current;
    params.pageSize = ipagination.value.pageSize;
    return filterObj(params);
  }

  /**
   * Field permission control
   */
  function getQueryField() {
    let str = 'id,';
    columns.value.forEach(function (value) {
      str += ',' + value.dataIndex;
    });
    return str;
  }

  /**
   * initialization data
   */
  function loadData(arg?) {
    if (arg === 1) {
      ipagination.value.current = 1;
    }
    loading.value = true;
    let params = getQueryParams();
    defHttp
      .get({ url: Api.list, params }, { isTransformResponse: false })
      .then((res) => {
        if (res.success) {
          dataSource.value = res.result.records;
          if (res.result && res.result.total) {
            ipagination.value.total = res.result.total;
          } else {
            ipagination.value.total = 0;
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        loading.value = false;
      });
  }

  //Query
  function searchQuery() {
    loadData(1);
    selectedRowKeys.value = [];
    selectionRows.value = [];
  }

  /**
   * Queryarea展switch闭
   */
  function handleToggleSearch() {
    toggleSearchStatus.value = !toggleSearchStatus.value;
  }

  /**
   * reset button
   */
  function searchReset() {
    queryParam.value = {};
    loadData(1);
  }

  /**
   * 获取预览picture
   */
  function getImgView(text) {
    if (text && text.indexOf(',') > 0) {
      text = text.substring(0, text.indexOf(','));
    }
    return getFileAccessHttpUrl(text);
  }

  /**
   * edit
   * @param record
   */
  function handleEdit(record) {
    oneProtogenesisModal.value.disableSubmit = false;
    oneProtogenesisModal.value.edit(record);
  }

  /**
   * Details
   * @param record
   */
  function handleDetail(record) {
    oneProtogenesisModal.value.disableSubmit = true;
    oneProtogenesisModal.value.edit(record);
  }

  /**
   * delete
   * @param id
   */
  function handleDelete(id) {
    defHttp.delete({ url: Api.delete, data: { ids: id } }, { joinParamsToUrl: true }).then((res) => {
      handleSuccess();
    });
  }

  /**
   * 初始化字典选item
   */
  async function initDictConfig() {
    dictOptions.value['flzds'] = await loadCategoryData({ code: 'B01' });
    dictOptions.value['xiala'] = await initDictOptions('sex');
    dictOptions.value['danxuan'] = await initDictOptions('sex');
    dictOptions.value['duoxuan'] = await initDictOptions('urgent_level');
  }

  /**
   * Callback event after saving the form
   */
  function handleSuccess() {
    selectedRowKeys.value = [];
    selectionRows.value = [];
    loadData(1);
  }
  onMounted(() => {
    dictOptions.value['kaiguan'] = [
      { text: 'yes', value: '1' },
      { text: 'no', value: '2' },
    ];
    //Initial loading page
    loadData();
    //初始化字典选item
    initDictConfig();
  });
</script>
