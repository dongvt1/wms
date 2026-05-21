<template>
  <a-form-item v-if="item.view === DateTypeEnum.Date" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <template v-if="single_mode === item.mode">
      <a-date-picker
        :showTime="false"
        valueFormat="YYYY-MM-DD"
        :placeholder="'Please select' + item.label"
        v-model:value="queryParam[item.field]"
      ></a-date-picker>
    </template>
    <template v-else>
      <a-date-picker
        :showTime="false"
        valueFormat="YYYY-MM-DD"
        placeholder="start date"
        v-model:value="queryParam[item.field + '_begin']"
        style="width: calc(50% - 15px)"
      ></a-date-picker>
      <span class="group-query-strig">~</span>
      <a-date-picker
        :showTime="false"
        valueFormat="YYYY-MM-DD"
        placeholder="end date"
        v-model:value="queryParam[item.field + '_end']"
        style="width: calc(50% - 15px)"
      ></a-date-picker>
    </template>
  </a-form-item>

  <a-form-item v-else-if="item.view === DateTypeEnum.Datetime" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label :title="item.label">
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <template v-if="single_mode === item.mode">
      <a-date-picker
        :placeholder="'Please select' + item.label"
        :show-time="true"
        valueFormat="YYYY-MM-DD HH:mm:ss"
        v-model:value="queryParam[item.field]"
      ></a-date-picker>
    </template>
    <template v-else>
      <a-date-picker
        placeholder="Select start time"
        :show-time="true"
        valueFormat="YYYY-MM-DD HH:mm:ss"
        v-model:value="queryParam[item.field + '_begin']"
        style="width: calc(50% - 9px); min-width: 60px"
      ></a-date-picker>
      <span class="group-query-strig" style="width: auto; padding: 0 4px">~</span>
      <a-date-picker
        placeholder="Select end time"
        :show-time="true"
        valueFormat="YYYY-MM-DD HH:mm:ss"
        v-model:value="queryParam[item.field + '_end']"
        style="width: calc(50% - 9px); min-width: 60px"
      ></a-date-picker>
    </template>
  </a-form-item>

  <a-form-item v-else-if="item.view === DateTypeEnum.Time" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <template v-if="single_mode === item.mode">
      <a-date-picker :placeholder="'Please select' + item.label" mode="time" valueFormat="HH:mm:ss" v-model:value="queryParam[item.field]"></a-date-picker>
    </template>
    <template v-else>
      <a-date-picker
        placeholder="Please select开始时间"
        mode="time"
        valueFormat="HH:mm:ss"
        v-model:value="queryParam[item.field + '_begin']"
        style="width: calc(50% - 15px)"
      ></a-date-picker>
      <span class="group-query-strig">~</span>
      <a-date-picker
        placeholder="Please select结束时间"
        mode="time"
        valueFormat="HH:mm:ss"
        v-model:value="queryParam[item.field + '_end']"
        style="width: calc(50% - 15px)"
      ></a-date-picker>
    </template>
  </a-form-item>

  <a-form-item
    v-else-if="item.view === CompTypeEnum.List || item.view === CompTypeEnum.Radio || item.view === CompTypeEnum.Switch"
    :labelCol="labelCol"
    :class="'jeecg-online-search'"
  >
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JDictSelectTag v-if="item.config === '1'" :placeholder="'Please select' + item.label" v-model="queryParam[item.field]" :dictCode="getDictCode(item)">
    </JDictSelectTag>
    <a-select v-else :placeholder="'Please select' + item.label" v-model:value="queryParam[item.field]">
      <template v-for="(obj, index) in dictOptions[getDictOptionKey(item)]" :key="index">
        <a-select-option :value="obj.value"> {{ obj.text }}</a-select-option>
      </template>
    </a-select>
  </a-form-item>

  <a-form-item v-else-if="item.view === CompTypeEnum.SelTree" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JTreeSelect
      :placeholder="'Please select' + item.label"
      v-model:value="queryParam[item.field]"
      :dict="item.dict"
      :pidField="item.pidField"
      :pidValue="item.pidValue"
      :hasChildField="item.hasChildField"
      load-triggle-change
    >
    </JTreeSelect>
  </a-form-item>

  <a-form-item v-else-if="item.view === CompTypeEnum.CatTree" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JCategorySelect :pcode="item.pcode" v-model:value="queryParam[item.field]" :placeholder="'Please select' + item.label" />
  </a-form-item>

  <a-form-item v-else-if="item.view === CompTypeEnum.SelSearch" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JOnlineSearchSelect v-model:value="queryParam[item.field]" :placeholder="'Please select'+item.label" :fieldId="item.fieldId"/>
  </a-form-item>

  <a-form-item v-else-if="item.view === CompTypeEnum.SelUser" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JSelectUserByDept v-model:value="queryParam[item.field]" :placeholder="'Please select' + item.label"></JSelectUserByDept>
  </a-form-item>

  <a-form-item v-else-if="item.view == CompTypeEnum.SelDepart" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JSelectDept v-model:value="queryParam[item.field]" :placeholder="'Please select' + item.label" />
  </a-form-item>

  <a-form-item v-else-if="item.view === CompTypeEnum.Popup" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JPopup
      :placeholder="'Please select' + item.label"
      v-model:value="queryParam[item.field]"
      :formElRef="formElRef"
      :code="item.dictTable"
      :field-config="item.dictCode"
      :multi="true"
    />
  </a-form-item>

  <a-form-item v-else-if="item.view === CompTypeEnum.Pca" :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <JAreaLinkage :placeholder="'Please select' + item.label" v-model:value="queryParam[item.field]" />
  </a-form-item>
  <!--TODO Missing components-->
  <a-form-item
    v-else-if="item.view === CompTypeEnum.Checkbox || item.view === CompTypeEnum.ListMulti"
    :labelCol="labelCol"
    :label="item.label"
    :class="'jeecg-online-search'"
  >
    <!-- <j-select-multiple
                v-if="item.config==='1'"
                :placeholder=" 'Please select'+item.label "
                v-model="queryParam[item.field]"
                :dictCode="getDictCode(item)">
        </j-select-multiple>
        <j-select-multiple
                v-else
                :placeholder=" 'Please select'+item.label "
                :options="dictOptions[item.dbField]"
                v-model="queryParam[item.field]">
        </j-select-multiple>-->
  </a-form-item>

  <a-form-item v-else :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>
    <template v-if="single_mode === item.mode && 'string'== item.view">
      <j-input :placeholder="'Please enter' + item.label" v-model:value="queryParam[item.field]"></j-input>
    </template>
    <template v-else-if="single_mode === item.mode">
      <a-input :placeholder="'Please enter' + item.label" v-model:value="queryParam[item.field]"></a-input>
    </template>
    <template v-else>
      <a-input :placeholder="'Please enter开始' + item.label" v-model:value="queryParam[item.field + '_begin']" style="width: calc(50% - 15px)"></a-input>
      <span class="group-query-strig">~</span>
      <a-input :placeholder="'Please enter结束' + item.label" v-model:value="queryParam[item.field + '_end']" style="width: calc(50% - 15px)"></a-input>
    </template>
  </a-form-item>
