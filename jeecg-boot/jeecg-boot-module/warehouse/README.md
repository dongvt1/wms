# Warehouse Location Management Module

## Overview

This module provides comprehensive warehouse location management functionality for the Warehouse Management System. It allows users to define hierarchical warehouse locations (areas, shelves, and slots) and manage product assignments to these locations.

## Features

### 1. Warehouse Area Management
- Create, read, update, and delete warehouse areas
- Track area capacity and usage
- Filter areas by status (active/inactive)

### 2. Warehouse Shelf Management
- Create, read, update, and delete warehouse shelves
- Assign shelves to specific areas
- Track shelf capacity and usage
- Filter shelves by area or status

### 3. Warehouse Slot Management
- Create, read, update, and delete warehouse slots
- Assign slots to specific shelves
- Track slot capacity and usage
- Manage product assignments to slots
- Move products between slots
- Find slots by product code
- Filter slots by status (empty/reserved/occupied)

## API Endpoints

### Warehouse Area APIs
- `GET /warehouse/area/list` - Get paginated list of areas
- `POST /warehouse/area/add` - Create a new area
- `PUT /warehouse/area/edit` - Update an existing area
- `DELETE /warehouse/area/delete` - Delete an area
- `DELETE /warehouse/area/deleteBatch` - Batch delete areas
- `GET /warehouse/area/queryById` - Get area by ID
- `GET /warehouse/area/getByStatus` - Get areas by status
- `GET /warehouse/area/exportXls` - Export areas to Excel
- `POST /warehouse/area/importExcel` - Import areas from Excel

### Warehouse Shelf APIs
- `GET /warehouse/shelf/list` - Get paginated list of shelves
- `POST /warehouse/shelf/add` - Create a new shelf
- `PUT /warehouse/shelf/edit` - Update an existing shelf
- `DELETE /warehouse/shelf/delete` - Delete a shelf
- `DELETE /warehouse/shelf/deleteBatch` - Batch delete shelves
- `GET /warehouse/shelf/queryById` - Get shelf by ID
- `GET /warehouse/shelf/getByAreaId` - Get shelves by area ID
- `GET /warehouse/shelf/getWithAreaInfo` - Get shelves with area information
- `GET /warehouse/shelf/exportXls` - Export shelves to Excel
- `POST /warehouse/shelf/importExcel` - Import shelves from Excel

### Warehouse Slot APIs
- `GET /warehouse/slot/list` - Get paginated list of slots
- `POST /warehouse/slot/add` - Create a new slot
- `PUT /warehouse/slot/edit` - Update an existing slot
- `DELETE /warehouse/slot/delete` - Delete a slot
- `DELETE /warehouse/slot/deleteBatch` - Batch delete slots
- `GET /warehouse/slot/queryById` - Get slot by ID
- `GET /warehouse/slot/getByShelfId` - Get slots by shelf ID
- `GET /warehouse/slot/getEmptySlots` - Get empty slots
- `POST /warehouse/slot/assignProduct` - Assign product to slot
- `POST /warehouse/slot/removeProduct` - Remove product from slot
- `POST /warehouse/slot/moveProduct` - Move product between slots
- `GET /warehouse/slot/findByProductCode` - Find slots by product code
- `GET /warehouse/slot/getWithShelfAndAreaInfo` - Get slots with shelf and area info
- `GET /warehouse/slot/exportXls` - Export slots to Excel
- `POST /warehouse/slot/importExcel` - Import slots from Excel

## Database Schema

### Warehouse Area Table (warehouse_area)
- `id` - Primary key
- `area_code` - Area code
- `area_name` - Area name
- `description` - Area description
- `status` - Status (0: Inactive, 1: Active)
- `capacity` - Maximum number of shelves
- `used_capacity` - Number of shelves in use

### Warehouse Shelf Table (warehouse_shelf)
- `id` - Primary key
- `area_id` - Foreign key to warehouse_area
- `shelf_code` - Shelf code
- `shelf_name` - Shelf name
- `description` - Shelf description
- `status` - Status (0: Inactive, 1: Active)
- `capacity` - Maximum number of slots
- `used_capacity` - Number of slots in use

### Warehouse Slot Table (warehouse_slot)
- `id` - Primary key
- `shelf_id` - Foreign key to warehouse_shelf
- `slot_code` - Slot code
- `slot_name` - Slot name
- `position` - Position (row, column)
- `description` - Slot description
- `status` - Status (0: Empty, 1: Reserved, 2: Occupied)
- `capacity` - Maximum number of products
- `used_capacity` - Number of products placed
- `product_code` - Code of assigned product

## Installation

1. Add the module to the parent pom.xml:
```xml
<module>jeecg-module-warehouse</module>
```

2. Execute the SQL script to create the database tables:
```sql
-- Run the warehouse_tables.sql script
```

3. Restart the application to load the new module.

## Usage

1. Define warehouse areas first
2. Create shelves within each area
3. Create slots within each shelf
4. Assign products to slots as needed
5. Use the search and filter functions to locate products
6. Use the move function to transfer products between slots

## Integration with Other Modules

This module is designed to integrate with:
- Product Management Module: To assign products to locations
- Inventory Management Module: To track stock levels by location
- Order Management Module: To reserve slots for orders
- Reporting Module: To generate location-based reports