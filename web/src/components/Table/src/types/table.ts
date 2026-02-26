import type { VNodeChild, ComputedRef } from 'vue';
import type { PaginationProps } from './pagination';
import type { FormProps } from '/@/components/Form';
import type { TableRowSelection as ITableRowSelection } from 'ant-design-vue/lib/table/interface';
import type { ColumnProps } from 'ant-design-vue/lib/table';

import { ComponentType } from './componentType';
import { VueNode } from '/@/utils/propTypes';
import { RoleEnum } from '/@/enums/roleEnum';

export declare type SortOrder = 'ascend' | 'descend';

export interface TableCurrentDataSource<T = Recordable> {
  currentDataSource: T[];
}

export interface TableRowSelection<T = any> extends ITableRowSelection {
  /**
   * Callback executed when selected rows change
   * @type Function
   */
  onChange?: (selectedRowKeys: string[] | number[], selectedRows: T[]) => any;

  /**
   * Callback executed when select/deselect one row
   * @type Function
   */
  onSelect?: (record: T, selected: boolean, selectedRows: Object[]) => any;

  /**
   * Callback executed when select/deselect all rows
   * @type Function
   */
  onSelectAll?: (selected: boolean, selectedRows: T[], changeRows: T[]) => any;

  /**
   * Callback executed when row selection is inverted
   * @type Function
   */
  onSelectInvert?: (selectedRows: string[] | number[]) => any;
  //【issues/8163】New associated records are lost
  selectedRows?: any[];
}

export interface TableCustomRecord<T> {
  record?: T;
  index?: number;
}

export interface ExpandedRowRenderRecord<T> extends TableCustomRecord<T> {
  indent?: number;
  expanded?: boolean;
}

export interface ColumnFilterItem {
  text?: string;
  value?: string;
  children?: any;
}

export interface TableCustomRecord<T = Recordable> {
  record?: T;
  index?: number;
}

export interface SorterResult {
  column: ColumnProps;
  order: SortOrder;
  field: string;
  columnKey: string;
}

export interface FetchParams {
  searchInfo?: Recordable;
  page?: number;
  sortInfo?: Recordable;
  filterInfo?: Recordable;
}

export interface GetColumnsParams {
  ignoreIndex?: boolean;
  ignoreAction?: boolean;
  // update-begin--author:liaozhiyang---date:20250729---for：【issues/8502】Solve the problem that permission columns are not displayed in the list，The column configuration also shows
  ignoreAuth?: boolean;
  ignoreIfShow?: boolean | ((column: BasicColumn) => boolean);
  // update-end--author:liaozhiyang---date:20250729---for：【issues/8502】Solve the problem that permission columns are not displayed in the list，The column configuration also shows
  sort?: boolean;
}

export type SizeType = 'middle' | 'small' | 'large';

export interface TableActionType {
  reload: (opt?: FetchParams) => Promise<void>;
  getSelectRows: <T = Recordable>() => T[];
  clearSelectedRowKeys: () => void;
  expandAll: () => void;
  collapseAll: () => void;
  getSelectRowKeys: () => string[];
  deleteSelectRowByKey: (key: string) => void;
  setPagination: (info: Partial<PaginationProps>) => void;
  setTableData: <T = Recordable>(values: T[]) => void;
  updateTableDataRecord: (rowKey: string | number, record: Recordable) => Recordable | void;
  deleteTableDataRecord: (rowKey: string | number | string[] | number[]) => void;
  insertTableDataRecord: (record: Recordable, index?: number) => Recordable | void;
  findTableDataRecord: (rowKey: string | number) => Recordable | void;
  getColumns: (opt?: GetColumnsParams) => BasicColumn[];
  setColumns: (columns: BasicColumn[] | string[]) => void;
  getDataSource: <T = Recordable>() => T[];
  getRawDataSource: <T = Recordable>() => T;
  setLoading: (loading: boolean) => void;
  setProps: (props: Partial<BasicTableProps>) => void;
  redoHeight: () => void;
  setSelectedRowKeys: (rowKeys: string[] | number[]) => void;
  getPaginationRef: () => PaginationProps | boolean;
  getSize: () => SizeType;
  getRowSelection: () => TableRowSelection<Recordable>;
  getCacheColumns: () => BasicColumn[];
  emit?: EmitType;
  updateTableData: (index: number, key: string, value: any) => Recordable;
  setShowPagination: (show: boolean) => Promise<void>;
  getShowPagination: () => boolean;
  setCacheColumnsByField?: (dataIndex: string | undefined, value: BasicColumn) => void;
  getColumnsRef: () => ComputedRef<BasicColumn[]>;
  getBindValuesRef: () => ComputedRef<any>;
}

