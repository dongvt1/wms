function fnW(str) {
    var num;
    str >= 10 ? num = str : num = "0" + str;
    return num;
}
//Get current time
var timer = setInterval(function () {
    var date = new Date();
    var year = date.getFullYear(); //current year
    var month = date.getMonth(); //current month
    var data = date.getDate(); //sky
    var hours = date.getHours(); //Hour
    var minute = date.getMinutes(); //point
    var second = date.getSeconds(); //Second
    var day = date.getDay(); //Get the current day of the week 
    var ampm = hours < 12 ? 'am' : 'pm';
    $('#time').html(fnW(hours) + ":" + fnW(minute) + ":" + fnW(second));
    $('#date').html('<span>' + year + '/' + (month + 1) + '/' + data + '</span><span>' + ampm + '</span><span>week' + day + '</span>')

}, 1000)



//Page map data
var geoCoordMap = {
    'Haimen': [121.15, 31.89],
    'Ordos': [109.781327, 39.608266],
    'Zhaoyuan': [120.38, 37.35],
    'Zhoushan': [122.207216, 29.985295],
    'Qiqihar': [123.97, 47.33],
    'Yancheng': [120.13, 33.38],
    'chifeng': [118.87, 42.28],
    'Qingdao': [120.33, 36.07],
    'Rushan': [121.52, 36.89],
    'Jinchang': [102.188043, 38.520089],
    'Quanzhou': [118.58, 24.93],
    'Lacey': [120.53, 36.86],
    'sunshine': [119.46, 35.42],
    'Jiaonan': [119.97, 35.88],
    'nantong': [121.05, 32.08],
    'Lhasa': [91.11, 29.97],
    'Yunfu': [112.02, 22.93],
    'Meizhou': [116.1, 24.55],
    'Wendeng': [122.05, 37.2],
    'Shanghai': [121.48, 31.22],
    'panzhihua': [101.718637, 26.582347],
    'Weihai': [122.1, 37.5],
    'Chengde': [117.93, 40.97],
    'Xiamen': [118.1, 24.46],
    'Shanwei': [115.375279, 22.786211],
    'Chaozhou': [116.63, 23.68],
    'Dandong': [124.37, 40.13],
    'Taicang': [121.1, 31.45],
    'Qujing': [103.79, 25.51],
    'Yantai': [121.39, 37.52],
    'fuzhou': [119.3, 26.08],
    'Wafangdian': [121.979603, 39.627114],
    'Jimo': [120.45, 36.38],
    'Fushun': [123.97, 41.97],
    'Yuxi': [102.52, 24.35],
    'Zhangjiakou': [114.87, 40.82],
    'Yangquan': [113.57, 37.85],
    'laizhou': [119.942327, 37.177017],
    'Huzhou': [120.1, 30.86],
    'Shantou': [116.69, 23.39],
    'Kunshan': [120.95, 31.39],
    'Ningbo': [121.56, 29.86],
    'Zhanjiang': [110.359377, 21.270708],
    'Jieyang': [116.35, 23.55],
    'Rongcheng': [122.41, 37.16],
    'Lianyungang': [119.16, 34.59],
    'huludao': [120.836932, 40.711052],
    'Changshu': [120.74, 31.64],
    'dongguan': [113.75, 23.04],
    'Heyuan': [114.68, 23.73],
    'Huai'an': [119.15, 33.5],
    'Taizhou': [119.9, 32.49],
    'Nanning': [108.33, 22.84],
    'Yingkou': [122.18, 40.65],
    'Huizhou': [114.4, 23.09],
    'Jiangyin': [120.26, 31.91],
    'Penglai': [120.75, 37.8],
    'Shaoguan': [113.62, 24.84],
    'Jiayuguan': [98.289152, 39.77313],
    'Guangzhou': [113.23, 23.16],
    'Yan'an': [109.47, 36.6],
    'Taiyuan': [112.53, 37.87],
    'Qingyuan': [113.01, 23.7],
    'Zhongshan': [113.38, 22.52],
    'Kunming': [102.73, 25.04],
    'Shouguang': [118.73, 36.86],
    'Panjin': [122.070714, 41.119997],
    'Changzhi': [113.08, 36.18],
    'Shenzhen': [114.07, 22.62],
    'Zhuhai': [113.52, 22.3],
    'suqian': [118.3, 33.96],
    'Xianyang': [108.72, 34.36],
    'Tongchuan': [109.11, 35.09],
    'Flatness': [119.97, 36.77],
    'foshan': [113.11, 23.05],
    'Haikou': [110.35, 20.02],
    'Jiangmen': [113.06, 22.61],
    'Zhangqiu': [117.53, 36.72],
    'Zhaoqing': [112.44, 23.05],
    'Dalian': [121.62, 38.92],
    'Linfen': [111.5, 36.08],
    'Wujiang': [120.63, 31.16],
    'Shizuishan': [106.39, 39.04],
    'shenyang': [123.38, 41.8],
    'suzhou': [120.62, 31.32],
    'Maoming': [110.88, 21.68],
    'Jiaxing': [120.76, 30.77],
    'Changchun': [125.35, 43.88],
    'Jiaozhou': [120.03336, 36.264622],
    'Yinchuan': [106.27, 38.47],
    'Zhangjiagang': [120.555821, 31.875428],
    'Sanmenxia': [111.19, 34.76],
    'Jinzhou': [121.15, 41.13],
    'Nanchang': [115.89, 28.68],
    'Liuzhou': [109.4, 24.33],
    'Sanya': [109.511909, 18.252847],
    'Zigong': [104.778442, 29.33903],
    'Jilin': [126.57, 43.87],
    'Yangjiang': [111.95, 21.85],
    'Luzhou': [105.39, 28.91],
    'Xining': [101.74, 36.56],
    'Yibin': [104.56, 29.77],
    'Hohhot': [111.65, 40.82],
    'Chengdu': [104.06, 30.67],
    'Datong': [113.3, 40.12],
    'Zhenjiang': [119.44, 32.2],
    'Guilin': [110.28, 25.29],
    'Zhangjiajie': [110.479191, 29.117096],
    'Yixing': [119.82, 31.36],
    'North Sea': [109.12, 21.49],
    'Xi'an': [108.95, 34.27],
    'Jintan': [119.56, 31.74],
    'Dongying': [118.49, 37.46],
    'Mudanjiang': [129.58, 44.6],
    'Zunyi': [106.9, 27.7],
    'Shaoxing': [120.58, 30.01],
    'Yangzhou': [119.42, 32.39],
    'Changzhou': [119.95, 31.79],
    'Weifang': [119.1, 36.62],
    'Chongqing': [106.54, 29.59],
    'Taizhou': [121.420757, 28.656386],
    'Nanjing': [118.78, 32.04],
    'binzhou': [118.03, 37.36],
    'Guiyang': [106.71, 26.57],
    'Wuxi': [120.29, 31.59],
    'Benxi': [123.73, 41.3],
    'Karamay': [84.77, 45.59],
    'Weinan': [109.5, 34.52],
    'Ma'anshan': [118.48, 31.56],
    'Baoji': [107.15, 34.38],
    'Jiaozuo': [113.21, 35.24],
    'Jurong': [119.16, 31.95],
    'Beijing': [116.46, 39.92],
    'Xuzhou': [117.2, 34.26],
    'Hengshui': [115.72, 37.72],
    'Baotou': [110, 40.58],
    'Mianyang': [104.73, 31.48],
    'Urumqi': [87.68, 43.77],
    'Zaozhuang': [117.57, 34.86],
    'Hangzhou': [120.19, 30.26],
    'Zibo': [118.05, 36.78],
    'Anshan': [122.85, 41.12],
    'Liyang': [119.48, 31.43],
    'Korla': [86.06, 41.68],
    'Anyang': [114.35, 36.1],
    'kaifeng': [114.35, 34.79],
    'Jinan': [117, 36.65],
    'Deyang': [104.37, 31.13],
    'Wenzhou': [120.65, 28.01],
    'Jiujiang': [115.97, 29.71],
    'Handan': [114.47, 36.6],
    'Lin'an': [119.72, 30.23],
    'Lanzhou': [103.73, 36.03],
    'Cangzhou': [116.83, 38.33],
    'Linyi': [118.35, 35.05],
    'Nanchong': [106.110698, 30.837793],
    'sky津': [117.2, 39.13],
    'fuyang': [119.95, 30.07],
    'Taian': [117.13, 36.18],
    'Zhuji': [120.23, 29.71],
    'Zhengzhou': [113.65, 34.76],
    'Harbin': [126.63, 45.75],
    'Liaocheng': [115.97, 36.45],
    'Wuhu': [118.38, 31.33],
    'Tangshan': [118.02, 39.63],
    'Pingdingshan': [113.29, 33.75],
    'xingtai': [114.48, 37.05],
    'Texas': [116.29, 37.45],
    'Jining': [116.59, 35.38],
    'Jingzhou': [112.239741, 30.335165],
    'Yichang': [111.3, 30.7],
    'Yiwu': [120.06, 29.32],
    'Lishui': [119.92, 28.45],
    'Luoyang': [112.44, 34.7],
    'Qinhuangdao': [119.57, 39.95],
    'Zhuzhou': [113.16, 27.83],
    'Shijiazhuang': [114.48, 38.03],
    'Laiwu': [117.67, 36.19],
    'Changde': [111.69, 29.05],
    'Baoding': [115.48, 38.85],
    'Xiangtan': [112.91, 27.87],
    'Jinhua': [119.64, 29.12],
    'Yueyang': [113.09, 29.37],
    'Changsha': [113, 28.21],
    'Quzhou': [118.88, 28.97],
    'Langfang': [116.7, 39.53],
    'Heze': [115.480656, 35.23375],
    'hefei': [117.27, 31.86],
    'Wuhan': [114.31, 30.52],
    'Daqing': [125.03, 46.58],
    'Anhui Province': [117.17, 31.52],
    'Beijing市': [116.24, 39.55],
    'Chongqing市': [106.54, 29.59],
    'Fujian Province': [119.18, 26.05],
    'Gansu Province': [103.51, 36.04],
    'Guangdong Province': [113.14, 23.08],
    'Guangxi Zhuang Autonomous Region': [108.19, 22.48],
    'Guizhou Province': [106.42, 26.35],
    'Hainan Province': [110.20, 20.02],
    'Hebei Province': [114.30, 38.02],
    'Henan Province': [113.40, 34.46],
    'Heilongjiang Province': [128.36, 45.44],
    'hubei province': [112.27, 30.15],
    'Hunan Province': [112.59, 28.12],
    'Jilin省': [125.19, 43.54],
    'Jiangsu Province': [118.46, 32.03],
    'Jiangxi Province': [115.55, 28.40],
    'Liaoning Province': [123.25, 41.48],
    'Inner Mongolia': [108.41, 40.48],
    'Inner Mongoliaautonomous region': [108.41, 40.48],
    'Ningxia Hui Autonomous Region': [106.16, 38.27],
    'Qinghai Province': [101.48, 36.38],
    'Shandong Province': [118.00, 36.40],
    'Shanxi Province': [112.33, 37.54],
    'Shaanxi Province': [108.57, 34.17],
    'Shanghai市': [121.29, 31.14],
    'Hainan': [108.77, 19.10],
    'Sichuan Province': [104.04, 30.40],
    'sky津市': [117.12, 39.02],
    'Tibet Autonomous Region': [91.08, 29.39],
    'Xinjiang Uygur Autonomous Region': [87.36, 43.45],
    'Yunnan Province': [102.42, 25.04],
    'Zhejiang Province': [120.10, 30.16],
    'Macao Special Administrative Region': [115.07, 21.33],
    'Taiwan Province': [121.21, 23.53],
    'Hong Kong Special Administrative Region': [114.1, 22.2]
};

