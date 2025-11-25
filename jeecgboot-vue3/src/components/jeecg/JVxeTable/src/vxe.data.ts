import { propTypes } from '/@/utils/propTypes';

export const vxeProps = () => ({
  rowKey: propTypes.string.def('id'),
  // Column information
  columns: {
    type: Array,
    required: true,
  },
  // data source
  dataSource: {
    type: Array,
    required: true,
  },
  authPre: {
    type: String,
    required: false,
    default: '',
  },
  // Whether to display the toolbar
  toolbar: propTypes.bool.def(false),
  // Toolbar configuration
  toolbarConfig: propTypes.object.def(() => ({
    // prefix prefix；suffix suffix；
    slots: ['prefix', 'suffix'],
    // add Add button；remove delete button；clearSelection Clear selection button；collapse ExpandCollapse
    btns: ['add', 'remove', 'clearSelection'],
  })),
  // Whether to display line numbers
  rowNumber: propTypes.bool.def(false),
  // Fixed line number position or not fixed 【QQYUN-8405】
  rowNumberFixed: propTypes.oneOf(['left', 'none']).def('left'),
  // update-begin--author:liaozhiyang---date:20240509---for：【issues/1162】JVxeTableColumn too long（A horizontal scroll bar appears）Unable to drag and sort
  dragSortFixed: propTypes.oneOf(['left', 'none']).def('left'),
  rowSelectionFixed: propTypes.oneOf(['left', 'none']).def('left'),
  // update-end--author:liaozhiyang---date:20240509---for：【issues/1162】JVxeTableColumn too long（A horizontal scroll bar appears）Unable to drag and sort
  // Is it possible to select rows?
  rowSelection: propTypes.bool.def(false),
  // Select row type
  rowSelectionType: propTypes.oneOf(['checkbox', 'radio']).def('checkbox'),
  // Whether rows can be expanded
  rowExpand: propTypes.bool.def(false),
  // Expand row configuration
  expandConfig: propTypes.object.def(() => ({})),
  // Whether rows can be inserted
  insertRow: propTypes.bool.def(true),
  // Is the page loading?
  loading: propTypes.bool.def(false),
  // table height
  height: propTypes.oneOfType([propTypes.number, propTypes.string]).def('auto'),
  // maximum height
  maxHeight: {
    type: Number,
    default: () => null,
  },
  // lines to disable
  disabledRows: propTypes.object.def(() => ({})),
  // Whether to disable all components
  disabled: propTypes.bool.def(false),
  // Whether it can be sorted by dragging and dropping（Unable to drag and drop sort when there are fixed columns，Can only be sorted up and down）
  dragSort: propTypes.bool.def(false),
  // Sorting field savedKey
  sortKey: propTypes.string.def('orderNum'),
  // Sorting sequence number starting value，Default is 0
  sortBegin: propTypes.number.def(0),
  // size，Optional values ​​are：medium（middle）、small（Small）、mini（micro）
  size: propTypes.oneOf(['medium', 'small', 'mini']).def('medium'),
  // Whether to display border lines
  bordered: propTypes.bool.def(false),
  // Custom column configuration Default inheritance setup.toolbar.custom
  custom: propTypes.bool.def(false),
  // Pager parameters，Once set, the paginator can be displayed
  pagination: propTypes.object.def(() => ({})),
  // Whether to display the subform when a row is clicked
  clickRowShowSubForm: propTypes.bool.def(false),
  // Whether to display the main form when a row is clicked
  clickRowShowMainForm: propTypes.bool.def(false),
  // 是否点击选middle行，lowest priority
  clickSelectRow: propTypes.bool.def(false),
  // Whether to turn on reload Data effect
  reloadEffect: propTypes.bool.def(false),
  // Verification rules
  editRules: propTypes.object.def(() => ({})),
  // Whether to delete rows asynchronously，If you want to implement asynchronous deletion，Then you need to turn this option on，
  // existremoveCalled in eventconfirmRemovemethod will actually delete（Unless all new lines are deleted）
  asyncRemove: propTypes.bool.def(false),
  // Whether to always display components，if forfalseThe component will only appear when clicked
  // Note：This parameter cannot be modified dynamically；if OK、When there are many columns and fields，It will cause different degrees of lagging depending on the performance of the machine.。
  // TODO new versionvxe-tablecanceled visible parameter，As a result, this function cannot be implemented
  alwaysEdit: propTypes.bool.def(false),
  // Linkage configuration，array，See the documentation for detailed configuration
  linkageConfig: propTypes.array.def(() => []),
  // Whether to turn on使用 webSocket Refresh without trace
  socketReload: propTypes.bool.def(false),
  // samesocketKeyRefresh each other when changing
  socketKey: propTypes.string.def('vxe-default'),
  // Toggle row activation status when adding new rows
  addSetActive: propTypes.bool.def(true),
  // Whether to turn on键盘编辑
  keyboardEdit: propTypes.bool.def(false),
  // update-begin--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
  // Horizontal virtual scrolling configuration（Expanding rows is not supported）
  // 【QQYUN-7676】xWhen the scroll bar scrolls, the dictionary becomesid
  scrollX: propTypes.object.def(() => ({ enabled: false })),
  // Vertical virtual scrolling configuration（Expanding rows is not supported）
  scrollY: propTypes.object.def(() => ({ enabled: true })),
  // update-end--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
  //【QQYUN-8566】Cache column settingskey（Unique within the routing page）
  cacheColumnsKey: propTypes.string.def(''),
  // update-begin--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
  rowClassName: {
    type: [String, Function],
    default: null,
  },
  // Rows that are not allowed to be dragged [{'key':field,'value':value}]
  notAllowDrag: propTypes.array.def(() => []),
  // update-end--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database

  // Add button配置
  addBtnCfg: propTypes.object,
  // delete button配置
  removeBtnCfg: propTypes.object,
});

export const vxeEmits = ['save', 'added', 'removed', 'inserted', 'dragged', 'selectRowChange', 'pageChange', 'valueChange', 'blur'];
