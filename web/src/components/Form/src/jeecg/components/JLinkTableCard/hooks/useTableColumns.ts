import type { Ref } from 'vue';
import { HrefSlots, OnlineColumn } from '/@/components/jeecg/OnLine/types/onlineConfig';
import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil';
import { computed, defineAsyncComponent, h, reactive, ref, toRaw, unref, watch, markRaw } from 'vue';
import { useRouter } from 'vue-router';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { getAreaTextByCode } from '/@/components/Form/src/utils/Area';
import { createImgPreview } from '/@/components/Preview/index';
import { importViewsFile, _eval } from '/@/utils';
import { useModal } from '/@/components/Modal';
import { getToken } from '/@/utils/auth';
import { downloadFile } from '/@/api/common/api';
import { getWeekMonthQuarterYear, split } from '/@/utils';
/**
 * Get the actual list neededcolumnConfiguration
 * @param onlineTableContext Data retrieved from the database
 * @param extConfigJson 扩展ConfigurationJSON
 */
export function useTableColumns(onlineTableContext, extConfigJson: Ref<any | undefined>) {
  // Get router object hrefUsed for jump
  let router = useRouter();

  // Column information
  const columns = ref<Array<OnlineColumn>>([]);
  // Is therebpm_status
  //const hasBpmStatus = ref<boolean>(false)
  // Dictionary information
  const dictOptionInfo = ref<any>({});
  //Selected value
  const selectedKeys = ref<any[]>([]);
  //Selected row records
  //const selectRows = ref<Array<any>>([]);
  // 选择列Configuration --computedThere is a problem
  const rowSelection = ref<any>(null);
  // Is there滚动条
  let enableScrollBar = ref(true);
  // tablepropertyscroll
  let tableScroll = computed(() => {
    if (enableScrollBar.value == true) {
      return undefined;
    } else {
      // Xaxis has no scrollbar
      return { x: false };
    }
  });

  //used for onlinelist Click pop-up event for a certain column-Pop-up window showing other forms
  const [registerOnlineHrefModal, { openModal: openOnlineHrefModal }] = useModal();
  const hrefMainTableId = ref('')
  // used for onlinein form Pop up another form
  const [registerPopModal, { openModal: openPopModal }] = useModal();
  const popTableId = ref('')

  // 对查询Column information的请求结果 Treatment method
  function handleColumnResult(result, type = 'checkbox') {
    // Dictionary settings
    dictOptionInfo.value = result.dictOptions;
    // rowSelectionset up
    if (result.checkboxFlag == 'Y') {
      rowSelection.value = {
        selectedRowKeys: selectedKeys,
        onChange: onSelectChange,
        type,
      };
    } else {
      rowSelection.value = null;
    }
    // Whether to allow scroll bars
    enableScrollBar.value = result.scrollFlag == 1;

    let dataColumns = result.columns;
    dataColumns.forEach((column) => {
      // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-4161】Column supports fixed functions
      if (column.fieldExtendJson) {
        const json = JSON.parse(column.fieldExtendJson);
        if (!!json.isFixed) {
          column.fixed = 'left';
        }
      }
      // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-4161】Column supports fixed functions
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-129】增加富文本控件ConfigurationhrefJump
      if (column.hrefSlotName && column.scopedSlots) {
        const obj = result.fieldHrefSlots?.find((item) => item.slotName === column.hrefSlotName);
        if (obj) {
          column.fieldHref = obj;
        }
      }
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-129】增加富文本控件ConfigurationhrefJump
      Object.keys(column).map((key) => {
        // Delete fields with null values（Do not delete empty string('') or number 0 ）
        if (column[key] == null) {
          delete column[key];
        }
      });
    });

    // href Jump
    let fieldHrefSlots: HrefSlots[] = result.fieldHrefSlots;
    const fieldHrefSlotKeysMap = {};
    fieldHrefSlots.forEach((item) => (fieldHrefSlotKeysMap[item.slotName] = item));

    let tableColumns: OnlineColumn[] = [];
    // Process the column href Jump和 dict dictionary，Make the two compatible
    tableColumns = handleColumnHrefAndDict(dataColumns, fieldHrefSlotKeysMap);
    // Is there bpm_statusField if there is，The list operation button needs to add a submit process button
    bpmStatusFilter(tableColumns);

    console.log('-----列表列Configuration----', tableColumns);
    // If it is a tree list 需要set upfirst columnField and first columnalign
    if (onlineTableContext.isTree() === true) {
      // 找到first column的Configuration
      let firstField = result.textField;
      let index = -1;
      for (let i = 0; i < tableColumns.length; i++) {
        if (tableColumns[i].dataIndex == firstField) {
          index = i;
          break;
        }
      }
      if (index > 0) {
        //in the case of0or是-1No processing required
        let deleteColumns = tableColumns.splice(index, 1);
        tableColumns.unshift(deleteColumns[0]);
      }
      //first column居左
      if (tableColumns.length > 0) {
        tableColumns[0].align = 'left';
      }
    }
    columns.value = tableColumns;
    // Column changed，Need to re-render table
    onlineTableContext.reloadTable();
  }

  /**
   * table select event [expose]
   * @param selectedRowKeys
   * @param selectRow
   */
  function onSelectChange(selectedRowKeys, selectedRows) {
    selectedKeys.value = selectedRowKeys;
    onlineTableContext['selectedRows'] = toRaw(selectedRows);
    onlineTableContext['selectedRowKeys'] = toRaw(selectedRowKeys);
  }

  /**
   * processing columnhref和dictionary翻译
   */
  function handleColumnHrefAndDict(columns: OnlineColumn[], fieldHrefSlotKeysMap: {}): OnlineColumn[] {
    for (let column of columns) {
      let { customRender, hrefSlotName, fieldType } = column;
      // online Report中类型Configuration为date（yyyy-MM-dd ），But the actual display is in date and time format(yyyy-MM-dd HH:mm:ss) issues/3042
      if (fieldType == 'date' || fieldType == 'Date') {
        column.customRender = ({ text }) => {
          if (!text) {
            return '';
          }
          if (text.length > 10) {
            return text.substring(0, 10);
          }
          return text;
        };
      } else if (fieldType == 'link_table') {
        // Related record list display
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930】表格列表中support关联记录Configuration是否只读
        const fieldExtendJson = column.fieldExtendJson ?? '{}';
        const json = JSON.parse(fieldExtendJson);
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930】表格列表中support关联记录Configuration是否只读
        column.customRender = ({ text, record }) => {
          if (!text) {
            return '';
          }
          if(onlineTableContext.isPopList===true){
            // in the case of弹窗columns表，Columns of associated records only support data translation，不需要Jump逻辑
            return record[column.dataIndex+"_dictText"]
          }else{
            let tempIdArray = (text+'').split(',');
            //update-begin-author:taoyan date:2023-2-15 for: QQYUN-4286【onlineform】Enable joint query for master and child tables Functional test reports error and cannot be opened
            let tempLabelArray = [];
            if(record[column.dataIndex+"_dictText"]){
              tempLabelArray = record[column.dataIndex+"_dictText"].split(',');
            }
            //update-end-author:taoyan date:2023-2-15 for: QQYUN-4286【onlineform】Enable joint query for master and child tables Functional test reports error and cannot be opened
            let renderResult:any = []
            if(renderResult.length==0){
              return ''
            }
            //If necessary, display all，But it will wrap：display: flex;width: 100%;flex-wrap: wrap;flex-direction: row;
            return h('div',{style:{'overflow':'hidden'}}, renderResult);
          }
        };
      } else if (fieldType === 'popup_dict') {
        // update-begin--author:liaozhiyang---date:20240402---for：【QQYUN-8833】JPopupDictList of translations
        column.customRender = ({ text, record }) => {
          const dict = record[column.dataIndex + '_dictText'];
          if (dict != undefined) {
            return record[column.dataIndex + '_dictText'];
          }
          return text;
        };
        // update-end--author:liaozhiyang---date:20240402---for：【QQYUN-8833】JPopupDictList of translations
      } else {
        if (!hrefSlotName && column.scopedSlots && column.scopedSlots.customRender) {
          //【OnlineReport】dictionary和hrefmutually exclusive pass herefieldHrefSlotKeysMap Find it firsthrefcolumns
          if (fieldHrefSlotKeysMap.hasOwnProperty(column.scopedSlots.customRender)) {
            hrefSlotName = column.scopedSlots.customRender;
          }
        }
        // if customRender A value indicates that it is useddictionary
        // if hrefSlotName A value indicates that it is usedhrefJump
        // Both are compatible。The specific idea of ​​compatibility is：先获取到dictionary替换value，Add morehref链接Jump
        if (customRender || hrefSlotName) {
          let dictCode = customRender as string;
          let replaceFlag = '_replace_text_';
          // 自定义渲染函数columns 需要手动Configurationellipsis
          column.ellipsis = true;
          column.customRender = ({ text, record }) => {
            let value = text;
            // if dictCode valuable，就进行dictionary转换
            if (dictCode) {
              if (dictCode.startsWith(replaceFlag)) {
                let textFieldName = dictCode.replace(replaceFlag, '');
                value = record[textFieldName];
              } else {
                value = filterMultiDictText(unref(dictOptionInfo)[dictCode], text + '');
              }
            }
            // 扩展parameterset up列的内容长度
            if (column.showLength) {
              if (value && value.length > column.showLength) {
                value = value.substr(0, column.showLength) + '...';
              }
            }
            // if hrefSlotName valuable，Just generate one a Label，包裹住dictionary替换后（or原生）value
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

        //  The old version is calledscopedSlots The new version is calledslots
        if (column.scopedSlots) {
          // slotcolumns 需要手动Configurationellipsis
          column.ellipsis = true;
          let slots = column.scopedSlots;
          column['slots'] = slots;
          delete column.scopedSlots;
        }
      }
    }
    return columns;
  }

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
      if(href.startsWith('ONLINE:')){
        // ONLINE:tableId:fieldName
        let arr = href.split(':')
        hrefMainTableId.value = arr[1];
        let fieldName = arr[2];
        openOnlineHrefModal(true, {
          isUpdate: true,
          disableSubmit: true,
          hideSub: true,
          record:{id: record[fieldName]},
        })
      }else{
        href = href.trim().replace(/\${([^}]+)?}/g, (_s1, s2) => record[s2]);
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
  }

  // style
  const dialogStyle = {
    top: 0,
    left: 0,
    height: '100%',
    margin: 0,
    padding: 0,
  };

  // update-begin--author:liaozhiyang---date:20231218---for：【QQYUN-6366】upgrade toantd4.x
  // 弹窗propertyConfiguration
  const hrefComponent = reactive({
    model: {
      title: '',
      okText: 'closure',
      width: '100%',
      open: false,
      destroyOnClose: true,
      style: dialogStyle,
      // dialogStyle: dialogStyle,
      bodyStyle: { padding: '8px', height: 'calc(100vh - 108px)', overflow: 'auto', overflowX: 'hidden' },
      // Hide cancel button
      cancelButtonProps: { style: { display: 'none' } },
    },
    on: {
      ok: () => (hrefComponent.model.open = false),
      cancel: () => (hrefComponent.model.open = false),
    },
    is: <any>null,
    params: {},
  });
  // update-end--author:liaozhiyang---date:20231218---for：【QQYUN-6366】upgrade toantd4.x

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
      hrefComponent.params = params;
    } else {
      hrefComponent.params = {};
    }
    // update-begin--author:liaozhiyang---date:20231218---for：【QQYUN-6366】upgrade toantd4.x
    hrefComponent.model.open = true;
    // update-end--author:liaozhiyang---date:20231218---for：【QQYUN-6366】upgrade toantd4.x
    hrefComponent.model.title = 'operate';
    hrefComponent.is = markRaw(defineAsyncComponent(() => importViewsFile(path)));
  }

  //If it is a tree list operate列只能右侧固定
  let fixedAction:any = 'left';
  if(onlineTableContext.isTree()){
    fixedAction = 'right'
  }
  const actionColumn = reactive<OnlineColumn>({
    title: 'operate',
    dataIndex: 'action',
    slots: { customRender: 'action' },
    fixed: fixedAction,
    align: 'center',
    width: 150,
  });

  // 监听扩展parameter的固定列Configuration，动态改变operate列的固定方式
  watch(() => extConfigJson?.value, () => {
    if (extConfigJson?.value?.tableFixedAction === 1) {
      actionColumn.fixed = extConfigJson?.value?.tableFixedActionType || 'right';
      // If it is a tree list operate列只能右侧固定
      if(onlineTableContext.isTree()){
        actionColumn.fixed = 'right'
      }
    }
  });

  // Process button state
  function bpmStatusFilter(tableColumns: OnlineColumn[]): boolean {
    let flag = false;
    for (let i = 0; i < tableColumns.length; i++) {
      let item = tableColumns[i];
      let fieldName = item.dataIndex;
      if (fieldName!.toLowerCase() == 'bpm_status') {
        flag = true;
        break;
      }
    }
    onlineTableContext['hasBpmStatus'] = flag;
    return flag;
  }

  /**
   * document
   * @param text
   */
  function downloadRowFile(text, record, column, id) {
    if (!text) {
      return;
    }
    // update-begin--author:liaozhiyang---date:20240124---for：【QQYUN-8020】online form有多个document走下载接口
    if (text.indexOf(',') > 0) {
      downloadFile(`/online/cgform/field/download/${id}/${record.id}/${column.dataIndex}`, `document_${record.id}.zip`);
    } else {
      const url = getFileAccessHttpUrl(text);
      window.open(url);
    }
    // update-end--author:liaozhiyang---date:20240124---for：【QQYUN-8020】online form有多个document走下载接口
  }

  /**
   * picture
   * @param text
   */
  function getImgView(text) {
    if (text && text.indexOf(',') > 0) {
      // update-begin--author:liaozhiyang---date:20250325---for：【issues/7990】pictureparameter中包含逗号会错误的识别成多张图
      text = split(text)[0];
      // update-end--author:liaozhiyang---date:20250325---for：【issues/7990】pictureparameter中包含逗号会错误的识别成多张图
    }
    return getFileAccessHttpUrl(text);
  }

  /**
   * Get provincial and municipal text based on encoding
   * @param code
   */
  function getPcaText(code) {
    if (!code) {
      return '';
    }
    return getAreaTextByCode(code);
  }

  /**
   * date formatting
   * @param text
   */
  function getFormatDate(text, column) {
    if (!text) {
      return '';
    }
    let a = text;
    if (a.length > 10) {
      a = a.substring(0, 10);
    }
    // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online date(year month day)Controls increase year、years，year week，Year quarter and other formats
    let fieldExtendJson = column?.fieldExtendJson;
    if (fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
        const result = getWeekMonthQuarterYear(a);
        return result[fieldExtendJson.picker];
      }
    }
    // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online date(year month day)Controls increase year、years，year week，Year quarter and other formats
    return a;
  }

  watch(selectedKeys, () => {
    onlineTableContext['selectedRowKeys'] = toRaw(selectedKeys.value);
  });

  onlineTableContext['clearSelectedRow'] = () => {
    selectedKeys.value = [];
    onlineTableContext['selectedRows'] = [];
    onlineTableContext['selectedRowKeys'] = [];
  };

  /**
   * Preview list cell picture
   * @param text
   */
  function viewOnlineCellImage(text) {
    if (text) {
      let imgList: any = [];
      // update-begin--author:liaozhiyang---date:20250325---for：【issues/7990】pictureparameter中包含逗号会错误的识别成多张图
      const arr = split(text);
      // update-end--author:liaozhiyang---date:20250325---for：【issues/7990】pictureparameter中包含逗号会错误的识别成多张图
      for (let str of arr) {
        if (str) {
          imgList.push(getFileAccessHttpUrl(str));
        }
      }
      createImgPreview({ imageList: imgList });
    }
  }

  /**
   * link tableThe control is displayed on the list support点击Jumpform
   * @param id
   * @param hrefTableName
   */
  const onlinePopModalRef = ref();
  async function handleClickLinkTable(id, hrefTableName, isListReadOnly){
    popTableId.value = hrefTableName;
    let formStatus =  await onlinePopModalRef.value.getFormStatus();
    // 判断当前form是否support编辑，不能编辑跳详情form
    if(formStatus==true){
      hrefMainTableId.value = hrefTableName;
      openOnlineHrefModal(true, {
        isUpdate: true,
        disableSubmit: true,
        hideSub: true,
        record:{id: id},
      })
    }else{
      openPopModal(true, {
        isUpdate: true,
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930】表格列表中support关联记录Configuration是否只读
        disableSubmit: isListReadOnly ? true : false,
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930】表格列表中support关联记录Configuration是否只读
        record: {
          id: id
        }
      });
    }
  }
  
  return {
    columns,
    actionColumn,
    selectedKeys,
    rowSelection,
    enableScrollBar,
    tableScroll,
    downloadRowFile,
    getImgView,
    getPcaText,
    getFormatDate,
    handleColumnResult,
    onSelectChange,
    hrefComponent,
    viewOnlineCellImage,
    hrefMainTableId,
    registerOnlineHrefModal,
    registerPopModal,
    openPopModal,
    openOnlineHrefModal,
    onlinePopModalRef,
    popTableId,
    handleClickFieldHref,
  };
}
