import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';

const warehouse: AppRouteModule = {
  path: '/warehouse',
  name: 'Warehouse',
  component: LAYOUT,
  redirect: '/warehouse/area',
  meta: {
    orderNo: 200000,
    icon: 'ant-design:shop-outlined',
    title: 'Warehouse Management',
  },
  children: [
    {
      path: 'area',
      name: 'WarehouseArea',
      component: () => import('/@/views/warehouse/area/WarehouseAreaList.vue'),
      meta: {
        title: 'Warehouse Area Management',
      },
    },
    {
      path: 'shelf',
      name: 'WarehouseShelf',
      component: () => import('/@/views/warehouse/shelf/WarehouseShelfList.vue'),
      meta: {
        title: 'Warehouse Shelf Management',
      },
    },
    {
      path: 'customer',
      name: 'CustomerManagement',
      component: () => import('/@/views/warehouse/customer/CustomerList.vue'),
      meta: {
        title: 'Customer Management',
      },
    },
    {
      path: 'order',
      name: 'OrderManagement',
      component: () => import('/@/views/warehouse/order/OrderList.vue'),
      meta: {
        title: 'Order Management',
      },
    },
  ],
};

export default warehouse;