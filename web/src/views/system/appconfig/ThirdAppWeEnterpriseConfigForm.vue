<template>
  <div class="base-collapse">
    <div class="header"> Enterprise WeChat integration </div>
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="1">
        <template #header>
          <div style="font-size: 16px"> 1.Get docking information</div>
        </template>
        <div class="base-desc">从企业微信平台Get docking information，You can start integrating and synchronizing your address book</div>
        <div style="margin-top: 5px">
          <a href="https://help.qiaoqiaoyun.com/expand/dingding.html" target="_blank">如何Get docking information?</a>
        </div>
      </a-collapse-panel>
    </a-collapse>
    <div>
      <a-collapse expand-icon-position="right" :bordered="false">
        <a-collapse-panel key="2">
          <template #header>
            <div style="width: 100%; justify-content: space-between; display: flex">
              <div style="font-size: 16px"> 2.Input and unbind docking information</div>
            </div>
          </template>
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
            <a-button @click="weEnterpriseEditClick">edit</a-button>
            <a-button v-if="appConfigData.id" @click="cancelBindClick" danger style="margin-left: 10px">Unbind</a-button>
          </div>
        </a-collapse-panel>
      </a-collapse>
      <div class="sync-padding">
        <div style="font-size: 16px; width: 100%"> 3.Data synchronization</div>
        <div style="margin-top: 20px" class="base-desc">
          Synchronize from corporate WeChat to Knockout Cloud
          <a style="margin-left: 10px" @click="seeBindWeChat">View bound enterprise WeChat users</a>
          <div style="float: right">
            <a-button @loading="btnLoading" @click="thirdUserByWechat">synchronous</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <ThirdAppConfigModal @register="registerAppConfigModal" @success="handleSuccess" />
  <ThirdAppBindWeEnterpriseModal @register="registerBindAppConfigModal" @success="handleBindSuccess" />
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import { getThirdConfigByTenantId, deleteThirdAppConfig } from './ThirdApp.api';
  import ThirdAppConfigModal from './ThirdAppConfigModal.vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getTenantId } from '@/utils/auth';
  import ThirdAppBindWeEnterpriseModal from './ThirdAppBindWeEnterpriseModal.vue';
  import { Modal } from "ant-design-vue";

  export default defineComponent({
    name: 'ThirdAppWeEnterpriseConfigForm',
    components: {
      ThirdAppConfigModal,
      ThirdAppBindWeEnterpriseModal,
    },
    setup() {
      const btnLoading = ref<boolean>(false);
      //Third-party configuration data
      const appConfigData = ref<any>({
        agentId: '',
        clientId: '',
        clientSecret: '',
      });
      //Enterprise WeChat DingTalk configurationmodal
      const [registerAppConfigModal, { openModal }] = useModal();
      const [registerBindAppConfigModal, { openModal: openBindModal }] = useModal();
      const { createMessage } = useMessage();

      /**
       * initialization data
       *
       * @param params
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
       * 企业微信edit
       */
      async function weEnterpriseEditClick() {
        let tenantId = getTenantId();
        openModal(true, {
          tenantId: tenantId,
          thirdType: 'wechat_enterprise',
        });
      }

      /**
       * Get users bound to Enterprise WeChat
       */
      async function thirdUserByWechat() {
        openBindModal(true, { izBind: false });
      }

      /**
       * successful callback
       */
      function handleSuccess() {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'wechat_enterprise' });
      }

      /**
       * Binding success return value
       *
       * @param options
       * @param item
       */
      function handleBindSuccess(options, item) {
        console.log("options:::",options)
        console.log("item:::",item)
        if (item.success) {
          if (options != null) {
            Modal.success(options);
          } else {
            createMessage.warning(item.message);
          }
        } else {
          if (options && options.title) {
            Modal.warning(options);
          } else {
            createMessage.warning({
              content: 'synchronous失败，Please check whether the docking information is filled in correctly.，And confirm whether the enterprise WeChat configuration has been turned on！',
              duration: 5,
            });
          }
        }
      }
      
      /**
       * View the bound corporate WeChat account
       */
      function seeBindWeChat() {
        openBindModal(true,{ izBind: true })
      }

      /**
       * Unbind
       */
      function cancelBindClick() {
        if(!appConfigData.value.id){
          createMessage.warning("Please bind the enterprise WeChat application first！");
          return;
        }
        Modal.confirm({
          title: 'Unbind',
          content: 'Do you want to unbind the current organization’s enterprise WeChat application configuration?？',
          okText: 'confirm',
          cancelText: 'Cancel',
          onOk: () => {
            deleteThirdAppConfig({ id: appConfigData.value.id }, handleSuccess);
          },
        });
      }
      
      onMounted(() => {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'wechat_enterprise' });
      });

      return {
        appConfigData,
        weEnterpriseEditClick,
        registerAppConfigModal,
        registerBindAppConfigModal,
        handleSuccess,
        btnLoading,
        thirdUserByWechat,
        handleBindSuccess,
        seeBindWeChat,
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
      color: @text-color;
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
  [data-theme='dark'] .base-collapse .ant-collapse {
    border: none !important;
  }
  /*end Compatible with dark night mode*/
  /*Document button question mark style*/
  .sync-text {
    margin-left: 2px;
    cursor: pointer;
    position: relative;
    top: 2px;
  }
</style>
