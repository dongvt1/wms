import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useGlobSetting } from '/@/hooks/setting';

const { createMessage, createWarningModal } = useMessage();
const glob = useGlobSetting();

/**
 * Export filexlsxofmime-type
 */
export const XLSX_MIME_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
/**
 * Export filexlsxof文件后缀
 */
export const XLSX_FILE_SUFFIX = '.xlsx';

export function useMethods() {
  /**
   * Exportxls
   * @param name
   * @param url
   */
  async function exportXls(name, url, params, isXlsx = false) {
    //update-begin---author:wangshuai---date:2024-01-25---for:【QQYUN-8118】Export超时时间设置长点---
    // Modified to return to native response，Easy to access headers
    const response = await defHttp.get(
      { url: url, params: params, responseType: 'blob', timeout: 60000 },
      { isTransformResponse: false, isReturnNativeResponse: true }
    );
    //update-end---author:wangshuai---date:2024-01-25---for:【QQYUN-8118】Export超时时间设置长点---
    if (!response || !response.data) {
      createMessage.warning('File download failed');
      return;
    }
    // judge header middle content-disposition Does it contain .xlsx
    let isXlsxByHeader = isXlsx;
    const disposition = response.headers && response.headers['content-disposition'];
    if (disposition && disposition.indexOf('.xlsx') !== -1) {
      isXlsxByHeader = true;
    }
    const data = response.data;
    //update-begin---author:wangshuai---date:2024-04-18---for: ExportexcelFailure prompt，不进行Export---
    let reader = new FileReader()
    reader.readAsText(data, 'utf-8')
    reader.onload = async () => {
      if(reader.result){
        if(reader.result.toString().indexOf("success") !=-1){
          // update-begin---author:liaozhiyang---date:2025-02-11---for:【issues/7738】文件middle带"success"Export报错 ---
          try {
            const { success, message } = JSON.parse(reader.result.toString());
            if (!success) {
              createMessage.warning('Export失败，Reason for failure：' + message);
            } else {
              exportExcel(name, isXlsxByHeader, data);
            }
            return;
          } catch (error) {
            exportExcel(name, isXlsxByHeader, data);
          }
          // update-end---author:liaozhiyang---date:2025-02-11---for:【issues/7738】文件middle带"success"Export报错 ---
        }
      }
      exportExcel(name, isXlsxByHeader, data);
      //update-end---author:wangshuai---date:2024-04-18---for: ExportexcelFailure prompt，不进行Export---
    }
  }

  /**
   * importxls
   * @param data importof数据
   * @param url
   * @param success 成功后of回调
   */
  async function importXls(data, url, success) {
    const isReturn = (fileInfo) => {
      try {
        if (fileInfo.code === 201) {
          let {
            message,
            result: { msg, fileUrl, fileName },
          } = fileInfo;
          let href = glob.uploadUrl + fileUrl;
          createWarningModal({
            title: message,
            centered: false,
            content: `<div>
                                <span>${msg}</span><br/> 
                                <span>For specific details please<a href = ${href} download = ${fileName}> Click to download </a> </span> 
                              </div>`,
          });
          //update-begin---author:wangshuai ---date:20221121  for：[VUEN-2827]import无权限，Tip icon error------------
        } else if (fileInfo.code === 500 || fileInfo.code === 510) {
          createMessage.error(fileInfo.message || `${data.file.name} import失败`);
          //update-end---author:wangshuai ---date:20221121  for：[VUEN-2827]import无权限，Tip icon error------------
        } else {
          createMessage.success(fileInfo.message || `${data.file.name} File uploaded successfully`);
        }
      } catch (error) {
        console.log('importof数据异常', error);
      } finally {
        typeof success === 'function' ? success(fileInfo) : '';
      }
    };
    await defHttp.uploadFile({ url }, { file: data.file }, { success: isReturn });
  }

  return {
    handleExportXls: (name: string, url: string, params?: object) => exportXls(name, url, params),
    handleImportXls: (data, url, success) => importXls(data, url, success),
    handleExportXlsx: (name: string, url: string, params?: object) => exportXls(name, url, params, true),
  };

  /**
   * Exportexcel
   * @param name
   * @param isXlsx
   * @param data
   */
  function exportExcel(name, isXlsx, data) {
    if (!name || typeof name != 'string') {
      name = 'Export file';
    }
    let blobOptions = { type: 'application/vnd.ms-excel' };
    let fileSuffix = '.xls';
    if (isXlsx) {
      blobOptions['type'] = XLSX_MIME_TYPE;
      fileSuffix = XLSX_FILE_SUFFIX;
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data], blobOptions), name + fileSuffix);
    } else {
      let url = window.URL.createObjectURL(new Blob([data], blobOptions));
      let link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', name + fileSuffix);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //Download complete remove elements
      window.URL.revokeObjectURL(url); //releaseblobobject
    }
  }
}
