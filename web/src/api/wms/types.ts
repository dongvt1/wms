/**
 * Kiểu dữ liệu phân trang chuẩn
 */
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Params phân trang chuẩn
 */
export interface PageParams {
  pageNo?: number;
  pageSize?: number;
  column?: string;
  order?: string;
  [key: string]: any;
}
