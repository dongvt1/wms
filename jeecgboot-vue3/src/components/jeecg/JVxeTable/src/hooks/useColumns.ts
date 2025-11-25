import type { JVxeColumn, JVxeDataProps, JVxeTableProps } from '../types';
import { computed, nextTick, toRaw } from 'vue';
import { isArray, isEmpty, isPromise } from '/@/utils/is';
import { cloneDeep } from 'lodash-es';
import { JVxeTypePrefix, JVxeTypes } from '../types/JVxeTypes';
import { initDictOptions } from '/@/utils/dict';
import { pushIfNotExist } from '/@/utils/common/compUtils';
import { getEnhanced } from '../utils/enhancedUtils';
import { isRegistered } from '../utils/registerUtils';
import { JVxeComponent } from '../types/JVxeComponent';
import { useValidateRules } from './useValidateRules';
import { JVxeTableMethods } from '../types';

// handle method parameters
export interface HandleArgs {
  props: JVxeTableProps;
  slots: any;
  data: JVxeDataProps;
  methods: JVxeTableMethods;
  col?: JVxeColumn;
  columns: JVxeColumn[];
  renderOptions?: any;
  enhanced?: JVxeComponent.Enhanced;
}

export function useColumns(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, slots) {
  data.vxeColumns = computed(() => {
    // update-begin--author:liaozhiyang---date:20250403---for：【issues/7812】linkageConfigchanged，vxetableNot updated
    // linkageConfigIt also needs to be executed when changing
    const linkageConfig = toRaw(props.linkageConfig);
    if (linkageConfig) {
      // console.log(linkageConfig);
    }
    // update-end--author:liaozhiyang---date:20250403---for：【issues/7812】linkageConfigchanged，vxetableNot updated
    let columns: JVxeColumn[] = [];
    if (isArray(props.columns)) {
      // handle method parameters
      const args: HandleArgs = { props, slots, data, methods, columns };
      let seqColumn, selectionColumn, expandColumn, dragSortColumn;

      const handleColumn = (column: JVxeColumn, container: JVxeColumn[]) => {
        // Exclude unauthorized columns 1 = show/hide； 2 = Disable
        let auth = methods.getColAuth(column.key);
        if (auth?.type == '1' && !auth.isAuth) {
          return;
        } else if (auth?.type == '2' && !auth.isAuth) {
          column.disabled = true;
        }
        // type Leave blank，Default is normal
        if (column.type == null || isEmpty(column.type)) {
          column.type = JVxeTypes.normal;
        }
        let col: JVxeColumn = cloneDeep(column);
        // 处理hide列
        if (col.type === JVxeTypes.hidden) {
          return handleInnerColumn(args, col, handleHiddenColumn);
        }
        // Process child columns
        // Determine whether it is a grouping column，If it is currently the parent，no need to process render
        if (Array.isArray(col.children) && col.children.length > 0) {
          const children: JVxeColumn[] = [];
          col.children.forEach((child: JVxeColumn) => handleColumn(child, children));
          col.children = children;
          container.push(col);
          return;
        }
        // Component not registered，automatically set to normal
        if (!isRegistered(col.type)) {
          col.type = JVxeTypes.normal;
        }
        args.enhanced = getEnhanced(col.type);
        args.col = col;
        args.renderOptions = {
          bordered: props.bordered,
          disabled: props.disabled,
          scrolling: data.scrolling,
          isDisabledRow: methods.isDisabledRow,
          listeners: {
            trigger: (name, event) => methods.trigger(name, event),
            valueChange: (event) => methods.trigger('valueChange', event),
            /** Reorder rows */
            rowResort: (event) => {
              methods.doSort(event.oldIndex, event.newIndex);
              methods.trigger('dragged', event);
            },
            /** Insert a row below the current row */
            rowInsertDown: (rowIndex) => methods.insertRows({}, rowIndex + 1),
          },
        };
        if (col.type === JVxeTypes.rowNumber) {
          seqColumn = col;
          container.push(col);
        } else if (col.type === JVxeTypes.rowRadio || col.type === JVxeTypes.rowCheckbox) {
          selectionColumn = col;
          container.push(col);
        } else if (col.type === JVxeTypes.rowExpand) {
          expandColumn = col;
          container.push(col);
        } else if (col.type === JVxeTypes.rowDragSort) {
          dragSortColumn = col;
          container.push(col);
        } else {
          col.params = column;
          args.columns = container;
          handlerCol(args);
        }
      }

      props.columns.forEach((column: JVxeColumn) => handleColumn(column, columns));

      handleInnerColumn(args, seqColumn, handleSeqColumn);
      handleInnerColumn(args, selectionColumn, handleSelectionColumn);
      handleInnerColumn(args, expandColumn, handleExpandColumn);
      handleInnerColumn(args, dragSortColumn, handleDragSortColumn, true);
      // update-begin--author:liaozhiyang---date:2024-05-30---for【TV360X-371】Non-editable components are required and missing*Number
      customComponentAddStar(columns);
      // update-end--author:liaozhiyang---date:2024-05-30---for：【TV360X-371】Non-editable components are required and missing*Number
    }
    return columns;
  });
}

