import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'routingID',
    dataIndex: 'routerId',
    width: 200,
    align: 'left',
  },
  {
    title: 'routing名称',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: 'routingURI',
    dataIndex: 'uri',
    width: 200,
  },
  {
    title: 'state',
    dataIndex: 'status',
    slots: { customRender: 'status' },
    width: 150,
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'routingID',
    component: 'Input',
    required: true,
  },
  {
    field: 'name',
    label: 'routing名称',
    component: 'InputNumber',
    required: true,
  },
  {
    field: 'uri',
    label: 'routingURI',
    component: 'Input',
  },
  {
    field: 'predicates',
    label: 'routing条件',
    slot: 'predicates',
    component: 'Input',
  },
];
