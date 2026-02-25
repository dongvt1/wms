package org.jeecg.modules.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.planning.entity.Routing;
import org.jeecg.modules.planning.entity.RoutingStep;
import org.jeecg.modules.planning.mapper.RoutingMapper;
import org.jeecg.modules.planning.mapper.RoutingStepMapper;
import org.jeecg.modules.planning.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: Routing Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
public class RoutingServiceImpl extends ServiceImpl<RoutingMapper, Routing> implements RoutingService {

    @Autowired
    private RoutingStepMapper routingStepMapper;

    @Override
    public List<Routing> getByProductId(String productId) {
        return baseMapper.selectByProductId(productId);
    }

    @Override
    public List<Routing> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public boolean isCodeUnique(String routingCode, String excludeId) {
        QueryWrapper<Routing> qw = new QueryWrapper<>();
        qw.eq("routing_code", routingCode);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoutingWithSteps(Routing routing, List<RoutingStep> steps) {
        // Calculate and set lead time for each step
        BigDecimal totalLt = BigDecimal.ZERO;
        if (steps != null) {
            for (RoutingStep step : steps) {
                BigDecimal stepLt = calculateStepLeadTime(step);
                step.setLeadTimeHours(stepLt);
                totalLt = totalLt.add(stepLt);
            }
        }
        routing.setTotalLeadTimeHours(totalLt);

        this.save(routing);
        if (steps != null && !steps.isEmpty()) {
            for (RoutingStep step : steps) {
                step.setRoutingId(routing.getId());
                routingStepMapper.insert(step);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoutingWithSteps(Routing routing, List<RoutingStep> steps) {
        // Calculate and set lead time for each step
        BigDecimal totalLt = BigDecimal.ZERO;
        if (steps != null) {
            for (RoutingStep step : steps) {
                BigDecimal stepLt = calculateStepLeadTime(step);
                step.setLeadTimeHours(stepLt);
                totalLt = totalLt.add(stepLt);
            }
        }
        routing.setTotalLeadTimeHours(totalLt);

        this.updateById(routing);
        // Delete existing steps and reinsert
        routingStepMapper.deleteByRoutingId(routing.getId());
        if (steps != null && !steps.isEmpty()) {
            for (RoutingStep step : steps) {
                step.setRoutingId(routing.getId());
                routingStepMapper.insert(step);
            }
        }
        return true;
    }

    @Override
    public List<RoutingStep> getRoutingSteps(String routingId) {
        return routingStepMapper.selectByRoutingId(routingId);
    }

    @Override
    public Map<String, Object> getRoutingDetail(String routingId) {
        Map<String, Object> result = new HashMap<>();
        Routing routing = this.getById(routingId);
        result.put("routing", routing);
        List<RoutingStep> steps = routingStepMapper.selectByRoutingId(routingId);
        result.put("steps", steps);
        return result;
    }

    @Override
    public BigDecimal calculateTotalLeadTime(String routingId, BigDecimal quantity) {
        List<RoutingStep> steps = routingStepMapper.selectByRoutingId(routingId);
        if (steps == null || steps.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalMinutes = BigDecimal.ZERO;
        for (RoutingStep step : steps) {
            int setup = step.getSetupTimeMinutes() != null ? step.getSetupTimeMinutes() : 0;
            int run = step.getRunTimeMinutes() != null ? step.getRunTimeMinutes() : 0;
            int wait = step.getWaitTimeMinutes() != null ? step.getWaitTimeMinutes() : 0;
            int move = step.getMoveTimeMinutes() != null ? step.getMoveTimeMinutes() : 0;

            // setup is per-batch, run is per-unit * quantity
            BigDecimal stepMinutes = new BigDecimal(setup)
                    .add(new BigDecimal(run).multiply(quantity))
                    .add(new BigDecimal(wait))
                    .add(new BigDecimal(move));
            totalMinutes = totalMinutes.add(stepMinutes);
        }

        return totalMinutes.divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate lead time for a single step (for 1 unit)
     */
    private BigDecimal calculateStepLeadTime(RoutingStep step) {
        int setup = step.getSetupTimeMinutes() != null ? step.getSetupTimeMinutes() : 0;
        int run = step.getRunTimeMinutes() != null ? step.getRunTimeMinutes() : 0;
        int wait = step.getWaitTimeMinutes() != null ? step.getWaitTimeMinutes() : 0;
        int move = step.getMoveTimeMinutes() != null ? step.getMoveTimeMinutes() : 0;

        BigDecimal totalMinutes = new BigDecimal(setup + run + wait + move);
        return totalMinutes.divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
    }
}
