<template>
  <PageWrapper title="Order number：234231029431" contentBackground>
    <template #extra>
      <a-button> Operation one </a-button>
      <a-button> Operation 2 </a-button>
      <a-button type="primary"> main operation </a-button>
    </template>

    <template #footer>
      <a-tabs default-active-key="1">
        <a-tab-pane key="1" tab="Details" />
        <a-tab-pane key="2" tab="rule" />
      </a-tabs>
    </template>

    <div class="pt-4 m-4 desc-wrap">
      <a-descriptions size="small" :column="2">
        <a-descriptions-item label="Creator"> Qu Lili </a-descriptions-item>
        <a-descriptions-item label="Order products"> XX Serve </a-descriptions-item>
        <a-descriptions-item label="creation time"> 2017-01-10 </a-descriptions-item>
        <a-descriptions-item label="Related documents">
          <a>12421</a>
        </a-descriptions-item>
        <a-descriptions-item label="Effective date"> 2017-07-07 ~ 2017-08-08 </a-descriptions-item>
        <a-descriptions-item label="Remark"> Please confirm within two working days </a-descriptions-item>
      </a-descriptions>
      <a-card title="Process progress" :bordered="false">
        <a-steps :current="1" progress-dot size="small">
          <a-step title="Create project">
            <template #description> <div>Jeecg</div> <p>2016-12-12 12:32</p> </template>
          </a-step>
          <a-step title="Department preliminary review">
            <template #description>
              <p>Chad</p>
            </template>
          </a-step>
          <a-step title="financial review" />
          <a-step title="Finish" />
        </a-steps>
      </a-card>

      <a-card title="User information" :bordered="false" class="mt-5">
        <a-descriptions :column="3">
          <a-descriptions-item label="User name"> Fu Xiaoxiao </a-descriptions-item>
          <a-descriptions-item label="Membership card number"> XX 32943898021309809423 </a-descriptions-item>
          <a-descriptions-item label="ID card"> 3321944288191034921 </a-descriptions-item>
          <a-descriptions-item label="Contact information"> 18100000000 </a-descriptions-item>
          <a-descriptions-item label="Contact address" :span="2"> Qu Lili 18100000000 Intersection of Huanggushan Road, Gongzhuan Road, Xihu District, Hangzhou City, Zhejiang Province </a-descriptions-item>
        </a-descriptions>

        <a-descriptions title="message group" :column="3">
          <a-descriptions-item label="XX data"> 111 </a-descriptions-item>
          <a-descriptions-item label="The data update time"> 2017-08-08 </a-descriptions-item>
          <a-descriptions-item label="XX data"> 725 </a-descriptions-item>
          <a-descriptions-item label="The data update time"> 2017-08-08 </a-descriptions-item>
        </a-descriptions>

        <h4>message group</h4>
        <a-card title="多层级message group">
          <a-descriptions title="Group name" :column="3">
            <a-descriptions-item label="person in charge"> Lin Dongdong </a-descriptions-item>
            <a-descriptions-item label="role code"> 1234567 </a-descriptions-item>
            <a-descriptions-item label="Department"> XXcompany - YYdepartment </a-descriptions-item>
            <a-descriptions-item label="Expiration time"> 2017-08-08 </a-descriptions-item>
            <a-descriptions-item label="describe" :span="2"> 这段describe很长很长很长很长很长很长很长很长很长很长很长很长很长很长... </a-descriptions-item>
          </a-descriptions>
          <a-divider />
          <a-descriptions title="Group name" :column="1">
            <a-descriptions-item label="scientific name">
              Citrullus lanatus (Thunb.) Matsum. et Nakaiannual creeping vine；stem、Strong branches，with obvious edges。tendrils thicker..
            </a-descriptions-item>
          </a-descriptions>
          <a-divider />
          <a-descriptions title="Group name" :column="1">
            <a-descriptions-item label="person in charge"> Fu Xiaoxiao </a-descriptions-item>
            <a-descriptions-item label="role code"> 1234568 </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-card>
      <a-card title="User call records in the past six months" class="my-5">
        <Empty />
      </a-card>
      <BasicTable @register="registerTimeTable" />
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { Divider, Card, Empty, Descriptions, Steps, Tabs } from 'ant-design-vue';

  import { refundTimeTableSchema, refundTimeTableData } from './data';
  export default defineComponent({
    components: {
      BasicTable,
      PageWrapper,
      [Divider.name]: Divider,
      [Card.name]: Card,
      Empty,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
      [Steps.name]: Steps,
      [Steps.Step.name]: Steps.Step,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
    },
    setup() {
      const [registerTimeTable] = useTable({
        title: 'Return progress',
        columns: refundTimeTableSchema,
        pagination: false,
        dataSource: refundTimeTableData,
        showIndexColumn: false,
        scroll: { y: 300 },
      });

      return {
        registerTimeTable,
      };
    },
  });
</script>
