// Check if it's IE browser
export function isIE() {
  return navigator.userAgent.indexOf('compatible') > -1 && navigator.userAgent.indexOf('MSIE') > -1;
}

export function isIE11() {
  return navigator.userAgent.indexOf('Trident') > -1 && navigator.userAgent.indexOf('rv:11.0') > -1;
}

// Check if it's IE Edge browser
export function isEdge() {
  return navigator.userAgent.indexOf('Edge') > -1 && !isIE();
}

export function getIEVersion() {
  let userAgent = navigator.userAgent; // Get the browser's userAgent string
  let isIE = isIE();
  let isIE11 = isIE11();
  let isEdge = isEdge();

  if (isIE) {
    let reIE = new RegExp('MSIE (\\d+\\.\\d+);');
    reIE.test(userAgent);
    let fIEVersion = parseFloat(RegExp['$1']);
    if (fIEVersion === 7 || fIEVersion === 8 || fIEVersion === 9 || fIEVersion === 10) {
      return fIEVersion;
    } else {
      return 6; // IE version < 7
    }
  } else if (isEdge) {
    return 'edge';
  } else if (isIE11) {
    return 11;
  } else {
    return -1;
  }
}
