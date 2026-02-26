import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import {JVxeTypes,JVxeColumn} from '/@/components/jeecg/JVxeTable/types'
import { getWeekMonthQuarterYear } from '/@/utils';
//List data
export const columns: BasicColumn[] = [
   {
    title: 'Interface name',
    align:"center",
    dataIndex: 'name'
   },
   {
    title: 'Request method',
    align:"center",
    dataIndex: 'requestMethod'
   },
   {
    title: 'interface address',
    align:"center",
    dataIndex: 'requestUrl'
   },
   {
    title: 'IP blacklist',
    align:"center",
    dataIndex: 'blackList'
   },
   // {
   //  title: 'state',
   //  align:"center",
   //  dataIndex: 'status'
   // },
   {
    title: 'Creator',
    align:"center",
    dataIndex: 'createBy'
   },
   {
    title: 'creation time',
    align:"center",
    dataIndex: 'createTime'
   },
];
//Query data
export const searchFormSchema: FormSchema[] = [
  {
    label: "Interface name",
    field: "name",
    component: 'JInput',
  },
  {
    label: "Creator",
    field: "createBy",
    component: 'JInput',
  },
];
//form data
export const formSchema: FormSchema[] = [
  {
    label: 'Interface name',
    field: 'name',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: 'Please enterInterface name!'},
          ];
     },
  },
  {
    label: 'original address',
    field: 'originUrl',
    component: 'Input',
  },
  {
    label: 'Request method',
    field: 'requestMethod',
    component: 'JSearchSelect',
    componentProps:{
      dictOptions: [
        {
          text: 'POST',
          value: 'POST',
        },
        {
          text: 'GET',
          value: 'GET',
        },
        {
          text: 'HEAD',
          value: 'HEAD',
        },
        {
          text: 'PUT',
          value: 'PUT',
        },
        {
          text: 'PATCH',
          value: 'PATCH',
        },
        {
          text: 'DELETE',
          value: 'DELETE',
        },{
          text: 'OPTIONS',
          value: 'OPTIONS',
        },{
          text: 'TRACE',
          value: 'TRACE',
        },
      ]
     },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: 'Please enterRequest method!'},
          ];
     },
  },
  {
    label: 'interface address',
    field: 'requestUrl',
    component: 'Input',
    dynamicDisabled:true
  },
  {
    label: 'IP blacklist',
    field: 'blackList',
    component: 'Input',
  },
  {
    label: 'Request body content',
    component:"Input",
    field: 'body'
  },
  {
    label: 'delete identifier',
    field: 'delFlag',
    component: 'Input',
    defaultValue:0,
    show:false
  },
  {
    label: 'state',
    field: 'status',
    component: 'Input',
    defaultValue:"1",
    show:false
  },
	// TODO Primary key hidden field，Currently written to death asID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];
//子form data
//子表List data
export const openApiHeaderColumns: BasicColumn[] = [
   // {
   //  title: 'apiId',
   //  align:"center",
   //  dataIndex: 'apiId'
   // },
   {
    title: 'Request headerKey',
    align:"center",
    dataIndex: 'headerKey'
   },
   {
    title: 'Is it required?',
    align:"center",
    dataIndex: 'required_dictText'
   },
   {
    title: 'default value',
    align:"center",
    dataIndex: 'defaultValue'
   },
   {
    title: 'Remark',
    align:"center",
    dataIndex: 'note'
   },
];
//子表List data
export const openApiParamColumns: BasicColumn[] = [
   // {
   //  title: 'apiId',
   //  align:"center",
   //  dataIndex: 'apiId'
   // },
   {
    title: 'parameterKey',
    align:"center",
    dataIndex: 'paramKey'
   },
   {
    title: 'Is it required?',
    align:"center",
    dataIndex: 'required_dictText'
   },
   {
    title: 'default value',
    align:"center",
    dataIndex: 'defaultValue'
   },
   {
    title: 'Remark',
    align:"center",
    dataIndex: 'note'
   },
];
//Subtable table configuration
export const openApiHeaderJVxeColumns: JVxeColumn[] = [
    // {
    //   title: 'apiId',
    //   key: 'apiId',
    //   type: JVxeTypes.input,
    //   width:"200px",
    //   placeholder: 'Please enter${title}',
    //   defaultValue:'',
    // },
    {
      title: 'Request headerKey',
      key: 'headerKey',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Please enter${title}',
      defaultValue:'',
    },
    {
      title: 'Is it required?',
      key: 'required',
      type: JVxeTypes.checkbox,
      options:[],
      // dictCode:"yn",
      width:"100px",
      placeholder: 'Please enter${title}',
      defaultValue:'',
      customValue: ['1','0']
    },
    {
      title: 'default value',
      key: 'defaultValue',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Please enter${title}',
      defaultValue:'',
    },
    {
      title: 'Remark',
      key: 'note',
      type: JVxeTypes.input,
      placeholder: 'Please enter${title}',
      defaultValue:'',
    },
  ]
