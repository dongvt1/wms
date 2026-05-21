import { inject, reactive, ref, watch, unref, Ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { isEmpty } from '@/utils/is';

export function useSelectBiz(getList, props, emit?) {
  //Receive drop down box options
  const selectOptions = inject('selectOptions', ref<Array<object>>([]));
  //Receive selected value
  const selectValues = <object>inject('selectValues', reactive({ value: [], change: false }));
  // Whether the echo is loading
  const loadingEcho = inject<Ref<boolean>>('loadingEcho', ref(false));
  //Dataset
  const dataSource = ref<Array<object>>([]);
  //Selected value
  const checkedKeys = ref<Array<string | number>>([]);
  //Selected row record
  const selectRows = ref<Array<object>>([]);
  //Prompt pop-up window
  const $message = useMessage();
  // Whether it is the first time to load the echo，Only first load，will be displayed loading
  let isFirstLoadEcho = true;

  /**
   * monitorselectValueschange
   */
  watch(
    selectValues,
    () => {
      //update-begin-author:liusq---date:2023-10-19--for: [issues/788]Make sure there is a set value before loading.
      //if (selectValues['change'] == false && !isEmpty(selectValues['value'])) {
      if (selectValues['change'] == false && !isEmpty(selectValues['value'])) {
        //update-end-author:liusq---date:2023-10-19--for: [issues/788]Make sure there is a set value before loading.
        //update-begin---author:wangshuai ---date:20220412  for：[VUEN-672]The user name of the drafter is displayed when editing in the draft box.------------
        // update-begin-author:liaozhiyang---date:2024-11-11--for:【issues/7405】Select users by department and select all users on two pages at the same time，echo back to parent page。The user displayed on the second page is not their real name
        let params = { isMultiTranslate: 'true', pageSize: selectValues.value?.length };
        // update-end-author:liaozhiyang---date:2024-10-11--for:【issues/7405】Select users by department and select all users on two pages at the same time，echo back to parent page。The user displayed on the second page is not their real name
        params[props.rowKey] = selectValues['value'].join(',');
        //update-end---author:wangshuai ---date:20220412  for：[VUEN-672]The user name of the drafter is displayed when editing in the draft box.--------------
        loadingEcho.value = isFirstLoadEcho;
        isFirstLoadEcho = false;
        getDataSource(params, true)
          .then()
          .finally(() => {
            loadingEcho.value = isFirstLoadEcho;
          });
      }
      //Settings list selected by default
      // update-begin--author:liaozhiyang---date:20250423---for：【QQYUN-12155】Check in the pop-up window，Click Cancel again，value is selected
      checkedKeys['value'] = [...selectValues['value']];
      // update-end--author:liaozhiyang---date:20250423---for：【QQYUN-12155】Check in the pop-up window，Click Cancel again，value is selected
    },
    { immediate: true }
  );

  async function onSelectChange(selectedRowKeys: (string | number)[], selectRow) {
    checkedKeys.value = selectedRowKeys;
    //Judgment Select All QuestionscheckedKeysandselectRowsMust be consistent
    if (props.showSelected && unref(checkedKeys).length !== unref(selectRow).length) {
      let { records } = await getList({
        code: unref(checkedKeys).join(','),
        pageSize: unref(checkedKeys).length,
      });
      selectRows.value = records;
    } else {
      selectRows.value = selectRow;
    }
  }

  /**
   * Select column configuration
   */
  const rowSelection = {
    //update-begin-author:liusq---date:20220517--for: Dynamic settingsrowSelectionoftypevalue,The default is'checkbox' ---
    type: props.isRadioSelection ? 'radio' : 'checkbox',
    //update-end-author:liusq---date:20220517--for: Dynamic settingsrowSelectionoftypevalue,The default is'checkbox' ---
    columnWidth: 20,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
    //update-begin-author:wangshuai---date:20221102--for: [VUEN-2562]User selection，After cross-page selection，Only people on the current page ---
    //table4.4.0新增属性选中之后是否清空上一页下一页of数据，defaultfalse
    preserveSelectedRowKeys:true,
    //update-end-author:wangshuai---date:20221102--for: [VUEN-2562]User selection，After cross-page selection，Only people on the current page ---
  };

  /**
   * Serial number column configuration
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: 50,
  };

  /**
   * 加载列表Dataset
   * @param params
   * @param flag 是否是default回显模式加载
   */
  async function getDataSource(params, flag) {
    let { records } = await getList(params);
    dataSource.value = records;
    if (flag) {
      let options = <any[]>[];
      records.forEach((item) => {
        options.push({ label: item[props.labelKey], value: item[props.rowKey] });
      });
      selectOptions.value = options;
    }
  }
  async function initSelectRows() {
    let { records } = await getList({
      code: selectValues['value'].join(','),
      pageSize: selectValues['value'].length,
    });
    // update-begin--author:liaozhiyang---date:20250423---for：【QQYUN-12155】Check in the pop-up window，Click Cancel again，value is selected
    checkedKeys['value'] = [...selectValues['value']];
    // update-end--author:liaozhiyang---date:20250423---for：【QQYUN-12155】Check in the pop-up window，Click Cancel again，value is selected
    selectRows['value'] = records;
  }

  /**
   * Pop-up box shows hidden trigger event
   */
  async function visibleChange(visible) {
    if (visible) {
      // update-begin--author:liaozhiyang---date:20250423---for：【QQYUN-12179】弹窗勾选了value，点击取消再次打开弹窗遗留了上次of勾选ofvalue
      checkedKeys['value'] = [...selectValues['value']];
      // update-begin--author:liaozhiyang---date:20250423---for：【QQYUN-12179】弹窗勾选了value，点击取消再次打开弹窗遗留了上次of勾选ofvalue
      //Settings list selected by default
      props.showSelected && initSelectRows();
    } else {
      // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9366】User selection组件取消and关闭会把选择数据带入
      emit?.('close');
      // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9366】User selection组件取消and关闭会把选择数据带入
    }
  }

  /**
   * Confirm selection
   */
  function getSelectResult(success) {
    let options = <any[]>[];
    let values = <any[]>[];
    selectRows.value.forEach((item) => {
      options.push({ label: item[props.labelKey], value: item[props.rowKey] });
    });
    checkedKeys.value.forEach((item) => {
      values.push(item);
    });
    selectOptions.value = options;
    if (props.maxSelectCount && values.length > props.maxSelectCount) {
      $message.createMessage.warning(`At most, you can only choose${props.maxSelectCount}piece of data`);
      return false;
    }
    success && success(options, values);
  }
  //删除已选择of信息
  function handleDeleteSelected(record) {
    //update-begin---author:wangshuai ---date:20230404  for：【issues/424】After opening the list on the right，When deleting a user from the list on the right，Logic problem------------
    checkedKeys.value = checkedKeys.value.filter((item) => item != record[props.rowKey]);
    selectRows.value = selectRows.value.filter((item) => item[props.rowKey] !== record[props.rowKey]);
    //update-end---author:wangshuai ---date:20230404  for：【issues/424】After opening the list on the right，When deleting a user from the list on the right，Logic problem------------
  }
  //Clear selections
  function reset() {
    checkedKeys.value = [];
    selectRows.value = [];
  }
  return [
    {
      onSelectChange,
      getDataSource,
      visibleChange,
      selectOptions,
      selectValues,
      rowSelection,
      indexColumnProps,
      checkedKeys,
      selectRows,
      dataSource,
      getSelectResult,
      handleDeleteSelected,
      reset,
    },
  ];
}
