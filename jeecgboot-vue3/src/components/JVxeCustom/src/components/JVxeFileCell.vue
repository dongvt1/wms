<template>
  <div>
    <template v-if="hasFile" v-for="(file, fileKey) of [innerFile || {}]" :key="fileKey">
      <div style="position: relative">
        <a-tooltip v-if="file.status === 'uploading'" :title="`Uploading(${Math.floor(file.percent)}%)`">
          <LoadingOutlined />
          <span style="margin-left: 5px">Uploading…</span>
        </a-tooltip>

        <a-tooltip v-else-if="file.status === 'done'" :title="file.name">
          <Icon icon="ant-design:paper-clip" />
          <span style="margin-left: 5px">{{ ellipsisFileName }}</span>
        </a-tooltip>

        <a-tooltip v-else :title="file.message || 'Upload failed'">
          <Icon icon="ant-design:exclamation-circle" style="color: red" />
          <span style="margin-left: 5px">{{ ellipsisFileName }}</span>
        </a-tooltip>

        <Dropdown :trigger="['click']" placement="bottomRight" style="margin-left: 10px">
          <a-tooltip title="operate">
            <Icon v-if="file.status !== 'uploading'" icon="ant-design:setting" style="cursor: pointer" />
          </a-tooltip>
          <template #overlay>
            <a-menu>
              <a-menu-item v-if="originColumn.allowDownload !== false" @click="handleClickDownloadFile">
                <span><Icon icon="ant-design:download" />&nbsp;download</span>
              </a-menu-item>
              <a-menu-item :disabled="cellProps.disabled" v-if="originColumn.allowRemove !== false" @click="handleClickDeleteFile">
                <span><Icon icon="ant-design:delete" />&nbsp;delete</span>
              </a-menu-item>
              <a-menu-item :disabled="cellProps.disabled" @click="handleMoreOperation">
                <span><Icon icon="ant-design:bars" />&nbsp;More</span>
              </a-menu-item>
            </a-menu>
          </template>
        </Dropdown>
      </div>
    </template>

    <a-upload
      v-if="!cellProps.disabledTable"
      v-show="!hasFile"
      name="file"
      :data="{ isup: 1 }"
      :multiple="false"
      :action="uploadAction"
      :headers="uploadHeaders"
      :showUploadList="false"
      v-bind="cellProps"
      @change="handleChangeUpload"
    >
      <a-button preIcon="ant-design:upload">{{ originColumn.btnText || 'Click to upload' }}</a-button>
    </a-upload>
    <JUploadModal :value="modalValue" @register="registerModel" @change="onModalChange" />
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { useFileCell, enhanced, components } from '../hooks/useFileCell';

  export default defineComponent({
    name: 'JVxeFileCell',
    components: components,
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      return useFileCell(props, UploadTypeEnum.file);
    },
    // 【Component enhancement】See notes for details：JVxeComponent.Enhanced
    enhanced: enhanced,
  });
</script>

<style scoped lang="less"></style>
