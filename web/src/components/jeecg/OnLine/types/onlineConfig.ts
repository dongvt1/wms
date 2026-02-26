interface ScopedSlots {
  customRender: string;
}

interface HrefSlots {
  // Link address
  href: string;
  // fieldHref_Field name
  slotName: string;
}

interface OnlineColumn {
  dataIndex?: string;
  title?: string;
  key?: string;
  fieldType?: string;
  width?: number | string;
  align?: string;
  sorter?: string | boolean;
  isTotal?: string | number | boolean;
  groupTitle?: string;
  // When hyperlinking andHrefSlotsinslotNamematch
  scopedSlots?: ScopedSlots;
  // Generally used in dictionaries What is passed from the dictionary is a dictionary-encoded string. backward function
  customRender?: string | Function;
  // I don’t know what this type is used for.
  hrefSlotName?: string;
  showLength?: number | string;
  children?: OnlineColumn[];
  sortOrder?: string;
  // Slot corresponding control type(list)
  slots?: ScopedSlots;
  //Exceeding the width will be automatically omitted，暂不支持and排序筛选一起使用。
  ellipsis?: boolean;
  // Whether to fix the column
  fixed?: boolean | 'left' | 'right';
  //Field type int/string 
  dbType?:string;
  //Used by other table fields
  linkField?:string;
  fieldExtendJson?:string
}

export { OnlineColumn, HrefSlots };
