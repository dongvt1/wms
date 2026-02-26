import CronParser from 'cron-parser';
import type { ValidatorRule } from 'ant-design-vue/lib/form/interface';

const cronRule: ValidatorRule = {
  validator({}, value) {
    // If it is not filled in, it will not be verified.
    if (!value) {
      return Promise.resolve();
    }
    const values: string[] = value.split(' ').filter((item) => !!item);
    if (values.length > 7) {
      return Promise.reject('CronMost expressions7item！');
    }
    // Check the section7item
    let val: string = value;
    if (values.length === 7) {
      const year = values[6];
      if (year !== '*' && year !== '?') {
        let yearValues: string[] = [];
        if (year.indexOf('-') >= 0) {
          yearValues = year.split('-');
        } else if (year.indexOf('/')) {
          yearValues = year.split('/');
        } else {
          yearValues = [year];
        }
        // Determine whether they are all numbers
        const checkYear = yearValues.some((item) => isNaN(Number(item)));
        if (checkYear) {
          return Promise.reject('Cronexpression parameters[Year]mistake：' + year);
        }
      }
      // 取其中的前六item
      val = values.slice(0, 6).join(' ');
    }
    // 6Bit 没有Year
    // 5Bit没有秒、Year
    try {
      const iter = CronParser.parseExpression(val);
      iter.next();
      return Promise.resolve();
    } catch (e) {
      return Promise.reject('Cron表达式mistake：' + e);
    }
  },
};

export default cronRule.validator;
