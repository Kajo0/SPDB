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
	var drivingPath;
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

	function getPath(polyline, color) {
		var coordinates = [];
		for (var i = 0; i < polyline.length; i++) {
			var point = polyline[i];
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

	function handleRouteResponse(response, resultDiv, color) {
		console.log(response);
		if (response.status == "ERROR") {
			resultDiv.text("Error: " + response.description);
			return null;
		} else {
			var path = getPath(response.route.polyline, color);
			path.setMap(map);

			resultDiv.html("Czas trwania podrozy: "
					+ response.route.time.toFixed(2) + "h<br>"
					+ "Dlugosc drogi: " + response.route.length.toFixed(2)
					+ "km<br>" + "Start: " + response.departureTime + "<br>"
					+ "Koniec: " + response.arrivalTime);
			return path;
		}
	}

	function findRoute() {
		var orig = $('input[name=origin]').val();
		var dest = $('input[name=destination]').val();

		if (drivingPath != null) {
			drivingPath.setMap(null);
		}
		if (transitPath != null) {
			transitPath.setMap(null);
		}

		$.get("transit", {
			origin : orig,
			destination : dest
		}, function(response) {
			var path = handleRouteResponse(response, $('#transit-result'),
					'#FF0000');
			if (path != null) {
				transitPath = path;
			}
		});

		$.get("driving", {
			origin : orig,
			destination : dest
		}, function(response) {
			var path = handleRouteResponse(response, $('#driving-result'),
					'#0000FF');
			if (path != null) {
				drivingPath = path;
			}
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
	<table>
		<tr>
			<td>Transit</td>
			<td>Route</td>
		</tr>
		<tr>
			<td>
				<div id="transit-result"></div>
			</td>
			<td>
				<div id="driving-result"></div>
			</td>
		</tr>
	</table>


	<div id="map-canvas"></div>
</body>
</html>
