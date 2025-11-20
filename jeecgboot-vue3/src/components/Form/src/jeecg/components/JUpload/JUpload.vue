<template>
  <div ref="containerRef" :class="`${prefixCls}-container`">
    <a-upload
      :headers="headers"
      :multiple="multiple"
      :action="uploadUrl"
      :fileList="fileList"
      :disabled="disabled"
      v-bind="bindProps"
      @remove="onRemove"
      @change="onFileChange"
      @preview="onFilePreview"
    >
      <template v-if="isImageMode">
        <div v-if="!isMaxCount">
          <Icon icon="ant-design:plus-outlined" />
          <div class="ant-upload-text">{{ text }}</div>
        </div>
      </template>
      <a-button v-else-if="buttonVisible" :disabled="buttonDisabled">
        <Icon icon="ant-design:upload-outlined" />
        <span>{{ text }}</span>
      </a-button>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch, nextTick, createApp,unref } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { getToken } from '/@/utils/auth';
  import { uploadUrl } from '/@/api/common/api';
  import { propTypes } from '/@/utils/propTypes';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { createImgPreview } from '/@/components/Preview/index';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { UploadTypeEnum } from './upload.data';
  import { getFileAccessHttpUrl, getHeaders } from '/@/utils/common/compUtils';
  import UploadItemActions from './components/UploadItemActions.vue';
  import { split } from '/@/utils/index';

  const { createMessage, createConfirm } = useMessage();
  const { prefixCls } = useDesign('j-upload');
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value']);
  const props = defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    text: propTypes.string.def('upload'),
    fileType: propTypes.string.def(UploadTypeEnum.all),
    /*这个属性用于控制文件upload的业务路径*/
    bizPath: propTypes.string.def('temp'),
    /**
     * Whether to returnurl，
     * true：Return onlyurl
     * false：returnfileName filePath fileSize
     */
    returnUrl: propTypes.bool.def(true),
    // 最大upload数量
    maxCount: propTypes.number.def(0),
    buttonVisible: propTypes.bool.def(true),
    multiple: propTypes.bool.def(true),
    // Whether to display left and right movement buttons
    mover: propTypes.bool.def(true),
    // Whether to display the download button
    download: propTypes.bool.def(true),
    // Whether to display a confirmation box when deleting
    removeConfirm: propTypes.bool.def(false),
    beforeUpload: propTypes.func,
    disabled: propTypes.bool.def(false),
    // Replace previous file，用于超出最大数量依然允许upload
    replaceLastOne: propTypes.bool.def(false),
  });

  const headers = getHeaders();
  const fileList = ref<any[]>([]);
  const uploadGoOn = ref<boolean>(true);
  // refs
  const containerRef = ref();
  // 是否达到了最大upload数量
  const isMaxCount = computed(() => props.maxCount > 0 && fileList.value.length >= props.maxCount);
  // 当前是否是uploadpicture模式
  const isImageMode = computed(() => props.fileType === UploadTypeEnum.image);
  // upload按钮是否禁用
  const buttonDisabled = computed(()=>{
    if(props.disabled === true){
      return true;
    }
    if(isMaxCount.value === true){
      if(props.replaceLastOne === true){
        return false
      }else{
        return true;
      }
    }
    return false
  });
  // merge props and attrs
  const bindProps = computed(() => {
    //update-begin-author:liusq date:20220411 for: [issue/455]uploadcomponents传入accept限制upload文件类型无效
    const bind: any = Object.assign({}, props, unref(attrs));
    //update-end-author:liusq date:20220411 for: [issue/455]uploadcomponents传入accept限制upload文件类型无效

    bind.name = 'file';
    bind.listType = isImageMode.value ? 'picture-card' : 'text';
    bind.class = [bind.class, { 'upload-disabled': props.disabled }];
    bind.data = { biz: props.bizPath, ...bind.data };
    //update-begin-author:taoyan date:20220407 for: CustomizebeforeUpload return false，并不能中断upload过程
    if (!bind.beforeUpload) {
      bind.beforeUpload = onBeforeUpload;
    }
    //update-end-author:taoyan date:20220407 for: CustomizebeforeUpload return false，并不能中断upload过程
    // 如果当前是pictureupload模式，就只能uploadpicture
    if (isImageMode.value && !bind.accept) {
      bind.accept = 'image/*';
    }
    return bind;
  });

  watch(
    () => props.value,
    (val) => {
      if (Array.isArray(val)) {
        if (props.returnUrl) {
          parsePathsValue(val.join(','));
        } else {
          parseArrayValue(val);
        }
      } else {
        //update-begin---author:liusq ---date:20230914  for：[issues/5327]UploadcomponentsreturnUrlforfalse时upload的字段值return了一个'[object Object]' ------------
        if (props.returnUrl) {
          parsePathsValue(val);
        } else {
          val && parseArrayValue(JSON.parse(val));
        }
        //update-end---author:liusq ---date:20230914  for：[issues/5327]UploadcomponentsreturnUrlforfalse时upload的字段值return了一个'[object Object]' ------------
      }
    },
    { immediate: true }
  );

  watch(fileList, () => nextTick(() => addActionsListener()), { immediate: true });

  const antUploadItemCls = 'ant-upload-list-item';

  // Listener
  function addActionsListener() {
    if (!isImageMode.value) {
      return;
    }
    const uploadItems = containerRef.value ? containerRef.value.getElementsByClassName(antUploadItemCls) : null;
    if (!uploadItems || uploadItems.length === 0) {
      return;
    }
    for (const uploadItem of uploadItems) {
      let hasActions = uploadItem.getAttribute('data-has-actions') === 'true';
      if (!hasActions) {
        uploadItem.addEventListener('mouseover', onAddActionsButton);
      }
    }
  }

  // Add buttons that can move left and right
  function onAddActionsButton(event) {
    const getUploadItem = () => {
      for (const path of event.path) {
        if (path.classList.contains(antUploadItemCls)) {
          return path;
        } else if (path.classList.contains(`${prefixCls}-container`)) {
          return null;
        }
      }
      return null;
    };
    const uploadItem = getUploadItem();
    if (!uploadItem) {
      return;
    }
    const actions = uploadItem.getElementsByClassName('ant-upload-list-item-actions');
    if (!actions || actions.length === 0) {
      return;
    }
    // Add action button
    const div = document.createElement('div');
    div.className = 'upload-actions-container';
    createApp(UploadItemActions, {
      element: uploadItem,
      fileList: fileList,
      mover: props.mover,
      download: props.download,
      emitValue: emitValue,
    }).mount(div);
    actions[0].appendChild(div);
    uploadItem.setAttribute('data-has-actions', 'true');
    uploadItem.removeEventListener('mouseover', onAddActionsButton);
  }

  // Parse comma-separated database storage
  function parsePathsValue(paths) {
    if (!paths || paths.length == 0) {
      fileList.value = [];
      return;
    }
    let list: any[] = [];
    // update-begin--author:liaozhiyang---date:20250325---for：【issues/7990】If the image parameters contain commas, they will be mistakenly recognized as multiple images.
    const result = split(paths);
    // update-end--author:liaozhiyang---date:20250325---for：【issues/7990】If the image parameters contain commas, they will be mistakenly recognized as multiple images.
    for (const item of result) {
      let url = getFileAccessHttpUrl(item);
      list.push({
        uid: uidGenerator(),
        name: getFileName(item),
        status: 'done',
        url: url,
        response: { status: 'history', message: item },
      });
    }
    fileList.value = list;
  }

  // Parse array values
  function parseArrayValue(array) {
    if (!array || array.length == 0) {
      fileList.value = [];
      return;
    }
    let list: any[] = [];
    for (const item of array) {
      let url = getFileAccessHttpUrl(item.filePath);
      list.push({
        uid: uidGenerator(),
        name: item.fileName,
        url: url,
        status: 'done',
        response: { status: 'history', message: item.filePath },
      });
    }
    fileList.value = list;
  }

  // 文件upload之前的操作
  function onBeforeUpload(file) {
    uploadGoOn.value = true;
    if (isImageMode.value) {
      if (file.type.indexOf('image') < 0) {
        createMessage.warning('请uploadpicture');
        uploadGoOn.value = false;
        return false;
      }
    }
    // Expand beforeUpload verify
    if (typeof props.beforeUpload === 'function') {
      return props.beforeUpload(file);
    }
    return true;
  }

  // Delete handling event
  function onRemove() {
    if (props.removeConfirm) {
      return new Promise((resolve) => {
        createConfirm({
          title: 'delete',
          content: `确定要delete这${isImageMode.value ? 'pictures' : 'files'}?？`,
          iconType: 'warning',
          onOk: () => resolve(true),
          onCancel: () => resolve(false),
        });
      });
    }
    return true;
  }

  // uploadcomponentschangeevent
  function onFileChange(info) {
    if (!info.file.status && uploadGoOn.value === false) {
      info.fileList.pop();
    }
    let fileListTemp = info.fileList;
    // 限制最大upload数
    if (props.maxCount > 0) {
      let count = fileListTemp.length;
      if (count >= props.maxCount) {
        let diffNum = props.maxCount - fileListTemp.length;
        if (diffNum >= 0) {
          fileListTemp = fileListTemp.slice(-props.maxCount);
        } else {
          return;
        }
      }
    }
    if (info.file.status === 'done') {
      let successFileList = [];
      if (info.file.response.success) {
        successFileList = fileListTemp.map((file) => {
          if (file.response) {
            let reUrl = file.response.message;
            file.url = getFileAccessHttpUrl(reUrl);
          }
          return file;
        });
      }else{
        successFileList = fileListTemp.filter(item=>{
          return item.uid!=info.file.uid;
        });
        createMessage.error(`${info.file.name} upload失败.`);
      }
      fileListTemp = successFileList;
    } else if (info.file.status === 'error') {
      createMessage.error(`${info.file.name} upload失败.`);
    }
    // update-begin--author:liaozhiyang---date:20240628---for：【issues/1273】uploadcomponentsJUploadConfigurationbeforeUpload阻止了upload，Thumbnails are still displayed on the front-end page
    // beforeUpload returnfalse，then nostatus
    info.file.status && (fileList.value = fileListTemp);
    // update-end--author:liaozhiyang---date:20240628---for：【issues/1273】uploadcomponentsJUploadConfigurationbeforeUpload阻止了upload，Thumbnails are still displayed on the front-end page
    if (info.file.status === 'done' || info.file.status === 'removed') {
      //returnUrlfortrue时Return only文件路径
      if (props.returnUrl) {
        handlePathChange();
      } else {
        //returnUrlforfalse时return文件名称、File path and file size
        let newFileList: any[] = [];
        for (const item of fileListTemp) {
          if (item.status === 'done') {
            let fileJson = {
              fileName: item.name,
              filePath: item.response.message,
              fileSize: item.size,
            };
            newFileList.push(fileJson);
          }else{
            return;
          }
        }
        //update-begin---author:liusq ---date:20230914  for：[issues/5327]UploadcomponentsreturnUrlforfalse时upload的字段值return了一个'[object Object]' ------------
        emitValue(JSON.stringify(newFileList));
        //update-end---author:liusq ---date:20230914  for：[issues/5327]UploadcomponentsreturnUrlforfalse时upload的字段值return了一个'[object Object]' ------------
      }
    }
  }

  function handlePathChange() {
    let uploadFiles = fileList.value;
    let path = '';
    if (!uploadFiles || uploadFiles.length == 0) {
      path = '';
    }
    let pathList: string[] = [];
    for (const item of uploadFiles) {
      if (item.status === 'done') {
        pathList.push(item.response.message);
      } else {
        return;
      }
    }
    if (pathList.length > 0) {
      path = pathList.join(',');
    }
    emitValue(path);
  }

  // Preview file、picture
  function onFilePreview(file) {
    if (isImageMode.value) {
      createImgPreview({ imageList: [file.url], maskClosable: true });
    } else {
      window.open(file.url);
    }
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  function uidGenerator() {
    return '-' + parseInt(Math.random() * 10000 + 1, 10);
  }

  function getFileName(path) {
    if (path.lastIndexOf('\\') >= 0) {
      let reg = new RegExp('\\\\', 'g');
      path = path.replace(reg, '/');
    }
    return path.substring(path.lastIndexOf('/') + 1);
  }

  defineExpose({
    addActionsListener,
  });
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-j-upload';

  .@{prefix-cls} {
    &-container {
      position: relative;

      .upload-disabled {
        .ant-upload-list-item {
          .anticon-close {
            display: none;
          }

          .anticon-delete {
            display: none;
          }
        }

        /* update-begin-author:taoyan date:2022-5-24 for:VUEN-1093Details interface picture下载按钮显示不全*/
        .upload-download-handler {
          right: 6px !important;
        }
        /* update-end-author:taoyan date:2022-5-24 for:VUEN-1093Details interface picture下载按钮显示不全*/
      }
      .ant-upload-text-icon {
        color: @primary-color;
      }
      .ant-upload-list-item {
        .upload-actions-container {
          position: absolute;
          top: -31px;
          left: -18px;
          z-index: 11;
          width: 84px;
          height: 84px;
          line-height: 28px;
          text-align: center;
          pointer-events: none;

          a {
            opacity: 0.9;
            margin: 0 5px;
            cursor: pointer;
            transition: opacity 0.3s;

            .anticon {
              color: #fff;
              font-size: 16px;
            }

            &:hover {
              opacity: 1;
            }
          }

          .upload-mover-handler,
          .upload-download-handler {
            position: absolute;
            pointer-events: auto;
          }

          .upload-mover-handler {
            width: 100%;
            bottom: 0;
          }

          .upload-download-handler {
            top: -4px;
            right: -4px;
          }
        }
      }
    }
  }
</style>
