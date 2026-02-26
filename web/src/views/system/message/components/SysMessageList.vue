<template>
  <a-list item-layout="horizontal" :data-source="messageList" :locale="locale">
    <template #loadMore>
      <div
        v-if="messageList && messageList.length > 0 && !loadEndStatus && !loadingMoreStatus"
        :style="{ textAlign: 'center', marginTop: '12px', height: '32px', lineHeight: '32px' }"
      >
        <a-button @click="onLoadMore">load more</a-button>
      </div>
      <div
        v-if="messageList && messageList.length > 0 && loadEndStatus"
        :style="{ textAlign: 'center', marginTop: '12px', height: '32px', lineHeight: '32px' }"
      >
        no more
      </div>
    </template>

    <template #renderItem="{ item }">
      <a-list-item :style="{ background: item?.izTop && item.izTop == 1 ? '#f7f7f7' : 'auto' }">
        <template #actions>
          <a-rate :value="item.starFlag=='1'?1:0" :count="1" @click="clickStar(item)" style="cursor: pointer" disabled />
        </template>

        <!-- update-begin-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，The other party cannot see which form it is.@of，no hyperlink。 -->
        <a-list-item-meta>
          <template #description>
            <div v-if="isFormComment(item)" style="background: #f7f7f7;color: #555;padding: 2px 5px;white-space:nowrap;overflow: hidden">
              <div v-html="getHtml(item.msgContent)"></div>
            </div>
            <div>
              {{item.createTime}}
            </div>
          </template>
          <!-- update-end-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，The other party cannot see which form it is.@of，no hyperlink。 -->

          <template #title>
            <div style="position: relative">
           <!--   <span style="display: inline-block; position: absolute; left: -16px">
                <exclamation-outlined v-if="noRead(item)" title="unread messages" style="color: red" />
              </span>-->

              <span>{{ getMsgCategory(item) }}</span>
              <span v-if="item.busType == 'bpm' || item.busType == 'bpm_cc' || item.busType == 'msg_node' || item.busType == 'bpm_msg_node'" class="bpm-cuiban-content" v-html="item.msgContent">
              </span>
<!--              <a-tooltip v-else>-->
<!--                <template #title>-->
<!--                  <div v-html="item.msgContent"></div>-->
<!--                </template>-->
<!--                {{ item.titile }}-->
<!--              </a-tooltip>-->
              <span v-else>{{ item.titile }}</span>

              <a @click="showMessageDetail(item)" style="margin-left: 16px">{{ getHrefText(item) }}</a>
            </div>
          </template>
          <template #avatar>
            <template v-if="item.busType=='email'">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><mail-outlined style="font-size: 16px" title="unread messages"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><mail-outlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else-if="item.busType=='bpm_task'">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><interaction-outlined style="font-size: 16px" title="unread messages"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><interaction-outlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else-if="item.busType=='bpm'">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><alert-outlined style="font-size: 16px" title="unread messages"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><alert-outlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else>
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><bell-filled style="font-size: 16px" title="unread messages"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><bell-filled style="font-size: 16px" /></a-avatar>
            </template>
          </template>
        </a-list-item-meta>
      </a-list-item>
    </template>
  </a-list>

  <!-- update-begin-author:liusq date:2023-10-26 for: [QQYUN-6713]System notification打开弹窗修改 -->
  <keep-alive>
    <component v-if="currentModal" v-bind="bindParams" :key="currentModal" :is="currentModal" @register="modalRegCache[currentModal].register" />
  </keep-alive>
  <!-- update-end-author:liusq date:2023-10-26 for: [QQYUN-6713]System notification打开弹窗修改 -->
</template>

