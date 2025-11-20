import { BasicColumn, FormSchema } from '@/components/Table';

//OCR table
export const columns: BasicColumn[] = [
  {
    title: 'ID',
    dataIndex: 'id',
    ifShow: false,
  },
  {
    title: 'Title',
    dataIndex: 'title',
    ellipsis: true,
    width: 300,
  },
  {
    title: 'Prompt',
    dataIndex: 'prompt',
    ellipsis: true,
    width: 300,
  },
];

//OCR form
export const schemas: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Title',
    field: 'title',
    component: 'Input',
    required: true,
  },
  {
    label: 'Prompt',
    field: 'prompt',
    component: 'InputTextArea',
    componentProps: {
      row: 4,
      autosize: { minRows: 4, maxRows: 6 },
    },
    required: true,
  },
];

//OCR analysis form
export const analysisSchemas: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Image',
    field: 'url',
    component: 'JImageUpload',
    required: true,
  },
  {
    label: 'Prompt',
    field: 'prompt',
    component: 'InputTextArea',
    show:false,
  },
  {
    label: 'Analysis Result',
    field: 'analysisResult',
    component: 'InputTextArea',
    componentProps: {
      row: 10,
      autosize: { minRows: 10, maxRows: 10 },
      readonly: true,
      placeholder:"Analysis results will be displayed here"
    },
  },
];
