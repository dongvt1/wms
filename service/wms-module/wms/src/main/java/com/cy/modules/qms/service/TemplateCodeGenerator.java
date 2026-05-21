package com.cy.modules.qms.service;

/**
 * Service sinh mã template theo format TPLyyyyMMddNNN.
 * Đảm bảo uniqueness bằng cách query counter hiện tại trong ngày.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface TemplateCodeGenerator {

    /**
     * Sinh mã template mới theo format TPLyyyyMMddNNN.
     * NNN là số thứ tự 3 chữ số, tăng dần trong ngày.
     *
     * @return mã template duy nhất, ví dụ: TPL20260315001
     */
    String generateCode();
}
