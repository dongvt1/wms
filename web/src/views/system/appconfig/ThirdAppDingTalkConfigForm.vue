<template>
  <div class="base-collapse">
    <div class="header"> DingTalk integration </div>
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="1">
        <template #header>
          <div style="font-size: 16px"> 1.Get docking information</div>
        </template>
        <div class="base-desc">从钉钉开放平台Get docking information，You can start integrating and synchronizing your address book</div>
        <div style="margin-top: 5px">
          <a href='https://help.qiaoqiaoyun.com/expand/dingding.html' target='_blank'>如何Get docking information?</a>
        </div>
      </a-collapse-panel>
    </a-collapse>
    <div class="sync-padding">
      <a-collapse expand-icon-position="right" :bordered="false">
        <a-collapse-panel key="2">
          <template #header>
            <div style="width: 100%; justify-content: space-between; display: flex">
              <div style="font-size: 16px"> 2.Input and unbind docking information</div>
            </div>
          </template>
          <div class="base-desc">Complete steps1back，fill inAgentld、 AppKey、AppSecretback Can be connected to applications and synchronized address book</div>
          <div class="flex-flow">
            <div class="base-title">CorpId</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.corpId" readonly />
            </div>
          </div>
          <div class="flex-flow">
            <div class="base-title">Agentld</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.agentId" readonly />
            </div>
          </div>
          <div class="flex-flow">
            <div class="base-title">AppKey</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.clientId" readonly />
            </div>
          </div>
          <div class="flex-flow">
            <div class="base-title">AppSecret</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.clientSecret" readonly />
            </div>
          </div>
          <div style="margin-top: 20px; width: 100%; text-align: right">
            <a-button @click="dingEditClick">edit</a-button>
            <a-button v-if="appConfigData.id" @click="cancelBindClick" danger style="margin-left: 10px">Unbind</a-button>
          </div>
        </a-collapse-panel>
      </a-collapse>
      <div class="sync-padding">
        <div style="font-size: 16px; width: 100%"> 3.Data synchronization</div>
        <div style="margin-top: 20px" class="base-desc">
          Synchronize from DingTalk to local
          <ul style='list-style-type: disc;margin-left: 20px;'>
            <li>Synchronize departments to local</li>
            <li>
              Synchronize users under departments to local
              <a-tooltip title='Synchronize user and department documents'>
                <a-icon @click='handleIconClick' type="question-circle" class="sync-text"/>
              </a-tooltip>
            </li>
          </ul>
          <div style="float: right">
            <a-button :loading="btnLoading" @click="syncDingTalk">{{ !btnLoading ? 'synchronous' : 'synchronous中' }}</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ThirdAppConfigModal @register="registerAppConfigModal" @success="handleSuccess" />
</template>

<script lang="ts">
  import { defineComponent, h, inject, onMounted, reactive, ref, watch } from 'vue';
  import { getThirdConfigByTenantId, syncDingTalkDepartUserToLocal, deleteThirdAppConfig } from './ThirdApp.api';
  import { useModal } from '/@/components/Modal';
  import ThirdAppConfigModal from './ThirdAppConfigModal.vue';
  import { Modal } from 'ant-design-vue';
  import { getTenantId } from '/@/utils/auth';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'OrganDingConfigForm',
    components: {
      ThirdAppConfigModal,
    },
    setup() {
      const { createMessage } = useMessage();
      //Collapse panel selectedkey
      const collapseActiveKey = ref<string>('');
      //button load event
      const btnLoading = ref<boolean>(false);
      //Third-party configuration data
      const appConfigData = ref<any>({
        agentId: undefined,
        clientId: '',
        clientSecret: '',
      });

      //Enterprise WeChat DingTalk configurationmodal
      const [registerAppConfigModal, { openModal }] = useModal();

      /**
       * 钉钉edit
       */
      async function dingEditClick() {
        let tenantId = getTenantId();
        openModal(true, {
          tenantId: tenantId,
          thirdType: 'dingtalk',
        });
      }

      /**
       * Initialize third-party data
       */
      async function initThirdAppConfigData(params) {
        let values = await getThirdConfigByTenantId(params);
        if (values) {
          appConfigData.value = values;
        } else {
          appConfigData.value = "";
        }
      }

      /**
       * successful callback
       */
      function handleSuccess() {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'dingtalk' });
      }

      /**
       * synchronous钉钉
       */
      async function syncDingTalk() {
        btnLoading.value = true;
        await syncDingTalkDepartUserToLocal()
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
            } else {
              if (options && options.title) {
                Modal.warning(options)
              } else {
                createMessage.warning({
                  content: res.message || "synchronous失败，Please check whether the docking information is filled in correctly.，And confirm whether DingTalk configuration has been turned on！",
                  duration: 5
                });
              }
            }
          })
          .finally(() => {
            btnLoading.value = false;
          });
      }

      /**
       * render text
       * @param h
       * @param value
       */
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

      /**
       * 钉钉synchronous文档
       */
      function handleIconClick(){
        window.open("https://help.qiaoqiaoyun.com/expand/dingdingsyn.html","_target")
      }

      /**
       * Unbind
       */
      function cancelBindClick() {
        if(!appConfigData.value.id){
          createMessage.warning("Please bind the DingTalk application first！");
          return;
        }
        Modal.confirm({
          title: 'Unbind',
          content: 'Do you want to unbind the DingTalk application configuration of the current organization?？',
          okText: 'confirm',
          cancelText: 'Cancel',
          onOk: () => {
            deleteThirdAppConfig({ id: appConfigData.value.id }, handleSuccess);
          },
        });
      }
      
      onMounted(() => {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'dingtalk' });
      });

      return {
        appConfigData,
        collapseActiveKey,
        registerAppConfigModal,
        dingEditClick,
        handleSuccess,
        syncDingTalk,
        btnLoading,
        handleIconClick,
        cancelBindClick,
      };
    },
  });
</script>

<style lang="less" scoped>
  .header {
    align-items: center;
    box-sizing: border-box;
    display: flex;
    height: 50px;
    justify-content: space-between;
    font-weight: 700;
    font-size: 18px;
    color: @text-color;
  }

  .flex-flow {
    display: flex;
    min-height: 0;
  }

  .sync-padding {
    padding: 12px 0 16px;
    color: @text-color;
  }

  .base-collapse {
    margin-top: 20px;
    padding: 0 24px;
    font-size: 20px;

    .base-desc {
      font-size: 14px;
    }

    .base-title {
      width: 100px;
      text-align: left;
      height: 50px;
      line-height: 50px;
    }

    .base-message {
      width: 100%;
      height: 50px;
      line-height: 50px;
    }

    :deep(.ant-collapse-header) {
      padding: 12px 0 16px;
    }

    :deep(.ant-collapse-content-box) {
      padding-left: 0;
    }
  }
  /*begin Compatible with dark night mode*/
  //The border of the card in dark mode is set tonone
  [data-theme='dark'] .base-collapse .ant-collapse{
    border: none !important;
  }
  /*end Compatible with dark night mode*/
  /*Document button question mark style*/
  .sync-text{
    margin-left: 2px;
    cursor: pointer;
    position: relative;
    top: 2px
  }
 :deep(.ant-collapse-borderless >.ant-collapse-item:last-child) {border-bottom-width:1px;}
</style>
