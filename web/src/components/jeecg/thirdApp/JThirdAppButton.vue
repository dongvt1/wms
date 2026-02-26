<template>
  <template v-if="syncToApp || syncToLocal">
    <JThirdAppDropdown v-if="enabledTypes.wechatEnterprise" type="wechatEnterprise" name="Qiwei" v-bind="bindAttrs" v-on="bindEvents" />
    <JThirdAppDropdown v-if="enabledTypes.dingtalk" type="dingtalk" name="DingTalk" v-bind="bindAttrs" v-on="bindEvents" />
  </template>
  <template v-else>No sync direction set</template>
</template>

<script lang="ts" setup>
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
  import { ref, computed, createVNode, h, resolveComponent } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { backEndUrl, getEnabledTypes, doSyncThirdApp } from './jThirdApp.api';
  import { Modal, Input } from 'ant-design-vue';
  import JThirdAppDropdown from './JThirdAppDropdown.vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage, createWarningModal } = useMessage();
  const props = defineProps({
    // Sync type，can be user、depart
    bizType: {
      type: String,
      required: true,
    },
    // Whether to allow synchronization to third partiesAPP
    syncToApp: Boolean,
    // Whether to allow third partiesAPPSync to local
    syncToLocal: Boolean,
    // selected row
    selectedRowKeys: Array,
  });
  // statementEmits
  const emit = defineEmits(['sync-ok', 'sync-error', 'sync-finally']);

  const enabledTypes = ref({});
  // Binding properties
  const bindAttrs = computed(() => {
    return {
      syncToApp: props.syncToApp,
      syncToLocal: props.syncToLocal,
    };
  });
  // binding method
  const bindEvents = computed(() => {
    return {
      'to-app': onToApp,
      'to-local': onToLocal,
    };
  });

  // Sync to third partyApp
  function onToApp(e) {
    doSync(e.type, '/toApp');
  }

  // Sync to local
  function onToLocal(e) {
    doSync(e.type, '/toLocal');
  }

  // Get enabled third partiesApp
  async function loadEnabledTypes() {
    enabledTypes.value = await getEnabledTypes();
  }

  // Start syncing third partyApp
  function doSync(type, direction) {
    let urls = backEndUrl[type];
    if (!(urls && urls[props.bizType])) {
      console.warn('Configuration error');
      return;
    }
    let url = urls[props.bizType] + direction;
    let selectedRowKeys = props.selectedRowKeys;
    let content = 'Are you sure you want to start syncing all data?？may take longer！';

    if (Array.isArray(selectedRowKeys) && selectedRowKeys.length > 0) {
      content = `Are you sure you want to start syncing this ${selectedRowKeys.length} Item?？`;
    } else {
      selectedRowKeys = [];
    }
    return new Promise((resolve, reject) => {
      const model = Modal.confirm({
        icon: createVNode(ExclamationCircleOutlined),
        title: 'synchronous',
        content,
        onOk: () => {
          model.update({
            keyboard: false,
            okText: 'synchronous中…',
            cancelButtonProps: { disabled: true },
          });
          let params = { ids: selectedRowKeys.join(',') };
          return defHttp
            .get({ url, params }, { isTransformResponse: false })
            .then((res) => {
              let options = {};
              if (res.result) {
                options = {
                  width: 600,
                  title: res.message,
                  content: () => {
                    let nodes;
                    let successInfo = [`Success information is as follows：`, renderTextarea(h, res.result.successInfo.map((v, i) => `${i + 1}. ${v}`).join('\n'))];
                    if (res.success) {
                      nodes = [...successInfo, h('br'), `No failure message！`];
                    } else {
                      nodes = [
                        `The failure information is as follows：`,
                        renderTextarea(h, res.result.failInfo.map((v, i) => `${i + 1}. ${v}`).join('\n')),
                        h('br'),
                        ...successInfo,
                      ];
                    }
                    return nodes;
                  },
                };
              }
              if (res.success) {
                if (options != null) {
                  Modal.success(options);
                } else {
                  createMessage.warning(res.message);
                }
                emit('sync-ok');
              } else {
                if (options != null) {
                  Modal.warning(options);
                } else {
                  createMessage.warning(res.message);
                }
                emit('sync-error');
              }
            })
            .catch(() => model.destroy())
            .finally(() => {
              resolve();
              emit('sync-finally', {
                type,
                direction,
                isToApp: direction === '/toApp',
                isToLocal: direction === '/toLocal',
              });
            });
        },
        onCancel() {
          resolve();
        },
      });
    });
  }

  function renderTextarea(h, value) {
    return h(
      'div',
      {
        id: 'box',
        style: {
          minHeight: '100px',
          border: '1px solid #d9d9d9',
          fontSize: '14px',
          maxHeight: '250px',
          whiteSpace: 'pre',
          overflow: 'auto',
          padding: '10px',
        },
      },
      value
    );
  }

  // Get enabled third partiesApp
  loadEnabledTypes();
</script>

<style scoped>
  #box:hover {
    border-color: #40a9ff;
  }
</style>
