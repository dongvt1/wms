<template>
  <PageWrapper title="Flat breadcrumbs example" content="Breadcrumbs for sub-level pages will overwrite the current level">
    <router-link to="/feat/breadcrumb/flatDetail"> Enter the level details page </router-link>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { PageWrapper },
  });
</script>