export const openApiParamJVxeColumns: JVxeColumn[] = [
    // {
    //   title: 'apiId',
    //   key: 'apiId',
    //   type: JVxeTypes.input,
    //   width:"200px",
    //   placeholder: 'Please enter${title}',
    //   defaultValue:'',
    // },
    {
      title: 'parameterKey',
      key: 'paramKey',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Please enter${title}',
      defaultValue:'',
    },
    {
      title: 'Is it required?',
      key: 'required',
      type: JVxeTypes.checkbox,
      options:[],
      // dictCode:"yn",
      width:"100px",
      placeholder: 'Please enter${title}',
      defaultValue:'',
      customValue: ['1','0']
    },
    {
      title: 'default value',
      key: 'defaultValue',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Please enter${title}',
      defaultValue:'',
    },
    {
      title: 'Remark',
      key: 'note',
      type: JVxeTypes.input,
      placeholder: 'Please enter${title}',
      defaultValue:'',
    },
  ]

// 高级Query data
export const superQuerySchema = {
  name: {title: 'Interface name',order: 0,view: 'text', type: 'string',},
  requestMethod: {title: 'Request method',order: 1,view: 'list', type: 'string',dictCode: '',},
  requestUrl: {title: 'interface address',order: 2,view: 'text', type: 'string',},
  blackList: {title: 'IP blacklist',order: 3,view: 'text', type: 'string',},
  status: {title: 'state',order: 5,view: 'number', type: 'number',},
  createBy: {title: 'Creator',order: 6,view: 'text', type: 'string',},
  createTime: {title: 'creation time',order: 7,view: 'datetime', type: 'string',},
  //Subtable advanced query
  openApiHeader: {
    title: 'Request header表',
    view: 'table',
    fields: {
        // apiId: {title: 'apiId',order: 0,view: 'text', type: 'string',},
        headerKey: {title: 'Request headerKey',order: 1,view: 'text', type: 'string',},
        required: {title: 'Is it required?',order: 2,view: 'number', type: 'number',dictCode: 'yn',},
        defaultValue: {title: 'default value',order: 3,view: 'text', type: 'string',},
        note: {title: 'Remark',order: 4,view: 'text', type: 'string',},
    }
  },
  openApiParam: {
    title: '请求parameter部分',
    view: 'table',
    fields: {
        // apiId: {title: 'apiId',order: 0,view: 'text', type: 'string',},
        paramKey: {title: 'parameterKey',order: 1,view: 'text', type: 'string',},
        required: {title: 'Is it required?',order: 2,view: 'number', type: 'number',dictCode: 'yn',},
        defaultValue: {title: 'default value',order: 3,view: 'text', type: 'string',},
        note: {title: 'Remark',order: 4,view: 'text', type: 'string',},
    }
  },
};

/**
* The process form calls this method to obtainformSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // The default is the same as the original form If permission data is configured in the process，This needs to be dealt with separatelyformSchema
  return formSchema;
}
