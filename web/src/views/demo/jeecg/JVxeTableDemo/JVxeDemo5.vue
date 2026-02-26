<template>
  <div>
    <b>Keyboard shortcut keys：</b>
    <div style="border: 1px solid #cccccc; padding: 8px; width: 740px">
      <pre>
             F2 | if exists，Active cell is in editing state
            Esc | if exists，Cancel cell editing status
              ↑ | if exists，then move to the cell above
              ↓ | if exists，then move to the cell below
              ← | if exists，then move to the cell on the left
              → | if exists，then move to the cell on the right
            Tab | if exists，then move to the right cell；If you move to the last column，then move from the next line to，cycle like this
    Shift + Tab | if exists，then move to the left cell，If you move to the first column，then move from the previous line to，cycle like this
          Enter | if exists，Cancel cell editing and move to the cell below
  Shift + Enter | if exists，Cancel cell editing and move to the cell above</pre
      >
    </div>

    <JVxeTable ref="tableRef" stripe toolbar rowNumber rowSelection keyboardEdit :columns="columns" :dataSource="dataSource"> </JVxeTable>
  </div>
</template>
<script lang="ts">
  import { ref, onMounted, nextTick, defineComponent } from 'vue';
  import { Popconfirm } from 'ant-design-vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';

  export default defineComponent({
    name: 'JVxeDemo5',
    components: { [Popconfirm.name]: Popconfirm },
    setup() {
      const tableRef = ref<JVxeTableInstance>();
      const columns = ref<JVxeColumn[]>([
        {
          title: 'single line of text',
          key: 'input',
          type: JVxeTypes.input,
          width: 220,
          defaultValue: '',
          placeholder: 'Please enter${title}',
        },
        {
          title: 'multiline text',
          key: 'textarea',
          type: JVxeTypes.textarea,
          width: 240,
        },
        {
          title: 'number',
          key: 'number',
          type: JVxeTypes.inputNumber,
          width: 120,
          defaultValue: 32,
        },
        {
          title: 'date time',
          key: 'datetime',
          type: JVxeTypes.datetime,
          width: 240,
          defaultValue: '2019-04-30 14:51:22',
          placeholder: 'Please select',
        },
        {
          title: 'time',
          key: 'time',
          type: JVxeTypes.time,
          width: 220,
          defaultValue: '14:52:22',
          placeholder: 'Please select',
        },
        {
          title: 'drop down box',
          key: 'select',
          type: JVxeTypes.select,
          width: 220,
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
          title: 'checkbox',
          key: 'checkbox',
          type: JVxeTypes.checkbox,
          // width: 100,
          customValue: ['Y', 'N'], // true ,false
          defaultChecked: false,
        },
      ]);
      const dataSource = ref([]);

      function handleView(props) {
        // Parameter introduction：
        // props.value          value of current cell
        // props.row            The data of the current row
        // props.rowId          current rowID
        // props.rowIndex       current row下标
        // props.column         Current column configuration
        // props.columnIndex    current column index
        // props.$table         vxe-tableExample，Can be calledvxe-tablebuilt-in methods
        // props.scrolling      is scrolling
        // props.reloadEffect   Whether the data refresh effect is enabled
        // props.triggerChange  triggerchangeevent，for changesslotvalue
        console.log('props: ', props);
      }

      function handleDelete({ row }) {
        // 使用Example：Delete the current row
        tableRef.value?.removeRows(row);
      }

      onMounted(async () => {
        console.log(tableRef.value);
        await nextTick();
        // Five lines of data are added by default
        tableRef.value!.addRows([{ input: 'input_1' }, { input: 'input_2' }, { input: 'input_3' }, { input: 'input_4' }, { input: 'input_5' }], {
          setActive: false,
        });
      });

      return { tableRef, columns, dataSource, handleView, handleDelete };
    },
  });
</script>
