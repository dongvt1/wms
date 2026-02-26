import { FormSchema } from '/@/components/Form';
import { getPositionByDepartId } from "./depart.api";
import { useMessage } from "@/hooks/web/useMessage";
import { BasicColumn } from "@/components/Table";
import { getDepartPathNameByOrgCode } from '@/utils/common/compUtils';
import { h, ref } from 'vue';

const { createMessage: $message } = useMessage();
//Department name
const departNamePath = ref<Record<string, string>>({});

// Department basic form
export function useBasicFormSchema(treeData) {
  const basicFormSchema: FormSchema[] = [
    {
      field: 'departName',
      label: 'Organization name',
      component: 'Input',
      componentProps: {
        placeholder: 'Please enter organization/Department name',
      },
      rules: [{ required: true, message: 'Organization name不能为空' }],
    },
    {
      field: 'parentId',
      label: 'superior department',
      component: 'TreeSelect',
      componentProps: {
        treeData: [],
        placeholder: 'none',
        treeCheckAble: true,
        multiple: true,
        dropdownStyle: { maxHeight: '200px', overflow: 'auto' },
        tagRender: (options) => {
          const { value, label, option } = options;
          if (departNamePath.value[value]) {
            return h(
                'span', {  style: { marginLeft: '10px' } },
                departNamePath.value[value]
            );
          }
          getDepartPathNameByOrgCode('', label, option.id).then((data) => {
            departNamePath.value[value] = data;
          });
        },
      },
    },
    {
      field: 'orgCode',
      label: 'Institution code',
      component: 'Input',
      componentProps: {
        placeholder: 'Please enter organization编码',
      },
    },
    {
      field: 'orgCategory',
      label: 'Institution type',
      component: 'RadioButtonGroup',
      componentProps: { options: [] },
    },
    {
      field: 'positionId',
      label: 'Job level',
      component: 'JDictSelectTag',
      componentProps: ({ formModel, formActionType }) => {
        return {
          dictCode: "sys_position,name,id, 1=1 order by post_level asc",
          getPopupContainer: ()=> document.body,
          onChange: (value) => {
            formModel.depPostParentId = "";
            return positionChange(value, formModel, treeData);
          },
        }
      },
      ifShow:({ values })=>{
        return values.orgCategory === '3'
      },
      required: true,
    },
    {
      field: 'depPostParentId',
      label: 'Superior position',
      component: 'TreeSelect',
      ifShow:({ values })=>{
        return values.orgCategory === '3'
      },
      slot: 'depPostParentId',
    },
    {
      field: 'departOrder',
      label: 'sort',
      component: 'InputNumber',
      componentProps: {},
    },
    {
      field: 'mobile',
      label: 'Telephone',
      component: 'Input',
      componentProps: {
        placeholder: '请输入Telephone',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'fax',
      label: 'fax',
      component: 'Input',
      componentProps: {
        placeholder: '请输入fax',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'address',
      label: 'address',
      component: 'Input',
      componentProps: {
        placeholder: '请输入address',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'memo',
      label: 'Remark',
      component: 'InputTextArea',
      componentProps: {
        placeholder: '请输入Remark',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
  ];
  return { basicFormSchema };
}

// Institution type选项
export const orgCategoryOptions = {
  // First level department
  root: [{ value: '1', label: 'company' }],
  // sub-department
  child: [
    { value: '4', label: '子company' },
    { value: '2', label: 'department' },
    { value: '3', label: 'post' },
  ],
  //departmentpost
  childDepartPost: [
    { value: '2', label: 'department' },
    { value: '3', label: 'post' },
  ],
  //post
  childPost: [
    { value: '3', label: 'post' },
  ]
};

/**
 * User list
 */
export const userColumns: BasicColumn[] = [
  {
    title: 'User account',
    dataIndex: 'username',
    width: 120,
  },
  {
    title: 'Name',
    dataIndex: 'realname',
    width: 150,
  },
  {
    title: 'cell phone',
    width: 150,
    dataIndex: 'phone',
  },
  {
    title: '主post',
    dataIndex: 'mainDepPostId_dictText',
    width: 200,
  },
];

/**
 * Position change event
 * @param value
 * @param model
 * @param treeData
 */
export function positionChange(value, model, treeData) {
  if(value && model.parentId){
    getPositionByDepartId({ parentId: model.parentId, departId: model.id ? model.id:'', positionId: value }).then((res) =>{
      if(res.success){
        treeData.value = res.result;
      }else{
        treeData.value = [];
        $message.warning(res.message);
      }
    });
  } else {
    treeData.value = [];
  }
}
