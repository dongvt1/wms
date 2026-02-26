/**
 * JInputComponent type
 */
export enum JInputTypeEnum {
  //Vague
  JINPUT_QUERY_LIKE = 'like',
  //No
  JINPUT_QUERY_NE = 'ne',
  //Greater than or equal to
  JINPUT_QUERY_GE = 'ge',
  //less than or equal to
  JINPUT_QUERY_LE = 'le',
}

/**
 * Constant definitions required by the panel designer
 */
export enum JDragConfigEnum {
  //baseURL
  DRAG_BASE_URL = 'drag-base-url',
  //Drag cache prefix
  DRAG_CACHE_PREFIX = 'drag-cache:',
}
// electron enumerate
export enum ElectronEnum {
  ELECTRON_API = '_ELECTRON_PRELOAD_UTILS_',
}
