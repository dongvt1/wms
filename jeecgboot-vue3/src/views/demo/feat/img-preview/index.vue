<template>
  <PageWrapper title="Image preview example">
    <h1>There is a preview image</h1>
    <ImagePreview :imageList="imgList" />
    <a-divider />
    <h1>No preview image</h1>
    <a-button @click="openImg" type="primary">Click to preview</a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { createImgPreview, ImagePreview } from '/@/components/Preview/index';
  import { PageWrapper } from '/@/components/Page';
  import { ImageProps } from '/@/components/Preview/src/typing';

  const imgList: ImageProps[] = [
    { src: 'https://jeecgos.oss-cn-beijing.aliyuncs.com/upload/test/login_1658829954004.png', width: 300 },
    { src: 'https://jeecgos.oss-cn-beijing.aliyuncs.com/upload/test/home_index_1658830084684.png', width: 300 },
    { src: 'https://jeecgos.oss-cn-beijing.aliyuncs.com/upload/test/design_1658830200539.png', width: 300 },
    { src: 'https://static.jeecg.com/upload/test/13_1592320121058.png', width: 300 },
    { src: 'https://static.jeecg.com/upload/test/16_1592320251436.png', width: 300 },
  ];
  export default defineComponent({
    components: { PageWrapper, ImagePreview },
    setup() {
      function openImg() {
        const onImgLoad = ({ index, url, dom }) => {
          console.log(`No.${index + 1}images loaded，URLfor：${url}`, dom);
        };
        // Can be usedcreateImgPreviewreturned PreviewActions to control the preview logic，Implement a similar slideshow、Naughty operations such as automatic rotation
        let imageList = imgList.map<string>((i) => i.src);
        createImgPreview({ imageList: imageList, defaultWidth: 700, rememberState: true, onImgLoad });
      }

      return { imgList, openImg };
    },
  });
</script>
