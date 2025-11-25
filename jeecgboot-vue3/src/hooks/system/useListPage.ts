import { reactive, ref, Ref, unref } from 'vue';
import { merge } from 'lodash-es';
import { DynamicProps } from '/#/utils';
import { BasicTableProps, TableActionType, useTable } from '/@/components/Table';
import { ColEx } from '/@/components/Form/src/types';
import { FormActionType } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { useMethods } from '/@/hooks/system/useMethods';
import { useDesign } from '/@/hooks/web/useDesign';
import { filterObj } from '/@/utils/common/compUtils';
import { isFunction } from '@/utils/is';
const { handleExportXls, handleImportXls } = useMethods();

// definition useListPage Parameters required by the method
interface ListPageOptions {
  // style scope
  designScope?: string;
  // 【Required】Table parameter configuration
  tableProps: TableProps;
  // Whether to paginate
  pagination?: boolean;
  // Export configuration
  exportConfig?: {
    url: string | (() => string);
    // Export file name
    name?: string | (() => string);
    //Export parameters
    params?: object | (() => object);
  };
  // Import configuration
  importConfig?: {
    //update-begin-author:taoyan date:20220507 for: erpcode generation Subtable The import address is dynamic
    url: string | (() => string);
    //update-end-author:taoyan date:20220507 for: erpcode generation Subtable The import address is dynamic
    // Callback after successful export
    success?: (fileInfo?: any) => void;
  };
}

interface IDoRequestOptions {
  // Whether to display a confirmation dialog box，default true
  confirm?: boolean;
  // Whether to automatically refresh the table，default true
  reload?: boolean;
  // Whether to automatically clear the selection，default true
  clearSelection?: boolean;
}

/**
 * listPagePage public methods
 *
 * @param options
 */