/**
 * 2024-05-30
 * liaozhiyang
 * Non-editable components are required to passtitleArtificial addition*Number
 */
function customComponentAddStar(columns) {
  columns.forEach((column) => {
    const { params } = column;
    if (params) {
      const { validateRules, type } = params;
      if (
        validateRules?.length &&
        [
          JVxeTypes.checkbox,
          JVxeTypes.radio,
          JVxeTypes.upload,
          JVxeTypes.progress,
          JVxeTypes.departSelect,
          JVxeTypes.userSelect,
          JVxeTypes.image,
          JVxeTypes.file,
        ].includes(type)
      ) {
        if (validateRules.find((item) => item.required)) {
          column.title = ` * ${column.title}`;
        }
      }
    }
  });
}

/** Handle built-in columns */
function handleInnerColumn(args: HandleArgs, col: JVxeColumn, handler: (args: HandleArgs) => void, assign?: boolean) {
  let renderOptions = col?.editRender || col?.cellRender;
  return handler({
    ...args,
    col: col,
    renderOptions: assign ? Object.assign({}, args.renderOptions, renderOptions) : renderOptions,
  });
}

/**
 * 处理hide列
 */
function handleHiddenColumn({ col, columns }: HandleArgs) {
  col!.params = cloneDeep(col);
  delete col!.type;
  col!.field = col!.key;
  col!.visible = false;
  columns.push(col!);
}

/**
 * 处理行Number列
 */
