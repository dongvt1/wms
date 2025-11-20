<template>
  <div ref="containerRef" v-bind="boxBindProps">
    <!-- full screen button -->
    <a-icon v-if="fullScreen" class="full-screen-icon" :type="fullScreenIcon" @click="onToggleFullScreen" />
    <textarea ref="textarea" v-bind="getBindValue"></textarea>
  </div>
</template>

<script lang="ts">
  import { defineComponent, onMounted, reactive, ref, watch, unref, computed } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  // Introduce global instance
  import _CodeMirror, { EditorFromTextArea } from 'codemirror';
  // Core styles
  import 'codemirror/lib/codemirror.css';
  // After introducing the theme, you still need to options The theme specified in will take effect
  import 'codemirror/theme/idea.css';
  // It is necessary to introduce a specific syntax highlighting library to have the corresponding syntax highlighting effect.
  import 'codemirror/mode/javascript/javascript.js';
  import 'codemirror/mode/css/css.js';
  import 'codemirror/mode/xml/xml.js';
  import 'codemirror/mode/clike/clike.js';
  import 'codemirror/mode/markdown/markdown.js';
  import 'codemirror/mode/python/python.js';
  import 'codemirror/mode/r/r.js';
  import 'codemirror/mode/shell/shell.js';
  import 'codemirror/mode/sql/sql.js';
  import 'codemirror/mode/swift/swift.js';
  import 'codemirror/mode/vue/vue.js';
  // Introduction of folding resources:start
  import 'codemirror/addon/fold/foldgutter.css';
  import 'codemirror/addon/fold/foldcode.js';
  import 'codemirror/addon/fold/brace-fold.js';
  import 'codemirror/addon/fold/comment-fold.js';
  import 'codemirror/addon/fold/indent-fold.js';
  import 'codemirror/addon/fold/foldgutter.js';
  // Introduction of folding resources:Finish
  //Cursor line background highlight，It is also required in the configurationstyleActiveLineset totrue
  import 'codemirror/addon/selection/active-line.js';
  // Support code auto-completion
  import 'codemirror/addon/hint/show-hint.css';
  import 'codemirror/addon/hint/show-hint.js';
  import 'codemirror/addon/hint/anyword-hint.js';
  // match brackets
  import 'codemirror/addon/edit/matchbrackets';
  // placeholder
  import 'codemirror/addon/display/placeholder.js';
  
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { isJsonObjectString } from '/@/utils/is.ts';
  // Code tips
  import { useCodeHinting } from '../hooks/useCodeHinting';

  import { useRootSetting } from '/@/hooks/setting/useRootSetting';
  import { ThemeEnum } from '/@/enums/appEnum';
  export default defineComponent({
    name: 'JCodeEditor',
    // will not attrs properties bound to html on the label
    inheritAttrs: false,
    components: {},
    props: {
      value: propTypes.string.def(''),
      height: propTypes.string.def('auto'),
      disabled: propTypes.bool.def(false),
      // 是否显示full screen button
      fullScreen: propTypes.bool.def(false),
      // After full screenz-index
      zIndex: propTypes.any.def(1500),
      theme: propTypes.string.def('idea'),
      language: propTypes.string.def(''),
      // Code tips
      keywords: propTypes.array.def([]),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const { getDarkMode } = useRootSetting();
      const containerRef = ref(null);
      const { prefixCls } = useDesign('code-editer');
      const CodeMirror = window.CodeMirror || _CodeMirror;
      const emitData = ref<object>();
      //form value
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      const textarea = ref<HTMLTextAreaElement>();
      let coder: Nullable<EditorFromTextArea> = null;
      const attrs = useAttrs();
      const height = ref(props.height);
      const options = reactive({
        // indent format
        tabSize: 2,
        // theme，对应theme库 JS Need to be introduced in advance
        // update-begin--author:liaozhiyang---date:20240327---for：【QQYUN-8639】暗黑theme适配
        theme: getDarkMode.value == ThemeEnum.DARK ? 'monokai' : props.theme,
        // update-end--author:liaozhiyang---date:20240327---for：【QQYUN-8639】暗黑theme适配
        smartIndent: true, // Whether to use smart indentation
        // Show line number
        lineNumbers: true,
        line: true,
        // Enable code folding related features:start
        foldGutter: true,
        lineWrapping: true,
        gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter', 'CodeMirror-lint-markers'],
        // Enable code folding related features:Finish
        // Cursor line highlighting
        styleActiveLine: true,
        // update-begin--author:liaozhiyang---date:20231201---for：【issues/869】JCodeEditorThere is no setting when the component is initialized.mode
        mode: props.language,
        // update-begin--author:liaozhiyang---date:20231201---for：【issues/869】JCodeEditorThere is no setting when the component is initialized.mode
        // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-898】Change the preview after code generation to read-only
        readOnly: props.disabled,
        // update-end--author:liaozhiyang---date:20240603---for：【TV360X-898】Change the preview after code generation to read-only
        // match brackets
        matchBrackets: true,
        extraKeys: {
          // Tab: function autoFormat(editor) {
          //   //var totalLines = editor.lineCount();
          //   //editor.autoFormatRange({line:0, ch:0}, {line:totalLines});
          //   setValue(innerValue, false);
          // },
          'Cmd-/': (cm) => comment(cm),
          'Ctrl-/': (cm) => comment(cm),
        },
      });
      // internal storage value，Initial is props.value
      let innerValue = props.value ?? '';
      // full screen state
      const isFullScreen = ref(false);
      const fullScreenIcon = computed(() => (isFullScreen.value ? 'fullscreen-exit' : 'fullscreen'));
      // External box parameters
      const boxBindProps = computed(() => {
        let _props = {
          class: [
            prefixCls,
            'full-screen-parent',
            'auto-height',
            {
              'full-screen': isFullScreen.value,
            },
          ],
          style: {},
        };
        if (isFullScreen.value) {
          _props.style['z-index'] = props.zIndex;
        }
        return _props;
      });
      // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-5955】online jsEnhance，加入Code tips
      const { codeHintingMount, codeHintingRegistry } = useCodeHinting(CodeMirror, props.keywords, props.language);
      codeHintingRegistry();
      // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-5955】online jsEnhance，加入Code tips
      /**
       * Listening component value
       */
      watch(
        () => props.value,
        () => {
          if (innerValue != props.value) {
            setValue(props.value, false);
          }
        }
      );
      onMounted(() => {
        initialize();
        // update-begin--author:liaozhiyang---date:20240318---for：【QQYUN-8473】The code editor will be blocked when loading for the first time.
        setTimeout(() => {
          refresh();
        }, 150);
        // update-end--author:liaozhiyang---date:20240318---for：【QQYUN-8473】The code editor will be blocked when loading for the first time.
      });

      /**
       * Component assignment
       * @param value
       * @param trigger Whether to trigger change event
       */
      function setValue(value: string, trigger = true) {
        if (value && isJsonObjectString(value)) {
          value = JSON.stringify(JSON.parse(value), null, 2);
        }
        coder?.setValue(value ?? '');
        innerValue = value;
        trigger && emitChange(innerValue);
        // update-begin--author:liaozhiyang---date:20240510---for：【QQYUN-9231】Code editor is blocked
        setTimeout(() => {
          refresh();
          // Brush again to prevent occlusion problems in a small probability
          setTimeout(() => {
            refresh();
          }, 600);
        }, 400);
        // update-end--author:liaozhiyang---date:20240510---for：【QQYUN-9231】Code editor is blocked
      }

      //编辑器值修改event
      function onChange(obj) {
        let value = obj.getValue();
        innerValue = value || '';
        if (props.value != innerValue) {
          emitChange(innerValue);
        }
      }

      function emitChange(value) {
        emit('change', value);
        emit('update:value', value);
      }

      //Component initialization
      function initialize() {
        coder = CodeMirror.fromTextArea(textarea.value!, options);
        //绑定值修改event
        coder.on('change', onChange);
        // Assign a value once when initialization is successful.
        setValue(innerValue, false);
        // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-5955】online jsEnhance，加入Code tips
        codeHintingMount(coder);
        // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-5955】online jsEnhance，加入Code tips
      }

      // 切换full screen state
      function onToggleFullScreen() {
        isFullScreen.value = !isFullScreen.value;
      }

      //update-begin-author:taoyan date:2022-5-9 for: codeEditorDisable function
      watch(
        () => props.disabled,
        (val) => {
          if (coder) {
            coder.setOption('readOnly', val);
          }
        }
      );
      //update-end-author:taoyan date:2022-5-9 for: codeEditorDisable function
      
      // Support dynamically setting language
      watch(()=>props.language, (val)=>{
        if(val && coder){
          coder.setOption('mode', val);
        }
      });

      const getBindValue = Object.assign({}, unref(props), unref(attrs));

      //update-begin-author:taoyan date:2022-10-18 for: VUEN-2480【seriousbug】online vue3test questions 8、online jsEnhance样式问题
      function refresh(){
        if(coder){
          coder.refresh();
        }
      }
      //update-end-author:taoyan date:2022-10-18 for: VUEN-2480【seriousbug】online vue3test questions 8、online jsEnhance样式问题

      /**
       * 2024-04-01
       * liaozhiyang
       * Code batch comments
       */
      function comment(cm) {
        var selection = cm.getSelection();
        var start = cm.getCursor('start');
        var end = cm.getCursor('end');
        var isCommented = selection.startsWith('//');
        if (isCommented) {
          // if already commented，Uncomment
          cm.replaceRange(selection.replace(/\n\/\/\s/g, '\n').replace(/^\/\/\s/, ''), start, end);
        } else {
          // Add comment
          cm.replaceRange('// ' + selection.replace(/\n(?=.)/g, '\n// '), start, end);
        }
      }

      return {
        state,
        textarea,
        boxBindProps,
        getBindValue,
        setValue,
        isFullScreen,
        fullScreenIcon,
        onToggleFullScreen,
        refresh,
        containerRef,
      };
    },
  });
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-code-editer';
  .@{prefix-cls} {
    &.auto-height {
      .CodeMirror {
        height: v-bind(height) !important;
        min-height: 100px;
      }
    }

    /* full screen style */

    &.full-screen-parent {
      position: relative;

      .full-screen-icon {
        opacity: 0;
        color: black;
        width: 20px;
        height: 20px;
        line-height: 24px;
        background-color: white;
        position: absolute;
        top: 2px;
        right: 2px;
        z-index: 9;
        cursor: pointer;
        transition: opacity 0.3s;
        padding: 2px 0 0 1.5px;
      }

      &:hover {
        .full-screen-icon {
          opacity: 1;

          &:hover {
            background-color: rgba(255, 255, 255, 0.88);
          }
        }
      }

      &.full-screen {
        position: fixed;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        padding: 8px;
        background-color: #f5f5f5;

        .full-screen-icon {
          top: 12px;
          right: 12px;
        }

        .full-screen-child,
        .CodeMirror {
          height: 100%;
          max-height: 100%;
          min-height: 100%;
        }
      }

      .full-screen-child {
        height: 100%;
      }
    }
    
    /** VUEN-2344【vue3】There is something wrong with this style，Should I add a border? */
    .CodeMirror{
      border: 1px solid #ddd;
    }
    .CodeMirror pre.CodeMirror-placeholder {
      color: #cacaca;
      font-family: -apple-system,BlinkMacSystemFont,Segoe UI,PingFang SC,Hiragino Sans GB,Microsoft YaHei,Helvetica Neue,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji,Segoe UI Symbol;
    }
  }
  .CodeMirror-hints.idea,
  .CodeMirror-hints.monokai {
    z-index: 1001;
    max-width: 600px;
    max-height: 300px;
  }
  // update-begin--author:liaozhiyang---date:20240327---for：【QQYUN-8639】暗黑theme适配
  html[data-theme='dark'] {
    .@{prefix-cls} {
      .CodeMirror {
        border: 1px solid #3a3a3a;
      }
    }
  }
  // update-end--author:liaozhiyang---date:20240327---for：【QQYUN-8639】暗黑theme适配
</style>
