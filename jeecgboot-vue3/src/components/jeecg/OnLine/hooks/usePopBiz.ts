import { reactive, ref, unref, defineAsyncComponent, toRaw, markRaw, isRef, watch, onUnmounted } from 'vue';
import { httpGroupRequest } from '/@/components/Form/src/utils/GroupRequest';
import { defHttp } from '/@/utils/http/axios';
import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil.js';
import { useMessage } from '/@/hooks/web/useMessage';
import { OnlineColumn } from '/@/components/jeecg/OnLine/types/onlineConfig';
import { h } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useMethods } from '/@/hooks/system/useMethods';
import { importViewsFile, _eval } from '/@/utils';
import {getToken} from "@/utils/auth";
import {replaceUserInfoByExpression} from "@/utils/common/compUtils";
import { isString } from '/@/utils/is';

export function usePopBiz(ob, tableRef?) {
  // update-begin--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
  let props: any;
  if (isRef(ob)) {
    props = ob.value;
    const stopWatch = watch(ob, (newVal) => {
      props = newVal;
    });
    onUnmounted(() => stopWatch());
  } else {
    props = ob;
  }
  // update-end--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
  const { createMessage } = useMessage();
  //Pop-up window visible status
  const visible = ref(false);
  //Table loading
  const loading = ref(false);
  //cgRpConfigId
  const cgRpConfigId = ref('');
  //title
  const title = ref('list');
  // sort field，No sorting by default
  const iSorter = ref<any>('');
  // Query object
  const queryInfo = ref([]);
  // query parameters
  const queryParam = ref<any>({});
  // dynamic parameters
  const dynamicParam = ref<any>({});
  //Dictionary configuration items
  const dictOptions = ref({});
  //Dataset
  const dataSource = ref<Array<object>>([]);
  //Define table information
  const columns = ref<Array<object>>([]);
  // current route
  const route = useRoute();
  //define requesturlinformation
  const configUrl = reactive({
    //list页loadcolumnanddata
    getColumnsAndData: '/online/cgreport/api/getColumnsAndData/',
    getColumns: '/online/cgreport/api/getRpColumns/',
    getData: '/online/cgreport/api/getData/',
    getQueryInfo: '/online/cgreport/api/getQueryInfo/',
    export: '/online/cgreport/api/exportManySheetXls/',
  });
  //Selected value
  const checkedKeys = ref<Array<string | number>>([]);
  //Selected row records
  const selectRows = ref<Array<any>>([]);
  // Click on a cell to select a row popupneed 但是Report预览不need
  let clickThenCheckFlag = true;
  if (props.clickToRowSelect === false) {
    clickThenCheckFlag = false;
  }

  /**
   * Select column configuration
   */
  const rowSelection = {
    fixed: true,
    type: props.multi ? 'checkbox' : 'radio',
    selectedRowKeys: checkedKeys,
    selectionRows: selectRows,
    onChange: onSelectChange,
  };

  /**
   * Serial number column configuration
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: '15px',
  };
  /**
   * Paging configuration
   */
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30'],
    // showTotal: (total, range) => {
    //     return range[0] + '-' + range[1] + ' common' + total + 'strip'
    // },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
    // aggregation logic [To be optimized 3.0]
    showTotal: (total) => onShowTotal(total),
    realPageSize: 10,
    realTotal: 0,
    // Is there a total column，Default is""，After the data is obtained for the first time, it will be designed astureorfalse
    isTotal: <string | boolean>'',
    onShowSizeChange: (current, pageSize) => onSizeChange(current, pageSize),
  });

  /**
   * table select event
   * @param selectedRowKeys
   * @param selectRow
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    // update-begin--author:liaozhiyang---date:20240105---for：【QQYUN-7514】popupSingle choice displayradio
    if (!props.multi) {
      selectRows.value = [];
      checkedKeys.value = [];
      // update-begin--author:liaozhiyang---date:20240717---for：【issues/6883】The radio mode is opened for the second time and is checked.
      // selectedRowKeys = [selectedRowKeys[selectedRowKeys.length - 1]];
      // update-end--author:liaozhiyang---date:20240717---for：【issues/6883】The radio mode is opened for the second time and is checked.
    }
    // update-end--author:liaozhiyang---date:20240105---for：【QQYUN-7514】popupSingle choice displayradio
    // update-begin--author:liaozhiyang---date:20230919---for：【QQYUN-4263】Cross-page selection export issue
    if (!selectedRowKeys || selectedRowKeys.length == 0) {
      selectRows.value = [];
      checkedKeys.value = [];
    } else {
      if (selectRows.value.length > selectedRowKeys.length) {
        // Cancel
        selectRows.value.forEach((item, index) => {
          const rowKey = combineRowKey(item);
          if (!selectedRowKeys.find((key) => key === rowKey)) {
            selectRows.value.splice(index, 1);
          }
        });
      } else {
        // New
        const append: any = [];
        const beforeRowKeys = selectRows.value.map((item) => combineRowKey(item));
        selectedRowKeys.forEach((key) => {
          if (!beforeRowKeys.find((item) => item === key)) {
            // 那就是New选中的行
            const row = getRowByKey(key);
            row && append.push(row);
          }
        });
        selectRows.value = [...selectRows.value, ...append];
      }
      checkedKeys.value = [...selectedRowKeys];
    }
    // update-end--author:liaozhiyang---date:20230919---for：【QQYUN-4263】Cross-page selection export issue
  }
  /**
   * Filter useless options
   * @param selectedRowKeys
   */
  function filterUnuseSelect() {
    selectRows.value = unref(selectRows).filter((item) => {
      let combineKey = combineRowKey(item);
      return unref(checkedKeys).indexOf(combineKey) >= 0;
    });
  }

  /**
   * according tokeyGetrowinformation
   * @param key
   */
  function getRowByKey(key) {
    let row = unref(dataSource).filter((record) => combineRowKey(record) === key);
    return row && row.length > 0 ? row[0] : '';
  }

  /**
   * loadrowKey
   */
  function combineRowKey(record) {
    let res = record?.id || '';
    if (props?.rowkey) {
      // update-begin--author:liaozhiyang---date:20250415--for：【issues/3656】popupdictecho
      res = record[props.rowkey];
      // update-end--author:liaozhiyang---date:20250415--for：【issues/3656】popupdictecho
    } else {
      Object.keys(record).forEach((key) => {
        res = key == 'rowIndex' ? record[key] + res : res + record[key];
      });
      res = res.length > 50 ? res.substring(0, 50) : res;
    }
    return res;
  }

  /**
   * load列information
   */
  function loadColumnsInfo() {
    const {code} = handleCodeParams(true)
    let url = `${configUrl.getColumns}${code}`;
    //cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      if (res.success) {
        initDictOptionData(res.result.dictOptions);
        cgRpConfigId.value = res.result.cgRpConfigId;
        title.value = res.result.cgRpConfigName;
        let currColumns = res.result.columns;
        for (let a = 0; a < currColumns.length; a++) {
          if (currColumns[a].customRender) {
            let dictCode = currColumns[a].customRender;
            currColumns[a].customRender = ({ text }) => {
              return filterMultiDictText(unref(dictOptions)[dictCode], text + '');
            };
          }
          // sort field受控
          if (unref(iSorter) && currColumns[a].dataIndex === unref(iSorter).column) {
            currColumns[a].sortOrder = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
          }
        }
        // update-begin--author:liaozhiyang---date:20250114---for：【issues/946】popup列宽and在线Report列宽读取配置
        currColumns.forEach((item) => {
          if (item.fieldWidth != null) {
            if (isString(item.fieldWidth) && item.fieldWidth.trim().length == 0) return;
            item.width = item.fieldWidth;
            delete item.fieldWidth;
          }
        });
        // update-end--author:liaozhiyang---date:20250114---for：【issues/946】popup列宽and在线Report列宽读取配置
        if (currColumns[0].key !== 'rowIndex') {
          currColumns.unshift({
            title: 'serial number',
            dataIndex: 'rowIndex',
            key: 'rowIndex',
            width: 60,
            align: 'center',
            customRender: function ({ text }) {
              // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7584】popup有合计时serial number列会出现NaN
              if (text == undefined) {
                return '';
              } else {
                return parseInt(text) + 1;
              }
              // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7584】popup有合计时serial number列会出现NaN
            },
          });
        }
        columns.value = [...currColumns];
        initQueryInfo(null);
      }
    });
  }

  /**
   * load列and数据[list专用]
   */
  function loadColumnsAndData() {
    // 第一次load Leave blankisTotal Make sure to call here This method is just after entering the page load一次 Do not use this method for other queries
    pagination.isTotal = '';
    let url = `${configUrl.getColumnsAndData}${props.id}`;

    const {query} = handleCodeParams()
    if (query) {
      url = url + query
    }
    //cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      if (res.success) {
        initDictOptionData(res.result.dictOptions);
        cgRpConfigId.value = props.id;
        let { columns: metaColumnList, cgreportHeadName, fieldHrefSlots, isGroupTitle } = res.result;
        title.value = cgreportHeadName;
        // href Jump
        const fieldHrefSlotKeysMap = {};
        fieldHrefSlots.forEach((item) => (fieldHrefSlotKeysMap[item.slotName] = item));
        let currColumns: any = handleColumnHrefAndDict(metaColumnList, fieldHrefSlotKeysMap);
        // update-begin--author:liaozhiyang---date:20250114---for：【issues/946】popup列宽and在线Report列宽读取配置
        currColumns.forEach((item) => {
          if (isString(item.fieldWidth) && item.fieldWidth.trim().length == 0) return;
          if (item.fieldWidth != null) {
            item.width = item.fieldWidth;
            delete item.fieldWidth;
          }
        });
        // update-end--author:liaozhiyang---date:20250114---for：【issues/946】popup列宽and在线Report列宽读取配置

        // popupneedserial number， 普通list不need
        if (clickThenCheckFlag === true) {
          currColumns.unshift({
            title: 'serial number',
            dataIndex: 'rowIndex',
            key: 'rowIndex',
            width: 60,
            align: 'center',
            customRender: function ({ text }) {
              return parseInt(text) + 1;
            },
          });
        }

        // Merge header
        if (isGroupTitle === true) {
          currColumns = handleGroupTitle(currColumns);
        }
        columns.value = [...currColumns];
        initQueryInfo(res.result.data);
      } else {
        //update-begin-author:taoyan date:20220401 for: VUEN-583【vue3】JeecgBootException: sqlBlacklist verification failed,Please contact the administrator!,No prompt at the front desk
        createMessage.warning(res.message);
        //update-end-author:taoyan date:20220401 for: VUEN-583【vue3】JeecgBootException: sqlBlacklist verification failed,Please contact the administrator!,No prompt at the front desk
      }
    });
  }

  // 处理dynamic parametersand系统变量
  function handleCodeParams(onlyCode: boolean = false) {
    if (!props.code) {
      return {code: '', query: ''}
    }
    const firstIndex = props.code.indexOf('?')
    if (firstIndex === -1) {
      return {code: props.code, query: ''}
    }
    const code = props.code.substring(0, firstIndex)
    if (onlyCode) {
      return {code: code, query: ''}
    }
    const queryOrigin = props.code.substring(firstIndex, props.code.length);
    let query: string
    // Replace system variables
    query = replaceUserInfoByExpression(queryOrigin)
    // Get表单值
    if (typeof props.getFormValues === 'function') {
      const values = props.getFormValues()
      // 替换dynamic parameters，if there is ${xxx} then replace it with the actual value
      query = query.replace(/\${([^}]+)}/g, (_$0, $1) => {
        if (values[$1] == null) {
          return ''
        }
        return values[$1]
      });

    }

    return {code, query, queryOrigin}
  }

  /**
   * 处理求andcolumns aggregation logic [To be optimized 3.0]
   */
  function handleSumColumn(metaColumnList: OnlineColumn[], dataTotal: number): void {
    // Getneed合计列的dataIndex
    let sumColumnList = getNeedSumColumns(metaColumnList);
    // 判断是否为第一次Get数据，if yes，则need重新set uppageSize
    if (pagination.isTotal == '') {
      if (sumColumnList.length > 0) {
        pagination.isTotal = true;
        // When there is a total field，Maximum number of original queries per timepageSize-1strip记录，另外need第一次时将查询的10strip中删除最后一strip
        // 删除最后一strip数据 If the data length obtained for the first time is equal topageSizewords，则删除最后一strip
        if (dataSource.value.length == pagination.pageSize) {
          let remove_data = dataSource.value.pop();
        }
        pagination.realPageSize = pagination.pageSize - 1;
      } else {
        pagination.isTotal = false;
      }
    }
    // need添加合计Field
    if (pagination.isTotal) {
      let totalRow = {};
      sumColumnList.forEach((dataIndex) => {
        let count = 0;
        dataSource.value.forEach((row) => {
          // Statistical removalnulland empty data
          if (row[dataIndex] != null && row[dataIndex] != '') {
            count += parseFloat(row[dataIndex]);
          }
        });
        totalRow[dataIndex] = isNaN(count) ? 'Contains non-digital content' : count.toFixed(2);

        // The total is not displayed during long shaping.00suffix
        let v = metaColumnList.find((v) => v.dataIndex == dataIndex);
        if (v && v.fieldType == 'Long') {
          totalRow[dataIndex] = parseInt(totalRow[dataIndex]);
        }
      });
      dataSource.value.push(totalRow);
      pagination.realTotal = dataTotal;
      pagination.total = Number(dataTotal) + Number(Math.floor(dataTotal / pagination.realPageSize));
    }
  }

  /**
   * Getneed求andcolumns dataIndex
   * @param columns
   */
  function getNeedSumColumns(columns: OnlineColumn[]): string[] {
    let arr: string[] = [];
    for (let column of columns) {
      if (column.isTotal === '1') {
        arr.push(column.dataIndex!);
      }
        // 【VUEN-1569】【onlineReport】Invalid total
      if (column.children && column.children.length > 0) {
        let subArray = getNeedSumColumns(column.children);
        if (subArray.length > 0) {
          arr.push(...subArray);
        }
      }
    }
    return arr;
  }

  /**
   * processing columnhrefand字典translate
   */
  function handleColumnHrefAndDict(columns: OnlineColumn[], fieldHrefSlotKeysMap: {}): OnlineColumn[] {
    for (let column of columns) {
      let { customRender, hrefSlotName, fieldType } = column;
      // online Report中类型配置为日期（yyyy-MM-dd ），But the actual display is in date and time format(yyyy-MM-dd HH:mm:ss) issues/3042
      if (fieldType == 'Date') {
        column.customRender = ({ text }) => {
          if (!text) {
            return '';
          }
          if (text.length > 10) {
            return text.substring(0, 10);
          }
          return text;
        };
      } else {
        if (!hrefSlotName && column.scopedSlots && column.scopedSlots.customRender) {
          //【OnlineReport】字典andhrefmutually exclusive pass herefieldHrefSlotKeysMap Find it firsthrefcolumns
          if (fieldHrefSlotKeysMap.hasOwnProperty(column.scopedSlots.customRender)) {
            hrefSlotName = column.scopedSlots.customRender;
          }
        }
        // if customRender A value indicates that a dictionary is used
        // if hrefSlotName A value indicates that it is usedhrefJump
        // Both are compatible。The specific idea of ​​compatibility is：先Get到字典替换value，Add morehref链接Jump
        if (customRender || hrefSlotName) {
          let dictCode = customRender as string;
          let replaceFlag = '_replace_text_';
          column.customRender = ({ text, record }) => {
            let value = text;
            // if dictCode valuable，Just do dictionary conversion
            if (dictCode) {
              if (dictCode.startsWith(replaceFlag)) {
                let textFieldName = dictCode.replace(replaceFlag, '');
                value = record[textFieldName];
              } else {
                value = filterMultiDictText(unref(dictOptions)[dictCode], text + '');
              }
            }
            // Extended parameters set the content length of the column
            if (column.showLength) {
              if (value && value.length > column.showLength) {
                value = value.substr(0, column.showLength) + '...';
              }
            }
            // if hrefSlotName valuable，Just generate one a Label，After wrapping the dictionary and replacing（or native）value
            if (hrefSlotName) {
              let field = fieldHrefSlotKeysMap[hrefSlotName];
              if (field) {
                return h(
                  'a',
                  {
                    onClick: () => handleClickFieldHref(field, record),
                  },
                  value
                );
              }
            }
            return value;
          };
        }
      }
    }
    return columns;
  }

  /**
   * 处理Merge header
   * @param columns
   */
  function handleGroupTitle(columns: OnlineColumn[]): OnlineColumn[] {
    let newColumns: OnlineColumn[] = [];
    for (let column of columns) {
      //sort field受控  ---- 此逻辑为New逻辑 treat
      if (unref(iSorter) && column.dataIndex === unref(iSorter).column) {
        column.sortOrder = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
      }
      //判断Field是否needMerge header
      if (column.groupTitle) {
        let clIndex = newColumns.findIndex((im) => im.title === column.groupTitle);
        if (clIndex !== -1) {
          //The header already exists directlypush children
          newColumns[clIndex].children!.push(column);
        } else {
          //表头不存在组装表头information
          let clGroup: OnlineColumn = {},
            child: OnlineColumn[] = [];
          child.push(column);
          clGroup.title = column.groupTitle;
          clGroup.align = 'center';
          clGroup.children = child;
          newColumns.push(clGroup);
        }
      } else {
        newColumns.push(column);
      }
    }
    return newColumns;
  }

  // Get路由器对象 hrefJump用到
  let router = useRouter();
  /**
   * href click event
   * @param field
   * @param record
   */
  function handleClickFieldHref(field, record) {
    let href = field.href;
    let urlPattern = /(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?/;
    let compPattern = /\.vue(\?.*)?$/;
    let jsPattern = /{{([^}]+)}}/g; // {{ xxx }}
    if (typeof href === 'string') {
      href = href.trim().replace(/\${([^}]+)?}/g, (s1, s2) => record[s2]);
      // implement {{...}} JSEnhancement statement
      if (jsPattern.test(href)) {
        href = href.replace(jsPattern, function (text, s0) {
          try {
            // support {{ ACCESS_TOKEN }} placeholder
            if (s0.trim() === 'ACCESS_TOKEN') {
              return getToken()
            }

            // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalReplace withnew Function，solvebuildwarn
            return _eval(s0);
            // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalReplace withnew Function，solvebuildwarn
          } catch (e) {
            console.error(e);
            return text;
          }
        });
      }
      if (urlPattern.test(href)) {
        window.open(href, '_blank');
      } else if (compPattern.test(href)) {
        // Handling pop-ups
        openHrefCompModal(href);
      } else {
        router.push(href);
      }
    }
  }

  /**
   * Export
   */
  function handleExport() {
    const { handleExportXls } = useMethods();
    let url = `${configUrl.export}${cgRpConfigId.value}`;
    let params = getQueryParams(); //查询strip件
    // 【VUEN-1568】if选中了某些行，就只Export选中的行
    let keys = unref(checkedKeys);
    if (keys.length > 0) {
      keys = keys
        .map((i) => selectRows.value.find((item) => combineRowKey(item) === i)?.id)
        .filter((i) => i != null && i !== '');
      // Determine whether there isIDField
      if (keys.length === 0) {
        createMessage.warning('Due to missing dataIDField，故无法使用选中Export功能');
        return;
      }
      params['force_id'] = keys.join(',');
    }
    handleExportXls(title.value, url, params);
  }

  /**
   * aggregation logic [To be optimized 3.0]
   * Pagination size change event
   * @param _current
   * @param size
   */
  function onSizeChange(_current, size) {
    pagination.isTotal = '';
    pagination.pageSize = size;
    if (pagination.isTotal) {
      pagination.realPageSize = size - 1;
    } else {
      pagination.realPageSize = size;
    }
    pagination.current = 1;
  }

  /**
   *  aggregation logic [To be optimized 3.0]
   * 显示总strip数
   * @param total
   */
  function onShowTotal(total) {
    // 重新according to是否有合计计算每页显示的数据
    let start = (pagination.current - 1) * pagination.realPageSize + 1;
    let end = start + (pagination.isTotal ? dataSource.value.length - 1 : dataSource.value.length) - 1;
    let realTotal = pagination.isTotal ? pagination.realTotal : total;
    return start + '-' + end + ' common' + realTotal + 'strip';
  }

  /**
   * Pop-up box shows hidden trigger event
   */
  async function visibleChange($event) {
    visible.value = $event;
    $event && loadColumnsInfo();
  }

  /**
   * initialization查询strip件
   * @param data data result set
   */
  function initQueryInfo(data) {
    let url = `${configUrl.getQueryInfo}${unref(cgRpConfigId)}`;
    //cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      // console.log("Get查询strip件", res);
      if (res.success) {
        dynamicParamHandler(res.result);
        queryInfo.value = res.result;
        console.log('queryInfo==>', queryInfo.value);
        //查询strip件load后再请求数据
        if (data) {
          setDataSource(data);
          //传递路由parameteranddynamic parameters，Not effective，
          loadData(1);
        } else {
          //no deliverydataQuery data when
          loadData(1);
        }
      } else {
        createMessage.warning(res.message);
      }
    });
  }

  /**
   * load表格数据
   * @param arg
   */
  function loadData(arg?) {
    if (arg == 1) {
      pagination.current = 1;
    }
    let params = getQueryParams(); //查询strip件
    params['onlRepUrlParamStr'] = getUrlParamString();
    console.log('params', params);
    loading.value = true;
    // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-578】onlineReportSQLtranslate，The second page does not turn data
    let url = `${configUrl.getColumnsAndData}${unref(cgRpConfigId)}`;
    // update-end--author:liaozhiyang---date:20240603---for：【TV360X-578】onlineReportSQLtranslate，The second page does not turn data
    const {query} = handleCodeParams()
    if (query) {
      url = url + query
    }
    //cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}${JSON.stringify(params)}` : '';
    httpGroupRequest(() => defHttp.get({ url, params }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-578】onlineReportSQLtranslate，The second page does not turn data
      res.result.dictOptions && initDictOptionData(res.result.dictOptions);
      // update-end--author:liaozhiyang---date:20240603---for：【TV360X-578】onlineReportSQLtranslate，The second page does not turn data
      loading.value = false;
      // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-578】onlineReportSQLtranslate，The second page does not turn data
      let data = res.result.data;
      // update-end--author:liaozhiyang---date:20240603---for：【TV360X-578】onlineReportSQLtranslate，The second page does not turn data
      console.log('表格information:', data);
      setDataSource(data);
    });
  }

  /**
   * Get地址栏的parameter
   */
  function getUrlParamString() {
   let query = route.query;
   let arr:any[] = []
   if(query && Object.keys(query).length>0){
     Object.keys(query).map(k=>{
       arr.push(`${k}=${query[k]}`)
     })
   }
   return arr.join('&')
  }

  /**
   * set updataSource
   */
  function setDataSource(data) {
    if (data) {
      pagination.total = Number(data.total);
      let currentPage = pagination?.current ?? 1;
      for (let a = 0; a < data.records.length; a++) {
        if (!data.records[a].rowIndex) {
          data.records[a].rowIndex = a + (currentPage - 1) * 10;
        }
      }
      dataSource.value = data.records;
      //update-begin-author:taoyan date:2023-2-11 for:issues/356 在线ReportPagination有问题
      //update-begin-author:liusq date:2023-4-04 for:issues/426 repair356Regression errors introduced when JPopupOnlReportModal.vue Unmodified in
      tableRef?.value && tableRef?.value?.setPagination({
        total: Number(data.total)
      })
      //update-end-author:liusq date:2023-4-04  for:issues/426 repair356Regression errors introduced when JPopupOnlReportModal.vue Unmodified in
      //update-end-author:taoyan date:2023-2-11 for:issues/356 在线ReportPagination有问题
    } else {
      pagination.total = 0;
      dataSource.value = [];
    }
    // aggregation logic [To be optimized 3.0]
    handleSumColumn(columns.value, pagination.total);
  }

  /**
   * Getquery parameters
   */
  function getQueryParams() {
    let paramTarget = {};
    if (unref(dynamicParam)) {
      //Handle custom parameters
      Object.keys(unref(dynamicParam)).map((key) => {
        paramTarget['self_' + key] = unref(dynamicParam)[key];
      });
    }
    let param = Object.assign(paramTarget, unref(queryParam), unref(iSorter));
    param.pageNo = pagination.current;
    // aggregation logic [To be optimized 3.0]
    //  Not used in actual queriestablecomponentpageSize，Instead of using a customrealPageSize,realPageSize会在第一次Get到数据后变化
    param.pageSize = pagination.realPageSize;
    return filterObj(param);
  }

  /**
   * 处理dynamic parameters
   */
  function dynamicParamHandler(arr?) {
    if (arr && arr.length > 0) {
      //第一次load查询strip件前 initializationqueryParamis an empty object
      let queryTemp = {};
      for (let item of arr) {
        if (item.mode === 'single') {
          queryTemp[item.field] = '';
        }
      }
      queryParam.value = { ...queryTemp };
    }
    // Merge routing parameters
    if (props.routeQuery) {
      queryParam.value = Object.assign(queryParam.value, props.routeQuery);
    }

    let dynamicTemp = {};
    if (props.param) {
      Object.keys(props.param).map((key) => {
        let str = props.param[key];
        //【issues/8426】solveJPopupComponent parameters cannot be received
        if (key in queryParam.value) {
          if (str && str.startsWith("'") && str.endsWith("'")) {
            str = str.substring(1, str.length - 1);
          }
          //if查询strip件包含parameter set up值
          unref(queryParam)[key] = str;
        }
        dynamicTemp[key] = props.param[key];
      });
    }
    dynamicParam.value = { ...dynamicTemp };
  }

  /**
   * Pagination
   * @param page
   * @param filters
   * @param sorter
   */
  function handleChangeInTable(page, filters, sorter) {
    console.log(page, filters, sorter);
    //Pagination、sort、Triggered when filter changes
    if (Object.keys(sorter).length > 0) {
      iSorter.value = {
        column: sorter.field,
        order: 'ascend' === sorter.order ? 'asc' : 'desc',
      };
      // sort field受控
      unref(columns).forEach((col) => {
        if (col['dataIndex'] === sorter.field) {
          col['sortOrder'] = sorter.order;
        }
      });
    }
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadData();
  }

  /**
   * 行click event
   * @param record
   */
  function clickThenCheck(record) {
    if (clickThenCheckFlag === true) {
      // update-begin--author:liaozhiyang---date:20240104---for：【QQYUN-7514】popupSingle choice displayradio
      if (!props.multi) {
        selectRows.value = [];
        checkedKeys.value = [];
      }
      // update-end--author:liaozhiyang---date:20240104---for：【QQYUN-7514】popupSingle choice displayradio
      let rowKey = combineRowKey(record);
      if (!unref(checkedKeys) || unref(checkedKeys).length == 0) {
        let arr1: any[] = [],
          arr2: any[] = [];
        arr1.push(record);
        arr2.push(rowKey);
        checkedKeys.value = arr2;
        //selectRows.value = arr1;
      } else {
        if (unref(checkedKeys).indexOf(rowKey) < 0) {
          //Select if it does not exist
          checkedKeys.value.push(rowKey);
          //selectRows.value.push(record);
        } else {
          //已选中就Cancel
          let rowKey_index = unref(checkedKeys).indexOf(rowKey);
          checkedKeys.value.splice(rowKey_index, 1);
          //selectRows.value.splice(rowKey_index, 1);
        }
      }
      // update-begin--author:liaozhiyang---date:20230914---for：【issues/5357】Click on row to select
      tableRef.value.setSelectedRowKeys([...checkedKeys.value]);
      // update-end--author:liaozhiyang---date:20230914---for：【issues/5357】Click on row to select
    }
  }

  //Prevent junk data in the dictionary
  function initDictOptionData(arr) {
    let obj = {};
    Object.keys(arr).map((k) => {
      obj[k] = arr[k].filter((item) => {
        return item != null;
      });
    });
    dictOptions.value = obj;
  }

  /**
   * Filter empty properties in objects
   * @param obj
   * @returns {*}
   */
  function filterObj(obj) {
    if (!(typeof obj == 'object')) {
      return;
    }

    for (let key in obj) {
      if (obj.hasOwnProperty(key) && (obj[key] == null || obj[key] == undefined || obj[key] === '')) {
        delete obj[key];
      }
    }
    return obj;
  }

  // style
  const dialogStyle = {
    top: 0,
    left: 0,
    height: '100%',
    margin: 0,
    padding: 0,
  };

  // Pop-up window property configuration
  const hrefComponent = ref({
    model: {
      title: '',
      okText: 'closure',
      width: '100%',
      open: false,
      destroyOnClose: true,
      style: dialogStyle,
      // dialogStyle: dialogStyle,
      bodyStyle: {
        padding: '8px',
        height: 'calc(100vh - 108px)',
        overflow: 'auto',
        overflowX: 'hidden',
      },
      // 隐藏掉Cancel按钮
      cancelButtonProps: { style: { display: 'none' } },
    },
    on: {
      ok: () => (hrefComponent.value.model.open = false),
      cancel: () => (hrefComponent.value.model.open = false),
    },
    is: <any>null,
    params: {},
  });

  // 超链click event--> open amodalwindow
  function openHrefCompModal(href) {
    // parse href parameter
    let index = href.indexOf('?');
    let path = href;
    if (index !== -1) {
      path = href.substring(0, index);
      let paramString = href.substring(index + 1, href.length);
      let paramArray = paramString.split('&');
      let params = {};
      paramArray.forEach((paramObject) => {
        let paramItem = paramObject.split('=');
        params[paramItem[0]] = paramItem[1];
      });
      hrefComponent.value.params = params;
    } else {
      hrefComponent.value.params = {};
    }
    hrefComponent.value.model.open = true;
    hrefComponent.value.model.title = 'operate';
    hrefComponent.value.is = markRaw(defineAsyncComponent(() => importViewsFile(path)));
  }

  //update-begin-author:taoyan date:2022-5-31 for: VUEN-1155 popup When selecting data，会选择多strip重复数据
  /**
   * emitevent Get选中的行数据
   */
  function getOkSelectRows(): any[] {
    let arr = unref(selectRows);
    let selectedRowKeys = checkedKeys.value;
    console.log('arr', arr);
    if (!selectedRowKeys || selectedRowKeys.length <= 0) {
      return [];
    }
    if (!arr || arr.length <= 0) {
      return [];
    }
    let rows: any = [];
    for (let key of selectedRowKeys) {
      for (let i = 0; i < arr.length; i++) {
        let combineKey = combineRowKey(arr[i]);
        if (key === combineKey) {
          rows.push(toRaw(arr[i]));
          break;
        }
      }
    }
    return rows;
  }
  //update-end-author:taoyan date:2022-5-31 for: VUEN-1155 popup When selecting data，会选择多strip重复数据

  return [
    {
      visibleChange,
      loadColumnsInfo,
      loadColumnsAndData,
      dynamicParamHandler,
      loadData,
      handleChangeInTable,
      combineRowKey,
      clickThenCheck,
      filterUnuseSelect,
      handleExport,
      getOkSelectRows,
    },
    {
      hrefComponent,
      visible,
      rowSelection,
      checkedKeys,
      selectRows,
      pagination,
      dataSource,
      columns,
      indexColumnProps,
      loading,
      title,
      iSorter,
      queryInfo,
      queryParam,
      dictOptions,
    },
  ];
}
