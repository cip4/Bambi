settings = {
	bindAll : function() {
		$("#button-apply-settings").bind('click', function(e) {
			console.log("Send POST request, serialize: " + $("#saveSettings").serialize());
			$.ajax({
				type: "POST",
				url: "/bambi-ngui/save_settings/",
//				dataType: "json",
				data: $("#saveSettings").serialize(), // "dateTimeFormatter=123",
				success: function(data, textStatus, jqXHR) {
					console.log("POST: success");
				},
				error: function (jqXHR, textStatus, errorThrown) {
					console.log("POST: error");
				}
			});
		});
		
		console.log("all ok: page loaded, scripts fired");
	},
	
	empty : function(obj) {
	}
};

$(document).ready(function() {
	settings.bindAll();
});
