/**
 * Global authority directive
 * Used for fine-grained control of component permissions
 * @Example v-auth="RoleEnum.TEST"
 */
import type { App, Directive, DirectiveBinding } from 'vue';

import { usePermission } from '/@/hooks/web/usePermission';

function isAuth(el: Element, binding: any) {
  // update-begin--author:liaozhiyang---date:20240529---for【TV360X-460】basicFormsupportv-authinstruction(Access control visible and hidden)
  const value = binding.value;
  if (!value) return;
  // update-end--author:liaozhiyang---date:20240529---for【TV360X-460】basicFormsupportv-authinstruction(Access control visible and hidden)
  const { hasPermission } = usePermission();
  if (!hasPermission(value)) {
    el.parentNode?.removeChild(el);
  }
}

const mounted = (el: Element, binding: DirectiveBinding<any>) => {
  isAuth(el, binding);
};

const authDirective: Directive = {
  mounted,
};

export function setupPermissionDirective(app: App) {
  app.directive('auth', authDirective);
}

export default authDirective;