</template>

<script lang="ts">
  //import JOnlineSearchSelect from '@/components/online/autoform/comp/JOnlineSearchSelect'
  import { defineComponent, ref } from 'vue';
  import { DateTypeEnum } from '/@/enums/DateTypeEnum.ts';
  import { CompTypeEnum } from '/@/enums/CompTypeEnum.ts';
  import { JDictSelectTag, JTreeSelect, JCategorySelect, JSelectUserByDept, JSelectDept, JPopup, JAreaLinkage,JInput,JSearchSelect } from '/@/components/Form';
  export default defineComponent({
    name: 'SearchFormItem',
    components: {
      //JOnlineSearchSelect
      JDictSelectTag,
      JTreeSelect,
      JCategorySelect,
      JSelectUserByDept,
      JSelectDept,
      JPopup,
      JAreaLinkage,
      JInput,
    },
    props: {
      formElRef: {
        type: Object,
        default: () => {},
      },
      item: {
        type: Object,
        default: () => {},
        required: true,
      },
      dictOptions: {
        type: Object,
        default: () => {},
        required: true,
      },
      queryParam: {
        type: Object,
        default: () => {},
        required: true,
      },
    },
    setup(props) {
      const single_mode = ref('single');
      console.log('dictOptions===>', props.dictOptions);
      function getDictCode(item) {
        if (item.dictTable && item.dictTable.length > 0) {
          return item.dictTable + ',' + item.dictText + ',' + item.dictCode;
        } else {
          return item.dictCode;
        }
      }

      function getSqlByDictCode(item) {
        let { dictTable, dictCode, dictText } = item;
        let temp = dictTable.toLowerCase();
        let arr = temp.split('where');
        let condition = '';
        if (arr.length > 1) {
          condition = ' where' + arr[1];
        }
        let sql = 'select ' + dictCode + " as 'value', " + dictText + " as 'text' from " + arr[0] + condition;
        console.log('sql', sql);
        return sql;
      }

      function getDictOptionKey(item) {
        if (item.dbField) {
          return item.dbField;
        } else {
          return item.field;
        }
      }

      // Define query conditions textlabelThe maximum width of Better than simply controlling the number of fonts
      const labelTextMaxWidth = '120px';
      const labelCol = {
        style: {
          'max-width': labelTextMaxWidth,
        },
      };
      return {
        labelTextMaxWidth,
        labelCol,
        single_mode,
        getDictOptionKey,
        getDictCode,
        getSqlByDictCode,
        DateTypeEnum,
        CompTypeEnum,
      };
    },
  });
</script>

<style lang="less" scoped>
  .group-query-strig {
    width: 30px;
    text-align: center;
    display: inline-block;
  }

  /* Query condition left alignment style setting */
  .jeecg-online-search :deep(.ant-form-item-label) {
    flex: 0 0 auto !important;
    width: auto;
  }
  .jeecg-online-search :deep(.ant-form-item-control) {
    max-width: 100%;
  }

  /* labeldisplay width beyond display... */
  .jeecg-online-search :deep(.label-text) {
    max-width: v-bind(labelTextMaxWidth);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow-wrap: break-word;
  }
</style>
