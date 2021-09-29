let map;
let markers;

/**Method to add the map to the webwindow.*/
function addMap(){
    window.jSToJavaBridge.loggerDebug("Adding map");
    markers = L.layerGroup();

    //Standard OSM:
    let streetOSMLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
        maxZoom: 19,
        attribution: '&copy; <a onclick="return false;" href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    });

    //dark:
    let darkModeLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; <a onclick="return false;" href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a onclick="return false;" href="https://carto.com/attributions">CARTO</a>',
        subdomains: 'abcd',
        maxZoom: 19
    });


    map = L.map('mapId', {
        center: [63.4305, 10.3951],
        zoom: 10,
        layers: [streetOSMLayer, darkModeLayer]
    });
    map.doubleClickZoom.disable();

    let baseMaps = {
        "Street": streetOSMLayer,
        "Dark mode": darkModeLayer
    };

    L.control.layers(baseMaps).addTo(map);
    window.jSToJavaBridge.loggerDebug("Added map and layer controls");
    getBounds();
    addMarkerFunctionality();
}



/**
 * Method to add markers to the map
 * @param singleMarkers all single markers that needs to be added to the map
 * @param multiMarkers all multimarkers that needs to be added to the map*/
function addMarkers(singleMarkers, multiMarkers){
    window.jSToJavaBridge.loggerDebug("Adding markers");

    markers.clearLayers();
    for(let i=0;i<singleMarkers.length;i++){
        let marker = addMarker(singleMarkers[i]);
        markers.addLayer(marker);
    }
    for(let i=0;i<multiMarkers.length;i++){
        let marker = addCollectionMarker(multiMarkers[i]);
        markers.addLayer(marker);
    }
    map.addLayer(markers);
}

/**
 * Method that adds a single marker
 * @param imgData the necessary data for the marker*/
function addMarker(imgData){

    let icon = L.icon({
        iconUrl: "file://"+imgData[0],
        iconSize:     [40, 40], // size of the icon
        iconAnchor:   [20, 40], // point of the icon which will correspond to marker's location
        popupAnchor:  [0, -40] // point from which the popup should open relative to the iconAnchor
    });

    let imgOnMap = L.marker([imgData[1], imgData[2]], {icon: icon});

    imgOnMap.clicked = 0;
    imgOnMap.on("click", function () {
        imgOnMap.clicked++;
        setTimeout(function(){  //This function will wait 300 milliseconds to check if the user intended a single click, or muliple.
            if(imgOnMap.clicked == 1){
                window.jSToJavaBridge.loggerDebug("Clicked marker with image path: " + imgData[0]);
                window.jSToJavaBridge.clickedMarker(imgData[0]);
                imgOnMap.clicked = 0;
            }
        }, 300);
    });
    imgOnMap.on("dblclick", function () {
        window.jSToJavaBridge.loggerDebug("Double clicked marker with image path: " + imgData[0]);
        window.jSToJavaBridge.clickedMarker(imgData[0]);
        imgOnMap.clicked = 0;
    });

    window.jSToJavaBridge.loggerDebug("Added marker with image data: " + imgData);
    return imgOnMap
}


/**
 * Method that adds a collection marker
 * @param imgData the necessary data for the marker*/
function addCollectionMarker(imgData) {
    let icon = L.icon({
        iconUrl: "file://C:\\Users\\robvo\\Desktop\\app-product\\src\\main\\resources\\mapResources\\icons8-image-48.png",
        iconSize:     [50, 50], // size of the icon
        iconAnchor:   [25, 50], // point of the icon which will correspond to marker's location
        popupAnchor:  [0, -50] // point from which the popup should open relative to the iconAnchor
    });

    let marker = L.marker([imgData[0], imgData[1]], {icon: icon});


    let content = addPopupContent(imgData);
    let popup = L.popup({
        maxHeight: 300
    }).setContent(content);

    marker.bindPopup(popup);
    window.jSToJavaBridge.loggerDebug("Added collection-marker with image data: " + imgData);
    return marker;
}