export interface FetchSetting {
  // Request the current page number of the interface
  pageField: string;
  // How many items to display per page
  sizeField: string;
  // Request result list fields  support a.b.c
  listField: string;
  // Request result total field  support a.b.c
  totalField: string;
}

export interface TableSetting {
  // Whether to display refresh button
  redo?: boolean;
  // Whether to display the resize button
  size?: boolean;
  // Whether to display field adjustment buttons
  setting?: boolean;
  // cache“Field adjustment”configuredkey，Used when there are multiple tables on the page that need to be distinguished
  cacheKey?: string;
  // Whether to show the full screen button
  fullScreen?: boolean;
}

export interface BasicTableProps<T = any> {
  // Click on row to select
  clickToRowSelect?: boolean;
  isTreeTable?: boolean;
  // Custom sorting method
  sortFn?: (sortInfo: SorterResult) => any;
  // Sorting method
  filterFn?: (data: Partial<Recordable<string[]>>) => any;
  // Cancel the default of the tablepadding
  inset?: boolean;
  // Show table settings
  showTableSetting?: boolean;
  // Operation button settings above the table
  tableSetting?: TableSetting;
  // zebra print
  striped?: boolean;
  // Whether to automatically generatekey
  autoCreateKey?: boolean;
  // How to calculate total rows
  summaryFunc?: (...arg: any) => Recordable[];
  // Customize total table content
  summaryData?: Recordable[];
  // Whether to display the total row
  showSummary?: boolean;
  // Whether columns can be dragged and dropped
  canColDrag?: boolean;
  // Interface request object
  api?: (...arg: any) => Promise<any>;
  // Process parameters before requesting
  beforeFetch?: Fn;
  // Custom processing interface return parameters
  afterFetch?: Fn;
  // Processing before query condition request
  handleSearchInfoFn?: Fn;
  // Request interface configuration
  fetchSetting?: Partial<FetchSetting>;
  // Request interface now
  immediate?: boolean;
  // When opening the search form，Whether to display the table if there is no data
  emptyDataIsShowTable?: boolean;
  // Additional request parameters
  searchInfo?: Recordable;
  // Default sort parameters
  defSort?: Recordable | Recordable[];
  // Use the search form
  useSearchForm?: boolean;
  // Form configuration
  formConfig?: Partial<FormProps>;
  // column configuration
  columns: BasicColumn[];
  // Set column maximum width uniformly
  maxColumnWidth?: number;
  // Whether to display the serial number column
  showIndexColumn?: boolean;
  // 序号column configuration
  indexColumnProps?: BasicColumn;
  // Whether to display the operation column
  showActionColumn?: boolean;
  // 操作column configuration
  actionColumn?: Partial<BasicColumn>;
  // Whether the text exceeds the width and is displayed。。。
  ellipsis?: boolean;
  // Is it possible to adjust the height?
  canResize?: boolean;
  // Adaptive height offset， Calculation result-offset
  resizeHeightOffset?: number;
  // Clear options when pagination changes
  clearSelectOnPageChange?: boolean;
  //
  rowKey?: string | ((record: Recordable) => string);
  // data
  dataSource?: Recordable[];
  // Tips on the right side of the title
  titleHelpMessage?: string | string[];
  // table minimum height
  minHeight?: number;
  // table scroll maximum height
  maxHeight?: number;
  // Whether to display borders
  bordered?: boolean;
  // update-begin--author:liaozhiyang---date:202401009---for：【TV360X-116】The table is misaligned when there are many embedded style fields
  // Expand column width
  expandColumnWidth: number;
  // update-end--author:liaozhiyang---date:202401009---for：【TV360X-116】The table is misaligned when there are many embedded style fields
  // Paging configuration
  pagination?: PaginationProps | boolean;
  // loadingload
  loading?: boolean;