function handleSeqColumn({ props, col, columns }: HandleArgs) {
  // 判断是否开启了行Number列
  if (props.rowNumber) {
    let column = {
      type: 'seq',
      title: '#',
      width: 60,
      // 【QQYUN-8405】
      fixed: props.rowNumberFixed,
      align: 'center',
    };
    // update-begin--author:liaozhiyang---date:20240306---for：【QQYUN-8405】vxetable支持序Number是否固定（Mobile terminal needs）
    if (props.rowNumberFixed === 'none') {
      delete column.fixed;
    }
    // update-end--author:liaozhiyang---date:20240306---for：QQYUN-8405】vxetable支持序Number是否固定（Mobile terminal needs）
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/**
 * Handle selectable columns
 */
function handleSelectionColumn({ props, data, col, columns }: HandleArgs) {
  // Determine whether selectable rows are enabled
  // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-1002】详情页面行编辑不showcheckbox
  if (props.rowSelection && props.disabled == false) {
    // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-1002】详情页面行编辑不showcheckbox
    let width = 45;
    if (data.statistics.has && !props.rowExpand && !props.dragSort) {
      width = 60;
    }
    let column: any = {
      type: props.rowSelectionType,
      width: width,
      fixed: 'left',
      align: 'center',
    };
    // update-begin--author:liaozhiyang---date:20240509---for：【issues/1162】JVxeTableColumn too long（A horizontal scroll bar appears）Unable to drag and sort
    if (props.rowSelectionFixed === 'none') {
      delete column.fixed;
    }
    // update-end--author:liaozhiyang---date:20240509---for：【issues/1162】JVxeTableColumn too long（A horizontal scroll bar appears）Unable to drag and sort
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/**
 * Handle expandable rows
 */
function handleExpandColumn({ props, data, col, columns }: HandleArgs) {
  // Whether rows can be expanded
  if (props.rowExpand) {
    let width = 40;
    if (data.statistics.has && !props.dragSort) {
      width = 60;
    }
    let column = {
      type: 'expand',
      title: '',
      width: width,
      fixed: 'left',
      align: 'center',
      slots: { content: 'expandContent' },
    };
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/** Handle sortable columns */
function handleDragSortColumn({ props, data, col, columns, renderOptions }: HandleArgs) {
  // Is draggable sorting possible?
  if (props.dragSort) {
    let width = 40;
    if (data.statistics.has) {
      width = 60;
    }
    let column: any = {
      title: '',
      width: width,
      fixed: 'left',
      align: 'center',
      // update-begin--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
      params: {
        insertRow: props.insertRow,
        notAllowDrag: props.notAllowDrag,
        ...col?.params,
      },
      // update-end--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
    };
    // update-begin--author:liaozhiyang---date:20240506---for：【issues/1162】JVxeTableColumn too long（A horizontal scroll bar appears）Unable to drag and sort
    if (props.dragSortFixed === 'none') {
      delete column.fixed;
    }
    // update-end--author:liaozhiyang---date:20240506---for：【issues/1162】JVxeTableColumn too long（A horizontal scroll bar appears）Unable to drag and sort
    let cellRender = {
      name: JVxeTypePrefix + JVxeTypes.rowDragSort,
      sortKey: props.sortKey,
    };
    if (renderOptions) {
      column.cellRender = Object.assign(renderOptions, cellRender);
    } else {
      column.cellRender = cellRender;
    }
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column);
    }
  }
}

/** Handling custom component columns */
function handlerCol(args: HandleArgs) {
  const { props, col, columns, enhanced } = args;
  if (!col) return;
  let { type } = col;
  col.field = col.key;
  delete col.type;
  let renderName = 'cellRender';
  // Rendering options
  let $renderOptions: any = { name: JVxeTypePrefix + type };
  if (enhanced?.switches.editRender) {
    if (!(enhanced.switches.visible || props.alwaysEdit)) {
      renderName = 'editRender';
    }
    // $renderOptions.type = (enhanced.switches.visible || props.alwaysEdit) ? 'visible' : 'default'
  }
  col[renderName] = $renderOptions;
  // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-5806】jsEnhanced change drop-down searchoptions（Add tocustomOptionsfortrueDon't read the dictionary，Go your own wayoptions）
  !col.params.customOptions && handleDict(args);
  // update-end--author:liaozhiyang---date:20240321---for：【QQYUN-5806】jsEnhanced change drop-down searchoptions（Add tocustomOptionsfortrueDon't read the dictionary，Go your own wayoptions）
  handleRules(args);
  handleStatistics(args);
  handleSlots(args);
  handleLinkage(args);
  handleReloadEffect(args);

  if (col.editRender) {
    Object.assign(col.editRender, args.renderOptions);
  }
  if (col.cellRender) {
    Object.assign(col.cellRender, args.renderOptions);
  }

  columns.push(col);
}

/**
 * Handle dictionaries
 */
async function handleDict({ col, methods }: HandleArgs) {
  if (col && col.params.dictCode) {
    /** Load the data dictionary and merge into options */
    try {
      // Query Dictionary
      if (!isPromise(col.params.optionsPromise)) {
        col.params.optionsPromise = new Promise(async (resolve) => {
          //update-begin-author:taoyan date:2022-6-1 for: VUEN-1180 【code generation】Subtables do not support conditions？
          let dictCodeString = col.params.dictCode;
          if (dictCodeString) {
            dictCodeString = encodeURI(dictCodeString);
          }
          const dictOptions: any = await initDictOptions(dictCodeString);
          //update-end-author:taoyan date:2022-6-1 for: VUEN-1180 【code generation】Subtables do not support conditions？
          let options = col.params.options ?? [];
          dictOptions.forEach((dict) => {
            // Filter duplicate data
            if (options.findIndex((o) => o.value === dict.value) === -1) {
              options.push(dict);
            }
          });
          resolve(options);
        });
      }
      col.params.options = await col.params.optionsPromise;
      await nextTick();
      await methods.getXTable().updateData();
    } catch (e) {
      console.group(`[JVxeTable] Query Dictionary "${col.params.dictCode}" Exception occurs when！`);
      console.warn(e);
      console.groupEnd();
    }
  }
}

/**
 * Handle verification
 */
function handleRules(args: HandleArgs) {
  if (isArray(args.col?.validateRules)) {
    useValidateRules(args);
  }
}

/**
 * Process statistical columns
 */
function handleStatistics({ col, data }: HandleArgs) {
  // sum = Sum、average = average value
  if (col && isArray(col.statistics)) {
    data.statistics.has = true;
    col.statistics.forEach((item) => {
      if (!isEmpty(item)) {
        let arr = data.statistics[(item as string).toLowerCase()];
        if (isArray(arr)) {
          pushIfNotExist(arr, col.key);
        }
      }
    });
  }
}

/**
 * handle slot
 */
function handleSlots({ slots, col, renderOptions }: HandleArgs) {
  // slot Special handling of components
  if (col && col.params.type === JVxeTypes.slot) {
    if (!isEmpty(col.slotName) && slots.hasOwnProperty(col.slotName)) {
      renderOptions.slot = slots[col.slotName];
    }
  }
}

/** Process linked columns */
function handleLinkage({ data, col, renderOptions, methods }: HandleArgs) {
  // Process linked columns，Linked columns can only act on select components
  if (col && col.params.type === JVxeTypes.select && data.innerLinkageConfig != null) {
    // Determine whether the current column is a linked column
    if (data.innerLinkageConfig.has(col.key)) {
      renderOptions.linkage = {
        config: data.innerLinkageConfig.get(col.key),
        getLinkageOptionsAsync: methods.getLinkageOptionsAsync,
        getLinkageOptionsSibling: methods.getLinkageOptionsSibling,
        handleLinkageSelectChange: methods.handleLinkageSelectChange,
      };
    }
  }
}

function handleReloadEffect({ props, data, renderOptions }: HandleArgs) {
  renderOptions.reloadEffect = {
    enabled: props.reloadEffect,
    getMap() {
      return data.reloadEffectRowKeysMap;
    },
    isEffect(rowId) {
      return data.reloadEffectRowKeysMap[rowId] === true;
    },
    removeEffect(rowId) {
      return (data.reloadEffectRowKeysMap[rowId] = false);
    },
  };
}
