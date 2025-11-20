package org.jeecg.modules.test.seata.account.service;

import java.math.BigDecimal;

/**
 * @Description: Account interface
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
public interface SeataAccountService {
    /**
     * Deduction amount
     * @param userId user ID
     * @param amount  Deduction amount
     */
    void reduceBalance(Long userId, BigDecimal amount);
}
