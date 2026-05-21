import { defHttp } from '/@/utils/http/axios';

const BASE = '/qms/attachment';

export interface QmsAttachmentModel {
  id?: string;
  entityType?: string;
  entityId?: string;
  fileName?: string;
  filePath?: string;
  fileSize?: number;
  fileType?: string;
  uploadBy?: string;
  uploadTime?: string;
}

export const wmsAttachmentApi = {
  /** Danh sách tệp đính kèm theo entity */
  list: (entityType: string, entityId: string) =>
    defHttp.get<QmsAttachmentModel[]>({ url: `${BASE}/list`, params: { entityType, entityId } }),

  /** Xóa tệp đính kèm */
  delete: (id: string) => defHttp.delete({ url: `${BASE}/delete`, params: { id } }),
};

/** URL upload dùng cho a-upload action */
export const qmsAttachmentUploadUrl = `${BASE}/upload`;
