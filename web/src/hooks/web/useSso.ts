// Single sign-on core class
import { getToken } from '/@/utils/auth';
import { getUrlParam } from '/@/utils';
import { useGlobSetting } from '/@/hooks/setting';
import { validateCasLogin } from '/@/api/sys/user';
import { useUserStore } from '/@/store/modules/user';
const globSetting = useGlobSetting();
const openSso = globSetting.openSso;
export function useSso() {
  //update-begin---author:wangshuai---date:2024-01-03---for:【QQYUN-7805】SSOLogin mandatoryhttp #957---
  let locationUrl = document.location.protocol +"//" + window.location.host + '/';
  //update-end---author:wangshuai---date:2024-01-03---for:【QQYUN-7805】SSOLogin mandatoryhttp #957---

  /**
   * Single sign-on
   */
  async function ssoLogin() {
    if (openSso == 'true') {
      let token = getToken();
      let ticket = getUrlParam('ticket');
      if (!token) {
        if (ticket) {
          await validateCasLogin({
            ticket: ticket,
            service: locationUrl,
          }).then((res) => {
            const userStore = useUserStore();
            userStore.setToken(res.token);
            return userStore.afterLoginAction(true, {});
          });
        } else {
          window.location.href = globSetting.casBaseUrl + '/login?service=' + encodeURIComponent(locationUrl);
        }
      }
    }
  }

  /**
   * Log out
   */
  async function ssoLoginOut() {
    window.location.href = globSetting.casBaseUrl + '/logout?service=' + encodeURIComponent(locationUrl);
  }
  return { ssoLogin, ssoLoginOut };
}
