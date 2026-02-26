<template>
  <PageWrapper title="Lazy loading custom animation example" content="Lazy loading component display animation">
    <div class="lazy-base-demo-wrap">
      <h1>scroll down</h1>

      <div class="lazy-base-demo-box">
        <LazyContainer transitionName="custom">
          <TargetContent />
        </LazyContainer>
      </div>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import TargetContent from './TargetContent.vue';
  import { LazyContainer } from '/@/components/Container/index';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { LazyContainer, TargetContent, PageWrapper },
  });
</script>
<style lang="less">
  .lazy-base-demo {
    &-wrap {
      display: flex;
      width: 50%;
      height: 2000px;
      margin: 20px auto;
      text-align: center;
      background-color: @component-background;
      justify-content: center;
      flex-direction: column;
      align-items: center;
    }

    &-box {
      width: 300px;
      height: 300px;
    }

    h1 {
      height: 1300px;
      margin: 20px 0;
    }
  }

  .custom-enter {
    opacity: 0;
    transform: scale(0.4) translate(100%);
  }

  .custom-enter-to {
    opacity: 1;
  }

  .custom-enter-active {
    position: absolute;
    top: 0;
    width: 100%;
    transition: all 0.5s;
  }

  .custom-leave {
    opacity: 1;
  }

  .custom-leave-to {
    opacity: 0;
    transform: scale(0.4) translate(-100%);
  }

  .custom-leave-active {
    transition: all 0.5s;
  }
</style>
