import {pcaa as REGION_DATA} from "@/utils/areaData/pcaUtils";
import { cloneDeep } from 'lodash-es';

// Code to Chinese character mapping object
const CodeToText = {};
// Chinese character to code mapping object
const TextToCode = {};
const provinceObject = REGION_DATA['86']; // Province object
const regionData = [];
let provinceAndCityData = [];

CodeToText[''] = 'All';

// Calculate provinces
for (const prop in provinceObject) {
  regionData.push({
    value: prop, // Province code value
    label: provinceObject[prop], // Province Chinese characters
  });
  CodeToText[prop] = provinceObject[prop];
  TextToCode[provinceObject[prop]] = {
    code: prop,
  };
  TextToCode[provinceObject[prop]]['all'] = {
    code: '',
  };
}
// Calculate cities
for (let i = 0, len = regionData.length; i < len; i++) {
  const provinceCode = regionData[i].value;
  const provinceText = regionData[i].label;
  const provinceChildren = [];
  for (const prop in REGION_DATA[provinceCode]) {
    provinceChildren.push({
      value: prop,
      label: REGION_DATA[provinceCode][prop],
    });
    CodeToText[prop] = REGION_DATA[provinceCode][prop];
    TextToCode[provinceText][REGION_DATA[provinceCode][prop]] = {
      code: prop,
    };
    TextToCode[provinceText][REGION_DATA[provinceCode][prop]]['all'] = {
      code: '',
    };
  }
  if (provinceChildren.length) {
    regionData[i].children = provinceChildren;
  }
}
provinceAndCityData = cloneDeep(regionData);

// calculation area
for (let i = 0, len = regionData.length; i < len; i++) {
  const province = regionData[i].children;
  const provinceText = regionData[i].label;
  if (province) {
    for (let j = 0, len = province.length; j < len; j++) {
      const cityCode = province[j].value;
      const cityText = province[j].label;
      const cityChildren = [];
      for (const prop in REGION_DATA[cityCode]) {
        cityChildren.push({
          value: prop,
          label: REGION_DATA[cityCode][prop],
        });
        CodeToText[prop] = REGION_DATA[cityCode][prop];
        TextToCode[provinceText][cityText][REGION_DATA[cityCode][prop]] = {
          code: prop,
        };
      }
      if (cityChildren.length) {
        province[j].children = cityChildren;
      }
    }
  }
}

// Add "All" option
const provinceAndCityDataPlus = cloneDeep(provinceAndCityData);
provinceAndCityDataPlus.unshift({
  value: '',
  label: 'All',
});
for (let i = 0, len = provinceAndCityDataPlus.length; i < len; i++) {
  const province = provinceAndCityDataPlus[i].children;
  if (province && province.length) {
    province.unshift({
      value: '',
      label: 'All',
    });
    for (let j = 0, len = province.length; j < len; j++) {
      const city = province[j].children;
      if (city && city.length) {
        city.unshift({
          value: '',
          label: 'All',
        });
      }
    }
  }
}

const regionDataPlus = cloneDeep(regionData);
regionDataPlus.unshift({
  value: '',
  label: 'All',
});
for (let i = 0, len = regionDataPlus.length; i < len; i++) {
  const province = regionDataPlus[i].children;
  if (province && province.length) {
    province.unshift({
      value: '',
      label: 'All',
    });

    for (let j = 0, len = province.length; j < len; j++) {
      const city = province[j].children;
      if (city && city.length) {
        city.unshift({
          value: '',
          label: 'All',
        });
      }
    }
  }
}
//--begin--@updateBy:liusq----date:20210922---for:Province-city-district three-level linkage requirement method-----
// Province data
const provinceOptions = [];
for (const prop in provinceObject) {
  provinceOptions.push({
    value: prop, // Province code value
    label: provinceObject[prop], // Province Chinese characters
  });
}
/**
 * Get dropdown option data based on code
 * @param code
 * @returns []
 */
function getDataByCode(code) {
  let data = [];
  for (const prop in REGION_DATA[code]) {
    data.push({
      value: prop, // Province code value
      label: REGION_DATA[code][prop], // Province Chinese characters
    });
  }
  return data;
}

/**
 * Get all province-city-district levels
 * @type {Array}
 */
const pca = [];
Object.keys(provinceObject).map((province) => {
  pca.push({ id: province, text: provinceObject[province], pid: '86', index: 1 });
  const cityObject = REGION_DATA[province];
  Object.keys(cityObject).map((city) => {
    pca.push({ id: city, text: cityObject[city], pid: province, index: 2 });
    const areaObject = REGION_DATA[city];
    if (areaObject) {
      Object.keys(areaObject).map((area) => {
        pca.push({ id: area, text: areaObject[area], pid: city, index: 3 });
      });
    }
  });
});

/**
 * Infer value from code
 * @param code
 * @param level
 * @returns {Array}
 */
function getRealCode(code, level) {
  let arr = [];
  getPcode(code, arr, level);
  return arr;
}
function getPcode(id, arr, index) {
  for (let item of pca) {
    if (item.id === id && item.index == index) {
      arr.unshift(id);
      if (item.pid != '86') {
        getPcode(item.pid, arr, --index);
      }
    }
  }
}
//--end--@updateBy:liusq----date:20210922---for:Province-city-district three-level linkage requirement method-----
export { provinceAndCityData, regionData, provinceAndCityDataPlus, regionDataPlus, getDataByCode, provinceOptions, getRealCode };
