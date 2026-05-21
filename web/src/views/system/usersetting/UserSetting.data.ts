import { FormSchema } from '/@/components/Form/index';
import { rules } from '/@/utils/helper/validator';
import anquan1 from './icons/anquan1.png'
import anquan2 from './icons/anquan2.png'
import app1 from './icons/app1.png'
import app2 from './icons/app2.png'
import geren1 from './icons/geren1.png'
import geren2 from './icons/geren2.png'
import zuhu1 from './icons/zuhu1.png'
import zuhu2 from './icons/zuhu2.png'
import { calculateFileSize } from "/@/utils/common/compUtils";
import { BasicColumn } from "@/components/Table";

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
    name: 'personal information',
    component: 'BaseSetting',
    icon:'ant-design:user-outlined',
    img1: geren1,
    img2: geren2,
  },
  {
    key: '2',
    name: '我of组织',
    component: 'TenantSetting',
    isSlot:false,
    icon:'ant-design:team-outlined',
    img1: zuhu1,
    img2: zuhu2,
  },
   {
    key: '3',
    name: 'Account security',
    component: 'AccountSetting',
    icon:'ant-design:lock-outlined',
    img1: anquan1,
    img2: anquan2,
  },
  {
    key: '4',
    name: 'third partyAPP',
    component: 'WeChatDingSetting',
    icon: 'ant-design:contacts-outlined',
    img1: app1,
    img2: app2,
  },
];


/**
 * userform
 */
export const formSchema: FormSchema[] = [
  {
    field: 'realname',
    component: 'Input',
    label: 'Name',
    colProps: { span: 24 },
    required:true
  },
  {
    field: 'birthday',
    component: 'DatePicker',
    label: 'Birthday',
    colProps: { span: 24 },
    componentProps:{
      showTime:false,
      valueFormat:"YYYY-MM-DD",
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'sex',
    component: 'RadioGroup',
    label: 'gender',
    colProps: { span: 24 },
    componentProps:{
      options: [
        {
          label: 'male',
          value: 1,
        },
        {
          label: 'female',
          value: 2,
        },
      ],
    }
  },
  {
    field: 'relTenantIds',
    component: 'JDictSelectTag',
    label: 'tenant',
    colProps: { span: 24 },
    componentProps:{
      mode:'multiple',
      dictCode:'sys_tenant,name,id',
      disabled:true
    }
  },
  {
    field: 'post',
    component: 'JDictSelectTag',
    label: 'Position',
    colProps: { span: 24 },
    componentProps:{
      mode:'multiple',
      dictCode:'sys_position,name,id',
      disabled:true
    }
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
]

//Password pop-up window
export const formPasswordSchema: FormSchema[] = [
  {
    label: 'User account',
    field: 'username',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Old Password',
    field: 'oldpassword',
    component: 'InputPassword',
    required: true,
  },
  {
    label: 'New Password',
    field: 'password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: '请输入New Password',
    },
    rules: [
      {
        required: true,
        message: '请输入New Password',
      },
    ],
  },
  {
    label: '确认New Password',
    field: 'confirmpassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
];
