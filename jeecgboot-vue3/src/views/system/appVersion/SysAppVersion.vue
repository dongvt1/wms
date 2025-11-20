<template>
  <PageWrapper contentFullHeight>
    <a-card :bordered="false" title="Version management">
      <!--edit mode-->
      <a-spin v-if="active" :spinning="confirmLoading">
        <a-form ref="formRef" :model="model" :labelCol="labelCol" :wrapperCol="wrapperCol" :rules="validatorRules">
          <a-row>
            <a-col :span="24">
              <a-form-item label="Version" name="appVersion">
                <a-input v-model:value="model.appVersion" placeholder="请输入Version" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="APPInstallapk" name="downloadUrl">
                <a-input placeholder="set upAPPInstallapk" v-model:value="model.downloadUrl">
                  <template #addonAfter>
                    <Icon icon="ant-design:upload-outlined" style="cursor: pointer" @click="showUploadModal('apk')" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="APPhot update file" name="wgtUrl">
                <a-input placeholder="set upAPPhot update file" v-model:value="model.wgtUrl">
                  <template #addonAfter>
                    <Icon icon="ant-design:upload-outlined" style="cursor: pointer" @click="showUploadModal('wgt')" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Update content">
                <a-textarea :rows="4" v-model:value="model.updateNote" placeholder="请输入Update content" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
        <JUploadModal :value="modalValue" :bizPath="filePath" :maxCount="1" @register="registerModel" @change="uploadBack" />
      </a-spin>
      <!--Detail mode-->
      <Description v-else class="desc" :column="1" :data="model" :schema="schema" />
      <!--bottom button-->
      <div class="anty-form-btn" v-if="hasPermission('app:edit:version')">
        <a-button v-if="active" @click="handleSubmit" type="primary" preIcon="ant-design:save-outlined">save</a-button>
        <a-button v-else @click="active = true" type="primary" preIcon="ant-design:edit-outlined">开启edit mode</a-button>
      </div>
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup name="portalapp-sysAppVersion">
  import { useMessage } from '/@/hooks/web/useMessage';
  import { usePermission } from '@/hooks/web/usePermission';
  import { JUploadModal } from '@/components/Form/src/jeecg/components/JUpload';
  import { useModal } from '@/components/Modal';
  import { reactive, ref, toRaw, unref, onMounted } from 'vue';
  import { PageWrapper } from '@/components/Page';
  import { queryAppVersion, saveAppVersion } from './appVersion.api';
  import { Description, DescItem } from '/@/components/Description/index';

  const { hasPermission } = usePermission();
  const { createMessage } = useMessage();

  const [registerModel, { openModal }] = useModal();
  const confirmLoading = ref(false);
  const active = ref(false);
  const formRef = ref<any>(null);
  const appKey = 'E0CC280';
  const filePath = 'appVersion';
  const uploadType = ref('');
  const modalValue = ref('');
  const labelCol = {
    xs: { span: 24 },
    sm: { span: 5 },
  };
  const wrapperCol = {
    xs: { span: 24 },
    sm: { span: 16 },
  };
  const model = reactive({
    id: 'E0CC280',
    appVersion: '',
    versionNum: 0,
    updateNote: '',
    downloadUrl: '',
    wgtUrl: '',
  });

  /**
   * Initialize form data
   * @param record
   */
  async function initFormData() {
    const appVersion = await queryAppVersion({ key: appKey });
    if (appVersion) {
      Object.assign(model, appVersion);
    }
  }

  /**
   * 提交saveVersion信息
   */
  function handleSubmit() {
    const form = unref(formRef);
    form.validate().then(async () => {
      let obj = toRaw(model);
      if (obj.appVersion.indexOf('.') != -1) {
        obj.versionNum = Number(obj.appVersion.replace(/\./g, ''));
      }
      obj.id = appKey;
      confirmLoading.value = true;
      await saveAppVersion(obj);
      createMessage.success('save成功');
      confirmLoading.value = false;
      active.value = false;
    });
  }

  /**
   * 显示set up弹窗
   * @param type
   */
  function showUploadModal(type) {
    uploadType.value = type;
    modalValue.value = type == 'apk' ? model.downloadUrl : model.wgtUrl;
    openModal(true, {
      maxCount: 1,
      bizPath: filePath,
    });
  }

  /**
   *Upload return
   */
  function uploadBack(value) {
    if (unref(uploadType) == 'apk') {
      model.downloadUrl = value;
    } else {
      model.wgtUrl = value;
    }
  }
  //Form validation rules
  const validatorRules = {
    appVersion: [{ required: true, message: 'Versioncannot be empty', trigger: 'blur' }],
    downloadUrl: [{ required: true, message: 'APPInstallapkcannot be empty', trigger: 'change' }],
    wgtUrl: [{ required: true, message: 'APPhot update filecannot be empty', trigger: 'change' }],
  };
  // Show fields
  const schema: DescItem[] = [
    {
      field: 'appVersion',
      label: 'Version',
    },
    {
      field: 'downloadUrl',
      label: 'APPInstallapk',
    },
    {
      field: 'wgtUrl',
      label: 'APPhot update file',
    },
    {
      field: 'updateNote',
      label: 'Update content',
    },
  ];

  onMounted(() => {
    initFormData();
  });
</script>

<style scoped>
  .anty-form-btn {
    width: 100%;
    text-align: center;
  }
  .anty-form-btn button {
    margin: 20px;
  }
  .approveDiv span {
    margin: 0 20px;
  }
  .desc {
    width: 80%;
    margin: 0 auto;
  }

  :deep(.ant-descriptions-item-label) {
    width: 30% !important;
    min-width: 150px !important;
  }
  :deep(.ant-descriptions-item-content) {
    padding: 16px !important;
    width: 60% !important;
  }
</style>
