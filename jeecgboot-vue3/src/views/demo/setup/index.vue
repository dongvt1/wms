<template>
  <PageWrapper title="Boot page" content="Used to provide guidance to users">
    <a-button type="primary" @click="handleStart">start</a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { useDesign } from '/@/hooks/web/useDesign';
  import intro from 'intro.js';
  import 'intro.js/minified/introjs.min.css';

  export default defineComponent({
    components: { PageWrapper },
    setup() {
      const { prefixVar } = useDesign('');

      function handleStart() {
        intro()
          .setOptions({
            steps: [
              {
                title: 'Welcome',
                intro: 'Hello World! 👋',
              },
              {
                title: 'Collapse Button',
                element: document.querySelector(`.${prefixVar}-layout-header-trigger`)!,
                intro: 'This is the menu collapse button.',
              },
              {
                title: 'User Action',
                element: document.querySelector(`.${prefixVar}-layout-header-action`)!,
                intro: 'This is the user function area.',
              },
            ],
          })
          .start();
      }
      return { handleStart };
    },
  });
</script>
