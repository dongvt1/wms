import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { h } from 'vue';
import { Tinymce } from '@/components/Tinymce';

export const columns: BasicColumn[] = [
  {
    title: 'title',
    width: 150,
    dataIndex: 'titile',
  },
  {
    title: 'Message type',
    dataIndex: 'msgCategory',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'msg_category');
    },
  },
  {
    title: 'Posted by',
    width: 100,
    dataIndex: 'sender',
  },
  {
    title: 'priority',
    dataIndex: 'priority',
    width: 70,
    customRender: ({ text }) => {
      const color = text == 'L' ? 'blue' : text == 'M' ? 'yellow' : 'red';
      return render.renderTag(render.renderDict(text, 'priority'), color);
    },
  },
  {
    title: 'Notification object',
    dataIndex: 'msgType',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'msg_type');
    },
  },
  {
    title: 'Release status',
    dataIndex: 'sendStatus',
    width: 70,
    customRender: ({ text }) => {
      const color = text == '0' ? 'red' : text == '1' ? 'green' : 'gray';
      return render.renderTag(render.renderDict(text, 'send_status'), color);
    },
  },
  {
    title: 'Release time',
    width: 100,
    dataIndex: 'sendTime',
  },
  {
    title: 'Undo time',
    width: 100,
    dataIndex: 'cancelTime',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'titile',
    label: 'title',
    component: 'JInput',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'msgCategory',
    label: 'Message type',
    required: true,
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      type: 'radio',
      dictCode: 'msg_category',
      placeholder: 'Please select type',
    },
  },
  {
    field: 'izTop',
    label: 'Whether to pin it to the top',
    defaultValue: '0',
    component: 'JSwitch',
    componentProps: {
      //value options
      options: ['1', '0'],
      //textoption
      labelOptions: ['yes', 'no'],
      placeholder: 'Whether to pin it to the top',
      checkedChildren: 'yes',
      unCheckedChildren: 'no',
    },
  },
  {
    field: 'titile',
    label: '通告title',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: '请输入title',
    },
    // update-begin--author:liaozhiyang---date:20240701---for：【TV360X-1632】title过长保存报错，Length check
    dynamicRules() {
      return [
        {
          validator: (_, value) => {
            return new Promise<void>((resolve, reject) => {
              if (value.length > 100) {
                reject('longest100characters');
              }
              resolve();
            });
          },
        },
      ];
    },
    // update-end--author:liaozhiyang---date:20240701---for：【TV360X-1632】title过长保存报错，Length check
  },
  {
    field: 'msgAbstract',
    label: 'Announcement summary',
    component: 'InputTextArea',
    componentProps: {
      allowClear: true,
      autoSize: {
        minRows: 2,
        maxRows: 5,
      },
    },
    required: true,
  },
  // {
  //   field: 'endTime',
  //   label: 'Expiration date',
  //   component: 'DatePicker',
  //   componentProps: {
  //     showTime: true,
  //     valueFormat: 'YYYY-MM-DD HH:mm:ss',
  //     placeholder: '请选择Expiration date',
  //   },
  //   dynamicRules: ({ model }) => rules.endTime(model.startTime, true),
  // },
  {
    field: 'msgType',
    label: 'receive user',
    defaultValue: 'ALL',
    component: 'JDictSelectTag',
    required: true,
    componentProps: {
      type: 'radio',
      dictCode: 'msg_type',
      placeholder: 'Please select the publication scope',
    },
  },
  {
    field: 'userIds',
    label: 'Specify user',
    component: 'JSelectUserByDepartment',
    required: true,
    componentProps: {
      rowKey: 'id',
      // update-begin--author:liaozhiyang---date:20240701---for：【TV360X-1627】Notification and Announcement User Selected Component Not Translated
      labelKey: 'realname',
      // update-end--author:liaozhiyang---date:20240701---for：【TV360X-1627】Notification and Announcement User Selected Component Not Translated
    },
    ifShow: ({ values }) => values.msgType == 'USER',
  },
  {
    field: 'msgClassify',
    label: 'Announcement classification',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'notice_type',
      placeholder: '请选择Announcement classification',
    },
  },
  {
    field: 'priority',
    label: 'priority别',
    defaultValue: 'H',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'priority',
      type: 'radio',
      placeholder: '请选择priority',
    },
  },
  {
    field: 'izApproval',
    label: 'yesno审批',
    component: 'RadioGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        {
          label: 'yes',
          value: '1',
        },
        {
          label: 'no',
          value: '0',
        },
      ],
    },
  },
  {
    field: 'msgTemplate',
    label: 'Announcement template',
    component: 'Input',
    slot: 'msgTemplate',
  },
  {
    field: 'files',
    label: 'Announcement attachment',
    component: 'JUpload',
    componentProps: {
      //yesno显示选择按钮
      text: 'File upload',
      //Maximum number of uploads
      maxCount: 20,
      //yesno显示下载按钮
      download: true,
    },
  },
  {
    field: 'msgContent',
    label: 'Announcement content',
    component: 'Input',
    colProps: { span: 24 },
    render: render.renderTinymce,
  },
];

