import { FormSchema } from '/@/components/Form';
import { defHttp } from '/@/utils/http/axios';

export const schemas: FormSchema[] = [
  {
    label: 'Verification code',
    field: 'code',
    component: 'InputCountDown',
    componentProps: {
      //'default': default, 'large': maximum, 'small': smallest
      size:'default',
      //Countdown
      count: 120,
    },
  },
  {
    label: 'Apidrop down selection',
    field: 'apiSelect',
    component: 'ApiSelect',
    componentProps: {
      //multiple: Multiple choice；Leave blank as single choice
      mode: 'multiple',
      //askapi,Return results{ result: { records:[{'id':'1',name:'scott'},{'id':'2',name:'Xiao Zhang'}] }}
      api: () => defHttp.get({ url: '/test/jeecgDemo/list' }),
      //Numerical value converted toString
      numberToString: false,
      //title field
      labelField: 'name',
      //value field
      valueField: 'id',
      //ask参数
      params: {},
      //Return resultsField
      resultField: 'records',
    },
  },
  {
    label: 'Apitree selection',
    field: 'apiSelect',
    component: 'ApiTreeSelect',
    componentProps: {
      /* askapi,Return results
         { result: { list: [{ title:'Options0',value:'0',key:'0',
           children: [ {"title": "Options0-0","value": "0-0","key": "0-0"},...]
           }, ...]
         }} */
      api: () => defHttp.get({ url: '/mock/tree/getDemoOptions' }),
      //ask参数
      params: {},
      //Return resultsField
      resultField: 'list',
    },
  },
  {
    label: 'Check password strength',
    field: 'pwd',
    component: 'StrengthMeter',
    componentProps: {
      //Whether to display the password text box
      showInput: true,
      //Whether to disable
      disabled: false,
    },
  },
  {
    label: 'Provincial, city and county linkage',
    field: 'province',
    component: 'JAreaLinkage',
    componentProps: {
      //Whether to display districts and counties，defaulttrue,Otherwise, only the province will be displayed
      showArea: true,
      //Is it all text，defaultfalse
      showAll: true,
    },
  },
  {
    label: 'Job selection',
    field: 'post',
    component: 'JSelectPosition',
    componentProps: {
      //Whether to display the selected list on the right
      showSelected: true,
      //maximum选择数量
      maxSelectCount: 1,
      //Job title
      modalTitle: 'post',
    },
  },
  {
    label: 'Character selection',
    field: 'role',
    component: 'JSelectRole',
    componentProps: {
      //ask参数 likeparams:{"code":"001"}
      params: {},
      //Single choice or not,defaultfalse
      isRadioSelection: true,
      //character title
      modalTitle: 'Role',
    },
  },
  {
    label: 'User selection',
    field: 'user',
    component: 'JSelectUser',
    componentProps: {
      //取value fieldConfiguration,Generally the primary key field
      rowKey: 'username',
      //Show field configuration
      labelKey: 'realname',
      //Whether to display the select button
      showButton: false,
      //user title
      modalTitle: 'user',
    },
  },
  {
    label: 'Image upload',
    field: 'uploadImage',
    component: 'JImageUpload',
    componentProps: {
      //Button display text
      text:'Image upload',
      //Supports two basic stylespictureandpicture-card
      listType:'picture-card',
      //Business path used to control file uploads,defaulttemp
      bizPath:'temp',
      //Whether to disable
      disabled:false,
      //maximum上传数量
      fileMax:1,
    },
  },
  {
    label: 'dictionary tag',
    field: 'dictTags',
    component: 'JDictSelectTag',
    componentProps: {
      //dictionarycodeConfiguration，比likepass性别dictionarycoding：sex，Can also be useddemo,name,id table name,name,value way
      dictCode:'sex',
      //supportradio(radio button)、radioButton(radio button btnstyle)、select(drop down box)
      type:'radioButton'
    },
  },
  {
    label: 'Department selection',
    field: 'dept',
    component: 'JSelectDept',
    componentProps: {
      //Whether to enable asynchronous loading
      sync: false,
      //Whether to show checkbox
      checkable: true,
      //Whether to display the select button
      showButton: false,
      //The selected status of the parent and child nodes is no longer associated.
      checkStrictly: true,
      //Select box title
      modalTitle: 'Department selection',
    },
  },
  {
    label: 'Provincial, city and county level linkage',
    field: 'provinceArea',
    component: 'JAreaSelect',
    componentProps: {
      //level 1 Show only provinces 2 Province and city 3 Province and city区
      level:3
    },
  },
  {
    label: 'rich text',
    field: 'editor',
    component: 'JEditor',
    componentProps: {
      //Whether to disable
      disabled: false
    },
  },
  {
    label: 'markdown',
    field: 'markdown',
    component: 'JMarkdownEditor',
    componentProps: {
      //Whether to disable
      disabled: false
    },
  },
  {
    label: '可输入drop down box',
    field: 'inputSelect',
    component: 'JSelectInput',
    componentProps: {
      options: [
        { label: 'Default', value: 'default' },
        { label: 'IFrame', value: 'iframe' },
      ],
      //Whether it is search mode
      showSearch: true,
      //Whether to disable
      disabled: false
    },
  },
  {
    label: 'Code editor component',
    field: 'jCode',
    component: 'JCodeEditor',
    componentProps: {
      //high，defaultauto
      height:'150px',
      //Whether to disable
      disabled:false,
      //Whether to full screen
      fullScreen:false,
      //coordinates after full screen
      zIndex: 999,
      //code theme，目前只supportidea,Can be expanded by the component itself
      theme:'idea',
      //Code tips
      keywords:['console'],
      //语言like（javascript,vue,markdown）Can be expanded by the component itself
      language:'javascript'
    },
  },
  {
    label: '分类dictionary树',
    field: 'dictTree',
    component: 'JCategorySelect',
    componentProps: {
      //Placeholder content
      placeholder:'请选择分类dictionary树',
      //Query conditions，like“{'name':'notebook'}”
      condition:"",
      //yesnoMultiple choice
      multiple: false,
      //Starting selectioncode，见Configuration的分类dictionary的类型coding
      pcode: 'A04',
      //parentid
      pid:'',
      //returnkey
      back:'id',
    },
  },
  {
    label: '下拉Multiple choice',
    field: 'selectMultiple',
    component: 'JSelectMultiple',
    componentProps: {
      //dictionarycodeConfiguration，比likepass性别dictionarycoding：sex，Can also be useddemo,name,id table name,name,value way
      dictCode:'company_rank',
      //Is it read-only?
      readOnly:false,
    },
  },
  {
    label: 'popup',
    field: 'popup',
    component: 'JPopup',
    componentProps: ({ formActionType }) => {
      const {setFieldsValue} = formActionType;
      return{
        setFieldsValue:setFieldsValue,
        //onlineReport coding
        code:"demo",
        //yesno为Multiple choice
        multi:false,
        //FieldConfiguration
        fieldConfig: [
          { source: 'name', target: 'popup' },
        ],
      }
    },
  },
  {
    label: 'Switch customization',
    field: 'switch',
    component: 'JSwitch',
    componentProps:{
      //value options
      options:['Y','N'],
      //textoption
      labelOptions:['yes', 'no'],
      //yesno启用下拉
      query: false,
      //Whether to disable
      disabled: false,
    },
  },
  {
    label: 'Timing expression selection',
    field: 'timing',
    component: 'JEasyCron',
    componentProps:{
      //yesno隐藏参数秒and年设置，like果隐藏，那么参数秒and年将会全部忽略掉。
      hideSecond: false,
      //yesno隐藏参数年设置，like果隐藏，Then all parameter years will be ignored
      hideYear: false,
      //Whether to disable
      disabled: false,
      //Function to get preview execution time list，The format is：remote (cronvalue, timeTimestamp, cbcallback function)
      remote:(cron,time,cb)=>{}
    },
  },
  {
    label: '分类dictionary树',
    field: 'treeDict',
    component: 'JTreeDict',
    componentProps:{
      //Specify the fields that the current component needs to store Optional: id(primary key)andcode(coding)
      field:'id',
      //yesno为异步
      async: true,
      //Whether to disable
      disabled: false,
      //指定一个节点的coding,加载该节点下的所有dictionary数据,If not specified，default加载所有数据
      parentCode:'A04'
    },
  },
  {
    label: 'Multi-line input window',
    field: 'inputPop',
    component: 'JInputPop',
    componentProps:{
      //title
      title:'Multi-line input window',
      //Pop-up window display position
      position:'bottom',
    },
  },
  {
    label: 'Multiple choice',
    field: 'multipleChoice',
    component: 'JCheckbox',
    componentProps:{
      //dictionarycodeConfiguration，比likepass职位dictionarycoding：company_rank，Can also be useddemo,name,id table name,name,value way
      dictCode:'company_rank',
      //Whether to disable
      disabled: false,
      //没有dictionarycodeCan be usedoptionto define
      // options:[
      //   {label:'CE0',value:'1'}
      // ]
    },
  },
  {
    label: '下拉tree selection',
    field: 'treeCusSelect',
    component: 'JTreeSelect',
    componentProps: {
      //dictionarycodeConfiguration，比likepass性别dictionarycoding：sex，Can also be usedsys_permission,name,id table name,name,value way
      dict: 'sys_permission,name,id',
      //parentidField
      pidField: 'parent_id',
    },
  },
  {
    label: '根据Department selectionuser组件',
    field: 'userByDept',
    component: 'JSelectUserByDept',
    componentProps: {
      //Whether to display the select button
      showButton: true,
      //Select box title
      modalTitle: '部门User selection'
    },
  },
  {
    label: 'File upload',
    field: 'uploadFile',
    component: 'JUpload',
    componentProps: {
      //Whether to display the select button
      text: 'File upload',
      //maximum上传数
      maxCount: 2,
      //yesno显示下载按钮
      download: true,
    },
  },
  {
    label: 'dictionary表搜索',
    field: 'dictSearchSelect',
    component: 'JSearchSelect',
    componentProps: {
      //dictionarycodeConfiguration，pass demo,name,id table name,name,value way
      dict: 'demo,name,id',
      //yesno异步加载
      async: true,
      //whenasyncset totruevalid when，When indicating an asynchronous query，The number of data obtained each time，default10
      pageSize:3
    },
  },
  {
    label: 'Dynamically createdinputbox',
    field: 'jAddInput',
    component: 'JAddInput',
    componentProps: {
      //Customize the number of rows before the delete button is displayed，default为1
      min:1
    },
  },
  {
    label: 'User selection组件',
    field: 'userCusSelect',
    component: 'UserSelect',
    componentProps: {
      //yesnoMultiple choice
      multi: true,
      //从user表中选择一List，其value作为该控件的存储value，defaultidList
      store: 'id',
      //yesno排除我自己(when前登录user)
      izExcludeMy: false,
      //Whether to disable
      disabled: false,
    },
  },  
  {
    label: '选择Role组件',
    field: 'roleSelect',
    component: 'RoleSelect',
    componentProps: {
      //maximum选择数量  
      maxSelectCount: 4,
      //Single choice or not
      multi: true
    },
  },  
  {
    label: '数value范围输入box',
    field: 'rangeNumber',
    component: 'JRangeNumber',
  }, 
  {
    label: 'remoteApi单选box组',
    field: 'apiRadioGroup',
    component: 'ApiRadioGroup',
    componentProps:{
      //ask接口Return results{ result:{ list: [ name: 'Options0',id: '1' ] }}
      api:()=> defHttp.get({ url: '/mock/select/getDemoOptions' }),
      //ask参数
      params:{},
      //yesno为按钮style类型，defaultfalse
      isBtn: false,
      //return集合name
      resultField: 'list',
      //title fieldname
      labelField: 'name',
      //value fieldname
      valueField: 'id',
    }
  },
];
