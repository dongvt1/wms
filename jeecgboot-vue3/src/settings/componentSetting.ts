// General configuration for configuring certain components，without modifying components

import type { SorterResult } from '../components/Table';

export default {
  // Table configuration
  table: {
    // Table interface requests common configuration，Available in componentspropcover
    // support xxx.xxx.xxxFormat
    fetchSetting: {
      // The current page field passed to the background
      pageField: 'pageNo',
      // How many fields are passed to the background to display on each page?
      sizeField: 'pageSize',
      // The interface returns fields of form data
      listField: 'records',
      // The interface returns the fields of the total number of tables
      totalField: 'total',
    },
    // Optional paging options
    pageSizeOptions: ['10', '50', '80', '100'],
    // table default size
    defaultSize: 'middle',
    //How many items are displayed per page by default
    defaultPageSize: 10,
    // Default sort method
    defaultSortFn: (sortInfo: SorterResult) => {
      //update-begin-author:taoyan date:2022-10-21 for: VUEN-2199【form designer】Sort by multiple fields
      if(sortInfo instanceof Array){
        let sortInfoArray:any[] = []
        for(let item of sortInfo){
          let info = getSort(item);
          if(info){
            sortInfoArray.push(info)
          }
        }
        return {
          sortInfoString: JSON.stringify(sortInfoArray)
        }
      }else{
        let info = getSort(sortInfo)
        return info || {}
      }
      //update-end-author:taoyan date:2022-10-21 for: VUEN-2199【form designer】Sort by multiple fields
    },
    // Custom filtering method
    defaultFilterFn: (data: Partial<Recordable<string[]>>) => {
      return data;
    },
    // update-begin--author:liaozhiyang---date:20240424---for：【issues/1188】BasicTableplusscrollToFirstRowOnChangetype definition
    scrollToFirstRowOnChange: false,
    // update-end--author:liaozhiyang---date:20240424---for：【issues/1188】BasicTableplusscrollToFirstRowOnChangetype definition
  },
  // Scroll component configuration
  scrollbar: {
    // Whether to use native scrolling style
    // After opening，menu，Pop-up window，The drawer will use the native scrollbar component
    native: false,
  },
  //Form configuration
  form: {
    labelCol: {
      xs: { span: 24 },
      sm: { span: 4 },
      xl: { span: 6 },
      xxl: { span: 4 },
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 18 },
    },
    //form default colon
    colon: true,
  },
};

/**
 * Get sorting information
 * @param item
 */
function getSort(item){
  const { field, order } = item;
  if (field && order) {
    let sortType = 'ascend' == order ? 'asc' : 'desc';
    return {
      // sort field
      column: field,
      // sort by asc/desc
      order: sortType,
    };
  }
  return ''
}
