//Drop-down selection box component publicprops
import { propTypes } from '/@/utils/propTypes';

export const selectProps = {
  //Whether to select multiple
  isRadioSelection: {
    type: Boolean,
    //update-begin---author:wangshuai ---date:20220527  for：The department user component should be single-selected by default.，Otherwise there will be problems elsewhere------------
    default: false,
    //update-end---author:wangshuai ---date:20220527  for：The department user component should be single-selected by default.，Otherwise there will be problems elsewhere--------------
  },
  //returnvalueField name
  rowKey: {
    type: String,
    default: 'id',
  },
  //return文本Field name
  labelKey: {
    type: String,
    default: 'name',
  },
  //query parameters
  params: {
    type: Object,
    default: () => {},
  },
  //Whether to display the select button
  showButton: propTypes.bool.def(true),
  //Whether to display the selection list on the right
  showSelected: propTypes.bool.def(false),
  //Maximum number of choices
  maxSelectCount: {
    type: Number,
    default: 0,
  },
};

//Tree selection component publicprops
export const treeProps = {
  //returnvalueField name
  rowKey: {
    type: String,
    default: 'key',
  },
  //return文本Field name
  labelKey: {
    type: String,
    default: 'title',
  },
  //Initial expanded level
  defaultExpandLevel: {
    type: [Number],
    default: 1,
  },
  //rootpidvalue
  startPid: {
    type: [Number, String],
    default: '',
  },
  //primary key field
  primaryKey: {
    type: [String],
    default: 'id',
  },
  //fatherIDField
  parentKey: {
    type: [String],
    default: 'parentId',
  },
  //titleField
  titleKey: {
    type: [String],
    default: 'title',
  },
  //Whether to enable server-side conversiontreedata structure
  serverTreeData: propTypes.bool.def(true),
  //Whether to enable asynchronous loading of data
  sync: propTypes.bool.def(true),
  //Whether to display the select button
  showButton: propTypes.bool.def(true),
  //Whether to show only companies
  onlyShowCompany: propTypes.bool.def(false),
  //Whether to show checkbox
  checkable: propTypes.bool.def(true),
  //checkable Node selection is fully controlled in the state（father子节点选中状态不再关联）
  checkStrictly: propTypes.bool.def(false),
  // Whether to allow multiple selections，default true
  multiple: propTypes.bool.def(true),
  // Whether to select only positions
  izOnlySelectDepartPost: propTypes.bool.def(false),
};
