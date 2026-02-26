<template>
  <div class="chatWrap">
    <div class="content">
      <div class="header-title" v-if="type === 'view' && headerTitle">
        {{headerTitle}}
        <div v-if="showAdvertising" class="header-advertisint">
          AI Customer Service provided by
          <a style="color: #4183c4;margin-left: 2px;margin-right: 2px" href="https://www.qiaoqiaoyun.com/aiCustomerService" target="_blank">
            QiaoQiaoYun
          </a>
        </div>
      </div>
      <div class="main">
        <div id="scrollRef" ref="scrollRef" class="scrollArea">
          <template v-if="chatData.length>0">
            <div class="chatContentArea">
              <chatMessage
                v-for="(item, index) of chatData"
                :key="index"
                :date-time="item.dateTime || item.datetime"
                :text="item.content"
                :inversion="item.inversion || item.role"
                :error="item.error"
                :loading="item.loading"
                :appData="appData"
                :presetQuestion="item.presetQuestion"
                :images = "item.images"
                :retrievalText="item.retrievalText"
                :referenceKnowledge="item.referenceKnowledge"
                @send="handleOutQuestion"
              ></chatMessage>
            </div>
          </template>
        </div>
      </div>
      <div class="footer">
        <div class="topArea">
          <presetQuestion @outQuestion="handleOutQuestion" :quickCommandData="quickCommandData"></presetQuestion>
        </div>
        <div class="bottomArea">
          <a-button type="text" class="delBtn" @click="handleDelSession()">
            <svg
              t="1706504908534"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="1584"
              width="18"
              height="18"
            >
              <path
                d="M816.872727 158.254545h-181.527272V139.636364c0-39.563636-30.254545-69.818182-69.818182-69.818182h-107.054546c-39.563636 0-69.818182 30.254545-69.818182 69.818182v18.618181H207.127273c-48.872727 0-90.763636 41.890909-90.763637 93.09091s41.890909 90.763636 90.763637 90.763636h609.745454c51.2 0 90.763636-41.890909 90.763637-90.763636 0-51.2-41.890909-93.090909-90.763637-93.09091zM435.2 139.636364c0-13.963636 9.309091-23.272727 23.272727-23.272728h107.054546c13.963636 0 23.272727 9.309091 23.272727 23.272728v18.618181h-153.6V139.636364z m381.672727 155.927272H207.127273c-25.6 0-44.218182-20.945455-44.218182-44.218181 0-25.6 20.945455-44.218182 44.218182-44.218182h609.745454c25.6 0 44.218182 20.945455 44.218182 44.218182 0 23.272727-20.945455 44.218182-44.218182 44.218181zM835.490909 407.272727h-121.018182c-13.963636 0-23.272727 9.309091-23.272727 23.272728s9.309091 23.272727 23.272727 23.272727h97.745455V837.818182c0 39.563636-30.254545 69.818182-69.818182 69.818182h-37.236364V602.763636c0-13.963636-9.309091-23.272727-23.272727-23.272727s-23.272727 9.309091-23.272727 23.272727V907.636364h-118.690909V602.763636c0-13.963636-9.309091-23.272727-23.272728-23.272727s-23.272727 9.309091-23.272727 23.272727V907.636364H372.363636V602.763636c0-13.963636-9.309091-23.272727-23.272727-23.272727s-23.272727 9.309091-23.272727 23.272727V907.636364h-34.909091c-39.563636 0-69.818182-30.254545-69.818182-69.818182V453.818182H558.545455c13.963636 0 23.272727-9.309091 23.272727-23.272727s-9.309091-23.272727-23.272727-23.272728H197.818182c-13.963636 0-23.272727 9.309091-23.272727 23.272728V837.818182c0 65.163636 51.2 116.363636 116.363636 116.363636h451.490909c65.163636 0 116.363636-51.2 116.363636-116.363636V430.545455c0-13.963636-11.636364-23.272727-23.272727-23.272728z"
                fill="currentColor"
                p-id="1585"
              ></path>
            </svg>
          </a-button>
          <a-button v-if="type === 'view'" type="text" class="contextBtn" :class="[usingContext && 'enabled']" @click="handleUsingContext">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              xmlns:xlink="http://www.w3.org/1999/xlink"
              aria-hidden="true"
              role="img"
              class="iconify iconify--ri"
              width="20"
              height="20"
              viewBox="0 0 24 24"
            >
              <path
                fill="currentColor"
                d="M12 2c5.523 0 10 4.477 10 10s-4.477 10-10 10a9.956 9.956 0 0 1-4.708-1.175L2 22l1.176-5.29A9.956 9.956 0 0 1 2 12C2 6.477 6.477 2 12 2m0 2a8 8 0 0 0-8 8c0 1.335.326 2.618.94 3.766l.35.654l-.656 2.946l2.948-.654l.653.349A7.955 7.955 0 0 0 12 20a8 8 0 1 0 0-16m1 3v5h4v2h-6V7z"
              ></path>
            </svg>
          </a-button>
          <div class="chat-textarea" :class="textareaActive?'textarea-active':''">
            <div class="textarea-top" v-if="uploadUrlList && uploadUrlList.length>0">
              <div v-for="(item,index) in uploadUrlList" class="top-image" :key="index">
                <img :src="getImage(item)" @click="handlePreview(item)"/>
                <div class="upload-icon" @click="deleteImage(index)">
                  <Icon icon="ant-design:close-outlined" size="12px"></Icon>
                </div>
              </div>
            </div>
            <div class="textarea-bottom">
              <a-textarea
                  ref="inputRef"
                  v-model:value="prompt"
                  :autoSize="{ minRows: 1, maxRows: 6 }"
                  :placeholder="placeholder"
                  @pressEnter="handleEnter"
                  @focus="textareaActive = true"
                  @blur="textareaActive = false"
                  autofocus
                  :readonly="loading"
                  style="border-color: #ffffff !important;box-shadow:none"
                  @paste="paste"
              >
              </a-textarea>
              <a-button v-if="loading" type="primary" danger @click="handleStopChat" class="stopBtn">
                <svg
                    t="1706148514627"
                    class="icon"
                    viewBox="0 0 1024 1024"
                    version="1.1"
                    xmlns="http://www.w3.org/2000/svg"
                    p-id="5214"
                    width="18"
                    height="18"
                >
                  <path
                      d="M512 967.111111c-250.311111 0-455.111111-204.8-455.111111-455.111111s204.8-455.111111 455.111111-455.111111 455.111111 204.8 455.111111 455.111111-204.8 455.111111-455.111111 455.111111z m0-56.888889c221.866667 0 398.222222-176.355556 398.222222-398.222222s-176.355556-398.222222-398.222222-398.222222-398.222222 176.355556-398.222222 398.222222 176.355556 398.222222 398.222222 398.222222z"
                      fill="currentColor"
                      p-id="5215"
                  ></path>
                  <path d="M341.333333 341.333333h341.333334v341.333334H341.333333z" fill="currentColor" p-id="5216"></path>
                </svg>
              </a-button>
              <a-upload
                  accept=".jpg,.jpeg,.png"
                  v-if="!loading"
                  name="file"
                  v-model:file-list="fileInfoList"
                  :showUploadList="false"
                  :headers="headers"
                  :beforeUpload="beforeUpload"
                  @change="handleChange"
                  :multiple="true"
                  :action="uploadUrl"
                  :max-count="3"
              >
                <a-tooltip title="Image upload, supports jpg/jpeg/png">
                  <a-button class="sendBtn" type="text">
                    <Icon icon="ant-design:picture-outlined" style="color: #3d4353"></Icon>
                  </a-button>
                </a-tooltip>
              </a-upload>
              <a-divider v-if="!loading" type="vertical" style="border-color:#38374314"></a-divider>
              <a-button
                  @click="
              () => {
                handleSubmit();
              }
            "
                  :disabled="!prompt"
                  class="sendBtn"
                  type="text"
                  v-if="!loading"
              >
                <svg
                    t="1706147858151"
                    class="icon"
                    viewBox="0 0 1024 1024"
                    version="1.1"
                    xmlns="http://www.w3.org/2000/svg"
                    p-id="4237"
                    width="1em"
                    height="1em"
                >
                  <path
                      d="M865.28 202.5472c-17.1008-15.2576-41.0624-19.6608-62.5664-11.5712L177.7664 427.1104c-23.2448 8.8064-38.5024 29.696-39.6288 54.5792-1.1264 24.8832 11.9808 47.104 34.4064 58.0608l97.5872 47.7184c4.5056 2.2528 8.0896 6.0416 9.9328 10.6496l65.4336 161.1776c7.7824 19.1488 24.4736 32.9728 44.7488 37.0688 20.2752 4.096 41.0624-2.1504 55.6032-16.7936l36.352-36.352c6.4512-6.4512 16.5888-7.8848 24.576-3.3792l156.5696 88.8832c9.4208 5.3248 19.8656 8.0896 30.3104 8.0896 8.192 0 16.4864-1.6384 24.2688-5.0176 17.8176-7.68 30.72-22.8352 35.4304-41.6768l130.7648-527.1552c5.5296-22.016-1.7408-45.2608-18.8416-60.416z m-20.8896 50.7904L713.5232 780.4928c-1.536 6.2464-5.8368 11.3664-11.776 13.9264s-12.5952 2.1504-18.2272-1.024L526.9504 704.512c-9.4208-5.3248-19.8656-7.9872-30.208-7.9872-15.9744 0-31.744 6.144-43.52 17.92l-36.352 36.352c-3.8912 3.8912-8.9088 5.9392-14.2336 6.0416l55.6032-152.1664c0.512-1.3312 1.2288-2.56 2.2528-3.6864l240.3328-246.1696c8.2944-8.4992-2.048-21.9136-12.3904-16.0768L301.6704 559.8208c-4.096-3.584-8.704-6.656-13.6192-9.1136L190.464 502.9888c-11.264-5.5296-11.5712-16.1792-11.4688-19.3536 0.1024-3.1744 1.536-13.824 13.2096-18.2272L817.152 229.2736c10.4448-3.9936 18.0224 1.3312 20.8896 3.8912 2.8672 2.4576 9.0112 9.3184 6.3488 20.1728z"
                      p-id="4238"
                      fill="currentColor"
                  ></path>
                </svg>
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Ref, watch } from 'vue';
  import { computed, ref, createVNode, onUnmounted, onMounted } from 'vue';
  import { useScroll } from './js/useScroll';
  import chatMessage from './chatMessage.vue';
  import presetQuestion from './presetQuestion.vue';
  import { DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue';
  import { message, Modal, Tabs } from 'ant-design-vue';
  import './style/github-markdown.less';
  import './style/highlight.less';
  import './style/github-markdown.less';
  import dayjs from 'dayjs';
  import { defHttp } from '@/utils/http/axios';
  import { cloneDeep } from "lodash-es";
  import {getFileAccessHttpUrl, getHeaders} from "@/utils/common/compUtils";
  import { createImgPreview } from "@/components/Preview";
  import { useAppInject } from "@/hooks/web/useAppInject";
  import { useGlobSetting } from "@/hooks/setting";

  message.config({
    prefixCls: 'ai-chat-message',
  });

  const props = defineProps(['uuid', 'prologue', 'formState', 'url', 'type','historyData','chatTitle','presetQuestion','quickCommandData','showAdvertising']);
  const emit = defineEmits(['save','reload-message-title']);
  const { scrollRef, scrollToBottom } = useScroll();
  const prompt = ref<string>('');
  const loading = ref<boolean>(false);
  const inputRef = ref<Ref | null>(null);
  const headerTitle = ref<string>(props.chatTitle);

  //Chat data
  const chatData = ref<any>([]);
  //Application data
  const appData = ref<any>({});
  const usingContext = ref<any>(true);
  const uuid = ref<string>(props.uuid);
  const topicId = ref<string>('');
  //Request ID
  const requestId = ref<string>('');
  const { getIsMobile } = useAppInject();
  const conversationList = computed(() => chatData.value.filter((item) => item.inversion != 'user' && !!item.conversationOptions));
  const placeholder = computed(() => {
    if(getIsMobile.value){
      return 'Tell me something...'
    } else {
      return 'Tell me something...（Shift + Enter = newline）';
    }
  });
  //Token
  const headers = getHeaders();
  //Textarea click event
  const textareaActive = ref<boolean>(false);

  const globSetting = useGlobSetting();
  const baseUploadUrl = globSetting.uploadUrl;
  const uploadUrl = ref<string>(`${baseUploadUrl}/airag/chat/upload`);
  
  function handleEnter(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSubmit();
    }
  }
  function handleSubmit() {
    let message = prompt.value;
    if (!message || message.trim() === '') return;
    prompt.value = '';
    onConversation(message);
  }
  const handleOutQuestion = (message) => {
    onConversation(message);
  };
  async function onConversation(message) {
    if(!props.type && props.type != 'view'){
      if(appData.value.type && appData.value.type == 'chatSimple' && !appData.value.modelId) {
        messageTip("Please select AI model");
        return;
      }
      if(appData.value.type && appData.value.type == 'chatFLow' && !appData.value.flowId) {
        messageTip("Please select associated flow");
        return;
      }
      if(!appData.value.name) {
        messageTip("Please fill in application name");
        return;
      }
    }
    if (loading.value) return;
    loading.value = true;

    addChat(uuid.value, {
      dateTime: new Date().toLocaleString(),
      content: message,
      images:uploadUrlList.value?uploadUrlList.value:[],
      inversion: 'user',
      error: false,
      conversationOptions: null,
      requestOptions: { prompt: message, options: null },
    });
    scrollToBottom();

    let options: any = {};
    const lastContext = conversationList.value[conversationList.value.length - 1]?.conversationOptions;
    if (lastContext && usingContext.value) {
      options = { ...lastContext };
    }

    addChat(uuid.value, {
      dateTime: new Date().toLocaleString(),
      content: 'Thinking...',
      loading: true,
      inversion: 'ai',
      error: false,
      conversationOptions: null,
      requestOptions: { prompt: message, options: { ...options } },
      referenceKnowledge: [],
    });

    scrollToBottom();

    //Send message
    sendMessage(message,options);
  }

  onUnmounted(() => {
    updateChatSome(uuid.value, chatData.value.length - 1, { loading: false });
  });

  const addChat = (uuid, data) => {
    chatData.value.push({ ...data });
  };
  const updateChat = async (uuid, index, data) => {
    chatData.value.splice(index, 1, data);
    await scrollToBottom();
  };
  /**
   * Pin opening remarks
   * @param txt
   */
  const topChat = (txt) => {
    let data = {
      content: txt,
      key: 'prologue',
      loading: false,
      dateTime: dayjs().format('YYYY/MM/DD HH:mm:ss'),
      inversion: 'ai',
      presetQuestion: props.presetQuestion ? JSON.parse(props.presetQuestion) : "",
    };
    if (chatData.value && chatData.value.length > 0) {
      let key = chatData.value[0].key;
      if (key === 'prologue') {
        chatData.value[0] = { ...data };
        return;
      }
    }
    chatData.value.unshift({ ...data });
  };
  const updateChatSome = (uuid, index, data) => {
    chatData.value[index] = { ...chatData.value[index], ...data };
  };
  const updateChatFail = (uuid, index, data) => {
    updateChat(uuid.value, chatData.value.length - 1, {
      dateTime: new Date().toLocaleString(),
      content: data,
      inversion: 'ai',
      error: true,
      loading: true,
      conversationOptions: null,
      requestOptions: null,
    });
    scrollToBottom();
  };

  /**
   * Clear conversation
   * @param id
   */
  function handleDelSession (){
    Modal.confirm({
      title: 'Clear Conversation',
      icon: createVNode(ExclamationCircleOutlined),
      content: 'Are you sure to clear the conversation?',
      closable: true,
      okText: 'Confirm',
      cancelText: 'Cancel',
      wrapClassName:'ai-chat-modal',
      async onOk() {
        try {
          defHttp.get({
            url: '/airag/chat/messages/clear/' + uuid.value,
          },{ isTransformResponse: false }).then((res) => {
            if(res.success){
              chatData.value = [];
              topicId.value = "";
              if(props.prologue){
                topChat(props.prologue);
              }
            }
          })
        } catch {
          return console.log('Oops errors!');
        }
      },
    });
  };

  // Stop response
  const handleStop = () => {
    console.log('ai chat：：：---stopped responding');
    if (loading.value) {
      loading.value = false;
    }
    updateChatSome(uuid, chatData.value.length - 1, { loading: false });
  };

  handleStop();

  const knowList = ref<Recordable[]>([])

  /**
   * Stop message
   */
  function handleStopChat() {
    //update-begin---author:wangshuai---date:2025-06-03---for:【issues/8338】AI应用chat回复stopinvalid，Replies will still be output---
    const currentRequestId = requestId.value
    if(currentRequestId){
      try{
        //调用后端接口stopped responding
        defHttp.get({
          url: '/airag/chat/stop/' + currentRequestId,
        },{ isTransformResponse: false });
      } finally {
        handleStop();
      }
      //update-end---author:wangshuai---date:2025-06-03---for:【issues/8338】AI应用chat回复stopinvalid，Replies will still be output---
    }
  }

  /**
   * Read text
   * @param message
   * @param options
   */
  async function sendMessage(message, options) {
    let param = {};
    if (!props.type && props.type != 'view') {
      param = {
        content: message,
        images: uploadUrlList.value?uploadUrlList.value:[],
        topicId: topicId.value,
        app: appData.value,
        responseMode: 'streaming',
      };
    }else{
      param = {
        content: message,
        topicId: usingContext.value?topicId.value:'',
        images: uploadUrlList.value?uploadUrlList.value:[],
        appId: appData.value.id,
        responseMode: 'streaming',
        conversationId: uuid.value === "1002"?'':uuid.value
      };

      if(headerTitle.value == 'New Chat'){
        headerTitle.value = message.length>10?truncateString(message,10):message
      }

      emit("reload-message-title",message.length>10?truncateString(message,10):message)
    }

    uploadUrlList.value = [];
    fileInfoList.value = [];
    knowList.value = [];
    options.message = message;
    const readableStream = await defHttp.post(
      {
        url: props.url,
        params: param,
        adapter: 'fetch',
        responseType: 'stream',
        timeout: 5 * 60 * 1000,
      },
      {
        isTransformResponse: false,
      }
    ).catch((e)=>{
      //update-begin---author:wangshuai---date:2025-04-28---for:【QQYUN-12297】【AI】Chat, timeout prompt---
      if(e.code === 'ETIMEDOUT'){
        updateChatFail(uuid, chatData.value.length - 1, "Too many users currently, please wait and try again later!");
        handleStop();
        return;
      }else{
        updateChatFail(uuid, chatData.value.length - 1, "Server error, please try again later!");
        handleStop();
        return;
      }
      console.error(e)
      //update-end---author:wangshuai---date:2025-04-28---for:【QQYUN-12297】【AI】Chat, timeout prompt---
    });
    await renderChatByResult(readableStream,options);
  }
  // Whether to use context
  const handleUsingContext = () => {
    usingContext.value = !usingContext.value;
    if (usingContext.value) {
      message.success("In current mode, sending messages will include previous chat history");
    } else {
      message.warning("In current mode, sending messages will not include previous chat history");
    }
  };

  /**
   * Tip
   * @param value
   */
  function messageTip(value) {
    message.warning(value);
  }

  /**
   * Render text
   * @param item
   * @param conversationId
   * @param text
   * @param options
   */
  async function renderText(item,conversationId,text,options) {
    let returnText = "";
    if (item.event == 'MESSAGE') {
      let message = item.data.message;
      let messageText = "";
      //update-begin---author:wangshuai---date:2025-04-24---for:Should first check if it contains card---
      if(message && message.indexOf("::card::") !== -1){
        messageText = message;
      } else {
        text = text + item.data.message;
        messageText = text;
        returnText = text;
      }
      //update-end---author:wangshuai---date:2025-04-24---for:Should first check if it contains card---
      // Get requestId from message
      if (item.requestId) {
        requestId.value = item.requestId;
      }
      //Update chat information
      updateChat(uuid.value, chatData.value.length - 1, {
        dateTime: new Date().toLocaleString(),
        content: messageText,
        inversion: 'ai',
        error: false,
        loading: true,
        conversationOptions: { conversationId: conversationId, parentMessageId: topicId.value },
        requestOptions: { prompt: message, options: { ...options } },
        referenceKnowledge: knowList.value,
      });
    }
    if(item.event == 'INIT_REQUEST_ID'){
      if (item.requestId) {
        requestId.value = item.requestId;
        localStorage.setItem('chat_requestId_' + uuid.value, JSON.stringify({ requestId: item.requestId, message: options.message }));
      }
    }
    if (item.event == 'MESSAGE_END') {
      topicId.value = item.topicId;
      conversationId = item.conversationId;
      uuid.value = item.conversationId;
      localStorage.removeItem('chat_requestId_' + uuid.value);
      handleStop();
    }
    if (item.event == 'FLOW_FINISHED') {
      //update-begin---author:wangshuai---date:2025-03-07---for:【QQYUN-11457】Chat calls flow, execution failed but no prompt---
      if(item.data && !item.data.success){
        updateChatFail(uuid, chatData.value.length - 1, item.data.message?item.data.message:'Request error, please try again later!');
        handleStop();
        return "";
      }
      //update-end---author:wangshuai---date:2025-03-07---for:【QQYUN-11457】Chat calls flow, execution failed but no prompt---
      topicId.value = item.topicId;
      conversationId = item.conversationId;
      uuid.value = item.conversationId;
      requestId.value = item.requestId;
      handleStop();
    }
    if (item.event == 'ERROR') {
      updateChatFail(uuid, chatData.value.length - 1, item.data.message?item.data.message:'Request error，Please try again later！');
      handleStop();
      return "";
    }

    //update-begin---author:wangshuai---date:2025-03-21---for:【QQYUN-11495】【AI】Real-time display of current thinking progress---
    if(item.event === "NODE_STARTED"){
      if(!item.data || item.data.type !== 'end'){
        let aiText = "";
        if(item.data.type === 'llm'){
          aiText = "Building response content";
        }
        if(item.data.type === 'knowledge'){
          aiText = "Performing deep search on knowledge base";
        }
        if(item.data.type === 'classifier'){
          aiText = "Classifying";
        }
        if(item.data.type === 'code'){
          aiText = "Executing code operations";
        }
        if(item.data.type === 'subflow'){
          aiText = "Running sub-process";
        }
        if(item.data.type === 'enhanceJava'){
          aiText = "Executing Java enhancement";
        }
        if(item.data.type === 'http'){
          aiText = "Sending HTTP request";
        }
        if(!text){
          //Update chat information
          updateChat(uuid.value, chatData.value.length - 1, {
            dateTime: new Date().toLocaleString(),
            retrievalText: aiText,
            text:"",
            inversion: 'ai',
            error: false,
            loading: true,
            conversationOptions: null,
            requestOptions: { prompt: message, options: { ...options } },
            referenceKnowledge: knowList.value,
          });
        }
      }
    }
    //update-end---author:wangshuai---date:2025-03-21---for:【QQYUN-11495】【AI】Real-time display of current thinking progress---
    else if (item.event === 'NODE_FINISHED') {
      if(!item.data || item.data.type !== 'end'){
        if(item.data.type === 'knowledge'){
          const id = item.data.id;
          const data = item.data.outputs[id + ".data"]
          knowList.value.push(data)
          //更新chat信息
          updateChatSome(uuid.value, chatData.value.length - 1, {referenceKnowledge: knowList.value})
        }
      }
    }
    if(!returnText){
      returnText = text;
    }
    return { returnText, conversationId };
  }

  //Upload file list collection
  const uploadUrlList = ref<any>([]);
  //File collection
  const fileInfoList = ref<any>([]);

  /**
   * File upload callback event
   * @param info
   */
  function handleChange(info) {
    let { fileList, file } = info;
    fileInfoList.value = fileList;
    if (file.status === 'error' || (file.response && file.response.code == 500)) {
      message.error(file.response?.message || `${file.name} upload failed, please check server logs`);
      return;
    }
    if (file.status === 'done') {
      uploadUrlList.value.push(file.response.message);
    }
  }

  /**
   * Get image URL
   *
   * @param url
   */
  function getImage(url) {
    return getFileAccessHttpUrl(url);
  }

  /**
   * Before upload event
   */
  function beforeUpload(file) {
    var fileType = file.type;
    if (fileType === 'image') {
      if (fileType.indexOf('image') < 0) {
        message.warning('Please upload an image');
        return false;
      }
    }
    if(uploadUrlList.value && uploadUrlList.value.length > 2){
      message.warning("You can only upload up to three images!");
      return false;
    }
    return true;
  }

  /**
   * Delete image
   */
  function deleteImage(index) {
    uploadUrlList.value.splice(index,1);
    fileInfoList.value.splice(index,1);
  }

  /**
   * Image preview
   * @param url
   */
  function handlePreview(url){
    const onImgLoad = ({ index, url, dom }) => {
      console.log(`Image ${index + 1} loaded, URL: ${url}`, dom);
    };
    let imageList = [getImage(url)];
    createImgPreview({ imageList: imageList, defaultWidth: 700, rememberState: true, onImgLoad });
  }

  /**
   * Truncate string
   * @param str
   * @param maxLength
   */
  function truncateString(str, maxLength) {
    if (str.length <= maxLength){
      return str;
    }
    let chineseCount = 0;
    let englishCount = 0;
    let digitCount = 0;
    let result = '';
    for (let i = 0; i < str.length; i++) {
      const char = str[i];
      if (/[\u4e00-\u9fa5]/.test(char)) { // Check if it's a Chinese character
        chineseCount++;
      } else if (/[a-zA-Z]/.test(char)) { // Check if it's an English letter
        englishCount++;
      } else if (/\d/.test(char)) { // Check if it's a number
        digitCount++;
      }
      if (chineseCount + englishCount / 2 + digitCount / 2 > maxLength) {
        break;
      }
      result += char;
    }

    return result;
  }

  /**
   * Paste event
   * @param event
   */
  function paste(event) {
    if(uploadUrlList.value && uploadUrlList.value.length > 2){
      message.warning("You can only upload up to three images!");
      return;
    }
    const items = (event.clipboardData || window.clipboardData).items;
    if (!items || items.length === 0){
      //Browser does not support copying images
      message.error('Current browser does not support opening local images!');
      return;
    }
    let image = null;
    for (let i = 0; i < items.length; i++) {
      if (items[i].type.indexOf('image') !== -1) {
        image = items[i].getAsFile();
        handleUploadImage(image);
        break;
      }
    }
  }

  /**
   * Paste image
   * @param image
   */
  async function handleUploadImage(image) {
    const isReturn = (fileInfo) => {
      try {
        if (fileInfo.code === 0) {
          let { message } = fileInfo;
          uploadUrlList.value.push(message);
          fileInfoList.value.push(image);
        } else if (fileInfo.code === 500 || fileInfo.code === 510) {
          message.error(fileInfo.message || `${image.name} Import failed`);
        }
      } catch (error) {
        console.log('Imported data exception', error);
        message.error(`${image.name} import failed`);
      }
    };
    await defHttp.uploadFile({ url: "/airag/chat/upload" }, { file: image }, { success: isReturn });
  }

  /**
   * Render returned results
   * @param readableStream
   * @param options
   */
  async function renderChatByResult(readableStream, options) {
    const reader = readableStream.getReader();
    const decoder = new TextDecoder('UTF-8');
    let conversationId = '';
    let buffer = '';
    let text = ''; // Split messages according to SSE protocol
    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      //update-begin---author:wangshuai---date:2025-03-12---for:【QQYUN-11555】Chat needs to display messages in streaming---
      let result = decoder.decode(value, { stream: true });
      result = buffer + result;
      const lines = result.split('\n\n');
      for (let line of lines) {
        if (line.startsWith('data:')) {
          let content = line.replace('data:', '').trim();
          if(!content){
            continue;
          }
          if(!content.endsWith('}')){
            buffer = buffer + line;
            continue;
          }
          buffer = "";
          try {
            //update-begin---author:wangshuai---date:2025-03-13---for:【QQYUN-11572】Cannot display content dynamically when published online, need to refresh to see full answer---
            if(content.indexOf(":::card:::") !== -1){
              content = content.replace(/\s+/g, '');
            }
            let parse = JSON.parse(content);
            await renderText(parse,conversationId,text,options).then((res)=>{
              text = res.returnText;
              conversationId = res.conversationId;
            });
            //update-end---author:wangshuai---date:2025-03-13---for:【QQYUN-11572】Cannot display content dynamically when published online, need to refresh to see full answer---
          } catch (error) {
            console.log('Error parsing update:', error);
          }
          //update-end---author:wangshuai---date:2025-03-12---for:【QQYUN-11555】Chat needs to display messages in streaming---
        }else{
          if(!line){
            continue;
          }
          if(!line.endsWith('}')){
            buffer = buffer + line;
            continue;
          }
          buffer = "";
          //update-begin---author:wangshuai---date:2025-03-13---for:【QQYUN-11572】Cannot display content dynamically when published online, need to refresh to see full answer---
          try {
            if(line.indexOf(":::card:::") !== -1){
              line = line.replace(/\s+/g, '');
            }
            let parse = JSON.parse(line);
            await renderText(parse, conversationId, text, options).then((res) => {
              text = res.returnText;
              conversationId = res.conversationId;
            });
          } catch (error) {
            console.log('Error parsing update:', error);
          }
          //update-end---author:wangshuai---date:2025-03-13---for:【QQYUN-11572】Cannot display content dynamically when published online, need to refresh to see full answer---
        }
      }
    }
  }

  /**
   * AI reconnection
   */
  async function aiReConnection() {
    //Query requestId
    let chat = localStorage.getItem("chat_requestId_" + uuid.value);
    if(chat) {
      let array = JSON.parse(chat);
      let message = array.message;
      let requestId = array.requestId;
      const result = await defHttp.get({ url: '/airag/chat/receive/' + requestId ,
        adapter: 'fetch',
        responseType: 'stream',
        timeout: 5 * 60 * 1000
      }, { isTransformResponse: false }).catch(async (err)=>{
        loading.value = false;
      });
      if(result && message){
        loading.value = true;
        //Send user message
        addChat(uuid.value, {
          dateTime: new Date().toLocaleString(),
          content: message,
          images:uploadUrlList.value?uploadUrlList.value:[],
          inversion: 'user',
          error: false,
          conversationOptions: null,
          requestOptions: { prompt: message, options: null },
        });
        let options: any = {};
        const lastContext = conversationList.value[conversationList.value.length - 1]?.conversationOptions;
        if (lastContext && usingContext.value) {
          options = { ...lastContext };
        }
        //Add toaiinformation
        addChat(uuid.value, {
          dateTime: new Date().toLocaleString(),
          content: 'Please wait',
          loading: false,
          inversion: 'ai',
          error: false,
          conversationOptions: null,
          requestOptions: { prompt: message, options: { ...options } },
          referenceKnowledge: [],
        });
        options.message = message;
        scrollToBottom();
        //Streaming output
        await renderChatByResult(result,options);
      } else {
        loading.value = false;
      }
    }
  }

  //Listen to opening remarks
  watch(
    () => props.prologue,
    (val) => {
      try {
        if (val) {
          topChat(val);
        }
      } catch (e) {}
    }
  );

  //Listen to opening remarks preset questions
  watch(
    () => props.presetQuestion,
    (val) => {
      topChat(props.prologue);
    }
  );

  //Listen to application information
  watch(
    () => props.formState,
    (val) => {
      try {
        if (val) {
          appData.value = val;
        }
      } catch (e) {}
    },
    { deep: true, immediate: true }
  );

  //Listen to historical information
  watch(
    () => props.historyData,
    (val) => {
      try {
        //update-begin---author:wangshuai---date:2025-03-06---for:【QQYUN-11384】Browser opening application loses opening remarks---
        if (val && val.length > 0) {
          chatData.value = cloneDeep(val);
          if(chatData.value[0]){
            topicId.value = chatData.value[0].topicId
          }
        }else{
          chatData.value = [];
          headerTitle.value = props.chatTitle;
        }
        if(props.prologue && props.chatTitle){
          topChat(props.prologue)
        }
        //AI reply reconnection
        aiReConnection();
      } catch (e) {
        console.log(e)
      }
      //update-end---author:wangshuai---date:2025-03-06---for:【QQYUN-11384】Browser opening application loses opening remarks---
    },
    { deep: true, immediate: true }
  );

  onMounted(() => {
    scrollToBottom();
    uploadUrlList.value = [];
    fileInfoList.value = [];
  });
