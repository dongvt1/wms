<template>
  <div>
    <textarea ref="textarea">
The sun is over the mountains，Yellow River flows into the sea。
Want to see a thousand miles away，Go to the next level。
        </textarea
    >
  </div>
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref, reactive } from 'vue';
  // Introduce global instance
  import _CodeMirror from 'codemirror';

  // Core styles
  import 'codemirror/lib/codemirror.css';
  // After introducing the theme, you still need to options The theme specified in will take effect
  import 'codemirror/theme/cobalt.css';

  // It is necessary to introduce a specific syntax highlighting library to have the corresponding syntax highlighting effect.
  // codemirror The official actually supports the adoption of /addon/mode/loadmode.js and /mode/meta.js To achieve dynamic loading of the corresponding syntax highlighting library
  // but vue It seems that there is no way to dynamically load the corresponding object after the instance is initialized. JS ，Therefore, the corresponding JS Introduced in advance
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
  // Try to get global instance

  export default defineComponent({
    components: {},
    setup() {
      const CodeMirror = window.CodeMirror || _CodeMirror;

      const textarea = ref(null);
      const options = reactive({
        // indent format
        tabSize: 2,
        // theme，对应theme库 JS 需要Introduced in advance
        theme: 'cobalt',
        // Show line number
        lineNumbers: true,
        line: true,
      });
      onMounted(() => {
        init();
      });

      function init() {
        CodeMirror.fromTextArea(textarea.value, options);
      }

      return {
        textarea,
      };
    },
  });
</script>

<style lang="css" scoped></style>
