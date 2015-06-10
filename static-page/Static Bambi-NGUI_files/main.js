application = {
	bindAll : function() {
		$(".button.details").bind('click', function(e) {
			$(this).parent().siblings(".device-details").toggleClass("hide");
		});
		
		$(".button.entries").bind('click', function(e) {
			$(this).parent().parent().siblings(".main-panel").children(".queue-entries").toggleClass("hide");
		});
		
		application.setJobHandler();
		
		console.log("all ok: page loaded, scripts fired");
		console.log("window.location: " + window.location);
		console.log("location.hostname: " + location.hostname);
		console.log("location.port: " + location.port);
		
		
		var ws = new WebSocket("ws://" + location.hostname + ":" + location.port + "/SimWorker/echo");
//		var ws = new WebSocket("ws://" + location.hostname + ":" + location.port + "/bambi-ngui/echo");
		ws.onopen = function () {
			console.log('ws onopen');
			ws.send("ky-ky! это я - такое сообщение!");
		};
		ws.onclose = function () {
			console.log('ws onclose');
		};
		ws.onerror = function (e) {
			console.log('ws error', e);
		};
		ws.onmessage = function (e) {
//			console.log('ws onmessage, full: ', e);
			console.log('ws onmessage, data: ', e.data);
			
			var data = e.data;
			var obj = JSON.parse(data);
			console.log('obj: ', obj);
			
			application.updateDeviceQueue(obj);
			application.addDeviceJob(obj);
		};
	},
	
	updateDeviceQueue : function(obj) {
		if (obj.UpdateDeviceQueue === undefined) {
			console.log("No UpdateDeviceQueue exists");
			return;
		}
		
		var v = obj.UpdateDeviceQueue.queueWaiting + "/" +
				obj.UpdateDeviceQueue.queueRunning + "/" +
				obj.UpdateDeviceQueue.queueCompleted + "/" +
				obj.UpdateDeviceQueue.queueAll;
		
		$("tr.device-" + obj.UpdateDeviceQueue.deviceId + " .left-panel .queue-status-bar").removeClass().addClass("queue-status-bar").addClass(obj.UpdateDeviceQueue.queueStatus);
		
		$(".device-" + obj.UpdateDeviceQueue.deviceId + " .queue-stat-value").text(v);
		$(".device-" + obj.UpdateDeviceQueue.deviceId + " .queue-stat-value").effect('highlight', {color:'#F00'}, 1000);
	},
	
	addDeviceJob : function(obj) {
		if (obj.AddDeviceJob === undefined) {
			console.log("No AddDeviceJob exists");
			return;
		}
		
		var job = $.tmpl($("#jobTemplate").html(), { param:obj.AddDeviceJob });
		$(".device-" + obj.AddDeviceJob.deviceId + " .queue-entries").prepend(job);
		
		$("tr.device-" + obj.AddDeviceJob.deviceId + " .view-level-1.hide.jobid-" + obj.AddDeviceJob.jobid).fadeIn(1000);
	},
	
	setJobHandler : function(obj) {
		$("tr").delegate("div.icon-level", "click", function() {
			console.log("change level");
			var parentEl = $(this).parent();
			
			if (parentEl.hasClass("view-level-1")) {
				parentEl.toggleClass("view-level-1");
				parentEl.toggleClass("view-level-2");
			} else if (parentEl.hasClass("view-level-2")) {
				parentEl.toggleClass("view-level-2");
				parentEl.toggleClass("view-level-3");
			} else {
				parentEl.toggleClass("view-level-3");
				parentEl.toggleClass("view-level-1");
			}
		});
	}
};

$(document).ready(function() {
	application.bindAll();
});
