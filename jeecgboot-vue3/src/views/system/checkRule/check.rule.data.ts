import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { validateCheckRule } from '/@/views/system/checkRule/check.rule.api';
import { array } from 'vue-types';

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
    title: 'Rule description',
    dataIndex: 'ruleDescription',
    width: 300,
    align: 'center',
    customRender: function ({ text }) {
      return render.renderTip(text, 30);
    },
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
                tableName: 'sys_check_rule',
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
    field: 'ruleDescription',
    label: 'Rule description',
    colProps: { span: 24 },
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入Rule description',
      rows: 2,
    },
  },
];

export const checkRuleInput: FormSchema[] = [
  {
    label: '123',
    field: 'ruleCode',
    component: 'Input',
    show: false,
  },
  {
    field: 'testValue',
    label: 'The value to be tested:',
    component: 'Input',
    componentProps: ({ formModel }) => {
      return {
        onChange: (e) => {
          formModel.testValue = e.target.value;
        },
      };
    },
    dynamicRules: ({ model }) => {
      const { ruleCode } = model;
      return [
        {
          required: false,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (ruleCode && value) {
                /*console.log({ruleCode,value})*/
                validateCheckRule(ruleCode, value)
                  .then((res) => {
                    //console.log(1233, res)
                    res['success'] ? resolve() : reject(res['message']);
                  })
                  .catch((err) => {
                    reject(err.message || err);
                  });
              } else {
                resolve();
              }
            });
          },
        },
      ];
    },
  },
];
