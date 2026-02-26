import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { dictItemCheck } from './dict.api';
import { rules } from '/@/utils/helper/validator';
import { h } from "vue";

export const columns: BasicColumn[] = [
  {
    title: 'Dictionary name',
    dataIndex: 'dictName',
    width: 240,
  },
  {
    title: 'dictionary encoding',
    dataIndex: 'dictCode',
    width: 240,
  },
  {
    title: 'describe',
    dataIndex: 'description',
    // width: 120
  },
];

export const recycleBincolumns: BasicColumn[] = [
  {
    title: 'Dictionary name',
    dataIndex: 'dictName',
    width: 120,
  },
  {
    title: 'dictionary encoding',
    dataIndex: 'dictCode',
    width: 120,
  },
  {
    title: 'describe',
    dataIndex: 'description',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Dictionary name',
    field: 'dictName',
    component: 'JInput',
    colProps: { span: 6 },
  },
  {
    label: 'dictionary encoding',
    field: 'dictCode',
    component: 'JInput',
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
    label: 'Dictionary name',
    field: 'dictName',
    required: true,
    component: 'Input',
  },
  {
    label: 'dictionary encoding',
    field: 'dictCode',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_dict', 'dict_code', model, schema, true),
  },
  {
    label: 'describe',
    field: 'description',
    component: 'Input',
  },
];

export const dictItemColumns: BasicColumn[] = [
  {
    title: 'name',
    dataIndex: 'itemText',
    width: 80,
  },
  {
    title: 'data value',
    dataIndex: 'itemValue',
    width: 80,
  },
  {
    title: 'Dictionary colors',
    dataIndex: 'itemColor',
    width: 80,
    align:'center',
    customRender:({ text }) => {
      return h('div', {
        style: {"background": text, "width":"18px","height":"18px","border-radius":"50%","margin":"0 auto"}
      })
    }
  },
];

export const dictItemSearchFormSchema: FormSchema[] = [
  {
    label: 'name',
    field: 'itemText',
    component: 'Input',
  },
  {
    label: 'state',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];

export const itemFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'name',
    field: 'itemText',
    required: true,
    component: 'Input',
  },
  {
    label: 'data value',
    field: 'itemValue',
    component: 'Input',
    dynamicRules: ({ values, model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('请输入data value');
            }
            if (new RegExp("[`~!@#$^&*()=|{}'.<>《》/?！￥（）—【】‘；：”“。，、？]").test(value)) {
              return Promise.reject('data value不能包含特殊字符！');
            }
            return new Promise<void>((resolve, reject) => {
              let params = {
                dictId: values.dictId,
                id: model.id,
                itemValue: value,
              };
              dictItemCheck(params)
                .then((res) => {
                  res.success ? resolve() : reject(res.message || 'Verification failed');
                })
                .catch((err) => {
                  reject(err.message || 'Authentication failed');
                });
            });
          },
        },
      ];
    },
  },
  {
    label: 'color value',
    field: 'itemColor',
    component: 'Input',
    slot:'itemColor'
  },
  {
    label: 'describe',
    field: 'description',
    component: 'Input',
  },
  {
    field: 'sortOrder',
    label: 'sort',
    component: 'InputNumber',
    defaultValue: 1,
  },
  {
    field: 'status',
    label: 'Whether to enable',
    defaultValue: 1,
    component: 'JDictSelectTag',
    componentProps: {
      type: 'radioButton',
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];
