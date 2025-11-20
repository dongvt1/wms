<template>
  <JVxeTable
    ref="vTable"
    toolbar
    rowNumber
    rowSelection
    :maxHeight="580"
    :dataSource="dataSource"
    :columns="columns"
    :linkageConfig="linkageConfig"
  />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes, JVxeColumn, JVxeLinkageConfig } from '/@/components/jeecg/JVxeTable/types';

  // Linkage configuration
  const linkageConfig = ref<JVxeLinkageConfig[]>([
    { requestData: requestMockData, key: 's1' },
    // Configurable multiple linkages
    { requestData: requestMenu, key: 'menu1' },
  ]);

  const columns = ref<JVxeColumn[]>([
    {
      title: 'gender',
      key: 'sex',
      type: JVxeTypes.select,
      dictCode: 'sex',
      width: '180px',
      placeholder: 'Please select${title}',
    },
    {
      title: 'Province/municipality/autonomous region',
      key: 's1',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Please select${title}',
      // Linkage field（That is, the next level field）
      linkageKey: 's2',
    },
    {
      title: 'city',
      key: 's2',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Please select${title}',
      // Linkage field（That is, the next level field）
      linkageKey: 's3',
    },
    {
      title: 'county/district',
      key: 's3',
      type: JVxeTypes.select,
      width: '180px',
      options: [],
      placeholder: 'Please select${title}',
    },
    {
      title: 'First level menu',
      key: 'menu1',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Please select${title}',
      // Linkage field（That is, the next level field）
      linkageKey: 'menu2',
    },
    {
      title: 'Secondary menu',
      key: 'menu2',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Please select${title}',
      // Linkage field（That is, the next level field）
      linkageKey: 'menu3',
    },
    {
      title: 'Third level menu',
      key: 'menu3',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Please select${title}',
    },
  ]);

  const dataSource = ref([
    { sex: '1', s1: '110000', s2: '110100', s3: '110101' },
    { sex: '2', s1: '130000', s2: '130300', s3: '130303' },
  ]);

  // simulated data
  const mockData = [
    { text: '北京city', value: '110000', parent: '' },
    { text: '天津city', value: '120000', parent: '' },
    { text: '河北Province', value: '130000', parent: '' },
    { text: '上海city', value: '310000', parent: '' },

    { text: '北京city', value: '110100', parent: '110000' },
    { text: '天津citycity', value: '120100', parent: '120000' },
    { text: '石家庄city', value: '130100', parent: '130000' },
    { text: '唐山city', value: '130200', parent: '130000' },
    { text: '秦皇岛city', value: '130300', parent: '130000' },
    { text: '上海city', value: '310100', parent: '310000' },

    { text: '东城district', value: '110101', parent: '110100' },
    { text: '西城district', value: '110102', parent: '110100' },
    { text: '朝阳district', value: '110105', parent: '110100' },
    { text: 'and平district', value: '120101', parent: '120100' },
    { text: '河东district', value: '120102', parent: '120100' },
    { text: '河西district', value: '120103', parent: '120100' },
    { text: '黄浦district', value: '310101', parent: '310100' },
    { text: '徐汇district', value: '310104', parent: '310100' },
    { text: '长宁district', value: '310105', parent: '310100' },
    { text: '长安district', value: '130102', parent: '130100' },
    { text: '桥西district', value: '130104', parent: '130100' },
    { text: '新华district', value: '130105', parent: '130100' },
    { text: '路南district', value: '130202', parent: '130200' },
    { text: '路北district', value: '130203', parent: '130200' },
    { text: '古冶district', value: '130204', parent: '130200' },
    { text: '海港district', value: '130302', parent: '130300' },
    { text: '山海关district', value: '130303', parent: '130300' },
    { text: '北戴河district', value: '130304', parent: '130300' },
  ];

  /** Simulate querying data from the background */
  function requestMockData(parent) {
    return new Promise((resolve) => {
      let data = mockData.filter((i) => i.parent === parent);
      setTimeout(() => resolve(data), 500);
    });
  }

  /** Query background real data */
  async function requestMenu(parent) {
    let result;
    // ifparentis empty，则查询第First level menu
    if (parent === '') {
      result = await defHttp.get({
        url: '/sys/permission/getSystemMenuList',
        params: {},
      });
    } else {
      result = await defHttp.get({
        url: '/sys/permission/getSystemSubmenu',
        params: { parentId: parent },
      });
    }
    // The returned data must contain value and text Field
    return result.map((item) => ({ value: item.id, text: item.name }));
  }
</script>
