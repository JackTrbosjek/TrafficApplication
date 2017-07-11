var options = {
    apiRootUrl: 'api.baasic.com',
    apiVersion: 'v1',
    enableHALJSON: false
};

var application = new baasicSdkJavaScript.BaasicApp('traffic-application', options);

function calculateAndDisplayRoute(directionsDisplay, directionsService,markerArray, stepDisplay, map) {
    // First, remove any existing markers from the map.
    for (var i = 0; i < markerArray.length; i++) {
        markerArray[i].setMap(null);
    }

    // Retrieve the start and end locations and create a DirectionsRequest using
    // WALKING directions.
    directionsService.route({
        origin: document.getElementById('start').value,
        destination: document.getElementById('end').value,
        travelMode: 'WALKING'
    }, function(response, status) {
        // Route the directions and pass the response to a function to create
        // markers for each step.
        if (status === 'OK') {
        document.getElementById('warnings-panel').innerHTML =
            '<b>' + response.routes[0].warnings + '</b>';
        directionsDisplay.setDirections(response);
        } else {
        window.alert('Directions request failed due to ' + status);
        }
    });
}

$(function () {
    loadResources();
    var searchQuery = "";
    var markers = [];
    function loadResources() {
        var searchObject = {
            pageNumber: 1,
            pageSize: 100,
            orderBy: 'dateCreated',
            orderDirection: 'asc'
        }
        if(searchQuery){
            searchObject.searchQuery= searchQuery;
        }
        application.dynamicResourceModule.find('File', searchObject)
            .then(function (collection) {
                console.log(collection);
                setupMarkers(collection.data);
            },
            function (response, status, headers, config) {
                console.log(response);
            });
    }

    document.getElementById('filer').addEventListener('click', applyFiler);
    function applyFiler(){
        searchQuery = "";
        var range = document.getElementById('range').value;
        if(range){
            var now = new Date().getTime();
            searchQuery = "WHERE date_created>'"+(now - range*1000*60*60)+"'";
        }
        var type = document.getElementById('type').value;
        if(type){
            if(searchQuery){
                searchQuery+=" AND ";
            }else{
                searchQuery = "WHERE ";
            }
            searchQuery+="is_image='"+type+"'";
        }
        loadResources();
    }
    
    function setupMarkers(data) {
        for (var i = 0; i < markers.length; i++) {
          markers[i].setMap(null);
        }
        markers = [];
        if(data.item.length == 0 || data.item ==null){
            return;
        }
        var centerLat = 0;
        var centerLon = 0;
        var infowindow = new google.maps.InfoWindow();
        _.each(data.item, function (item) {
            var contentString = '';
            var date = new Date(item.date_created);
            var icon;
            if (item.is_image) {
                icon = "img/image.png";
                contentString = '<p>' + date.toLocaleString() + '</p><a href="' + item.file_url + '" target="_blank"><img src="' + item.file_url + '?width=150&height=150" height="150" width="150"/></a>';
            } else {
                icon = "img/video.png";
                contentString = '<p>' + date.toLocaleString() + '</p><a href="' + item.file_url + '" target="_blank"><video width="320" height="240" controls>  <source src="' + item.file_url + '" type="video/mp4"></video></a>';
            }

            
            var latitude = Number(item.latitude);
            var longitude = Number(item.longitude);
            centerLat += latitude;
            centerLon += longitude;
            var location = { lat: latitude, lng: longitude };
            var marker = new google.maps.Marker({
                position: location,
                icon: icon,
                map: map,
                title: 'Marker Title'
            });
            marker.addListener('click', function () {
                infowindow.setContent(contentString);
                infowindow.open(map, marker);
            });
            markers.push(marker);
        });
        map.setCenter({ lat: centerLat / data.item.length, lng: centerLon / data.item.length });
    }

    

});