$('.select').on('blur', function () {
        $(this).find('.select-ul').hide();
    })
    //Click the drop-down box to display the contents of the drop-down box
$('.select-div').on('click', function () {
    if ($(this).siblings('.select-ul').is(":hidden")) {
        $(this).siblings('.select-ul').show();
    } else {
        $(this).siblings('.select-ul').hide();
    }
})


$('.select-ul').on('click', 'li', function () {
    $(this).addClass('active').siblings('li').removeClass('active').parent().hide().siblings('.select-div').html($(this).html());
    var parentDiv = $(this).parent().parent().parent();
})

//Mouse slide to button，Button content turns white
var imgName;
$('.title-box').children('button').hover(function () {
    imgName = $(this).children('img').attr('src').split('.png')[0];
    $(this).children('img').attr('src', imgName + '_on.png');
}, function () {
    $(this).children('img').attr('src', imgName + '.png');

});


var startColor = ['#0e94eb', '#c440ef', '#efb013', '#2fda07', '#d8ef13', '#2e4af8', '#0eebc4', '#f129b1', '#17defc', '#f86363'];
var borderStartColor = ['#0077c5', '#a819d7', '#c99002', '#24bc00', '#b6cb04', '#112ee2', '#00bd9c', '#ce078f', '#00b2cd', '#ec3c3c'];



