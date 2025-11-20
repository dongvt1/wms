<template>
  <BasicModal v-bind="$attrs" destroyOnClose @register="register" title="Modal Title" :helpMessage="['hint1', 'hint2']" @visible-change="handleShow">
    <template #insertFooter>
      <a-button type="primary" danger @click="setLines" :disabled="loading">Click me to update content</a-button>
    </template>
    <template v-if="loading">
      <div class="empty-tips"> loading，Hold on3Second…… </div>
    </template>
    <template v-if="!loading">
      <ul>
        <li v-for="index in lines" :key="index">Loading completed{{ index }}！</li>
      </ul>
    </template>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  export default defineComponent({
    components: { BasicModal },
    setup() {
      const loading = ref(true);
      const lines = ref(10);
      const [register, { setModalProps, redoModalHeight }] = useModalInner();

      watch(
        () => lines.value,
        () => {
          redoModalHeight();
        }
      );

      function handleShow(visible: boolean) {
        if (visible) {
          loading.value = true;
          setModalProps({ loading: true, confirmLoading: true });
          setTimeout(() => {
            lines.value = Math.round(Math.random() * 30 + 5);
            loading.value = false;
            setModalProps({ loading: false, confirmLoading: false });
          }, 3000);
        }
      }

      function setLines() {
        lines.value = Math.round(Math.random() * 20 + 10);
      }
      return { register, loading, handleShow, lines, setLines };
    },
  });
</script>
<style scoped>
  .empty-tips {
    height: 100px;
    line-height: 100px;
    text-align: center;
  }
</style>
