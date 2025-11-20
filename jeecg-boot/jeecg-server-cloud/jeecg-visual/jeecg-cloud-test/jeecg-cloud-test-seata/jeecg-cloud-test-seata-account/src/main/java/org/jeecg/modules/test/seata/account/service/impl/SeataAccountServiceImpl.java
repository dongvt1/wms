package org.jeecg.modules.test.seata.account.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.test.seata.account.entity.SeataAccount;
import org.jeecg.modules.test.seata.account.mapper.SeataAccountMapper;
import org.jeecg.modules.test.seata.account.service.SeataAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Description: TODO
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Service
public class SeataAccountServiceImpl implements SeataAccountService {
    @Resource
    private SeataAccountMapper accountMapper;

    /**
     * The transaction propagation property is set to REQUIRES_NEW Start a new transaction
     */
    @DS("account")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    public void reduceBalance(Long userId, BigDecimal amount) {
        log.info("xid:"+ RootContext.getXID());
        log.info("=============ACCOUNT START=================");
        SeataAccount account = accountMapper.selectById(userId);
        Assert.notNull(account, "User does not exist");
        BigDecimal balance = account.getBalance();
        log.info("Order placing user{}The balance is {},The total price of the goods is{}", userId, balance, amount);

        if (balance.compareTo(amount)==-1) {
            log.warn("user {} Insufficient balance，Current balance:{}", userId, balance);
            throw new RuntimeException("Insufficient balance");
        }
        log.info("开始扣减user {} balance", userId);
        BigDecimal currentBalance = account.getBalance().subtract(amount);
        account.setBalance(currentBalance);
        accountMapper.updateById(account);
        log.info("扣减user {} balance成功,扣减后user账户The balance is{}", userId, currentBalance);
        log.info("=============ACCOUNT END=================");
    }
}
