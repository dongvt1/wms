import type { PropType } from 'vue';
import type { ReplaceFields, ActionItem, Keys, CheckKeys, ContextMenuOptions, TreeItem } from './typing';
import type { ContextMenuItem } from '/@/hooks/web/useContextMenu';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import { propTypes } from '/@/utils/propTypes';

export const basicProps = {
  value: {
    type: [Object, Array] as PropType<Keys | CheckKeys>,
  },
  renderIcon: {
    type: Function as PropType<(params: Recordable) => string>,
  },

  helpMessage: {
    type: [String, Array] as PropType<string | string[]>,
    default: '',
  },

  title: propTypes.string,
  toolbar: propTypes.bool,
  search: propTypes.bool,
  searchValue: propTypes.string,
  checkStrictly: propTypes.bool,
  clickRowToExpand: propTypes.bool.def(true),
  checkable: propTypes.bool.def(false),
  defaultExpandLevel: {
    type: [String, Number] as PropType<string | number>,
    default: '',
  },
  // Highlight search value，Highlight only specific matching values（passtitle）The value istrueUse default color values ​​when，The value is#xxxUse this value instead and highlight it when
  highlight: {
    type: [Boolean, String] as PropType<Boolean | String>,
    default: false,
  },
  defaultExpandAll: propTypes.bool.def(false),

  replaceFields: {
    type: Object as PropType<ReplaceFields>,
  },

  treeData: {
    type: Array as PropType<TreeDataItem[]>,
  },

  actionList: {
    type: Array as PropType<ActionItem[]>,
    default: () => [],
  },

  expandedKeys: {
    type: Array as PropType<Keys>,
    default: () => [],
  },

  selectedKeys: {
    type: Array as PropType<Keys>,
    default: () => [],
  },

  checkedKeys: {
    type: Array as PropType<CheckKeys>,
    default: () => [],
  },

  beforeRightClick: {
    type: Function as PropType<(...arg: any) => ContextMenuItem[] | ContextMenuOptions>,
    default: null,
  },

  rightMenuList: {
    type: Array as PropType<ContextMenuItem[]>,
  },
  // Custom data filtering judgment method(Note: Not the entire filtering method，It is a judgment method based on built-in filtering.，用于增强原本仅能passtitleHow to filter)
  filterFn: {
    type: Function as PropType<(searchValue: any, node: TreeItem, replaceFields: ReplaceFields) => boolean>,
    default: null,
  },
  // Automatically expand results when search is complete
  expandOnSearch: propTypes.bool.def(false),
  // When the search is completed, all results are automatically selected.,if and only if checkable===true effective when
  checkOnSearch: propTypes.bool.def(false),
  // Search completed automaticallyselectAll results
  selectedOnSearch: propTypes.bool.def(false),
};

export const treeNodeProps = {
  actionList: {
    type: Array as PropType<ActionItem[]>,
    default: () => [],
  },
  replaceFields: {
    type: Object as PropType<ReplaceFields>,
  },
  treeData: {
    type: Array as PropType<TreeDataItem[]>,
    default: () => [],
  },
};
