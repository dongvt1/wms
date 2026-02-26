<template>
  <div class="p-4">
    <a-card :bordered="false" style="height: 100%">
      <a-tabs v-model:activeKey="activeKey" @change="tabChange">
        <a-tab-pane key="1" tab="Server information"></a-tab-pane>
        <a-tab-pane key="2" tab="JVMinformation" force-render></a-tab-pane>
         <a-tab-pane key="3" tab="Tomcatinformation"></a-tab-pane> 
<!--        <a-tab-pane key="6" tab="Undertowinformation"></a-tab-pane>-->
        <a-tab-pane key="4" tab="Disk monitoring">
          <DiskInfo v-if="activeKey == 4" style="height: 100%"></DiskInfo>
        </a-tab-pane>
        <a-tab-pane key="5" tab="内存information" />
      </a-tabs>
      <!--  update-begin---author:wangshuai ---date: 20230829 for：性能监控switch到Disk monitoring再切回来报错列为空，The ____ does not workifjudge------------>
      <BasicTable @register="registerTable" :searchInfo="searchInfo" :dataSource="dataSource" v-show="activeKey != 4">
      <!--  update-end---author:wangshuai ---date: 20230829 for：性能监控switch到Disk monitoring再切回来报错列为空，The ____ does not workifjudge------------>
        <template #tableTitle>
          <div slot="message"
            >Last updated：{{ lastUpdateTime }}
            <a-divider type="vertical" />
            <a @click="handleUpdate">Update now</a></div
          >
        </template>
        <template #param="{ record, text }">
          <a-tag :color="textInfo[record.param].color">{{ text }}</a-tag>
        </template>
        <template #text="{ record }">
          {{ textInfo[record.param].text }}
        </template>
        <template #value="{ record, text }"> {{ text }} {{ textInfo[record.param].unit }} </template>
      </BasicTable>
    </a-card>
  </div>
</template>
<script lang="ts" name="monitor-server" setup>
  import { onMounted, ref, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import DiskInfo from '../disk/DiskInfo.vue';
  import { getServerInfo, getTextInfo, getMoreInfo } from './server.api';
  import { columns } from './server.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import dayjs from 'dayjs';

  const dataSource = ref([]);
  const activeKey = ref('1');
  const moreInfo = ref({});
  const lastUpdateTime = ref({});
  let textInfo = ref({});
  const { createMessage } = useMessage();
  const checkedKeys = ref<Array<string | number>>([]);

  const searchInfo = { logType: '1' };
  const [registerTable, { reload }] = useTable({
    columns,
    showIndexColumn: false,
    bordered: true,
    pagination: false,
    canResize: false,
    tableSetting: { fullScreen: true },
    rowKey: 'id',
  });

  //tabswitch
  function tabChange(key) {
    if (key != 4) {
      getInfoList(key);
    }
  }

  //加载information
  function getInfoList(infoType) {
    lastUpdateTime.value = dayjs().format('YYYYYearMMmoonDDday HHhourmmpointssSecond');
    getServerInfo(infoType).then((res) => {
      textInfo.value = getTextInfo(infoType);
      moreInfo.value = getMoreInfo(infoType);
      let info = [];
      if (infoType === '5') {
        for (let param in res[0].result) {
          let data = res[0].result[param];
          let val = convert(data, unref(textInfo)[param].valueType);
          info.push({ id: param, param, text: 'false value', value: val });
        }
      } else {
        res.forEach((value, id) => {
          let more = unref(moreInfo)[value.name];
          if (!(more instanceof Array)) {
            more = [''];
          }
          more.forEach((item, idx) => {
            let param = value.name + item;
            let val = convert(value.measurements[idx].value, unref(textInfo)[param].valueType);
            info.push({ id: param + id, param, text: 'false value', value: val });
          });
        });
      }
      dataSource.value = info;
    });
  }

  function handleUpdate() {
    getInfoList(activeKey.value);
  }

  //Unit conversion
  function convert(value, type) {
    if (type === 'Number') {
      return Number(value * 100).toFixed(2);
    } else if (type === 'Date') {
      return dayjs(value * 1000).format('YYYY-MM-DD HH:mm:ss');
    } else if (type === 'RAM') {
      return Number(value / 1048576).toFixed(3);
    }
    return value;
  }

  onMounted(() => {
    getInfoList(activeKey.value);
  });
</script>
