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
  ],
};

export default warehouse;