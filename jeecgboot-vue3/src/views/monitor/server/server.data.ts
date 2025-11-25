import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'parameter',
    dataIndex: 'param',
    width: 80,
    align: 'left',
    slots: { customRender: 'param' },
  },
  {
    title: 'describe',
    dataIndex: 'text',
    slots: { customRender: 'text' },
    width: 80,
  },
  {
    title: 'current value',
    dataIndex: 'value',
    slots: { customRender: 'value' },
    width: 80,
  },
];
