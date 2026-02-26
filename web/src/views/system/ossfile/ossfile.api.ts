import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/oss/file/list',
  deleteFile = '/sys/oss/file/delete',
  ossUpload = '/sys/oss/file/upload',
  minioUpload = '/sys/upload/uploadMinio',
}

/**
 * ossupload
 * @param params
 */
export const getOssUrl = Api.ossUpload;
/**
 * minioupload
 * @param params
 */
export const getMinioUrl = Api.minioUpload;
/**
 * List interface
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Delete user
 */
export const deleteFile = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteFile, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
