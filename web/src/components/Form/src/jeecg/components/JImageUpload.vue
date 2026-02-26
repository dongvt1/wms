<template>
  <div class="clearfix">
    <a-upload
      :listType="listType"
      accept="image/*"
      :multiple="multiple"
      :action="uploadUrl"
      :headers="headers"
      :data="{ biz: bizPath }"
      v-model:fileList="uploadFileList"
      :beforeUpload="beforeUpload"
      :disabled="disabled"
      @change="handleChange"
      @preview="handlePreview"
    >
      <div v-if="uploadVisible">
        <div v-if="listType == 'picture-card'">
          <LoadingOutlined v-if="loading" />
          <UploadOutlined v-else />
          <div class="ant-upload-text">{{ text }}</div>
        </div>
        <a-button v-if="listType == 'picture'" :disabled="disabled">
          <UploadOutlined></UploadOutlined>
          {{ text }}
        </a-button>
      </div>
    </a-upload>
    <a-modal :width="previewWidth" :open="previewVisible" :footer="null" @cancel="handleCancel()">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted, nextTick } from 'vue';
  import { LoadingOutlined, UploadOutlined } from '@ant-design/icons-vue';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFileAccessHttpUrl, getHeaders, getRandom } from '/@/utils/common/compUtils';
  import { uploadUrl as systemUploadUrl } from '/@/api/common/api';
  import { getToken } from '/@/utils/auth';

  const { createMessage, createErrorModal } = useMessage();
  export default defineComponent({
    name: 'JImageUpload',
    components: { LoadingOutlined, UploadOutlined },
    inheritAttrs: false,
    props: {
      //Binding value
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      //button text
      listType: {
        type: String,
        required: false,
        default: 'picture-card',
      },
      //button text
      text: {
        type: String,
        required: false,
        default: 'upload',
      },
      //这个属性用于控制文件upload的业务路径
      bizPath: {
        type: String,
        required: false,
        default: 'temp',
      },
      //Whether to disable
      disabled: {
        type: Boolean,
        required: false,
        default: false,
      },
      //upload数量
      fileMax: {
        type: Number,
        required: false,
        default: 1,
      },
      uploadUrl: {
        type: String,
        default: systemUploadUrl,
      },
      previewWidth: {
        type: Number,
        required: false,
        default: 520,
      },
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>([]);
      const attrs = useAttrs();
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //Get file name
      const getFileName = (path) => {
        if (path.lastIndexOf('\\') >= 0) {
          let reg = new RegExp('\\\\', 'g');
          path = path.replace(reg, '/');
        }
        return path.substring(path.lastIndexOf('/') + 1);
      };
      //token
      const headers = getHeaders();
      //upload状态
      const loading = ref<boolean>(false);
      //Is it an initial load?
      const initTag = ref<boolean>(true);
      //file list
      let uploadFileList = ref<any[]>([]);
      //Preview
      const previewImage = ref<string | undefined>('');
      //Preview box status
      const previewVisible = ref<boolean>(false);

      //计算是否开启多图upload
      const multiple = computed(() => {
        return props['fileMax'] > 1 || props['fileMax'] === 0;
      });

      //计算是否可以继续upload
      const uploadVisible = computed(() => {
        if (props['fileMax'] === 0) {
          return true;
        }
        return uploadFileList.value.length < props['fileMax'];
      });

      /**
       * monitorvaluechange
       */
      watch(
        () => props.value,
        (val, prevCount) => {
         //update-begin---author:liusq ---date:20230601  for：【issues/556】JImageUploadcomponentsvalueAssigning initial value does not display the picture------------
            if (val && val instanceof Array) {
            val = val.join(',');
          }
          if (initTag.value == true) {
            initFileList(val);
          }
        },
        { immediate: true }
        //update-end---author:liusq ---date:20230601  for：【issues/556】JImageUploadcomponentsvalueAssigning initial value does not display the picture------------
      );

      /**
       * 初始化file list
       */
      function initFileList(paths) {
        if (!paths || paths.length == 0) {
          uploadFileList.value = [];
          return;
        }
        let files = [];
        let arr = paths.split(',');
        arr.forEach((value) => {
          let url = getFileAccessHttpUrl(value);
          files.push({
            uid: getRandom(10),
            name: getFileName(value),
            status: 'done',
            url: url,
            response: {
              status: 'history',
              message: value,
            },
          });
        });
        uploadFileList.value = files;
      }

      /**
       * upload前校验
       */
      function beforeUpload(file) {
        let fileType = file.type;
        if (fileType.indexOf('image') < 0) {
          createMessage.info('请upload图片');
          return false;
        }
      }
      /**
       * 文件upload结果回调
       */
      function handleChange({ file, fileList, event }) {
        initTag.value = false;
        // update-begin--author:liaozhiyang---date:20231116---for：【issues/846】upload多个列表只显示一个
        // uploadFileList.value = fileList;
        if (file.status === 'error') {
          createMessage.error(`${file.name} upload失败.`);
        }
        // update-begin--author:liaozhiyang---date:20240704---for：【TV360X-1640】upload图片大小超出限制显示优化
        if (file.status === 'done' && file.response.success === false) {
          const failIndex = uploadFileList.value.findIndex((item) => item.uid === file.uid);
          if (failIndex != -1) {
            uploadFileList.value.splice(failIndex, 1);
          }
          createMessage.warning(file.response.message);
          return;
        }
        // update-end--author:liaozhiyang---date:20240704---for：【TV360X-1640】upload图片大小超出限制显示优化
        let fileUrls = [];
        let noUploadingFileCount = 0;
        if (file.status != 'uploading') {
          fileList.forEach((file) => {
            if (file.status === 'done') {
              fileUrls.push(file.response.message);
            }
            if (file.status != 'uploading') {
              noUploadingFileCount++;
            }
          });
          if (file.status === 'removed') {
            handleDelete(file);
          }
          if (noUploadingFileCount == fileList.length) {
            state.value = fileUrls.join(',');
            emit('update:value', fileUrls.join(','));
            // update-begin---author:wangshuai ---date:20221121  for：[issues/248]原生表单内使用图片components,关闭弹窗图片components值不会被清空------------
            nextTick(() => {
              initTag.value = true;
            });
            // update-end---author:wangshuai ---date:20221121  for：[issues/248]原生表单内使用图片components,关闭弹窗图片components值不会被清空------------
          }
        }
        // update-end--author:liaozhiyang---date:20231116---for：【issues/846】upload多个列表只显示一个
      }

      /**
       * Delete picture
       */
      function handleDelete(file) {
        //Add if necessary delete logic
        console.log(file);
      }

      /**
       * Preview片
       */
      function handlePreview(file) {
        previewImage.value = file.url || file.thumbUrl;
        previewVisible.value = true;
      }

      function getAvatarView() {
        if (uploadFileList.length > 0) {
          let url = uploadFileList[0].url;
          return getFileAccessHttpUrl(url, null);
        }
      }

      function handleCancel() {
        previewVisible.value = false;
      }

      return {
        state,
        attrs,
        previewImage,
        previewVisible,
        uploadFileList,
        multiple,
        headers,
        loading,
        beforeUpload,
        uploadVisible,
        handlePreview,
        handleCancel,
        handleChange,
      };
    },
  });
</script>
<style scoped>
  .ant-upload-select-picture-card i {
    font-size: 32px;
    color: #999;
  }

  .ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
  }
</style>
