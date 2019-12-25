
*EMDKScanner Cordova Plugin*
=========================================================
This plugin is based on [https://github.com/DavidTalamona/Cordova-Plugin-BarcodeScanner-EMDK](https://github.com/DavidTalamona/Cordova-Plugin-BarcodeScanner-EMDK) by DavidTalamona

*LICENSE*
=========================================================
Copyright 2017 BULLZER TECHNOLOGIES, SL

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

斑马扫描枪(Zebra) 插件使用说明
1.  安装   cordova plugin add /XXX/ZebraScanner

2.  angular里直接使用    declare var ZebraScanner: any;

3. 调用扫描里的方法   
a.  首先调用初始化方法   ZebraScanner.init();
b.  扫描：   ZebraScanner.startScanning(function (code){
							String result = code;  //code为扫描二维码
				     }, function(error) {
					 
					 }
				 )