//Proportion of warehousing volume，Pie chart with border effect
function chart1() {
    //data for simulated data
    var data = [{
        name: 'SF Express',
        value: 192581,
        percent: '30.8721',
    }, {
        name: 'Jingdong',
        value: 215635,
        percent: '34.076',
    }, {
        name: 'EMS',
        value: 224585,
        percent: '35.49',
    }];
    var myChart = echarts.init(document.getElementById('pie'));
    var myChart1 = echarts.init(document.getElementById('pie1'));
    window.addEventListener('resize', function () {
        myChart.resize();
        myChart1.resize();
    });

    var str = '';
    for (var i = 0; i < data.length; i++) {
        str += '<p><span><i class="legend" style="background:' + startColor[i] + '"></i></span>' + data[i].name + '<span class="pie-number" style="color:' + startColor[i] + '">' + data[i].value + '</span>' + Number(data[i].percent).toFixed(2) + '%</p>';
    }

    $('.pie-data').append(str);


    function deepCopy(obj) {
        if (typeof obj !== 'object') {
            return obj;
        }
        var newobj = {};
        for (var attr in obj) {
            newobj[attr] = obj[attr];
        }
        return newobj;
    }
    var xData = [],
        yData = [];
    data.map((a, b) => {
        xData.push(a.name);
        yData.push(a.value);
    });


    var RealData = [];
    var borderData = [];
    data.map((item, index) => {
        var newobj = deepCopy(item);
        var newobj1 = deepCopy(item);
        RealData.push(newobj);
        borderData.push(newobj1);
    });
    RealData.map((item, index) => {
        item.itemStyle = {
            normal: {
                color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [{
                        offset: 0,
                        color: startColor[index] // 0% color
                }, {
                        offset: 1,
                        color: startColor[index] // 100% color
                }],
                    globalCoord: false // The default is false
                },
            }
        }
    });
    borderData.map((item, index) => {
        item.itemStyle = {
            normal: {
                color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [{
                        offset: 0,
                        color: borderStartColor[index] // 0% color
                }, {
                        offset: 1,
                        color: borderStartColor[index] // 100% color
                }],
                    globalCoord: false // The default is false
                },
            }
        }
    });
    var option = {
        tooltip: {
            trigger: 'item',
            //            position: ['30%', '50%'],
            confine: true,
            formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        series: [
        // main display layer
            {
                radius: ['50%', '85%'],
                center: ['50%', '50%'],
                type: 'pie',
                label: {
                    normal: {
                        show: false
                    },
                    emphasis: {
                        show: false
                    }
                },
                labelLine: {
                    normal: {
                        show: false
                    },
                    emphasis: {
                        show: false
                    }
                },
                name: "派piecesProportion of warehousing volume内容",
                data: RealData
        },
        // Border settings
            {
                radius: ['45%', '50%'],
                center: ['50%', '50%'],
                type: 'pie',
                label: {
                    normal: {
                        show: false
                    },
                    emphasis: {
                        show: false
                    }
                },
                labelLine: {
                    normal: {
                        show: false
                    },
                    emphasis: {
                        show: false
                    }
                },
                animation: false,
                tooltip: {
                    show: false
                },
                data: borderData
        }
    ]
    };

    myChart.setOption(option);
    myChart1.setOption(option);
}

chart1()

//----------------------派piecesProportion of warehousing volume内容end---------------

//------------Guangdong Province寄派piecesdata内容---------------
//Click the filter button
$('#filBtn').on('click', function () {
        if ($('#filCon').is(":hidden")) {
            $('#filCon').attr('style', 'display:flex');
        } else {
            $('#filCon').hide();
        }
    })
    //Click the filter buttonend


function chart2(chartType) {
    var data = [
        {
            name: 'Guangzhou市',
            value: 120057.34
            },
        {
            name: 'Shaoguan市',
            value: 15477.48
            },
        {
            name: 'Shenzhen市',
            value: 131686.1
            },
        {
            name: 'Zhuhai市',
            value: 6992.6
            },
        {
            name: 'Shantou市',
            value: 44045.49
            },
        {
            name: 'foshan市',
            value: 40689.64
            },
        {
            name: 'Jiangmen市',
            value: 37659.78
            },
        {
            name: 'Zhanjiang市',
            value: 45180.97
            },
        {
            name: 'Maoming市',
            value: 5204.26
            },
        {
            name: 'Zhaoqing市',
            value: 21900.9
            },
        {
            name: 'Huizhou市',
            value: 4918.26
            },
        {
            name: 'Meizhou市',
            value: 5881.84
            },
        {
            name: 'Shanwei市',
            value: 4178.01
            },
        {
            name: 'Heyuan市',
            value: 2227.92
            },
        {
            name: 'Yangjiang市',
            value: 2180.98
            },
        {
            name: 'Qingyuan市',
            value: 9172.94
            },
        {
            name: 'dongguan市',
            value: 3368
            },
        {
            name: 'Zhongshan市',
            value: 306.98
            },
        {
            name: 'Chaozhou市',
            value: 810.66
            },
        {
            name: 'Jieyang市',
            value: 542.2
            },
        {
            name: 'Yunfu市',
            value: 256.38
            }]

    var myChart = echarts.init(document.getElementById('gdMap'));
    var myCharts = echarts.init(document.getElementById('gdMaps'));
    window.addEventListener('resize', function () {
        myChart.resize();
        myCharts.resize();
    });
    var yMax = 0;
    for (var j = 0; j < data.length; j++) {
        if (yMax < data[j].value) {
            yMax = data[j].value;
        }
    }
        myChart.hideLoading();
        myCharts.hideLoading();
        var option = {
            animation: true,
            tooltip: {
                show: true
            },
            visualMap: {
                min: 0,
                max: yMax,
                text: ['high', 'Low'],
                orient: 'horizontal',
                itemWidth: 15,
                itemHeight: 200,
                right: 0,
                bottom: 30,
                inRange: {
                    color: ['#75ddff', '#0e94eb']
                },
                textStyle: {
                    color: 'white'
                }
            },
            series: [
                {
                    name: 'Data name',
                    type: 'map',
                    mapType: 'Guangdong',
                    selectedMode: 'multiple',
                    tooltip: {
                        trigger: 'item',
                        formatter: '{b}<br/>{c} (pieces)'
                    },
                    itemStyle: {
                        normal: {
                            borderWidth: 1,
                            borderColor: '#0e94eb',
                            label: {
                                show: false
                            }
                        },
                        emphasis: { // Also selected style
                            borderWidth: 1,
                            borderColor: '#fff',
                            backgroundColor: 'red',
                            label: {
                                show: true,
                                textStyle: {
                                    color: '#fff'
                                }
                            }
                        }
                    },
                    data: data,
            }
            ]
        };

        myChart.setOption(option);
        myCharts.setOption(option);
}
chart2('');

