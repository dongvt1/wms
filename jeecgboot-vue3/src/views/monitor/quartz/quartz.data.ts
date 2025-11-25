import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { JCronValidator } from '/@/components/Form';

export const columns: BasicColumn[] = [
  {
    title: 'Task class name',
    dataIndex: 'jobClassName',
    width: 200,
    align: 'left',
  },
  {
    title: 'Cronexpression',
    dataIndex: 'cronExpression',
    width: 200,
  },
  {
    title: 'parameter',
    dataIndex: 'parameter',
    width: 200,
  },
  {
    title: 'describe',
    dataIndex: 'description',
    width: 200,
  },
  {
    title: 'state',
    dataIndex: 'status',
    width: 100,
    customRender: ({ text }) => {
      const color = text == '0' ? 'green' : text == '-1' ? 'red' : 'gray';
      return render.renderTag(render.renderDict(text, 'quartz_status'), color);
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'jobClassName',
    label: 'Task class name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: '任务state',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'quartz_status',
      stringToNumber: true,
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'jobClassName',
    label: 'Task class name',
    component: 'Input',
    required: true,
  },
  {
    field: 'cronExpression',
    label: 'Cronexpression',
    component: 'JEasyCron',
    defaultValue: '* * * * * ? *',
    rules: [{ required: true, message: 'Please enterCronexpression' }, { validator: JCronValidator }],
  },
  {
    field: 'paramterType',
    label: 'parameter类型',
    component: 'Select',
    defaultValue: 'string',
    componentProps: {
      options: [
        { label: 'string', value: 'string' },
        { label: 'JSONobject', value: 'json' },
      ],
    },
  },
  {
    field: 'parameter',
    label: 'parameter',
    component: 'InputTextArea',
    ifShow: ({ values }) => {
      return values.paramterType == 'string';
    },
  },
  {
    field: 'parameter',
    label: 'parameter',
    component: 'JAddInput',
    helpMessage: 'Fill in the key-value pair form',
    ifShow: ({ values }) => {
      return values.paramterType == 'json';
    },
  },
  {
    field: 'status',
    label: 'state',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'quartz_status',
      type: 'radioButton',
      stringToNumber: true,
      dropdownStyle: {
        maxHeight: '6vh',
      },
    },
  },
  {
    field: 'description',
    label: 'describe',
    component: 'InputTextArea',
  },
];
