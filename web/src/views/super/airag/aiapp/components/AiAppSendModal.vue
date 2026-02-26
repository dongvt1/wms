<template>
  <div class="p-2">
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" :width="width" :title="title" :footer="null">
      <!--  Embed form    -->
      <div v-if="type === 'menu'">
        <a-form layout="vertical" :model="appData">
          <a-form-item label="Menu Name">
            <a-input v-model:value="appData.name" readonly/>
          </a-form-item>  
          <a-form-item label="Menu Address">
            <a-input v-model:value="appData.menu" readonly/>
          </a-form-item>
          <a-form-item style="text-align:right">
            <a-button @click.prevent="copyMenu">Copy Menu</a-button>
            <a-button  type="primary" style="margin-left: 10px" @click="copySql">Copy SQL</a-button>
          </a-form-item>
        </a-form>
      </div>
      <!--   Embed website   -->
      <div v-else-if="type === 'web'" class="web">
    
        <div style="display: flex;margin: 0 auto">
          <div :class="activeKey===1?'active':''" class="web-img" @click="handleImageClick(1)">
            <img  src="../img/webEmbedded.png" />
          </div>
          <div style="margin-left: 10px" :class="activeKey===2?'active':''" class="web-img" @click="handleImageClick(2)">
            <img  src="../img/iconWebEmbedded.png" />
          </div>
        </div>    
        <div class="web-title" v-if="activeKey === 1">
          Embed the following iframe into the target position on your website
        </div>  
        <div class="web-title" v-else>
          Add the following script to the body area of the webpage
        </div>
        <div class="web-code" v-if="activeKey === 1">
          <div class="web-code-title">
            <div class="web-code-desc">
              html
            </div>
            <Icon class="pointer" icon="ant-design:copy-outlined" @click="copyIframe(1)"></Icon>
          </div>
          <div class="web-code-iframe">
              <pre> {{getIframeText(1)}} </pre>
          </div>
        </div>
        
        <div class="web-code" v-if="activeKey === 2">
          <div class="web-code-title">
            <div class="web-code-desc">
              html
            </div>
            <Icon class="pointer" icon="ant-design:copy-outlined" @click="copyIframe(2)"></Icon>
          </div>
          <div class="web-code-iframe">
            <pre> {{getIframeText(2)}} </pre>
          </div>
        </div>
      </div>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { buildUUID } from '@/utils/uuid';
  import { copyTextToClipboard } from '@/hooks/web/useCopyToClipboard';
  import { isDevMode } from '/@/utils/env';

  export default {
    name: 'AiAppSendModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      //Title
      const title = ref<string>('Embed Website');
      const $message = useMessage();
      //Type
      const type = ref<string>('web');
      //Application information
      const appData = ref<any>({});
      //Modal width
      const width = ref<string>("800px");
      //Selected key
      const activeKey = ref<number>(1);
      //Register modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        type.value = data.type;
        appData.value = data.data;
        appData.value.menu = "/ai/chat/"+ data.data.id
        activeKey.value = 1;
        let minHeight = 220;
        if(data.type === 'web'){
          title.value = 'Embed Website';
          width.value = '640px';
          minHeight = 500
        }else{
          title.value = 'Configure Menu';
          width.value = '500px';
        }
        setModalProps({ height: minHeight, bodyStyle: { padding: '10px' } });
      });

      /**
       * Copy menu
       */
      function copyMenu() {
        copyText(appData.value.menu);
      }
      
      /**
       * Copy SQL
       */
      function copySql() {
        const insertMenuSql = `INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
                               VALUES ('${buildUUID()}', NULL, '${appData.value.name}', '${appData.value.menu}', '1', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', null, NULL, NULL, 0)`;
        copyText(insertMenuSql);
      }

      /**
       * Get current text
       */
      function getIframeText(value) {
        let locationUrl = document.location.protocol +"//" + window.location.host;
        //update-begin---author:wangshuai---date:2025-03-20---for:【QQYUN-11649】【AI】Application embedding, support small icon to click out chat---
        if(value === 1){
          return '<iframe\n' +
              '   src="'+locationUrl+'/ai/app/chat/'+appData.value.id+'"\n' +
              '   style="width: 100%; height: 100%;">\n' +
              '</iframe>';
        }else{
            //update-begin---author:wangshuai---date:2025-03-28---for:【QQYUN-11649】Application embedding, support small icon to click out chat---
            let path = "/src/views/super/airag/aiapp/chat/js/chat.js"
            if(!isDevMode()){
              path = "/chat/chat.js";
            }
            let text ='<script src=' + locationUrl + path +' id="e7e007dd52f67fe36365eff636bbffbd">'+'<'+'/script>';
            text += '\n <'+'script>\n';
            text += '    createAiChat({\n' +
                    '       appId:"'+ appData.value.id +'",\n';
            text += '       // Support top-left, top-right, bottom-left, bottom-right\n';
            text += '       iconPosition:"bottom-right"\n';
            text += '    })\n';
            text += ' <'+'/script>';
            return text;
            //update-end---author:wangshuai---date:2025-03-28---for:【QQYUN-11649】Application embedding, support small icon to click out chat---
        }
        //update-end---author:wangshuai---date:2025-03-20---for:【QQYUN-11649】【AI】Application embedding，Supports clicking a small icon to chat---
      }

      /**
       * Copy iframe
       */
      function copyIframe(value) {
        copyText(getIframeText(value));
      }

      // Copy text to clipboard
      function copyText(text: string) {
        const success = copyTextToClipboard(text);
        if (success) {
          $message.createMessage.success('Copy successful!');
        } else {
          $message.createMessage.error('Copy failed!');
        }
        return success;
      }

      /**
       * Image click event
       *
       * @param value
       */
      function handleImageClick(value) {
        activeKey.value = value;
      }
      
      return {
        registerModal,
        title,
        type,
        appData,
        copySql,
        copyMenu,
        width,
        copyIframe,
        getIframeText,
        activeKey,
        handleImageClick,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }

  .type-title {
    color: #1d2025;
    margin-bottom: 4px;
  }

  .type-desc {
    color: #8f959e;
    font-weight: 400;
  }
  
  .web{
   padding: 0 10px;
  }
  .web-title{
    font-size: 13px;
    font-weight: bold;
    line-height: 16px;
  }
  .web-img{
    border-width: 1.5px;
    width: 240px;
    margin-top: 20px;
    border-radius: 6px;
    img{
      border-radius: 6px;
      width: 240px;
      height: 150px;
    }
    margin-bottom: 10px;
  }
  .active{
    border-color: rgb(41 112 255);
  }
  .web-code{
    border-width: 1.5px;
    margin-top: 20px;
    background-color: #f9fafb;
    border-color: #10182814;
    width: 100%;
    border-radius: 5px;
    .web-code-title{
      width: 100%;
      padding:10px;
      background-color: #f2f4f7;
      display: inline-flex;
      justify-content: space-between;
      align-items: center;
    }
    .web-code-desc{
      color: #354052;
      font-size: 13px;
      font-weight: 500;
      line-height: 16px;
    }
    .web-code-iframe{
      padding: 15px;
      line-height: 1.5;
      font-size: 13px;
      display: grid;
      gap: 4px;
      color: #354052;
    }
  }
  .pointer{
    cursor: pointer;
  }
</style>
