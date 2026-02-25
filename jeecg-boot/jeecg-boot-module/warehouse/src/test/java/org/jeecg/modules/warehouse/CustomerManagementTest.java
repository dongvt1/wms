package org.jeecg.modules.warehouse;

import org.jeecg.modules.warehouse.entity.Customer;
import org.jeecg.modules.warehouse.entity.CustomerBalance;
import org.jeecg.modules.warehouse.service.CustomerService;
import org.jeecg.modules.warehouse.service.CustomerBalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Customer Management Integration Tests
 * 
 * @author BMad
 * @date 2025-11-21
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CustomerManagementTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerBalanceService customerBalanceService;

    @Test
    public void testCreateCustomer() {
        // Create a new customer
        Customer customer = new Customer();
        customer.setCustomerCode("TEST001");
        customer.setCustomerName("Test Customer");
        customer.setContactPerson("John Doe");
        customer.setPhone("1234567890");
        customer.setEmail("test@example.com");
        customer.setAddress("123 Test Street");
        customer.setTaxCode("TAX001");
        customer.setStatus(1);

        // Save customer
        boolean saved = customerService.save(customer);
        assertTrue(saved, "Customer should be saved successfully");
        assertNotNull(customer.getId(), "Customer ID should be generated");

        // Verify customer balance is created automatically
        CustomerBalance balance = customerBalanceService.getByCustomerId(customer.getId());
        assertNotNull(balance, "Customer balance should be created automatically");
        assertEquals(BigDecimal.ZERO, balance.getBalance(), "Initial balance should be zero");
    }

    @Test
    public void testGetCustomerByCode() {
        // Create a customer first
        Customer customer = new Customer();
        customer.setCustomerCode("TEST002");
        customer.setCustomerName("Test Customer 2");
        customer.setStatus(1);
        customerService.save(customer);

        // Get customer by code
        Customer foundCustomer = customerService.getByCode("TEST002");
        assertNotNull(foundCustomer, "Customer should be found by code");
        assertEquals("Test Customer 2", foundCustomer.getCustomerName(), "Customer name should match");
    }

    @Test
    public void testUpdateCustomerBalance() {
        // Create a customer first
        Customer customer = new Customer();
        customer.setCustomerCode("TEST003");
        customer.setCustomerName("Test Customer 3");
        customer.setStatus(1);
        customerService.save(customer);

        // Update customer balance
        boolean updated = customerBalanceService.updateBalance(customer.getId(), new BigDecimal("100.00"), "test_user");
        assertTrue(updated, "Customer balance should be updated successfully");

        // Verify balance
        CustomerBalance balance = customerBalanceService.getByCustomerId(customer.getId());
        assertEquals(new BigDecimal("100.00"), balance.getBalance(), "Balance should be updated to 100.00");
    }

    @Test
    public void testSearchCustomers() {
        // Create test customers
        Customer customer1 = new Customer();
        customer1.setCustomerCode("SEARCH001");
        customer1.setCustomerName("Search Customer 1");
        customer1.setStatus(1);
        customerService.save(customer1);

        Customer customer2 = new Customer();
        customer2.setCustomerCode("SEARCH002");
        customer2.setCustomerName("Search Customer 2");
        customer2.setStatus(1);
        customerService.save(customer2);

        // Search customers
        List<Customer> searchResults = customerService.searchCustomers("Search");
        assertEquals(2, searchResults.size(), "Should find 2 customers with 'Search' in name or code");

        // Search by code
        List<Customer> codeResults = customerService.searchCustomers("SEARCH001");
        assertEquals(1, codeResults.size(), "Should find 1 customer with code 'SEARCH001'");
    }

    @Test
    public void testGetActiveCustomers() {
        // Create active and inactive customers
        Customer activeCustomer = new Customer();
        activeCustomer.setCustomerCode("ACTIVE001");
        activeCustomer.setCustomerName("Active Customer");
        activeCustomer.setStatus(1);
        customerService.save(activeCustomer);

        Customer inactiveCustomer = new Customer();
        inactiveCustomer.setCustomerCode("INACTIVE001");
        inactiveCustomer.setCustomerName("Inactive Customer");
        inactiveCustomer.setStatus(0);
        customerService.save(inactiveCustomer);

        // Get active customers
        List<Customer> activeCustomers = customerService.getActiveCustomers();
        assertTrue(activeCustomers.stream().anyMatch(c -> "ACTIVE001".equals(c.getCustomerCode())), 
                  "Active customer should be in the list");
        assertFalse(activeCustomers.stream().anyMatch(c -> "INACTIVE001".equals(c.getCustomerCode())), 
                  "Inactive customer should not be in the list");
    }

    @Test
    public void testCustomerCodeUniqueness() {
        // Create first customer
        Customer customer1 = new Customer();
        customer1.setCustomerCode("UNIQUE001");
        customer1.setCustomerName("Unique Customer 1");
        customer1.setStatus(1);
        customerService.save(customer1);

        // Try to create second customer with same code
        Customer customer2 = new Customer();
        customer2.setCustomerCode("UNIQUE001");
        customer2.setCustomerName("Unique Customer 2");
        customer2.setStatus(1);

        // Check uniqueness
        boolean isUnique = customerService.isCodeUnique("UNIQUE001", null);
        assertFalse(isUnique, "Customer code should not be unique when already exists");

        // Check uniqueness excluding first customer
        boolean isUniqueExcluding = customerService.isCodeUnique("UNIQUE001", customer1.getId());
        assertTrue(isUniqueExcluding, "Customer code should be unique when excluding existing customer");
    }
}