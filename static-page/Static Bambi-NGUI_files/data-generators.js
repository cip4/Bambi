var i = 0;
var jobid = 4;

DataGenerator = {
	start : function() {
		setInterval(function() {
			console.log('jobid: ' + jobid);
			
			var obj =
				{
				UpdateDeviceQueue : {deviceId:"Device-01", queueStatus:"Held", queueStatistic:"0/0/0/" + jobid},
				AddDeviceJob : {deviceId:"Device-01", jobid:jobid, status:"Waiting", submission:"2015-MAR-08 12:34:56",
					start:"2015-MAR-08 12:34:56", end:"2015-MAR-09 12:34:56"}
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
