// JavaScript Document

window.onload = checkState;

// check the url parameters
function checkState() {
	var arrGroups = new Array;
	
	// get argument String
	var strParam = window.location.search;
	
	// remove "?"
	strParam = strParam.substr(1,strParam.length);
	
	// split whole string to parameters
	var arrGroups = strParam.split("&");
	
			document.getElementById('refreshButton').setAttribute('value', 'Refresh continually');
			document.getElementById('refreshValue').setAttribute('name', 'refresh');
			document.getElementById('refreshValue').setAttribute('value', 'true');
	// for every argument


	for (var i=0; i<arrGroups.length; i++) {
		
		// check if refresh is enabled	
		if(arrGroups[i]=='refresh=true') 
		{	
			// set input values for the refresh button
			document.getElementById('refreshButton').setAttribute('value', 'Stop refresh');
			document.getElementById('refreshValue').setAttribute('name', 'refresh');
			document.getElementById('refreshValue').setAttribute('value', 'false');
			// trigger for refresh and Time
  			setTimeout("window.location.reload();", 7000); // milliseconds
		}
		else if (arrGroups[i]=='refresh=false')
		{
			
			// set input values for the refresh button
			document.getElementById('refreshButton').setAttribute('value', 'Refresh continually');
			document.getElementById('refreshValue').setAttribute('name', 'refresh');
			document.getElementById('refreshValue').setAttribute('value', 'true');
		}


	}
	
}