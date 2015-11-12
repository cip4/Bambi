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
	},

	bindContextMenuQueue : function() {
		$(".left-panel").contextmenu({
			delegate: ".queue-status-bar",
			menu: [
				{title: "Set queue status:"},
				{title: "----"},
				{title: "Open", cmd: "open"},
				{title: "Close", cmd: "close"},
				{title: "Resume", cmd: "resume"},
				{title: "Hold", cmd: "hold"}
			],
			select: function(event, ui) {
				console.log("Selected '" + ui.cmd + "' command on element text: " + ui.target.text());
				console.log("ui.target.context.id: " + ui.target.context.id);
				$.ajax({
					method: "POST",
					url: "queueChangeStatus",
					data: "deviceId=" + ui.target.context.id + "&newStatus=" + ui.cmd
				});
			}
		});
	},

	bindContextMenuJob : function() {
		$(".main-panel").contextmenu({
			delegate: ".job-status-bar",
			menu: [
				{title: "Set job status:"},
				{title: "----"},
				{title: "Waiting", cmd: "Waiting"},
				{title: "Removed", cmd: "Removed"},
				{title: "PendingReturn", cmd: "PendingReturn"},
				{title: "Suspended", cmd: "Suspended"},
				{title: "Completed", cmd: "Completed"},
				{title: "Aborted", cmd: "Aborted"}
			],
			select: function(event, ui) {
				console.log("Selected '" + ui.cmd + "' command on element text: " + ui.target.text());
				console.log("ui.target.context.id: " + ui.target.context.id);
//				$.ajax({
//					method: "POST",
//					url: "jobChangeStatus",
//					data: "deviceId=" + ui.target.context.id + "&newStatus=" + ui.cmd
//				});
			}
		});
	},

	bindWebSocketTransport : function() {
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
			
			if (obj.UpdateDeviceQueue !== undefined) {
				application.updateDeviceQueue(obj);
			} else if (obj.AddDeviceJob !== undefined) {
				application.addDeviceJob(obj);
			} else if (obj.DeleteDeviceJob !== undefined) {
				application.deleteDeviceJob(obj);
			} else if (obj.JobPropertiesChanged !== undefined) {
				application.jobPropertiesChanged(obj);
			} else {
				console.log('Unsupported command, obj: ', obj);
			}
		};
	},

	updateDeviceQueue : function(obj) {
		var v = obj.UpdateDeviceQueue.queueStatistic;
		
		$("tr.device-" + obj.UpdateDeviceQueue.deviceId + " .left-panel .queue-status-bar").removeClass().addClass("queue-status-bar").addClass(obj.UpdateDeviceQueue.queueStatus);
		$("tr.device-" + obj.UpdateDeviceQueue.deviceId + " .left-panel .queue-status-bar").html(obj.UpdateDeviceQueue.queueStatus);
		
		$(".device-" + obj.UpdateDeviceQueue.deviceId + " .queue-stat-value").text(v);
		$(".device-" + obj.UpdateDeviceQueue.deviceId + " .queue-stat-value").effect('highlight', {color:'#F00'}, 1000);
	},
	
	jobPropertiesChanged : function(obj) {
		$("tr.device-" + obj.JobPropertiesChanged.deviceId + " .main-panel .jobid-" + obj.JobPropertiesChanged.jobId + " .job-status-bar").removeClass().addClass("job-status-bar").addClass(obj.JobPropertiesChanged.status);
		
		$("tr.device-" + obj.JobPropertiesChanged.deviceId + " .main-panel .jobid-" + obj.JobPropertiesChanged.jobId + " .job-started").text(obj.JobPropertiesChanged.start);
		$("tr.device-" + obj.JobPropertiesChanged.deviceId + " .main-panel .jobid-" + obj.JobPropertiesChanged.jobId + " .job-ended").text(obj.JobPropertiesChanged.end);
	},
	
	addDeviceJob : function(obj) {
		var job = $.tmpl($("#jobTemplate").html(), { param:obj.AddDeviceJob });
		$(".device-" + obj.AddDeviceJob.deviceId + " .queue-entries").prepend(job);
		
		$("tr.device-" + obj.AddDeviceJob.deviceId + " .view-level-1.hide.jobid-" + obj.AddDeviceJob.jobid).fadeIn(1000);
	},
	
	deleteDeviceJob : function(obj) {
		$("tr.device-" + obj.DeleteDeviceJob.deviceId + " .jobid-" + obj.DeleteDeviceJob.jobid).fadeOut(1000, function() {
			$("tr.device-" + obj.DeleteDeviceJob.deviceId + " .jobid-" + obj.DeleteDeviceJob.jobid).remove();
		});
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
	application.bindWebSocketTransport();
	application.bindAll();
	application.bindContextMenuQueue();
	application.bindContextMenuJob();
});
