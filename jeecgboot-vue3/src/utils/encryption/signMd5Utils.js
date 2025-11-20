import md5 from 'md5';
// Signature key string (front-end and back-end must be consistent, please modify by yourself for official release)
const signatureSecret = 'dd05f1c54d63749eda95f9fa6d49v442a';

export default class signMd5Utils {
  /**
   * JSON parameter ascending order
   * @param jsonObj Send parameters
   */

  static sortAsc(jsonObj) {
    let arr = new Array();
    let num = 0;
    for (let i in jsonObj) {
      arr[num] = i;
      num++;
    }
    let sortArr = arr.sort();
    let sortObj = {};
    for (let i in sortArr) {
      sortObj[sortArr[i]] = jsonObj[sortArr[i]];
    }
    return sortObj;
  }

  /**
   * @param url Requested url, should contain request parameters (parameters after ? in url)
   * @param requestParams Request parameters (@RequestParam(get) JSON parameters)
   * @param requestBodyParams Request parameters (@RequestBody(post) parameters)
   * @returns {string} Get signature
   */
  static getSign(url, requestParams, requestBodyParams) {
    let urlParams = this.parseQueryString(url);
    let jsonObj = this.mergeObject(urlParams, requestParams);
    //update-begin---author:wangshuai---date:2024-04-16---for:【QQYUN-9005】Send SMS signature---
    if(requestBodyParams){
      jsonObj = this.mergeObject(jsonObj, requestBodyParams)
    }
    //update-end---author:wangshuai---date:2024-04-16---for:【QQYUN-9005】Send SMS signature---
    let requestBody = this.sortAsc(jsonObj);
    delete requestBody._t;
    // console.log('sign requestBody:', requestBody);
    return md5(JSON.stringify(requestBody) + signatureSecret).toUpperCase();
  }

  /**
   * @param url Requested url
   * @returns {{}} Assemble request parameters in url into json object (parameters after ? in url)
   */
  static parseQueryString(url) {
    let urlReg = /^[^\?]+\?([\w\W]+)$/,
      paramReg = /([^&=]+)=([\w\W]*?)(&|$|#)/g,
      urlArray = urlReg.exec(url),
      result = {};

    // Get parameter variable with comma at the end of URL sys/dict/getDictItems/sys_user,realname,username
    //【No encode here】Example with conditional parameters: /sys/dict/getDictItems/sys_user,realname,id,username!='admin'%20order%20by%20create_time
    let lastpathVariable = url.substring(url.lastIndexOf('/') + 1);
    if (lastpathVariable.includes(',')) {
      if (lastpathVariable.includes('?')) {
        lastpathVariable = lastpathVariable.substring(0, lastpathVariable.indexOf('?'));
      }
      //update-begin---author:wangshuai ---date:20221103  for：[issues/183]Drop down search，Use dynamic dictionaries，The online page does not report errors，The generated code reports an error ------------
      //solveSign Signature verification failed #2728
      //decodeURINo encoding or decoding capabilities for special characters，Need to usedecodeURIComponent
      result['x-path-variable'] = decodeURIComponent(lastpathVariable);
      //update-end---author:wangshuai ---date:20221103  for：[issues/183]Dropdown search, using dynamic dictionary, online page does not report error, generated code reports error ------------
    }
    if (urlArray && urlArray[1]) {
      let paramString = urlArray[1],
        paramResult;
      while ((paramResult = paramReg.exec(paramString)) != null) {
        //Convert numeric values to string type, keep front-end and back-end encryption rules consistent
        if (this.myIsNaN(paramResult[2])) {
          paramResult[2] = paramResult[2].toString();
        }
        result[paramResult[1]] = paramResult[2];
      }
    }
    return result;
  }

  /**
   * @returns {*} Merge two objects into one
   */
  static mergeObject(objectOne, objectTwo) {
    if (objectTwo && Object.keys(objectTwo).length > 0) {
      for (let key in objectTwo) {
        if (objectTwo.hasOwnProperty(key) === true) {
          //Convert numeric values to string type, keep front-end and back-end encryption rules consistent
          if (this.myIsNaN(objectTwo[key])) {
            objectTwo[key] = objectTwo[key].toString();
          }
          //Convert boolean type to string type, keep front-end and back-end encryption rules consistent
          if (typeof objectTwo[key] === 'boolean') {
            objectTwo[key] = objectTwo[key].toString();
          }
          objectOne[key] = objectTwo[key];
        }
      }
    }
    return objectOne;
  }

  static urlEncode(param, key, encode) {
    if (param == null) return '';
    let paramStr = '';
    let t = typeof param;
    if (t == 'string' || t == 'number' || t == 'boolean') {
      paramStr += '&' + key + '=' + (encode == null || encode ? encodeURIComponent(param) : param);
    } else {
      for (let i in param) {
        let k = key == null ? i : key + (param instanceof Array ? '[' + i + ']' : '.' + i);
        paramStr += this.urlEncode(param[i], k, encode);
      }
    }
    return paramStr;
  }

  /**
   * Interface signature used to generate timestamp in header
   * @returns {number}
   */
  static getTimestamp() {
    return new Date().getTime();
  }

  // static getDateTimeToString() {
  //   const date_ = new Date()
  //   const year = date_.getFullYear()
  //   let month = date_.getMonth() + 1
  //   let day = date_.getDate()
  //   if (month < 10) month = '0' + month
  //   if (day < 10) day = '0' + day
  //   let hours = date_.getHours()
  //   let mins = date_.getMinutes()
  //   let secs = date_.getSeconds()
  //   const msecs = date_.getMilliseconds()
  //   if (hours < 10) hours = '0' + hours
  //   if (mins < 10) mins = '0' + mins
  //   if (secs < 10) secs = '0' + secs
  //   if (msecs < 10) secs = '0' + msecs
  //   return year + '' + month + '' + day + '' + hours + '' + mins + '' + secs
  // }
  // true: numeric type, false: non-numeric type
  static myIsNaN(value) {
    return typeof value === 'number' && !isNaN(value);
  }
}