  /**
   * The column contains children to display
   * @default 'children'
   * @type string | string[]
   */
  childrenColumnName?: string;

  /**
   * Override default table elements
   * @type object
   */
  components?: object;

  /**
   * Expand all rows initially
   * @default false
   * @type boolean
   */
  defaultExpandAllRows?: boolean;

  /**
   * Initial expanded row keys
   * @type string[]
   */
  defaultExpandedRowKeys?: string[];

  /**
   * Current expanded row keys
   * @type string[]
   */
  expandedRowKeys?: string[];

  /**
   * Expanded container render for each row
   * @type Function
   */
  expandedRowRender?: (record?: ExpandedRowRenderRecord<T>) => VNodeChild | JSX.Element;

  /**
   * Customize row expand Icon.
   * @type Function | VNodeChild
   */
  expandIcon?: Function | VNodeChild | JSX.Element;

  /**
   * Whether to expand row by clicking anywhere in the whole row
   * @default false
   * @type boolean
   */
  expandRowByClick?: boolean;

  /**
   * The index of `expandIcon` which column will be inserted when `expandIconAsCell` is false. default 0
   */
  expandIconColumnIndex?: number;

  /**
   * Table footer renderer
   * @type Function | VNodeChild
   */
  footer?: Function | VNodeChild | JSX.Element;

  /**
   * Indent size in pixels of tree data
   * @default 15
   * @type number
   */
  indentSize?: number;

  /**
   * i18n text including filter, sort, empty text, etc
   * @default { filterConfirm: 'Ok', filterReset: 'Reset', emptyText: 'No Data' }
   * @type object
   */
  locale?: object;

  /**
   * Row's className
   * @type Function
   */
  rowClassName?: (record: TableCustomRecord<T>, index: number) => string;

  /**
   * Row selection config
   * @type object
   */
  rowSelection?: TableRowSelection;

  /**
   * Set horizontal or vertical scrolling, can also be used to specify the width and height of the scroll area.
   * It is recommended to set a number for x, if you want to set it to true,
   * you need to add style .ant-table td { white-space: nowrap; }.
   * @type object
   */
  // update-begin--author:liaozhiyang---date:20240424---for：【issues/1188】BasicTableplusscrollToFirstRowOnChangetype definition
  scroll?: { x?: number | true | 'max-content'; y?: number; scrollToFirstRowOnChange?: boolean };
  // update-end--author:liaozhiyang---date:20240424---for：【issues/1188】BasicTableplusscrollToFirstRowOnChangetype definition

  /**
   * Whether to show table header
   * @default true
   * @type boolean
   */
  showHeader?: boolean;

  /**
   * Size of table
   * @default 'default'
   * @type string
   */
  size?: SizeType;

  /**
   * Table title renderer
   * @type Function | ScopedSlot
   */
  title?: VNodeChild | JSX.Element | string | ((data: Recordable) => string);

  /**
   * Set props on per header row
   * @type Function
   */
  customHeaderRow?: (column: ColumnProps, index: number) => object;

  /**
   * Set props on per row
   * @type Function
   */
  customRow?: (record: T, index: number) => object;

  /**
   * `table-layout` attribute of table element
   * `fixed` when header/columns are fixed, or using `column.ellipsis`
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/CSS/table-layout
   * @version 1.5.0
   */
  tableLayout?: 'auto' | 'fixed' | string;

