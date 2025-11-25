import { BasicColumn, FormSchema } from '/@/components/Table';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';

export const columns: BasicColumn[] = [
  {
    title: 'Rule name',
    dataIndex: 'ruleName',
    width: 200,
    align: 'center',
  },
  {
    title: 'Rule encoding',
    dataIndex: 'ruleCode',
    width: 200,
    align: 'center',
  },
  {
    title: 'Rule implementation class',
    dataIndex: 'ruleClass',
    width: 300,
    align: 'center',
  },
  {
    title: 'Rule parameters',
    dataIndex: 'ruleParams',
    width: 200,
    align: 'center',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'ruleName',
    label: 'Rule name',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'ruleCode',
    label: 'Rule encoding',
    component: 'Input',
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
    field: 'ruleName',
    label: 'Rule name',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'ruleCode',
    label: 'Rule encoding',
    component: 'Input',
    colProps: { span: 24 },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (!value) {
                return reject('请输入Rule encoding！');
              }
              let params = {
                tableName: 'sys_fill_rule',
                fieldName: 'rule_code',
                fieldVal: value,
                dataId: model.id,
              };
              duplicateCheckDelay(params)
                .then((res) => {
                  res.success ? resolve() : reject('Rule encoding已存在!');
                })
                .catch((err) => {
                  reject(err.message || 'Verification failed');
                });
            });
          },
        },
      ];
    },
  },
  {
    field: 'ruleClass',
    label: 'Rule implementation class',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'ruleParams',
    label: 'Rule parameters',
    colProps: { span: 24 },
    component: 'JAddInput',
    componentProps: {
      min: 0,
    },
  },
];
