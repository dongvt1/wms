import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Form';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { stockTransactionApi, supplierApi } from './stockTransaction.api';
import { productApi } from '../product/product.api';

// Stock Transaction Table Columns
export const stockTransactionColumns: BasicColumn[] = [
  {
    title: 'Transaction Number',
    dataIndex: 'transactionNumber',
    width: 150,
    fixed: 'left',
  },
  {
    title: 'Transaction Type',
    dataIndex: 'transactionType_dictText',
    width: 120,
    customRender: ({ record }) => {
      const type = record.transactionType;
      let color = 'blue';
      if (type === 'IN') {
        color = 'green';
      } else if (type === 'OUT') {
        color = 'red';
      } else if (type === 'TRANSFER') {
        color = 'orange';
      }
      return h(Tag, { color }, { default: () => record.transactionType_dictText });
    },
  },
  {
    title: 'Status',
    dataIndex: 'status_dictText',
    width: 100,
    customRender: ({ record }) => {
      const status = record.status;
      let color = 'default';
      if (status === 'PENDING') {
        color = 'orange';
      } else if (status === 'APPROVED') {
        color = 'green';
      } else if (status === 'CANCELLED') {
        color = 'red';
      }
      return h(Tag, { color }, { default: () => record.status_dictText });
    },
  },
  {
    title: 'Source Location',
    dataIndex: 'sourceLocationName',
    width: 150,
  },
  {
    title: 'Target Location',
    dataIndex: 'targetLocationName',
    width: 150,
  },
  {
    title: 'Supplier',
    dataIndex: 'supplierName',
    width: 150,
  },
  {
    title: 'Transaction Date',
    dataIndex: 'transactionDate',
    width: 120,
  },
  {
    title: 'Created By',
    dataIndex: 'createByName',
    width: 120,
  },
  {
    title: 'Create Time',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: 'Remarks',
    dataIndex: 'remarks',
    width: 200,
    ellipsis: true,
  },
];

// Stock Transaction Search Form Schema
export const stockTransactionSearchFormSchema: FormSchema[] = [
  {
    field: 'transactionNumber',
    label: 'Transaction Number',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'transactionType',
    label: 'Transaction Type',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Stock In', value: 'IN' },
        { label: 'Stock Out', value: 'OUT' },
        { label: 'Stock Transfer', value: 'TRANSFER' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: 'Status',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Pending', value: 'PENDING' },
        { label: 'Approved', value: 'APPROVED' },
        { label: 'Cancelled', value: 'CANCELLED' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'transactionDate',
    label: 'Transaction Date',
    component: 'RangePicker',
    colProps: { span: 6 },
  },
];

// Stock Transaction Form Schema
export const stockTransactionFormSchema: FormSchema[] = [
  {
    field: 'transactionType',
    label: 'Transaction Type',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: 'Stock In', value: 'IN' },
        { label: 'Stock Out', value: 'OUT' },
        { label: 'Stock Transfer', value: 'TRANSFER' },
      ],
      onChange: (value) => {
        // Handle form field visibility based on transaction type
        return value;
      },
    },
  },
  {
    field: 'sourceLocationId',
    label: 'Source Location',
    component: 'TreeSelect',
    required: true,
    ifShow: ({ values }) => values.transactionType === 'OUT' || values.transactionType === 'TRANSFER',
    componentProps: {
      api: () => Promise.resolve({ records: [] }), // TODO: Replace with actual warehouse location API
      params: { parentId: '0' },
      resultField: 'records',
      labelField: 'name',
      valueField: 'id',
      showSearch: true,
      treeDefaultExpandAll: true,
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'targetLocationId',
    label: 'Target Location',
    component: 'TreeSelect',
    required: true,
    ifShow: ({ values }) => values.transactionType === 'IN' || values.transactionType === 'TRANSFER',
    componentProps: {
      api: () => Promise.resolve({ records: [] }), // TODO: Replace with actual warehouse location API
      params: { parentId: '0' },
      resultField: 'records',
      labelField: 'name',
      valueField: 'id',
      showSearch: true,
      treeDefaultExpandAll: true,
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'supplierId',
    label: 'Supplier',
    component: 'Select',
    required: true,
    ifShow: ({ values }) => values.transactionType === 'IN',
    componentProps: {
      api: async () => {
        const result = await supplierApi.getActive();
        return { records: result };
      },
      params: { status: 1 },
      resultField: 'records',
      labelField: 'name',
      valueField: 'id',
      showSearch: true,
      optionFilterProp: 'label',
    },
  },
  {
    field: 'transactionDate',
    label: 'Transaction Date',
    component: 'DatePicker',
    required: true,
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      style: { width: '100%' },
    },
  },
  {
    field: 'remarks',
    label: 'Remarks',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: 'Please enter remarks',
    },
  },
];

