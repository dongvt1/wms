<template>
  <div :class="[prefixCls]" v-if="showMask && show">
    <div class="progress-bar-rear">
      <div class="progress-bar-front" :style="{ width: progressBarWidth }"></div>
    </div>
    <div class="value">{{ percentage }}</div>
  </div>
</template>
<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import {useDesign} from "@/hooks/web/useDesign";

  const props = defineProps({
    backColor: {
      type: [String],
      default: 'white',
    },
    processColor: {
      type: String,
      default: '#018FFB',
    },
    show: {
      type: Boolean,
      default: false,
    },
  });
  const { prefixCls } = useDesign('tinymce-process-mask');

  //Show mask
  const showMask = ref(false);
  //Progress value proportion
  const progressValue = ref<any>(0);
  //Current quantity
  const currentNum = ref(0);
  // Calculated property for calculating the width of the progress bar
  const progressBarWidth = computed(() => {
    return progressValue.value > 0 ? `${progressValue.value}px` : '0px';
  });
  // Calculate progress bar percentage
  const percentage = computed(() => {
    return `${progressValue.value}%`;
  });
  // progress color
  const frontColor = computed(() => {
    return props.processColor;
  });
  // background color
  const rearColor = computed(() => {
    return props.backColor;
  });
  function calcProcess(totalNum) {
    !showMask.value && (showMask.value = true);
    currentNum.value += 1;
    progressValue.value = ((currentNum.value / totalNum) * 100).toFixed(2);
    console.log('currentNum.value', currentNum.value);
    console.log('totalNum.value', totalNum);
    if (currentNum.value == totalNum) {
      showMask.value = false;
      currentNum.value = 0;
      progressValue.value = 0;
    }
  }
  defineExpose({
    calcProcess,
    showMask,
  });
</script>

<style lang="less">
//noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-tinymce-process-mask';

.@{prefix-cls} {

  & {
    position: absolute; /* Or use other methods such as fixed positioning */
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5); /* translucent mask */
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    z-index: 99;
  }

  .progress-bar-rear {
    width: 100px; /* progress bar width */
    height: 10px; /* progress bar height */
    background-color: v-bind(rearColor); /* progress bar color */
    border-radius: 4px;
  }

  .progress-bar-front {
    height: 10px; /* progress bar height */
    background-color: v-bind(frontColor); /* progress bar color */
    border-radius: 4px;
  }

  .value {
    color: #fff;
    margin-left: 5px;
    font-size: 16px;
    font-weight: 600;
  }
}

</style>
