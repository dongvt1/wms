/** Component type */
export enum JVxeTypes {
  // Line number column
  rowNumber = 'row-number',
  // select column
  rowCheckbox = 'row-checkbox',
  // radio select column
  rowRadio = 'row-radio',
  // Expand column
  rowExpand = 'row-expand',
  // Sort up and down
  rowDragSort = 'row-drag-sort',

  input = 'input',
  inputNumber = 'input-number',
  textarea = 'textarea',
  select = 'select',
  date = 'date',
  datetime = 'datetime',
  time = 'time',
  checkbox = 'checkbox',
  upload = 'upload',
  // Drop down search
  selectSearch = 'select-search',
  // Drop-down multiple selection
  selectMultiple = 'select-multiple',
  // progress bar
  progress = 'progress',
  //Department selection
  departSelect = 'depart-select',
  //User selection
  userSelect = 'user-select',

  // tugboatTags（Not useful yet）
  tags = 'tags', // TODO To be realized

  slot = 'slot',
  normal = 'normal',
  hidden = 'hidden',

  // The following are custom components
  popup = 'popup',
  selectDictSearch = 'selectDictSearch',
  radio = 'radio',
  image = 'image',
  file = 'file',
  // Provinces and municipalities
  pca = 'pca',
}

// In order to prevent and vxe Built-in type conflict，So add a prefix
// The prefix is ​​added automatically，Just use it directly in the code（JVxeTypes.input）
export const JVxeTypePrefix = 'j-';

/** VxeTable Rendering type */
export enum JVxeRenderType {
  editer = 'editer',
  spaner = 'spaner',
  default = 'default',
}
