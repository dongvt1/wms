<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
          <a-row>
            <a-col :span="24">
              <a-form-item label="text" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.name">
                <a-input v-model:value="formState.name" placeholder="Please entertext"></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="password" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.miMa">
                <a-input-password v-model:value="formState.miMa" placeholder="Please enterpassword" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Dictionary drop down" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.xiala">
                <JDictSelectTag type="select" v-model:value="formState.xiala" dictCode="sex" placeholder="Please selectDictionary drop down" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Dictionary radio" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.danxuan">
                <JDictSelectTag type="radio" v-model:value="formState.danxuan" dictCode="sex" placeholder="Please selectDictionary radio" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Dictionary multiple choice" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.duoxuan">
                <JCheckbox v-model:value="formState.duoxuan" dictCode="urgent_level" placeholder="Please selectDictionary multiple choice" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="switch" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.kaiguan">
                <JSwitch v-model:value="formState.kaiguan" :options="['1', '0']"></JSwitch>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="date" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.riqi">
                <a-date-picker placeholder="Please selectdate" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" v-model:value="formState.riqi" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="year month day hour minute second" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.nyrsfm">
                <a-date-picker show-time v-model:value="formState.nyrsfm" style="width: 100%" valueFormat="YYYY-MM-DD HH:mm:ss" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="time" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.shijian">
                <TimePicker placeholder="Please selecttime" v-model:value="formState.shijian" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="document" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.wenjian">
                <JUpload v-model:value="formState.wenjian"></JUpload>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="picture" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.tupian">
                <JImageUpload :fileMax="2" v-model:value="formState.tupian"></JImageUpload>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="多行text框" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.dhwb">
                <a-textarea v-model:value="formState.dhwb" rows="4" placeholder="Please enter多行text框" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Dictionary drop-down search box" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.xlss">
                <JSearchSelect v-model:value="formState.xlss" dict="sys_user,realname,username" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="popupPop-up window" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.popup">
                <JPopup
                  v-model:value="formState.popup"
                  :fieldConfig="[
                    { source: 'name', target: 'popup' },
                    { source: 'id', target: 'popback' },
                  ]"
                  code="report_user"
                  :multi="true"
                  :setFieldsValue="setFieldsValue"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="popback" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.popback">
                <a-input v-model:value="formState.popback" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Classification Dictionary Tree" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.flzds">
                <JCategorySelect
                  @change="(value) => handleFormChange('flzds', value)"
                  v-model:value="formState.flzds"
                  pcode="B02"
                  placeholder="Please selectClassification Dictionary Tree"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Department selection" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.bmxz">
                <JSelectDept v-model:value="formState.bmxz" :multi="true" type="array" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="User selection" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.yhxz">
                <JSelectUserByDept v-model:value="formState.yhxz" :multi="true" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="富text" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.fwb">
                <JEditor v-model:value="formState.fwb" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="markdown" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.markdownString">
                <JMarkdownEditor v-model:value="formState.markdownString"></JMarkdownEditor>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Provinces and municipalitiesJAreaSelect" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.shq">
                <JAreaSelect v-model:value="formState.shq" placeholder="Please enterProvinces and municipalities" />
              </a-form-item>
            </a-col>
    
            <a-col :span="24">
              <a-form-item label="Provinces and municipalitiesJAreaLinkage" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.jssq">
                <JAreaLinkage v-model:value="formState.jssq" placeholder="Please enterProvinces and municipalities" />
              </a-form-item>
            </a-col>
    
            <a-col :span="24">
              <a-form-item label="JInputPop" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.ldzje">
                <JInputPop
                  v-model:value="formState.ldzje"
                  placeholder="Please enterJInputPop"
                  @change="(value) => handleFormChange('ldzje', value)"
                ></JInputPop>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="JSelectInput" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.ldzjs">
                <JSelectInput
                  v-model:value="formState.ldzjs"
                  placeholder="Please selectJSelectInput"
                  :options="ldzjsOptions"
                  @change="(value) => handleFormChange('ldzjs', value)"
                ></JSelectInput>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Drop-down multiple selection" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zddtjxl">
                <JSelectMultiple v-model:value="formState.zddtjxl" placeholder="Please selectDrop-down multiple selection" dictCode="sex"></JSelectMultiple>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="user" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.yongHu">
                <JSelectUser v-model:value="formState.yongHu" placeholder="Please selectuser"></JSelectUser>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Position" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zhiWu">
                <JSelectPosition
                  v-model:value="formState.zhiWu"
                  placeholder="Please selectPosition"
                  @change="(value) => handleFormChange('zhiWu', value)"
                ></JSelectPosition>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Role" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.jueSe">
                <JSelectRole v-model:value="formState.jueSe" placeholder="Please selectRole" @change="(value) => handleFormChange('jueSe', value)"></JSelectRole>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Custom tree" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zdys">
                <JTreeSelect
                  ref="treeSelect"
                  placeholder="Please selectCustom tree"
                  v-model:value="formState.zdys"
                  dict="sys_category,name,id"
                  pidValue="0"
                  loadTriggleChange
                >
                </JTreeSelect>
              </a-form-item>
            </a-col>
    
            <a-col :span="24">
              <a-form-item label="numerical value" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.yuanjia">
                <a-input-number v-model:value="formState.yuanjia" placeholder="Please enterdoubletype" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="enter2arrive10bit letters" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.ywzz">
                <a-input v-model:value="formState.ywzz" placeholder="Please enter2arrive10bit letters"></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="JTreeDict" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zdbxl">
                <JTreeDict
                  v-model:value="formState.zdbxl"
                  placeholder="Please selectJTreeDict"
                  @change="(value) => handleFormChange('zdbxl', value)"
                ></JTreeDict>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="JCodeEditor" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zdmrz">
                <JCodeEditor
                  v-model:value="formState.zdmrz"
                  placeholder="Please enterJCodeEditor"
                  @change="(value) => handleFormChange('zdmrz', value)"
                ></JCodeEditor>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="parameter" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.jsonParam">
                <JAddInput v-model:value="formState.jsonParam" placeholder="parameter"></JAddInput>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, nextTick, computed } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import dayjs from 'dayjs';
  import { TimePicker, Form } from 'ant-design-vue';
  import JCheckbox from '/@/components/Form/src/jeecg/components/JCheckbox.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JSwitch from '/@/components/Form/src/jeecg/components/JSwitch.vue';
  import JUpload from '/@/components/Form/src/jeecg/components/JUpload/JUpload.vue';
  import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUpload.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import JPopup from '/@/components/Form/src/jeecg/components/JPopup.vue';
  import JCategorySelect from '/@/components/Form/src/jeecg/components/JCategorySelect.vue';
  import JSelectUserByDept from '/@/components/Form/src/jeecg/components/JSelectUserByDept.vue';
  import JEditor from '/@/components/Form/src/jeecg/components/JEditor.vue';
  import JMarkdownEditor from '/@/components/Form/src/jeecg/components/JMarkdownEditor.vue';
  import JTreeSelect from '/@/components/Form/src/jeecg/components/JTreeSelect.vue';
  import JInputPop from '/@/components/Form/src/jeecg/components/JInputPop.vue';
  import JSelectInput from '/@/components/Form/src/jeecg/components/JSelectInput.vue';
  import JSelectPosition from '/@/components/Form/src/jeecg/components/JSelectPosition.vue';
  import JSelectMultiple from '/@/components/Form/src/jeecg/components/JSelectMultiple.vue';
  import JInput from '/@/components/Form/src/jeecg/components/JInput.vue';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import JAreaSelect from '/@/components/Form/src/jeecg/components/JAreaSelect.vue';
  import JAreaLinkage from '/@/components/Form/src/jeecg/components/JAreaLinkage.vue';
  import JSelectRole from '/@/components/Form/src/jeecg/components/JSelectRole.vue';
  import JTreeDict from '/@/components/Form/src/jeecg/components/JTreeDict.vue';
  import JCodeEditor from '/@/components/Form/src/jeecg/components/JCodeEditor.vue';
  import JAddInput from '/@/components/Form/src/jeecg/components/JAddInput.vue';
  import { getValueType } from '/@/utils';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';

  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
  });
  // form disabled
  const disabled = computed(()=>{
    return props.formDisabled;
  });
  const emit = defineEmits(['register', 'ok']);
  //update-begin---author:wangshuai ---date:20220616  for：Report sample verification modification--------------
  const formState = reactive<Record<string, any>>({
    name: '',
    miMa: '',
    ywzz: '',
    xiala: '',
    danxuan: '',
    duoxuan: '',
    riqi: '',
    shijian: '',
    wenjian: '',
    tupian: '',
    dhwb: '',
    xlss: '',
    popup: '',
    flzds: '',
    yhxz: '',
    fwb: '',
    shq: '',
    ldzje: '',
    ldzjs: '',
    zddtjxl: '',
    yongHu: '',
    zhiWu: '',
    jueSe: '',
    zdys: '',
    jssq: '',
    zdbxl: '',
    zdmrz: '',
    jsonParam: '',
    bmxz: '',
    yuanjia: '',
    nyrsfm: '',
  });
  //update-end---author:wangshuai ---date:20220616  for：Report sample verification modification--------------
  const { createMessage } = useMessage();
  const formRef = ref();
  const useForm = Form.useForm;
  const url = reactive<any>({
    duplicateCheck: '/sys/duplicate/check',
    add: '/test/jeecgDemo/oneNative/add',
    edit: '/test/jeecgDemo/oneNative/edit',
  });
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //form validation
  const validatorRules = {
    name: [{ required: false, message: 'Please entertext!' }],
    miMa: [{ required: false, message: 'Please enterpassword!' }],
    ywzz: [{ required: false }, { pattern: '^[a-z|A-Z]{2,10}$', message: 'Does not comply with verification rules!' }],
    xiala: [{ required: false, message: 'Please select下拉组件!' }],
    danxuan: [{ required: false, message: 'Please select单选组件!' }],
    duoxuan: [{ required: false, message: 'Please select多选组件!' }],
    riqi: [{ required: false, message: 'Please selectdate!' }],
    shijian: [{ required: false, message: 'Please selecttime!' }],
    wenjian: [{ required: false, message: '请上传document!' }],
    tupian: [{ required: false, message: '请上传picture!' }],
    dhwb: [{ required: false, message: '请填写多行text!' }],
    xlss: [{ required: false, message: 'Please selectDictionary drop down搜索!' }],
    popup: [{ required: false, message: 'Please selectpopupPop-up window!' }],
    flzds: [{ required: false, message: 'Please selectClassification Dictionary Tree!' }],
    yhxz: [{ required: false, message: 'Please selectuser!' }],
    fwb: [{ required: false, message: '请填写富text!' }],
    shq: [{ required: false, message: 'Please select省市级!' }],
    ldzje: [{ required: false, message: 'Please enterJInputPop!' }],
    ldzjs: [{ required: false, message: 'Please select下拉enter框!' }],
    zddtjxl: [{ required: false, message: 'Please select多选enter框!' }],
    yongHu: [{ required: false, message: 'Please selectuser!' }],
    zhiWu: [{ required: false, message: 'Please selectPosition!' }],
    jueSe: [{ required: false, message: 'Please selectRole!' }],
    zdys: [{ required: false, message: 'Please selectCustom tree!' }],
    jssq: [{ required: false, message: 'Please select三级联动!' }],
    zdbxl: [{ required: false, message: 'Please selectJTreeDict!' }],
    zdmrz: [{ required: false, message: 'Please enterJCodeEditor!' }],
    jsonParam: [{ required: false, message: 'Please enterparameter!' }],
    bmxz: [{ required: false, message: 'Please select部门!' }],
    yuanjia: [{ required: false, message: 'Please enternumerical value!' }],
    nyrsfm: [{ required: false, message: 'Please selectyear month day hour minute second!' }],
  };
  //update-begin---author:wangshuai ---date:20220616  for：Report sample verification modification------------
  const { resetFields, validate, validateInfos } = useForm(formState, validatorRules, { immediate: false });
  //update-end---author:wangshuai ---date:20220616  for：Report sample verification modification------------
  const ldzjsOptions = ref([
    { label: 'male', value: '1' },
    { label: 'female', value: '2' },
  ]);

  /**
   * New
   */
  function add() {
    edit({});
  }

  /**
   * edit
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      //Assignment
      Object.assign(formState, record);
    });
  }

  /**
   * Submit data
   */
  async function submitForm() {
    // 触发form validation
    //update-begin---author:wangshuai ---date:20220616  for：Report sample verification modification------------
    await validate();
    confirmLoading.value = true;
    let httpurl = '';
    let method = '';
    //time格式化
    let model = formState;
    if (!model.id) {
      httpurl += url.add;
      method = 'post';
    } else {
      httpurl += url.edit;
      method = 'put';
    }
    //If the loop data is an array
    for (let data in formState) {
      //如果该数据是数组并且是字符串type
      if (formState[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        //如果是字符串type的需要变成以逗号分割的字符串
        if (valueType === 'string') {
          formState[data] = formState[data].join(',');
        }
      }
    }
    defHttp
      .request(
        {
          url: httpurl,
          params: model,
          method: method,
        },
        { isTransformResponse: false }
      )
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
    //update-end---author:wangshuai ---date:20220616  for：Report sample verification modification--------------
  }

  /**
   * popupsuccess callback event
   */
  function popupHandleSuccess(values) {
    Object.assign(formState, values);
  }

  /**
   *  popupComponent value change event
   */
  function setFieldsValue(map) {
    Object.keys(map).map((key) => {
      formState[key] = map[key];
    });
  }

  /**
   * Value change event triggers
   * @param key
   * @param value
   */
  function handleFormChange(key, value) {
    formState[key] = value;
  }

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 24px 24px 24px 24px;
  }
</style>
