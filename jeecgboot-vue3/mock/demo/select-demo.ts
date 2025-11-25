import { MockMethod } from 'vite-plugin-mock';
import { resultSuccess, baseUrl } from '../_util';

const demoList = (keyword, count = 20) => {
  const result = {
    list: [] as any[],
  };
  for (let index = 0; index < count; index++) {
    //Match based on search keywords
    let name = `Options${index}`;
    if(keyword && name.indexOf(keyword)!=-1){
      result.list.push({
        name: `Options${index}`,
        id: `${index}`,
      });
    }else if(!keyword){
      result.list.push({
        name: `Options${index}`,
        id: `${index}`,
      });
    }
  }
  return result;
};

export default [
  {
    url: `${baseUrl}/select/getDemoOptions`,
    timeout: 1000,
    method: 'get',
    response: ({ query }) => {
      const { keyword,count} = query;
      console.log("Query conditions：", keyword);
      return resultSuccess(demoList(keyword,count));
    },
  },
] as MockMethod[];
