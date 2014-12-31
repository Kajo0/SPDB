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
	function initialize() {
		var mapOptions = {
			center : {
				lat : -34.397,
				lng : 150.644
			},
			zoom : 8
		};
		var map = new google.maps.Map(document.getElementById('map-canvas'),
				mapOptions);
	}
	google.maps.event.addDomListener(window, 'load', initialize);

	function findRoute() {
		var orig = $('input[name=origin]').val();
		var dest = $('input[name=destination]').val();
		$
				.get("find-transport", {
					origin : orig,
					destination : dest
				},
						function(response) {
							var firstPoint = response[0];

							var mapOptions = {
								zoom : 18,
								center : new google.maps.LatLng(firstPoint.lat,
										firstPoint.lng),
								mapTypeId : google.maps.MapTypeId.TERRAIN
							};

							var map = new google.maps.Map(document
									.getElementById('map-canvas'), mapOptions);

							var coordinates = [];
							for (var i = 0; i < response.length; i++) {
								var point = response[i];
								var coord = new google.maps.LatLng(point.lat,
										point.lng);
								coordinates.push(coord);
							}

							var path = new google.maps.Polyline({
								path : coordinates,
								geodesic : true,
								strokeColor : '#FF0000',
								strokeOpacity : 1.0,
								strokeWeight : 2
							});

							path.setMap(map);
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
