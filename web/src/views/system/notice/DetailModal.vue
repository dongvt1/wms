<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :width="800" title="check the details" :showCancelBtn="false" :showOkBtn="false" :maxHeight="500">
    <div class="print-btn" @click="onPrinter">
      <Icon icon="ant-design:printer-filled" />
      <span class="print-text">Print</span>
    </div>
    <iframe ref="iframeRef" :src="frameSrc" class="detail-iframe" @load="onIframeLoad"></iframe>
    <template v-if="noticeFiles && noticeFiles.length > 0">
      <div class="files-title">Related accessories：</div>
      <template v-for="(file, index) in noticeFiles" :key="index">
        <div class="files-area">
          <div class="files-area-text">
            <span>
              <paper-clip-outlined />
              <a
                target="_blank"
                rel="noopener noreferrer"
                :title="file.fileName"
                :href="getFileAccessHttpUrl(file.filePath)"
                class="ant-upload-list-item-name"
                >{{ file.fileName }}</a
              >
            </span>
          </div>
          <div class="files-area-operate">
            <download-outlined class="item-icon" @click="handleDownloadFile(file.filePath)" />
            <eye-outlined class="item-icon" @click="handleViewFile(file.filePath)" />
          </div>
        </div>
      </template>
      <a v-if="noticeFiles.length > 1" :href="downLoadFiles + '?id=' + noticeId + '&token=' + getToken()" target="_blank"  style="margin: 15px 6px;color: #5ac0fa;">
        <download-outlined class="item-icon" style="margin-right: 5px"  /><span>Batch download all attachments</span>
      </a>
    </template>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { ref } from 'vue';
  import { buildUUID } from '@/utils/uuid';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import { DownloadOutlined, EyeOutlined, PaperClipOutlined } from '@ant-design/icons-vue';
  import { encryptByBase64 } from '@/utils/cipher';
  import { useGlobSetting } from '@/hooks/setting';
  import { getToken } from "@/utils/auth";
  const glob = useGlobSetting();
  // Getprops
  defineProps({
    frameSrc: propTypes.string.def(''),
  });
  /**
   * Download file path
   */
  const downLoadFiles = `${glob.domainUrl}/sys/annountCement/downLoadFiles`;

  //Attachment content
  const noticeFiles = ref([]);
  //dataID
  const noticeId = ref('');
  //form assignment
  const [registerModal] = useModalInner((data) => {
    noticeFiles.value = [];
    noticeId.value = data.record.id;
    if (data.record?.files && data.record?.files.length > 0) {
      noticeFiles.value = data.record.files.split(',').map((item) => {
        return {
          fileName: item.split('/').pop(),
          filePath: item,
        };
      });
    }
  });
  // iframeQuote
  const iframeRef = ref<HTMLIFrameElement>();
  // 存储当前Print会话ID
  const printSessionId = ref<string>('');
  // iframeInitialize communication after loading is complete
  const onIframeLoad = () => {
    printSessionId.value = buildUUID(); // Generate new session for each loadID
  };
  //Print
  function onPrinter() {
    if (!iframeRef.value) return;
    console.log('onPrinter', iframeRef.value);
    iframeRef.value?.contentWindow?.postMessage({ printSessionId: printSessionId.value, type: 'action:print' }, '*');
  }
  /**
   * Download file
   * @param filePath
   */
  function handleDownloadFile(filePath) {
    window.open(getFileAccessHttpUrl(filePath), '_blank');
  }
  /**
   * Preview file
   * @param filePath
   */
  function handleViewFile(filePath) {
    if (filePath) {
      let url = encodeURIComponent(encryptByBase64(filePath));
      let previewUrl = `${glob.viewUrl}?url=` + url;
      window.open(previewUrl, '_blank');
    }
  }
</script>

<style scoped lang="less">
  .print-btn {
    position: absolute;
    top: 80px;
    right: 40px;
    cursor: pointer;
    color: #a3a3a5;
    z-index: 999;
    .print-text {
      margin-left: 5px;
    }
    &:hover {
      color: #40a9ff;
    }
  }
  .detail-iframe {
    border: 0;
    width: 100%;
    height: 100%;
    min-height: 500px;
    // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1685】Two scroll bars appear when viewing notifications and announcements
    display: block;
    // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1685】Two scroll bars appear when viewing notifications and announcements
  }
  .files-title {
    font-size: 16px;
    margin: 10px;
    font-weight: 600;
    color: #333;
  }
  .files-area {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    margin: 6px;
    &:hover {
      background-color: #f5f5f5;
    }
    .files-area-text {
      display: flex;
      .ant-upload-list-item-name {
        margin: 0 6px;
        color: #56befa;
      }
    }
    .files-area-operate {
      display: flex;
      margin-left: 10px;
      .item-icon {
        cursor: pointer;
        margin: 0 6px;
        &:hover {
          color: #56befa;
        }
      }
    }
  }
</style>
