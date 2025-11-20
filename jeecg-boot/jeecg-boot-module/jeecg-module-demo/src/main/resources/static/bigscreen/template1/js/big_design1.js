//The current selection of the plan completion table
var indexnum = 0;
var color=['#F35331','#2499F8','#3DF098','#33B734'];
var fontColor='#FFF';

//Define progress bar components and properties
var y_gauge1 =null;
var y_gauge2 =null;
var y_gauge3 =null;
var y_gauge4 =null;
var m_gauge1 =null;
var m_gauge2 =null;
var m_gauge3 =null;
var m_gauge4 =null;
var d_gauge1 =null;
var d_gauge2 =null;
var d_gauge3 =null;
var d_gauge4 =null;
var option_Progress =null;

//Define dashboard components and properties
var gauge1 =null;
var gauge2 =null;
var gauge3 =null;
var gauge4 =null;
var gauge5 =null;
var option_gauge =null;

//Production quality stacked chart components and properties
var quality_chart = null;
var quality_option=null;

//Production planning line chart components and properties
var plan_chart = null;
var plan_option=null;

//Donut chart style definition
var dataStyle = {
	normal: {
		label: {show:false},
		labelLine: {show:false}
	}
};
var placeHolderStyle = {
	normal : {
		color: 'rgba(0,0,0,0.1)',
		label: {show:false},
		labelLine: {show:false}
	},
	emphasis : {
		color: 'rgba(0,0,0,0)'
	}
};

//Maximum order number
var lastOrderNumber=1;

