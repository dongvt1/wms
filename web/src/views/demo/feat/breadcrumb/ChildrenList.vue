<template>
  <PageWrapper title="Hierarchical breadcrumbs example" content="Breadcrumbs for sub-level pages will be added after the current level">
    <router-link to="/feat/breadcrumb/children/childrenDetail"> Enter the child details page </router-link>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { PageWrapper },
  });
</script>
