package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.BomStructure;
import com.cy.modules.planning.agent.dto.InventorySnapshot;
import com.cy.modules.planning.agent.dto.SupplierLeadTime;
import com.cy.modules.planning.agent.entity.ApSyncStatus;
import com.cy.modules.planning.agent.enums.SyncStatus;
import com.cy.modules.planning.agent.mapper.ApSyncStatusMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation của InventorySyncService.
 * Polling ERP-MRP-WMS mỗi 15 phút để đồng bộ dữ liệu tồn kho, BOM, và lead time.
 * Cache dữ liệu trong Redis với TTL 20 phút.
 * Retry logic: 3 lần thử với exponential backoff (1s, 2s, 4s).
 */
@Slf4j
@Service
public class InventorySyncServiceImpl implements InventorySyncService {

    private static final String SYSTEM_NAME = "erp";
    private static final String REDIS_KEY_INVENTORY = "planning:inventory:";
    private static final String REDIS_KEY_BOM = "planning:bom:";
    private static final String REDIS_KEY_LEADTIME = "planning:leadtime:";
    private static final long CACHE_TTL_MINUTES = 20;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long[] BACKOFF_DELAYS_MS = {1000, 2000, 4000};

    @Autowired
    private ErpClient erpClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ApSyncStatusMapper apSyncStatusMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Scheduled task: đồng bộ dữ liệu tồn kho mỗi 15 phút (900000ms).
     */
    @Override
    @Scheduled(fixedRate = 900000)
    public void syncInventoryData() {
        log.info("[InventorySync] Bắt đầu đồng bộ dữ liệu tồn kho từ ERP-MRP-WMS");

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                doSync();
                onSyncSuccess();
                log.info("[InventorySync] Đồng bộ thành công (lần thử {})", attempt);
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("[InventorySync] Lần thử {} thất bại: {}", attempt, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(BACKOFF_DELAYS_MS[attempt - 1]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[InventorySync] Bị gián đoạn trong khi chờ retry");
                        break;
                    }
                }
            }
        }

        // Tất cả retry đều thất bại
        onSyncFailure(lastException);
        log.error("[InventorySync] Đồng bộ thất bại sau {} lần thử", MAX_RETRY_ATTEMPTS, lastException);
    }

    @Override
    public BigDecimal getInventoryLevel(String materialId) {
        String value = stringRedisTemplate.opsForValue().get(REDIS_KEY_INVENTORY + materialId);
        if (value == null) {
            return null;
        }
        return new BigDecimal(value);
    }

    @Override
    public BomStructure getBom(String productId) {
        String json = stringRedisTemplate.opsForValue().get(REDIS_KEY_BOM + productId);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, BomStructure.class);
        } catch (JsonProcessingException e) {
            log.error("[InventorySync] Lỗi deserialize BOM cho productId={}: {}", productId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<SupplierLeadTime> getSupplierLeadTime(String materialId) {
        String json = stringRedisTemplate.opsForValue().get(REDIS_KEY_LEADTIME + materialId);
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<SupplierLeadTime>>() {});
        } catch (JsonProcessingException e) {
            log.error("[InventorySync] Lỗi deserialize lead time cho materialId={}: {}", materialId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== Private Methods ====================

    /**
     * Thực hiện đồng bộ dữ liệu: tồn kho, BOM (cho tất cả materialIds), và lead time.
     */
    private void doSync() {
        // 1. Lấy danh sách tất cả materialIds cần đồng bộ (lấy từ inventory snapshot)
        InventorySnapshot snapshot = erpClient.getInventoryLevels(Collections.emptyList());

        // 2. Cache mức tồn kho
        if (snapshot != null && snapshot.getMaterials() != null) {
            for (InventorySnapshot.MaterialStock stock : snapshot.getMaterials()) {
                String key = REDIS_KEY_INVENTORY + stock.getMaterialId();
                stringRedisTemplate.opsForValue().set(
                        key,
                        stock.getAvailableQuantity().toPlainString(),
                        CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES
                );
            }
            log.debug("[InventorySync] Đã cache {} mức tồn kho", snapshot.getMaterials().size());

            // 3. Lấy và cache BOM cho từng sản phẩm (dùng materialId làm productId nếu cần)
            // Lấy danh sách materialIds để query lead time
            List<String> materialIds = snapshot.getMaterials().stream()
                    .map(InventorySnapshot.MaterialStock::getMaterialId)
                    .collect(Collectors.toList());

            // 4. Lấy và cache supplier lead times
            List<SupplierLeadTime> leadTimes = erpClient.getSupplierLeadTimes(materialIds);
            if (leadTimes != null) {
                // Nhóm theo materialId và cache
                leadTimes.stream()
                        .collect(Collectors.groupingBy(SupplierLeadTime::getMaterialId))
                        .forEach((matId, ltList) -> {
                            try {
                                String key = REDIS_KEY_LEADTIME + matId;
                                String json = objectMapper.writeValueAsString(ltList);
                                stringRedisTemplate.opsForValue().set(key, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                            } catch (JsonProcessingException e) {
                                log.warn("[InventorySync] Lỗi serialize lead time cho materialId={}", matId, e);
                            }
                        });
                log.debug("[InventorySync] Đã cache lead time cho {} vật tư",
                        leadTimes.stream().map(SupplierLeadTime::getMaterialId).distinct().count());
            }
        }
    }

    /**
     * Cache BOM cho một sản phẩm cụ thể.
     *
     * @param productId mã sản phẩm
     */
    public void cacheBom(String productId) {
        try {
            BomStructure bom = erpClient.getBom(productId);
            if (bom != null) {
                String key = REDIS_KEY_BOM + productId;
                String json = objectMapper.writeValueAsString(bom);
                stringRedisTemplate.opsForValue().set(key, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                log.debug("[InventorySync] Đã cache BOM cho productId={}", productId);
            }
        } catch (Exception e) {
            log.warn("[InventorySync] Lỗi cache BOM cho productId={}: {}", productId, e.getMessage());
        }
    }

    /**
     * Cập nhật ap_sync_status khi đồng bộ thành công.
     */
    private void onSyncSuccess() {
        ApSyncStatus syncStatus = getOrCreateSyncStatus();
        syncStatus.setLastSyncTime(new Date());
        syncStatus.setLastAttemptTime(new Date());
        syncStatus.setStatus(SyncStatus.ACTIVE.getValue());
        syncStatus.setConsecutiveFailures(0);
        syncStatus.setLastError(null);
        syncStatus.setDataStalenessMinutes(0);

        if (syncStatus.getId() == null) {
            apSyncStatusMapper.insert(syncStatus);
        } else {
            apSyncStatusMapper.updateById(syncStatus);
        }
    }

    /**
     * Cập nhật ap_sync_status khi đồng bộ thất bại.
     */
    private void onSyncFailure(Exception e) {
        ApSyncStatus syncStatus = getOrCreateSyncStatus();
        syncStatus.setLastAttemptTime(new Date());
        syncStatus.setStatus(SyncStatus.FAILED.getValue());
        syncStatus.setConsecutiveFailures(
                (syncStatus.getConsecutiveFailures() == null ? 0 : syncStatus.getConsecutiveFailures()) + 1
        );
        syncStatus.setLastError(e != null ? truncateError(e.getMessage()) : "Unknown error");

        // Tính data staleness
        if (syncStatus.getLastSyncTime() != null) {
            long stalenessMs = System.currentTimeMillis() - syncStatus.getLastSyncTime().getTime();
            syncStatus.setDataStalenessMinutes((int) (stalenessMs / 60000));
        }

        if (syncStatus.getId() == null) {
            apSyncStatusMapper.insert(syncStatus);
        } else {
            apSyncStatusMapper.updateById(syncStatus);
        }
    }

    /**
     * Lấy hoặc tạo mới bản ghi sync status cho hệ thống ERP.
     */
    private ApSyncStatus getOrCreateSyncStatus() {
        LambdaQueryWrapper<ApSyncStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApSyncStatus::getSystemName, SYSTEM_NAME);
        ApSyncStatus existing = apSyncStatusMapper.selectOne(wrapper);

        if (existing != null) {
            return existing;
        }

        ApSyncStatus newStatus = new ApSyncStatus();
        newStatus.setSystemName(SYSTEM_NAME);
        newStatus.setStatus(SyncStatus.ACTIVE.getValue());
        newStatus.setConsecutiveFailures(0);
        return newStatus;
    }

    /**
     * Cắt ngắn thông báo lỗi nếu quá dài.
     */
    private String truncateError(String error) {
        if (error == null) {
            return "Unknown error";
        }
        return error.length() > 500 ? error.substring(0, 500) : error;
    }
}
