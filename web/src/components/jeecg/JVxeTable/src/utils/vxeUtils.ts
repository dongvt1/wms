/**
 *
 * according to tagName Get parent node
 *
 * @param dom Level 1domnode
 * @param tagName tag name，Not case sensitive
 */
export function getParentNodeByTagName(dom: HTMLElement, tagName: string = 'body'): HTMLElement | null {
  if (tagName === 'body') {
    return document.body;
  }
  if (dom.parentElement) {
    if (dom.parentElement.tagName.toLowerCase() === tagName.trim().toLowerCase()) {
      return dom.parentElement;
    } else {
      return getParentNodeByTagName(dom.parentElement, tagName);
    }
  } else {
    return null;
  }
}
