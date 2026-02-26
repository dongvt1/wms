export type ErrorMessageMode = 'none' | 'modal' | 'message' | undefined;
export type SuccessMessageMode = 'none' | 'success' | 'error' | undefined;

export interface RequestOptions {
  // Splice request parameters intourl
  joinParamsToUrl?: boolean;
  // Format request parameter time
  formatDate?: boolean;
  // Whether to process the request result
  isTransformResponse?: boolean;
  // Whether to return local response headers,Use this attribute when you need to get the response headers
  isReturnNativeResponse?: boolean;
  // The default will beprefix add tourl
  joinPrefix?: boolean;
  // interface address，If left empty，then use the default value
  apiUrl?: string;
  // Request splicing path
  urlPrefix?: string;
  // Error message prompt type
  errorMessageMode?: ErrorMessageMode;
  // Success message prompt type
  successMessageMode?: SuccessMessageMode;
  // Whether to add timestamp
  joinTime?: boolean;
  ignoreCancelToken?: boolean;
  //Whether to send token in header
  withToken?: boolean;
}

export interface Result<T = any> {
  code: number;
  type: 'success' | 'error' | 'warning';
  message: string;
  result: T;
}

//File upload parameters
export interface UploadFileParams {
  // Other parameters
  data?: Recordable;
  // File parameter interface field name
  name?: string;
  // document
  file: File | Blob;
  // document名
  filename?: string;
  [key: string]: any;
}
//document返回参数
export interface UploadFileCallBack {
  // Success callback method
  success?: any;
  // Whether to return response headers,Use this attribute when you need to get the response headers
  isReturnResponse?: boolean;
}
