var exec = require('cordova/exec');

var ZebraScanner = function () {}

ZebraScanner.prototype.init = function () {
	exec(function () {}, function () {}, "ZebraScanner", "init", []);
};

ZebraScanner.prototype.startScanning = function (successCallback, errorCallback) {
	exec(successCallback, errorCallback, "ZebraScanner", "startScanning", []);
};

ZebraScanner.prototype.stopScanning = function () {
	exec(function () {}, function () {}, "ZebraScanner", "stopScanning", []);
};

module.exports = new ZebraScanner();
