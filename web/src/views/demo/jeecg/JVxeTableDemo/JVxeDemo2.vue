<template>
  <JVxeTable
    ref="tableRef"
    toolbar
    row-number
    row-selection
    keep-source
    :height="492"
    :loading="loading"
    :dataSource="dataSource"
    :columns="columns"
    :pagination="pagination"
    style="margin-top: 8px"
    @pageChange="handlePageChange"
  >
    <template #toolbarSuffix>
      <a-button @click="handleTableGet">Get data</a-button>
    </template>
  </JVxeTable>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  // noinspection ES6UnusedImports
  import { Popconfirm } from 'ant-design-vue';
  import { JVxeColumn, JVxeTableInstance, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { random } from 'lodash-es';
  import { buildUUID } from '/@/utils/uuid';
  import { uploadUrl } from '/@/api/common/api';

  const tableRef = ref<JVxeTableInstance>();
  const { createMessage } = useMessage();
  const loading = ref(false);
  const columns = ref<JVxeColumn[]>([
    {
      title: 'drop down box_dictionary search',
      key: 'select_dict_search',
      type: JVxeTypes.selectDictSearch,
      width: 200,
      async: true, // Asynchronous search，Default is true
      // Dictionary table configuration information：Database table name,Show field name,Store field name
      dict: 'sys_user,realname,username',
      tipsContent: 'Please enter query conditions',
    },
    {
      title: 'JPopup',
      key: 'popup',
      type: JVxeTypes.popup,
      width: 180,
      popupCode: 'demo',
      // field: 'name,sex,age',
      // orgFields: 'name,sex,age',
      // destFields: 'popup,popup_sex,popup_age',
      fieldConfig: [
        { source: 'name', target: 'popup' },
        { source: 'sex', target: 'popup_sex' },
        { source: 'age', target: 'popup_age' },
      ],
    },
    {
      title: 'JP-gender',
      key: 'popup_sex',
      type: JVxeTypes.select,
      dictCode: 'sex',
      disabled: true,
      width: 100,
    },
    {
      title: 'JP-age',
      key: 'popup_age',
      type: JVxeTypes.normal,
      width: 80,
    },
    {
      title: 'User selection',
      key: 'userSelect',
      type: JVxeTypes.userSelect,
      width: 240,
    },
    {
      title: 'Department selection',
      key: 'departSelect',
      type: JVxeTypes.departSelect,
      width: 240,
    },
    {
      title: 'Single choice',
      key: 'radio',
      type: JVxeTypes.radio,
      width: 130,
      options: [
        { text: 'male', value: '1' },
        { text: 'female', value: '2' },
      ],
      // Allow clear selection（Click again to cancel the selection）
      allowClear: false,
    },
    {
      title: 'upload',
      key: 'upload',
      type: JVxeTypes.upload,
      width: 180,
      btnText: '点击upload',
      token: true,
      responseName: 'message',
      action: uploadUrl,
    },
    {
      title: '图片upload',
      key: 'image',
      type: JVxeTypes.image,
      width: 180,
      maxCount: 6,
    },
    {
      title: '文件upload',
      key: 'file',
      type: JVxeTypes.file,
      width: 180,
      maxCount: 2,
    },
    {
      title: 'progress bar',
      key: 'progress',
      type: JVxeTypes.progress,
      minWidth: 320,
    },
  ]);
  const dataSource = ref<any[]>([]);
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30', '100', '200'],
    total: 1000,
  });

  randomPage(pagination.current, pagination.pageSize, true);

  // Event triggered when paging parameters change
  function handlePageChange(event) {
    // reassign
    pagination.current = event.current;
    pagination.pageSize = event.pageSize;
    // Query data
    randomPage(event.current, event.pageSize, true);
  }

  /** Get value，Ignore form validation */
  function handleTableGet() {
    const values = tableRef.value!.getTableData();
    console.log('Get value:', { values });
    createMessage.success('Get value成功，Please see the console output');
  }

  /* Randomly generate paginated data */
  function randomPage(current, pageSize, $loading = false) {
    if ($loading) {
      loading.value = true;
    }
    let begin = Date.now();
    let values: any[] = [];
    for (let i = 0; i < pageSize; i++) {
      let radio = random(0, 2);
      values.push({
        id: buildUUID(),
        select_dict_search: ['admin', '', 'jeecg'][random(0, 2)],
        progress: random(0, 100),
        radio: radio ? radio.toString() : null,
      });
    }
    dataSource.value = values;
    let end = Date.now();
    let diff = end - begin;
    if ($loading && diff < pageSize) {
      setTimeout(() => (loading.value = false), pageSize - diff);
    }
  }
</script>
