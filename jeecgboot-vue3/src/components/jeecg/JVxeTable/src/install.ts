import type { App } from 'vue';
// introduce vxe-table
import 'xe-utils';
import VxeUIAll from 'vxe-pc-ui';
import VXETable /*Grid*/ from 'vxe-table';
import VXETablePluginAntd from 'vxe-table-plugin-antd';
import 'vxe-pc-ui/lib/style.css';
import 'vxe-table/lib/style.css';

import JVxeTable from './JVxeTable';
import { getEventPath } from '/@/utils/common/compUtils';
import { registerAllComponent } from './utils/registerUtils';
import { getEnhanced } from './utils/enhancedUtils';

export function registerJVxeTable(app: App) {
  // VXETable Global configuration
  const VXETableSettings = {
    // z-index starting value
    zIndex: 1000,
    table: {},
  };

  // Add event interceptor event.clearActived
  // For example, after clicking the pop-up layer panel of a component，The activated cell should not be automatically closed at this time，by returning false Default behavior can be prevented。
  VXETable.interceptor.add('event.clearActived', preventClosingPopUp);
  VXETable.interceptor.add('event.clearEdit', preventClosingPopUp);
  // Register plugin
  VXETable.use(VXETablePluginAntd);
  // Register a custom component
  registerAllComponent();
  // Execute registration method
  app.use(VxeUIAll);
  app.use(VXETable, VXETableSettings);
  app.component('JVxeTable', JVxeTable);
}


/**
 * Prevent closing pop-ups during line editing
 * @param params
 */
function preventClosingPopUp(this: any, params) {
  // Get component enhancements
  let col = params.column.params;
  // update-begin--author:liaozhiyang---date:20250429---for：【issues/8178】Use nativevxe-tableError reported when losing focus in component editing mode
  if (col === undefined) {
    // It shows that the use of pure nativevxe-table
    return;
  }
  // update-end--author:liaozhiyang---date:20250429---for：【issues/8178】Use nativevxe-tableError reported when losing focus in component editing mode
  let { $event } = params;
  const interceptor = getEnhanced(col.type).interceptor;
  // Execution enhancement
  let flag = interceptor['event.clearActived']?.call(this, ...arguments);
  if (flag === false) {
    return false;
  }

  let path = getEventPath($event);
  for (let p of path) {
    let className: any = p.className || '';
    className = typeof className === 'string' ? className : className.toString();

    /* --- Special handling of the following components，Do not clear editing status when clicking the following tags --- */

    // The clicked label isJInputPop
    if (className.includes('j-input-pop')) {
      return false;
    }
    // The clicked label isJPopuppopup layer、Department selection、User selection
    if (className.includes('j-popup-modal') || className.includes('j-depart-select-modal') || className.includes('j-user-select-modal')) {
      return false;
    }
    // Click on the date picker
    if (className.includes('j-vxe-date-picker')) {
      return false;
    }
    // Execution enhancement
    let flag = interceptor['event.clearActived.className']?.call(this, className, ...arguments);
    if (flag === false) {
      return false;
    }
  }
}