export function useListPage(options: ListPageOptions) {
  const $message = useMessage();
  let $design = {} as ReturnType<typeof useDesign>;
  if (options.designScope) {
    $design = useDesign(options.designScope);
  }

  const tableContext = useListTable(options.tableProps);

  const [, { getForm, reload, setLoading }, { selectedRowKeys }] = tableContext;

  // Export excel
  async function onExportXls() {
    //update-begin---author:wangshuai ---date:20220411  for：Export新增自definitionparameter------------
    let { url, name, params } = options?.exportConfig ?? {};
    let realUrl = typeof url === 'function' ? url() : url;
    if (realUrl) {
      let title = typeof name === 'function' ? name() : name;
      //update-begin-author:taoyan date:20220507 for: erpcode generation Subtable Export报错，Unknown reason-
      let paramsForm:any = {};
      try {
        //update-begin-author:liusq---date:2025-03-20--for: [QQYUN-11627]code generation原生表单，数据Export，Front-end error，and the range parameter is not converted #7962
        //whenuseSearchFornot equal tofalsewhen，Just trigger itvalidate
        if (options?.tableProps?.useSearchForm !== false) {
          paramsForm = await getForm().validate();
          console.log('paramsForm', paramsForm);
        }
        //update-end-author:liusq---date:2025-03-20--for:[QQYUN-11627]code generation原生表单，数据Export，Front-end error，and the range parameter is not converted #7962
      } catch (e) {
        console.warn(e);
      }
      //update-end-author:taoyan date:20220507 for: erpcode generation Subtable Export报错，Unknown reason-

      //update-begin-author:liusq date:20230410 for:[/issues/409]Export功能没有按排序结果Export,设置Exportdefault排序，Creation time in reverse order
      if(!paramsForm?.column){
         Object.assign(paramsForm,{column:'createTime',order:'desc'});
      }
      //update-begin-author:liusq date:20230410 for: [/issues/409]Export功能没有按排序结果Export,设置Exportdefault排序，Creation time in reverse order

      //If the parameter is not empty，are integrated together
      //update-begin-author:taoyan date:20220507 for: erpcode generation Subtable Export动态设置mainId
      if (params) {
        //update-begin-author:liusq---date:2025-03-20--for: [QQYUN-11627]code generation原生表单，数据Export，Front-end error，and the range parameter is not converted #7962
        const realParams = isFunction(params) ? await params() : { ...(params || {}) };
        //update-end-author:liusq---date:2025-03-20--for:[QQYUN-11627]code generation原生表单，数据Export，Front-end error，and the range parameter is not converted #7962
        Object.keys(realParams).map((k) => {
          let temp = (realParams as object)[k];
          if (temp) {
            paramsForm[k] = unref(temp);
          }
        });
      }
      //update-end-author:taoyan date:20220507 for: erpcode generation Subtable Export动态设置mainId
      if (selectedRowKeys.value && selectedRowKeys.value.length > 0) {
        paramsForm['selections'] = selectedRowKeys.value.join(',');
      }
      console.log()
      return handleExportXls(title as string, realUrl, filterObj(paramsForm));
      //update-end---author:wangshuai ---date:20220411  for：Export新增自definitionparameter--------------
    } else {
      $message.createMessage.warn('no delivery exportConfig.url parameter');
      return Promise.reject();
    }
  }

  // import excel
  function onImportXls(file) {
    let { url, success } = options?.importConfig ?? {};
    //update-begin-author:taoyan date:20220507 for: erpcode generation Subtable The import address is dynamic
    let realUrl = typeof url === 'function' ? url() : url;
    if (realUrl) {
      return handleImportXls(file, realUrl, success || reload);
      //update-end-author:taoyan date:20220507 for: erpcode generation Subtable The import address is dynamic
    } else {
      $message.createMessage.warn('no delivery importConfig.url parameter');
      return Promise.reject();
    }
  }

  /**
   * Common request handling methods，Automatically refresh the table，Automatically clear selection
   * @param api askapi
   * @param options Whether to display a confirmation box
   */
  function doRequest(api: () => Promise<any>, options?: IDoRequestOptions) {
    return new Promise((resolve, reject) => {
      const execute = async () => {
        try {
          setLoading(true);
          const res = await api();
          if (options?.reload ?? true) {
            reload();
          }
          if (options?.clearSelection ?? true) {
            selectedRowKeys.value = [];
          }
          resolve(res);
        } catch (e) {
          reject(e);
        } finally {
          setLoading(false);
        }
      };
      if (options?.confirm ?? true) {
        $message.createConfirm({
          iconType: 'warning',
          title: 'delete',
          content: '确定要delete吗？',
          onOk: () => execute(),
          onCancel: () => reject(),
        });
      } else {
        execute();
      }
    });
  }

  /** 执行单个deleteoperate */
  function doDeleteRecord(api: () => Promise<any>) {
    return doRequest(api, { confirm: false, clearSelection: false });
  }

  return {
    ...$design,
    ...$message,
    onExportXls,
    onImportXls,
    doRequest,
    doDeleteRecord,
    tableContext,
  };
}

// definition表格所需parameter
type TableProps = Partial<DynamicProps<BasicTableProps>>;
type UseTableMethod = TableActionType & {
  getForm: () => FormActionType;
};

/**
 * useListTable 列表页面标准表格parameter
 *
 * @param tableProps 表格parameter
 */
