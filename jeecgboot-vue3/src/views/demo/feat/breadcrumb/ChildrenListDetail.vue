<template>
  <PageWrapper title="Child details page">
    <div> Child details page内容在此 </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  export default defineComponent({ components: { PageWrapper } });
</script>
