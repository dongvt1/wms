import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/qms/notification';

export interface QmsNotificationModel {
  id?: string;
  userId?: string;
  title?: string;
  content?: string;
  entityType?: string; // 'iqc' | 'pqc' | 'fqc' | 'ncr' | 'review'
  entityId?: string;
  isRead?: number; // 0 = unread, 1 = read
  createTime?: string;
}

export const qmsNotificationApi = {
  /** Danh sách thông báo cho người dùng hiện tại */
  list: (params?: PageParams) =>
    defHttp.get<PageResult<QmsNotificationModel>>({ url: `${BASE}/list`, params }),

  /** Số lượng thông báo chưa đọc */
  unreadCount: () => defHttp.get<number>({ url: `${BASE}/unreadCount` }),

  /** Đánh dấu đã đọc một thông báo */
  markRead: (id: string) => defHttp.put({ url: `${BASE}/markRead/${id}` }),

  /** Đánh dấu tất cả đã đọc */
  markAllRead: () => defHttp.put({ url: `${BASE}/markAllRead` }),
};
