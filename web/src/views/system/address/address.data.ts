import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Name',
    dataIndex: 'realname',
    width: 150,
  },
  {
    title: 'Job number',
    dataIndex: 'workNo',
    width: 100,
  },
  {
    title: 'department',
    dataIndex: 'orgCodeTxt',
    width: 200,
  },
  {
    title: 'main post',
    dataIndex: 'mainDepPostId_dictText',
    width: 200,
  },
  {
    title: 'Position',
    dataIndex: 'post',
    width: 150,
    slots: { customRender: 'post' },
  },
  {
    title: 'cell phone',
    width: 150,
    dataIndex: 'phone',
  },
  {
    title: 'Mail',
    width: 150,
    dataIndex: 'email',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Name',
    field: 'realname',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'Job number',
    field: 'workNo',
    component: 'Input',
    colProps: { span: 6 },
  },
];
