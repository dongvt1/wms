import { FormSchema } from '/@/components/Table';
import { render } from "/@/utils/common/renderUtils";
import { getToken } from '/@/utils/auth';

//list
export const columns = [
  {
    title:'User account',
    align:"center",
    dataIndex: 'username',
    customRender: ( {text,record} ) => {
      let token = getToken();
      if(record.token === token) {
        return text + '（I）'
      }
      return text
    },
  },{
    title:'User name',
    align:"center",
    dataIndex: 'realname'
  },{
    title: 'avatar',
    align: "center",
    width: 120,
    dataIndex: 'avatar',
    customRender: render.renderAvatar,
  },{
    title:'Birthday',
    align:"center",
    dataIndex: 'birthday'
  },{
    title: 'gender',
    align: "center",
    dataIndex: 'sex',
    customRender: ({text}) => {
      return render.renderDict(text, 'sex');
    }
  },{
    title:'Phone number',
    align:"center",
    dataIndex: 'phone'
  }
];

//Query area
export const searchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: 'User account',
    component: 'Input',
    colProps: { span: 6 },
  }
];
