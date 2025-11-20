<template>
  <div class="m-5 result-success">
    <Result
      status="success"
      title="Submission successful"
      sub-title="The submission result page is used to feedback the processing results of a series of operation tasks.， If it is just a simple operation，use Message Just provide global prompt feedback。 This text area can display simple supplementary instructions，If there is a similar display “Document”needs，The gray area below can present more complex content.。"
    >
      <template #extra>
        <a-button key="console" type="primary"> Return to list </a-button>
        <a-button key="buy"> View items </a-button>
        <a-button key="buy"> Print </a-button>
      </template>
    </Result>
    <div class="result-success__content">
      <Descriptions title="Project name">
        <DescriptionItem label="project ID"> 111222 </DescriptionItem>
        <DescriptionItem label="person in charge"> Jeecg </DescriptionItem>
        <DescriptionItem label="Effective time"> 2016-12-12 ~ 2017-12-12 </DescriptionItem>
      </Descriptions>
      <Steps :current="1" progress-dot size="small">
        <Step title="创建project">
          <template #description> <div>Jeecg</div> <p>2016-12-12 12:32</p> </template>
        </Step>
        <Step title="Department preliminary review">
          <template #description>
            <p>Chad</p>
          </template>
        </Step>
        <Step title="financial review" />
        <Step title="Finish" />
      </Steps>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Result, Steps, Descriptions } from 'ant-design-vue';
  export default defineComponent({
    components: {
      Result,
      Steps,
      Step: Steps.Step,
      Descriptions,
      DescriptionItem: Descriptions.Item,
    },
  });
</script>
<style lang="less" scoped>
  .result-success {
    padding: 48px 32px;
    background-color: @component-background;

    &__content {
      padding: 24px 40px;
      background-color: @background-color-light;
    }
  }
</style>
