<template>
  <PageWrapper title="TabDetails page">
    <div>{{ index }} - Details page content is here</div>
  </PageWrapper>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useRoute } from 'vue-router';

  export default defineComponent({
    name: 'TabDetail',
    components: { PageWrapper },
    setup() {
      const route = useRoute();
      const index = route.params?.id ?? -1;
      const { setTitle } = useTabs();

      // Set identity
      setTitle(`No.${index} - Detailed information`);
      return {
        index,
      };
    },
  });
</script>
