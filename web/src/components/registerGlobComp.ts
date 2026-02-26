import type { App } from 'vue';
import { Icon } from './Icon';
import AIcon from '/@/components/jeecg/AIcon.vue';
//Tinymcerich text
 import Editor from '/@/components/Tinymce/src/Editor.vue'

import { Button, JUploadButton } from './Button';

// Register on demandantdcomponents
import {
  // Need
  Button as AntButton,
  Select,
  Alert,
  Checkbox,
  DatePicker,
  TimePicker,
  Calendar,
  Radio,
  Switch,
  Card,
  List,
  Tabs,
  Descriptions,
  Tree,
  Table,
  Divider,
  Modal,
  Drawer,
  TreeSelect,
  Dropdown,
  Tag,
  Tooltip,
  Badge,
  Popover,
  Upload,
  Transfer,
  Steps,
  PageHeader,
  Result,
  Empty,
  Avatar,
  Menu,
  Breadcrumb,
  Form,
  Input,
  Row,
  Col,
  Spin,
  Space,
  Layout,
  Collapse,
  Slider,
  InputNumber,
  Carousel,
  Popconfirm,
  Skeleton,
  Cascader,
  Rate,
  Progress
} from 'ant-design-vue';
const compList = [AntButton.Group, Icon, AIcon, JUploadButton];


export function registerGlobComp(app: App) {
  compList.forEach((comp) => {
    app.component(comp.name || comp.displayName, comp);
  });
  
  //Dashboard dependenciesTinymce，Need to be loaded in advance（No way to load on demand）
  app.component(Editor.name, Editor);
  // update-begin--author:liaozhiyang---date:20240308---for：【QQYUN-8241】TinymceAsynchronous loading
  // app.component(
  //   'Tinymce',
  //   createAsyncComponent(() => import('./Tinymce/src/Editor.vue'), {
  //     loading: true,
  //   })
  // );
  // update-end--author:liaozhiyang---date:20240308---for：【QQYUN-8241】TinymceAsynchronous loading
  app.use(Select)
    .use(Alert)
    .use(Button)
    .use(Breadcrumb)
    .use(Checkbox)
    .use(DatePicker)
    .use(TimePicker)
    .use(Calendar)
    .use(Radio)
    .use(Switch)
    .use(Card)
    .use(List)
    .use(Descriptions)
    .use(Tree)
    .use(TreeSelect)
    .use(Table)
    .use(Divider)
    .use(Modal)
    .use(Drawer)
    .use(Dropdown)
    .use(Tag)
    .use(Tooltip)
    .use(Badge)
    .use(Popover)
    .use(Upload)
    .use(Transfer)
    .use(Steps)
    .use(PageHeader)
    .use(Result)
    .use(Empty)
    .use(Avatar)
    .use(Menu)
    .use(Tabs)
    .use(Form)
    .use(Input)
    .use(Row)
    .use(Col)
    .use(Spin)
    .use(Space)
    .use(Layout)
    .use(Collapse)
    .use(Slider)
    .use(InputNumber)
    .use(Carousel)
    .use(Popconfirm)
    .use(Skeleton)
    .use(Cascader)
    .use(Rate)
    .use(Progress);
    console.log("---initialization---， Global registrationAntd、Dashboard、process designer、online、Process components--------------")
}
