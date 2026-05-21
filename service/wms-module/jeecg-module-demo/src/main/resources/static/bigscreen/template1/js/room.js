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
var option_Progress =null;

//Order status spiral chart
var orderStatus=null;
var orderStatus_option =null;

//Define dashboard components and properties
var gauge1 =null;
var gauge2 =null;
var gauge3 =null;
var gauge4 =null;
var gauge5 =null;
var option_gauge =null;

//Product Pie Chart Components and Properties
var productPie=null;
var productPie_option=null;

//Business Progress Diagram Components and Properties
var businessProgress=null;
var businessProgress_placeHoledStyle = null;
var businessProgress_dataStyle =null;
var businessProgress_option=null;

//Production quality stacked chart components and properties
var quality_chart = null;
var quality_option=null;

//Word cloud components and properties
var wordCloud= null;
var wordCloud_option=null;

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
	
	//Order fulfillment spiral chart
	var yearPlanData=[];
	var yearOrderData=[];
	var differenceData=[];
	var visibityData=[];
	var xAxisData=[];
	
	for(var i=0;i<12;i++)
	{
		yearPlanData.push(Math.round(Math.random()*900)+100);
		yearOrderData.push(Math.round(Math.random()*yearPlanData[i]));
		differenceData.push(yearPlanData[i]-yearOrderData[i]);
		visibityData.push(yearOrderData[i]);
		xAxisData.push((i+1).toString()+"moon");
	}
	orderStatus= echarts.init(document.getElementById('orderStatus'));
	orderStatus_option={
		title :{show:false},
		tooltip : {
			trigger: 'axis',
			formatter: function (params){
				return params[0].name + '<br/>'
					   + params[0].seriesName + ' : ' + params[0].value + '<br/>'
					   + params[1].seriesName + ' : ' + params[1].value + '<br/>'
					   +'completion rate：'
					   + (params[0].value > 0 ? (params[1].value/params[0].value*100).toFixed(2)+'%' : '-') 
					   + '<br/>'
			},
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		toolbox: {show:false},
		legend:{
			top: 'top',
			textStyle: {
				color: '#B7E2FF',
				fontSize:24,
				fontFamily:'Microsoft Yahei'
			},
			data:['Planned production','Order received']
		},
		xAxis: {
			data: xAxisData,
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
		series : [{
				name:'Planned production',
				type:'line',
				smooth :true,
				symbol: 'circle',
				symbolSize: 10,
				showAllSymbol : true,
				color:color[1],
				data:yearPlanData
			},
			{
				name:'Order received',
				type:'line',
				smooth :true,
				symbol: 'circle',
				symbolSize: 10,
				showAllSymbol : true,
				color:'#F90',
				itemStyle:{					
					normal:{
					  lineStyle: {
						width:2
					  }
					}
				},
				data:yearOrderData
			},
			{
				name:'Invisible',
				type:'bar',
				stack: '1',
				barWidth: 1,
				itemStyle:{
					normal:{
						color:'rgba(0,0,0,0)'
					},
					emphasis:{
						color:'rgba(0,0,0,0)'
					}
				},
				data:visibityData
			},
			{
				name:'change',
				type:'bar',
				stack: '1',
				barWidth: 1,
				color:'#B7E1FF',
				data:differenceData
			}
		]
	}
	orderStatus.setOption(orderStatus_option);
	
	//Product Sales Donut Chart
	var productLegend=[['car1','car2','car3','car4','car5','car6','car7','car8','car9'],['SUV1','SUV2','SUV3','SUV4'],['truck1','truck2','truck3','truck4','truck5','truck6']];
	var productClassLegend=['car','SUV','truck'];
	var productClassColor=['rgba(255,153,0,','rgba(153,204,102,','rgba(0,102,255,'];
	var productClassData=[];
	var productData=[];
	var productColor=[];
	for(var i=0;i<productClassLegend.length;i++)
	{	
		var total=0;
		for(var j=0;j<productLegend[i].length;j++)
		{
			var n=Math.round(Math.random()*100)+1;
			productData.push({name:productLegend[i][j],value:n});
			total+=n;
		}
		for(var j=0;j<productLegend[i].length;j++)
		{		
			productColor.push(productClassColor[i]+(1.0-productData[j].value/total).toFixed(2)+")");
		}
		productClassData.push({name:productClassLegend[i],value:total});
	}
	
	productPie=echarts.init(document.getElementById('productPie'));
	productPie_option={
		title : {
			text: 'sales',			
			x: 'center',
			y: 'center',
			itemGap: 10,
			textStyle : {
				color : '#09F',
				fontWeight: 'normal',
				fontFamily : 'Microsoft Yahei',
				fontSize : 32
			}
		},
		calculable : false,
		tooltip : {
			trigger: 'item',
			textStyle: {
				color:'#FFF',
				fontSize:24
			},
			formatter: "{a} <br/>{b} : {c} ({d}%)"
		},
		series : [
			{
				name:'category',
				type:'pie',
				selectedMode: 'single',
				radius : ['20%','40%'],				
				width: '40%',
				funnelAlign: 'right',
				itemStyle : {
					normal : {
						color:function(d)
						{
							return productClassColor[d.dataIndex]+'1)';
						},
						borderColor:'#032749',
						label : {
							position : 'inner',							
							fontSize:28,
						},
						labelLine : {
							show : false
						}
					}
				},
				data:productClassData
			},
			{
				name:'car model',
				type:'pie',
				radius : ['40%','70%'],				
				width: '35%',
				funnelAlign: 'left',
				itemStyle : {					
					normal : {
						color:function(d)
						{
							return productColor[d.dataIndex];
						},
						borderColor:'#032749',
						label : {
							color:'#B7E1FF',						
							fontSize:24
						}
					}
				},			
				data:productData
			}
		]
	};
	productPie.setOption(productPie_option);
	
	//business progress chart
	businessProgress=echarts.init(document.getElementById('businessProgress'));	
	businessProgress_placeHoledStyle = {
		normal:{
			barBorderColor:'rgba(0,0,0,0)',
			color:'rgba(0,0,0,0)'
		},
		emphasis:{
			barBorderColor:'rgba(0,0,0,0)',
			color:'rgba(0,0,0,0)'
		}
	};
	businessProgress_dataStyle = { 
		normal: {
			barBorderColor:'rgba(0,102,255,1)',
			color:function(d){
				return 'rgba(0,102,255,0.3)';
			},
			label : {
				show: true,
				position: 'insideLeft',
				formatter: '{c}%',
				textStyle: {						
					fontFamily:'Arial',
					fontWeight: 'bolder',
					color:'#B7E1FF',
					fontSize:24
				}
			}
		}
	};
	
	businessProgress_option = {
		title: {show:false},
		tooltip : {
			trigger: 'axis',
			axisPointer : {
				type : 'shadow'
			},
			textStyle: {
				color:'#FFF',
				fontSize:24
			},
			formatter : '{b}<br/>{a0}:{c0}%<br/>{a2}:{c2}%<br/>{a4}:{c4}%<br/>{a6}:{c6}%'
		},
		legend: {			
			itemGap : 60,
			top:'top',
			textStyle: {						
				fontFamily:'Microsoft Yahei',
				fontWeight: 'bolder',
				color:'#B7E1FF',
				fontSize:24
			},
			data:['Project bidding', 'Bid progress','Project in progress', 'Project delivery']
		},
		toolbox: {show : false},
		grid: {
			left: 150
		},
		xAxis : [
			{
				type : 'value',
				position: 'top',
				axisLine:{
					lineStyle:{color:'#09F'}
				},
				splitLine :{
					lineStyle:{color:'#09F'	}
				},
				axisLabel: {show: false},
			}
		],
		yAxis : [
			{
				type : 'category',
				data : ['major business1', 'major business2', 'major business3', 'major business4'],
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
				splitLine :{
					lineStyle:{color:'#09F'	}
				}
			}
		],
		series : [
			{
				name:'Project bidding',
				type:'bar',
				stack: 'schedule',
				itemStyle : businessProgress_dataStyle,
				data:[100, 100, 100, 70]
			},
			{
				name:'Project bidding',
				type:'bar',
				stack: 'schedule',
				itemStyle: businessProgress_placeHoledStyle,
				data:[0, 0, 0, 30]
			},
			{
				name:'Bid progress',
				type:'bar',
				stack: 'schedule',
				itemStyle : businessProgress_dataStyle,
				data:[100, 100, 42, 0]
			},
			{
				name:'Bid progress',
				type:'bar',
				stack: 'schedule',
				itemStyle: businessProgress_placeHoledStyle,
				data:[0, 0, 58, 100]
			},
			{
				name:'Project in progress',
				type:'bar',
				stack: 'schedule',
				itemStyle : businessProgress_dataStyle,
				data:[100, 100, 0, 0]
			},
			{
				name:'Project in progress',
				type:'bar',
				stack: 'schedule',
				itemStyle: businessProgress_placeHoledStyle,
				data:[0, 0, 100, 100]
			},
			{
				name:'Project delivery',
				type:'bar',
				stack: 'schedule',
				itemStyle : businessProgress_dataStyle,
				data:[71, 50, 0, 0]
			},
			{
				name:'Project delivery',
				type:'bar',
				stack: 'schedule',
				itemStyle: businessProgress_placeHoledStyle,
				data:[29, 50, 100, 100]
			}
		]
	};
	businessProgress.setOption(businessProgress_option);
	
	
	//Monitoring dashboard
	/*option_gauge = {		
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
					color:[[0.2, color[0]],[0.8, color[1]],[1, color[0]]],
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
	gauge5.setOption(option_gauge);*/
	
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
			data: ['1moon','2moon','3moon','4moon','5moon','6moon','7moon','8moon','9moon','10moon','11moon','12moon'],
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
				data:[2200,2900,3680,2200,2900,3680,2200,2900,3680,2200,2900,3680]
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
				data: [1800,1100,320,1800,1100,320,1800,1100,320,1800,1100,320]
			}
		]
	};
	quality_chart.setOption(quality_option);
	
	//Production plan line chart
	var plan_data1=[];
	var plan_data2=[];
	var plan_xAxis=[];
	for (var i = 1; i <= 12; i++) {
		plan_xAxis.push(i+"moon");
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
	/*function clock(){
	  showToolTip_highlight(plan_chart);	  
	}
	setInterval(clock, 5000);*/
	
	//word cloud
	var cloudData=[];
	for(var i=0;i<30;i++)
	{
		cloudData.push({name:'word cloud characters'+i.toString(),value:Math.random()*1000});
	}
	wordCloud=echarts.init(document.getElementById('wordCloud'));
	wordCloud_option={
		left: 'center',
        top: 'center',        
		tooltip: {
			textStyle: {
				color: '#FFF',
				fontSize:24
			}},
		series : [{  
            type : 'wordCloud',  
            shape:'smooth',  
			drawOutOfBound: true,
            gridSize : 10,  
            sizeRange : [ 10, 48 ],
			rotationRange: [0, 0],
            textStyle : {  
                normal : {  
                    color :function (d) {
						// Random color
						return 'rgba(0,153,255,'+(d.value/1000)+ ')';
					}
                },  
                emphasis : {  
                    shadowBlur : 10,  
                    shadowColor : '#333'  
                }  
            },  
            data : cloudData
        }]
	};
	
	wordCloud.setOption(wordCloud_option);
	
	//map start
	var map_chart = echarts.init(document.getElementById('map'));
	/*map_option = {			
		title : {show:false},
		tooltip: {
			show:function(d)
			{
				return (d.value!=null && d.value>=0);
			},
			trigger: 'item',
			formatter:function(d){
				return (d.value>=0)?d.name+'</br>strategic intensity：'+(d.value).toFixed(2):'';
			},
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		legend: {
			show:false			
		},
		dataRange: {  
			show:false,
			min: 0,  
			max: 100,  
			text:['High','Low'],  
			realtime: false,  
			calculable : false,  
			color: ['rgba(0,51,204,0.8)','rgba(0,102,255,0.8)','rgba(0,153,255,0.8)'],
			splitList: [
				{start: 0,end: 30},
				{start: 31, end: 70},
				{start: 71, end: 100},
			]
		},		
		series: [{
			name: 'layout',  
			type: 'map',  
			mapType: 'china',  
			roam: false, 
			showLegendSymbol : false,
			label: {
				show: true,
				textStyle: {
					 color: '#FFF',
					 fontSize:18
				 }
			},
			itemStyle :{
				areaColor :'rgba(0,0,0,0.2)',
				borderColor : '#09F'
			},
			emphasis:{				
				areaColor :'rgba(255,0,0,0.8)',
				borderColor : 'rgba(255,0,0,0.8)'
			},
			data:[
				{name: 'Beijing', value:Math.random()*100},
				{name: 'Tianjin', value:Math.random()*100},
				{name: 'Shanghai', value:Math.random()*100},
				{name: 'Chongqing', value:Math.random()*100},
				{name: 'Hebei', value:Math.random()*100},
				{name: 'Henan', value:Math.random()*100},				
				{name: 'Liaoning', value:Math.random()*100},
				{name: 'Heilongjiang', value:Math.random()*100},
				{name: 'Hunan', value:Math.random()*100},
				{name: 'Anhui', value:Math.random()*100},
				{name: 'Shandong', value:Math.random()*100},				
				{name: 'Jiangsu', value:Math.random()*100},
				{name: 'Zhejiang', value:Math.random()*100},
				{name: 'Jiangxi', value:Math.random()*100},
				{name: 'hubei', value:Math.random()*100},
				{name: 'Guangxi', value:Math.random()*100},
				{name: 'Gansu', value:Math.random()*100},
				{name: 'Shanxi', value:Math.random()*100},
				{name: 'Inner Mongolia', value:Math.random()*100},
				{name: 'Shaanxi', value:Math.random()*100},
				{name: 'Jilin', value:Math.random()*100},
				{name: 'Fujian', value:Math.random()*100},
				{name: 'Guizhou', value:Math.random()*100},
				{name: 'Guangdong', value:Math.random()*100},
				{name: 'Qinghai', value:Math.random()*100},				
				{name: 'Sichuan', value:Math.random()*100},
				{name: 'Ningxia', value:Math.random()*100},
				{name: 'Hainan', value:Math.random()*100}
			]	
		}]
	};*/
	var mapData=[];
	for(key in geoCoordMap)
	{
		var geoCoord = geoCoordMap[key];
		mapData.push({name:key,value:geoCoord.concat((Math.random()*1000).toFixed(2))});
	}	

	map_option = {
		title : {show:false},
		tooltip : {
			trigger: 'item',
			formatter: function(params) {
                if (typeof(params.value)[2] == "undefined") {
                    return params.name + ' : ' + params.value;
                } else {
                    return params.name + ' : ' + params.value[2];
                }
            },
			textStyle: {
				color: '#FFF',
				fontSize:24
			}
		},
		legend: {
			show:false			
		},
		geo: {
			map: 'china',
			label: {
				normal: {show: false},
				emphasis: {show: false}
			},
			roam: false,
			itemStyle: {
				normal: {
					areaColor: 'rgba(0,153,255,0.6)',
					borderColor: '#09F'
				},
				emphasis: {
					areaColor: 'rgba(0,153,255,0.6)',
					borderColor: '#09F'
				}
			}
		},
		series: [{
			name: '战略layoutpoint',
			type: 'scatter',
			coordinateSystem: 'geo',
			zlevel: 1,
			rippleEffect: {
				brushType: 'stroke'
			},			
			symbolSize: function (val) {
				return val[2] / 30;
			},
			label: {				
				normal: {show: false},
				emphasis: {show: false}
			},
			itemStyle: {
				normal: {color: 'rgba(255,255,0,0.8)'},
				emphasis: {color: 'rgba(246,33,87,1)'}
			},
			data: mapData
		},	
		{
			name: '战略layoutTOP5',
			type: 'effectScatter',
			coordinateSystem: 'geo',
			zlevel: 2,
			hoverAnimation: true,
			showEffectOn: 'render',
			rippleEffect: {
				brushType: 'stroke'
			},
			label: {				
				normal: {
					show: true,
					fontFamily:'Microsoft Yahei',
					fontSize: 24,
					color:'#FFF',
					formatter: '{b}',
					position: 'right',
					shadowBlur: 5,
                    shadowColor: '#000'					
				},
				emphasis: {
					show: true
				}
			},			
			symbolSize: function (val) {
				return val[2] / 30;
			},
			itemStyle: {
				normal: {
					color: 'rgba(255,255,255,1)'
				}
			},
			data: mapData.sort(function(a, b) {
                    return b.value[2] - a.value[2];
                }).slice(0, 5)
		},	
		{
			name: 'point',
			type: 'scatter',
			coordinateSystem: 'geo',
			symbol: 'pin',
			symbolSize: function(val) {                    
				return val[2]/10;
			},
			label: {				
				normal: {
					show: true,
					fontFamily:'Arial, Helvetica, sans-serif',
					formatter:'{@[2]}',					
					textStyle: {
						color: '#FFF',
						fontSize: 18,
					}
				}
			},
			itemStyle: {
				normal: {
					color: 'rgba(246,33,87,1)', //logo color
				}
			},
			zlevel: 3,                
			data: mapData.sort(function(a, b) {
                    return b.value[2] - a.value[2];
                }).slice(0, 5)
		}]
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
	$('#currentDate').html(myDate.getFullYear()+"/"+insertZero(myDate.getMonth()+1)+"/"+insertZero(myDate.getDate()));	
	
	var maxg=Math.round(Math.random()*500)+400;
	var n1=Math.round(Math.random()*(maxg-100))+100;
	var n2=Math.round(Math.random()*(n1-50))+50;	
	var n3=(n2/maxg*100).toFixed(2);	
	
	//年schedule条
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
	
	option_Progress.title.text ="计划completion rate";
	option_Progress.series[0].data[0].value = n3;
	option_Progress.title.subtext =n3+"%";
	option_Progress.series[0].data[1].value =(100-n3);
	y_gauge4.setOption(option_Progress);
	
	//Dashboard refresh
	/*option_gauge.series[0].data[0].value = (Math.random() * 100).toFixed(2) - 0;
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
	gauge5.setOption(option_gauge);	*/
			
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

//Open modal window
function openDialog(DlgName)
{		
	$('#'+DlgName).dialog('open');	
}