export function useListTable(tableProps: TableProps): [
  (instance: TableActionType, formInstance: UseTableMethod) => void,
  TableActionType & {
    getForm: () => FormActionType;
  },
  {
    rowSelection: any;
    selectedRows: Ref<Recordable[]>;
    selectedRowKeys: Ref<any[]>;
  }
] {
  // Adaptive column configuration
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 12, // ≥576px
    md: 12, // ≥768px
    lg: 8, // ≥992px
    xl: 8, // ≥1200px
    xxl: 6, // ≥1600px
  };
  const defaultTableProps: TableProps = {
    rowKey: 'id',
    // Use search criteria area
    useSearchForm: true,
    // Query condition area configuration
    formConfig: {
      // compact mode
      compact: true,
      // labeldefault宽度
      // labelWidth: 120,
      // Automatically submit after pressing Enter
      autoSubmitOnEnter: true,
      // default row Configuration
      rowProps: { gutter: 8 },
      // default col Configuration
      baseColProps: {
        ...adaptiveColProps,
      },
      labelCol: {
        xs: 24,
        sm: 8,
        md: 6,
        lg: 8,
        xl: 6,
        xxl: 6,
      },
      wrapperCol: {},
      // Whether to display Expand/close button
      showAdvancedButton: true,
      // 超过指定列数default折叠
      autoAdvancedCol: 3,
      // operatebuttonConfiguration
      actionColOptions: {
        ...adaptiveColProps,
        style: { textAlign: 'left' },
      },
    },
    // zebra print
    striped: false,
    // Is it possible to adjust the height?
    canResize: true,
    // table minimum height
    // update-begin--author:liaozhiyang---date:20240603---for【TV360X-861】The list query area cannot be scrolled up.
    minHeight: 300,
    // update-end--author:liaozhiyang---date:20240603---for【TV360X-861】The list query area cannot be scrolled up.
    // Click on row to select
    clickToRowSelect: false,
    // Whether to display边框
    bordered: true,
    // Whether to display序号列
    showIndexColumn: false,
    // Show table settings
    showTableSetting: true,
    // Table full screen setting
    tableSetting: {
      fullScreen: false,
    },
    // Whether to displayOperation column
    showActionColumn: true,
    // Operation column
    actionColumn: {
      width: 120,
      title: 'operate',
      //是否锁定Operation column取值 right ,left,false
      fixed: false,
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  };
  // 合并用户个性化Configuration
  if (tableProps) {
    //update-begin---author:wangshuai---date:2024-04-28---for:【issues/6180】前端代码Configuration表变查询条件显示列不生效---
    if(tableProps.formConfig){
      setTableProps(tableProps.formConfig);
    }
    //update-end---author:wangshuai---date:2024-04-28---for:【issues/6180】前端代码Configuration表变查询条件显示列不生效---
    // merge Methods to merge objects deeply
    merge(defaultTableProps, tableProps);
  }

  // 发送ask之前调用的方法
  function beforeFetch(params) {
    // default以 createTime Sort descending
    return Object.assign({ column: 'createTime', order: 'desc' }, params);
  }

  // merge method
  Object.assign(defaultTableProps, { beforeFetch });
  if (typeof tableProps.beforeFetch === 'function') {
    defaultTableProps.beforeFetch = function (params) {
      params = beforeFetch(params);
      // @ts-ignore
      tableProps.beforeFetch(params);
      return params;
    };
  }

  // when前选择的行
  const selectedRowKeys = ref<any[]>([]);
  // Selected row records
  const selectedRows = ref<Recordable[]>([]);

  // 表格选择列Configuration
  const rowSelection: any = tableProps?.rowSelection ?? {};
  const defaultRowSelection = reactive({
    ...rowSelection,
    type: rowSelection.type ?? 'checkbox',
    // Select column width，default 50
    columnWidth: rowSelection.columnWidth ?? 50,
    selectedRows: selectedRows,
    selectedRowKeys: selectedRowKeys,
    onChange(...args) {
      selectedRowKeys.value = args[0];
      selectedRows.value = args[1];
      if (typeof rowSelection.onChange === 'function') {
        rowSelection.onChange(...args);
      }
    },
  });
  delete defaultTableProps.rowSelection;

  /**
   * 设置表格parameter
   *
   * @param formConfig
   */
  function setTableProps(formConfig: any) {
    const replaceAttributeArray: string[] = ['baseColProps','labelCol'];
    for (let item of replaceAttributeArray) {
      if(formConfig && formConfig[item]){
        if(defaultTableProps.formConfig){
          let defaultFormConfig:any = defaultTableProps.formConfig;
          defaultFormConfig[item] = formConfig[item];
        }
        formConfig[item] = {};
      }
    }
  }

  return [
    ...useTable(defaultTableProps),
    {
      selectedRows,
      selectedRowKeys,
      rowSelection: defaultRowSelection,
    },
  ];
}
