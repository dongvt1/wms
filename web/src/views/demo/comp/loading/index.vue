<template>
  <PageWrapper v-loading="loadingRef" loading-tip="loading..." title="LoadingComponent example">
    <div ref="wrapEl">
      <a-alert message="Component approach" />
      <a-button class="my-4 mr-4" type="primary" @click="openCompFullLoading"> full screen Loading </a-button>
      <a-button class="my-4" type="primary" @click="openCompAbsolute"> inside container Loading </a-button>
      <Loading :loading="loading" :absolute="absolute" :theme="theme" :background="background" :tip="tip" />

      <a-alert message="Functional approach" />

      <a-button class="my-4 mr-4" type="primary" @click="openFnFullLoading"> full screen Loading </a-button>
      <a-button class="my-4" type="primary" @click="openFnWrapLoading"> inside container Loading </a-button>

      <a-alert message="Command mode" />
      <a-button class="my-4 mr-4" type="primary" @click="openDirectiveLoading"> open commandLoading </a-button>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs, ref } from 'vue';
  import { Loading, useLoading } from '/@/components/Loading';
  import { PageWrapper } from '/@/components/Page';
  import { Alert } from 'ant-design-vue';

  export default defineComponent({
    components: { Loading, PageWrapper, [Alert.name]: Alert },
    setup() {
      const wrapEl = ref<ElRef>(null);

      const loadingRef = ref(false);
      const compState = reactive({
        absolute: false,
        loading: false,
        theme: 'dark',
        background: 'rgba(111,111,111,.7)',
        tip: 'loading...',
      });
      const [openFullLoading, closeFullLoading] = useLoading({
        tip: 'loading...',
      });

      const [openWrapLoading, closeWrapLoading] = useLoading({
        target: wrapEl,
        props: {
          tip: 'loading...',
          absolute: true,
        },
      });

      function openLoading(absolute: boolean) {
        compState.absolute = absolute;
        compState.loading = true;
        setTimeout(() => {
          compState.loading = false;
        }, 2000);
      }

      function openCompFullLoading() {
        openLoading(false);
      }

      function openCompAbsolute() {
        openLoading(true);
      }

      function openFnFullLoading() {
        openFullLoading();

        setTimeout(() => {
          closeFullLoading();
        }, 2000);
      }

      function openFnWrapLoading() {
        openWrapLoading();

        setTimeout(() => {
          closeWrapLoading();
        }, 2000);
      }

      function openDirectiveLoading() {
        loadingRef.value = true;
        setTimeout(() => {
          loadingRef.value = false;
        }, 2000);
      }

      return {
        openCompFullLoading,
        openFnFullLoading,
        openFnWrapLoading,
        openCompAbsolute,
        wrapEl,
        loadingRef,
        openDirectiveLoading,
        ...toRefs(compState),
      };
    },
  });
</script>
