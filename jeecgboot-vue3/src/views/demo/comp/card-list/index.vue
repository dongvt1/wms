<template>
  <PageWrapper title="Card list example" content="Basic package">
    <CardList :params="params" :api="demoListApi" @getMethod="getMethod" @delete="handleDel">
      <template #header>
        <Button type="primary" color="error"> button1 </Button>
        <Button type="primary" color="success"> button2 </Button>
      </template>
    </CardList>
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { CardList } from '/@/components/CardList';
  import { Button } from '/@/components/Button';
  import { PageWrapper } from '/@/components/Page';
  import { demoListApi } from '/@/api/demo/table';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { notification } = useMessage();
  // askapiwith parameters
  const params = {};

  let reload = () => {};
  // get insidefetchmethod;
  function getMethod(m: any) {
    reload = m;
  }
  //删除button事件
  function handleDel(id) {
    console.log(id);
    notification.success({ message: `successfully deleted${id}` });
    reload();
  }
</script>
