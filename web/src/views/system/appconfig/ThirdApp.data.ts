//third partyappConfiguration form
import { FormSchema } from '/@/components/Form';

//third partyappform
export const thirdAppFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'thirdType',
    field: 'thirdType',
    component: 'Input',
    show: false,
  },
  {
    label: 'CorpId',
    field: 'corpId',
    component: 'Input',
    ifShow: ({ values }) => {
      return values.thirdType === 'dingtalk';
    },
    required: true,
  },
  {
    label: 'Agentld',
    field: 'agentId',
    component: 'Input',
    required: true,
  },
  {
    label: 'AppKey',
    field: 'clientId',
    component: 'Input',
    required: true,
  },
  {
    label: 'AppSecret',
    field: 'clientSecret',
    component: 'Input',
    required: true,
  },{
    label: 'enable',
    field: 'status',
    component: 'Switch',
    componentProps:{
      checkedChildren:'closure',
      checkedValue:1,
      unCheckedChildren:'turn on',
      unCheckedValue: 0
    },
    defaultValue: 1
  },{
    label: 'tenantid',
    field: 'tenantId',
    component: 'Input',
    show: false,
  },
];
