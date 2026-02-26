import { nextTick } from 'vue';
import $printJS, { Configuration } from 'print-js';
import Print from 'vue-print-nb-jeecg/src/printarea';

/**
 * call printJS，iftype = html，Just leave printNB method
 */
export function printJS(configuration: Configuration) {
  if (configuration?.type === 'html') {
    printNb(configuration.printable);
  } else {
    return $printJS(configuration);
  }
}

/** call printNB Print */
export function printNb(domId) {
  if (domId) {
    localPrint(domId);
  } else {
    window.print();
  }
}

let closeBtn = true;

function localPrint(domId) {
  if (typeof domId === 'string' && !domId.startsWith('#')) {
    domId = '#' + domId;
  }
  nextTick(() => {
    if (closeBtn) {
      closeBtn = false;
      new Print({
        el: domId,
        endCallback() {
          closeBtn = true;
        },
      });
    }
  });
}
