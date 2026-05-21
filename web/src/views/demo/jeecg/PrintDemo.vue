<template>
  <PageWrapper>
    <a-card :bordered="false" class="j-print-demo">
      <div style="text-align: right">
        <a-button type="primary" ghost @click="onPrint">Print</a-button>
      </div>
      <section ref="print" id="printContent">
        <div style="text-align: center">
          <p style="font-size: 24px; font-weight: 800">Print测试表单</p>
        </div>
        <!--signature-->
        <a-col :md="24" :sm="24">
          <div class="sign" style="text-align: center; height: inherit">
            <a-col :span="24">
              <span>Print人员:</span>
              <a-input style="width: 30%" v-model:value="model.printer" />
              <span style="margin-left: 12.5%">Print日期:</span>
              <a-input style="width: 30%" v-model:value="model.printTime" />
            </a-col>
            <a-col :span="24"> </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print内容:</span>
              <a-input style="width: 80%" v-model:value="model.printContent" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的1:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的2:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的3:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的4:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的5:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的6:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的7:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的8:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的9:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的10:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的11:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的12:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的13:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col :span="24" style="margin-top: 20px">
              <span>Print目的14:</span>
              <a-input style="width: 80%" v-model:value="model.printReason" />
            </a-col>
            <a-col style="margin-top: 20px" :span="24">
              <span>Print图片:</span>
              <br />
              <a-upload
                action="/jsonplaceholder.typicode.com/posts/"
                listType="picture-card"
                :fileList="model.fileList"
                @preview="handlePreview"
                @change="handleChange"
              >
                <div v-if="model.fileList.length < 3">
                  <Icon icon="ant-design:plus-outlined" />
                  <div class="ant-upload-text">Upload</div>
                </div>
              </a-upload>
              <a-modal :open="previewVisible" :footer="null" @cancel="previewVisible = false">
                <img alt="example" style="width: 100%" :src="previewImage" />
              </a-modal>
            </a-col>
          </div>
        </a-col>
      </section>
    </a-card>
  </PageWrapper>
</template>
<script lang="ts">
  import { ref, reactive } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import Icon from '/@/components/Icon/src/Icon.vue';
  import { printJS } from '/@/hooks/web/usePrintJS';

  export default {
    name: 'PrintDemo',
    components: { PageWrapper, Icon },
    props: {
      reBizCode: {
        type: String,
        default: '',
      },
    },
    setup() {
      const model = reactive({
        printer: 'Zhang San',
        printTime: '2021-12-31 23:59:59',
        printContent: 'Print内容：这是一个Print测试！',
        printReason: '做一个Print测试',
        fileList: [
          {
            uid: '-1',
            name: 'xxx.png',
            status: 'done',
            url: 'https://zos.alipayobjects.com/rmsportal/jkjgkEfvpUPVyRjUImniVslZfWPnJuuZ.png',
          },
          {
            uid: '-2',
            name: 'pic1.png',
            status: 'done',
            url: 'https://www.gizbot.com/img/2016/11/whatsapp-error-lead-image-08-1478607387.jpg',
          },
        ],
      });
      const previewImage = ref('');
      const previewVisible = ref(false);

      function onPrint() {
        printJS({
          printable: '#printContent',
          type: 'html',
        });
      }

      function handlePreview(file) {
        previewImage.value = file.url || file.thumbUrl;
        previewVisible.value = true;
      }

      function handleChange({ fileList }) {
        model.fileList = fileList;
      }

      return {
        model,
        previewImage,
        previewVisible,
        onPrint,
        handlePreview,
        handleChange,
      };
    },
  };
</script>
<style lang="less" scoped>
  .j-print-demo .ant-card-body {
    margin-left: 0;
    margin-right: 0;
    margin-bottom: 1%;
    border: 0 solid black;
    min-width: 800px;
    color: #000000 !important;
  }

  .sign .ant-input {
    font-weight: bolder;
    text-align: center;
    border-left-width: 0 !important;
    border-top-width: 0 !important;
    border-right-width: 0 !important;
    outline: none !important;
    box-shadow: none !important;
  }

  /* you can make up upload button and sample style by using stylesheets */
  .ant-upload-select-picture-card i {
    font-size: 32px;
    color: #999;
  }

  .ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
  }
</style>