$(document).ready(function ()
{	
	//Circular progress bar setting object	
	option_Progress={
		title : {
			text: 'Current progress',
			subtext: '50%',
			x: 'center',
			y: 90,
			itemGap: 10,
			textStyle : {
				color : '#B7E1FF',
				fontWeight: 'normal',
				fontFamily : 'Microsoft Yahei',
				fontSize : 24
			},
			subtextStyle:{
				color: '#B7E1FF',
				fontWeight: 'bolder',
				fontSize:24,
				fontFamily : 'Microsoft Yahei'
			}
		},
		series : [{
			type : 'pie',
			center : ['50%', '50%'],
			radius : [75,90],
			x: '0%',
			tooltip:{show:false},		
			data : [{
				name:'Achievement rate', 
				value:79,
				itemStyle:{color :'rgba(0,153,255,0.8)'},
				hoverAnimation: false, 
				label : {
					show : false,
					position : 'center',
					textStyle: {						
						fontFamily:'Microsoft Yahei',
						fontWeight: 'bolder',
						color:'#B7E1FF',
						fontSize:24
					}
				},
				labelLine : {
					show : false
				}
			},
			{
				name:'79%',
				value:21,
				itemStyle:{color: 'rgba(0,153,255,0.1)'},
				hoverAnimation: false, 
				label : {
					show : false,
					position : 'center',
					padding:20,		
					textStyle: {
						fontFamily:'Microsoft Yahei',
						fontSize: 24,
						color:'#B7E1FF'
					}
				},
				labelLine : {
					show : false
				}
			}]
		},
		{
			type : 'pie',
			center : ['50%', '50%'],
			radius : [95,100],
			x: '0%',
			hoverAnimation: false, 
			data : [{
				value:100,
				itemStyle:{color :'rgba(0,153,255,0.3)'},
				label : {show : false},
				labelLine : {show : false}
			}]	
		},
		{
			type : 'pie',
			center : ['50%', '50%'],
			radius : [69,70],
			x: '0%',
			hoverAnimation: false, 
			data : [{
				value:100,
				itemStyle:{color :'rgba(0,153,255,0.3)'},
				label : {show : false},
				labelLine : {show : false}
			}]	
		}]
	};	
	
	//year dashboard
	y_gauge1 = echarts.init(document.getElementById('y_gauge1'));
	y_gauge2 = echarts.init(document.getElementById('y_gauge2'));
	y_gauge3 = echarts.init(document.getElementById('y_gauge3'));
	y_gauge4 = echarts.init(document.getElementById('y_gauge4'));	
	
	//Monthly dashboard
	m_gauge1 = echarts.init(document.getElementById('m_gauge1'));
	m_gauge2 = echarts.init(document.getElementById('m_gauge2'));
	m_gauge3 = echarts.init(document.getElementById('m_gauge3'));
	m_gauge4 = echarts.init(document.getElementById('m_gauge4'));			
	
	//daily dashboard
	d_gauge1 = echarts.init(document.getElementById('d_gauge1'));
	d_gauge2 = echarts.init(document.getElementById('d_gauge2'));
	d_gauge3 = echarts.init(document.getElementById('d_gauge3'));
	d_gauge4 = echarts.init(document.getElementById('d_gauge4'));			
		
	//Monitoring dashboard
	option_gauge = {		
		title: {
			text: '', //Title text content
		},
		toolbox: { //Visual toolbox
			show: false,                
		},
		tooltip: { //Pop-up component
			formatter: "{a} <br/>{b} : {c}%"
		},			
		series: [{          
			type: 'gauge',
			axisLine: {// Coordinate axis
				lineStyle: { // propertylineStyleControl line style
					color: [
						[0.2, color[0]],
						[0.8, color[1]],
						[1, color[2]]
					],
					width: 18
				 }
			},				 
			splitLine: { // divider
					show:true,
					length: 18,
					lineStyle: {                            
						color: '#28292D',
						width: 1
					}
				},
			axisTick : { //tick mark style（and short line style）
				show:false,
				lineStyle: {                    
						color: 'auto',
						width: 1
					},
				length : 20
			},
			axisLabel : {
				color:'#FFF',
				fontSize:14,
				fontFamily:'Verdana, Geneva, sans-serif'
			},
			title: {					
					textStyle: { // 其余property默认使用全局文本样式，See detailsTEXTSTYLE
						fontWeight: 'bolder',
						fontSize: 20,                          
						color: '#FFF'
					},
					offsetCenter: [0, '30%']
				},
			pointer: {
					width: 5,                     
					color: '#F00',
					shadowColor: '#FF0',
					shadowBlur: 10
				},
			detail: {
				show:false,
				formatter:'{value}%',
				textStyle: 
				{
					fontFamily:'Arial',
					color: '#000',
					fontSize:'32px'						
				},
				offsetCenter: [0, '90%']
			},
			data: [{value: 45, name: 'water'}]
		}]
     };
		
	gauge1 = echarts.init(document.getElementById('gauge1'));
	gauge2 = echarts.init(document.getElementById('gauge2'));
	gauge3 = echarts.init(document.getElementById('gauge3'));
	gauge4 = echarts.init(document.getElementById('gauge4'));	
	gauge5 = echarts.init(document.getElementById('gauge5'));
	option_gauge.series[0].axisLine.lineStyle.color=[[0.2, color[0]],[0.8, color[1]],[1, color[2]]];
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="water";
	$('#vg1').html(option_gauge.series[0].data[0].value);
	gauge1.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="electricity";
	$('#vg2').html(option_gauge.series[0].data[0].value);
	gauge2.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="natural gas";
	$('#vg3').html(option_gauge.series[0].data[0].value);
	gauge3.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="compressed air";
	$('#vg4').html(option_gauge.series[0].data[0].value);
	gauge4.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="steam";
	$('#vg5').html(option_gauge.series[0].data[0].value);
	gauge5.setOption(option_gauge);
	
	//Production quality stack chart
	quality_chart = echarts.init(document.getElementById('quality'));
	quality_option={
		title: {			
			show:false,
			text: 'AUDIT',
			left: 'center',
			textStyle: {
				color: '#F00',
				fontSize:32
			}
		},
		xAxis: {
			data: ['1moon10day','2moon10day','3moon10day','4moon10day','5moon10day','6moon10day'],
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24
				}
			},
			axisLine:{
				lineStyle:{
					color:'#09F'	
				}
			},
			axisTick:{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		yAxis: {
			inverse: false,
			splitArea: {show: false},
			axisLine:  {show: false},
			axisTick:  {show: false},
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24,
					fontFamily:'Arial',
				}
			},
			splitLine :{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		grid: {
			left: 100
		},
		tooltip: {
			trigger: 'item',
			textStyle: {
				color: '#B7E1FF',
				fontSize:24
			}
		},
		legend:{
			show:false,
			top: 'bottom',
			textStyle: {
				color: '#F00',
				fontSize:24,
				fontFamily:'Microsoft Yahei'
			},
			data:['AUDITFraction1','AUDITFraction']
		},
		series: [
			{
				name: 'AUDITFraction1',
				type: 'bar',
				stack: 'one',
				itemStyle: 
				{
					normal: {color: color[1]}
				},
				barWidth : 60,
				data:[2200,2900,3680,2200,2900,3680]
			},
			{
				name: 'AUDITFraction',
				type: 'bar',
				stack: 'one',
				itemStyle: {
					normal: {
						color: '#F90',
						label: {
							 show: true,
							 position: 'insideTop',
							 textStyle: {
								 color: '#000',
								 fontSize:24
							 }
						 }
					}
				},
				barWidth : 50,
				data: [1800,1100,320,1800,1100,320]
			}
		]
	};
	quality_chart.setOption(quality_option);
	
	//Production plan line chart
	var plan_data1=[];
	var plan_data2=[];
	var plan_xAxis=[];
	for (var i = 1; i <= 7; i++) {
		plan_xAxis.push("3moon"+i+"day");
		plan_data1.push(Math.round(Math.random() * 100));
		plan_data2.push(Math.round(Math.random() * 100));
	}
	plan_chart = echarts.init(document.getElementById('plan'));
	plan_option={		
		xAxis: {
			data:plan_xAxis,
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24
				}
			},
			axisLine:{
				lineStyle:{
					color:'#09F'	
				}
			},
			axisTick:{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		yAxis: {			
			inverse: false,
			splitArea: {show: false},
			axisLine:  {show: false},
			axisTick:  {show: false},
			axisLabel: {
				textStyle: {
					color: '#B7E1FF',
					fontSize:24,
					fontFamily:'Arial',
				}
			},
			splitLine :{
				lineStyle:{
					color:'#09F'	
				}
			}
		},
		tooltip: {
			trigger: 'axis',
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		grid: {
			left: 100
		},
		legend:{
			show:false,
			top: 'bottom',
			textStyle: {
				color: '#F00',
				fontSize:24
			},			
			data:['Number of planned completions','actual number of completions']
		},
		series: [
			{
				name: 'Number of planned completions',
				type: 'bar',
				itemStyle: 
				{
					normal: {color: color[1]},
					emphasis: {color: color[2]}
				},
				barWidth : 40,
				data:plan_data1
			},
			{
				name: 'actual number of completions',
				type: 'line',
				itemStyle: {
					normal: {
						color: '#F90',
						label: {
							 show: true,
							 position: 'top',
							 textStyle: {
								 color: '#CCC',
								 fontSize:24
							 }
						},
						lineStyle:{
							color:'#F90',
							width:4
						}				 
					},
					emphasis: {
						color: '#FF0'
					}	
				},			
				symbolSize: 24,
				data: plan_data2
			}
		]
	};
	plan_chart.setOption(plan_option);
	
	//Display in turntips
	function clock(){
	  showToolTip_highlight(plan_chart);	  
	}
	setInterval(clock, 5000);
	
	//map start
	var map_chart = echarts.init(document.getElementById('map'));
	
	var CCData = [
		[{name:'Changchun'}, {name:'Shanghai',value:95}],
		[{name:'Changchun'}, {name:'Guangzhou',value:90}],
		[{name:'Changchun'}, {name:'Dalian',value:80}],
		[{name:'Changchun'}, {name:'Nanning',value:70}],
		[{name:'Changchun'}, {name:'Nanchang',value:60}],
		[{name:'Changchun'}, {name:'Lhasa',value:50}],
		[{name:'Changchun'}, {name:'Changchun',value:40}],
		[{name:'Changchun'}, {name:'Baotou',value:30}],
		[{name:'Changchun'}, {name:'Chongqing',value:20}],
		[{name:'Changchun'}, {name:'Beijing',value:10}]
	];
	
	var series = [];
	[['Changchun', CCData]].forEach(function (item, i) {
		series.push({
			name: 'FAW Auto Sales',
			type: 'lines',
			zlevel: 1,
			effect: {
				show: true,
				period: 6,
				trailLength: 0.7,
				color: '#FF0',
				symbolSize: 3
			},
			lineStyle: {
				normal: {
					color: '#000',
					width: 0,
					curveness: 0.2
				}
			},
			data: convertData(item[1])
		},
		{
			name: 'FAW Auto Sales',
			type: 'lines',
			zlevel: 2,
			symbol: ['none', 'arrow'],
			symbolSize: 10,
			lineStyle: {
				normal: {
					color: '#FF0',
					width: 1,
					opacity: 0.6,
					curveness: 0.2
				}
			},
			data: convertData(item[1])
		},
		{
			name: 'FAW Auto Sales',
			type: 'effectScatter',
			coordinateSystem: 'geo',
			zlevel: 2,
			rippleEffect: {
				brushType: 'stroke'
			},
			label: {
				normal: {
					show: true,
					position: 'right',
					formatter: '{b}'
				}
			},
			symbolSize: function (val) {
				return 15;
			},
			itemStyle: {
				normal: {
					color: '#FFF',
					label: {
						 show: true,
						 position: 'top',
						 textStyle: {
							 color: '#FFF',
							 fontSize:24
						 }
					}
				}
			},
			data: item[1].map(function (dataItem) {
				return {
					name: dataItem[1].name,
					value: geoCoordMap[dataItem[1].name].concat([dataItem[1].value])
				};
			})
		});
	});
	
	map_option = {
		backgroundColor: '',		
		title : {
			show:false,
			text: 'FAW Auto Sales地域分布示意图',
			subtext: 'As of2018Year05moon04day',
			left: 'center',
			top:10,
			textStyle : {
				color: '#09F',
				fontSize:32
			},
			subtextStyle:{
				color: '#09F',
				fontSize:24
			}
		},
		tooltip : {
			trigger: 'item'
		},
		legend: {
			show:false,
			orient: 'vertical',
			top: 'bottom',
			left: 'right',
			data:['FAW Auto Sales'],
			textStyle: {
				color: '#000'
			},
			selectedMode: 'single'
		},
		geo: {
			map: 'china',
			label: {
				emphasis: {
					show: false
				}
			},
			roam: true,
			itemStyle: {
				normal: {
					areaColor: '#09F',
					borderColor: '#09F',
					opacity:0.5
				},
				emphasis: {
					areaColor: '#09F',
					borderColor: '#09F',
					opacity:0.8
				}
			}
		},
		series: series
	};
	
	map_chart.setOption(map_option, true);
	
	resresh();
	
	//Start scheduled refresh
	setInterval(resresh, 5*1000);
});

var convertData = function (data) {
    var res = [];
    for (var i = 0; i < data.length; i++) {
        var dataItem = data[i];
        var fromCoord = geoCoordMap[dataItem[0].name];
        var toCoord = geoCoordMap[dataItem[1].name];
        if (fromCoord && toCoord) {
            res.push({
                fromName: dataItem[0].name,
                toName: dataItem[1].name,
                coords: [fromCoord, toCoord]
            });
        }
    }
    return res;
};

function showToolTip_highlight(mychart)
{  
  var echartObj = mychart;
  	  
  // Highlight current graphic
  var highlight =setInterval(function() 
  {
	  echartObj.dispatchAction({
		  type: 'highlight',
		  seriesIndex: 0,
		  dataIndex: indexnum
	  });
	  
	  echartObj.dispatchAction({
		  type: 'showTip',
		  seriesIndex: 0,
		  dataIndex: indexnum
	  });
	  clearInterval(highlight);
	  indexnum = indexnum + 1;
  	  if(indexnum>=7) indexnum=0;	  	  
  },1000);
}

//Refresh data regularly
function resresh()
{
	var myDate = new Date();
	
	// $('#refresh').html("<img src=\"images/wait.gif\" align=\"absmiddle\"><span>Data is being refreshed...</span>");
	
	//Yearmoonday刷新
	$('#currentYear').html(myDate.getFullYear()+"Year");
	$('#currentMonth').html(insertZero(myDate.getMonth()+1)+"moon");
	$('#currentDay').html(insertZero(myDate.getDate())+"day");
	$('#currentDate').html(myDate.getFullYear()+"/"+insertZero(myDate.getMonth()+1)+"/"+insertZero(myDate.getDate()));	
	
	option_gauge.series[0].axisLabel.show=true;
	option_gauge.series[0].axisLine.lineStyle.color=[[0.2, color[0]],[0.8, color[1]],[1, color[2]]]
	
	var maxg=Math.round(Math.random()*500)+400;
	var n1=Math.round(Math.random()*(maxg-100))+100;
	var n2=Math.round(Math.random()*(n1-50))+50;	
	var n3=(n2/maxg*100).toFixed(2);	
	
	//Year进度条
	option_Progress.title.text ="Planned production";
	option_Progress.series[0].data[0].value = maxg;
	option_Progress.title.subtext =maxg+"tower";
	option_Progress.series[0].data[1].value =0;
	y_gauge1.setOption(option_Progress);
	
	option_Progress.title.text ="Order received";
	option_Progress.series[0].data[0].value = n1;
	option_Progress.title.subtext =n1+"tower";
	option_Progress.series[0].data[1].value =(maxg-n1);
	y_gauge2.setOption(option_Progress);
	
	option_Progress.title.text ="Already completed";
	option_Progress.series[0].data[0].value = n2;
	option_Progress.title.subtext =n2+"tower";
	option_Progress.series[0].data[1].value =(maxg-n2);
	y_gauge3.setOption(option_Progress);
	
	option_Progress.title.text ="plan completion rate";
	option_Progress.series[0].data[0].value = n3;
	option_Progress.title.subtext =n3+"%";
	option_Progress.series[0].data[1].value =(100-n3);
	y_gauge4.setOption(option_Progress);
	
	//moon进度条
	maxg=Math.round(Math.random()*maxg)+1;
	n1=Math.round(Math.random()*maxg)+1;
	n2=Math.round(Math.random()*n1);
	n3=(n2/maxg*100).toFixed(2);
	
	option_Progress.title.text ="Planned production";
	option_Progress.series[0].data[0].value = maxg;
	option_Progress.title.subtext =maxg+"tower";
	option_Progress.series[0].data[1].value =0;
	m_gauge1.setOption(option_Progress);
	
	option_Progress.title.text ="Order received";
	option_Progress.series[0].data[0].value = n1;
	option_Progress.title.subtext =n1+"tower";
	option_Progress.series[0].data[1].value =(maxg-n1);
	m_gauge2.setOption(option_Progress);
	
	option_Progress.title.text ="Already completed";
	option_Progress.series[0].data[0].value = n2;
	option_Progress.title.subtext =n2+"tower";
	option_Progress.series[0].data[1].value =(maxg-n2);
	m_gauge3.setOption(option_Progress);
	
	option_Progress.title.text ="plan completion rate";
	option_Progress.series[0].data[0].value = n3;
	option_Progress.title.subtext =n3+"%";
	option_Progress.series[0].data[1].value =(100-n3);
	m_gauge4.setOption(option_Progress);
	
	//day进度条
	maxg=Math.round(Math.random()*maxg)+1;
	n1=Math.round(Math.random()*maxg)+1;
	n2=Math.round(Math.random()*n1);
	n3=(n2/maxg*100).toFixed(2);
	
	option_Progress.title.text ="Planned production";
	option_Progress.series[0].data[0].value = maxg;
	option_Progress.title.subtext =maxg+"tower";
	option_Progress.series[0].data[1].value =0;
	d_gauge1.setOption(option_Progress);
	
	option_Progress.title.text ="Order received";
	option_Progress.series[0].data[0].value = n1;
	option_Progress.title.subtext =n1+"tower";
	option_Progress.series[0].data[1].value =(maxg-n1);
	d_gauge2.setOption(option_Progress);
	
	option_Progress.title.text ="Already completed";
	option_Progress.series[0].data[0].value = n2;
	option_Progress.title.subtext =n2+"tower";
	option_Progress.series[0].data[1].value =(maxg-n2);
	d_gauge3.setOption(option_Progress);
	
	option_Progress.title.text ="plan completion rate";
	option_Progress.series[0].data[0].value = n3;
	option_Progress.title.subtext =n3+"%";
	option_Progress.series[0].data[1].value =(100-n3);
	d_gauge4.setOption(option_Progress);
	
	//Dashboard refresh
	option_gauge.series[0].axisLine.lineStyle.color=[[0.2, color[0]],[0.8, color[1]],[1, color[0]]];
	
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="water";
	$('#vg1').html(option_gauge.series[0].data[0].value);
	gauge1.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="electricity";
	$('#vg2').html(option_gauge.series[0].data[0].value);
	gauge2.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="natural gas";
	$('#vg3').html(option_gauge.series[0].data[0].value);
	gauge3.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="compressed air";
	$('#vg4').html(option_gauge.series[0].data[0].value);
	gauge4.setOption(option_gauge);
	option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
	option_gauge.series[0].data[0].name ="steam";
	$('#vg5').html(option_gauge.series[0].data[0].value);
	gauge5.setOption(option_gauge);	
			
	//Show last updated time
	$('#refresh').html("<span id=\"refreshTime\">Last refresh time："+myDate.toLocaleDateString()+" "+myDate.toLocaleTimeString()+"</span>");
}

//Generate order number
function getOrderNumber(n)
{
	var no="000000"+n.toString();
	return no.substring(no.length-6);
}

//Make up for it in front0
function insertZero(n)
{
	var no="000000"+n.toString();
	return no.substring(no.length-2);
}