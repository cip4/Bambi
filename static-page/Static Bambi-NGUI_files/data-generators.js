var i = 0;
var jobid = 4;

DataGenerator = {
	start : function() {
		setInterval(function() {
			console.log('jobid: ' + jobid);
			
			var obj =
				{
				UpdateDeviceQueue : {deviceId:"Device-01", queueWaiting:0, queueRunning:0, queueCompleted:0, queueAll:jobid, queueStatus:"Held"},
				AddDeviceJob : {deviceId:"Device-01", jobid:jobid, status:"running", submission:"2015-MAR-08 12:34:56"}
				};
			
			application.updateDeviceQueue(obj);
			application.addDeviceJob(obj);
			
			jobid++;
		}, 15000);
	}
};

$(document).ready(function() {
	DataGenerator.start();
});