//------------Guangdong Province寄派piecesdata内容end---------------

//cityNameNational provincial administrative region data
var cityName = [{
    "ProID": 1,
    "name": "Beijing",
    "ProSort": 1,
    "firstP": "B",
    "ProRemark": "municipality"
}, {
    "ProID": 2,
    "name": "sky津",
    "ProSort": 2,
    "firstP": "T",
    "ProRemark": "municipality"
}, {
    "ProID": 3,
    "name": "Hebei",
    "ProSort": 5,
    "firstP": "H",
    "ProRemark": "province"
}, {
    "ProID": 4,
    "name": "Shanxi",
    "ProSort": 6,
    "firstP": "S",
    "ProRemark": "province"
}, {
    "ProID": 5,
    "name": "Inner Mongolia",
    "ProSort": 32,
    "firstP": "N",
    "ProRemark": "autonomous region"
}, {
    "ProID": 6,
    "name": "Liaoning",
    "ProSort": 8,
    "firstP": "L",
    "ProRemark": "province"
}, {
    "ProID": 7,
    "name": "Jilin",
    "ProSort": 9,
    "firstP": "J",
    "ProRemark": "province"
}, {
    "ProID": 8,
    "name": "Heilongjiang",
    "ProSort": 10,
    "firstP": "H",
    "ProRemark": "province"
}, {
    "ProID": 9,
    "name": "Shanghai",
    "ProSort": 3,
    "firstP": "S",
    "ProRemark": "municipality"
}, {
    "ProID": 10,
    "name": "Jiangsu",
    "ProSort": 11,
    "firstP": "J",
    "ProRemark": "province"
}, {
    "ProID": 11,
    "name": "Zhejiang",
    "ProSort": 12,
    "firstP": "Z",
    "ProRemark": "province"
}, {
    "ProID": 12,
    "name": "Anhui",
    "ProSort": 13,
    "firstP": "A",
    "ProRemark": "province"
}, {
    "ProID": 13,
    "name": "Fujian",
    "ProSort": 14,
    "firstP": "F",
    "ProRemark": "province"
}, {
    "ProID": 14,
    "name": "Jiangxi",
    "ProSort": 15,
    "firstP": "J",
    "ProRemark": "province"
}, {
    "ProID": 15,
    "name": "Shandong",
    "ProSort": 16,
    "firstP": "S",
    "ProRemark": "province"
}, {
    "ProID": 16,
    "name": "Henan",
    "ProSort": 17,
    "firstP": "H",
    "ProRemark": "province"
}, {
    "ProID": 17,
    "name": "hubei",
    "ProSort": 18,
    "firstP": "H",
    "ProRemark": "province"
}, {
    "ProID": 18,
    "name": "Hunan",
    "ProSort": 19,
    "firstP": "H",
    "ProRemark": "province"
}, {
    "ProID": 19,
    "name": "Guangdong",
    "ProSort": 20,
    "firstP": "G",
    "ProRemark": "province"
}, {
    "ProID": 20,
    "name": "Hainan",
    "ProSort": 24,
    "firstP": "H",
    "ProRemark": "province"
}, {
    "ProID": 21,
    "name": "Guangxi",
    "ProSort": 28,
    "firstP": "G",
    "ProRemark": "autonomous region"
}, {
    "ProID": 22,
    "name": "Gansu",
    "ProSort": 21,
    "firstP": "G",
    "ProRemark": "province"
}, {
    "ProID": 23,
    "name": "Shaanxi Province",
    "ProSort": 27,
    "firstP": "S",
    "ProRemark": "province"
}, {
    "ProID": 24,
    "name": "Xinjiang Uygur",
    "ProSort": 31,
    "firstP": "X",
    "ProRemark": "autonomous region"
}, {
    "ProID": 25,
    "name": "Qinghai",
    "ProSort": 26,
    "firstP": "Q",
    "ProRemark": "province"
}, {
    "ProID": 26,
    "name": "Ningxia",
    "ProSort": 30,
    "firstP": "N",
    "ProRemark": "autonomous region"
}, {
    "ProID": 27,
    "name": "Chongqing",
    "ProSort": 4,
    "firstP": "C",
    "ProRemark": "municipality"
}, {
    "ProID": 28,
    "name": "Sichuan Province",
    "ProSort": 22,
    "firstP": "S",
    "ProRemark": "province"
}, {
    "ProID": 29,
    "name": "Guizhou Province",
    "ProSort": 23,
    "firstP": "G",
    "ProRemark": "province"
}, {
    "ProID": 30,
    "name": "Yunnan Province",
    "ProSort": 25,
    "firstP": "Y",
    "ProRemark": "province"
}, {
    "ProID": 31,
    "name": "Tibet",
    "ProSort": 29,
    "firstP": "X",
    "ProRemark": "autonomous region"
}, {
    "ProID": 32,
    "name": "Taiwan",
    "ProSort": 7,
    "firstP": "T",
    "ProRemark": "province"
}, {
    "ProID": 33,
    "name": "Macao",
    "ProSort": 33,
    "firstP": "A",
    "ProRemark": "special administrative region"
}, {
    "ProID": 34,
    "name": "Hongkong",
    "ProSort": 34,
    "firstP": "X",
    "ProRemark": "special administrative region"
}]

