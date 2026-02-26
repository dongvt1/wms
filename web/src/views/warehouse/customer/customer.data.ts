import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Form';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// Customer Table Columns
export const customerColumns: BasicColumn[] = [
  {
    title: 'Customer Code',
    dataIndex: 'customerCode',
    width: 120,
    fixed: 'left',
  },
  {
    title: 'Customer Name',
    dataIndex: 'customerName',
    width: 150,
    fixed: 'left',
  },
  {
    title: 'Contact Person',
    dataIndex: 'contactPerson',
    width: 120,
  },
  {
    title: 'Phone',
    dataIndex: 'phone',
    width: 120,
  },
  {
    title: 'Email',
    dataIndex: 'email',
    width: 180,
  },
  {
    title: 'Address',
    dataIndex: 'address',
    width: 250,
    ellipsis: true,
  },
  {
    title: 'Tax Code',
    dataIndex: 'taxCode',
    width: 120,
  },
  {
    title: 'Status',
    dataIndex: 'status_dictText',
    width: 80,
    customRender: ({ record }) => {
      const status = record.status;
      let color = 'default';
      if (status === 1) {
        color = 'green';
      } else if (status === 0) {
        color = 'red';
      }
      return h(Tag, { color }, { default: () => record.status_dictText });
    },
  },
  {
    title: 'Create Time',
    dataIndex: 'createTime',
    width: 150,
  },
];

// Customer Search Form Schema
export const customerSearchFormSchema: FormSchema[] = [
  {
    field: 'customerCode',
    label: 'Customer Code',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'customerName',
    label: 'Customer Name',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'contactPerson',
    label: 'Contact Person',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: 'Status',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Active', value: 1 },
        { label: 'Inactive', value: 0 },
      ],
    },
    colProps: { span: 6 },
  },
];

// Customer Form Schema
export const customerFormSchema: FormSchema[] = [
  {
    field: 'customerCode',
    label: 'Customer Code',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: 'Please enter customer code',
    },
    rules: [
      { required: true, message: 'Please enter customer code' },
      { pattern: /^[A-Z0-9-_]+$/, message: 'Customer code can only contain uppercase letters, numbers, hyphens and underscores' }
    ],
  },
  {
    field: 'customerName',
    label: 'Customer Name',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: 'Please enter customer name',
    },
    rules: [{ required: true, message: 'Please enter customer name' }],
  },
  {
    field: 'contactPerson',
    label: 'Contact Person',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter contact person',
    },
  },
  {
    field: 'phone',
    label: 'Phone',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter phone number',
    },
    rules: [
      { pattern: /^[0-9+\-\s()]+$/, message: 'Please enter a valid phone number' }
    ],
  },
  {
    field: 'email',
    label: 'Email',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter email address',
    },
    rules: [
      { type: 'email', message: 'Please enter a valid email address' }
    ],
  },
  {
    field: 'address',
    label: 'Address',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
      placeholder: 'Please enter address',
    },
  },
  {
    field: 'taxCode',
    label: 'Tax Code',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter tax code',
    },
  },
  {
    field: 'status',
    label: 'Status',
    component: 'RadioButtonGroup',
    defaultValue: 1,
    componentProps: {
      options: [
        { label: 'Active', value: 1 },
        { label: 'Inactive', value: 0 },
      ],
    },
  },
];

// Customer Balance Table Columns
export const customerBalanceColumns: BasicColumn[] = [
  {
    title: 'Customer Code',
    dataIndex: 'customerCode',
    width: 120,
  },
  {
    title: 'Customer Name',
    dataIndex: 'customerName',
    width: 150,
  },
  {
    title: 'Balance',
    dataIndex: 'balance',
    width: 120,
    customRender: ({ text }) => {
      const balance = parseFloat(text);
      if (balance >= 0) {
        return `<span style="color: green">¥${balance.toFixed(2)}</span>`;
      } else {
        return `<span style="color: red">¥${Math.abs(balance).toFixed(2)}</span>`;
      }
    },
  },
  {
    title: 'Last Updated',
    dataIndex: 'lastUpdated',
    width: 150,
  },
  {
    title: 'Updated By',
    dataIndex: 'updatedBy',
    width: 120,
  },
];

// Customer Order History Table Columns
export const customerOrderHistoryColumns: BasicColumn[] = [
  {
    title: 'Order Number',
    dataIndex: 'orderNumber',
    width: 120,
  },
  {
    title: 'Order Date',
    dataIndex: 'orderDate',
    width: 120,
  },
  {
    title: 'Total Amount',
    dataIndex: 'totalAmount',
    width: 120,
    customRender: ({ text }) => `¥${parseFloat(text).toFixed(2)}`,
  },
  {
    title: 'Status',
    dataIndex: 'status',
    width: 100,
    customRender: ({ record }) => {
      const status = record.status;
      let color = 'default';
      if (status === 'COMPLETED') {
        color = 'green';
      } else if (status === 'PENDING') {
        color = 'orange';
      } else if (status === 'CANCELLED') {
        color = 'red';
      }
      return h(Tag, { color }, { default: () => status });
    },
  },
  {
    title: 'Create Time',
    dataIndex: 'createTime',
    width: 150,
  },
];

// Customer Statistics Schema
export const customerStatisticsSchema = [
  {
    field: 'totalOrders',
    label: 'Total Orders',
    component: 'InputNumber',
    componentProps: {
      disabled: true,
      style: { width: '100%' },
    },
  },
  {
    field: 'totalValue',
    label: 'Total Value',
    component: 'InputNumber',
    componentProps: {
      disabled: true,
      style: { width: '100%' },
      formatter: (value) => `¥${value}`,
    },
  },
  {
    field: 'lastOrderDate',
    label: 'Last Order Date',
    component: 'Input',
    componentProps: {
      disabled: true,
      style: { width: '100%' },
    },
  },
];