import { dateUtil } from '/@/utils/dateUtil';
import { duplicateCheck } from '/@/views/system/user/user.api';

export const rules = {
  rule(type, required) {
    if (type === 'email') {
      return this.email(required);
    }
    if (type === 'phone') {
      return this.phone(required);
    }
  },
  email(required) {
    return [
      {
        required: required ? required : false,
        validator: async (_rule, value) => {
          if (required == true && !value) {
            return Promise.reject('Please enter your email!');
          }
          if (
            value &&
            !new RegExp(
              /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
            ).test(value)
          ) {
            return Promise.reject('Please enter the correct email format!');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ] as ArrayRule;
  },
  phone(required) {
    return [
      {
        required: required,
        validator: async (_, value) => {
          if (required && !value) {
            return Promise.reject('Please enter mobile phone number!');
          }
          if (!/^1[3456789]\d{9}$/.test(value)) {
            return Promise.reject('Mobile number format is wrong');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  startTime(endTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('Please select a start time');
          }
          if (endTime && value && dateUtil(endTime).isBefore(value)) {
            return Promise.reject('The start time must be less than the end time');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  endTime(startTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('Please select end time');
          }
          if (startTime && value && dateUtil(value).isBefore(startTime)) {
            return Promise.reject('The end time needs to be greater than the start time');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  confirmPassword(values, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (!value) {
            return Promise.reject('Password cannot be empty');
          }
          if (value !== values.password) {
            return Promise.reject('The passwords entered twice are inconsistent!');
          }
          return Promise.resolve();
        },
      },
    ];
  },
  duplicateCheckRule(tableName, fieldName, model, schema, required?) {
    return [
      {
        validator: (_, value) => {
          if (!value && required) {
            return Promise.reject(`Please enter${schema.label}`);
          }
          return new Promise<void>((resolve, reject) => {
            duplicateCheck({
              tableName,
              fieldName,
              fieldVal: value,
              dataId: model.id,
            })
              .then((res) => {
                res.success ? resolve() : reject(res.message || 'Verification failed');
              })
              .catch((err) => {
                reject(err.message || 'Authentication failed');
              });
          });
        },
      },
    ] as ArrayRule;
  },
};

//update-begin-author:taoyan date:2022-6-16 for: code generation-For native forms
/**
 * unique verification function，Give to native<a-form>use，vben的表单校验建议use上述rules
 * @param tableName table name
 * @param fieldName Field name
 * @param fieldVal field value
 * @param dataId dataID
 */
export async function duplicateValidate(tableName, fieldName, fieldVal, dataId) {
  try {
    let params = {
      tableName,
      fieldName,
      fieldVal,
      dataId: dataId,
    };
    const res = await duplicateCheck(params);
    if (res.success) {
      return Promise.resolve();
    } else {
      return Promise.reject(res.message || 'Verification failed');
    }
  } catch (e) {
    return Promise.reject('Verification failed,可能是断网等问题导致的Verification failed');
  }
}
//update-end-author:taoyan date:2022-6-16 for: code generation-For native forms