  /**
   * the render container of dropdowns in table
   * @param triggerNode
   * @version 1.5.0
   */
  getPopupContainer?: (triggerNode?: HTMLElement) => HTMLElement;

  /**
   * Data can be changed again before rendering.
   * The default configuration of general user empty data.
   * You can configured globally through [ConfigProvider](https://antdv.com/components/config-provider-cn/)
   *
   * @version 1.5.4
   */
  transformCellText?: Function;

  /**
   * Callback executed before editable cell submit value, not for row-editor
   *
   * The cell will not submit data while callback return false
   */
  beforeEditSubmit?: (data: { record: Recordable; index: number; key: string | number; value: any }) => Promise<any>;

  /**
   * Callback executed when pagination, filters or sorter is changed
   * @param pagination
   * @param filters
   * @param sorter
   * @param currentDataSource
   */
  onChange?: (pagination: any, filters: any, sorter: any, extra: any) => void;

  /**
   * Callback executed when the row expand icon is clicked
   *
   * @param expanded
   * @param record
   */
  onExpand?: (expande: boolean, record: T) => void;

  /**
   * Callback executed when the expanded rows change
   * @param expandedRows
   */
  onExpandedRowsChange?: (expandedRows: string[] | number[]) => void;

  onColumnsChange?: (data: ColumnChangeParam[]) => void;
}

export type CellFormat = string | ((text: string, record: Recordable, index: number) => string | number) | Map<string | number, any>;

// @ts-ignore
export interface BasicColumn extends ColumnProps<Recordable> {
  children?: BasicColumn[];
  filters?: {
    text: string;
    value: string;
    children?: unknown[] | (((props: Record<string, unknown>) => unknown[]) & (() => unknown[]) & (() => unknown[]));
  }[];

  //
  flag?: 'INDEX' | 'DEFAULT' | 'CHECKBOX' | 'RADIO' | 'ACTION';
  // update-begin--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
  title: string | Fn;
  // update-end--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
  customTitle?: VueNode;

  slots?: Recordable;
  // slotsbackup，Compatible with old writing methods，Convert to new writing method to avoid console warnings
  slotsBak?: Recordable;

  // Whether to hide the column by default, it can be displayed in the column configuration
  defaultHidden?: boolean;

  // Help text for table column header
  helpMessage?: string | string[];

  format?: CellFormat;

  // Editable
  edit?: boolean;
  editRow?: boolean;
  editable?: boolean;
  editComponent?: ComponentType;
  // update-begin--author:liaozhiyang---date:20250818---for：【issues/8680】editComponentPropsAccepts a function passed inrecord
  editComponentProps?: Recordable | ((record: Recordable) => Recordable);
  // update-end--author:liaozhiyang---date:20250818---for：【issues/8680】editComponentPropsAccepts a function passed inrecord
  editRule?: boolean | ((text: string, record: Recordable) => Promise<string>);
  editValueMap?: (value: any) => string;
  onEditRow?: () => void;
  // Permission encoding controls whether to display
  auth?: RoleEnum | RoleEnum[] | string | string[];
  // Whether business control is displayed
  ifShow?: boolean | ((column: BasicColumn) => boolean);
  //compType-for record type
  compType?: string;
  // update-begin--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能兼容老ofsummary（Total at the end of the table）
  customSummaryRender?: (opt: {
    value: any;
    text: any;
    record: Recordable;
    index: number;
    renderIndex?: number;
    column: BasicColumn;
  }) => any | VNodeChild | JSX.Element;
  // update-end--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能兼容老ofsummary（Total at the end of the table）
  // 额外of属性
  extraProps?: Recordable;
}

export type ColumnChangeParam = {
  dataIndex: string;
  fixed: boolean | 'left' | 'right' | undefined;
  visible: boolean;
};

export interface InnerHandlers {
  onColumnsChange: (data: ColumnChangeParam[]) => void;
}
