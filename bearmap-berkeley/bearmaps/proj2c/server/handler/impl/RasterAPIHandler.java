package bearmaps.proj2c.server.handler.impl;

import bearmaps.proj2ab.Point;
import bearmaps.proj2c.AugmentedStreetMapGraph;
import bearmaps.proj2c.server.handler.APIRouteHandler;
import spark.Request;
import spark.Response;
import bearmaps.proj2c.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bearmaps.proj2c.utils.Constants.*;

/**
 * Handles requests from the web browser for map images. These images
 * will be rastered into one large image to be displayed to the user.
 *
 * @author rahul, Josh Hug, _________
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {

    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest(). <br>
     * ullat : upper left corner latitude, <br> ullon : upper left corner longitude, <br>
     * lrlat : lower right corner latitude,<br> lrlon : lower right corner longitude <br>
     * w : user viewport window width in pixels,<br> h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};


    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel
     *     (LonDPP) possible, while still covering less than or equal to the amount of
     *     longitudinal distance per pixel in the query box for the user viewport size. </li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the
     *     above condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param requestParams Map of the HTTP GET request's query parameters - the query box and
     *                      the user viewport width and height.
     * @param response      : Not used by this function. You may ignore.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     * can also be interpreted as the length of the numbers in the image
     * string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */
    @Override
    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
        // System.out.println("yo, wanna know the parameters given by the web browser? They are:");
        // System.out.println(requestParams);

        double ullat = requestParams.get("ullat");
        double ullon = requestParams.get("ullon");
        double lrlat = requestParams.get("lrlat");
        double lrlon = requestParams.get("lrlon");
        double w = requestParams.get("w");
        double h = requestParams.get("h");

        int depth = getDepth(lrlon, ullon, w);

        int xul = getXIndex(ullon, depth);
        int xlr = getXIndex(lrlon, depth);
        int yul = getYIndex(ullat, depth);
        int ylr = getYIndex(lrlat, depth);
        String[][] renderGrid = getRenderGrid(xul, xlr, yul, ylr, depth);

        Point rasterUL = gridUL(xul, yul, depth);
        Point rasterLR = gridLR(xlr, ylr, depth);

        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", rasterUL.getX());
        results.put("raster_ul_lat", rasterUL.getY());
        results.put("raster_lr_lon", rasterLR.getX());
        results.put("raster_lr_lat", rasterLR.getY());
        results.put("depth", depth);
        results.put("query_success", true);

        return results;
    }

    /*********************Helper Functions for processRequest**********************/
    /**
     * Get the greatest depth whose LonDPP is less than or equal to the LonDPP of the query box
     * (as zoomed out as possible). If the requested LonDPP is less than what is available in the data files,
     * use the lowest LonDPP available instead.
     *
     * @param lrlon
     * @param ullon
     * @param width
     * @return
     */
    private int getDepth(double lrlon, double ullon, double width) {
        double reqLonDPP = getLonDPP(lrlon, ullon, width);
        double rootLonDPP = getLonDPP(ROOT_LRLON, ROOT_ULLON, TILE_SIZE);
        return reqLonDPP > rootLonDPP ? 0 : Math.min(DEEPEST_DEPTH,
                (int) (Math.ceil(Math.log(rootLonDPP / reqLonDPP) / Math.log(2)) + 1e-10));
    }

    /**
     * Compute the longitudinal distance per pixel.
     *
     * @param lrlon lower right longitude
     * @param ullon upper left longitude
     * @param width with in pixels
     * @return the longitudinal distance per pixel
     */
    private double getLonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    /**
     * Compute the longitude difference in a grid image
     *
     * @param depth
     * @return
     */
    private double gridLonDiff(double depth) {
        return (ROOT_LRLON - ROOT_ULLON) / Math.pow(2, depth);
    }

    private double gridLatDiff(double depth) {
        return (ROOT_LRLAT - ROOT_ULLAT) / Math.pow(2, depth);
    }

    /**
     * Compute the x index of an image where the given longitude is in.
     *
     * @param longitude
     * @param depth
     * @return
     */
    private int getXIndex(double longitude, int depth) {
        if (longitude < ROOT_ULLON) {
            return 0;
        } else if (longitude > ROOT_LRLON) {
            return (int) Math.pow(2, depth) - 1;
        } else {
            return (int) ((longitude - ROOT_ULLON) / gridLonDiff(depth));
        }
    }

    private int getYIndex(double latitude, int depth) {
        if (latitude > ROOT_ULLAT) {
            return 0;
        } else if (latitude < ROOT_LRLAT) {
            return (int) Math.pow(2, depth) - 1;
        } else {
            return (int) ((latitude - ROOT_ULLAT) / gridLatDiff(depth));
        }
    }

    /**
     * Generate the render grid of image file name
     *
     * @param xul
     * @param xlr
     * @param yul
     * @param ylr
     * @param depth
     * @return
     */
    private String[][] getRenderGrid(int xul, int xlr, int yul, int ylr, int depth) {
        String[][] renderGrid = new String[ylr - yul + 1][xlr - xul + 1];
        for (int y = yul; y <= ylr; y++) {
            for (int x = xul; x <= xlr; x++) {
                renderGrid[y - yul][x - xul] = "d" + depth + "_x" + x + "_y" + y + ".png";
            }
        }
        return renderGrid;
    }

    /**
     * Get the upper left corner point of a grid
     *
     * @param x
     * @param y
     * @param depth
     * @return
     */
    private Point gridUL(int x, int y, int depth) {
        double lon = gridLonDiff(depth) * x + ROOT_ULLON;
        double lat = gridLatDiff(depth) * y + ROOT_ULLAT;
        return new Point(lon, lat);
    }

    private Point gridLR(int x, int y, int depth) {
        double lon = gridLonDiff(depth) * (x + 1) + ROOT_ULLON;
        double lat = gridLatDiff(depth) * (y + 1) + ROOT_ULLAT;
        return new Point(lon, lat);
    }


    /*******************************************************************************/

    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }

    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        results.put("depth", 0);
        results.put("query_success", false);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     *
     * @param rip : Parameters provided by the rasterer
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("Your rastering result is missing the " + p + " field.");
                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success was reported as a failure");
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the images corresponding to rasteredImgParams to the output stream.
     * In Spring 2016, students had to do this on their own, but in 2017,
     * we made this into provided code since it was just a bit too low level.
     */
    private void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                           ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;

        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (int r = 0; r < numVertTiles; r += 1) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                graphic.drawImage(getImage(Constants.IMG_ROOT + renderGrid[r][c]), x, y, null);
                // System.out.println(Constants.IMG_ROOT + renderGrid[r][c]);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it. */
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (route != null && !route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
//                File in = new File(imgPath);
//                tileImg = ImageIO.read(in);
                tileImg = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(imgPath));
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }
}
