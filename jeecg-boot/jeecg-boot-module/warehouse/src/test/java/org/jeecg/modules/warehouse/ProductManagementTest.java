package org.jeecg.modules.warehouse;

import org.jeecg.modules.warehouse.entity.Product;
import org.jeecg.modules.warehouse.entity.ProductCategory;
import org.jeecg.modules.warehouse.service.ProductService;
import org.jeecg.modules.warehouse.service.ProductCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description: Product Management Test
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
@SpringBootTest
public class ProductManagementTest {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductCategoryService categoryService;

    @Test
    public void testCreateProduct() {
        // Create a test product
        Product product = new Product();
        product.setCode("TEST001");
        product.setName("Test Product");
        product.setDescription("This is a test product");
        product.setPrice(new BigDecimal("99.99"));
        product.setMinStockLevel(10);
        product.setStatus(1);
        
        // Save product
        boolean result = productService.save(product);
        
        // Verify product was saved
        assertTrue(result);
        
        // Verify product can be retrieved
        Product savedProduct = productService.getById(product.getId());
        assertNotNull(savedProduct);
        assertEquals("TEST001", savedProduct.getCode());
        assertEquals("Test Product", savedProduct.getName());
    }

    @Test
    public void testUpdateProduct() {
        // Create a test product first
        Product product = new Product();
        product.setCode("TEST002");
        product.setName("Test Product 2");
        product.setPrice(new BigDecimal("199.99"));
        product.setMinStockLevel(20);
        product.setStatus(1);
        productService.save(product);
        
        // Update product
        product.setName("Updated Test Product 2");
        product.setPrice(new BigDecimal("299.99"));
        boolean result = productService.updateById(product);
        
        // Verify product was updated
        assertTrue(result);
        
        // Verify product was updated
        Product updatedProduct = productService.getById(product.getId());
        assertNotNull(updatedProduct);
        assertEquals("Updated Test Product 2", updatedProduct.getName());
        assertEquals(new BigDecimal("299.99"), updatedProduct.getPrice());
    }

    @Test
    public void testDeleteProduct() {
        // Create a test product first
        Product product = new Product();
        product.setCode("TEST003");
        product.setName("Test Product 3");
        product.setPrice(new BigDecimal("149.99"));
        product.setMinStockLevel(5);
        product.setStatus(1);
        productService.save(product);
        String productId = product.getId();
        
        // Delete product
        boolean result = productService.removeById(productId);
        
        // Verify product was deleted
        assertTrue(result);
        
        // Verify product was deleted
        Product deletedProduct = productService.getById(productId);
        assertNull(deletedProduct);
    }

    @Test
    public void testProductCodeUniqueness() {
        // Create a test product
        Product product1 = new Product();
        product1.setCode("UNIQUE001");
        product1.setName("Test Product 1");
        productService.save(product1);
        
        // Try to create another product with same code
        Product product2 = new Product();
        product2.setCode("UNIQUE001");
        product2.setName("Test Product 2");
        
        // Check if code is unique
        assertFalse(productService.isCodeUnique("UNIQUE001", null));
        
        // Check if code is unique when excluding the first product
        assertTrue(productService.isCodeUnique("UNIQUE001", product1.getId()));
    }

    @Test
    public void testCreateCategory() {
        // Create a test category
        ProductCategory category = new ProductCategory();
        category.setName("Test Category");
        category.setDescription("This is a test category");
        category.setStatus(1);
        
        // Save category
        boolean result = categoryService.save(category);
        
        // Verify category was saved
        assertTrue(result);
        
        // Verify category can be retrieved
        ProductCategory savedCategory = categoryService.getById(category.getId());
        assertNotNull(savedCategory);
        assertEquals("Test Category", savedCategory.getName());
    }

    @Test
    public void testGetCategoriesByStatus() {
        // Get active categories
        List<ProductCategory> activeCategories = categoryService.getByStatus(1);
        
        // Verify categories are returned
        assertNotNull(activeCategories);
        
        // Verify all categories have status 1
        for (ProductCategory category : activeCategories) {
            assertEquals(1, category.getStatus());
        }
    }

    @Test
    public void testGetProductsByCategory() {
        // Create a test category first
        ProductCategory category = new ProductCategory();
        category.setName("Test Category for Products");
        category.setStatus(1);
        categoryService.save(category);
        
        // Create test products in this category
        Product product1 = new Product();
        product1.setCode("CAT001");
        product1.setName("Product 1 in Category");
        product1.setCategoryId(category.getId());
        product1.setStatus(1);
        productService.save(product1);
        
        Product product2 = new Product();
        product2.setCode("CAT002");
        product2.setName("Product 2 in Category");
        product2.setCategoryId(category.getId());
        product2.setStatus(1);
        productService.save(product2);
        
        // Get products by category
        List<Product> productsInCategory = productService.getByCategoryId(category.getId());
        
        // Verify products are returned
        assertNotNull(productsInCategory);
        assertEquals(2, productsInCategory.size());
        
        // Verify all products belong to the category
        for (Product product : productsInCategory) {
            assertEquals(category.getId(), product.getCategoryId());
        }
    }

    @Test
    public void testSearchProducts() {
        // Create test products
        Product product1 = new Product();
        product1.setCode("SEARCH001");
        product1.setName("Searchable Product 1");
        product1.setStatus(1);
        productService.save(product1);
        
        Product product2 = new Product();
        product2.setCode("SEARCH002");
        product2.setName("Another Searchable Product");
        product2.setStatus(1);
        productService.save(product2);
        
        // Search products by code
        List<Product> searchResults = productService.searchProducts("SEARCH");
        
        // Verify search results
        assertNotNull(searchResults);
        assertTrue(searchResults.size() >= 2);
        
        // Verify all results contain the search keyword
        for (Product product : searchResults) {
            assertTrue(
                product.getCode().contains("SEARCH") || 
                product.getName().contains("Search")
            );
        }
    }
}