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
	var paths=[];
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

	function getRandomColor() {
	    var letters = '0123456789ABCDEF'.split('');
	    var color = '#';
	    for (var i = 0; i < 6; i++ ) {
	        color += letters[Math.floor(Math.random() * 16)];
	    }
	    return color;
	}
	
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

	function handleRouteResponse(response, resultDiv) {
		console.log(response);
		if (response.status == "ERROR") {
			resultDiv.text("Error: " + response.description);
			return null;
		} else {
			for(var i in response.route.parts) {
				var color = getRandomColor();
				var path = getPath(response.route.parts[i].polyline, color);
	            path.setMap(map);
	            paths.push(path);
			}
			

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

		for (var i in paths) {
			paths[i].setMap(null);
		}
		paths = [];

		$.get("transit", {
			origin : orig,
			destination : dest,
            departure_time : new Date().getTime()
		}, function(response) {
			var path = handleRouteResponse(response, $('#transit-result'));
			$('#transit-result').append(response.description);
		});

		$.get("driving", {
			origin : orig,
			destination : dest,
			departure_time : new Date().getTime()
		}, function(response) {
			var path = handleRouteResponse(response, $('#driving-result'));
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
