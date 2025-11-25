import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: 'Order number',
    dataIndex: 'orderCode',
    width: 260,
  },
  {
    title: 'Order type',
    dataIndex: 'ctype',
    width: 160,
    customRender: ({ text }) => {
      return text == '1' ? 'Domestic orders' : text == '2' ? 'international orders' : '';
    },
  },
  {
    title: 'order date',
    dataIndex: 'orderDate',
    width: 300,
  },
  {
    title: 'Order amount',
    width: 200,
    dataIndex: 'orderMoney',
  },
  {
    title: 'Order notes',
    width: 200,
    dataIndex: 'content',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Order number',
    field: 'orderCode',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'Order type',
    field: 'ctype',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: 'Domestic orders',
          value: '1',
          key: '1',
        },
        {
          label: 'international orders',
          value: '2',
          key: '2',
        },
      ],
    },
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Order number',
    field: 'orderCode',
    component: 'Input',
    required: true,
  },
  {
    label: 'Order type',
    field: 'ctype',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: 'Domestic orders',
          value: '1',
          key: '1',
        },
        {
          label: 'international orders',
          value: '2',
          key: '2',
        },
      ],
    },
  },
  {
    label: 'order date',
    field: 'orderDate',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD hh:mm:ss',
    },
  },
  {
    label: 'Order amount',
    field: 'orderMoney',
    component: 'InputNumber',
  },
  {
    label: 'Order notes',
    field: 'content',
    component: 'Input',
  },
];

export const customColumns: BasicColumn[] = [
  {
    title: 'Customer name',
    dataIndex: 'name',
    width: 260,
  },
  {
    title: 'gender',
    dataIndex: 'sex',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
  {
    title: 'ID number',
    dataIndex: 'idcard',
    width: 300,
  },
  {
    title: 'Telephone',
    width: 200,
    dataIndex: 'telphone',
  },
];

export const customerFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Customer name',
    field: 'name',
    component: 'Input',
    required: true,
  },
  {
    label: 'gender',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '请选择gender',
    },
  },
  {
    label: 'ID number码',
    field: 'idcard',
    component: 'Input',
  },
  {
    label: 'Scanned copy of ID card',
    field: 'idcardPic',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 2,
    },
  },
  {
    label: 'Contact information',
    field: 'telphone',
    component: 'Input',
    rules: [{ required: false, pattern: /^1[3456789]\d{9}$/, message: 'Mobile number format is wrong' }],
  },
  {
    label: 'orderId',
    field: 'orderId',
    component: 'Input',
    show: false,
  },
];

export const ticketColumns: BasicColumn[] = [
  {
    title: 'flight number',
    dataIndex: 'ticketCode',
  },
  {
    title: 'Flight time',
    dataIndex: 'tickectDate',
  },
  {
    title: 'Creator',
    dataIndex: 'createBy',
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
  },
];

export const ticketFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'flight number',
    field: 'ticketCode',
    component: 'Input',
    required: true,
  },
  {
    label: 'Flight time',
    field: 'tickectDate',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      getPopupContainer:()=>document.body,
    },
  },
  {
    label: 'orderId',
    field: 'orderId',
    component: 'Input',
    show: false,
  },
];
