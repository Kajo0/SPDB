<html>
<head>
<style type="text/css">
html, body, #map-canvas {
	height: 100%;
	margin: 0;
	padding: 0;
}
</style>
<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js">
	
</script>
<script src="//code.jquery.com/jquery-1.11.2.min.js" /></script>
<script type="text/javascript">

var map;
var carPath;
var transitPath;
	function initialize() {
		var mapOptions = {
			center : {
				lat : 52.232938,
				lng : 21.0611941
			},
			zoom : 12
		};
		map = new google.maps.Map(document.getElementById('map-canvas'),
				mapOptions);
	}
	google.maps.event.addDomListener(window, 'load', initialize);

	function getPath(response, color) {
		var coordinates = [];
		for (var i = 0; i < response.length; i++) {
			var point = response[i];
			var coord = new google.maps.LatLng(point.lat, point.lng);
			coordinates.push(coord);
		}

		var path = new google.maps.Polyline({
			path : coordinates,
			geodesic : true,
			strokeColor : color,
			strokeOpacity : 1.0,
			strokeWeight : 2
		});

		return path;
	}

	function findRoute() {
		var orig = $('input[name=origin]').val();
		var dest = $('input[name=destination]').val();
		
		if (carPath != null) {
			carPath.setMap(null);
		}
		if (transitPath != null) {
			transitPath.setMap(null);
		}
		
		$.get("find-transport", {
			origin : orig,
			destination : dest
		},
				function(response) {
					var firstPoint = response[0];

					carPath = getPath(response, '#FF0000');
					carPath.setMap(map);
				});

		$.get("find-route", {
			origin : orig,
			destination : dest
		},
				function(response) {
					var firstPoint = response[0];

					transitPath = getPath(response, '#00FF00');
					transitPath.setMap(map);
				});

	}
</script>
</head>
<body>
	<form action="javascript:findRoute();" id="find_route_form">
		Origin:<input type="text" name="origin"
			value="Polnego wiatru 24, Warszawa" /> Destination:<input
			type="text" name="destination" value="Politechnika, Warszawa" /> <input
			type="submit" value="OK" />
	</form>
	<div id="map-canvas"></div>
</body>
</html>
