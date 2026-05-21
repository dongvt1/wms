import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: 'title',
    dataIndex: 'titile',
    width: 100,
    align: 'left',
  },
  {
    title: 'Message type',
    dataIndex: 'msgCategory',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: 'Notices and Announcements', value: '1', color: 'blue' },
          { label: 'System messages', value: '2' },
        ],
        true
      );
    },
  },
  {
    title: 'Posted by',
    dataIndex: 'sender',
    width: 80,
  },
  {
    title: 'Release time',
    dataIndex: 'sendTime',
    width: 80,
  },
  {
    title: 'priority',
    dataIndex: 'priority',
    width: 80,
    customRender: ({ text }) => {
      const color = text == 'L' ? 'blue' : text == 'M' ? 'yellow' : 'red';
      return render.renderTag(render.renderDict(text, 'priority'), color);
    },
  },
  {
    title: 'reading status',
    dataIndex: 'readFlag',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: 'unread', value: '0', color: 'red' },
          { label: 'Read', value: '1' },
        ],
        true
      );
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'titile',
    label: 'title',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'sender',
    label: 'Posted by',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'sendTime',
    label: 'Release time',
    component: 'RangeDate',
    componentProps: {
      valueType: 'Date',
    },
    colProps: { span: 6 },
  },
  {
    field: 'msgCategory',
    label: 'Message type',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Notices and Announcements', value: '1' },
        { label: 'System messages', value: '2' },
      ],
    },
    colProps: { span: 6 },
  },
];