/**
 * The process form calls this method to obtainformSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // The default is the same as the original form If permission data is configured in the process，This needs to be dealt with separatelyformSchema
  return [
    {
      field: 'id',
      label: 'id',
      component: 'Input',
      show: false,
    },
    {
      field: 'msgCategory',
      label: 'Message type',
      required: true,
      component: 'JDictSelectTag',
      defaultValue: '1',
      componentProps: {
        type: 'radio',
        dictCode: 'msg_category',
        placeholder: 'Please select type',
      },
    },
    {
      field: 'izTop',
      label: 'Whether to pin it to the top',
      defaultValue: '0',
      component: 'JSwitch',
      componentProps: {
        //value options
        options: ['1', '0'],
        //textoption
        labelOptions: ['yes', 'no'],
        placeholder: 'Whether to pin it to the top',
        checkedChildren: 'yes',
        unCheckedChildren: 'no',
      },
    },
    {
      field: 'titile',
      label: '通告title',
      component: 'Input',
      required: true,
      componentProps: {
        placeholder: '请输入title',
      },
      // update-begin--author:liaozhiyang---date:20240701---for：【TV360X-1632】title过长保存报错，Length check
      dynamicRules() {
        return [
          {
            validator: (_, value) => {
              return new Promise<void>((resolve, reject) => {
                if (value.length > 100) {
                  reject('longest100characters');
                }
                resolve();
              });
            },
          },
        ];
      },
      // update-end--author:liaozhiyang---date:20240701---for：【TV360X-1632】title过长保存报错，Length check
    },
    {
      field: 'msgAbstract',
      label: 'Announcement summary',
      component: 'InputTextArea',
      required: true,
    },
    {
      field: 'msgType',
      label: 'receive user',
      defaultValue: 'ALL',
      component: 'JDictSelectTag',
      required: true,
      componentProps: {
        type: 'radio',
        dictCode: 'msg_type',
        placeholder: 'Please select the publication scope',
      },
    },
    {
      field: 'userIds',
      label: 'Specify user',
      component: 'JSelectUserByDepartment',
      required: true,
      componentProps: {
        rowKey: 'id',
        // update-begin--author:liaozhiyang---date:20240701---for：【TV360X-1627】Notification and Announcement User Selected Component Not Translated
        labelKey: 'realname',
        // update-end--author:liaozhiyang---date:20240701---for：【TV360X-1627】Notification and Announcement User Selected Component Not Translated
      },
      ifShow: ({ values }) => values.msgType == 'USER',
    },
    {
      field: 'msgClassify',
      label: 'Announcement classification',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: 'notice_type',
        placeholder: '请选择Announcement classification',
      },
    },
    {
      field: 'priority',
      label: 'priority别',
      defaultValue: 'H',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: 'priority',
        type: 'radio',
        placeholder: '请选择priority',
      },
    },
    {
      field: 'msgTemplate',
      label: 'Announcement template',
      component: 'Input',
      slot: 'msgTemplate',
    },
    {
      field: 'files',
      label: 'Announcement attachment',
      component: 'JUpload',
      componentProps: {
        //yesno显示选择按钮
        text: 'File upload',
        //Maximum number of uploads
        maxCount: 2,
        //yesno显示下载按钮
        download: true,
      },
    },
    {
      field: 'msgContent',
      label: 'Announcement content',
      component: 'Input',
      colProps: { span: 24 },
      ifShow: ({}) => _formData.disabled == false,
      render: ({ model, field }) => {
        return h(Tinymce, {
          showImageUpload: false,
          disabled: _formData.disabled !== false,
          height: 300,
          value: model[field],
          onChange: (value: string) => {
            model[field] = value;
          },
        });
      },
    },
    {
      field: 'msgContent',
      label: 'Announcement content',
      component: 'Input',
      colProps: { span: 24 },
      ifShow: ({}) => _formData.disabled !== false,
      slot: 'msgContent',
    },
  ];
}
