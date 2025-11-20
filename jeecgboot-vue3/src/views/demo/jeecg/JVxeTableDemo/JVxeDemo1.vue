<template>
  <a-space>
    <a-button @click="onToggleLoading">Toggle loading</a-button>
    <a-button @click="onToggleDisabled">Toggle disabled</a-button>
  </a-space>
  <!--This usage scenario is usefulheight，usemaxHeightThere's something wrong with the bottom layer-->
  <JVxeTable
    ref="tableRef"
    stripe
    toolbar
    rowNumber
    rowSelection
    rowExpand
    resizable
    asyncRemove
    clickSelectRow
    :height="480"
    :checkboxConfig="{ range: true }"
    :disabledRows="{ input: ['text--16', 'text--18'] }"
    :loading="loading"
    :disabled="disabled"
    :columns="columns"
    :dataSource="dataSource"
    @removed="onJVxeRemove"
    @valueChange="handleValueChange"
    @blur="handleBlur"
    :custom="true"
  >
    <template #toolbarSuffix>
      <a-button @click="handleTableCheck">form validation</a-button>
      <a-tooltip placement="top" title="Get value，忽略form validation" :autoAdjustOverflow="true">
        <a-button @click="onGetData">Get data</a-button>
      </a-tooltip>
      <a-tooltip placement="top" title="Simulate loading1000piece of data" :autoAdjustOverflow="true">
        <a-button @click="handleTableSet">Set value</a-button>
      </a-tooltip>
      <a-button @click="onGetSelData">Get selected data</a-button>
      <a-button @click="onClearSel">Clear selection</a-button>
      <a-button @click="onDelFirst">Delete the first row of data</a-button>
      <a-button @click="onDelSel">Delete selected data</a-button>
    </template>

    <template #expandContent="props">
      <div style="padding: 20px">
        <span>Hello! My name is: {{ props.row.input }}!</span>
      </div>
    </template>

    <template #myAction="props">
      <a @click="onLookRow(props)">Check</a>
      <a-divider type="vertical" />
      <Popconfirm title="Are you sure to delete?？" @confirm="onDeleteRow(props)">
        <a>delete</a>
      </Popconfirm>
    </template>
  </JVxeTable>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  // noinspection ES6UnusedImports
  import { Popconfirm } from 'ant-design-vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { random } from 'lodash-es';
  import { buildUUID } from '/@/utils/uuid';
  import dayjs from 'dayjs';
  import { pushIfNotExist } from '/@/utils/common/compUtils';

  const { createMessage } = useMessage();
  const tableRef = ref<JVxeTableInstance>();
  const loading = ref(false);
  const disabled = ref(false);
  const columns = ref<JVxeColumn[]>([
    {
      title: 'ID',
      key: 'id',
      type: JVxeTypes.hidden,
    },
    {
      title: 'Not editable',
      key: 'noEdit',
      type: JVxeTypes.normal,
      width: 180,
      defaultValue: 'noEdit-new',
    },
    {
      title: 'single line of text',
      key: 'input',
      type: JVxeTypes.input,
      width: 180,
      defaultValue: '',
      placeholder: 'Please enter${title}',
      validateRules: [
        {
          required: true, // Required
          message: 'Please enter${title}', // displayed text
        },
        {
          pattern: /^[a-z|A-Z][a-z|A-Z\d_-]*$/, // regular
          message: 'Must start with a letter，Can contain numbers、Underline、horizontal bar',
        },
        {
          unique: true,
          message: '${title}cannot be repeated',
        },
        {
          handler({ cellValue, row, column }, callback, target) {
            // cellValue Current check value
            // callback(flag, message) Method must be executed and can only be executed once
            //          flag = Whether it passed the verification，Do not fill in or fill in null Represents no action
            //          message = type of prompt，默认使use配置的 message
            // target Instance object of row edit
            if (cellValue === 'abc') {
              callback(false, '${title}Can't beabc'); // false = failed verification
            } else {
              callback(true); // true = Passed verification
            }
          },
          message: '${title}Default prompt',
        },
      ],
    },
    {
      title: 'multiline text',
      key: 'textarea',
      type: JVxeTypes.textarea,
      width: 200,
    },
    {
      title: 'number',
      key: 'number',
      type: JVxeTypes.inputNumber,
      width: 80,
      defaultValue: 32,
      // 【Statistics column】sum = Sum、average = average value
      statistics: ['sum', 'average'],
    },
    {
      title: 'drop down box',
      key: 'select',
      type: JVxeTypes.select,
      width: 180,
      // drop down options
      options: [
        { title: 'String', value: 'string' },
        { title: 'Integer', value: 'int' },
        { title: 'Double', value: 'double' },
        { title: 'Boolean', value: 'boolean' },
      ],
      // allowInput: true,
      allowSearch: true,
      placeholder: 'Please select',
    },
    {
      title: 'drop down box_dictionary',
      key: 'select_dict',
      type: JVxeTypes.select,
      width: 180,
      options: [],
      dictCode: 'sex',
      placeholder: 'Please select',
    },
    {
      title: 'drop down box_Multiple choice',
      key: 'select_multiple',
      type: JVxeTypes.selectMultiple,
      width: 205,
      options: [
        { title: 'String', value: 'string' },
        { title: 'Integer', value: 'int' },
        { title: 'Double', value: 'double' },
        { title: 'Boolean', value: 'boolean' },
      ],
      defaultValue: ['int', 'boolean'], // Multiple defaults
      // defaultValue: 'string,double,int', // 也可使use这种方式
      placeholder: 'Multiple choice',
    },
    {
      title: 'drop down box_search',
      key: 'select_search',
      type: JVxeTypes.selectSearch,
      width: 180,
      options: [
        { title: 'String', value: 'string' },
        { title: 'Integer', value: 'int' },
        { title: 'Double', value: 'double' },
        { title: 'Boolean', value: 'boolean' },
      ],
    },
    {
      title: 'date time',
      key: 'datetime',
      type: JVxeTypes.datetime,
      width: 200,
      defaultValue: '2019-04-30 14:52:22',
      placeholder: 'Please select',
    },
    {
      title: 'time',
      key: 'time',
      type: JVxeTypes.time,
      width: 200,
      defaultValue: '14:52:22',
      placeholder: 'Please select',
    },
    {
      title: 'checkbox',
      key: 'checkbox',
      type: JVxeTypes.checkbox,
      width: 100,
      customValue: ['Y', 'N'], // true ,false
      defaultChecked: false,
    },
    {
      title: 'operate',
      key: 'action',
      type: JVxeTypes.slot,
      fixed: 'right',
      minWidth: 120,
      align: 'center',
      slotName: 'myAction',
    },
  ]);
  const dataSource = ref<any[]>([]);

  /* Randomly generate data */
  function randomPage(current, pageSize, isLoading = false) {
    if (isLoading) {
      loading.value = true;
    }

    let randomDatetime = () => {
      let time = random(1000, 9999999999999);
      return dayjs(new Date(time)).format('YYYY-MM-DD HH:mm:ss');
    };

    let limit = (current - 1) * pageSize;

    let options = ['string', 'int', 'double', 'boolean'];

    let begin = Date.now();
    let values: any[] = [];
    for (let i = 0; i < pageSize; i++) {
      values.push({
        id: buildUUID(),
        noEdit: `noEdit-${limit + i + 1}`,
        input: `text-${limit + i + 1}`,
        textarea: `textarea-${limit + i + 1}`,
        number: random(0, 233),
        select: options[random(0, 3)],
        select_dict: random(1, 2).toString(),
        select_multiple: (() => {
          let length = random(1, 4);
          let arr = [];
          for (let j = 0; j < length; j++) {
            pushIfNotExist(arr, options[random(0, 3)]);
          }
          return arr.join(',');
        })(),
        select_search: options[random(0, 3)],
        datetime: randomDatetime(),
        checkbox: ['Y', 'N'][random(0, 1)],
      });
    }

    dataSource.value = values;
    let end = Date.now();
    let diff = end - begin;

    if (isLoading && diff < pageSize) {
      setTimeout(() => (loading.value = false), pageSize - diff);
    }
  }

  randomPage(0, 20, true);

  function onLookRow(props) {
    createMessage.success('请在控制台Check输出');
    // Parameter introduction：
    // props.value          value of current cell
    // props.row            The data of the current row
    // props.rowId          current rowID
    // props.rowIndex       current row下标
    // props.column         Current column configuration
    // props.columnIndex    current column index
    // props.$table         vxeExample，可以调usevxebuilt-in methods
    // props.target         JVXEExample，可以调useJVXEbuilt-in methods
    // props.caseId         JVXEExample唯一ID
    // props.scrolling      is scrolling
    // props.triggerChange  triggerchangeevent，use于更改slotvalue
    console.log('Check: ', { props });
  }

  // async function onDeleteRow(props) {
  //   // 同步调usedelete方法
  //   const res = await tableRef.value?.removeRows(props.row);
  //   if (res && res.rows.length > 0) {
  //     createMessage.success('delete成功');
  //   }
  // }

  async function onDeleteRow(props) {
    // 异步调usedelete方法
    const res = await tableRef.value?.removeRows(props.row, true);
    console.log('delete成功~', res);
  }

  function handleValueChange(event) {
    console.log('handleValueChange.event: ', event);
  }

  // update-begin--author:liaozhiyang---date:20230817---for：【issues/636】JVxeTableplusblurevent
  function handleBlur(event){
    console.log("blur",event);
  }
  // update-end--author:liaozhiyang---date:20230817---for：【issues/636】JVxeTableplusblurevent
  /** form validation */
  function handleTableCheck() {
    tableRef.value!.validateTable().then((errMap) => {
      if (errMap) {
        console.log('form validation未通过：', { errMap });
        createMessage.error('Verification failed，请在控制台Check详细');
      } else {
        createMessage.success('Verification passed');
      }
    });
  }

  /** Get value，忽略form validation */
  function onGetData() {
    const values = tableRef.value!.getTableData();
    console.log('Get value:', { values });
    createMessage.success('Get value成功，Please see the console output');
  }

  /** Simulate loading1000piece of data */
  function handleTableSet() {
    randomPage(1, 1000, true);
  }

  function onDelFirst() {
    const xTable = tableRef.value!.getXTable();
    const record = xTable.getTableData().fullData[0];
    tableRef.value!.removeRows(record);
  }

  function onDelSel() {
    const xTable = tableRef.value!.getXTable();
    xTable.removeCheckboxRow();
  }

  function onGetSelData() {
    createMessage.info('Please see the console');
    console.log(tableRef.value!.getSelectionData());
  }

  function onClearSel() {
    tableRef.value!.clearSelection();
  }

  function onToggleLoading() {
    loading.value = !loading.value;
  }

  function onToggleDisabled() {
    disabled.value = !disabled.value;
  }

  function doDelete(deleteRows) {
    let rowId;
    return new Promise((resolve) => {
      if (Array.isArray(deleteRows)) {
        rowId = deleteRows.filter((row) => row.id);
      } else {
        rowId = deleteRows.id;
      }
      console.log('delete rowId: ', rowId);
      setTimeout(() => resolve(true), 1500);
    });
  }

  /** 异步delete示例 */
  async function onJVxeRemove(event) {
    const hideLoading = createMessage.loading('delete中…', 0);
    try {
      // 1. Pass to background event.deleteRows 以delete
      let flag = await doDelete(event.deleteRows);
      if (flag) {
        // Note：如果启use了表格的 loading state，则必须先停止再delete，否则会导致无法从表格上delete数据
        // 2. 调use event.confirmRemove 方法确认delete成功
        // await tableRef.value!.removeSelection();
        await event.confirmRemove()
        createMessage.success('delete成功！');
      } else {
        // 3. 若delete失败，不调use event.confirmRemove() 方法就不会delete数据
        createMessage.warn('delete失败！');
      }
    } finally {
      hideLoading();
    }
  }
</script>