</script>

<style lang="less" scoped>
  .chatWrap {
    width: 100%;
    height: 100%;
    padding: 20px;
    .content {
      height: 100%;
      width: 100%;
      background: #fff;
      display: flex;
      flex-direction: column;
    }
  }
  .main {
    flex: 1;
    min-height: 0;
    .scrollArea {
      overflow-y: auto;
      height: 100%;
    }
    .chatContentArea {
      padding: 10px;
    }
  }
  .emptyArea {
    display: flex;
    justify-content: center;
    align-items: center;
    color: #d4d4d4;
  }
  .stopArea {
    display: flex;
    justify-content: center;
    padding: 10px 0;
  }
  .footer {
    display: flex;
    flex-direction: column;
    padding: 6px 16px;
    .topArea {
      padding-left: 6%;
      margin-bottom: 6px;
    }
    .bottomArea {
      display: flex;
      align-items: center;

      .ant-input {
        margin: 0 8px;
      }
      .ant-input,
      .ant-btn {
        height: 36px;
      }
      textarea.ant-input {
        padding-top: 6px;
        padding-bottom: 6px;
      }
      .contextBtn,
      .delBtn {
        padding: 0;
        width: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .delBtn {
        margin-right: 8px;
      }
      .contextBtn {
        color: #a8071a;
        &.enabled {
          color: @primary-color;
        }
        font-size: 18px;
      }
      .sendBtn {
        font-size: 18px;
        width: 36px;
        display: flex;
        padding: 8px;
        align-items: center;
      }
      .stopBtn {
        width: 32px;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 8px;
      }
    }
  }
  :deep(.chatgpt .markdown-body) {
    background-color: #f4f6f8;
  }
  :deep(.ant-message) {
    top: 50% !important;
  }
  .header-title{
    color: #101828;
    font-size: 16px;
    font-weight: 400;
    padding-bottom: 8px;
    margin-left: 20px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: flex;
    justify-content: space-between;
    height: 30px;
    .header-advertisint{
      display:flex;
      margin-right: 20px;
      font-size: 12px;
    }
  }
  .chat-textarea{
    display: flex;
    align-items: center;
    width: 100%;
    border-radius: 15px;
    border-style: solid;
    border-width: 1px;
    flex-direction: column;
    transition: width 0.3s;
    border-color: #d2d7e5;
    .textarea-top{
      border-bottom: 1px solid #f0f0f5;
      padding: 12px 28px;
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      .top-image{
        display: flex;
        img{
          border-radius: 8px;
          cursor: pointer;
          height: 60px;
          position: relative;
          width: 60px;
        }
      }
    }
    .textarea-bottom{
      display: flex;
      flex-direction: row;
      align-items: center;
      flex: 1 1;
      min-height: 48px;
      position: relative;
      padding: 8px 8px 8px 10px;
      width: 100%;
    }
  }
  .chat-textarea:hover{
    border-color: #9dc1fb;
  }
  .textarea-active{
    border-color: #98bdfa !important;
  }
  :deep(.ant-divider-vertical){
    margin: 0 2px;
  }
  .upload-icon{
    cursor: pointer;
    position: absolute;
    background-color: #1D1C23;
    color: white;
    border-radius: 50%;
    padding: 4px;
    display: none;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 4px #e6e6e6;
    margin-left: 44px;
    margin-top: -4px;
  }
  .top-image:hover{
    .upload-icon{
      display: flex;
    }
  }
  
  @media (max-width: 600px) {
    //Mobile styles, tablets don't need adjustment
    .footer{
      padding: 0;
      .bottomArea{
        .delBtn{
          margin-right: 0;
        }
      }
    }
    .chatWrap{
      padding: 10px 10px 10px 0;
    }
    .main .chatContentArea{
      padding: 10px 0 0 10px;
    }
  }
</style>
<style lang="less">
 .ai-chat-modal{
   z-index: 9999 !important;
 }
 .ai-chat-message{
   z-index: 9999 !important;
 }
</style>
