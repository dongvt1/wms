import { FormSchema } from '/@/components/Form/index';
import { rules } from '/@/utils/helper/validator';

export interface ListItem {
  key: string;
  title: string;
  description: string;
  extra?: string;
  avatar?: string;
  color?: string;
}

// taboflist
export const settingList = [
  {
    key: '1',
    name: 'Basic settings',
    component: 'BaseSetting',
  },
  {
    key: '2',
    name: 'Security settings',
    component: 'SecureSetting',
  },
  /* {
    key: '3',
    name: 'Account binding',
    component: 'AccountBind',
  },
  {
    key: '4',
    name: 'New message notification',
    component: 'MsgNotify',
  },*/
];

// Basic settings form
export const baseSetschemas: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'realname',
    component: 'Input',
    label: 'Nick name',
    colProps: { span: 18 },
  },
  {
    field: 'sex',
    label: 'gender',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '请选择gender',
      stringToNumber: true,
    },
    colProps: { span: 18 },
  },
  {
    label: 'Birthday',
    field: 'birthday',
    component: 'DatePicker',
    colProps: { span: 18 },
  },
  {
    field: 'email',
    component: 'Input',
    label: 'Mail',
    colProps: { span: 18 },
  },
  {
    field: 'phone',
    component: 'Input',
    label: 'Contact number',
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, false)[0] },
        { pattern: /^1[3456789]\d{9}$/, message: 'Mobile number format is wrong' },
      ];
    },
    colProps: { span: 18 },
  },
];

// Security settings list
export const secureSettingList: ListItem[] = [
  {
    key: '1',
    title: 'Account password',
    description: 'Current password strength：：powerful',
    extra: 'Revise',
  },
  {
    key: '2',
    title: 'Security mobile phone',
    description: 'Mobile phone bound：：138****8293',
    extra: 'Revise',
  },
  {
    key: '3',
    title: 'Security issues',
    description: '未设置Security issues，Security issues可有效保护账户安全',
    extra: 'Revise',
  },
  {
    key: '4',
    title: '备用Mail',
    description: '已bindingMail：：ant***sign.com',
    extra: 'Revise',
  },
  {
    key: '5',
    title: 'MFA equipment',
    description: 'Not bound MFA equipment，After binding，A second confirmation is possible',
    extra: 'Revise',
  },
];

// Account binding list
export const accountBindList: ListItem[] = [
  {
    key: '1',
    title: 'Bind Taobao',
    description: '当前Not bound淘宝账号',
    extra: 'binding',
    avatar: 'ri:taobao-fill',
    color: '#ff4000',
  },
  {
    key: '2',
    title: 'binding支付宝',
    description: '当前Not bound支付宝账号',
    extra: 'binding',
    avatar: 'fa-brands:alipay',
    color: '#2eabff',
  },
  {
    key: '3',
    title: 'binding钉钉',
    description: '当前Not bound钉钉账号',
    extra: 'binding',
    avatar: 'ri:dingding-fill',
    color: '#2eabff',
  },
];

// New message notification list
export const msgNotifyList: ListItem[] = [
  {
    key: '1',
    title: 'Account password',
    description: '其他用户of消息将以站内信of形式通知',
  },
  {
    key: '2',
    title: 'System messages',
    description: 'System messages将以站内信of形式通知',
  },
  {
    key: '3',
    title: 'To-do tasks',
    description: 'To-do tasks将以站内信of形式通知',
  },
];
