package org.jeecg.modules.test.seata.product.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;


import org.jeecg.modules.test.seata.product.entity.SeataProduct;
import org.jeecg.modules.test.seata.product.mapper.SeataProductMapper;
import org.jeecg.modules.test.seata.product.service.SeataProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Description: Product service category
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Service
public class SeataProductServiceImpl implements SeataProductService {

    @Resource
    private SeataProductMapper productMapper;

    /**
     * The transaction propagation property is set to REQUIRES_NEW Start a new transaction
     */
    @DS("product")
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    @Override
    public BigDecimal reduceStock(Long productId, Integer count) {
        log.info("xid:"+ RootContext.getXID());
        log.info("=============PRODUCT START=================");
        // Check inventory
        SeataProduct product = productMapper.selectById(productId);
        Assert.notNull(product, "Product does not exist");
        Integer stock = product.getStock();
        log.info("The product number is {} The inventory is{},The order quantity is{}", productId, stock, count);

        if (stock < count) {
            log.warn("The product number is{} Insufficient stock，Current inventory:{}", productId, stock);
            throw new RuntimeException("Insufficient stock");
        }
        log.info("开始扣减The product number is {} in stock,The unit price of the product is{}", productId, product.getPrice());
        // 扣减in stock
        int currentStock = stock - count;
        product.setStock(currentStock);
        productMapper.updateById(product);
        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(count));
        log.info("扣减The product number is {} in stock成功,扣减后in stock为{}, {} The total price of the items is {} ", productId, currentStock, count, totalPrice);
        log.info("=============PRODUCT END=================");
        return totalPrice;
    }
}