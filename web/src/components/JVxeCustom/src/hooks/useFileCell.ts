import { computed } from 'vue';
import { fileGetValue, fileSetValue, useJVxeUploadCell } from '/@/components/jeecg/JVxeTable/src/hooks/cells/useJVxeUploadCell';
import { uploadUrl } from '/@/api/common/api';
import { JUploadModal, UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';
import { useModal } from '/@/components/Modal';
import { JVxeComponent } from '/@/components/jeecg/JVxeTable/src/types/JVxeComponent';
import { Icon } from '/@/components/Icon';
import { Dropdown } from 'ant-design-vue';
import { LoadingOutlined } from '@ant-design/icons-vue';

export function useFileCell(props, fileType: UploadTypeEnum, options?) {
  const setup = useJVxeUploadCell(props, { token: true, action: uploadUrl, ...options });

  const { innerFile, handleChangeCommon, originColumn } = setup;
  const [registerModel, { openModal }] = useModal();

  // Intercept file name
  const ellipsisFileName = computed(() => {
    let length = 5;
    let file = innerFile.value;
    if (!file || !file.name) {
      return '';
    }
    if (file.name.length > length) {
      return file.name.substr(0, length) + '…';
    }
    return file.name;
  });

  const modalValue = computed(() => {
    if (innerFile.value) {
      if (innerFile.value['url']) {
        return innerFile.value['url'];
      } else if (innerFile.value['path']) {
        return innerFile.value['path'];
      }
    }
    return '';
  });

  const maxCount = computed(() => {
    let maxCount = originColumn.value.maxCount;
    // online ExpandJSON
    if (originColumn.value && originColumn.value.fieldExtendJson) {
      let json = JSON.parse(originColumn.value.fieldExtendJson);
      maxCount = json.uploadnum ? json.uploadnum : 0;
    }
    return maxCount ?? 0;
  });

  // Click the more button
  function handleMoreOperation() {
    openModal(true, {
      removeConfirm: true,
      mover: true,
      download: true,
      ...originColumn.value.props,
      maxCount: maxCount.value,
      fileType: fileType,
      action: originColumn.value.action ?? void 0,
    });
  }

  // More upload callbacks
  function onModalChange(path) {
    if (path) {
      // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-235】The text of the image upload button cannot be seen clearly when rich text is disabled.
      if (innerFile.value === null) {
        innerFile.value = {};
      }
      // update-end-author:liaozhiyang---date:20240524---for：【TV360X-235】The text of the image upload button cannot be seen clearly when rich text is disabled.
      innerFile.value.path = path;
      handleChangeCommon(innerFile.value);
    } else {
      //update-begin-author:liusq date:2023-06-05 for: [issues/530]JVxeTable ofJVxeTypes.imagetype，Unable to delete all uploaded images
      handleChangeCommon(null);
      //update-end-author:liusq date:2023-06-05 for:  [issues/530]JVxeTable ofJVxeTypes.imagetype，Unable to delete all uploaded images
    }
  }

  return {
    ...setup,
    modalValue,
    maxCount,
    ellipsisFileName,
    registerModel,
    onModalChange,
    handleMoreOperation,
  };
}

export const components = {
  Icon,
  Dropdown,
  LoadingOutlined,
  JUploadModal,
};

export const enhanced = {
  switches: { visible: true },
  getValue: (value) => fileGetValue(value),
  setValue: (value) => fileSetValue(value),
} as JVxeComponent.EnhancedPartial;
