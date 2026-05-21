import { FormSchema } from '/@/components/Form';

import dayjs from 'dayjs';

export const schemas: FormSchema[] = [
  {
    label: 'text box',
    field: 'name',
    component: 'Input',
    componentProps: {
      prefix: 'Chinese',
      showCount: true,
    },
    defaultValue: 'Zhang San',
  },
  {
    label: 'password',
    field: 'password',
    component: 'InputPassword',
    componentProps: {
      //是否显示切换按钮或者控制password显隐
      visibilityToggle: true,
      prefix: 'password',
    },
  },
  {
    label: 'search box',
    field: 'searchBox',
    component: 'InputSearch',
    componentProps: {
      onSearch: (value) => {
        console.log(value);
      },
    },
  },
  {
    label: 'text field',
    field: 'textArea',
    component: 'InputTextArea',
    componentProps: {
      //You can click the clear icon to delete content
      allowClear: true,
      //Whether to display word count
      showCount: true,
      //Adaptive content height，Can be set to true | false or object：{ minRows: 2, maxRows: 6 }
      autoSize: {
        //Minimum number of display lines
        minRows: 2,
        //Maximum number of displayed lines
        maxRows: 3,
      },
    },
  },
  {
    label: 'Numeric input box',
    field: 'number',
    component: 'InputNumber',
    componentProps: {
      //tagged input，Set post label
      addonAfter: 'Keep to two decimal places',
      //maximum value
      max: 100,
      //numerical longitude
      precision: 2,
      //number of steps
      step: 0.1,
    },
  },

  {
    label: 'drop down box',
    field: 'jinputtype',
    component: 'Select',
    componentProps: {
      options: [
        { value: 'like', label: 'Vague（like）' },
        { value: 'ne', label: 'not equal to（ne）' },
        { value: 'ge', label: 'Greater than or equal to（ge）' },
        { value: 'le', label: 'less than or equal to（le)' },
      ],
      //Drop-down multiple selection
      mode: 'multiple',
      //Is the configuration searchable?
      showSearch: true,
    },
  },
  {
    field: 'TreeSelect',
    label: 'Drop down tree',
    component: 'TreeSelect',
    componentProps: {
      //是否显示drop down box，defaultfalse
      treeCheckable: true,
      //title
      title: 'Drop down tree',
      //Drop down tree
      treeData: [
        {
          label: 'washing machine',
          value: '0',
          children: [
            {
              label: '滚筒washing machine',
              value: '0-1',
            },
          ],
        },
        {
          label: 'TV set',
          value: '1',
          children: [
            {
              label: 'flat screen tv',
              value: '1-1',
              disabled: true,
            },
            {
              label: 'CRTTV set',
              value: '1-2',
            },
            {
              label: 'projection tv',
              value: '1-3',
            },
          ],
        },
      ],
    },
  },
  {
    label: 'RadioButtonGroupcomponents',
    field: 'status',
    component: 'RadioButtonGroup',
    componentProps: {
      options: [
        { label: 'efficient', value: 1 },
        { label: 'invalid', value: 0 },
      ],
    },
  },
  {
    label: 'radio button',
    field: 'radioSex',
    component: 'RadioGroup',
    componentProps: {
      //optionsThere are one by one insideradiocomposition,supportdisabledDisable
      options: [
        { label: 'male', value: 1, disabled: false },
        { label: 'female', value: 0 },
      ],
    },
  },
  {
    label: 'checkbox',
    field: 'checkbox',
    component: 'Checkbox',
    componentProps: {
      //是否Disable,defaultfalse
      disabled: false,
    },
  },
  {
    label: 'checkbox组',
    field: 'checkSex',
    component: 'CheckboxGroup',
    componentProps: {
      //RadioGroup Download all input[type="radio"] of name property
      name: 'Hobby',
      //optionssupportdisabledDisable
      options: [
        { label: 'sports', value: 0, disabled: true },
        { label: 'listen to music', value: 1 },
        { label: 'read a book', value: 2 },
      ],
    },
    defaultValue: [2],
  },
  {
    label: '自动完成components',
    field: 'AutoComplete',
    component: 'AutoComplete',
    componentProps: {
      options: [{ value: 'Burns Bay Road' }, { value: 'Downing Street' }, { value: 'Wall Street' }],
    },
  },
  {
    label: 'Cascade selection',
    field: 'cascade',
    component: 'Cascader',
    componentProps: {
      //How many are displayed at most?tag
      maxTagCount: 2,
      //Floating layer default position
      placement: 'bottomRight',
      //在选择框中显示search box,defaultfalse
      showSearch: true,
      options: [
        {
          label: 'Beijing',
          value: 'BeiJin',
          children: [
            {
              label: 'Haidian District',
              value: 'HaiDian',
            },
          ],
        },
        {
          label: 'Jiangsu Province',
          value: 'JiangSu',
          children: [
            {
              label: 'Nanjing',
              value: 'Nanjing',
              children: [
                {
                  label: 'china gate',
                  value: 'ZhongHuaMen',
                },
              ],
            },
          ],
        },
      ],
    },
  },
  {
    label: 'date selection',
    field: 'dateSelect',
    component: 'DatePicker',
    componentProps: {
      //date formatting，页面上显示of值
      format: 'YYYY-MM-DD',
      //Return value formatting（绑定值of格式）
      valueFormat: 'YYYY-MM-DD',
      //Whether to show today button
      showToday: true,
      //Date cannot be selected
      disabledDate: (currentDate) => {
        let date = dayjs(currentDate).format('YYYY-MM-DD');
        let nowDate = dayjs(new Date()).format('YYYY-MM-DD');
        //Not available on the day
        if (date == nowDate) {
          return true;
        }
        return false;
      },
    },
  },
  {
    label: 'Month selection',
    field: 'monthSelect',
    component: 'MonthPicker',
    componentProps: {
      //Date cannot be selected
      disabledDate: (currentDate) => {
        let date = dayjs(currentDate).format('YYYY-MM');
        let nowDate = dayjs(new Date()).format('YYYY-MM');
        //Not available on the day
        if (date == nowDate) {
          return true;
        }
        return false;
      },
    },
  },
  {
    label: 'Weekly selection',
    field: 'weekSelect',
    component: 'WeekPicker',
    componentProps: {
      size: 'small',
    },
  },
  {
    label: 'Time selection',
    field: 'timeSelect',
    component: 'TimePicker',
    componentProps: {
      size: 'default',
      //Whether to display the current moment in date time or time mode，不supportdate time range和time range
      showNow: true,
    },
  },
  {
    label: 'date time range',
    field: 'dateTimeRangeSelect',
    component: 'RangePicker',
    componentProps: {
      //Whether to display time
      showTime: true,
      //date formatting
      format: 'YYYY/MM/DD HH:mm:ss',
      //Collection of range text descriptions
      placeholder: ['Please select a start date and time', 'Please select an end date and time'],
    },
  },
  {
    label: 'date range',
    field: 'dateRangeSelect',
    component: 'RangeDate',
    componentProps: {
      //date formatting
      format: 'YYYY/MM/DD',
      //Collection of range text descriptions
      placeholder: ['Please select a start date', 'Please select an end date'],
    },
  },
  {
    label: 'time range',
    field: 'timeRangeSelect',
    component: 'RangeTime',
    componentProps: {
      //date formatting
      format: 'HH/mm/ss',
      //Collection of range text descriptions
      placeholder: ['Please select a start time', 'Please select end time'],
    },
  },
  {
    label: 'switch',
    field: 'switch',
    component: 'Switch',
    componentProps: {
      //switch大小，Optional value：default small
      size: 'default',
      //非选中时of内容
      unCheckedChildren: 'turn on',
      //非选中时of值
      unCheckedValue: '0',
      //选中时of内容
      checkedChildren: 'closure',
      //选中时of值
      checkedValue: '1',
      //是否Disable
      disabled: false,
    },
  },
  {
    label: 'sliding input bar',
    field: 'slider',
    component: 'Slider',
    componentProps: {
      //minimum value
      min: -20,
      //maximum value
      max: 100,
      //Whether it is dual slider mode
      range: true,
      //tick marks
      marks: {
        '-20': '-20°C',
        0: '0°C',
        26: '26°C',
        37: '37°C',
        100: {
          style: {
            color: '#f50',
          },
          label: '100°C',
        },
      },
    },
  },
  {
    label: 'score',
    field: 'rate',
    component: 'Rate',
    componentProps: {
      //Whether to allow half selection
      allowHalf: true,
      //star total
      count: 5,
      //tooltiphint，Write how many stars there are
      tooltips: ['very bad', 'Poor', 'normal', 'very good', '非very good'],
    },
  },
  {
    label: 'dividing line',
    field: 'divisionLine',
    component: 'Divider',
    componentProps: {
      //Dotted line or not?
      dashed: false,
      //dividing linetitleof位置（left | right | center）
      orientation: 'center',
      //Whether the text is displayed in normal text style
      plain: true,
      //horizontal or vertical type（horizontal | vertical）
      type: 'horizontal',
    },
  },
];
