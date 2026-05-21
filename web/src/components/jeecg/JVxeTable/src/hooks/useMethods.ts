import { Ref, watch } from 'vue';
import XEUtils from 'xe-utils';
import { simpleDebounce } from '/@/utils/common/compUtils';
import { JVxeDataProps, JVxeRefs, JVxeTableProps, JVxeTypes } from '../types';
import { getEnhanced } from '../utils/enhancedUtils';
import { VxeTableInstance, VxeTablePrivateMethods } from 'vxe-table';
import { cloneDeep } from 'lodash-es';
import { isArray, isEmpty, isNull, isString } from '/@/utils/is';
import { useLinkage } from './useLinkage';
import { useWebSocket } from './useWebSocket';
import { getPrefix, getJVxeAuths } from '../utils/authUtils';
import { excludeKeywords } from '../componentMap';
import { useColumnsCache } from './useColumnsCache';
import { isEnabledVirtualYScroll } from '/@/components/jeecg/JVxeTable/utils';

export function useMethods(props: JVxeTableProps, { emit }, data: JVxeDataProps, refs: JVxeRefs, instanceRef: Ref) {
  let xTableTemp: VxeTableInstance & VxeTablePrivateMethods;

  function getXTable() {
    if (!xTableTemp) {
      // !. for typescript non-null assertion
      xTableTemp = refs.gridRef.value!.getRefMaps().refTable.value;
    }
    return xTableTemp;
  }

  // noinspection JSUnusedGlobalSymbols
  const hookMethods = {
    getXTable,
    addRows,
    pushRows,
    insertRows,
    addOrInsert,
    setValues,
    getValues,
    getTableData,
    getNewData,
    getNewDataWithId,
    getIfRowById,
    getNewRowById,
    getDeleteData,
    getSelectionData,
    getSelectedData,
    removeRows,
    removeRowsById,
    removeSelection,
    resetScrollTop,
    validateTable,
    fullValidateTable,
    clearSelection,
    filterNewRows,
    isDisabledRow,
    recalcDisableRows,
    rowResort,
  };

  // Multi-level linkage
  const linkageMethods = useLinkage(props, data, hookMethods);
  // WebSocket Refresh without trace
  const socketMethods = useWebSocket(props, data, hookMethods);

  // Methods that can be explicitly called externally
  const publicMethods = {
    ...hookMethods,
    ...linkageMethods,
    ...socketMethods,
  };

  /** monitorvxescroll bar position */
  function handleVxeScroll(event) {
    let { scroll } = data;

    // Record the position of the scroll bar
    scroll.top = event.scrollTop;
    scroll.left = event.scrollLeft;

    refs.subPopoverRef.value?.close();
    data.scrolling.value = true;
    closeScrolling();
  }

  // Event triggered when a radio option is manually checked
  function handleVxeRadioChange(event) {
    let row = event.$table.getRadioRecord();
    data.selectedRows.value = row ? [row] : [];
    handleSelectChange('radio', data.selectedRows.value, event);
  }

  // Event triggered when manually selecting all
  function handleVxeCheckboxAll(event) {
    data.selectedRows.value = event.$table.getCheckboxRecords();
    handleSelectChange('checkbox-all', data.selectedRows.value, event);
  }

  // Event triggered when manually checked and the value changes
  function handleVxeCheckboxChange(event) {
    data.selectedRows.value = event.$table.getCheckboxRecords();
    handleSelectChange('checkbox', data.selectedRows.value, event);
  }

  // row selectionchangeevent
  function handleSelectChange(type, selectedRows, $event) {
    let action;
    if (type === 'radio') {
      action = 'selected';
    } else if (type === 'checkbox') {
      action = selectedRows.includes($event.row) ? 'selected' : 'unselected';
    } else {
      action = 'selected-all';
    }

    data.selectedRowIds.value = selectedRows.map((row) => row.id);
    trigger('selectRowChange', {
      type: type,
      action: action,
      $event: $event,
      row: $event.row,
      selectedRows: data.selectedRows.value,
      selectedRowIds: data.selectedRowIds.value,
    });
  }

  // 点击单元格hourtriggeredevent
  function handleCellClick(event) {
    let { row, column, $event, $table } = event;

    // Editable clicked
    if (column.editRender) {
      refs.subPopoverRef.value?.close();
      return;
    }

    // Show details
    if (column.params?.showDetails) {
      refs.detailsModalRef.value?.open(event);
    } else if (refs.subPopoverRef.value) {
      refs.subPopoverRef.value.toggle(event);
    } else if (props.clickSelectRow) {
      let className = $event.target.className || '';
      className = isString(className) ? className : className.toString();
      // Clicked onexpand，No processing
      if (className.includes('vxe-table--expand-btn')) {
        return;
      }
      // Clicked oncheckbox，No processing
      if (className.includes('vxe-checkbox--icon') || className.includes('vxe-cell--checkbox')) {
        return;
      }
      // Clicked onradio，No processing
      if (className.includes('vxe-radio--icon') || className.includes('vxe-cell--radio')) {
        return;
      }
      if (props.rowSelectionType === 'radio') {
        $table.setRadioRow(row);
        handleVxeRadioChange(event);
      } else {
        $table.toggleCheckboxRow(row);
        handleVxeCheckboxChange(event);
      }
    }
  }

  // 单元格被激活编辑hour会trigger该event
  function handleEditActived({ column }) {
    // Execution enhancement
    getEnhanced(column.params.type).aopEvents.editActived!.apply(instanceRef.value, arguments as any);
  }

  // 单元格编辑状态下被关闭hour会trigger该event
  function handleEditClosed({ column }) {
    // Execution enhancement
    getEnhanced(column.params.type).aopEvents.editClosed!.apply(instanceRef.value, arguments as any);
  }

  // The return value determines whether the row can be selected
  function handleCheckMethod({ row }) {
    if (props.disabled) {
      return false;
    }
    return !data.disabledRowIds.includes(row.id);
  }

  // The return value determines whether the cell can be edited
  function handleActiveMethod({ row, column }) {
    let flag = (() => {
      if (props.disabled) {
        return false;
      }
      if (data.disabledRowIds.includes(row.id)) {
        return false;
      }
      if (column.params?.disabled) {
        return false;
      }
      // Execution enhancement
      return getEnhanced(column.params.type).aopEvents.activeMethod!.apply(instanceRef.value, arguments as any) ?? true;
    })();
    if (!flag) {
      // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-1404】vxetablewarn
      getXTable().clearEdit();
      // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-1404】vxetablewarn
    }
    return flag;
  }

  /**
   * Determine whether the line is disabled
   * @param row row data
   * @param rowIndex Line number
   * @param force Whether to force judgment
   */
  function isDisabledRow(row, rowIndex: number | boolean = -1, force = true) {
    if(typeof rowIndex === 'boolean'){
      force = rowIndex;
      rowIndex = -1;
    }
    if (!force) {
      return !data.disabledRowIds.includes(row.id);
    }
    if (props.disabledRows == null || isEmpty(props.disabledRows)) {
      return false;
    }
    let disabled: boolean = false;
    let keys: string[] = Object.keys(props.disabledRows);
    for (const key of keys) {
      // Determine whether the attribute exists
      if (row.hasOwnProperty(key)) {
        let value = row[key];
        let temp: any = props.disabledRows![key];
        // The disabling rule can be a function
        if (typeof temp === 'function') {
          disabled = temp(value, row, rowIndex);
        } else if (isArray(temp)) {
          // Disable rules can be an array
          disabled = temp.includes(value);
        } else {
          // The disabling rule can be a specific value
          disabled = temp === value;
        }
        if (disabled) {
          break;
        }
      }
    }
    return disabled;
  }

  // Recalculate disabled rows
  function recalcDisableRows() {
    let xTable = getXTable();
    data.disabledRowIds = [];
    const { tableFullData } = xTable.internalData;
    tableFullData.forEach((row, rowIndex) => {
      // Determine whether the line is disabled
      if (isDisabledRow(row, rowIndex)) {
        data.disabledRowIds.push(row.id);
      }
    });
    xTable.updateData();
  }

  // monitor disabledRows，更改hourRecalculate disabled rows
  watch(
    () => props.disabledRows,
    () => recalcDisableRows()
  );

  // The return value determines whether expansion is allowed、Collapse row
  function handleExpandToggleMethod({ expanded }) {
    return !(expanded && props.disabled);
  }

  // set up data.scrolling Anti-shake mode
  const closeScrolling = simpleDebounce(function () {
    data.scrolling.value = false;
  }, 100);

  /** Table tail data processing method，Used to display statistics */
  function handleFooterMethod({ columns, data: $data }) {
    const { statistics } = data;
    let footers: any[] = [];
    if (statistics.has) {
      if (statistics.sum.length > 0) {
        footers.push(
          getFooterStatisticsMap({
            columns: columns,
            title: 'total',
            checks: statistics.sum,
            method: (column) => XEUtils.sum($data, column.property),
          })
        );
      }
      if (statistics.average.length > 0) {
        footers.push(
          getFooterStatisticsMap({
            columns: columns,
            title: 'average',
            checks: statistics.average,
            method: (column) => XEUtils.mean($data, column.property),
          })
        );
      }
    }
    return footers;
  }

  /** Get bottom statisticsMap */
  function getFooterStatisticsMap({ columns, title, checks, method }) {
    return columns.map((column, columnIndex) => {
      if (columnIndex === 0) {
        return title;
      }
      if (checks.includes(column.property)) {
        return method(column, columnIndex);
      }
      return null;
    });
  }

  // Create new row，Automatically add default value
  function createRow(record: Recordable = {}) {
    let xTable = getXTable();
    // Add default value
    xTable.internalData.tableFullColumn.forEach((column) => {
      let col = column.params;
      // Columns that cannot be registered do not get enhancements
      if (col && !excludeKeywords.includes(col.type)) {
        if (col.key && (record[col.key] == null || record[col.key] === '')) {
          // set up默认值
          let createValue = getEnhanced(col.type).createValue;
          let defaultValue = col.defaultValue ?? '';
          let ctx = { context: { row: record, column, $table: xTable } };
          record[col.key] = createValue(defaultValue, ctx);
        }
        // Process linked columns
        if (col.type === JVxeTypes.select && data.innerLinkageConfig.size > 0) {
          // Determine whether the current column is a linked column
          if (data.innerLinkageConfig.has(col.key)) {
            let configItem = data.innerLinkageConfig.get(col.key);
            linkageMethods.getLinkageOptionsAsync(configItem, '');
          }
        }
      } else if (col?.type === JVxeTypes.hidden) {
        record[col.key] = col.defaultValue ?? '';
      }
    });
    return record;
  }

  async function addOrInsert(rows: Recordable | Recordable[] = {}, index, triggerName, options?: IAddRowsOptions) {
    let xTable = getXTable();
    let records;
    if (isArray(rows)) {
      records = rows;
    } else {
      records = [rows];
    }
    // 遍历Add default value
    records.forEach((record) => createRow(record));
    let setActive = options?.setActive ?? props.addSetActive ?? true;
    let result = await pushRows(records, { index: index, setActive });
    // Iterate over inserted rows
    // online jsEnhancehour以传过来值for准，No more default values
    if (!(options?.isOnlineJS ?? false)) {
      if (triggerName != null) {
        for (let i = 0; i < result.rows.length; i++) {
          let row = result.rows[i];
          trigger(triggerName, {
            row: row,
            rows: result.rows,
            insertIndex: index,
            $table: xTable,
            target: instanceRef.value,
            isModalData: options?.isModalData
          });
        }
      }
    }
    return result;
  }

  // New、Optional parameters when inserting a row
  interface IAddRowsOptions {
    // whether it is onlineJSEnhance triggered
    isOnlineJS?: boolean;
    // Whether to activate editing status
    setActive?: boolean;
    //Does it need to be triggered?changeevent
    emitChange?:boolean
    // whether it ismodalData added by pop-up window
    isModalData?:boolean
  }

  /**
   * Add one or more rows
   *
   * @param rows
   * @param options parameter
   * @return
   */
  async function addRows(rows: Recordable | Recordable[] = {}, options?: IAddRowsOptions) {
    //update-begin-author:taoyan date:2022-8-12 for: VUEN-1892【onlineSub-table pop-up box】There is a master-slave relationshipjshour，Sub-table pop-up box修改了data，Main table fields have not been modified
    let result = await addOrInsert(rows, -1, 'added', options);
    if(options && options!.emitChange==true){
      trigger('valueChange', {column: 'all', row: result.row})
    }
    // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-279】Row editing adds new fields and scrolls to corresponding positions
    let xTable = getXTable();
    setTimeout(() => {
      xTable.scrollToRow(result.row);
    }, 0);
    // update-end--author:liaozhiyang---date:20240607---for：【TV360X-279】Row editing adds new fields and scrolls to corresponding positions
    return result;
    //update-end-author:taoyan date:2022-8-12 for: VUEN-1892【onlineSub-table pop-up box】There is a master-slave relationshipjshour，Sub-table pop-up box修改了data，Main table fields have not been modified
  }

  /**
   * Add one or more rows临hourdata，Default value will not be populated，Whatever is passed is added.
   * @param rows
   * @param options Options
   * @param options.setActive Whether to activate the editing mode of the last line
   */
  async function pushRows(rows: Recordable | Recordable[] = {}, options = { setActive: false, index: -1 }) {
    let xTable = getXTable();
    let { setActive, index } = options;
    index = index === -1 ? index : xTable.internalData.tableFullData[index];
    index = index == null ? -1 : index;
    // Insert row
    let result = await xTable.insertAt(rows, index);
    if (setActive) {
      // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-1404】vxetablewarn
      // Activate edit mode for the last line
      xTable.setEditRow(result.rows[result.rows.length - 1], true);
      // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-1404】vxetablewarn
    }
    await recalcSortNumber();
    return result;
  }

  /**
   * 插入一行或多行临hourdata
   *
   * @param rows
   * @param index Add subscript，number，Required
   * @param options parameter
   * @return
   */
  function insertRows(rows: Recordable | Recordable[] = {}, index: number, options?: IAddRowsOptions) {
    if (index < 0) {
      console.warn(`【JVxeTable】insertRows：index必须传递number，and greater than-1`);
      return;
    }
    return addOrInsert(rows, index, 'inserted', options);
  }

  /** Get the value in the table form */
  function getValues(callback, rowIds) {
    let tableData = getTableData({ rowIds: rowIds });
    // update-begin--author:liaozhiyang---date:20241227---for：【issues/7631】JVxeTablecomponentgetValues回调函数parameter修正
    callback(tableData, tableData);
    // update-end--author:liaozhiyang---date:20241227---for：【issues/7631】JVxeTablecomponentgetValues回调函数parameter修正
  }

  type getTableDataOptions = {
    rowIds?: string[];
    // Whether to retain new linesid
    keepNewId?: boolean;
  }

  /** Get table data */
  function getTableData(options: getTableDataOptions = {}) {
    let { rowIds } = options;
    let tableData;
    // Only query specifiedidof rows
    if (isArray(rowIds) && rowIds.length > 0) {
      tableData = [];
      rowIds.forEach((rowId) => {
        let { row } = getIfRowById(rowId);
        if (row) {
          tableData.push(row);
        }
      });
    } else {
      // Query all rows
      tableData = getXTable().getTableData().fullData;
    }
    return filterNewRows(tableData, {
      keepNewId: options.keepNewId ?? false,
      removeNewLine: false,
    });
  }

  /** 仅获取New的data */
  function getNewData() {
    let newData = getNewDataWithId();
    newData.forEach((row) => delete row.id);
    return newData;
  }

  /** 仅获取New的data,withid */
  function getNewDataWithId() {
    let xTable = getXTable();
    return cloneDeep(xTable.getInsertRecords());
  }

  /** according toIDGet row，Newof rows也能查出来 */
  function getIfRowById(id) {
    let xTable = getXTable();
    let row = xTable.getRowById(id),
      isNew = false;
    if (!row) {
      row = getNewRowById(id);
      if (!row) {
        console.warn(`JVxeTable.getIfRowById：not foundidfor"${id}"of rows`);
        return { row: null };
      }
      isNew = true;
    }
    return { row, isNew };
  }

  /** 通过临hourID获取Newof rows */
  function getNewRowById(id) {
    let records = getXTable().getInsertRecords();
    for (let record of records) {
      if (record.id === id) {
        return record;
      }
    }
    return null;
  }

  type filterNewRowsOptions = {
    keepNewId?: boolean;
    removeNewLine?: boolean;
  } | boolean

  /**
   * 过滤添加of rows
   * @param rows 要筛选的row data
   * @param optOrRm If you pass boolean It is removeNewLine parameter（true = 删除New，false=Delete onlyid），If you pass对象It is配置parameter
   * @param handler function
   */
  function filterNewRows(rows, optOrRm:filterNewRowsOptions = true, handler?: Fn) {
    let insertRecords = getXTable().getInsertRecords();
    let records: Recordable[] = [];
    optOrRm = typeof optOrRm === 'boolean' ? { removeNewLine: optOrRm } : optOrRm;
    // true = 删除New，false=Delete onlyid
    let removeNewLine = optOrRm?.removeNewLine ?? true;
    for (let row of rows) {
      let item = cloneDeep(row);
      if (insertRecords.includes(row)) {
        handler ? handler({ item, row, insertRecords }) : null;
        if (removeNewLine) {
          continue;
        }
        if (!optOrRm?.keepNewId) {
          delete item.id;
        }
      }
      records.push(item);
    }
    return records;
  }

  /**
   * reset scrollbarTopLocation
   * @param top newtopLocation，留空则滚动到上次记录的Location，Used to resolve switchingtabOptions卡hour导致白屏以及自动将滚动条滚动到顶部的问题
   */
  function resetScrollTop(top?) {
    let xTable = getXTable();
    xTable.scrollTo(null, top == null || top === '' ? data.scroll.top : top);
  }

  /** checktable，Return on failureerrMap，Return successfullynull */
  async function validateTable(rows?) {
    let xTable = getXTable();
    const errMap = await xTable.validate(rows ?? true).catch((errMap) => errMap);
    return errMap ? errMap : null;
  }

  /** 完整check */
  async function fullValidateTable(rows?) {
    let xTable = getXTable();
    const errMap = await xTable.fullValidate(rows ?? true).catch((errMap) => errMap);
    return errMap ? errMap : null;
  }

  type setValuesParam = { rowKey: string; values: Recordable };

  /**
   * set up某行某列的值
   *
   * @param values
   * @return Returns the number of cells affected
   */
  function setValues(values: setValuesParam[]): number {
    if (!isArray(values)) {
      console.warn(`[JVxeTable] setValues Array must be passed`);
      return 0;
    }
    let xTable = getXTable();
    let count = 0;
    values.forEach((item) => {
      let { rowKey, values: record } = item;
      let { row } = getIfRowById(rowKey);
      if (!row) {
        return;
      }
      Object.keys(record).forEach((colKey) => {
        let column = xTable.getColumnByField(colKey);
        if (column) {
          let oldValue = row[colKey];
          let newValue = record[colKey];
          if (newValue !== oldValue) {
            row[colKey] = newValue;
            // trigger valueChange event
            trigger('valueChange', {
              type: column.params.type,
              value: newValue,
              oldValue: oldValue,
              col: column.params,
              column: column,
              isSetValues: true,
              row: {...row}
            });
            count++;
          }
        } else {
          console.warn(`[JVxeTable] setValues not foundkeyfor"${colKey}"columns`);
        }
      });
    });
    if (count > 0) {
      xTable.updateData();
    }
    return count;
  }

  /** Clear selected rows */
  async function clearSelection() {
    const xTable = getXTable();
    let event = { $table: xTable, target: instanceRef.value };
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      await xTable.clearRadioRow();
      handleVxeRadioChange(event);
    } else {
      await xTable.clearCheckboxRow();
      handleVxeCheckboxChange(event);
    }
  }

  /**
   * Get selected data
   * @param isFull if isFull=true Then get the selected data of the entire table
   */
  function getSelectionData(isFull?: boolean) {
    const xTable = getXTable();
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      let row = xTable.getRadioRecord(isFull);
      if (isNull(row)) {
        return [];
      }
      return filterNewRows([row], false);
    } else {
      return filterNewRows(xTable.getCheckboxRecords(isFull), false);
    }
  }

  /** Get only deleted data（New又被删除的data不会被获取到） */
  function getDeleteData() {
    return filterNewRows(getXTable().getRemoveRecords(), false);
  }

  /** 删除一行或多row data */
  async function removeRows(rows, asyncRemove = false) {
    // update-begin--author:liaozhiyang---date:20231123---for：vxe-table removeRowsMethod plus asynchronous deletion
    const xTable = getXTable();
    const removeEvent: any = { deleteRows: rows, $table: xTable };
    if (asyncRemove) {
      const selectedRows = Array.isArray(rows) ? rows : [rows];
      const deleteOldRows = filterNewRows(selectedRows);
      if (deleteOldRows.length) {
        return new Promise((resolve) => {
          // Confirm deletion，Only by calling this method will it be deleted.
          removeEvent.confirmRemove = async () => {
            const insertRecords = xTable.getInsertRecords();
            selectedRows.forEach((item) => {
              // 删除new添加的dataid
              if (insertRecords.includes(item)) {
                delete item.id;
              }
            });
            const res = await xTable.remove(rows);
            await recalcSortNumber();
            resolve(res);
          };
          trigger('removed', removeEvent);
        });
      } else {
        // 全newof rows立马删除，don't wait。
        const res = await xTable.remove(rows);
        removeEvent.confirmRemove = () => {};
        trigger('removed', removeEvent);
        await recalcSortNumber();
        return res;
      }
    } else {
      const res = await xTable.remove(rows);
      trigger('removed', removeEvent);
      await recalcSortNumber();
      return res;
    }
    // update-end--author:liaozhiyang---date:20231123---for：vxe-table removeRowsMethod plus asynchronous deletion
  }

  /** according toidDelete one or more rows */
  function removeRowsById(rowId) {
    let rowIds;
    if (isArray(rowId)) {
      rowIds = rowId;
    } else {
      rowIds = [rowId];
    }
    let rows = rowIds
      .map((id) => {
        let { row } = getIfRowById(id);
        if (!row) {
          return;
        }
        if (row) {
          return row;
        } else {
          console.warn(`【JVxeTable】removeRowsById：${id}does not exist`);
          return null;
        }
      })
      .filter((row) => row != null);
    return removeRows(rows);
  }

  // Delete selected data
  async function removeSelection() {
    let xTable = getXTable();
    let res;
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      res = await xTable.removeRadioRow();
    } else {
      res = await xTable.removeCheckboxRow();
    }
    await clearSelection();
    await recalcSortNumber();
    return res;
  }

  /** 重new计算排序字段的数值 */
  async function recalcSortNumber(force = false) {
    if (props.dragSort || force) {
      let xTable = getXTable();
      let sortKey = props.sortKey ?? 'orderNum';
      let sortBegin = props.sortBegin ?? 0;
      xTable.internalData.tableFullData.forEach((data) => (data[sortKey] = sortBegin++));
      // update-begin--author:liaozhiyang---date:20231011---for：【QQYUN-5133】JVxeTable Line editing upgrade
      // 4.1.0
      //await xTable.updateCache();
      // 4.1.1
      await xTable.cacheRowMap(true)
      // update-end--author:liaozhiyang---date:20231011---for：【QQYUN-5133】JVxeTable Line editing upgrade
      return await xTable.updateData();
    }
  }

  /**
   * sort table
   * @param oldIndex
   * @param newIndex
   * @param force Force sorting
   */
  async function doSort(oldIndex: number, newIndex: number, force = false) {
    if (props.dragSort || force) {
      let xTable = getXTable();
      let sort = (array) => {
        // storageolddata，and delete the item
        let row = array.splice(oldIndex, 1)[0];
        // TowardsnewIndexAdd somewhereolddata
        array.splice(newIndex, 0, row);
      };
      sort(xTable.internalData.tableFullData);
      if (xTable.keepSource) {
        sort(xTable.internalData.tableSourceData);
      }
      // -update-begin--author:liaozhiyang---date:20240620---for：【TV360X-585】Virtual scrolling does not work when dragging fields
      if (isEnabledVirtualYScroll(props, xTable)) {
        await xTable.loadData(xTable.internalData.tableFullData);
      }
      // -update-end--author:liaozhiyang---date:20240620---for：【TV360X-585】Virtual scrolling does not work when dragging fields
      return await recalcSortNumber(force);
    }
  }

  /** 行重new排序 */
  function rowResort(oldIndex: number, newIndex: number) {
    return doSort(oldIndex, newIndex, true);
  }

  // ---------------- begin Permission control ----------------
  // Load permissions
  function loadAuthsMap() {
    if (!props.authPre || props.authPre.length == 0) {
      data.authsMap.value = null;
    } else {
      data.authsMap.value = getJVxeAuths(props.authPre);
    }
  }

  /**
   * according to Permissionscode 获取Permissions
   * @param authCode
   */
  function getAuth(authCode) {
    if (data.authsMap.value != null && props.authPre) {
      let prefix = getPrefix(props.authPre);
      return data.authsMap.value.get(prefix + authCode);
    }
    return null;
  }

  // 获取列Permissions
  function getColAuth(key: string) {
    return getAuth(key);
  }

  // 判断按钮Permissions
  function hasBtnAuth(key: string) {
    return getAuth('btn:' + key)?.isAuth ?? true;
  }

  // ---------------- end Permission control ----------------

  /* --- Helper methods ---*/

  function created() {
    loadAuthsMap();
  }

  // triggerevent
  function trigger(name, event: any = {}) {
    event.$target = instanceRef.value;
    event.$table = getXTable();
    //onlineEnhanceparameter兼容
    event.target = instanceRef.value;
    emit(name, event);
  }

  /**
   * 获取选中of rows-and getSelectionData 区别在于对于Newof rows也会返回ID
   * used foronlinePopForm
   * @param isFull
   */
  function getSelectedData(isFull?: boolean) {
    const xTable = getXTable();
    let rows:any[] = []
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      let row = xTable.getRadioRecord(isFull);
      if (isNull(row)) {
        return [];
      }
      rows = [row]
    } else {
      rows = xTable.getCheckboxRecords(isFull)
    }
    let records: Recordable[] = [];
    for (let row of rows) {
      let item = cloneDeep(row);
      records.push(item);
    }
    return records;
  }
  /**
   *  2024-03-21
   *  liaozhiyang
   *  VXETable列set up保存缓存字段名
   * */
  function handleCustom({ type, $grid }) {
    const { saveSetting, resetSetting } = useColumnsCache({ cacheColumnsKey: props.cacheColumnsKey });
    if (type === 'confirm') {
      saveSetting($grid);
    } else if (type == 'reset') {
      resetSetting($grid);
    }
  }

  return {
    methods: {
      trigger,
      ...publicMethods,
      closeScrolling,
      doSort,
      recalcSortNumber,
      handleVxeScroll,
      handleVxeRadioChange,
      handleVxeCheckboxAll,
      handleVxeCheckboxChange,
      handleFooterMethod,
      handleCellClick,
      handleEditActived,
      handleEditClosed,
      handleCheckMethod,
      handleActiveMethod,
      handleExpandToggleMethod,
      getColAuth,
      hasBtnAuth,
      handleCustom,
    },
    publicMethods,
    created,
  };
}
