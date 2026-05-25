package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.QmsClient;
import com.cy.modules.planning.agent.dto.QualityReport;
import com.cy.modules.planning.agent.entity.ApSyncStatus;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.enums.SyncStatus;
import com.cy.modules.planning.agent.mapper.ApSyncStatusMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.QualitySyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Triển khai đồng bộ dữ liệu chất lượng từ QMS
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class QualitySyncServiceImpl implements QualitySyncService {

    private static final String SYSTEM_NAME = "qms";

    /** Redis key prefix cho quality data */
    private static final String REDIS_KEY_PREFIX = "planning:quality:";

    /** Redis key suffix cho defect rate 30 ngày */
    private static final String DEFECT_RATE_30D_SUFFIX = ":defectRate30d";

    /** Redis key suffix cho yield rate 90 ngày */
    private static final String YIELD_RATE_90D_SUFFIX = ":yieldRate90d";

    /** Redis key suffix cho inspection results */
    private static final String INSPECTION_SUFFIX = ":inspection";

    /** Redis key cho last sync timestamp */
    private static final String LAST_SYNC_KEY = "planning:quality:lastSyncTime";

    /** Ngưỡng staleness: 30 phút */
    private static final long STALENESS_THRESHOLD_MINUTES = 30;

    /** TTL cho cache: 2 giờ */
    private static final long CACHE_TTL_HOURS = 2;

    @Resource
    private QmsClient qmsClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ApSyncStatusMapper apSyncStatusMapper;

    @Resource
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Resource
    private PlanningNotificationService planningNotificationService;

    @Override
    @Scheduled(fixedRate = 900000) // 15 phút = 900,000 ms
    public void syncQualityData() {
        log.info("[QualitySync] Bắt đầu đồng bộ dữ liệu chất lượng từ QMS");
        try {
            doSync();
        } catch (Exception e) {
            log.error("[QualitySync] Lỗi đồng bộ dữ liệu chất lượng: {}", e.getMessage(), e);
            handleSyncFailure(e);
        }
    }

    @Override
    public void triggerManualSync() {
        log.info("[QualitySync] Kích hoạt đồng bộ thủ công");
        syncQualityData();
    }

    @Override
    public BigDecimal getDefectRate30Day(String productId, String lineId) {
        String key = buildRedisKey(productId, lineId) + DEFECT_RATE_30D_SUFFIX;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return new BigDecimal(value.toString());
        }

        // Nếu không có trong cache, kiểm tra staleness và trả về null
        if (isDataStale()) {
            log.warn("[QualitySync] Dữ liệu chất lượng đã cũ (>30 phút), không có defect rate 30d cho product={}, line={}",
                    productId, lineId);
        }
        return null;
    }

    @Override
    public BigDecimal getYieldRate90Day(String productId, String lineId) {
        String key = buildRedisKey(productId, lineId) + YIELD_RATE_90D_SUFFIX;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return new BigDecimal(value.toString());
        }

        // Nếu data stale, trả về historical yield rate từ cache (nếu có)
        if (isDataStale()) {
            log.warn("[QualitySync] Dữ liệu chất lượng đã cũ (>30 phút), sử dụng historical yield rate cho product={}, line={}",
                    productId, lineId);
            // Trả về giá trị cached cuối cùng (đã hết hạn nhưng vẫn dùng được)
            return getHistoricalYieldRate(productId, lineId);
        }
        return null;
    }

    @Override
    public boolean isDataStale() {
        Object lastSyncTimeObj = redisTemplate.opsForValue().get(LAST_SYNC_KEY);
        if (lastSyncTimeObj == null) {
            // Chưa bao giờ đồng bộ thành công
            return true;
        }

        long lastSyncTime = Long.parseLong(lastSyncTimeObj.toString());
        long minutesSinceLastSync = (System.currentTimeMillis() - lastSyncTime) / (60 * 1000);
        return minutesSinceLastSync > STALENESS_THRESHOLD_MINUTES;
    }

    /**
     * Thực hiện logic đồng bộ chính.
     * Lấy dữ liệu chất lượng cho tất cả product/line đang hoạt động,
     * tính toán rolling averages và cache vào Redis.
     */
    private void doSync() {
        // 1. Lấy hoặc tạo sync status
        ApSyncStatus syncStatus = getOrCreateSyncStatus();

        // 2. Cập nhật thời điểm thử đồng bộ
        syncStatus.setLastAttemptTime(new Date());
        apSyncStatusMapper.updateById(syncStatus);

        // 3. Lấy danh sách product/line cần đồng bộ từ active batches
        List<String[]> productLineList = getActiveProductLines();

        if (productLineList.isEmpty()) {
            log.info("[QualitySync] Không có product/line đang hoạt động, bỏ qua đồng bộ");
            updateSyncSuccess(syncStatus);
            return;
        }

        // 4. Đồng bộ dữ liệu cho từng product/line
        int successCount = 0;
        int failCount = 0;
        LocalDate today = LocalDate.now();

        for (String[] productLine : productLineList) {
            String productId = productLine[0];
            String lineId = productLine[1];

            try {
                syncProductLineQuality(productId, lineId, today);
                successCount++;
            } catch (Exception e) {
                log.warn("[QualitySync] Lỗi đồng bộ quality cho product={}, line={}: {}",
                        productId, lineId, e.getMessage());
                failCount++;
            }
        }

        log.info("[QualitySync] Hoàn thành đồng bộ: success={}, fail={}", successCount, failCount);

        // 5. Cập nhật trạng thái đồng bộ
        if (failCount == 0) {
            updateSyncSuccess(syncStatus);
        } else if (successCount > 0) {
            // Partial success - vẫn coi là thành công nhưng ghi nhận warning
            updateSyncSuccess(syncStatus);
            log.warn("[QualitySync] Đồng bộ hoàn thành với {} lỗi", failCount);
        } else {
            // Tất cả đều thất bại
            handleSyncFailure(new RuntimeException("Tất cả product/line đều thất bại đồng bộ quality"));
        }
    }

    /**
     * Đồng bộ dữ liệu chất lượng cho một cặp product/line cụ thể.
     * Tính toán rolling 30-day defect rate và 90-day yield rate.
     */
    private void syncProductLineQuality(String productId, String lineId, LocalDate today) {
        String baseKey = buildRedisKey(productId, lineId);

        // Lấy dữ liệu 90 ngày (bao gồm cả 30 ngày gần nhất)
        LocalDate from90 = today.minusDays(90);
        QualityReport report90 = qmsClient.getQualityData(productId, lineId, from90, today);

        if (report90 == null) {
            log.debug("[QualitySync] Không có dữ liệu quality từ QMS cho product={}, line={}", productId, lineId);
            return;
        }

        // Tính và cache rolling 30-day defect rate
        BigDecimal defectRate30d = calculateDefectRate30Day(report90, today);
        if (defectRate30d != null) {
            redisTemplate.opsForValue().set(
                    baseKey + DEFECT_RATE_30D_SUFFIX,
                    defectRate30d.toPlainString(),
                    CACHE_TTL_HOURS, TimeUnit.HOURS
            );
        }

        // Tính và cache rolling 90-day yield rate
        BigDecimal yieldRate90d = calculateYieldRate90Day(report90);
        if (yieldRate90d != null) {
            redisTemplate.opsForValue().set(
                    baseKey + YIELD_RATE_90D_SUFFIX,
                    yieldRate90d.toPlainString(),
                    CACHE_TTL_HOURS, TimeUnit.HOURS
            );
            // Lưu thêm bản historical (TTL dài hơn) để dùng khi data stale
            redisTemplate.opsForValue().set(
                    baseKey + ":historicalYield",
                    yieldRate90d.toPlainString(),
                    24, TimeUnit.HOURS
            );
        }

        // Cache inspection results
        if (report90.getInspectionResults() != null && !report90.getInspectionResults().isEmpty()) {
            redisTemplate.opsForValue().set(
                    baseKey + INSPECTION_SUFFIX,
                    report90.getInspectionResults(),
                    CACHE_TTL_HOURS, TimeUnit.HOURS
            );
        }

        log.debug("[QualitySync] Cached quality data cho product={}, line={}: defectRate30d={}, yieldRate90d={}",
                productId, lineId, defectRate30d, yieldRate90d);
    }

    /**
     * Tính tỷ lệ lỗi trung bình 30 ngày từ dữ liệu báo cáo.
     * Sử dụng inspection results trong 30 ngày gần nhất.
     */
    private BigDecimal calculateDefectRate30Day(QualityReport report, LocalDate today) {
        // Ưu tiên sử dụng giá trị đã tính sẵn từ QMS
        if (report.getRollingThirtyDayDefectRate() != null) {
            return report.getRollingThirtyDayDefectRate();
        }

        // Tính từ inspection results nếu có
        if (report.getInspectionResults() == null || report.getInspectionResults().isEmpty()) {
            return report.getAverageDefectRate();
        }

        LocalDate thirtyDaysAgo = today.minusDays(30);
        BigDecimal totalInspected = BigDecimal.ZERO;
        BigDecimal totalDefects = BigDecimal.ZERO;

        for (QualityReport.InspectionResult result : report.getInspectionResults()) {
            if (result.getInspectionDate() != null && !result.getInspectionDate().isBefore(thirtyDaysAgo)) {
                if (result.getInspectedQuantity() != null) {
                    totalInspected = totalInspected.add(result.getInspectedQuantity());
                }
                if (result.getDefectQuantity() != null) {
                    totalDefects = totalDefects.add(result.getDefectQuantity());
                }
            }
        }

        if (totalInspected.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // Tỷ lệ lỗi = (tổng lỗi / tổng kiểm tra) * 100
        return totalDefects.multiply(BigDecimal.valueOf(100))
                .divide(totalInspected, 4, RoundingMode.HALF_UP);
    }

    /**
     * Tính tỷ lệ yield trung bình 90 ngày.
     * Yield rate = (tổng đạt / tổng kiểm tra) * 100
     */
    private BigDecimal calculateYieldRate90Day(QualityReport report) {
        // Ưu tiên sử dụng giá trị đã tính sẵn từ QMS
        if (report.getRollingNinetyDayDefectRate() != null && report.getYieldRate() != null) {
            return report.getYieldRate();
        }

        if (report.getYieldRate() != null) {
            return report.getYieldRate();
        }

        // Tính từ inspection results
        if (report.getInspectionResults() == null || report.getInspectionResults().isEmpty()) {
            // Nếu có defect rate, yield = 100 - defect rate
            if (report.getAverageDefectRate() != null) {
                return BigDecimal.valueOf(100).subtract(report.getAverageDefectRate());
            }
            return null;
        }

        BigDecimal totalInspected = BigDecimal.ZERO;
        BigDecimal totalPassed = BigDecimal.ZERO;

        for (QualityReport.InspectionResult result : report.getInspectionResults()) {
            if (result.getInspectedQuantity() != null) {
                totalInspected = totalInspected.add(result.getInspectedQuantity());
            }
            if (result.getPassedQuantity() != null) {
                totalPassed = totalPassed.add(result.getPassedQuantity());
            }
        }

        if (totalInspected.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // Yield rate = (tổng đạt / tổng kiểm tra) * 100
        return totalPassed.multiply(BigDecimal.valueOf(100))
                .divide(totalInspected, 4, RoundingMode.HALF_UP);
    }

    /**
     * Lấy historical yield rate khi data stale (>30 phút).
     * Trả về giá trị cached với TTL dài hơn.
     */
    private BigDecimal getHistoricalYieldRate(String productId, String lineId) {
        String key = buildRedisKey(productId, lineId) + ":historicalYield";
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.info("[QualitySync] Sử dụng historical yield rate cho product={}, line={} (data stale)",
                    productId, lineId);
            // Gửi cảnh báo staleness
            notifyStalenessWarning(productId, lineId);
            return new BigDecimal(value.toString());
        }
        return null;
    }

    /**
     * Gửi cảnh báo khi dữ liệu chất lượng đã cũ.
     */
    private void notifyStalenessWarning(String productId, String lineId) {
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("lineId", lineId);
        data.put("systemName", SYSTEM_NAME);
        data.put("stalenessMinutes", getDataStalenessMinutes());

        planningNotificationService.notifyProductionManager(
                NotificationType.SYNC_FAILURE,
                String.format("Dữ liệu chất lượng từ QMS đã cũ hơn 30 phút cho sản phẩm %s trên dây chuyền %s. " +
                        "Đang sử dụng historical yield rate.", productId, lineId),
                data
        );
    }

    /**
     * Lấy danh sách product/line đang hoạt động (có batch đang sản xuất).
     * Trả về list các mảng [productId, lineId].
     */
    private List<String[]> getActiveProductLines() {
        LambdaQueryWrapper<WeeklyPlanBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WeeklyPlanBatch::getStatus, "planned", "in_progress");
        wrapper.select(WeeklyPlanBatch::getProductType, WeeklyPlanBatch::getProductionLineId);

        List<WeeklyPlanBatch> batches = weeklyPlanBatchMapper.selectList(wrapper);

        if (batches == null || batches.isEmpty()) {
            return Collections.emptyList();
        }

        // Deduplicate product/line combinations
        Set<String> seen = new HashSet<>();
        List<String[]> result = new ArrayList<>();
        for (WeeklyPlanBatch batch : batches) {
            String key = batch.getProductType() + "|" + batch.getProductionLineId();
            if (seen.add(key)) {
                result.add(new String[]{batch.getProductType(), batch.getProductionLineId()});
            }
        }
        return result;
    }

    /**
     * Lấy hoặc tạo mới bản ghi sync status cho QMS.
     */
    private ApSyncStatus getOrCreateSyncStatus() {
        LambdaQueryWrapper<ApSyncStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApSyncStatus::getSystemName, SYSTEM_NAME);
        ApSyncStatus syncStatus = apSyncStatusMapper.selectOne(wrapper);

        if (syncStatus == null) {
            syncStatus = new ApSyncStatus();
            syncStatus.setSystemName(SYSTEM_NAME);
            syncStatus.setStatus(SyncStatus.ACTIVE.getValue());
            syncStatus.setConsecutiveFailures(0);
            apSyncStatusMapper.insert(syncStatus);
            log.info("[QualitySync] Tạo mới bản ghi sync status cho {}", SYSTEM_NAME);
        }
        return syncStatus;
    }

    /**
     * Cập nhật trạng thái đồng bộ thành công.
     */
    private void updateSyncSuccess(ApSyncStatus syncStatus) {
        Date now = new Date();
        syncStatus.setLastSyncTime(now);
        syncStatus.setLastAttemptTime(now);
        syncStatus.setStatus(SyncStatus.ACTIVE.getValue());
        syncStatus.setConsecutiveFailures(0);
        syncStatus.setLastError(null);
        syncStatus.setDataStalenessMinutes(0);
        apSyncStatusMapper.updateById(syncStatus);

        // Cập nhật last sync time trong Redis
        redisTemplate.opsForValue().set(LAST_SYNC_KEY, String.valueOf(System.currentTimeMillis()));

        log.info("[QualitySync] Đồng bộ thành công tại {}", now);
    }

    /**
     * Xử lý khi đồng bộ thất bại: tăng consecutive_failures và ghi lỗi.
     */
    private void handleSyncFailure(Exception e) {
        try {
            ApSyncStatus syncStatus = getOrCreateSyncStatus();
            int failures = (syncStatus.getConsecutiveFailures() != null ? syncStatus.getConsecutiveFailures() : 0) + 1;
            syncStatus.setConsecutiveFailures(failures);
            syncStatus.setLastAttemptTime(new Date());
            syncStatus.setStatus(SyncStatus.FAILED.getValue());
            syncStatus.setLastError(truncateError(e.getMessage()));

            // Tính staleness nếu có last_sync_time
            if (syncStatus.getLastSyncTime() != null) {
                long minutesSinceLastSync = (System.currentTimeMillis() - syncStatus.getLastSyncTime().getTime()) / (60 * 1000);
                syncStatus.setDataStalenessMinutes((int) minutesSinceLastSync);

                // Nếu staleness > 30 phút, đánh dấu stale
                if (minutesSinceLastSync > STALENESS_THRESHOLD_MINUTES) {
                    syncStatus.setStatus(SyncStatus.STALE.getValue());
                }
            }

            apSyncStatusMapper.updateById(syncStatus);

            // Gửi thông báo lỗi đồng bộ
            Map<String, Object> data = new HashMap<>();
            data.put("systemName", SYSTEM_NAME);
            data.put("consecutiveFailures", failures);
            data.put("error", truncateError(e.getMessage()));

            planningNotificationService.notifyProductionManager(
                    NotificationType.SYNC_FAILURE,
                    String.format("Lỗi đồng bộ dữ liệu chất lượng từ QMS (lần thứ %d): %s",
                            failures, truncateError(e.getMessage())),
                    data
            );

            log.warn("[QualitySync] Ghi nhận thất bại lần thứ {} cho {}", failures, SYSTEM_NAME);
        } catch (Exception ex) {
            log.error("[QualitySync] Không thể cập nhật trạng thái thất bại: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Lấy số phút kể từ lần đồng bộ thành công gần nhất.
     */
    private long getDataStalenessMinutes() {
        Object lastSyncTimeObj = redisTemplate.opsForValue().get(LAST_SYNC_KEY);
        if (lastSyncTimeObj == null) {
            return -1;
        }
        long lastSyncTime = Long.parseLong(lastSyncTimeObj.toString());
        return (System.currentTimeMillis() - lastSyncTime) / (60 * 1000);
    }

    /**
     * Xây dựng Redis key cho quality data.
     */
    private String buildRedisKey(String productId, String lineId) {
        return REDIS_KEY_PREFIX + productId + ":" + lineId;
    }

    /**
     * Cắt ngắn thông báo lỗi để tránh vượt quá giới hạn cột DB.
     */
    private String truncateError(String errorMessage) {
        if (errorMessage == null) {
            return "Unknown error";
        }
        return errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage;
    }
}
