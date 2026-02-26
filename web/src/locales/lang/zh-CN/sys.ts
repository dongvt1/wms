export default {
  api: {
    operationFailed: 'Operation failed',
    errorTip: 'Error message',
    errorMessage: 'Operation failed,System exception!',
    timeoutMessage: 'Login timeout,Please log in again!',
    apiTimeoutMessage: 'Interface request timeout,Please refresh the page and try again!',
    apiRequestFailed: 'Request error，Please try again later',
    networkException: 'Network abnormality',
    networkExceptionMsg: 'Network abnormality，Please check if your network connection is normal!',

    errMsg401: 'User does not have permission（token、username、Wrong password）!',
    errMsg403: 'User is authorized，But access is prohibited。!',
    errMsg404: 'Network request error,The resource was not found!',
    errMsg405: 'Network request error,Request method not allowed!',
    errMsg408: 'Network request timeout!',
    errMsg500: 'Server error,Please contact the administrator!',
    errMsg501: 'Network is not implemented!',
    errMsg502: 'network error!',
    errMsg503: 'Service unavailable，The server is temporarily overloaded or undergoing maintenance!',
    errMsg504: 'Network timeout!',
    errMsg505: 'httpThe version does not support the request!',

    registerMsg: 'Registration successful',
  },
  app: { logoutTip: 'Warm reminder', logoutMessage: 'Are you sure to exit the system??', menuLoading: 'Menu loading...' },
  errorLog: {
    tableTitle: 'Error log list',
    tableColumnType: 'type',
    tableColumnDate: 'time',
    tableColumnFile: 'document',
    tableColumnMsg: 'error message',
    tableColumnStackMsg: 'stackinformation',

    tableActionDesc: 'Details',

    modalTitle: 'mistakeDetails',

    fireVueError: 'Click triggervuemistake',
    fireResourceError: 'Click trigger资源加载mistake',
    fireAjaxError: 'Click triggerajaxmistake',

    enableMessage: 'only in`/src/settings/projectSetting.ts` withinuseErrorHandle=trueeffective when.',
  },
  exception: {
    backLogin: 'Return to login',
    backHome: 'Return to homepage',
    subTitle403: 'Feel sorry，You do not have permission to access this page。',
    subTitle404: 'Feel sorry，The page you visited does not exist。',
    subTitle500: 'Feel sorry，服务器报告mistake。',
    noDataTitle: 'There is no data on the current page',
    networkErrorTitle: 'network error',
    networkErrorSubTitle: 'Feel sorry，Your network connection has been lost，Please check your network！',
  },
  lock: {
    unlock: 'Click to unlock',
    alert: '锁屏Wrong password',
    backToLogin: 'Return to login',
    entry: 'Enter the system',
    placeholder: 'Lock screen password',
  },
  login: {
    backSignIn: 'return',
    signInFormTitle: 'Log in',
    mobileSignInFormTitle: '手机Log in',
    qrSignInFormTitle: '二维码Log in',
    signUpFormTitle: 'register',
    forgetFormTitle: 'reset password',

    signInTitle: 'Jeecg Boot',
    signInDesc: 'is China’s most influential Enterprise-grade low-code platform！Online development，Visual drag and drop design，Zero code implementation80%basic functions~',
    policy: 'I agree to Knockout Cloud Privacy Policy',
    scanSign: `After scanning the code，即可完成Log in`,
    scanSuccess: `Scan code successfully，Log in中`,

    loginButton: 'Log in',
    registerButton: 'register',
    rememberMe: 'remember me',
    forgetPassword: 'forget the password?',
    otherSignIn: '其他Log in方式',

    // notify
    loginSuccessTitle: 'Log in成功',
    loginSuccessDesc: 'welcome back',

    // placeholder
    accountPlaceholder: 'Please enter account number',
    passwordPlaceholder: 'Please enter password',
    inputCodePlaceholder: 'Please enter the verification code',
    smsPlaceholder: 'Please enter the verification code',
    mobilePlaceholder: 'Please enter mobile phone number',
    mobileCorrectPlaceholder: 'Please enter the correct mobile phone number',
    policyPlaceholder: '勾选后才能register',
    diffPwd: 'The password entered twice is inconsistent',

    userName: 'account',
    password: 'password',
    inputCode: 'Verification code',
    confirmPassword: '确认password',
    email: 'Mail',
    smsCode: '短信Verification code',
    mobile: 'phone number',

    subTitleText: '{0}秒后Return to login页面',

    //reset password页面中文
    authentication:'Verify identity',
    resetLoginPassword:'重置Log inpassword',
    resetSuccess:'Reset successful',
    nextStep:'Next step',
    goToLogin:'去Log in'
  },
};
