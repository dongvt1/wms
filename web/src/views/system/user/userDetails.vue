<template>
  <Description @register="register" class="mt-4" />
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Description, DescItem, useDescription } from '/@/components/Description/index';
  const mockData = {
    username: 'test',
    nickName: 'VB',
    age: '123',
    phone: '15695909xxx',
    email: '190848757@qq.com',
    addr: 'Siming District, Xiamen City',
    sex: 'male',
    certy: '3504256199xxxxxxxxx',
    tag: 'orange',
  };
  const schema: DescItem[] = [
    {
      field: 'username',
      label: 'username',
    },
    {
      field: 'nickName',
      label: 'Nick name',
      render: (curVal, data) => {
        return `${data.username}-${curVal}`;
      },
    },
    {
      field: 'phone',
      label: 'Contact number',
    },
    {
      field: 'email',
      label: 'Mail',
    },
    {
      field: 'addr',
      label: 'address',
    },
  ];
  export default defineComponent({
    components: { Description },
    setup() {
      const [register] = useDescription({
        title: 'useDescription',
        data: mockData,
        schema: schema,
      });
      return { mockData, schema, register };
    },
  });
</script>
