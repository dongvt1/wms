<template>
  <BasicModal v-bind="$attrs" title="Modal Title" :helpMessage="['hint1', 'hint2']" width="700px">
    <p class="h-20" v-for="index in 20" :key="index"> Adapt to screen height </p>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicModal } from '/@/components/Modal';
  export default defineComponent({
    components: { BasicModal },
    setup() {
      return {};
    },
  });
</script>