addCityBtn(cityName);

function addCityBtn(data) {
    var li_con = '';
    for (var i = 0; i < data.length; i++) {
        li_con += '<li>' + data[i].name + '</li>'
    }
    $('#city').html(li_con);
    $('#citys').html(li_con);
}

$('.city-btn').on('click', 'li', function () {
    var str;
    var patt = [/[a-z]/i, /[a-e]/i, /[f-i]/i, /[k-o]/i, /[p-t]/i, /[u-z]/i];
    var index = $(this).index();
    var li_con = '';
    for (var i = 0; i < cityName.length; i++) {
        str = cityName[i].firstP;
        if (patt[index].test(str)) {
            li_con += '<li>' + cityName[i].name + '</li>'
        }
    }

    $(this).addClass('active').siblings('li').removeClass('active');
    if (index == 0) {
        $('#city').children().removeClass('active');
        if ($(this).parent().data('city') == 1) {
            $('.ranking-box').show();
            if ($("#barType").find('.active').data('value') == 1) {
                $('#titleQ').html('<span>Whole network</span>到Zhuhai');
            } else if ($("#barType").find('.active').data('value') == 2) {
                $('#titleQ').html('Zhuhai到<span>Whole network</span>')
            }
            $('#city').html(li_con);
        } else if ($(this).parent().data('city') == 2) {
            if ($('.cont-div').eq(0).css('visibility') != 'hidden') {
                $('.ranking-box').show();
            }
            if ($("#barTypes").find('.active').data('value') == 1) {
                $('#titleQs').html('<span>Whole network</span>到Zhuhai');
            } else if ($("#barTypes").find('.active').data('value') == 2) {
                $('#titleQs').html('Zhuhai到<span>Whole network</span>')
            }
            $('#citys').html(li_con);
        }
    } else {
        if ($(this).parent().data('city') == 1) {
            $('#city').html(li_con);
        } else if ($(this).parent().data('city') == 2) {
            $('#citys').html(li_con);
        }
    }


})


$('#city').on('click', 'li', function () {
    $(this).addClass('active').siblings('li').removeClass('active');
    $('.center-bottom .ranking-box').hide();
    if ($("#barType").find('.active').data('value') == 1) {
        $('#titleQ').html('<span>' + $(this).html() + '</span>到Zhuhai');
    } else if ($("#barType").find('.active').data('value') == 2) {
        $('#titleQ').html('Zhuhai到<span>' + $(this).html() + '</span>')
    }
})

$('#citys').on('click', 'li', function () {
    $(this).addClass('active').siblings('li').removeClass('active');
    $('.pop-data .ranking-box').hide();
    if ($("#barTypes").find('.active').data('value') == 1) {
        $('#titleQs').html('<span>' + $(this).html() + '</span>到Zhuhai');
    } else if ($("#barTypes").find('.active').data('value') == 2) {
        $('#titleQs').html('Zhuhai到<span>' + $(this).html() + '</span>')
    }
})

//寄派pieces选择
$("#barType").on('click', 'li', function () {
    $(this).addClass('active').siblings('li').removeClass('active');
    $('#barTitle').html($(this).html() + 'data');
    $('#tabBtn').data('state', $(this).data('value'));
    if ($(this).data('value') == 1) {
        $('.table1').eq(0).show().siblings('table').hide();
    } else if ($(this).data('value') == 2) {
        $('.table1').eq(1).show().siblings('table').hide();
    }
    chart3($(this).data('value'), 0);
    chart4(chart4Data, $(this).data('value'), 0);
})

//寄派pieces选择
$("#barTypes").on('click', 'li', function () {
    $(this).addClass('active').siblings('li').removeClass('active');
    $('#barTitles').html($(this).html() + 'data');
    $('#tabBtns').data('state', $(this).data('value'));
    if ($(this).data('value') == 1) {
        $('.table2').eq(0).show().siblings('table').hide();
    } else if ($(this).data('value') == 2) {
        $('.table2').eq(1).show().siblings('table').hide();
    }
    chart3($(this).data('value'), 1);
    chart4(chart4Data, $(this).data('value'), 1);

})