<script>

  import { FilterOutlined, CloseOutlined, BellFilled, ExclamationOutlined, MailOutlined,InteractionOutlined, AlertOutlined } from '@ant-design/icons-vue';
  import { useSysMessage, useMessageHref } from './useSysMessage';
  import {getGloablEmojiIndex, useEmojiHtml} from "/@/components/jeecg/comment/useComment";
  import { ref, h, watch } from "vue";

  export default {
    name: 'SysMessageList',
    components: {
      FilterOutlined,
      CloseOutlined,
      BellFilled,
      ExclamationOutlined,
      MailOutlined,
      InteractionOutlined,
      AlertOutlined,
    },
    props:{
      star: {
        type:Boolean,
        default: false
      },
      isLowApp:{
        type:Boolean,
        default: false
      },
      messageCount: {
        type: Number,
        default: 0
      },
      cancelStarAfterDel: {
        type: Boolean,
        default: false,
      },
    },
    emits:['close', 'detail', 'clear', 'close-modal'],
    setup(props, {emit}){
      const { messageList,loadEndStatus,loadingMoreStatus,onLoadMore,noRead, getMsgCategory, getHrefText, searchParams, reset, loadData, updateStarMessage } = useSysMessage(setLocaleText);

      //System messages
      const messageCount = ref(0);

      function reload(params){
        let { fromUser, rangeDateKey, rangeDate, noticeType } = params;
        searchParams.fromUser = fromUser||'';
        searchParams.rangeDateKey = rangeDateKey||'';
        searchParams.rangeDate = rangeDate||[];
        searchParams.noticeType = noticeType || '';
        //listAssign initial value when list is empty
        locale.value = { locale: { emptyText: `<a-empty />` }};
        if(props.star===true){
          searchParams.starFlag = '1'
        }else{
          searchParams.starFlag = ''
        }
        reset();
        loadData();
      }

      function clickStar(item){
        console.log(item)
        updateStarMessage(item);
        // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-349】notify-Star newstabAfter the list is unstarred，This message is removed from the star list
        if (item.starFlag == '1' && props.cancelStarAfterDel) {
          const findIndex = messageList.value.findIndex((item) => item.id === item.id);
          if (findIndex !== -1) {
            messageList.value.splice(findIndex, 1);
            return;
          }
        }
        // update-end--author:liaozhiyang---date:20240717---for：【TV360X-349】notify-Star newstabAfter the list is unstarred，This message is removed from the star list
        if(item.starFlag=='1'){
          item.starFlag = '0'
        }else{
          item.starFlag = '1'
        }
      }

      // update-begin-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，The other party cannot see which form it is.@of，no hyperlink
      const { goPage, currentModal, modalRegCache, bindParams, isFormComment } = useMessageHref(emit, props);
      //const emojiIndex = inject('$globalEmojiIndex')
      const emojiIndex = getGloablEmojiIndex()
      const { getHtml } = useEmojiHtml(emojiIndex);
      // update-end-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，The other party cannot see which form it is.@of，no hyperlink

      function showMessageDetail(record){
        record.readFlag = '1'
        goPage(record);
        emit('close', record.id)
        //update-begin---author:wangshuai---date:2024-06-11---for:【TV360X-791】收到邮件notify，Click to reply，应该把notify公告列表关闭---
        if(record.busType==='email'){
          emit('close-modal')
        }
        //update-end---author:wangshuai---date:2024-06-11---for:【TV360X-791】收到邮件notify，Click to reply，应该把notify公告列表关闭---
      }

      //returnlist列表为空数据时展示of内容
      const locale = ref({});

      /**
       * Unread click event
       */
      function noReadClick() {
        emit('clear')
      }

      /**
       * set uplist为空of时候提示文本
       *
       */
      function setLocaleText() {
        //update-begin---author:wangshuai---date:2024-04-24---for:【QQYUN-9105】There is a problem with unread---
        let rangeDateKey = searchParams.rangeDateKey;
        let value = messageCount.value;
        if (value > 0 && !props.star && rangeDateKey && rangeDateKey === '7day') {
        //update-end---author:wangshuai---date:2024-04-24---for:【QQYUN-9105】There is a problem with unread---
          locale.value = {
            emptyText: h(
                'span',
                {
                  style: {'color': 'rgb(255,154,0)', 'cursor': 'pointer', 'text-align': 'left', 'display': 'block'},
                  onClick: () => {
                    noReadClick();
                  },
                }, `还剩余unread messages(${value > 99 ? '99+' : value})`)
          }
        } else {
          locale.value = { locale: {emptyText: `<a-empty />` }};
        }
      }

      //Number of monitoring messages
      watch(() => props.messageCount, (value) => {
        messageCount.value = value;
      }, { immediate: true })

      return {
        messageList,
        loadEndStatus,
        loadingMoreStatus,
        onLoadMore,
        noRead,
        getMsgCategory,
        getHrefText,
        reload,
        clickStar,
        showMessageDetail,
        isFormComment,
        getHtml,
        modalRegCache,
        currentModal,
        bindParams,
        locale,
      };
    },
  };
</script>

<style scoped lang="less">
   .msg-no-read{
     :deep(.ant-badge-dot){
      top: 5px;
      right: 3px;
    }
  }
   :deep(.bpm-cuiban-content) p{
    display: inherit;
     margin-bottom: 0;
     margin-top: 0;
   }

   /** QQYUN-5688 【style】Why is it not your hand when you put the mouse on it? */
   :deep(.ant-rate-disabled){
     .ant-rate-star{
       cursor: pointer !important;
     }
   }
</style>
