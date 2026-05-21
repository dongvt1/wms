import { FormSchema } from '@/components/Form';

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
    label: 'Application Name',
    field: 'name',
    required: true,
    componentProps: {
      //Whether to show character count
      showCount: true,
      maxlength: 64,
    },
    component: 'Input',
  },
  {
    label: 'Application Description',
    field: 'descr',
    component: 'InputTextArea',
    componentProps: {
      placeholder: 'Describe the application scenarios and uses of this application',
      rows: 4,
      //Whether to show character count
      showCount: true,
      maxlength: 256,
    },
  },
  {
    label: 'Application Icon',
    field: 'icon',
    component: 'JImageUpload',
  },
  {
    label: 'Select Application Type',
    field: 'type',
    component: 'Input',
    show:({ values })=>{
      return !values.id;
    },
    slot: 'typeSlot',
  },
];

/**
 * Quick Command Form
 */
export const quickCommandFormSchema: FormSchema[] = [
  {
    label: 'key',
    field: 'key',
    component: 'Input',
    show: false,
  },
  {
    label: 'Button Name',
    field: 'name',
    required: true,
    component: 'Input',
    componentProps: {
      showCount: true,
      maxLength: 10,
    },
  },
  {
    label: 'Button Icon',
    field: 'icon',
    component: 'IconPicker',
  },
  {
    label: 'Command Content',
    field: 'descr',
    required: true,
    component: 'InputTextArea',
    componentProps: {
      autosize: { minRows: 4, maxRows: 4 },
      showCount: true,
      maxLength: 100,
    }
  },
];
