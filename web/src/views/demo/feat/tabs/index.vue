<template>
  <PageWrapper title="Tab operation example">
    <CollapseContainer title="Enter text in the input box below,The content will be saved when you return after switching.">
      <a-alert banner message="This operation will not affect the page title，Modify onlyTabtitle" />
      <div class="mt-2 flex flex-grow-0">
        <a-button class="mr-2" @click="setTabTitle" type="primary"> set upTabtitle </a-button>
        <a-input placeholder="Please enter" v-model:value="title" class="mr-4 w-12" />
      </div>
    </CollapseContainer>

    <CollapseContainer class="mt-4" title="Tab operations">
      <a-button class="mr-2" @click="() => closeAll()"> Close all </a-button>
      <a-button class="mr-2" @click="() => closeLeft()"> close left </a-button>
      <a-button class="mr-2" @click="() => closeRight()"> close right </a-button>
      <a-button class="mr-2" @click="() => closeOther()"> Close other </a-button>
      <a-button class="mr-2" @click="closeCurrent"> Close current </a-button>
      <a-button class="mr-2" @click="refreshPage"> refresh current </a-button>
    </CollapseContainer>

    <CollapseContainer class="mt-4" title="Tab reuse exceeds the limit and is automatically closed(Usage scenarios: dynamic routing)">
      <a-button v-for="index in 6" :key="index" class="mr-2" @click="toDetail(index)"> Open{{ index }}Details page </a-button>
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { CollapseContainer } from '/@/components/Container';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { PageWrapper } from '/@/components/Page';
  import { Input, Alert } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'TabsDemo',
    components: { CollapseContainer, PageWrapper, [Input.name]: Input, [Alert.name]: Alert },
    setup() {
      const go = useGo();
      const title = ref<string>('');
      const { closeAll, closeLeft, closeRight, closeOther, closeCurrent, refreshPage, setTitle } = useTabs();
      const { createMessage } = useMessage();
      function setTabTitle() {
        if (title.value) {
          setTitle(title.value);
        } else {
          createMessage.error('Please enter要set up的Tabtitle！');
        }
      }

      function toDetail(index: number) {
        go(`/comp/basic/tabs/detail/${index}`);
      }
      return {
        closeAll,
        closeLeft,
        closeRight,
        closeOther,
        closeCurrent,
        toDetail,
        refreshPage,
        setTabTitle,
        title,
      };
    },
  });
</script>