// Stock Transaction Item Table Columns
export const stockTransactionItemColumns: BasicColumn[] = [
  {
    title: 'Product Code',
    dataIndex: 'productCode',
    width: 120,
  },
  {
    title: 'Product Name',
    dataIndex: 'productName',
    width: 200,
  },
  {
    title: 'Specification',
    dataIndex: 'productSpec',
    width: 150,
  },
  {
    title: 'Unit',
    dataIndex: 'productUnit',
    width: 80,
  },
  {
    title: 'Quantity',
    dataIndex: 'quantity',
    width: 100,
  },
  {
    title: 'Unit Price',
    dataIndex: 'unitPrice',
    width: 100,
    customRender: ({ text }) => {
      return text ? `¥${text}` : '';
    },
  },
  {
    title: 'Total Amount',
    dataIndex: 'totalAmount',
    width: 120,
    customRender: ({ text }) => {
      return text ? `¥${text}` : '';
    },
  },
  {
    title: 'Batch Number',
    dataIndex: 'batchNumber',
    width: 120,
  },
  {
    title: 'Expiry Date',
    dataIndex: 'expiryDate',
    width: 120,
  },
  {
    title: 'Remarks',
    dataIndex: 'remarks',
    width: 200,
    ellipsis: true,
  },
];

// Stock Transaction Item Form Schema
export const stockTransactionItemFormSchema: FormSchema[] = [
  {
    field: 'productId',
    label: 'Product',
    component: 'Select',
    required: true,
    componentProps: {
      api: async () => {
        const result = await productApi.list({ pageSize: 999 });
        return result;
      },
      params: { status: 1 },
      resultField: 'records',
      labelField: 'name',
      valueField: 'id',
      showSearch: true,
      optionFilterProp: 'label',
      onChange: (value, options) => {
        // Auto-fill product details
        return {
          productCode: options?.code,
          productSpec: options?.spec,
          productUnit: options?.unit,
        };
      },
    },
  },
  {
    field: 'quantity',
    label: 'Quantity',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 1,
      precision: 0,
      style: { width: '100%' },
    },
  },
  {
    field: 'unitPrice',
    label: 'Unit Price',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      style: { width: '100%' },
      formatter: (value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ','),
      parser: (value) => value.replace(/¥\s?|(,*)/g, ''),
    },
  },
  {
    field: 'batchNumber',
    label: 'Batch Number',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter batch number',
    },
  },
  {
    field: 'expiryDate',
    label: 'Expiry Date',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      style: { width: '100%' },
    },
  },
  {
    field: 'remarks',
    label: 'Remarks',
    component: 'InputTextArea',
    componentProps: {
      rows: 2,
      placeholder: 'Please enter remarks',
    },
  },
];

// Supplier Table Columns
export const supplierColumns: BasicColumn[] = [
  {
    title: 'Supplier Code',
    dataIndex: 'supplierCode',
    width: 120,
    fixed: 'left',
  },
  {
    title: 'Supplier Name',
    dataIndex: 'supplierName',
    width: 200,
    fixed: 'left',
  },
  {
    title: 'Contact Person',
    dataIndex: 'contactPerson',
    width: 120,
  },
  {
    title: 'Contact Phone',
    dataIndex: 'contactPhone',
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

// Supplier Search Form Schema
export const supplierSearchFormSchema: FormSchema[] = [
  {
    field: 'supplierCode',
    label: 'Supplier Code',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'supplierName',
    label: 'Supplier Name',
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

// Supplier Form Schema
export const supplierFormSchema: FormSchema[] = [
  {
    field: 'supplierCode',
    label: 'Supplier Code',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: 'Please enter supplier code',
    },
  },
  {
    field: 'supplierName',
    label: 'Supplier Name',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: 'Please enter supplier name',
    },
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
    field: 'contactPhone',
    label: 'Contact Phone',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter contact phone',
    },
  },
  {
    field: 'email',
    label: 'Email',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter email',
    },
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