function chart3(type, chartType) {
    var myChart = echarts.init(document.getElementById('chart3'));
    var myCharts = echarts.init(document.getElementById('chart3s'));
    window.addEventListener('resize', function () {
        myChart.resize();
        myCharts.resize();
    });

    //    Set parameters for background shadow，获取data的最大值

    var data; //横坐标data，Not moving
    var data_; //模拟data
    if (type == 1) {
        data_ = [{
                name: "入库pieces",
                value: 584
            },
            {
                name: "滞留pieces",
                value: 152
            }, {
                name: "丢失pieces",
                value: 100
            },
            {
                name: "正常pieces",
                value: 689
            },
            {
                name: "派送pieces",
                value: 200
            }, {
                name: "自提pieces",
                value: 121
            }, {
                name: "退签pieces",
                value: 92
            }]
    } else if (type == 2) {
        data_ = [{
                name: "入库pieces",
                value: 568
                }, {
                name: "丢失pieces",
                value: 287
                }, {
                name: "滞留pieces",
                value: 120
                },
            {
                name: "撤销pieces",
                value: 152
                },
            {
                name: "出库pieces",
                value: 125
                }, {
                name: "正常pieces",
                value: 122
        }]
    }
    var series_data; //Draw charts的data
    //Draw charts
    var yMax = 0;
    for (var j = 0; j < data_.length; j++) {
        if (yMax < data_[j].value) {
            yMax = data_[j].value;
        }
    }
    var dataShadow = [];
    for (var i = 0; i < 10; i++) {
        dataShadow.push(yMax * 2);
    }

    if (type == 1) {
        data = ['入库pieces', '在库pieces', '出库pieces', '退签pieces', '丢失pieces'];

        if (chartType == '') {
            $(' .dph-data1').html(data_[0].value);
            $(' .dph-data2').html(data_[1].value + data_[3].value);
            $(' .dph-data3').html(data_[3].value);
            $(' .dph-data4').html(data_[2].value);
            $(' .dph-data5').html(data_[1].value);
            $(' .dph-data6').html(data_[4].value + data_[5].value);
            $(' .dph-data7').html(data_[4].value);
            $(' .dph-data8').html(data_[5].value);
            $(' .dph-data9').html(data_[6].value);
        } else if (chartType == 0) {
            $('.table1 .dph-data1').html(data_[0].value);
            $('.table1 .dph-data2').html(data_[1].value + data_[3].value);
            $('.table1 .dph-data3').html(data_[3].value);
            $('.table1 .dph-data4').html(data_[2].value);
            $('.table1 .dph-data5').html(data_[1].value);
            $('.table1 .dph-data6').html(data_[4].value + data_[5].value);
            $('.table1 .dph-data7').html(data_[4].value);
            $('.table1 .dph-data8').html(data_[5].value);
            $('.table1 .dph-data9').html(data_[6].value);
        } else if (chartType == 1) {
            $('.table2 .dph-data1').html(data_[0].value);
            $('.table2 .dph-data2').html(data_[1].value + data_[3].value);
            $('.table2 .dph-data3').html(data_[3].value);
            $('.table2 .dph-data4').html(data_[2].value);
            $('.table2 .dph-data5').html(data_[1].value);
            $('.table2 .dph-data6').html(data_[4].value + data_[5].value);
            $('.table2 .dph-data7').html(data_[4].value);
            $('.table2 .dph-data8').html(data_[5].value);
            $('.table2 .dph-data9').html(data_[6].value);
        }

        series_data = [
            { // For shadow
                type: 'bar',
                barWidth: 20,
                xAxisIndex: 2,
                tooltip: {
                    show: false
                },
                itemStyle: {
                    normal: {
                        color: 'rgba(14, 148, 235, 0.102)'
                    }
                },
                data: dataShadow,
                animation: false
            },
            {
                name: '入库pieces',
                type: 'bar',
                barGap: '-100%',
                barWidth: '40%',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: '#0e94eb'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [data_[0], 0, 0, 0, 0],
            },
            {
                name: '滞留pieces',
                type: 'bar',
                stack: '在库pieces',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(239,176,19,.9)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, data_[1], 0, 0, 0],
            },
            {
                name: '丢失pieces',
                type: 'bar',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(239,176,19,0.4)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, 0, 0, 0, data_[2]],
            },
            {
                name: '正常pieces',
                type: 'bar',
                stack: '在库pieces',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(239,176,19,0.3)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, data_[3], 0, 0, 0],
            },
            {
                name: '派送pieces',
                type: 'bar',
                stack: '出库pieces',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(196,64,239,0.8)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, 0, data_[4], 0, 0],
            },
            {
                name: '自提pieces',
                type: 'bar',
                stack: '出库pieces',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(196,64,239,0.4)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, 0, data_[5], 0, 0],
            },
            {
                name: '退签pieces',
                type: 'bar',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(219,44,44,0.8)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, 0, 0, data_[6], 0],
            }
        ]


    } else if (type == 2) {
        data = ['入库pieces', '在库pieces', '出库pieces', '丢失pieces', '撤销pieces'];
        if (chartType == '') {
            $('.mail-data1').html(data_[0].value);
            $('.mail-data2').html(data_[2].value + data_[5].value);
            $('.mail-data3').html(data_[1].value);
            $('.mail-data4').html(data_[2].value);
            $('.mail-data5').html(data_[3].value);
            $('.mail-data6').html(data_[4].value);
            $('.mail-data7').html(data_[5].value);
        } else if (chartType == 0) {
            $('.table1 .mail-data1').html(data_[0].value);
            $('.table1 .mail-data2').html(data_[2].value + data_[5].value);
            $('.table1 .mail-data3').html(data_[1].value);
            $('.table1 .mail-data4').html(data_[2].value);
            $('.table1 .mail-data5').html(data_[3].value);
            $('.table1 .mail-data6').html(data_[4].value);
            $('.table1 .mail-data7').html(data_[5].value);
        } else if (chartType == 1) {
            $('.table2 .mail-data1').html(data_[0].value);
            $('.table2 .mail-data2').html(data_[2].value + data_[5].value);
            $('.table2 .mail-data3').html(data_[1].value);
            $('.table2 .mail-data4').html(data_[2].value);
            $('.table2 .mail-data5').html(data_[3].value);
            $('.table2 .mail-data6').html(data_[4].value);
            $('.table2 .mail-data7').html(data_[5].value);
        }

        series_data = [
            { // For shadow
                type: 'bar',
                barWidth: 20,
                xAxisIndex: 2,
                tooltip: {
                    show: false
                },
                itemStyle: {
                    normal: {
                        color: 'rgba(14, 148, 235, 0.102)'
                    }
                },
                data: dataShadow,
                animation: false
            },
            {
                name: '入库pieces',
                barGap: '-100%',
                barWidth: '40%',
                type: 'bar',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: '#0e94eb'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [data_[0], 0, 0, 0, 0],
            },
            {
                name: '正常pieces',
                type: 'bar',
                stack: '在库pieces',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(239,176,19,.9)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, data_[5], 0, 0, 0, 0],
                },
            {
                name: '丢失pieces',
                type: 'bar',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(239,176,19,.9)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, 0, 0, data_[1], 0],
                    },
            {
                name: '滞留pieces',
                type: 'bar',
                xAxisIndex: 1,
                stack: '在库pieces',
                itemStyle: {
                    normal: {
                        color: 'rgba(239,176,19,0.4)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },

                data: [0, data_[2], 0, 0, 0],
                    },
            {
                name: '撤销pieces',
                type: 'bar',
                xAxisIndex: 1,
                itemStyle: {
                    normal: {
                        color: 'rgba(239,176,19,0.3)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, 0, 0, 0, data_[3]],
                    },
            {
                name: '出库pieces',
                type: 'bar',
                xAxisIndex: 1,
                stack: '退签pieces',
                itemStyle: {
                    normal: {
                        color: 'rgba(196,64,239,0.8)'
                    },
                    emphasis: {
                        opacity: 1
                    }
                },
                data: [0, 0, data_[4], 0, 0],
                    }

                    ]
    }

    var option = {
        title: '',
        grid: {
            top: '10%',
            containLabel: true
        },
        tooltip: {
            show: true
        },
        xAxis: [{
                type: 'category',
                show: false,
                data: data,
                axisLabel: {
                    textStyle: {
                        color: '#fff'
                    }
                }
            },
            {
                type: 'category',
                position: "bottom",
                data: data,
                boundaryGap: true,
                // offset: 40,
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#fff'
                    }
                }
            },
            {
                show: false,
                data: dataShadow,
                axisLabel: {
                    inside: true,
                    textStyle: {
                        color: '#fff'
                    }
                },
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: false
                },
                z: 10
        },
        ],
        yAxis: [{
                show: true,
                splitLine: {
                    show: false,
                    lineStyle: {
                        color: "#0e94eb"
                    }
                },
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: false
                },
                axisLabel: {
                    show: true,
                    color: '#0e94eb'
                }
        }, {
                show: false,
                type: "value",
                nameTextStyle: {
                    color: '#0e94eb'
                },
                axisLabel: {
                    color: '#0e94eb'
                },
                splitLine: {
                    show: false
                },
                axisLine: {
                    show: false
                },
                axisTick: {
                    show: false
                }
        },
            {
                axisLine: {
                    show: false
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                }
                }],
        //        color: ['#e54035'],
        series: series_data
    }
    if (chartType === '') {
        myChart.clear();
        myCharts.clear();
        myChart.setOption(option);
        myCharts.setOption(option);
    } else if (chartType === 0) {
        myChart.clear();
        myChart.setOption(option);
    } else if (chartType === 1) {
        myCharts.clear();
        myCharts.setOption(option);
    }
}

