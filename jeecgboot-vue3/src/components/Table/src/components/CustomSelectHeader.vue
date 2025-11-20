<!-- Custom select columns，Header implementation part -->
<template>
  <!-- update-begin--author:liaozhiyang---date:20231130---for：【issues/5595】BasicTablecomponentshideSelectAll: trueUnable to hide select all box -->
  <template v-if="isRadio">
    <!-- radioThere is no select all，So put an empty tag -->
    <span></span>
  </template>
  <template v-else>
    <template v-if="hideSelectAll">
      <span></span>
    </template>
    <a-checkbox :disabled="disabled" v-else :checked="checked" :indeterminate="isHalf" @update:checked="onChange" />
  </template>
  <!-- update-end--author:liaozhiyang---date:20231130---for：【issues/5595】BasicTablecomponentshideSelectAll: trueUnable to hide select all box -->
</template>
<script setup lang="ts">
  import { computed } from 'vue';

  const props = defineProps({
    isRadio: {
      type: Boolean,
      required: true,
    },
    selectedLength: {
      type: Number,
      required: true,
    },
    // Number of entries on the current page
    pageSize: {
      type: Number,
      required: true,
    },
    hideSelectAll: {
      type: Boolean,
      default: false,
    },
    // update-begin--author:liaozhiyang---date:20231016---for：【QQYUN-6774】solvecheckboxAfter disabling, select all can still check the questions.
    disabled: {
      type: Boolean,
      required: true,
    },
    // update-end--author:liaozhiyang---date:20231016---for：【QQYUN-6774】solvecheckboxAfter disabling, select all can still check the questions.
  });
  const emit = defineEmits(['select-all']);

  // Whether to select all
  const checked = computed(() => {
    if (props.isRadio) {
      return false;
    }
    return props.selectedLength > 0 && props.selectedLength >= props.pageSize;
  });

  // Is it half-selected?
  const isHalf = computed(() => {
    if (props.isRadio) {
      return false;
    }
    return props.selectedLength > 0 && props.selectedLength < props.pageSize;
  });

  function onChange(checked: boolean) {
    emit('select-all', checked);
  }
</script>

<style scoped lang="scss"></style>
