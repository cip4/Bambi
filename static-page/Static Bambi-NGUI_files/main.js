var i = 0;
var jobid = 0;

application = {
	bindAll : function() {
		$(".button.details").bind('click', function(e) {
			$(this).parent().siblings(".device-details").toggleClass("hide");
		});
		
		$(".button.entries").bind('click', function(e) {
			$(this).parent().parent().siblings(".main-panel").children(".queue-entries").toggleClass("hide");
		});
		
		setInterval(function() {
			console.log('i: ' + i);
			$(".device-Device-01 .queue-stat-value").text("0/0/0/" + i);
			$(".device-Device-01 .queue-stat-value").effect('highlight', {color:'#F00'}, 1000);
			
			$(".device-Device-02 .queue-stat-value").text("0/0/0/" + i);
			$(".device-Device-02 .queue-stat-value").effect('highlight', {color:'#F00'}, 1000);
			
			i++;
		}, 3000);
		
		setInterval(function() {
			var jobCode =
			'<div class="view-level-1 hide jobid-' + jobid + '">' +
            '<div class="icon-level"></div>' +
            '<div style="display:inline-block;" class="">Job ID: ' + jobid + '</div>' +
            '<div class="queue-status-bar running"></div>' +
            '<div>* Submission: 2015-MAR-08 12:34:56</div>' +
            '<div class="view level-basic">' +
              '<div>* Priority: xxx</div>' +
              '<div>* Start: 2015-MAR-08 12:34:56</div>' +
              '<div>* End: 2015-MAR-08 12:34:56</div>' +
            '</div>' +
            '<div class="view level-full">' +
              '<div>* Attribute-01: xxx</div>' +
              '<div>* Attribute-02: xxx</div>' +
            '</div>' +
            '<div>&nbsp;</div>' +
          '</div>';
			
			$(".device-Device-01 .queue-entries").prepend(jobCode);
			
			$("tr.device-Device-01 .view-level-1.hide.jobid-" + jobid).fadeIn(1000);
//			$("tr.device-Device-01 .view-level-1.hide").fadeIn(1000);
			
			jobid++;
		}, 15000);
		
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
			
			$("#" + obj.deviceId + " .queueAll").text(obj.queueAll);
		};
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
