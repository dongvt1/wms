<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" :width="1000" :canFullscreen="false" :showOkBtn="false">
    <BasicTable @register="registerTable" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable } from '/@/components/Table';
  import { historyColumns } from './product.data';
  import { productApi } from './product.api';

  const emit = defineEmits(['register', 'success']);
  const productId = ref('');
  const productName = ref('');

  // Table configuration
  const [registerTable, { reload, setProps }] = useTable({
    title: 'Product Change History',
    columns: historyColumns,
    bordered: true,
    showIndexColumn: true,
    pagination: true,
    canResize: false,
  });

  // Modal configuration
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    
    if (data) {
      productId.value = data.productId;
      productName.value = data.productName;
      
      // Load history data
      await loadHistory();
    }
  });

  // Set title
  const getTitle = computed(() => `History: ${productName.value || 'Product'}`);

  // Load product history
  async function loadHistory() {
    if (!productId.value) return;
    
    try {
      setProps({
        loading: true,
      });
      
      const result = await productApi.getHistory({ productId: productId.value });
      
      setProps({
        dataSource: result.records,
        loading: false,
        pagination: {
          total: result.total,
          current: result.current,
          pageSize: result.size,
        },
      });
    } catch (error) {
      console.error('Failed to load product history:', error);
      setProps({
        loading: false,
      });
    }
  }
</script>