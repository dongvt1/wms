import { ComponentInternalInstance, ExtractPropTypes } from 'vue';
import { useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

export namespace JVxeComponent {
  export type Props = ExtractPropTypes<ReturnType<typeof useJVxeCompProps>>;

  interface EnhancedCtx {
    props?: JVxeComponent.Props;
    context?: any;
  }

  /** Component enhancement type */
  export interface Enhanced {
    // Registration parameters（See details：https://xuliangzhan_admin.gitee.io/vxe-table/v4/table/renderer/edit）
    installOptions: {
      // autofocus class Class name
      autofocus?: string;
    } & Recordable;
    // event interceptor（for compatibility）
    interceptor: {
      // Realized：event.clearActived
      // illustrate：For example, after clicking the pop-up layer panel of a component，The activated cell should not be automatically closed at this time，by returning false Default behavior can be prevented。
      'event.clearActived'?: (params, event, target, ctx?: EnhancedCtx) => boolean;
      // Customize：event.clearActived.className
      // illustrate：One more parameter than the original one：className，The style name used to determine the clicked element（Recurse to top level）
      'event.clearActived.className'?: (params, event, target, ctx?: EnhancedCtx) => boolean;
    };
    // 【Function switch】
    switches: {
      // Whether to use editRender model（Only current component，Not the overall situation）
      // If set totrue，An editable icon will appear above the header.
      editRender?: boolean;
      // false = Visible after component is triggered）；true = Components are always visible
      visible?: boolean;
    };
    // 【Section enhancement】Aspect event handling，Generally executed synchronously after certain methods are executed
    aopEvents: {
      // This event is triggered when the cell is activated for editing.
      editActived?: (this: ComponentInternalInstance, ...args) => any;
      // This event will be triggered when the cell is closed in editing state.
      editClosed?: (this: ComponentInternalInstance, ...args) => any;
      // The return value determines whether the cell can be edited
      activeMethod?: (this: ComponentInternalInstance, ...args) => boolean;
    };
    // 【Translation enhancement】It is possible to implement for exampleselectComponent savedvalue，butspanmodel下需要显示成text
    translate: {
      // Whether to enable translation
      enabled?: boolean;
      /**
       * 【Translation processing method】ifhandlerLeave blank，then use the default translation method
       *
       * @param value Value to be translated
       * @returns{*} Return translated data
       */
      handler?: (value, ctx?: EnhancedCtx) => any;
    };
    /**
     * 【Get value enhancement】The value thrown by the component
     *
     * @param value value saved in database
     * @returns{*} Return the processed value
     */
    getValue: (value, ctx?: EnhancedCtx) => any;
    /**
     * 【Setting value enhancement】The value set to the component
     *
     * @param value The value that the component triggers
     * @returns{*} Return the processed value
     */
    setValue: (value, ctx?: EnhancedCtx) => any;
    /**
     * 【New line enhancement】Event triggered when user clicks Add，Returns the default value for new rows
     *
     * @param defaultValue default value
     * @param row row data
     * @param column column configuration，.params Is a parameter configured by the user
     * @param $table vxe Example
     * @param renderOptions Rendering options
     * @param params Available here $table
     *
     * @returns Return new value
     */
    createValue: (defaultValue: any, ctx?: EnhancedCtx) => any;
  }

  export type EnhancedPartial = Partial<Enhanced>;
}
