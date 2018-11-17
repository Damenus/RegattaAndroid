package pl.f4.regatta;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class map extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        WebView webview = (WebView) findViewById(R.id.webview);
        //webview.loadUrl("http://vps485240.ovh.net:8080/");
        webview.getSettings().setJavaScriptEnabled(true);
        String summary = "\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n" +
                "        <title>OpenSeaMap</title>\n" +
                "\n" +
                "\n" +
                "        <style type=\"text/css\">\n" +
                "            .olImageLoadError {\n" +
                "                display: none !important;\n" +
                "            }\n" +
                "        </style>\n" +
                "\n" +
                "        <!-- bring in the OpenLayers javascript library\n" +
                "            (here we bring it from the remote site, but you could\n" +
                "            easily serve up this javascript yourself) -->\n" +
                "        <script src=\"http://www.openlayers.org/api/OpenLayers.js\"></script>\n" +
                "\n" +
                "        <!-- bring in the OpenStreetMap OpenLayers layers.\n" +
                "            Using this hosted file will make sure we are kept up\n" +
                "            to date with any necessary changes -->\n" +
                "        <script src=\"http://www.openstreetmap.org/openlayers/OpenStreetMap.js\"></script>\n" +
                "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/harbours.js\"></script>\n" +
                "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/map_utils.js\"></script>\n" +
                "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/utilities.js\"></script>\n" +
                "        <script type=\"text/javascript\">\n" +
                "\n" +
                "            var map;\n" +
                "            var layer_mapnik;\n" +
                "            var layer_tah;\n" +
                "            var layer_seamark;\n" +
                "            var marker;\n" +
                "\n" +
                "            // Position and zoomlevel of the map\n" +
                "            var lon = 12.0915;\n" +
                "            var lat = 54.1878;\n" +
                "            var zoom = 15;\n" +
                "                \n" +
                "            var linkTextSkipperGuide = \"Beschreibung auf SkipperGuide\";\n" +
                "            var linkTextWeatherHarbour = \"Meteogramm\";\n" +
                "            var language = 'de';\n" +
                "\n" +
                "            \n" +
                "            function jumpTo(lon, lat, zoom) {\n" +
                "                var x = Lon2Merc(lon);\n" +
                "                var y = Lat2Merc(lat);\n" +
                "                map.setCenter(new OpenLayers.LonLat(x, y), zoom);\n" +
                "                return false;\n" +
                "            }\n" +
                "\n" +
                "            function Lon2Merc(lon) {\n" +
                "                return 20037508.34 * lon / 180;\n" +
                "            }\n" +
                "\n" +
                "            function Lat2Merc(lat) {\n" +
                "                var PI = 3.14159265358979323846;\n" +
                "                lat = Math.log(Math.tan( (90 + lat) * PI / 360)) / (PI / 180);\n" +
                "                return 20037508.34 * lat / 180;\n" +
                "            }\n" +
                "\n" +
                "            function addMarker(layer, lon, lat, popupContentHTML) {\n" +
                "                var ll = new OpenLayers.LonLat(Lon2Merc(lon), Lat2Merc(lat));\n" +
                "                var feature = new OpenLayers.Feature(layer, ll);\n" +
                "                feature.closeBox = true;\n" +
                "                feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(260, 100) } );\n" +
                "                feature.data.popupContentHTML = popupContentHTML;\n" +
                "                feature.data.overflow = \"hidden\";\n" +
                "\n" +
                "                marker = new OpenLayers.Marker(ll);\n" +
                "                marker.feature = feature;\n" +
                "\n" +
                "                var markerClick = function(evt) {\n" +
                "                    if (this.popup == null) {\n" +
                "                        this.popup = this.createPopup(this.closeBox);\n" +
                "                        map.addPopup(this.popup);\n" +
                "                        this.popup.show();\n" +
                "                    } else {\n" +
                "                        this.popup.toggle();\n" +
                "                    }\n" +
                "                    OpenLayers.Event.stop(evt);\n" +
                "                };\n" +
                "                marker.events.register(\"mousedown\", feature, markerClick);\n" +
                "\n" +
                "                layer.addMarker(marker);\n" +
                "                map.addPopup(feature.createPopup(feature.closeBox));\n" +
                "            }\n" +
                "\n" +
                "            function getTileURL(bounds) {\n" +
                "                var res = this.map.getResolution();\n" +
                "                var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));\n" +
                "                var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));\n" +
                "                var z = this.map.getZoom();\n" +
                "                var limit = Math.pow(2, z);\n" +
                "                if (y < 0 || y >= limit) {\n" +
                "                    return null;\n" +
                "                } else {\n" +
                "                    x = ((x % limit) + limit) % limit;\n" +
                "                    url = this.url;\n" +
                "                    path= z + \"/\" + x + \"/\" + y + \".\" + this.type;\n" +
                "                    if (url instanceof Array) {\n" +
                "                        url = this.selectUrl(path, url);\n" +
                "                    }\n" +
                "                    return url+path;\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            function drawmap() {\n" +
                "\n" +
                "\n" +
                "                map = new OpenLayers.Map('map', {\n" +
                "                    projection: new OpenLayers.Projection(\"EPSG:900913\"),\n" +
                "                    displayProjection: new OpenLayers.Projection(\"EPSG:4326\"),\n" +
                "                    eventListeners: {\n" +
                "                        \"moveend\": mapEventMove,\n" +
                "                        //\"zoomend\": mapEventZoom\n" +
                "                    },\n" +
                "                    controls: [\n" +
                "                        new OpenLayers.Control.Navigation(),\n" +
                "                        new OpenLayers.Control.ScaleLine({topOutUnits : \"nmi\", bottomOutUnits: \"km\", topInUnits: 'nmi', bottomInUnits: 'km', maxWidth: '40'}),\n" +