chart3(1, '')
    //
    //
    //
$('#dateBtn').on('click', function () {
    if ($('#timeBox').is(":hidden")) {
        $('#timeBox').show();
        document.getElementById('timeBox').focus();

    } else {
        $('#timeBox').hide();
    }
})

$('#dateBtns').on('click', function () {
    if ($('#timeBoxs').is(":hidden")) {
        $('#timeBoxs').show();
        document.getElementById('timeBoxs').focus();

    } else {
        $('#timeBoxs').hide();
    }
})

$('#switchBtn').on('click', 'span', function () {
    $(this).addClass('active').siblings().removeClass('active');
    if ($(this).data('datatype') == 'income') {
        $('#totalProfit').html('123,456.5Yuan');
    } else if ($(this).data('datatype') == 'expend') {
        $('#totalProfit').html('32,111.4Yuan');
    }
})

$('#tabBtn').on('click', function () {
    var _this = $(this);
    if ($('.right-top').children('.chart-box').is(':hidden')) {
        _this.children('span').html('chart');
        $('.right-top').children('.chart-box').show().siblings('.data-box').hide();

    } else {
        _this.children('span').html('sheet');
        $('.right-top').children('.data-box').show().siblings('.chart-box').hide();
        if (_this.data('state') == 1) {
            $('.table1').eq(0).show().siblings('table').hide();
        } else if (_this.data('state') == 2) {
            $('.table1').eq(1).show().siblings('table').hide();
        }
    }
})


$('#tabBtns').on('click', function () {
    var _this = $(this);
    if (_this.siblings('.pop-chart').is(':hidden')) {
        _this.children('span').html('chart');
        _this.siblings('.pop-chart').show().siblings('.data-box').hide();

    } else {
        _this.children('span').html('sheet');
        _this.siblings('.data-box').show().siblings('.chart-box').hide();
        if (_this.data('state') == 1) {
            $('.table2').eq(0).show().siblings('table').hide();
        } else if (_this.data('state') == 2) {
            $('.table2').eq(1).show().siblings('table').hide();
        }
    }
})




//time picker
var startV = '';
var endV = '';
laydate.skin('danlan');
var startTime = {
    elem: '#startTime',
    format: 'YYYY-MM-DD',
    min: '1997-01-01', //Set the minimum date to the current date
    max: laydate.now(), //maximum date
    istime: true,
    istoday: true,
    fixed: false,
    choose: function (datas) {
        startV = datas;
        endTime.min = datas; //After selecting the start date，Minimum date to reset end day
    }
};
var endTime = {
    elem: '#endTime',
    format: 'YYYY-MM-DD',
    min: laydate.now(),
    max: laydate.now(),
    istime: true,
    istoday: true,
    fixed: false,
    choose: function (datas) {
        //        startTime.max = datas; //After selecting the end date，重置开始日的maximum date
        endV = datas;
    }
};

laydate(startTime);
laydate(endTime);

//time picker
var startVs = '';
var endVs = '';
laydate.skin('danlan');
var startTimes = {
    elem: '#startTimes',
    format: 'YYYY-MM-DD',
    min: '1997-01-01', //Set the minimum date to the current date
    max: '2099-06-16', //maximum date
    istime: true,
    istoday: true,
    fixed: false,
    choose: function (datas) {
        startVs = datas;
        endTimes.min = datas; //After selecting the start date，Minimum date to reset end day
        setQgData($('#barTypes').parent().parent(), 1);
    }
};
var endTimes = {
    elem: '#endTimes',
    format: 'YYYY-MM-DD',
    min: laydate.now(),
    max: laydate.now(),
    istime: true,
    istoday: true,
    fixed: false,
    choose: function (datas) {
        //        startTime.max = datas; //After selecting the end date，重置开始日的maximum date
        endVs = datas;
        setQgData($('#barTypes').parent().parent(), 1);
    }
};

laydate(startTimes);
laydate(endTimes);

//点击time picker的时候更改样式
$('#endTime').on('click', function () {
    dateCss();
})

$('#end').on('click', function () {
    dateCss();
})


//更改日期插pieces的样式
function dateCss() {
    var arr = $('#laydate_box').attr('style').split(';');
    var cssStr =
        'position:absolute;right:0;';
    for (var i = 0; i < arr.length; i++) {
        if (arr[i].indexOf('top') != -1) {
            cssStr += arr[i];
        }
    }

    $('#laydate_box').attr('style', cssStr);
}



//chart4Data模拟data
var chart4Data = [{
    'name': "sky津市",
    'value': 178546
    }, {
    'name': "Hunan Province",
    'value': 125687
    }, {
    'name': "Fujian Province",
    'value': 78452
    }, {
    'name': "Beijing市",
    'value': 57841
    }, {
    'name': "Jiangsu Province",
    'value': 45879
    }, {
    'name': "Hainan",
    'value': 28584
    }, {
    'name': "Sichuan Province",
    'value': 14852
    }, {
    'name': "Zhejiang Province",
    'value': 12589
    }, {
    'name': "Chongqing市",
    'value': 5261
    }, {
    'name': "Hong Kong Special Administrative Region",
    'value': 2563
    }, {
    'name': "Inner Mongolia",
    'value': 856
    }]
