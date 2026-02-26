import { FormSchema } from '/@/components/Form';

export const schemas: FormSchema[] = [
  {
    field: 'title',
    component: 'Input',
    label: 'title',
    componentProps: {
      placeholder: 'Give the goal a name',
    },
    required: true,
  },
  {
    field: 'time',
    component: 'RangePicker',
    label: 'Start and end date',
    required: true,
  },
  {
    field: 'target',
    component: 'InputTextArea',
    label: 'Goal description',
    componentProps: {
      placeholder: 'Please enter your phased work goals',
      rows: 4,
    },
    required: true,
  },
  {
    field: 'metrics',
    component: 'InputTextArea',
    label: 'measure',
    componentProps: {
      placeholder: 'Please entermeasure',
      rows: 4,
    },
    required: true,
  },
  {
    field: 'client',
    component: 'Input',
    label: 'client',
    helpMessage: 'target service recipients',
    subLabel: '( Optional )',
    componentProps: {
      placeholder: '请描述你服务的client，内部client直接 @Name／Job number',
    },
  },
  {
    field: 'inviteer',
    component: 'Input',
    label: 'Invite reviewers',
    subLabel: '( Optional )',
    componentProps: {
      placeholder: 'Please direct @Name／Job number，Maximum number of invites 5 people',
    },
  },
  {
    field: 'weights',
    component: 'InputNumber',
    label: 'weight',
    subLabel: '( Optional )',
    componentProps: {
      formatter: (value: string) => (value ? `${value}%` : ''),
      parser: (value: string) => value.replace('%', ''),
      placeholder: 'Please enter',
    },
  },
  {
    field: 'disclosure',
    component: 'RadioGroup',
    label: 'target public',
    itemProps: {
      extra: 'client、Invite reviewers默认被分享',
    },
    componentProps: {
      options: [
        {
          label: 'public',
          value: '1',
        },
        {
          label: '部分public',
          value: '2',
        },
        {
          label: '不public',
          value: '3',
        },
      ],
    },
  },
  {
    field: 'disclosurer',
    component: 'Select',
    label: ' ',
    show: ({ model }) => {
      return model.disclosure === '2';
    },
    componentProps: {
      placeholder: 'public给',
      mode: 'multiple',
      options: [
        {
          label: 'colleague1',
          value: '1',
        },
        {
          label: 'colleague2',
          value: '2',
        },
        {
          label: 'colleague3',
          value: '3',
        },
      ],
    },
  },
];
