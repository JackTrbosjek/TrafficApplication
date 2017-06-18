var options = {
    apiRootUrl: 'api.baasic.com',
    apiVersion: 'v1',
    enableHALJSON: false
};

var application = new baasicSdkJavaScript.BaasicApp('traffic-application', options);

(function (application) {
    loadResources();


    function loadResources() {
        application.dynamicResourceModule.find('File', {
            pageNumber: 1,
            pageSize: 100,
            orderBy: 'dateCreated',
            orderDirection: 'asc'
        })
            .then(function (collection) {
                console.log(collection);
                setupMarkers(collection.data);
            },
            function (response, status, headers, config) {
                console.log(response);
            });
    }

    function setupMarkers(data) {
        var centerLat = 0;
        var centerLon = 0;
        _.each(data.item, function (item) {
            var contentString = '';
            var date = new Date(item.date_created);
            if (item.is_image) {
                contentString = '<p>' + date.toLocaleString() + '</p><img src="' + item.file_url + '?width=150&height=150" height="150" width="150"/>';
            } else {
                contentString = '<p>' + date.toLocaleString() + '</p><video width="320" height="240" controls>  <source src="' + item.file_url + '" type="video/mp4"></video>';
            }

            var infowindow = new google.maps.InfoWindow({
                content: contentString
            });
            var latitude = Number(item.latitude);
            var longitude = Number(item.longitude);
            centerLat += latitude;
            centerLon += longitude;
            var location = { lat: latitude, lng: longitude };
            var marker = new google.maps.Marker({
                position: location,
                map: map,
                title: 'Marker Title'
            });
            marker.addListener('click', function () {
                infowindow.open(map, marker);
            });
        });
        map.setCenter({ lat: centerLat / data.item.length, lng: centerLon / data.item.length });
    }

})(application);