chart4(chart4Data, 1, '');

function chart4(data, type, chartType) {
    var str = '<li><span></span><p>City</p><p>派pieces</p></li>';
    for (var i = 0; i < 10; i++) {
        str += '<li><span>' + (i + 1) + '</span><p>' + data[i].name + '</p><p>' + data[i].value + '</p></li>';
    }

    var s_data = [];
    var myChart = echarts.init(document.getElementById('chart4'));
    var myCharts = echarts.init(document.getElementById('chart4s'));
    window.addEventListener('resize', function () {
        myChart.resize();
        myCharts.resize();
    });


    function formtGCData(geoData, data, srcNam, dest) {
        var tGeoDt = [];
        if (dest) {
            for (var i = 0, len = data.length; i < len; i++) {
                if (srcNam != data[i].name) {
                    tGeoDt.push({
                        coords: [geoData[srcNam], geoData[data[i].name]],
                    });
                }
            }
        } else {
            for (var i = 0, len = data.length; i < len; i++) {
                if (srcNam != data[i].name) {
                    tGeoDt.push({
                        coords: [geoData[data[i].name], geoData[srcNam]],
                    });
                }
            }
        }
        return tGeoDt;
    }

    function formtVData(geoData, data, srcNam) {
        var tGeoDt = [];
        for (var i = 0, len = data.length; i < len; i++) {
            var tNam = data[i].name
            if (srcNam != tNam) {
                tGeoDt.push({
                    name: tNam,
                    symbolSize: 2,
                    itemStyle: {
                        normal: {
                            color: '#ffeb40',
                        }
                    },
                    value: geoData[tNam]
                });
            }

        }
        tGeoDt.push({
            name: srcNam,
            value: geoData[srcNam],
            symbolSize: 5,
            itemStyle: {
                normal: {
                    color: '#2ef358',
                }
            }

        });
        return tGeoDt;
    }

    var planePath = 'pin';
    if (type == 2) {
        s_data.push({
            type: 'lines',
            zlevel: 2,
            mapType: 'china',
            symbol: 'none',
            effect: {
                show: true,
                period: 1.5,
                trailLength: 0.1,
                //                color: '#ffeb40',
                color: '#2ef358',
                symbol: planePath,
                symbolSize: 6,
                trailLength: 0.5

            },
            lineStyle: {
                normal: {
                    color: '#2ef358',
                    width: 1,
                    opacity: 0.4,
                    curveness: 0.2
                }
            },
            data: formtGCData(geoCoordMap, data, 'Zhuhai', true)
        })

    } else if (type == 1) {
        s_data.push({
            type: 'lines',
            zlevel: 2,
            effect: {
                show: true,
                period: 1.5,
                trailLength: 0.1,
                //                color: '#2ef358',
                color: '#ffeb40',
                symbol: planePath,
                symbolSize: 6,
                trailLength: 0.5
            },
            lineStyle: {
                normal: {
                    color: '#ffeb40',
                    width: 1,
                    opacity: 0.4,
                    curveness: 0.2
                }
            },
            data: formtGCData(geoCoordMap, data, 'Zhuhai', false)
        }, {

            type: 'effectScatter',
            coordinateSystem: 'geo',
            zlevel: 2,
            rippleEffect: {
                period: 4,
                scale: 2.5,
                brushType: 'stroke'
            },
            symbol: 'none',
            symbolSize: 4,
            itemStyle: {
                normal: {
                    color: '#fff'
                }
            },

            data: formtVData(geoCoordMap, data, 'Zhuhai')
        })
    }

    var option = {
        tooltip: {
            trigger: 'item',
        },
        geo: {
            map: 'china',
            label: {
                show: true,
                position: 'insideLeft',
                color: 'white',
                fontSize: '10',
                emphasis: {
                    show: true
                }
            },
            roam: true,
            silent: true,
            itemStyle: {
                normal: {
                    areaColor: 'transparent',
                    borderColor: '#0e94eb',
                    shadowBlur: 10,
                    shadowColor: '#0e94ea'
                }
            },
            left: 10,
            right: 10
        },
        series: s_data
    };
    if (chartType === '') {
        $('.ranking-box').html(str);
        myChart.setOption(option);
        myCharts.setOption(option);
    } else if (chartType === 0) {
        $('.center-bottom .ranking-box').html(str);
        myChart.setOption(option);
    } else if (chartType === 1) {
        $('.pop-data .ranking-box').html(str);
        myCharts.setOption(option);
    }
}

$('.close-pop').on('click', function () {
    $(this).parent().parent().hide().find('.cont-div').attr('style', 'visibility: hidden');
})

$('#setBtn').on('click', function () {
    $('.container').attr('style', 'visibility: visible').find('.pop-up').eq(4).attr('style', 'visibility: visible').siblings().attr('style', 'visibility: hidden');

})

var workDate;
var time = {
    elem: '#times',
    format: 'YYYY-MM-DD',
    min: laydate.now(),
    max: laydate.now() + 30,
    istime: true,
    istoday: true,
    fixed: false,
    choose: function (datas) {
        //        startTime.max = datas; //After selecting the end date，重置开始日的maximum date
        workDate = datas;
    }
};

laydate(time);

$('#addT').on('click', function () {
    $('#mineusT').show();
    if ($(this).siblings('input').length < 6) {
        if ($(this).siblings('input').length == 5) {
            $(this).hide();
        }
        $(this).before('<input type="text" value="">');
    }

})

$('#mineusT').on('click', function () {
    if ($(this).siblings('input').length > 1) {
        if ($(this).siblings('input').length == 6) {
            $('#addT').show();
        } else if ($(this).siblings('input').length == 2) {
            $(this).hide()
        }
        $(this).siblings('input:last').remove();
    }
})

$('#addL').on('click', function () {
    $('#mineusL').show();
    if ($(this).siblings('input').length < 3) {
        if ($(this).siblings('input').length == 2) {
            $(this).hide();
        }
        $(this).before('<input type="text" value="">');
    }

})

$('#mineusL').on('click', function () {
    if ($(this).siblings('input').length > 1) {
        if ($(this).siblings('input').length == 3) {
            $('#addL').show();
        } else if ($(this).siblings('input').length == 2) {
            $(this).hide()
        }
        $(this).siblings('input:last').remove();
    }
})