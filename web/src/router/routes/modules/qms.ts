import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';

const qms: AppRouteModule = {
  path: '/qms',
  name: 'QMS',
  component: LAYOUT,
  redirect: '/qms/iqc',
  meta: {
    orderNo: 300000,
    icon: 'ant-design:safety-certificate-outlined',
    title: 'Quản lý chất lượng (QMS)',
  },
  children: [
    // --- Existing QMS routes ---
    {
      path: 'iqc',
      name: 'QmsIqcInspection',
      component: () => import('/@/views/qms/IqcInspectionList.vue'),
      meta: {
        title: 'Kiểm tra đầu vào (IQC)',
      },
    },
    {
      path: 'pqc',
      name: 'QmsPqcInspection',
      component: () => import('/@/views/qms/PqcInspectionList.vue'),
      meta: {
        title: 'Kiểm tra sản xuất (PQC)',
      },
    },
    {
      path: 'fqc',
      name: 'QmsFqcInspection',
      component: () => import('/@/views/qms/FqcInspectionList.vue'),
      meta: {
        title: 'Kiểm tra thành phẩm (FQC)',
      },
    },
    {
      path: 'ncr',
      name: 'QmsNcr',
      component: () => import('/@/views/qms/NcrList.vue'),
      meta: {
        title: 'Báo cáo NCR',
      },
    },
    {
      path: 'checklist',
      name: 'QmsChecklistTemplate',
      component: () => import('/@/views/qms/ChecklistTemplateList.vue'),
      meta: {
        title: 'Mẫu Checklist',
      },
    },
    {
      path: 'stage',
      name: 'QmsStage',
      component: () => import('/@/views/qms/QcStageList.vue'),
      meta: {
        title: 'Công đoạn QC',
      },
    },
    {
      path: 'session',
      name: 'QmsSession',
      component: () => import('/@/views/qms/QcSessionList.vue'),
      meta: {
        title: 'Phiên kiểm tra',
      },
    },
    {
      path: 'review',
      name: 'QmsReview',
      component: () => import('/@/views/qms/QcReviewList.vue'),
      meta: {
        title: 'QC Review',
      },
    },
    {
      path: 'analytics',
      name: 'QmsAnalytics',
      component: () => import('/@/views/qms/QmsAnalyticsDashboard.vue'),
      meta: {
        title: 'Phân tích chất lượng',
      },
    },
    {
      path: 'supplier-report',
      name: 'QmsSupplierReport',
      component: () => import('/@/views/qms/SupplierQualityReport.vue'),
      meta: {
        title: 'Báo cáo chất lượng NCC',
        hideMenu: true,
      },
    },

    // --- Inspection Template routes (Quản_lý_QC) ---
    {
      path: 'inspection-template',
      name: 'QmsInspectionTemplateList',
      component: () => import('/@/views/qms/InspectionTemplateList.vue'),
      meta: {
        title: 'Mẫu kiểm tra (Inspection Template)',
        // Permission: Quản_lý_QC only
      },
    },
    {
      path: 'inspection-template/form/:id',
      name: 'QmsInspectionTemplateForm',
      component: () => import('/@/views/qms/InspectionTemplateForm.vue'),
      meta: {
        title: 'Cấu hình mẫu kiểm tra',
        hideMenu: true,
        currentActiveMenu: '/qms/inspection-template',
        // Permission: Quản_lý_QC only
      },
    },

    // --- Inspection Execution routes (Nhân_viên_QC + Quản_lý_QC) ---
    {
      path: 'inspection-execution',
      name: 'QmsInspectionExecutionList',
      component: () => import('/@/views/qms/InspectionExecutionList.vue'),
      meta: {
        title: 'Phiên kiểm tra (Inspection Execution)',
        // Permission: Nhân_viên_QC + Quản_lý_QC
      },
    },
    {
      path: 'inspection-execution/:id',
      name: 'QmsInspectionExecutionForm',
      component: () => import('/@/views/qms/InspectionExecutionForm.vue'),
      meta: {
        title: 'Thực hiện kiểm tra',
        hideMenu: true,
        currentActiveMenu: '/qms/inspection-execution',
        // Permission: Nhân_viên_QC + Quản_lý_QC
      },
    },

    // --- Approval route (Quản_lý_QC only) ---
    {
      path: 'approval',
      name: 'QmsApproval',
      component: () => import('/@/views/qms/ApprovalPanel.vue'),
      meta: {
        title: 'Phê duyệt kết quả kiểm tra',
        // Permission: Quản_lý_QC only
      },
    },

    // --- Report route (Quản_lý_QC + Nhân_viên_QC) ---
    {
      path: 'report',
      name: 'QmsInspectionReport',
      component: () => import('/@/views/qms/InspectionReportView.vue'),
      meta: {
        title: 'Báo cáo kiểm tra',
        // Permission: Quản_lý_QC + Nhân_viên_QC
      },
    },
  ],
};

export default qms;
