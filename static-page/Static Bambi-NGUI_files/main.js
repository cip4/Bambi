application = {
	bindAll : function() {
		$(".button.details").bind('click', function(e) {
			$(this).parent().siblings(".device-details").toggleClass("hide");
		});
		
		$(".button.entries").bind('click', function(e) {
			$(this).parent().parent().siblings(".main-panel").children(".queue-entries").toggleClass("hide");
		});
		
		$(".icon-level").bind('click', function(e) {
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
	}
};

$(document).ready(function() {
	application.bindAll();
});
