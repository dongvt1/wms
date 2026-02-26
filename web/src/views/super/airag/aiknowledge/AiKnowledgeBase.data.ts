import { FormSchema } from '@/components/Form';
import { BasicColumn } from '@/components/Table';

/**
 * Form
 */
export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Knowledge Base Name',
    field: 'name',
    required: true,
    componentProps: {
      placeholder: 'Please enter knowledge base name',
      //Whether to show character count
      showCount: true,
      maxlength: 64,
    },
    component: 'Input',
  },
  {
    label: 'Knowledge Base Description',
    field: 'descr',
    component: 'InputTextArea',
    componentProps: {
      placeholder: 'Describe the content of the knowledge base. Detailed descriptions will help AI deeply understand the content, retrieve content more accurately, and improve the hit rate of this knowledge base.',
      //Whether to show character count
      showCount: true,
      maxlength: 256,
    },
  },
  {
    label: 'Vector Model',
    field: 'embedId',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "airag_model where model_type = 'EMBED' and activate_flag = 1,name,id",
    },
  },
  {
    label: 'Status',
    field: 'status',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      options: [
        { label: 'Enable', value: 'enable' },
        { label: 'Disable', value: 'disable' },
      ],
      type: 'radioButton',
    },
    defaultValue: 'enable',
  },
];

//Document text form
export const docTextSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Knowledge Base ID',
    field: 'knowledgeId',
    show: false,
    component: 'Input',
  },
  {
    label: 'Title',
    field: 'title',
    required: true,
    component: 'Input',
  },
  {
    label: 'Type',
    field: 'type',
    required: true,
    component: 'Input',
    show: false
  },
  {
    label: 'Content',
    field: 'content',
    rules: [{ required: true, message: 'Please enter content' }],
    component: 'JMarkdownEditor',
    componentProps: {
      placeholder: "Please enter content",
      preview:{ mode: 'view', action: [] }
    },
    ifShow:({ values})=>{
      if(values.type === 'text'){
        return true;
      }
      return false;
    }
  },
  {
    label: 'File',
    field: 'filePath',
    rules: [{ required: true, message: 'Please upload file' }],
    component: 'JUpload',
    helpMessage:'Supports txt, markdown, pdf, docx, xlsx, pptx',
    componentProps:{
      fileType: 'file',
      maxCount: 1,
      multiple: false,
      text: 'Upload Document'
    },
    ifShow:({ values })=>{
      if(values.type === 'file'){
        return true;
      }
      return false;
    }
  },
];

