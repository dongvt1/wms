package com.cy.modules.qms.service;

/**
 * Service sinh mã execution theo format EXCyyyyMMddNNN.
 * Đảm bảo uniqueness bằng cách query counter hiện tại trong ngày.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface ExecutionCodeGenerator {

    /**
     * Sinh mã execution mới theo format EXCyyyyMMddNNN.
     * NNN là số thứ tự 3 chữ số, tăng dần trong ngày.
     *
     * @return mã execution duy nhất, ví dụ: EXC20260315001
     */
    String generateCode();
}
