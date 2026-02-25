package org.jeecg.modules.warehouse;

import org.jeecg.modules.warehouse.entity.WarehouseArea;
import org.jeecg.modules.warehouse.entity.WarehouseShelf;
import org.jeecg.modules.warehouse.entity.WarehouseSlot;
import org.jeecg.modules.warehouse.service.WarehouseAreaService;
import org.jeecg.modules.warehouse.service.WarehouseShelfService;
import org.jeecg.modules.warehouse.service.WarehouseSlotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WarehouseLocationTest {

    @Autowired
    private WarehouseAreaService warehouseAreaService;

    @Autowired
    private WarehouseShelfService warehouseShelfService;

    @Autowired
    private WarehouseSlotService warehouseSlotService;

    @Test
    public void testCreateWarehouseArea() {
        WarehouseArea area = new WarehouseArea();
        area.setAreaCode("TEST001");
        area.setAreaName("Test Area");
        area.setDescription("Test Description");
        area.setStatus(1);
        area.setCapacity(10);
        area.setUsedCapacity(0);

        boolean result = warehouseAreaService.save(area);
        assertTrue(result, "Failed to create warehouse area");

        WarehouseArea savedArea = warehouseAreaService.getById(area.getId());
        assertNotNull(savedArea, "Saved area should not be null");
        assertEquals("TEST001", savedArea.getAreaCode());
        assertEquals("Test Area", savedArea.getAreaName());
    }

    @Test
    public void testCreateWarehouseShelf() {
        WarehouseShelf shelf = new WarehouseShelf();
        shelf.setAreaId("1");
        shelf.setShelfCode("SHELF_TEST");
        shelf.setShelfName("Test Shelf");
        shelf.setDescription("Test Shelf Description");
        shelf.setStatus(1);
        shelf.setCapacity(20);
        shelf.setUsedCapacity(0);

        boolean result = warehouseShelfService.save(shelf);
        assertTrue(result, "Failed to create warehouse shelf");

        WarehouseShelf savedShelf = warehouseShelfService.getById(shelf.getId());
        assertNotNull(savedShelf, "Saved shelf should not be null");
        assertEquals("SHELF_TEST", savedShelf.getShelfCode());
        assertEquals("Test Shelf", savedShelf.getShelfName());
    }

    @Test
    public void testCreateWarehouseSlot() {
        WarehouseSlot slot = new WarehouseSlot();
        slot.setShelfId("1");
        slot.setSlotCode("SLOT_TEST");
        slot.setSlotName("Test Slot");
        slot.setPosition("A1-1");
        slot.setDescription("Test Slot Description");
        slot.setStatus(0);
        slot.setCapacity(1);
        slot.setUsedCapacity(0);

        boolean result = warehouseSlotService.save(slot);
        assertTrue(result, "Failed to create warehouse slot");

        WarehouseSlot savedSlot = warehouseSlotService.getById(slot.getId());
        assertNotNull(savedSlot, "Saved slot should not be null");
        assertEquals("SLOT_TEST", savedSlot.getSlotCode());
        assertEquals("Test Slot", savedSlot.getSlotName());
    }

    @Test
    public void testAssignProductToSlot() {
        // First create a slot
        WarehouseSlot slot = new WarehouseSlot();
        slot.setShelfId("1");
        slot.setSlotCode("SLOT_ASSIGN");
        slot.setSlotName("Test Slot Assign");
        slot.setPosition("A1-2");
        slot.setStatus(0);
        slot.setCapacity(1);
        slot.setUsedCapacity(0);
        warehouseSlotService.save(slot);

        // Assign product to slot
        boolean result = warehouseSlotService.assignProduct(slot.getId(), "PROD001");
        assertTrue(result, "Failed to assign product to slot");

        // Verify assignment
        WarehouseSlot updatedSlot = warehouseSlotService.getById(slot.getId());
        assertEquals("PROD001", updatedSlot.getProductCode());
        assertEquals(1, updatedSlot.getStatus());
        assertEquals(1, updatedSlot.getUsedCapacity());
    }

    @Test
    public void testRemoveProductFromSlot() {
        // First create and assign a slot with product
        WarehouseSlot slot = new WarehouseSlot();
        slot.setShelfId("1");
        slot.setSlotCode("SLOT_REMOVE");
        slot.setSlotName("Test Slot Remove");
        slot.setPosition("A1-3");
        slot.setStatus(2);
        slot.setCapacity(1);
        slot.setUsedCapacity(1);
        slot.setProductCode("PROD002");
        warehouseSlotService.save(slot);

        // Remove product from slot
        boolean result = warehouseSlotService.removeProduct(slot.getId());
        assertTrue(result, "Failed to remove product from slot");

        // Verify removal
        WarehouseSlot updatedSlot = warehouseSlotService.getById(slot.getId());
        assertNull(updatedSlot.getProductCode());
        assertEquals(0, updatedSlot.getStatus());
        assertEquals(0, updatedSlot.getUsedCapacity());
    }

    @Test
    public void testMoveProductBetweenSlots() {
        // Create source slot with product
        WarehouseSlot fromSlot = new WarehouseSlot();
        fromSlot.setShelfId("1");
        fromSlot.setSlotCode("SLOT_FROM");
        fromSlot.setSlotName("Test Slot From");
        fromSlot.setPosition("A1-4");
        fromSlot.setStatus(2);
        fromSlot.setCapacity(1);
        fromSlot.setUsedCapacity(1);
        fromSlot.setProductCode("PROD003");
        warehouseSlotService.save(fromSlot);

        // Create destination slot
        WarehouseSlot toSlot = new WarehouseSlot();
        toSlot.setShelfId("1");
        toSlot.setSlotCode("SLOT_TO");
        toSlot.setSlotName("Test Slot To");
        toSlot.setPosition("A1-5");
        toSlot.setStatus(0);
        toSlot.setCapacity(1);
        toSlot.setUsedCapacity(0);
        warehouseSlotService.save(toSlot);

        // Move product
        boolean result = warehouseSlotService.moveProduct(fromSlot.getId(), toSlot.getId());
        assertTrue(result, "Failed to move product between slots");

        // Verify move
        WarehouseSlot updatedFromSlot = warehouseSlotService.getById(fromSlot.getId());
        WarehouseSlot updatedToSlot = warehouseSlotService.getById(toSlot.getId());

        assertNull(updatedFromSlot.getProductCode());
        assertEquals(0, updatedFromSlot.getStatus());
        assertEquals(0, updatedFromSlot.getUsedCapacity());

        assertEquals("PROD003", updatedToSlot.getProductCode());
        assertEquals(2, updatedToSlot.getStatus());
        assertEquals(1, updatedToSlot.getUsedCapacity());
    }

    @Test
    public void testGetEmptySlots() {
        List<WarehouseSlot> emptySlots = warehouseSlotService.getEmptySlots();
        assertNotNull(emptySlots, "Empty slots list should not be null");
        
        for (WarehouseSlot slot : emptySlots) {
            assertEquals(0, slot.getStatus(), "All slots should be empty");
            assertNull(slot.getProductCode(), "Product code should be null for empty slots");
        }
    }

    @Test
    public void testFindByProductCode() {
        // Create and assign a slot with product
        WarehouseSlot slot = new WarehouseSlot();
        slot.setShelfId("1");
        slot.setSlotCode("SLOT_FIND");
        slot.setSlotName("Test Slot Find");
        slot.setPosition("A1-6");
        slot.setStatus(2);
        slot.setCapacity(1);
        slot.setUsedCapacity(1);
        slot.setProductCode("PROD004");
        warehouseSlotService.save(slot);

        // Find by product code
        List<WarehouseSlot> foundSlots = warehouseSlotService.findByProductCode("PROD004");
        assertNotNull(foundSlots, "Found slots list should not be null");
        assertFalse(foundSlots.isEmpty(), "Should find at least one slot");

        boolean found = false;
        for (WarehouseSlot foundSlot : foundSlots) {
            if ("PROD004".equals(foundSlot.getProductCode())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should find the slot with the specified product code");
    }
}