/**
 * Method that adds the popup content to the collection markers.
 * @param imgData the necessary data for the popup.*/
function addPopupContent(imgData) {
    let content = "";
    for(let i=2;i<imgData.length;i++){
        let imgSource = "\"file://" + imgData[i] + "\"";
        let imgTag = "<img src="+ imgSource +"height=\"45\" width=\"45\" class='imgOnPopup' oncontextmenu=\"return false;\" onclick='clickedImageInPopup(this.src)'>";
        content += imgTag;
    }
    window.jSToJavaBridge.loggerDebug("Generated popup-content");
    return content;
}



/**Method that get the bounds of the map for the user. Method is only used for logging purposes. */
function getBounds() {
    map.on('resize moveend zoomend', function () {
        let mapBound = map.getBounds();
        let bounds = mapBound.getNorth()+", "+mapBound.getEast()+", "+mapBound.getSouth()+", "+mapBound.getWest();
        window.jSToJavaBridge.loggerDebug("Moved/resized/zoomed map, with map bounds: " + bounds);
    });
}

/**
 * Method that ads the functionality that they are not rendered when not in the users screen. */
function addMarkerFunctionality(){
    L.Marker.addInitHook(function() {
            // setup virtualization after marker was added
            this.on('add', function() {
                this._updateIconVisibility = function() {
                    let map = this._map,
                        isVisible = map.getBounds().contains(this.getLatLng()),
                        wasVisible = this._wasVisible,
                        icon = this._icon,
                        iconParent = this._iconParent,
                        shadow = this._shadow,
                        shadowParent = this._shadowParent;

                    // remember parent of icon
                    if (!iconParent) {
                        iconParent = this._iconParent = icon.parentNode;
                    }
                    if (shadow && !shadowParent) {
                        shadowParent = this._shadowParent = shadow.parentNode;
                    }
                    // add/remove from DOM on change
                    if (isVisible != wasVisible) {
                        if (isVisible) {
                            iconParent.appendChild(icon);
                            if (shadow) {
                                shadowParent.appendChild(shadow);
                            }
                        } else {
                            iconParent.removeChild(icon);
                            if (shadow) {
                                shadowParent.removeChild(shadow);
                            }
                        }
                        this._wasVisible = isVisible;
                    }
                };
                // on map size change, remove/add icon from/to DOM
                this._map.on('resize moveend zoomend', this._updateIconVisibility, this);
                this._updateIconVisibility();

            }, this);
    });
    window.jSToJavaBridge.loggerDebug("Added extra marker functionality.");
}


/**
 * Method that sends the clicked image-path in a popup to the jSToJavaBridge-object
 * @param imgPath the image-path of the clicked image. */
function clickedImageInPopup(imgPath){
    imgPath = imgPath.substring(8,imgPath.length);
    window.jSToJavaBridge.loggerDebug("Clicked on a image in a popup with path: " + imgPath);
    window.jSToJavaBridge.clickedMarker(imgPath);
}










/*--------------------------------------TEMAER (TILE-LAYERS)--------------------------------------*/

//trenger nøkkel:

/*L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    id: 'mapbox/streets-v11',
    tileSize: 512,
    zoomOffset: -1,
    accessToken: 'pk.eyJ1Ijoicm9idm9sZCIsImEiOiJjazgzZmdsam0wbDRlM3Juemd4eWxwZXh0In0.ijetzKC8kB_1qz_Wi54ZJQ'
}).addTo(mymap);*/



//trenger ikke nøkkel:

//Hvit:
/*L.tileLayer('https://stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}{r}.{ext}', {
    attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    subdomains: 'abcd',
    minZoom: 0,
    maxZoom: 20,
    ext: 'png'
}).addTo(mymap);*/


//Darkmode:
/*var CartoDB_DarkMatter = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
    subdomains: 'abcd',
    maxZoom: 19
}).addTo(mymap);*/