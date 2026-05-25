package com.cy.modules.planning.agent.client;

import com.cy.modules.planning.agent.dto.MachineStatus;
import com.cy.modules.planning.agent.dto.ProductionProgress;

import java.time.LocalDate;
import java.util.List;

/**
 * Client interface cho tích hợp với hệ thống Scada.
 * Cung cấp khả năng lấy trạng thái máy và tiến độ sản xuất.
 */
public interface ScadaClient {

    /**
     * Lấy trạng thái hiện tại của các máy trên các dây chuyền sản xuất.
     *
     * @param lineIds danh sách mã dây chuyền sản xuất
     * @return danh sách trạng thái máy
     */
    List<MachineStatus> getMachineStatuses(List<String> lineIds);

    /**
     * Lấy tiến độ sản xuất của dây chuyền trong ngày.
     *
     * @param lineId mã dây chuyền sản xuất
     * @param date   ngày cần lấy tiến độ
     * @return tiến độ sản xuất
     */
    ProductionProgress getProductionProgress(String lineId, LocalDate date);
}
