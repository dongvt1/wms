import {areaList} from '@vant/area-data'
import {freezeDeep} from "@/utils/common/compUtils";

// Flat province and city data
export const pcaa = freezeDeep(usePlatPcaaData())

/**
 * 获取Flat province and city data
 */
function usePlatPcaaData() {
  const {city_list: city, county_list: county, province_list: province} = areaList;
  const dataMap = new Map<string, Recordable>()
  const flatData: Recordable = {'86': province}
  // Province
  Object.keys(province).forEach((code) => {
    flatData[code] = {}
    dataMap.set(code.slice(0, 2), flatData[code])
  })
  // urban area
  Object.keys(city).forEach((code) => {
    flatData[code] = {}
    dataMap.set(code.slice(0, 4), flatData[code])
    // Fill in the previous level
    const getProvince = dataMap.get(code.slice(0, 2))
    if (getProvince) {
      getProvince[code] = city[code]
    }
  });
  // county
  Object.keys(county).forEach((code) => {
    // Fill in the previous level
    const getCity = dataMap.get(code.slice(0, 4))
    if (getCity) {
      getCity[code] = county[code]
    }
  });
  return flatData
}