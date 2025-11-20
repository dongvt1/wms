<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" title="importEXCEL" :width="600" @cancel="handleClose" :confirmLoading="uploading" destroyOnClose>
      <!--Whether to verify-->
      <div style="margin: 0 5px 5px" v-if="online">
        <span style="display: inline-block; height: 32px; line-height: 32px; vertical-align: middle">Whether to enable verification:</span>
        <span style="margin-left: 6px">
          <a-switch :checked="validateStatus == 1" @change="handleChangeValidateStatus" checked-children="yes" un-checked-children="no" />
        </span>
      </div>
      <!--upload-->
      <a-upload name="file" accept=".xls,.xlsx" :multiple="true" :fileList="fileList" @remove="handleRemove" :beforeUpload="beforeUpload">
        <a-button preIcon="ant-design:upload-outlined">选择import文件</a-button>
      </a-upload>
      <!--footer-->
      <template #footer>
        <a-button @click="handleClose">closure</a-button>
        <a-button type="primary" @click="handleImport" :disabled="uploadDisabled" :loading="uploading">{{
          uploading ? 'upload中...' : '开始upload'
        }}</a-button>
      </template>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, unref, watchEffect, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { defHttp } from '/@/utils/http/axios';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isObject } from '/@/utils/is';

  export default defineComponent({
    name: 'JImportModal',
    components: {
      BasicModal,
    },
    props: {
      url: {
        type: String,
        default: '',
        required: false,
      },
      biz: {
        type: String,
        default: '',
        required: false,
      },
      //yesnoonlineimport
      online: {
        type: Boolean,
        default: false,
        required: false,
      },
    },
    emits: ['ok', 'register'],
    setup(props, { emit, refs }) {
      const { createMessage, createWarningModal } = useMessage();
      //Registration pop-up box
      const [register, { closeModal }] = useModalInner((data) => {
        reset(data);
      });
      const glob = useGlobSetting();
      const attrs = useAttrs();
      const uploading = ref(false);
      //file collection
      const fileList = ref([]);
      //uploadurl
      const uploadAction = ref('');
      const foreignKeys = ref('');
      //Verification status
      const validateStatus = ref(0);
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      //monitorurl
      watchEffect(() => {
        props.url && (uploadAction.value = `${glob.uploadUrl}${props.url}`);
      });
      //buttondisabledstate
      const uploadDisabled = computed(() => !(unref(fileList).length > 0));

      //closure方法
      function handleClose() {
        // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7477】closure弹窗清空内容（之前upload失败closure后不会清除）
        closeModal();
        reset();
        // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7477】closure弹窗清空内容（之前upload失败closure后不会清除）
      }

      //Verification status切换
      function handleChangeValidateStatus(checked) {
        validateStatus.value = !!checked ? 1 : 0;
      }

      //移除upload文件
      function handleRemove(file) {
        const index = unref(fileList).indexOf(file);
        const newFileList = unref(fileList).slice();
        newFileList.splice(index, 1);
        fileList.value = newFileList;
      }

      //upload前处理
      function beforeUpload(file) {
        fileList.value = [...unref(fileList), file];
        return false;
      }

      //文件upload
      function handleImport() {
        let { biz, online } = props;
        const formData = new FormData();
        if (biz) {
          formData.append('isSingleTableImport', biz);
        }
        if (unref(foreignKeys) && unref(foreignKeys).length > 0) {
          formData.append('foreignKeys', unref(foreignKeys));
        }
        // update-begin--author:liaozhiyang---date:20240429---for：【issues/6124】When the user does not【Onlineform development】页面的权限时用户无权import从表数据
        if (isObject(foreignKeys.value)) {
          formData.append('foreignKeys', JSON.stringify(foreignKeys.value));
        }
        // update-end--author:liaozhiyang---date:20240429---for：【issues/6124】When the user does not【Onlineform development】页面的权限时用户无权import从表数据
        if (!!online) {
          formData.append('validateStatus', unref(validateStatus));
        }
        unref(fileList).forEach((file) => {
          formData.append('files[]', file);
        });
        uploading.value = true;

        //TODO How to handle the request
        let headers = {
          'Content-Type': 'multipart/form-data;boundary = ' + new Date().getTime(),
        };
        defHttp.post({ url: props.url, params: formData, headers }, { isTransformResponse: false }).then((res) => {
          uploading.value = false;
          if (res.success) {
            if (res.code == 201) {
              errorTip(res.message, res.result);
            } else {
              createMessage.success(res.message);
            }
            handleClose();
            reset();
            emit('ok');
          } else {
            createMessage.warning(res.message);
          }
        }).catch(() => {
          uploading.value = false;
        });
      }

      //Error message prompt
      function errorTip(tipMessage, fileUrl) {
        let href = glob.uploadUrl + fileUrl;
        createWarningModal({
          title: 'import成功,但yes有错误数据!',
          centered: false,
          content: `<div>
                        <span>${tipMessage}</span><br/>
                        <span>For specific details please<a href = ${href} target="_blank"> Click to download </a> </span>
                      </div>`,
        });
      }

      //reset
      function reset(arg?) {
        fileList.value = [];
        uploading.value = false;
        foreignKeys.value = arg;
        validateStatus.value = 0;
      }

      return {
        register,
        getBindValue,
        uploadDisabled,
        fileList,
        uploading,
        validateStatus,
        handleClose,
        handleChangeValidateStatus,
        handleRemove,
        beforeUpload,
        handleImport,
      };
    },
  });
</script>