//                "                       new OpenLayers.Control.LayerSwitcher(),\n" +
//                "                        new OpenLayers.Control.MousePosition(),\n" +
//                "                        new OpenLayers.Control.PanZoomBar()" +
                "],\n" +
                "                        maxExtent:\n" +
                "                        new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),\n" +
                "                    numZoomLevels: 18,\n" +
                "                    maxResolution: 156543,\n" +
                "                    units: 'meters'\n" +
                "                });\n" +
                "\n" +
                "                // Add Layers to map-------------------------------------------------------------------------------------------------------\n" +
                "                // Mapnik\n" +
                "                layer_mapnik = new OpenLayers.Layer.OSM.Mapnik(\"Mapnik\");\n" +
                "                // Seamark\n" +
                "                layer_seamark = new OpenLayers.Layer.TMS(\"Seezeichen\", \"http://tiles.openseamap.org/seamark/\", { numZoomLevels: 18, type: 'png', getURL: getTileURL, isBaseLayer: false, displayOutsideMaxExtent: true});\n" +
                "                // Harbours\n" +
                "                layer_pois = new OpenLayers.Layer.Vector(\"Häfen\", { projection: new OpenLayers.Projection(\"EPSG:4326\"), visibility: true, displayOutsideMaxExtent:true});\n" +
                "                layer_pois.setOpacity(0.8);\n" +
                "                \n" +
                "                map.addLayers([layer_mapnik, layer_seamark, layer_pois]);\n" +
                "                jumpTo(lon, lat, zoom);\n" +
                "\n" +
                "                // Update harbour layer\n" +
                "                refreshHarbours();\n" +
                "            }\n" +
                "\n" +
                "            // Map event listener moved\n" +
                "            function mapEventMove(event) {\n" +
                "                // Update harbour layer\n" +
                "                refreshHarbours();\n" +
                "            }\n" +
                "    </script>\n" +
                "\n" +
                "</head>\n" +
                "\n" +
                "<!-- body.onload is called once the page is loaded (call the 'init' function) -->\n" +
                "<body onload=\"drawmap();\">\n" +
                "\n" +
                "    <!-- define a DIV into which the map will appear. Make it take up the whole window -->\n" +
                "    <div style=\"width:100%; height:100%\" id=\"map\"></div>\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        webview.loadData(summary, "text/html", null);
    }
}
