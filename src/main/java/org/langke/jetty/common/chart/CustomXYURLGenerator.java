package org.langke.jetty.common.chart;


import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * @version 创建时间：2006-8-24 14:46:54 类说明
 */
public class CustomXYURLGenerator extends StandardXYURLGenerator {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6255242872992246621L;

	/** Prefix to the URL */
    private String prefix;

    /** Series parameter name to go in each URL */
    private String seriesParameterName;

    /** Item parameter name to go in each URL */
    private String itemParameterName;

    private DateFormat dateFormat;

    public CustomXYURLGenerator(String prefix, DateFormat df) {
        this(prefix, DEFAULT_SERIES_PARAMETER, DEFAULT_ITEM_PARAMETER, df);
    }

    /**
     * Constructor that overrides all the defaults
     * 
     * @param prefix the prefix to the URL (<code>null</code> not permitted).
     * @param seriesParameterName the name of the series parameter to go in each
     *            URL (<code>null</code> not permitted).
     * @param itemParameterName the name of the item parameter to go in each URL (<code>null</code>
     *            not permitted).
     */
    public CustomXYURLGenerator(String prefix, String seriesParameterName,
            String itemParameterName, DateFormat df) {
        if (prefix == null) {
            throw new IllegalArgumentException("Null 'prefix' argument.");
        }
        if (seriesParameterName == null) {
            throw new IllegalArgumentException(
                    "Null 'seriesParameterName' argument.");
        }
        if (itemParameterName == null) {
            throw new IllegalArgumentException(
                    "Null 'itemParameterName' argument.");
        }
        this.prefix = prefix;
        this.seriesParameterName = seriesParameterName;
        this.itemParameterName = itemParameterName;

        this.dateFormat = df;
    }

    /**
     * Generates a URL for a particular item within a series.
     * 
     * @param dataset the dataset.
     * @param series the series number (zero-based index).
     * @param item the item number (zero-based index).
     * @return The generated URL.
     */
    public String generateURL(XYDataset dataset, int series, int item) {

        String serieString = (String) dataset.getSeriesKey(series);
        
        String itemValue = this.dateFormat.format(new Date((long) dataset.getXValue(series,
                item)));

        String url = this.prefix;
        boolean firstParameter = url.indexOf("?") == -1;
        url += firstParameter ? "?" : "&amp;";
        /*
         * url += this.seriesParameterName + "=" + series + "&amp;" +
         * this.itemParameterName + "=" + item;
         */
        url += this.seriesParameterName + "=" + serieString + "&amp;"
                + this.itemParameterName + "=" + itemValue;
        return url;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

}

