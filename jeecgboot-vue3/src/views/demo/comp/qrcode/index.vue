<template>
  <PageWrapper title="QR code component usage example">
    <div class="flex flex-wrap">
      <CollapseContainer title="Basic example" :canExpan="true" class="text-center mb-6 qrcode-demo-item">
        <QrCode :value="qrCodeUrl" />
      </CollapseContainer>

      <CollapseContainer title="rendered intoimgLabel example" class="text-center mb-6 qrcode-demo-item">
        <QrCode :value="qrCodeUrl" tag="img" />
      </CollapseContainer>

      <CollapseContainer title="Configuration style example" class="text-center mb-6 qrcode-demo-item">
        <QrCode
          :value="qrCodeUrl"
          :options="{
            color: { dark: '#55D187' },
          }"
        />
      </CollapseContainer>

      <CollapseContainer title="locallogoExample" class="text-center mb-6 qrcode-demo-item">
        <QrCode :value="qrCodeUrl" :logo="LogoImg" />
      </CollapseContainer>

      <CollapseContainer title="onlinelogoExample" class="text-center mb-6 qrcode-demo-item">
        <QrCode
          :value="qrCodeUrl"
          logo="http://jeecg.com/images/logo.png"
          :options="{
            color: { dark: '#55D187' },
          }"
        />
      </CollapseContainer>

      <CollapseContainer title="logo配置Example" class="text-center mb-6 qrcode-demo-item">
        <QrCode
          :value="qrCodeUrl"
          :logo="{
            src: 'http://jeecg.com/images/logo.png',
            logoSize: 0.2,
            borderSize: 0.05,
            borderRadius: 50,
            bgColor: 'blue',
          }"
        />
      </CollapseContainer>

      <CollapseContainer title="downloadExample" class="text-center qrcode-demo-item">
        <QrCode :value="qrCodeUrl" ref="qrRef" :logo="LogoImg" />
        <a-button class="mb-2" type="primary" @click="download"> download </a-button>
        <div class="msg"> (onlinelogoWill cause the image to cross domain，需要download图片需要自行解决跨域问题) </div>
      </CollapseContainer>

      <CollapseContainer title="配置大小Example" class="text-center qrcode-demo-item">
        <QrCode :value="qrCodeUrl" :width="300" />
      </CollapseContainer>

      <CollapseContainer title="扩展绘制Example" class="text-center qrcode-demo-item">
        <QrCode :value="qrCodeUrl" :width="200" :options="{ margin: 5 }" ref="qrDiyRef" :logo="LogoImg" @done="onQrcodeDone" />
        <a-button class="mb-2" type="primary" @click="downloadDiy"> download </a-button>
        <div class="msg"> To perform extended drawing, you cannottagset toimg </div>
      </CollapseContainer>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { QrCode, QrCodeActionType } from '/@/components/Qrcode/index';
  import LogoImg from '/@/assets/images/logo.png';
  import { CollapseContainer } from '/@/components/Container/index';
  import { PageWrapper } from '/@/components/Page';

  const qrCodeUrl = 'https://www.vvbin.cn';
  export default defineComponent({
    components: { CollapseContainer, QrCode, PageWrapper },
    setup() {
      const qrRef = ref<Nullable<QrCodeActionType>>(null);
      const qrDiyRef = ref<Nullable<QrCodeActionType>>(null);
      function download() {
        const qrEl = unref(qrRef);
        if (!qrEl) return;
        qrEl.download('file name');
      }
      function downloadDiy() {
        const qrEl = unref(qrDiyRef);
        if (!qrEl) return;
        qrEl.download('Qrcode');
      }

      function onQrcodeDone({ ctx }: any) {
        if (ctx instanceof CanvasRenderingContext2D) {
          // extra draw
          ctx.fillStyle = 'black';
          ctx.font = '16px "Microsoft Yahei"';
          ctx.textBaseline = 'bottom';
          ctx.textAlign = 'center';
          ctx.fillText('You are handsome, scan first', 100, 195, 200);
        }
      }
      return {
        onQrcodeDone,
        qrCodeUrl,
        LogoImg,
        download,
        downloadDiy,
        qrRef,
        qrDiyRef,
      };
    },
  });
</script>
<style scoped>
  .qrcode-demo-item {
    width: 30%;
    margin-right: 1%;
  }
</style>
