<template>
  <Card title="Quick navigation" v-bind="$attrs">
    <template v-for="item in navItems" :key="item">
      <CardGrid @click="goPage">
        <span class="flex flex-col items-center">
          <Icon :icon="item.icon" :color="item.color" size="20" />
          <span class="text-md mt-2">{{ item.title }}</span>
        </span>
      </CardGrid>
    </template>
  </Card>
</template>
<script lang="ts" setup>
  import { Card } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Icon } from '/@/components/Icon';

  const CardGrid = Card.Grid;
  const $message = useMessage();
  const navItems = [
    {
      title: 'Business acceptance',
      icon: 'ion:home-outline',
      color: '#1fdaca',
    },
    {
      title: 'business management',
      icon: 'ion:grid-outline',
      color: '#bf0c2c',
    },
    {
      title: 'File management',
      icon: 'ion:layers-outline',
      color: '#e18525',
    },
    {
      title: 'Information inquiry',
      icon: 'ion:settings-outline',
      color: '#3fb27f',
    }, 
		{
				title: 'Notices and Announcements',
				icon: 'ion:notifications',
				color: '#13b0ff',
		}, 
		{
				title: 'my task',
				icon: 'ion:person-stalker',
				color: '#b27315',
		}
  ];

  function goPage() {
    $message.createMessage.success('Handle jump pages according to business!');
  }
</script>
