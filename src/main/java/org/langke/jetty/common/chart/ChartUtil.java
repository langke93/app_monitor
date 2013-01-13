package org.langke.jetty.common.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.OverLIBToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;
import org.langke.jetty.common.Dbop;

/**
 * @version 0.3 创建时间：2006-5-11 22:20:52
 */
public class ChartUtil {

    private static final Logger logger = Logger.getLogger(ChartUtil.class);

    private static final Font DEFAULT_TITLE_FONT = new Font("宋体", Font.PLAIN,
            12);

    private static final String NO_DATA_MESSAGE = "当前图表无数据,请和系统管理员联系";

    private static final Font NO_DATA_MESSAGE_FONT = ChartUtil.DEFAULT_TITLE_FONT;

    private static final Color NO_DATA_MESSAGE_COLOR = Color.RED;

    static class CustomBarRenderer extends BarRenderer {

        private static final long serialVersionUID = 4264630198825046562L;

        public Paint getItemPaint(final int i, final int j) {

            final Paint returnPaint = this.colors[j % this.colors.length];
            return returnPaint;
        }

        private Paint colors[];

        public CustomBarRenderer(final Paint apaint[]) {
            this.colors = apaint;
        }
    }

    static class CustomBarRenderer3D extends BarRenderer3D {

        private static final long serialVersionUID = -7961652368995175375L;

        /** The default x-offset for the 3D effect. */
        public static final double DEFAULT_X_OFFSET = 4D;

        /** The default y-offset for the 3D effect. */
        public static final double DEFAULT_Y_OFFSET = 4D;

        public Paint getItemPaint(final int i, final int j) {

            final Paint returnPaint = this.colors[j % this.colors.length];

            return returnPaint;
        }

        private Paint colors[];

        public CustomBarRenderer3D(final Paint apaint[]) {
            super(CustomBarRenderer3D.DEFAULT_X_OFFSET,
                    CustomBarRenderer3D.DEFAULT_Y_OFFSET);
            this.colors = apaint;
        }
    }

    /**
     * 生成默认的平面图表,图片数据不保存在session中,并默认无标题
     * 
     * @param pieSQL 生成饼图数据的SQL语句
     * @param pw 页面输出流
     * @param showLegend 是否显示图例
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 饼图宽度
     * @param height 饼图高度
     * @return 最终生成图片的文件名
     */
    public static String generatePieChart(final String pieSQL,
            final PrintWriter pw, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generatePieChart(String, PrintWriter, boolean, int, int) - start");
        }

        final String returnString = ChartUtil.generatePieChart(pieSQL, null,
                pw, null, true, false, showLegend, mapURL, isOpenWindow, width,
                height);
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generatePieChart(String, PrintWriter, boolean, int, int) - end");
        }
        return returnString;
    }

    /**
     * 生成标准的平面饼图
     * 
     * @param pieSQL 生成饼图数据的SQL语句
     * @param session 把保存在session中，可以设置为空，当为空是一次性，不会保存在临时目录
     * @param pw 页面输出流
     * @param chartTitle 饼图标题，可是为空
     * @param percentEnable 为真时，饼图的提示文本为100%方式展现
     * @param showPercentValue 当percentEnable为真时，可以设置时显示值，还是显示标题
     * @param showLegend 是否显示图例
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 饼图宽度
     * @param height 饼图高度
     * @return 最终生成图片的文件名
     */
    public static String generatePieChart(final String pieSQL,
            final HttpSession session, final PrintWriter pw,
            final String chartTitle, final boolean percentEnable,
            final boolean showPercentValue, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generatePieChart(String, HttpSession, PrintWriter) - start");
        }

        String filename = null;
        try {
            final PieDataset pieDataset = ChartUtil.createPieDataset(pieSQL);

            // 创建图表对象
            final PiePlot plot = new PiePlot(pieDataset);

            // plot.setSectionPaint(Color.RED); //自定义颜色

            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                plot.setURLGenerator(new StandardPieURLGenerator(mapURL));
            }

            plot.setToolTipGenerator(new StandardPieToolTipGenerator(
                    StandardPieToolTipGenerator.DEFAULT_SECTION_LABEL_FORMAT));
            plot.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            ChartUtil.showNoDataMessage(plot);
            // 设置提示文本以百分比方式显示
            if (percentEnable) {

                // 设置是提示文本是显示值还是显示名称
                if (showPercentValue) {
                    plot
                            .setLabelGenerator(new StandardPieSectionLabelGenerator(
                                    "{1} ({2})"));
                }
                else {
                    plot
                            .setLabelGenerator(new StandardPieSectionLabelGenerator(
                                    "{0} ({2})"));
                }

            }

            final JFreeChart chart = new JFreeChart(chartTitle,
                    JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);

            chart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
            chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            // 设置图表的背景色
            chart.setBackgroundPaint(java.awt.Color.white);

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(chart, width, height,
                    info, null);
            // ChartUtilities.writeImageMap(pw, filename, info, false);
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger.error(
                    "generatePieChart(String, HttpSession, PrintWriter)", e);
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generatePieChart(String, HttpSession, PrintWriter) - end");
        }
        return filename;
    }

    /**
     * 创建PieDataset，通过传入SQL语句
     * 
     * @param sqlString SQL语句
     * @return 饼图数据
     */
    private static PieDataset createPieDataset(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createPieDataset(String) - start");
        }
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Dbop dbop = null;
        final DefaultPieDataset pieDataset = new DefaultPieDataset();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);
            while (rs.next()) {
                pieDataset.setValue(rs.getString(2), rs.getDouble(1));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createPieDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createPieDataset(String) - end");
        }
        return pieDataset;
    }


    /**
     * 创建CategoryDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return 饼图数据
     */
    private static CategoryDataset createCategoryDataset(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        final DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);
            while (rs.next()) {
                categoryDataset.addValue(rs.getDouble(1), rs.getString(2), rs
                        .getString(3));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }
        return categoryDataset;
    }

    /**
     * 创建CategoryDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return 饼图数据
     */
    private static CategoryDataset createCategoryDatasetByTime(
            final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        final DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);
            while (rs.next()) {
                //
                // if (rs.getDouble(3) == 0d) {
                // categoryDataset.addValue(null, rs.getString(1), rs
                // .getString(2));
                // }
                // else {
                categoryDataset.addValue(rs.getDouble(3), rs.getString(1), rs
                        .getString(2));
                // }
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }
        return categoryDataset;
    }

    /**
     * 创建SimpleCategoryDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return CategoryDataset
     */
    private static CategoryDataset createSimpleCategoryDataset(
            final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("createSimpleCategoryDataset(String) - start");
        }

        final DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);
            while (rs.next()) {
                categoryDataset.setValue(rs.getDouble(1), "", rs.getString(2));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createSimpleCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createSimpleCategoryDataset(String) - end");
        }
        return categoryDataset;
    }

    /**
     * 创建平均值列表
     * 
     * @param sqlString 传入的SQL语句
     * @return HashMap
     */
    private static HashMap createAvgList(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("createSimpleCategoryDataset(String) - start");
        }

        final HashMap avgMap = new HashMap();
        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);
            while (rs.next()) {
                avgMap.put(rs.getString(1), rs.getString(2));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createSimpleCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createSimpleCategoryDataset(String) - end");
        }
        return avgMap;
    }

    /**
     * 创建XYCurveDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return 饼图数据
     */
    private static XYDataset createXYCurveDataset(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }
        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        TimeSeries timeSeries = null;
        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);
            while (rs.next()) {
                if (timeseriescollection.getSeries(rs.getString(1)) == null) {
                    timeSeries = new TimeSeries(rs.getString(1),
                            org.jfree.data.time.FixedMillisecond.class);
                    timeseriescollection.addSeries(timeSeries);
                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(1));
                }
                timeSeries.add(new  org.jfree.data.time.FixedMillisecond(rs.getTimestamp(2)), rs.getDouble(3));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }
        return timeseriescollection;
    }
    /**
     * 创建XYDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return 饼图数据
     */
    private static XYDataset createXYDataset(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        TimeSeries timeSeries = null;

        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);

            while (rs.next()) {

                if (timeseriescollection.getSeries(rs.getString(2)) == null) {
                    timeSeries = new TimeSeries(rs.getString(2),
                            org.jfree.data.time.Month.class);
                    timeseriescollection.addSeries(timeSeries);

                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(2));
                }
                timeSeries.add(new Month(rs.getDate(3)), rs.getDouble(4));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }
        return timeseriescollection;
    }

    /**
     * 创建XYDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return 饼图数据
     */
    private static XYDataset createXYDatasetByDay(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        TimeSeries timeSeries = null;

        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);

            while (rs.next()) {

                if (timeseriescollection.getSeries(rs.getString(2)) == null) {
                    timeSeries = new TimeSeries(rs.getString(2), Day.class);
                    timeseriescollection.addSeries(timeSeries);

                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(2));
                }
                timeSeries.add(new Day(rs.getDate(3)), rs.getDouble(4));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }
        return timeseriescollection;
    }

    private static XYDataset createXYDatasetByMonth(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        TimeSeries timeSeries = null;

        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);

            while (rs.next()) {

                if (timeseriescollection.getSeries(rs.getString(2)) == null) {
                    timeSeries = new TimeSeries(rs.getString(2), Month.class);
                    timeseriescollection.addSeries(timeSeries);

                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(2));
                }
                timeSeries.add(new Month(rs.getDate(3)), rs.getDouble(4));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        // timeseriescollection.setDomainIsPointsInTime(true);
        // timeseriescollection.setXPosition(TimePeriodAnchor.MIDDLE);

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }
        return timeseriescollection;
    }

    /**
     * 创建XYDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return 饼图数据
     */
    private static XYDataset createXYDatasetByWeek(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        TimeSeries timeSeries = null;

        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);

            String year;
            String week;

            while (rs.next()) {

                if (timeseriescollection.getSeries(rs.getString(2)) == null) {
                    timeSeries = new TimeSeries(rs.getString(2), Week.class);
                    timeseriescollection.addSeries(timeSeries);
                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(2));
                }

                year = rs.getString(3).substring(0, 4);
                week = rs.getString(3).substring(4, 6);

                timeSeries.add(new Week(Integer.parseInt(week), Integer
                        .parseInt(year)), rs.getDouble(4));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }

        timeseriescollection.setDomainIsPointsInTime(true);
        timeseriescollection.setXPosition(TimePeriodAnchor.MIDDLE);

        return timeseriescollection;
    }

    /**
     * 创建XYDataset
     * 
     * @param sqlString 传入的SQL语句
     * @return 饼图数据
     */
    private static XYDataset createInfoDatasetByWeek(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        TimeSeries timeSeries = null;

        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);

            String year;
            String week;

            while (rs.next()) {

                if (timeseriescollection.getSeries(rs.getString(1)) == null) {
                    timeSeries = new TimeSeries(rs.getString(1), Week.class);
                    timeseriescollection.addSeries(timeSeries);
                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(1));
                }

                year = rs.getString(2).substring(0, 4);
                week = rs.getString(2).substring(4, 6);

                timeSeries.addOrUpdate(new Week(Integer.parseInt(week), Integer
                        .parseInt(year)), rs.getDouble(1));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }

        timeseriescollection.setDomainIsPointsInTime(true);
        timeseriescollection.setXPosition(TimePeriodAnchor.MIDDLE);

        return timeseriescollection;
    }

    private static XYDataset createInfoDatasetByDay(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        TimeSeries timeSeries = null;

        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);

            while (rs.next()) {

                if (timeseriescollection.getSeries(rs.getString(1)) == null) {
                    timeSeries = new TimeSeries(rs.getString(1), Day.class);
                    timeseriescollection.addSeries(timeSeries);
                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(1));
                }
                timeSeries.addOrUpdate(new Day(rs.getDate(2)), rs.getDouble(1));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }

        // timeseriescollection.setDomainIsPointsInTime(true);
        // timeseriescollection.setXPosition(TimePeriodAnchor.MIDDLE);

        return timeseriescollection;
    }

    private static XYDataset createInfoDatasetByMonth(final String sqlString) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - start");
        }

        Dbop dbop = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        TimeSeries timeSeries = null;

        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();

        try {
            dbop = new Dbop();
            conn = dbop.GetConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);

            while (rs.next()) {

                if (timeseriescollection.getSeries(rs.getString(1)) == null) {
                    timeSeries = new TimeSeries(rs.getString(1), Month.class);
                    timeseriescollection.addSeries(timeSeries);
                }
                else {
                    timeSeries = timeseriescollection
                            .getSeries(rs.getString(1));
                }
                timeSeries.addOrUpdate(new Month(rs.getDate(2)), rs
                        .getDouble(1));
            }
        }
        catch (final Exception e) {
            ChartUtil.logger.error("createCategoryDataset(String)", e);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    dbop.CloseConnection(conn);
                }
            }
            catch (final Exception e) {
                // TODO: handle exception
            }
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger.debug("createCategoryDataset(String) - end");
        }

        // timeseriescollection.setDomainIsPointsInTime(true);
        // timeseriescollection.setXPosition(TimePeriodAnchor.START);

        return timeseriescollection;
    }

    private static List createShape() {

        final List shapes = new ArrayList();

        final TimeSeries timeseries = new TimeSeries(
                "L&G European Index Trust", org.jfree.data.time.Month.class);
        timeseries.add(new Month(2, 2001), 181.80000000000001D);
        timeseries.add(new Month(3, 2001), 167.30000000000001D);
        final TimeSeries timeseries1 = new TimeSeries("L&G UK Index Trust",
                org.jfree.data.time.Month.class);
        timeseries1.add(new Month(6, 2002), 108.8D);
        timeseries1.add(new Month(7, 2002), 101.59999999999999D);
        final TimeSeries timeseries2 = new TimeSeries(
                "L&G European Index Trust", org.jfree.data.time.Month.class);
        timeseries2.add(new Month(1, 2001), 181.80000000000001D);
        timeseries2.add(new Month(5, 2001), 167.30000000000001D);
        final TimeSeries timeseries4 = new TimeSeries(
                "L&G European Index Trust", org.jfree.data.time.Month.class);
        timeseries4.add(new Month(8, 2001), 181.80000000000001D);
        timeseries4.add(new Month(9, 2001), 181.80000000000001D);
        final TimeSeries timeseries5 = new TimeSeries(
                "L&G European Index Trust", org.jfree.data.time.Month.class);
        timeseries5.add(new Month(10, 2001), 167.30000000000001D);
        timeseries5.add(new Month(11, 2001), 167.30000000000001D);
        final TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        timeseriescollection.addSeries(timeseries);
        timeseriescollection.addSeries(timeseries1);
        timeseriescollection.addSeries(timeseries2);
        timeseriescollection.addSeries(timeseries4);
        timeseriescollection.addSeries(timeseries5);
        final JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(
                "Legal & General Unit Trust Prices", "Date", "Price Per Unit",
                timeseriescollection, true, true, false);
        jfreechart.setBackgroundPaint(Color.white);
        final XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(true);
        final org.jfree.chart.renderer.xy.XYItemRenderer xyitemrenderer = xyplot
                .getRenderer();
        for (int i = 0; i < xyplot.getSeriesCount(); i++) {
            shapes.add(i, xyitemrenderer.getSeriesShape(i));
        }
        return shapes;
    }

    /**
     * 生成默认的3D图表,图片数据不保存在session中,并默认无标题不透明
     * 
     * @param pieSQL 生成饼图数据的SQL语句
     * @param pw 页面输出流
     * @param showLegend 是否显示图例
     * @param width 饼图宽度
     * @param height 饼图高度
     * @return 最终生成图片的文件名
     */
    public static String generate3DPieChart(final String pieSQL,
            final PrintWriter pw, final boolean showLegend, final int width,
            final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DPieChart(String, PrintWriter, boolean, int, int) - start");
        }

        final String returnString = ChartUtil.generate3DPieChart(pieSQL, null,
                pw, null, true, false, showLegend, null, false, width, height,
                null);
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DPieChart(String, PrintWriter, boolean, int, int) - end");
        }
        return returnString;
    }

    /**
     * 生成默认的3D图表,图片数据不保存在session中,并默认无标题
     * 
     * @param pieSQL 生成饼图数据的SQL语句
     * @param pw 页面输出流
     * @param showLegend 是否显示图例
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 饼图宽度
     * @param height 饼图高度
     * @param alpha 图表透明度
     * @return 最终生成图片的文件名
     */
    public static String generate3DPieChart(final String pieSQL,
            final PrintWriter pw, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final float alpha) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DPieChart(String, PrintWriter, boolean, int, int, float) - start");
        }

        final String returnString = ChartUtil.generate3DPieChart(pieSQL, null,
                pw, null, true, false, showLegend, mapURL, isOpenWindow, width,
                height, new Float(alpha));
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DPieChart(String, PrintWriter, boolean, int, int, float) - end");
        }
        return returnString;
    }

    /**
     * 生成标准的3D饼图
     * 
     * @param pieSQL 生成饼图数据的SQL语句
     * @param session 把图像保存在session中，可以设置为空，当为空是一次性，不会保存在临时目录
     * @param pw 页面输出流
     * @param chartTitle 饼图标题，可是为空
     * @param percentEnable 为真时，饼图的提示文本为100%方式展现
     * @param showPercentValue 当percentEnable为真时，可以设置时显示值，还是显示标题
     * @param showLegend 是否显示图例
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 饼图宽度
     * @param height 饼图高度
     * @param alpha 透明度
     * @return 最终生成图片的文件名
     */
    public static String generate3DPieChart(final String pieSQL,
            final HttpSession session, final PrintWriter pw,
            final String chartTitle, final boolean percentEnable,
            final boolean showPercentValue, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final Float alpha) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DPieChart(String, HttpSession, PrintWriter, String, boolean, boolean, boolean, int, int, Float) - start");
        }
        String filename = null;

        try {

            final PieDataset pieDataset = ChartUtil.createPieDataset(pieSQL);

            // 创建图表对象
            final PiePlot3D plot = new PiePlot3D(pieDataset);

            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                plot.setURLGenerator(new StandardPieURLGenerator(mapURL));
            }
            plot.setToolTipGenerator(new StandardPieToolTipGenerator(
                    StandardPieToolTipGenerator.DEFAULT_SECTION_LABEL_FORMAT));
            plot.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            ChartUtil.showNoDataMessage(plot);

            // 设置图表透明度
            if (alpha != null) {
                plot.setForegroundAlpha(alpha.floatValue());
            }

            // 设置提示文本以百分比方式显示
            if (percentEnable) {

                // 设置是提示文本是显示值还是显示名称
                if (showPercentValue) {
                    plot
                            .setLabelGenerator(new StandardPieSectionLabelGenerator(
                                    "{1} ({2})"));
                }
                else {
                    plot
                            .setLabelGenerator(new StandardPieSectionLabelGenerator(
                                    "{0} ({2})"));
                }

            }

            final JFreeChart chart = new JFreeChart(chartTitle,
                    JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);

            chart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
            chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            // 设置图表的背景色
            chart.setBackgroundPaint(java.awt.Color.white);

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(chart, width, height,
                    info, null);
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger
                    .error(
                            "generate3DPieChart(String, HttpSession, PrintWriter, String, boolean, boolean, boolean, int, int, Float)",
                            e);

        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DPieChart(String, HttpSession, PrintWriter, String, boolean, boolean, boolean, int, int, Float) - end");
        }
        return filename;
    }

    /**
     * 生成标准的线图
     * 
     * @param lineSQL 生成线图数据的SQL语句
     * @param session 把图像保存在session中，可以设置为空，当为空是一次性，不会保存在临时目录
     * @param pw 页面输出流
     * @param chartTitle 图表标题，可是为空
     * @param x_axisName X轴名称
     * @param y_axisName Y轴名称
     * @param showLegend 是否显示图例
     * @param orientation XY轴垂直还是水平标志
     * @param shapesVisible 是否显示节点
     * @param shapesFilled 节点是否颜色填充
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generateLineChart(final String lineSQL,
            final HttpSession session, final PrintWriter pw,
            final String chartTitle, final String x_axisName,
            final String y_axisName, final boolean showLegend,
            final int orientation, final boolean shapesVisible,
            final boolean shapesFilled, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final Double avgValue, final String frameName) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateLineChart(String, HttpSession, PrintWriter, String, String, String, boolean, int, boolean, boolean, int, int) - start");
        }

        String filename = null;
        try {

            final CategoryDataset defaultcategorydataset = ChartUtil
                    .createCategoryDataset(lineSQL);

            JFreeChart jfreechart = null;

            boolean isURL = false;
            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                isURL = true;
            }

            if (orientation == 0) {
                jfreechart = ChartFactory.createLineChart(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.HORIZONTAL, showLegend, true, isURL);
            }
            else {
                jfreechart = ChartFactory.createLineChart(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.VERTICAL, showLegend, true, isURL);
            }

            // 设置图例的字体
            if (showLegend) {
                jfreechart.getLegend()
                        .setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
            }

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
            jfreechart.getRenderingHints().put(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            jfreechart.setBackgroundPaint(Color.white);

            final CategoryPlot categoryplot = (CategoryPlot) jfreechart
                    .getPlot();
            categoryplot.setBackgroundPaint(Color.white);
            categoryplot.setRangeGridlinePaint(Color.lightGray);

            // 增加平均线选项
            if (avgValue != null) {
                final ValueMarker valuemarker = ChartUtil
                        .createValueMarker(avgValue);
                categoryplot.addRangeMarker(valuemarker);
            }

            final NumberAxis numberaxis = (NumberAxis) categoryplot
                    .getRangeAxis();
            numberaxis
                    .setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            numberaxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            numberaxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            final CategoryAxis domainAxis = categoryplot.getDomainAxis();
            domainAxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            domainAxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            final LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
                    .getRenderer();

            lineandshaperenderer.setShapesVisible(shapesVisible);
            lineandshaperenderer.setShapesFilled(shapesFilled);
            lineandshaperenderer.setDrawOutlines(true);
            lineandshaperenderer.setStroke(new BasicStroke(1.5F));

            if (isURL) {
                lineandshaperenderer
                        .setBaseItemURLGenerator(new StandardCategoryURLGenerator(
                                mapURL));
            }

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(jfreechart, width,
                    height, info, null);

            if ((frameName != null) && (frameName.length() > 0)) {
                ChartUtil.writeImageMap(pw, filename, info, false,
                        isOpenWindow, null, frameName);
            }
            else {
                ChartUtil
                        .writeImageMap(pw, filename, info, false, isOpenWindow);
            }
            // ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger
                    .error(
                            "generateLineChart(String, HttpSession, PrintWriter, String, String, String, boolean, int, boolean, boolean, int, int)",
                            e);
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateLineChart(String, HttpSession, PrintWriter, String, String, String, boolean, int, boolean, boolean, int, int) - end");
        }
        return filename;
    }

    public static String generateCombinedLineChart(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String frameName) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateLineChart(String, HttpSession, PrintWriter, String, String, String, boolean, int, boolean, boolean, int, int) - start");
        }

        String filename = null;
        try {

            final CategoryDataset defaultcategorydataset = ChartUtil
                    .createCategoryDataset(statSql);
            final CategoryDataset categorydateset = ChartUtil
                    .createCategoryDataset(sqlStr);

            final JFreeChart jfreechart = null;

            boolean isURL = false;
            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                isURL = true;
            }

            // 设置图例的字体
            if (showLegend) {
                jfreechart.getLegend()
                        .setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
            }

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
            jfreechart.getRenderingHints().put(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            jfreechart.setBackgroundPaint(Color.white);

            final CategoryPlot categoryplot = (CategoryPlot) jfreechart
                    .getPlot();
            categoryplot.setBackgroundPaint(Color.white);
            categoryplot.setRangeGridlinePaint(Color.lightGray);

            final NumberAxis numberaxis = (NumberAxis) categoryplot
                    .getRangeAxis();
            numberaxis
                    .setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            numberaxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            numberaxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            final CategoryAxis domainAxis = categoryplot.getDomainAxis();
            domainAxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            domainAxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            final LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
                    .getRenderer();

            lineandshaperenderer.setShapesVisible(true);
            lineandshaperenderer.setShapesFilled(true);
            lineandshaperenderer.setDrawOutlines(true);
            lineandshaperenderer.setStroke(new BasicStroke(1.5F));

            if (isURL) {
                lineandshaperenderer
                        .setBaseItemURLGenerator(new StandardCategoryURLGenerator(
                                mapURL));
            }

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(jfreechart, width,
                    height, info, null);

            if ((frameName != null) && (frameName.length() > 0)) {
                ChartUtil.writeImageMap(pw, filename, info, false,
                        isOpenWindow, null, frameName);
            }
            else {
                ChartUtil
                        .writeImageMap(pw, filename, info, false, isOpenWindow);
            }
            // ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger
                    .error(
                            "generateLineChart(String, HttpSession, PrintWriter, String, String, String, boolean, int, boolean, boolean, int, int)",
                            e);
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateLineChart(String, HttpSession, PrintWriter, String, String, String, boolean, int, boolean, boolean, int, int) - end");
        }
        return filename;
    }

    /**
     * @param avgValue
     * @return
     */
    private static ValueMarker createValueMarker(final Double avgValue) {
        final ValueMarker valuemarker = new ValueMarker(avgValue.doubleValue());
        valuemarker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        valuemarker.setPaint(Color.GRAY);
        valuemarker.setStroke(new BasicStroke(0.5F, 1, 1, 0.5F, new float[] {
                10F, 6F }, 0.0F));
        // valuemarker.setLabel(""+avgValue);
        valuemarker.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
        valuemarker.setLabelPaint(Color.BLACK);
        valuemarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        valuemarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
        return valuemarker;
    }

    /**曲线图*
     * 
     * @param statSql select app_name,add_time,val from t_api_monitor where type='thread'
     * @param session
     * @param pw
     * @param title 图表标题
     * @param timeAxisLabel 时间（X）轴标签
     * @param valueAxisLabel 值（Y）轴标签
     * @param showLegend 是否显示图例
     * @param mapURL 
     * @param isOpenWindow
     * @param width
     * @param height
     * @param frameName
     * @return
     * @throws Exception
     */
    public static String generateXYCurveLineChart(final String statSql, 
            final PrintWriter pw,String title,String timeAxisLabel,String valueAxisLabel, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final String frameName)
            throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }
        String filename = "";
        final XYDataset dataset = ChartUtil.createXYCurveDataset(statSql);

        final JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, timeAxisLabel,valueAxisLabel, dataset, showLegend, true, isURL);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }
        jfreechart.setBackgroundPaint(Color.WHITE);
        final XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setBackgroundPaint(Color.WHITE);
        xyplot.setDomainGridlinePaint(Color.gray);
        xyplot.setRangeGridlinePaint(Color.gray);

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        final NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot.getRenderer();
        if (isURL) {
            xylineandshaperenderer.setURLGenerator(new CustomXYURLGenerator(
                    mapURL, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
        }
        final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,info, null);
        if ((frameName != null) && (frameName.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow,
                    null, frameName);
        }else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();
        return filename;
    }
    
    
    public static String generateXYLineChart(final String statSql,
            final String avgSql, final HttpSession session,
            final PrintWriter pw, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final Double avgValue, final String frameName)
            throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        String filename = "";
        // "SELECT p.bureau_id, (SELECT NAME FROM dw.bureau WHERE bureau_id =
        // p.bureau_id) bureau_name, to_date(s.mon, 'yyyymm'), round(SUM(charge)
        // / 10000, 2) charge FROM place.lead_stat_income_mon s, dw.place_tree p
        // WHERE s.sub_bureau = p.node AND p.state = 'A' AND p.place_type = '3'
        // and p.bureau_id = 1 GROUP BY p.bureau_id, s.mon"
        final XYDataset dataset = ChartUtil.createXYDataset(statSql);

        final JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(null,
                null, null, dataset, true, true, false);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        jfreechart.setBackgroundPaint(Color.WHITE);
        final XYPlot xyplot = (XYPlot) jfreechart.getPlot();

        xyplot.setBackgroundPaint(Color.WHITE);
        xyplot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        Double avg = null;

        // 增加平均值 "SELECT (SELECT NAME FROM dw.bureau WHERE bureau_id =
        // p.bureau_id) bureau_name, round(SUM(charge/10000) / count(distinct
        // s.mon), 2) FROM place.lead_stat_income_mon s, dw.place_tree p WHERE
        // s.sub_bureau = p.node AND p.state = 'A' AND p.place_type = '3' and
        // p.bureau_id = 1 GROUP BY p.bureau_id"
        final HashMap map = ChartUtil.createAvgList(avgSql);
        // 增加异常标识
        final MyDrawer circledrawer = new MyDrawer(Color.BLUE, new BasicStroke(
                1.0F), null);
        final MyDrawer circledrawer1 = new MyDrawer(Color.RED, new BasicStroke(
                1.0F), null);

        XYDrawableAnnotation xydrawableannotation = null;

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            avg = Double.valueOf((String) map.get(dataset.getSeriesKey(i)));
            final ValueMarker valuemarker = ChartUtil.createValueMarker(avg);
            xyplot.addRangeMarker(valuemarker);

            for (int j = 0; j < dataset.getItemCount(i); j++) {
                if (Math.abs((dataset.getYValue(i, j) - avg.doubleValue())
                        / dataset.getYValue(i, j)) >= 0.3D) {

                    if ((dataset.getYValue(i, j) - avg.doubleValue()) > 0) {
                        xydrawableannotation = new XYDrawableAnnotation(dataset
                                .getXValue(i, j), dataset.getYValue(i, j), 10D,
                                10D, circledrawer);
                    }
                    else {
                        xydrawableannotation = new XYDrawableAnnotation(dataset
                                .getXValue(i, j), dataset.getYValue(i, j), 10D,
                                10D, circledrawer1);
                    }
                    xyplot.addAnnotation(xydrawableannotation);
                }
            }
        }

        final XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot
                .getRenderer();
        xylineandshaperenderer.setShapesVisible(true);
        xylineandshaperenderer.setShapesFilled(true);

        xylineandshaperenderer
                .setToolTipGenerator(new StandardXYToolTipGenerator(
                        StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                        new SimpleDateFormat("yyyyMM"), NumberFormat
                                .getInstance()));

        final NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
        // numberaxis.setLabelFont(DEFAULT_TITLE_FONT);
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        final DateAxis dateAxis = (DateAxis) xyplot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyyMM"));

        if (isURL) {
            xylineandshaperenderer.setURLGenerator(new CustomXYURLGenerator(
                    mapURL, new SimpleDateFormat("yyyyMM")));
        }

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,
                info, null);

        if ((frameName != null) && (frameName.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow,
                    null, frameName);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

    public static String generateXYLineChartByDay(final String statSql,
            final String avgSql, final HttpSession session,
            final PrintWriter pw, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final Double avgValue, final String frameName)
            throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        String filename = "";
        // "SELECT p.bureau_id, (SELECT NAME FROM dw.bureau WHERE bureau_id =
        // p.bureau_id) bureau_name, to_date(s.DAY, 'yyyymmdd'),
        // round(SUM(charge) / 10000, 2) charge FROM
        // place.lead_stat_income_daily s, dw.place_tree p WHERE s.sub_bureau =
        // p.node AND p.state = 'A' AND p.place_type = '3' GROUP BY p.bureau_id,
        // s.DAY"
        final XYDataset dataset = ChartUtil.createXYDatasetByDay(statSql);

        final JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(null,
                null, null, dataset, true, true, false);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        jfreechart.setBackgroundPaint(Color.WHITE);
        final XYPlot xyplot = (XYPlot) jfreechart.getPlot();

        xyplot.setBackgroundPaint(Color.WHITE);
        xyplot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美

        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        Double avg = null;

        // 增加平均值"SELECT (SELECT NAME FROM dw.bureau WHERE bureau_id =
        // p.bureau_id) bureau_name, round(SUM(charge/10000) / count(distinct
        // s.DAY), 2) FROM place.lead_stat_income_daily s, dw.place_tree p WHERE
        // s.sub_bureau = p.node AND p.state = 'A' AND p.place_type = '3' GROUP
        // BY p.bureau_id"

        final HashMap map = ChartUtil.createAvgList(avgSql);
        // 增加异常标识
        final MyDrawer circledrawer = new MyDrawer(Color.BLUE, new BasicStroke(
                1.0F), null);
        final MyDrawer circledrawer1 = new MyDrawer(Color.RED, new BasicStroke(
                1.0F), null);

        XYDrawableAnnotation xydrawableannotation = null;

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            avg = Double.valueOf((String) map.get(dataset.getSeriesKey(i)));
            final ValueMarker valuemarker = ChartUtil.createValueMarker(avg);
            xyplot.addRangeMarker(valuemarker);

            for (int j = 0; j < dataset.getItemCount(i); j++) {
                if (Math.abs((dataset.getYValue(i, j) - avg.doubleValue())
                        / dataset.getYValue(i, j)) >= 0.3D) {

                    if ((dataset.getYValue(i, j) - avg.doubleValue()) > 0) {
                        xydrawableannotation = new XYDrawableAnnotation(dataset
                                .getXValue(i, j), dataset.getYValue(i, j), 10D,
                                10D, circledrawer);
                    }
                    else {
                        xydrawableannotation = new XYDrawableAnnotation(dataset
                                .getXValue(i, j), dataset.getYValue(i, j), 10D,
                                10D, circledrawer1);
                    }
                    xyplot.addAnnotation(xydrawableannotation);
                }
            }
        }

        final XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot
                .getRenderer();
        xylineandshaperenderer.setShapesVisible(true);
        xylineandshaperenderer.setShapesFilled(true);

        xylineandshaperenderer
                .setToolTipGenerator(new StandardXYToolTipGenerator(
                        StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                        new SimpleDateFormat("yyyyMMdd"), NumberFormat
                                .getInstance()));

        final NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
        // numberaxis.setLabelFont(DEFAULT_TITLE_FONT);
        // numberaxis.setTickLabelFont(DEFAULT_TITLE_FONT);
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final DateAxis dateAxis = (DateAxis) xyplot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyyMMdd"));
        // dateAxis.setLabelFont(DEFAULT_TITLE_FONT);
        // dateAxis.setTickLabelFont(DEFAULT_TITLE_FONT);
        if (isURL) {
            xylineandshaperenderer.setURLGenerator(new CustomXYURLGenerator(
                    mapURL, new SimpleDateFormat("yyyyMMdd")));
        }

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,
                info, null);

        if ((frameName != null) && (frameName.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow,
                    null, frameName);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

    public static String generateXYLineChartByWeek(final String statSql,
            final String avgSql, final HttpSession session,
            final PrintWriter pw, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final Double avgValue, final String frameName)
            throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        String filename = "";

        /**
         * generateXYLineChartByWeek(String, String, HttpSession, PrintWriter,
         * boolean, String, boolean, int, int, Double, String)
         */

        final XYDataset dataset = ChartUtil.createXYDatasetByWeek(statSql);

        final JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(null,
                null, null, dataset, true, true, false);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        jfreechart.setBackgroundPaint(Color.WHITE);
        final XYPlot xyplot = (XYPlot) jfreechart.getPlot();

        xyplot.setBackgroundPaint(Color.WHITE);
        xyplot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        Double avg = null;
        // 增加平均值
        final HashMap map = ChartUtil.createAvgList(avgSql);
        // 增加异常标识
        final MyDrawer circledrawer = new MyDrawer(Color.BLUE, new BasicStroke(
                1.0F), null);
        final MyDrawer circledrawer1 = new MyDrawer(Color.RED, new BasicStroke(
                1.0F), null);

        final XYDrawableAnnotation xydrawableannotation = null;

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            avg = Double.valueOf((String) map.get(dataset.getSeriesKey(i)));
            final ValueMarker valuemarker = ChartUtil.createValueMarker(avg);
            xyplot.addRangeMarker(valuemarker);

            // for (int j = 0; j < dataset.getItemCount(i); j++) {
            // if (Math.abs((dataset.getYValue(i, j) - avg.doubleValue())
            // / dataset.getYValue(i, j)) >= 0.3D) {
            //
            // if ((dataset.getYValue(i, j) - avg.doubleValue()) > 0) {
            // xydrawableannotation = new XYDrawableAnnotation(dataset
            // .getXValue(i, j), dataset.getYValue(i, j), 10D,
            // 10D, circledrawer);
            // }
            // else {
            // xydrawableannotation = new XYDrawableAnnotation(dataset
            // .getXValue(i, j), dataset.getYValue(i, j), 10D,
            // 10D, circledrawer1);
            // }
            // xyplot.addAnnotation(xydrawableannotation);
            // }
            // }
        }

        final XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot
                .getRenderer();
        xylineandshaperenderer.setShapesVisible(true);
        xylineandshaperenderer.setShapesFilled(true);
        final Shape shape = xylineandshaperenderer.getSeriesShape(0);

        for (int i = 0; i < xyplot.getSeriesCount(); i++) {
            // xylineandshaperenderer.setSeriesShape(i, shape);
            System.out.println(xylineandshaperenderer.getSeriesShape(i));
        }

        xylineandshaperenderer
                .setToolTipGenerator(new StandardXYToolTipGenerator(
                        StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                        new SimpleDateFormat("yyyyw"), NumberFormat
                                .getInstance()));

        final PeriodAxis periodaxis = new PeriodAxis(null);
        periodaxis.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
        periodaxis.setAutoRangeTimePeriodClass(org.jfree.data.time.Week.class);
        // periodaxis.setPositiveArrowVisible(false);
        final PeriodAxisLabelInfo aperiodaxislabelinfo[] = new PeriodAxisLabelInfo[1];

        // aperiodaxislabelinfo[0] = new
        // PeriodAxisLabelInfo(org.jfree.data.time.Day.class, new
        // SimpleDateFormat("d"));
        aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(
                org.jfree.data.time.Week.class, new SimpleDateFormat("yyyyw"));
        // aperiodaxislabelinfo[1] = new
        // PeriodAxisLabelInfo(org.jfree.data.time.Year.class, new
        // SimpleDateFormat("yyyy"));
        periodaxis.setLabelInfo(aperiodaxislabelinfo);
        xyplot.setDomainAxis(periodaxis);
        // xyplot.getRangeAxis().setVisible(false);
        // xyplot.getDomainAxis().setVisible(false);

        /*
         * NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis(); //
         * numberaxis.setLabelFont(DEFAULT_TITLE_FONT); //
         * numberaxis.setTickLabelFont(DEFAULT_TITLE_FONT);
         * numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         */

        // DateAxis dateAxis = (DateAxis) xyplot.getDomainAxis();
        // dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyyw"));
        // dateAxis.calculateLowestVisibleTickValue()
        // DateTickUnit dateTickUnit = new DateTickUnit(DateTickUnit.DAY, 7);
        // dateAxis.setTickUnit(dateTickUnit);
        if (isURL) {
            xylineandshaperenderer.setURLGenerator(new CustomXYURLGenerator(
                    mapURL, new SimpleDateFormat("yyyyw")));
        }

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,
                info, null);

        if ((frameName != null) && (frameName.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow,
                    null, frameName);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

    /**
     * @param statSql
     * @param avgSql
     * @param sqlStr
     * @param session
     * @param pw
     * @param showLegend
     * @param mapURL
     * @param isOpenWindow
     * @param width
     * @param height
     * @param divString
     * @param frameName
     * @return
     * @throws Exception
     */
    public static String generateInfoChartByWeek(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName, Double lable)
            throws Exception {

        /*
         * boolean isURL = false; // 创建图表的MAP热点链接 if ((mapURL != null) &&
         * (mapURL.trim().length() > 0)) { isURL = true; } String filename = "";
         * final XYDataset dataset1 = ChartUtil.createXYDatasetByWeek(statSql);
         * final XYDataset dataset = ChartUtil.createInfoDatasetByWeek(sqlStr);
         * final XYLineAndShapeRenderer standardxyitemrenderer = new
         * XYLineAndShapeRenderer();
         * standardxyitemrenderer.setShapesFilled(true);
         * standardxyitemrenderer.setShapesVisible(true);
         * standardxyitemrenderer.setBaseLinesVisible(false); final Paint[]
         * paints = ChartUtil.createMonthPaint(); final List shapes =
         * ChartUtil.createShape(); for (int i = 0; i <
         * dataset.getSeriesCount(); i++) {
         * standardxyitemrenderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
         * standardxyitemrenderer.setSeriesShape(i, (Shape) shapes.get(i));
         * standardxyitemrenderer.setSeriesPaint(i, paints[i + 4]); } final
         * NumberAxis numberaxis = new NumberAxis("Range 1");
         * numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         * numberaxis.setRange(0d, 4.5d); final XYPlot xyplot = new
         * XYPlot(dataset, null, numberaxis, standardxyitemrenderer);
         * xyplot.setDomainGridlinesVisible(false);
         * xyplot.setRangeGridlinesVisible(false);
         * xyplot.getRangeAxis().setVisible(false); final XYLineAndShapeRenderer
         * standardxyitemrenderer1 = new XYLineAndShapeRenderer();
         * standardxyitemrenderer.setShapesFilled(true);
         * standardxyitemrenderer.setShapesVisible(true); final NumberAxis
         * numberaxis1 = new NumberAxis(null);
         * numberaxis1.setAutoRangeIncludesZero(false); final XYPlot xyplot1 =
         * new XYPlot(dataset1, null, numberaxis1, standardxyitemrenderer1);
         * xyplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
         * xyplot1.setDomainGridlinesVisible(true); int j = 0; for (int i = 0; i <
         * xyplot1.getSeriesCount(); i++) {
         * standardxyitemrenderer1.setSeriesShape(i, new Rectangle(-2, -2, 5,
         * 5)); if (i % 11 == 0) { j = 0; }
         * standardxyitemrenderer1.setSeriesPaint(i, paints[j++]); } final
         * PeriodAxis periodaxis = new PeriodAxis(null); //
         * periodaxis.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
         * periodaxis.setTimeZone(TimeZone.getDefault());
         * periodaxis.setPositiveArrowVisible(false); final PeriodAxisLabelInfo
         * aperiodaxislabelinfo[] = new PeriodAxisLabelInfo[1];
         * aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(
         * org.jfree.data.time.Week.class, new SimpleDateFormat("yyyyw"));
         * periodaxis.setLabelInfo(aperiodaxislabelinfo);
         * periodaxis.setAutoRangeTimePeriodClass(Week.class); //
         * periodaxis.setAxisLineVisible(false); final CombinedDomainXYPlot
         * combineddomainxyplot = new CombinedDomainXYPlot( periodaxis);
         * combineddomainxyplot.setGap(0D); combineddomainxyplot.add(xyplot, 1);
         * combineddomainxyplot.add(xyplot1, 6);
         * combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL); final
         * JFreeChart jfreechart = new JFreeChart(null,
         * JFreeChart.DEFAULT_TITLE_FONT, combineddomainxyplot, true);
         * jfreechart.setBackgroundPaint(Color.WHITE); // 设置图例的字体 if
         * (showLegend) {
         * jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT); } //
         * 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美 jfreechart.getRenderingHints().put(
         * RenderingHints.KEY_TEXT_ANTIALIASING,
         * RenderingHints.VALUE_TEXT_ANTIALIAS_OFF); // 增加平均值 Double avg = null;
         * final HashMap map = ChartUtil.createAvgList(avgSql); for (int i = 0;
         * i < dataset1.getSeriesCount(); i++) { avg = Double.valueOf((String)
         * map.get(dataset1.getSeriesKey(i))); final ValueMarker valuemarker =
         * ChartUtil.createValueMarker(avg);
         * xyplot1.addRangeMarker(valuemarker); } standardxyitemrenderer1
         * .setToolTipGenerator(new StandardXYToolTipGenerator(
         * StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new
         * SimpleDateFormat("yyyyw"), NumberFormat .getInstance())); if (isURL) {
         * standardxyitemrenderer1.setURLGenerator(new CustomXYURLGenerator(
         * mapURL, new SimpleDateFormat("yyyyw"))); }
         * standardxyitemrenderer.setURLGenerator(new CustomXYURLGenerator(
         * "mycustfreechart.jsp", new SimpleDateFormat("yyyyw"))); final
         * ChartRenderingInfo info = new ChartRenderingInfo( new
         * StandardEntityCollection()); // 把图片写入临时目录并输出 filename =
         * ServletUtilities.saveChartAsPNG(jfreechart, width, height, info,
         * null); if ((divString != null) && (divString.length() > 0)) {
         * ChartUtil.writeImageMap(pw, filename, info, false, frameName,
         * divString); } else { ChartUtil.writeImageMap(pw, filename, info,
         * false, isOpenWindow); } pw.flush(); return filename;
         */
        return generateInfoChart(statSql, avgSql, sqlStr, session, pw,
                showLegend, mapURL, isOpenWindow, width, height, divString,
                frameName, lable, true);
    }

    public static String generateInfoChartByWeek1(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName) throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        String filename = "";

        final XYDataset dataset1 = ChartUtil.createXYDatasetByWeek(statSql);
        final XYDataset dataset = ChartUtil.createInfoDatasetByWeek(sqlStr);

        final XYLineAndShapeRenderer standardxyitemrenderer = new XYLineAndShapeRenderer();
        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);
        standardxyitemrenderer.setBaseLinesVisible(false);
        final Paint[] paints = ChartUtil.createMonthPaint();

        final List shapes = ChartUtil.createShape();

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            standardxyitemrenderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
            standardxyitemrenderer.setSeriesShape(i, (Shape) shapes.get(i));
            standardxyitemrenderer.setSeriesPaint(i, paints[i + 4]);
        }

        final NumberAxis numberaxis = new NumberAxis("Range 1");
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setRange(0d, 4.5d);
        final XYPlot xyplot = new XYPlot(dataset, null, numberaxis,
                standardxyitemrenderer);
        xyplot.setDomainGridlinesVisible(false);
        xyplot.setRangeGridlinesVisible(false);

        xyplot.getRangeAxis().setVisible(false);

        final XYLineAndShapeRenderer standardxyitemrenderer1 = new XYLineAndShapeRenderer();

        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);
        final NumberAxis numberaxis1 = new NumberAxis(null);
        numberaxis1.setAutoRangeIncludesZero(false);

        final XYPlot xyplot1 = new XYPlot(dataset1, null, numberaxis1,
                standardxyitemrenderer1);
        xyplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
        xyplot1.setDomainGridlinesVisible(true);

        int j = 0;
        for (int i = 0; i < xyplot1.getSeriesCount(); i++) {
            standardxyitemrenderer1.setSeriesShape(i, new Rectangle(-2, -2, 5,
                    5));
            if (i % 11 == 0) {
                j = 0;
            }
            standardxyitemrenderer1.setSeriesPaint(i, paints[j++]);
        }

        final DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyyMMdd"));
        // dateAxis.setTickUnit();
        final TickUnits standardUnits = new TickUnits();
        standardUnits.add(new DateTickUnit(DateTickUnit.DAY, 1,
                new SimpleDateFormat("MMM dd ''yy")));
        standardUnits.add(new DateTickUnit(DateTickUnit.DAY, 7,
                new SimpleDateFormat("MMM dd ''yy")));
        standardUnits.add(new DateTickUnit(DateTickUnit.MONTH, 1,
                new SimpleDateFormat("MMM ''yy")));
        dateAxis.setStandardTickUnits(standardUnits);

        final CombinedDomainXYPlot combineddomainxyplot = new CombinedDomainXYPlot(
                dateAxis);
        combineddomainxyplot.setGap(0D);
        combineddomainxyplot.add(xyplot, 1);
        combineddomainxyplot.add(xyplot1, 6);
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);

        final JFreeChart jfreechart = new JFreeChart(null,
                JFreeChart.DEFAULT_TITLE_FONT, combineddomainxyplot, true);
        jfreechart.setBackgroundPaint(Color.WHITE);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // 增加平均值
        Double avg = null;
        final HashMap map = ChartUtil.createAvgList(avgSql);
        for (int i = 0; i < dataset1.getSeriesCount(); i++) {
            avg = Double.valueOf((String) map.get(dataset1.getSeriesKey(i)));
            final ValueMarker valuemarker = ChartUtil.createValueMarker(avg);
            xyplot1.addRangeMarker(valuemarker);
        }

        standardxyitemrenderer1
                .setToolTipGenerator(new StandardXYToolTipGenerator(
                        StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                        new SimpleDateFormat("yyyyw"), NumberFormat
                                .getInstance()));

        if (isURL) {
            standardxyitemrenderer1.setURLGenerator(new CustomXYURLGenerator(
                    mapURL, new SimpleDateFormat("yyyyw")));
        }

        standardxyitemrenderer.setURLGenerator(new CustomXYURLGenerator(
                "mycustfreechart.jsp", new SimpleDateFormat("yyyyw")));

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,
                info, null);

        if ((divString != null) && (divString.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, frameName,
                    divString);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

    public static String generateInfoChart(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName,
            Double labelPosition, boolean combined) throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        String filename = "";

        final CategoryDataset dataset1 = ChartUtil
                .createCategoryDatasetByTime(statSql);
        final CategoryDataset dataset = ChartUtil
                .createCategoryDatasetByTime(sqlStr);

        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();

        // List rowKeys = dataset.getRowKeys();
        List columnKeys = dataset1.getColumnKeys();

        // int rowCount = rowKeys.size();
        int columnCount = columnKeys.size();

        String rowKey = null;
        String columnKey = null;

        Number valueString = null;

        for (int i = 1; i <= 5; i++) {
            for (int j = 0; j < columnCount; j++) {

                rowKey = String.valueOf(i);
                columnKey = (String) columnKeys.get(j);

                try {
                    valueString = dataset.getValue(rowKey, columnKey);
                }
                catch (Exception e) {
                    valueString = null;
                }
                categoryDataset.addValue(valueString, rowKey, columnKey);
            }
        }

        // for (int i = 0; i < categoryDataset.getRowCount(); i++) {
        // for (int j = 0; j < categoryDataset.getColumnCount(); j++) {
        // System.out.println(categoryDataset.getRowKey(i) + " "
        // + categoryDataset.getColumnKey(j) + " "
        // + categoryDataset.getValue(i, j));
        // }
        // }
        //
        // for (int i = 0; i < dataset1.getRowCount(); i++) {
        // for (int j = 0; j < dataset1.getColumnCount(); j++) {
        // System.out.println(dataset1.getRowKey(i) + " "
        // + dataset1.getColumnKey(j) + " "
        // + dataset1.getValue(i, j));
        // }
        // }

        final LineAndShapeRenderer standardxyitemrenderer = new LineAndShapeRenderer();
        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);
        standardxyitemrenderer.setBaseLinesVisible(false);

        final Paint[] paints = ChartUtil.createMonthPaint();

        final List shapes = ChartUtil.createShape();

        for (int i = 0; i < categoryDataset.getRowCount(); i++) {
            standardxyitemrenderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
            standardxyitemrenderer.setSeriesShape(i, (Shape) shapes.get(i));
            standardxyitemrenderer.setSeriesPaint(i, paints[i + 4]);
        }

        final NumberAxis numberaxis = new NumberAxis("Range 1");
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setRange(0d, 6.5d);
        final CategoryPlot xyplot = new CategoryPlot(categoryDataset, null,
                numberaxis, standardxyitemrenderer);

        xyplot.getRangeAxis().setVisible(false);
        xyplot.setDomainGridlinesVisible(true);
        xyplot.setRangeGridlinesVisible(false);
        xyplot.setBackgroundPaint(new Color(237, 251, 240));

        final LineAndShapeRenderer standardxyitemrenderer1 = new LineAndShapeRenderer();

        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);

        final NumberAxis numberaxis1 = new NumberAxis(null);
        numberaxis1.setNumberFormatOverride(DecimalFormat.getInstance());
        numberaxis1.setAutoRangeIncludesZero(false);
        // CategoryAxis domainAxis = new CategoryAxis();

        final CategoryPlot xyplot1 = new CategoryPlot(dataset1, null,
                numberaxis1, standardxyitemrenderer1);
        xyplot1.setDomainGridlinesVisible(true);
        standardxyitemrenderer1
                .setToolTipGenerator(new StandardCategoryToolTipGenerator());
        // xyplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        int j = 0;
        for (int i = 0; i < dataset1.getRowCount(); i++) {
            standardxyitemrenderer1.setSeriesShape(i, new Rectangle(-2, -2, 5,
                    5));
            if (i % 11 == 0) {
                j = 0;
            }
            standardxyitemrenderer1.setSeriesPaint(i, paints[j++]);
        }

        final CombinedDomainCategoryPlot combineddomainxyplot = new CombinedDomainCategoryPlot();
        combineddomainxyplot.setGap(0D);
        if (combined) {
            combineddomainxyplot.add(xyplot, 1);
        }

        combineddomainxyplot.add(xyplot1, 6);
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);

        // 设置标题倾斜度，用来解决标题过长变成...的问题，但是字体变得相对难看
        if (labelPosition != null) {
            combineddomainxyplot.getDomainAxis().setCategoryLabelPositions(
                    CategoryLabelPositions
                            .createDownRotationLabelPositions(labelPosition
                                    .doubleValue()));
        }

        final JFreeChart jfreechart = new JFreeChart(null,
                JFreeChart.DEFAULT_TITLE_FONT, combineddomainxyplot, showLegend);
        jfreechart.setBackgroundPaint(Color.WHITE);

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        // 增加平均值
        Double avg = null;
        final HashMap map = ChartUtil.createAvgList(avgSql);
        for (int i = 0; i < dataset1.getRowCount(); i++) {
            avg = Double.valueOf((String) map.get(dataset1.getRowKey(i)));
            final ValueMarker valuemarker = ChartUtil.createValueMarker(avg);
            xyplot1.addRangeMarker(valuemarker);
        }

        if (isURL) {

            standardxyitemrenderer1
                    .setBaseItemURLGenerator(new StandardCategoryURLGenerator(
                            "mycustfreechart1.jsp"));

        }

        standardxyitemrenderer
                .setBaseItemURLGenerator(new StandardCategoryURLGenerator(
                        "mycustfreechart1.jsp"));

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,
                info, null);

        if ((divString != null) && (divString.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, frameName,
                    divString);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

    public static String generateInfoChartByDay(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName, Double lable)
            throws Exception {

        /*
         * boolean isURL = false; // 创建图表的MAP热点链接 if ((mapURL != null) &&
         * (mapURL.trim().length() > 0)) { isURL = true; } String filename = "";
         * final XYDataset dataset1 = ChartUtil.createXYDatasetByDay(statSql);
         * final XYDataset dataset = ChartUtil.createInfoDatasetByDay(sqlStr);
         * final XYLineAndShapeRenderer standardxyitemrenderer = new
         * XYLineAndShapeRenderer();
         * standardxyitemrenderer.setShapesFilled(true);
         * standardxyitemrenderer.setShapesVisible(true);
         * standardxyitemrenderer.setBaseLinesVisible(false); final Paint[]
         * paints = ChartUtil.createMonthPaint(); final List shapes =
         * ChartUtil.createShape(); for (int i = 0; i <
         * dataset.getSeriesCount(); i++) {
         * standardxyitemrenderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
         * standardxyitemrenderer.setSeriesShape(i, (Shape) shapes.get(i));
         * standardxyitemrenderer.setSeriesPaint(i, paints[i + 4]); } final
         * NumberAxis numberaxis = new NumberAxis("Range 1");
         * numberaxis.setRange(0d, 4.5d); final XYPlot xyplot = new
         * XYPlot(dataset, null, numberaxis, standardxyitemrenderer);
         * xyplot.getRangeAxis().setVisible(false); final XYLineAndShapeRenderer
         * standardxyitemrenderer1 = new XYLineAndShapeRenderer();
         * standardxyitemrenderer.setShapesFilled(true);
         * standardxyitemrenderer.setShapesVisible(true); final NumberAxis
         * numberaxis1 = new NumberAxis(null);
         * numberaxis1.setAutoRangeIncludesZero(false); // DateAxis dateAxis1 =
         * new DateAxis(); final XYPlot xyplot1 = new XYPlot(dataset1, null,
         * numberaxis1, standardxyitemrenderer1);
         * xyplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT); int j = 0;
         * for (int i = 0; i < xyplot1.getSeriesCount(); i++) {
         * standardxyitemrenderer1.setSeriesShape(i, new Rectangle(-2, -2, 5,
         * 5)); if (i % 11 == 0) { j = 0; }
         * standardxyitemrenderer1.setSeriesPaint(i, paints[j++]); } final
         * DateAxis dateAxis = new DateAxis();
         * dateAxis.setDateFormatOverride(new SimpleDateFormat("MMdd")); final
         * CombinedDomainXYPlot combineddomainxyplot = new CombinedDomainXYPlot(
         * dateAxis); combineddomainxyplot.setGap(0D);
         * combineddomainxyplot.add(xyplot, 1);
         * combineddomainxyplot.add(xyplot1, 6);
         * combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL); final
         * JFreeChart jfreechart = new JFreeChart(null,
         * JFreeChart.DEFAULT_TITLE_FONT, combineddomainxyplot, true);
         * jfreechart.setBackgroundPaint(Color.WHITE); // 设置图例的字体 if
         * (showLegend) {
         * jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT); } //
         * 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美 jfreechart.getRenderingHints().put(
         * RenderingHints.KEY_TEXT_ANTIALIASING,
         * RenderingHints.VALUE_TEXT_ANTIALIAS_OFF); // 增加平均值 Double avg = null;
         * final HashMap map = ChartUtil.createAvgList(avgSql); for (int i = 0;
         * i < dataset1.getSeriesCount(); i++) { avg = Double.valueOf((String)
         * map.get(dataset1.getSeriesKey(i))); final ValueMarker valuemarker =
         * ChartUtil.createValueMarker(avg);
         * xyplot1.addRangeMarker(valuemarker); } standardxyitemrenderer1
         * .setToolTipGenerator(new StandardXYToolTipGenerator(
         * StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new
         * SimpleDateFormat("yyyyMMdd"), NumberFormat .getInstance())); if
         * (isURL) { standardxyitemrenderer1.setURLGenerator(new
         * CustomXYURLGenerator( mapURL, new SimpleDateFormat("yyyyMMdd"))); }
         * standardxyitemrenderer.setURLGenerator(new CustomXYURLGenerator(
         * "mycustfreechart.jsp", new SimpleDateFormat("yyyyMMdd"))); final
         * ChartRenderingInfo info = new ChartRenderingInfo( new
         * StandardEntityCollection()); // 把图片写入临时目录并输出 filename =
         * ServletUtilities.saveChartAsPNG(jfreechart, width, height, info,
         * null); if ((divString != null) && (divString.length() > 0)) {
         * ChartUtil.writeImageMap(pw, filename, info, false, frameName,
         * divString); } else { ChartUtil.writeImageMap(pw, filename, info,
         * false, isOpenWindow); } pw.flush(); return filename;
         */
        return generateInfoChart(statSql, avgSql, sqlStr, session, pw,
                showLegend, mapURL, isOpenWindow, width, height, divString,
                frameName, lable, true);
    }

    /**
     * @param statSql
     * @param avgSql
     * @param sqlStr
     * @param session
     * @param pw
     * @param showLegend
     * @param mapURL
     * @param isOpenWindow
     * @param width
     * @param height
     * @param divString
     * @param frameName
     * @return
     * @throws Exception
     * @deprecated
     */
    public static String generateInfoChartByMonth1(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName) throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        String filename = "";

        final XYDataset dataset1 = ChartUtil.createXYDatasetByMonth(statSql);
        final XYDataset dataset = ChartUtil.createInfoDatasetByMonth(sqlStr);

        final XYLineAndShapeRenderer standardxyitemrenderer = new XYLineAndShapeRenderer();
        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);
        standardxyitemrenderer.setBaseLinesVisible(false);
        final Paint[] paints = ChartUtil.createMonthPaint();
        final List shapes = ChartUtil.createShape();

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            standardxyitemrenderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
            standardxyitemrenderer.setSeriesShape(i, (Shape) shapes.get(i));
            standardxyitemrenderer.setSeriesPaint(i, paints[i + 4]);
        }

        final NumberAxis numberaxis = new NumberAxis("Range 1");
        numberaxis.setRange(0d, 4.5d);
        final XYPlot xyplot = new XYPlot(dataset, null, numberaxis,
                standardxyitemrenderer);
        xyplot.getRangeAxis().setVisible(false);
        xyplot.setDomainGridlinesVisible(false);
        xyplot.setRangeGridlinesVisible(false);

        final XYLineAndShapeRenderer standardxyitemrenderer1 = new XYLineAndShapeRenderer();

        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);
        final NumberAxis numberaxis1 = new NumberAxis(null);
        numberaxis1.setAutoRangeIncludesZero(false);
        // DateAxis dateAxis1 = new DateAxis();
        final XYPlot xyplot1 = new XYPlot(dataset1, null, numberaxis1,
                standardxyitemrenderer1);
        xyplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
        xyplot1.setDomainGridlinesVisible(false);
        xyplot1.setDomainCrosshairVisible(false);
        // xyplot1.setDomainCrosshairLockedOnData(true);

        int j = 0;
        for (int i = 0; i < xyplot1.getSeriesCount(); i++) {
            standardxyitemrenderer1.setSeriesShape(i, new Rectangle(-2, -2, 5,
                    5));

            if (i % 11 == 0) {
                j = 0;
            }
            standardxyitemrenderer1.setSeriesPaint(i, paints[j++]);
        }

        final PeriodAxis periodaxis = new PeriodAxis(null);
        // periodaxis.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
        periodaxis.setTimeZone(TimeZone.getDefault());
        periodaxis.setPositiveArrowVisible(false);
        final PeriodAxisLabelInfo aperiodaxislabelinfo[] = new PeriodAxisLabelInfo[1];
        aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(
                org.jfree.data.time.Month.class, new SimpleDateFormat("yyyyMM"));

        periodaxis.setLabelInfo(aperiodaxislabelinfo);

        periodaxis.setAutoRangeTimePeriodClass(Month.class);

        final CombinedDomainXYPlot combineddomainxyplot = new CombinedDomainXYPlot(
                periodaxis);
        combineddomainxyplot.setGap(0D);
        combineddomainxyplot.add(xyplot, 1);
        combineddomainxyplot.add(xyplot1, 6);
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);

        final JFreeChart jfreechart = new JFreeChart(null,
                JFreeChart.DEFAULT_TITLE_FONT, combineddomainxyplot, true);
        jfreechart.setBackgroundPaint(Color.WHITE);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // 增加平均值
        Double avg = null;
        final HashMap map = ChartUtil.createAvgList(avgSql);
        for (int i = 0; i < dataset1.getSeriesCount(); i++) {
            avg = Double.valueOf((String) map.get(dataset1.getSeriesKey(i)));
            final ValueMarker valuemarker = ChartUtil.createValueMarker(avg);
            xyplot1.addRangeMarker(valuemarker);
        }

        standardxyitemrenderer1
                .setToolTipGenerator(new StandardXYToolTipGenerator(
                        StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                        new SimpleDateFormat("yyyyMM"), NumberFormat
                                .getInstance()));

        if (isURL) {
            standardxyitemrenderer1.setURLGenerator(new CustomXYURLGenerator(
                    mapURL, new SimpleDateFormat("yyyyMM")));
        }

        standardxyitemrenderer.setURLGenerator(new CustomXYURLGenerator(
                "mycustfreechart.jsp", new SimpleDateFormat("yyyyMM")));

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,
                info, null);

        if ((divString != null) && (divString.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, frameName,
                    divString);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

    public static String generateInfoChartByMonth(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName, Double lable)
            throws Exception {

        /*
         * boolean isURL = false; // 创建图表的MAP热点链接 if ((mapURL != null) &&
         * (mapURL.trim().length() > 0)) { isURL = true; } String filename = "";
         * final XYDataset dataset1 = ChartUtil.createXYDatasetByMonth(statSql);
         * final XYDataset dataset = ChartUtil.createInfoDatasetByMonth(sqlStr);
         * final XYLineAndShapeRenderer standardxyitemrenderer = new
         * XYLineAndShapeRenderer();
         * standardxyitemrenderer.setShapesFilled(true);
         * standardxyitemrenderer.setShapesVisible(true);
         * standardxyitemrenderer.setBaseLinesVisible(false); final Paint[]
         * paints = ChartUtil.createMonthPaint(); final List shapes =
         * ChartUtil.createShape(); for (int i = 0; i <
         * dataset.getSeriesCount(); i++) {
         * standardxyitemrenderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
         * standardxyitemrenderer.setSeriesShape(i, (Shape) shapes.get(i));
         * standardxyitemrenderer.setSeriesPaint(i, paints[i + 4]); } final
         * NumberAxis numberaxis = new NumberAxis("Range 1");
         * numberaxis.setRange(0d, 4.5d); final XYPlot xyplot = new
         * XYPlot(dataset, null, numberaxis, standardxyitemrenderer);
         * xyplot.getRangeAxis().setVisible(false);
         * xyplot.setDomainGridlinesVisible(false);
         * xyplot.setRangeGridlinesVisible(false); final XYLineAndShapeRenderer
         * standardxyitemrenderer1 = new XYLineAndShapeRenderer();
         * standardxyitemrenderer.setShapesFilled(true);
         * standardxyitemrenderer.setShapesVisible(true); final NumberAxis
         * numberaxis1 = new NumberAxis(null);
         * numberaxis1.setAutoRangeIncludesZero(false); // DateAxis dateAxis1 =
         * new DateAxis(); final XYPlot xyplot1 = new XYPlot(dataset1, null,
         * numberaxis1, standardxyitemrenderer1);
         * xyplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
         * xyplot1.setDomainGridlinesVisible(true);
         * xyplot1.setDomainCrosshairVisible(false); //
         * xyplot1.setDomainCrosshairLockedOnData(true); int j = 0; for (int i =
         * 0; i < xyplot1.getSeriesCount(); i++) {
         * standardxyitemrenderer1.setSeriesShape(i, new Rectangle(-2, -2, 5,
         * 5)); if (i % 11 == 0) { j = 0; }
         * standardxyitemrenderer1.setSeriesPaint(i, paints[j++]); } //
         * PeriodAxis periodaxis = new PeriodAxis(null); // //
         * periodaxis.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland")); //
         * periodaxis.setTimeZone(TimeZone.getDefault()); //
         * periodaxis.setPositiveArrowVisible(false); // PeriodAxisLabelInfo
         * aperiodaxislabelinfo[] = new // PeriodAxisLabelInfo[1]; //
         * aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo( //
         * org.jfree.data.time.Month.class, new SimpleDateFormat("yyyyMM")); // //
         * periodaxis.setLabelInfo(aperiodaxislabelinfo); // //
         * periodaxis.setAutoRangeTimePeriodClass(Month.class); final DateAxis
         * dateAxis = new DateAxis(); dateAxis.setDateFormatOverride(new
         * SimpleDateFormat("yyyyMM")); dateAxis.setTickUnit(new
         * DateTickUnit(DateTickUnit.MONTH, 1)); final CombinedDomainXYPlot
         * combineddomainxyplot = new CombinedDomainXYPlot( dateAxis);
         * combineddomainxyplot.setGap(0D); combineddomainxyplot.add(xyplot, 1);
         * combineddomainxyplot.add(xyplot1, 6);
         * combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL); final
         * JFreeChart jfreechart = new JFreeChart(null,
         * JFreeChart.DEFAULT_TITLE_FONT, combineddomainxyplot, true);
         * jfreechart.setBackgroundPaint(Color.WHITE); // 设置图例的字体 if
         * (showLegend) {
         * jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT); } //
         * 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美 jfreechart.getRenderingHints().put(
         * RenderingHints.KEY_TEXT_ANTIALIASING,
         * RenderingHints.VALUE_TEXT_ANTIALIAS_OFF); // 增加平均值 Double avg = null;
         * final HashMap map = ChartUtil.createAvgList(avgSql); for (int i = 0;
         * i < dataset1.getSeriesCount(); i++) { avg = Double.valueOf((String)
         * map.get(dataset1.getSeriesKey(i))); final ValueMarker valuemarker =
         * ChartUtil.createValueMarker(avg);
         * xyplot1.addRangeMarker(valuemarker); } standardxyitemrenderer1
         * .setToolTipGenerator(new StandardXYToolTipGenerator(
         * StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new
         * SimpleDateFormat("yyyyMM"), NumberFormat .getInstance())); if (isURL) {
         * standardxyitemrenderer1.setURLGenerator(new CustomXYURLGenerator(
         * mapURL, new SimpleDateFormat("yyyyMM"))); }
         * standardxyitemrenderer.setURLGenerator(new CustomXYURLGenerator(
         * "mycustfreechart.jsp", new SimpleDateFormat("yyyyMM"))); final
         * ChartRenderingInfo info = new ChartRenderingInfo( new
         * StandardEntityCollection()); // 把图片写入临时目录并输出 filename =
         * ServletUtilities.saveChartAsPNG(jfreechart, width, height, info,
         * null); if ((divString != null) && (divString.length() > 0)) {
         * ChartUtil.writeImageMap(pw, filename, info, false, frameName,
         * divString); } else { ChartUtil.writeImageMap(pw, filename, info,
         * false, isOpenWindow); } pw.flush(); return filename;
         */
        return generateInfoChart(statSql, avgSql, sqlStr, session, pw,
                showLegend, mapURL, isOpenWindow, width, height, divString,
                frameName, lable, true);
    }

    /**
     * 生成简单的线图
     * 
     * @param lineSQL 生成线图数据的SQL语句
     * @param pw 页面输出流
     * @param showLegend 是否显示图例
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generateLineChart(final String lineSQL,
            final PrintWriter pw, final boolean showLegend,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final Double avgValue, final String frameName) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateLineChart(String, PrintWriter, boolean, int, int) - start");
        }

        // ChartUtil.generateLineChart("",pw,false,"",false,width,200,new
        // Double(0.0), null);

        final String filename = ChartUtil.generateLineChart(lineSQL, null, pw,
                null, null, null, showLegend, 1, true, true, mapURL,
                isOpenWindow, width, height, avgValue, frameName);

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateLineChart(String, PrintWriter, boolean, int, int) - end");
        }
        return filename;
    }

    /**
     * 生成简单柱图
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param rowKey 柱图的行标识，对于简单柱图可以任意设置，只要求非空的字段即可
     * @param session 把图像保存在session中，可以设置为空，当为空是一次性，不会保存在临时目录
     * @param pw 页面输出流
     * @param chartTitle 图表标题，可是为空
     * @param x_axisName X轴名称
     * @param y_axisName Y轴名称
     * @param showLegend 是否显示图例
     * @param orientation XY轴垂直还是水平标志
     * @param labelPosition 标题倾斜度
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generateSimpleBarChart(final String barSQL,
            final String rowKey, final HttpSession session,
            final PrintWriter pw, final String chartTitle,
            final String x_axisName, final String y_axisName,
            final boolean showLegend, final int orientation,
            final Double labelPosition, final String mapURL,
            final boolean isOpenWindow, final int width, final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimpleBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - start");
        }

        String filename = null;
        try {

            final CategoryDataset defaultcategorydataset = ChartUtil
                    .createSimpleCategoryDataset(barSQL);

            JFreeChart jfreechart = null;
            if (orientation == 0) {
                jfreechart = ChartFactory.createBarChart(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.HORIZONTAL, showLegend, true, false);
            }
            else {
                jfreechart = ChartFactory.createBarChart(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.VERTICAL, showLegend, true, false);
            }
            jfreechart.setBackgroundPaint(Color.white);

            // 设置图例的字体
            if (showLegend) {
                jfreechart.getLegend()
                        .setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
            }

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
            jfreechart.getRenderingHints().put(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            final CategoryPlot categoryplot = (CategoryPlot) jfreechart
                    .getPlot();
            categoryplot.setBackgroundPaint(Color.white);
            categoryplot.setInsets(new RectangleInsets(10D, 5D, 5D, 5D));
            categoryplot.setOutlinePaint(Color.black);
            categoryplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            categoryplot.setRangeGridlineStroke(new BasicStroke(1.0F));

            ChartUtil.showNoDataMessage(categoryplot);

            final Paint apaint[] = ChartUtil.createMonthPaint();
            final CustomBarRenderer custombarrenderer = new CustomBarRenderer(
                    apaint);
            custombarrenderer
                    .setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
                            "{2}", NumberFormat.getInstance()));

            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                custombarrenderer
                        .setItemURLGenerator(new StandardCategoryURLGenerator(
                                mapURL));
            }

            categoryplot.setRenderer(custombarrenderer);

            final NumberAxis numberaxis = (NumberAxis) categoryplot
                    .getRangeAxis();
            numberaxis
                    .setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            numberaxis.setTickMarkPaint(Color.black);
            numberaxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            numberaxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            final CategoryAxis domainAxis = categoryplot.getDomainAxis();
            domainAxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            domainAxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            // 设置标题倾斜度，用来解决标题过长变成...的问题，但是字体变得相对难看
            if (labelPosition != null) {
                domainAxis.setCategoryLabelPositions(CategoryLabelPositions
                        .createDownRotationLabelPositions(labelPosition
                                .doubleValue()));
            }

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(jfreechart, width,
                    height, info, null);
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger
                    .error(
                            "generateSimpleBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int)",
                            e);
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimpleBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - end");
        }
        return filename;
    }

    /**
     * 生成简单3D图表
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param pw 页面输出流
     * @param labelPosition 标题倾斜度
     * @param mapURL 热点链接的URL
     * @param isOpenWindow 是否在新窗口中显示URL
     * @param width 图表高度
     * @param height
     * @return 最终生成图片的文件名
     */
    public static String generateSimpleBarChart(final String barSQL,
            final PrintWriter pw, final Double labelPosition,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimpleBarChart(String, PrintWriter, Double, int, int) - start");
        }

        final String returnString = ChartUtil.generateSimpleBarChart(barSQL,
                "", null, pw, null, null, null, false, 1, labelPosition,
                mapURL, isOpenWindow, width, height);
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimpleBarChart(String, PrintWriter, Double, int, int) - end");
        }
        return returnString;
    }

    /**
     * 生成简单3D柱图
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param rowKey 柱图的行标识，对于简单柱图可以任意设置，只要求非空的字段即可
     * @param session 把图像保存在session中，可以设置为空，当为空是一次性，不会保存在临时目录
     * @param pw 页面输出流
     * @param chartTitle 图表标题，可是为空
     * @param x_axisName X轴名称
     * @param y_axisName Y轴名称
     * @param showLegend 是否显示图例
     * @param orientation XY轴垂直还是水平标志
     * @param labelPosition 标题倾斜度
     * @param mapURL 热点链接的URL
     * @param isOpenWindow 是否在新窗口中显示URL
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generateSimple3DBarChart(final String barSQL,
            final String rowKey, final HttpSession session,
            final PrintWriter pw, final String chartTitle,
            final String x_axisName, final String y_axisName,
            final boolean showLegend, final int orientation,
            final Double labelPosition, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final float alpha, final String colorType) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - start");
        }

        String filename = null;
        try {
            final CategoryDataset defaultcategorydataset = ChartUtil
                    .createSimpleCategoryDataset(barSQL);

            JFreeChart jfreechart = null;
            if (orientation == 0) {
                jfreechart = ChartFactory.createBarChart3D(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.HORIZONTAL, showLegend, true, false);
            }
            else {
                jfreechart = ChartFactory.createBarChart3D(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.VERTICAL, showLegend, true, false);
            }
            jfreechart.setBackgroundPaint(Color.white);
            // 设置图例的字体

            if (showLegend) {
                jfreechart.getLegend()
                        .setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
            }

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美

            jfreechart.getRenderingHints().put(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            final CategoryPlot categoryplot = (CategoryPlot) jfreechart
                    .getPlot();
            categoryplot.setBackgroundPaint(Color.white);
            categoryplot.setInsets(new RectangleInsets(10D, 5D, 5D, 5D));
            categoryplot.setOutlinePaint(Color.black);
            categoryplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            categoryplot.setRangeGridlineStroke(new BasicStroke(1.0F));
            categoryplot.setForegroundAlpha(alpha);
            ChartUtil.showNoDataMessage(categoryplot);

            Paint apaint[] = null;
            if (colorType.toLowerCase().equals("m")) {
                apaint = ChartUtil.createMonthPaint();
            }
            else {
                apaint = ChartUtil.createWeekPaint();
            }
            final CustomBarRenderer3D custombarrenderer = new CustomBarRenderer3D(
                    apaint);
            custombarrenderer
                    .setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
                            "{2}", NumberFormat.getInstance()));
            custombarrenderer.setMaximumBarWidth(0.06D);

            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                custombarrenderer
                        .setItemURLGenerator(new StandardCategoryURLGenerator(
                                mapURL));
            }
            categoryplot.setRenderer(custombarrenderer);

            final NumberAxis numberaxis = (NumberAxis) categoryplot
                    .getRangeAxis();
            numberaxis
                    .setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            numberaxis.setTickMarkPaint(Color.black);
            numberaxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            numberaxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            // double upperRange = numberaxis.getRange().getUpperBound();
            // upperRange += numberaxis.getRange().getCentralValue();
            // numberaxis.setRange(numberaxis.getRange().getLowerBound(),
            // upperRange);

            final CategoryAxis domainAxis = categoryplot.getDomainAxis();
            domainAxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            domainAxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            // 设置标题倾斜度，用来解决标题过长变成...的问题，但是字体变得相对难看
            if (labelPosition != null) {
                domainAxis.setCategoryLabelPositions(CategoryLabelPositions
                        .createDownRotationLabelPositions(labelPosition
                                .doubleValue()));
            }

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(jfreechart, width,
                    height, info, null);
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow,
                    mapURL);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger
                    .error(
                            "generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int)",
                            e);
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - end");
        }
        return filename;
    }

    /**
     * 生成简单3D图表
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param pw 页面输出流
     * @param labelPosition 标题倾斜度
     * @param mapURL 热点链接的URL
     * @param isOpenWindow 是否在新窗口中显示URL
     * @param width 图表高度
     * @param height
     * @return 最终生成图片的文件名
     */
    public static String generateSimple3DBarChart(final String barSQL,
            final PrintWriter pw, final Double labelPosition,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final float alpha, final String colorType) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, PrintWriter, Double, int, int) - start");
        }

        final String returnString = ChartUtil.generateSimple3DBarChart(barSQL,
                "", null, pw, null, null, null, false, 1, labelPosition,
                mapURL, isOpenWindow, width, height, alpha, colorType);
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, PrintWriter, Double, int, int) - end");
        }
        return returnString;
    }

    /**
     * 自定义颜色，给自定义的BAR图表类型使用
     * 
     * @return 颜色列表
     */
    private static Paint[] createMonthPaint() {
        final Paint[] apaint = { Color.red, Color.blue, Color.green,
                new Color(200, 200, 0), new Color(255, 112, 255),
                new Color(138, 232, 232), new Color(255, 175, 175),
                new Color(128, 128, 128), new Color(103, 5, 186),
                new Color(0, 147, 104), new Color(118, 167, 1),
                new Color(186, 102, 2) };
        return apaint;
    }

    /**
     * 自定义颜色，给自定义的BAR图表类型使用
     * 
     * @return 颜色列表
     */
    private static Paint[] createWeekPaint() {
        final Paint[] apaint = { Color.red, Color.blue, Color.green,
                Color.yellow, new Color(255, 112, 255),
                new Color(138, 232, 232), new Color(255, 175, 175) };
        return apaint;
    }

    /**
     * 生成标准3D柱图
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param session 把图像保存在session中，可以设置为空，当为空是一次性，不会保存在临时目录
     * @param pw 页面输出流
     * @param chartTitle 图表标题，可是为空
     * @param x_axisName X轴名称
     * @param y_axisName Y轴名称
     * @param showLegend 是否显示图例
     * @param orientation XY轴垂直还是水平标志
     * @param labelPosition 标题倾斜度
     * @param mapURL 热点链接
     * @param isOpenWindow 是否在新窗口中显示URL
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generate3DBarChart(final String barSQL,
            final HttpSession session, final PrintWriter pw,
            final String chartTitle, final String x_axisName,
            final String y_axisName, final boolean showLegend,
            final int orientation, final Double labelPosition,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height, final float alpha) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - start");
        }

        String filename = null;
        try {

            final CategoryDataset defaultcategorydataset = ChartUtil
                    .createCategoryDataset(barSQL);

            JFreeChart jfreechart = null;
            if (orientation == 0) {
                jfreechart = ChartFactory.createBarChart3D(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.HORIZONTAL, showLegend, true, false);
            }
            else {
                jfreechart = ChartFactory.createBarChart3D(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.VERTICAL, showLegend, true, false);
            }
            jfreechart.setBackgroundPaint(Color.white);
            // 设置图例的字体

            if (showLegend) {
                jfreechart.getLegend()
                        .setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
            }

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
            jfreechart.getRenderingHints().put(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            final CategoryPlot categoryplot = (CategoryPlot) jfreechart
                    .getPlot();
            categoryplot.setBackgroundPaint(Color.white);
            categoryplot.setInsets(new RectangleInsets(10D, 5D, 5D, 5D));
            categoryplot.setOutlinePaint(Color.black);
            categoryplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            categoryplot.setRangeGridlineStroke(new BasicStroke(1.0F));
            categoryplot.setForegroundAlpha(alpha);

            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                categoryplot.getRenderer().setItemURLGenerator(
                        new StandardCategoryURLGenerator(mapURL));
            }

            ChartUtil.showNoDataMessage(categoryplot);

            final NumberAxis numberaxis = (NumberAxis) categoryplot
                    .getRangeAxis();
            numberaxis
                    .setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            numberaxis.setTickMarkPaint(Color.black);
            numberaxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            numberaxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            final CategoryAxis domainAxis = categoryplot.getDomainAxis();
            domainAxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            domainAxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            // 设置标题倾斜度，用来解决标题过长变成...的问题，但是字体变得相对难看
            if (labelPosition != null) {
                domainAxis.setCategoryLabelPositions(CategoryLabelPositions
                        .createDownRotationLabelPositions(labelPosition
                                .doubleValue()));
            }

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(jfreechart, width,
                    height, info, null);
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger
                    .error(
                            "generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int)",
                            e);
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - end");
        }
        return filename;
    }

    /**
     * 生成标准3D柱图
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param pw 页面输出流
     * @param showLegend 是否显示图例
     * @param labelPosition 标题倾斜度
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generate3DBarChart(final String barSQL,
            final PrintWriter pw, final boolean showLegend,
            final Double labelPosition, final String mapURL,
            final boolean isOpenWindow, final int width, final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DBarChart(String, PrintWriter, boolean, int, Double, int, int) - start");
        }

        final String returnString = ChartUtil.generate3DBarChart(barSQL, null,
                pw, null, null, null, showLegend, 1, labelPosition, mapURL,
                isOpenWindow, width, height, 1F);
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DBarChart(String, PrintWriter, boolean, int, Double, int, int) - end");
        }
        return returnString;
    }


    /**
     * 生成标准3D柱图
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param session 把图像保存在session中，可以设置为空，当为空是一次性，不会保存在临时目录
     * @param pw 页面输出流
     * @param chartTitle 图表标题，可是为空
     * @param x_axisName X轴名称
     * @param y_axisName Y轴名称
     * @param showLegend 是否显示图例
     * @param orientation XY轴垂直还是水平标志
     * @param labelPosition 标题倾斜度
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generateBarChart(final String barSQL,
            final HttpSession session, final PrintWriter pw,
            final String chartTitle, final String x_axisName,
            final String y_axisName, final boolean showLegend,
            final int orientation, final Double labelPosition,
            final String mapURL, final boolean isOpenWindow, final int width,
            final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - start");
        }

        String filename = null;
        try {

            final CategoryDataset defaultcategorydataset = ChartUtil
                    .createCategoryDataset(barSQL);

            JFreeChart jfreechart = null;
            if (orientation == 0) {
                jfreechart = ChartFactory.createBarChart(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.HORIZONTAL, showLegend, true, false);
            }
            else {
                jfreechart = ChartFactory.createBarChart(chartTitle,
                        x_axisName, y_axisName, defaultcategorydataset,
                        PlotOrientation.VERTICAL, showLegend, true, false);
            }
            jfreechart.setBackgroundPaint(Color.white);
            // 设置图例的字体

            if (showLegend) {
                jfreechart.getLegend()
                        .setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
            }

            // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
            jfreechart.getRenderingHints().put(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            final CategoryPlot categoryplot = (CategoryPlot) jfreechart
                    .getPlot();
            categoryplot.setNoDataMessage("NO DATA!");
            categoryplot.setBackgroundPaint(Color.white);
            categoryplot.setInsets(new RectangleInsets(10D, 5D, 5D, 5D));
            categoryplot.setOutlinePaint(Color.black);
            categoryplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            categoryplot.setRangeGridlineStroke(new BasicStroke(1.0F));

            // 创建图表的MAP热点链接
            if ((mapURL != null) && (mapURL.trim().length() > 0)) {
                categoryplot.getRenderer().setItemURLGenerator(
                        new StandardCategoryURLGenerator(mapURL));
            }

            ChartUtil.showNoDataMessage(categoryplot);

            final NumberAxis numberaxis = (NumberAxis) categoryplot
                    .getRangeAxis();
            numberaxis
                    .setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            numberaxis.setTickMarkPaint(Color.black);
            numberaxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);
            numberaxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            final CategoryAxis domainAxis = categoryplot.getDomainAxis();
            domainAxis.setLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            domainAxis.setTickLabelFont(ChartUtil.DEFAULT_TITLE_FONT);

            // 设置标题倾斜度，用来解决标题过长变成...的问题，但是字体变得相对难看
            if (labelPosition != null) {
                domainAxis.setCategoryLabelPositions(CategoryLabelPositions
                        .createDownRotationLabelPositions(labelPosition
                                .doubleValue()));
            }

            final ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());

            // 把图片写入临时目录并输出
            filename = ServletUtilities.saveChartAsPNG(jfreechart, width,
                    height, info, null);
            ChartUtilities.writeImageMap(pw, filename, info, false);
            pw.flush();
        }
        catch (final Exception e) {
            ChartUtil.logger
                    .error(
                            "generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int)",
                            e);
        }

        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generateSimple3DBarChart(String, String, HttpSession, PrintWriter, String, String, String, boolean, int, Double, int, int) - end");
        }
        return filename;
    }

    /**
     * 生成标准3D柱图
     * 
     * @param barSQL 生成柱图数据的SQL语句
     * @param pw 页面输出流
     * @param showLegend 是否显示图例
     * @param labelPosition 标题倾斜度
     * @param mapURL 为空将不创建图表热点链接.
     * @param isOpenWindow 热点链接时本页刷新还是弹出新窗口?
     * @param width 图表高度
     * @param height 图表宽度
     * @return 最终生成图片的文件名
     */
    public static String generateBarChart(final String barSQL,
            final PrintWriter pw, final boolean showLegend,
            final Double labelPosition, final String mapURL,
            final boolean isOpenWindow, final int width, final int height) {
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DBarChart(String, PrintWriter, boolean, int, Double, int, int) - start");
        }

        final String returnString = ChartUtil.generateBarChart(barSQL, null,
                pw, null, null, null, showLegend, 1, labelPosition, mapURL,
                isOpenWindow, width, height);
        if (ChartUtil.logger.isDebugEnabled()) {
            ChartUtil.logger
                    .debug("generate3DBarChart(String, PrintWriter, boolean, int, Double, int, int) - end");
        }
        return returnString;
    }

    /**
     * Writes an image map to an output stream.
     * 
     * @param writer the writer (<code>null</code> not permitted).
     * @param name the map name (<code>null</code> not permitted).
     * @param info the chart rendering info (<code>null</code> not
     *            permitted).
     * @param useOverLibForToolTips whether to use OverLIB for tooltips
     *            (http://www.bosrup.com/web/overlib/).
     * @throws IOException if there are any I/O errors.
     */
    private static void writeImageMap(final PrintWriter writer,
            final String name, final ChartRenderingInfo info,
            final boolean useOverLibForToolTips,
            final boolean useNewWindowForURL) throws IOException {

        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator = null;
        if (useOverLibForToolTips) {
            toolTipTagFragmentGenerator = new OverLIBToolTipTagFragmentGenerator();
        }
        else {
            toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
        }

        URLTagFragmentGenerator urlTagFragmentGenerator = null;
        if (useNewWindowForURL) {
            urlTagFragmentGenerator = new WindowURLTagFragmentGenerator();
        }
        else {
            urlTagFragmentGenerator = new StandardURLTagFragmentGenerator();
        }

        ImageMapUtilities.writeImageMap(writer, name, info,
                toolTipTagFragmentGenerator, urlTagFragmentGenerator);
    }

    /**
     * Writes an image map to an output stream.
     * 
     * @param writer the writer (<code>null</code> not permitted).
     * @param name the map name (<code>null</code> not permitted).
     * @param info the chart rendering info (<code>null</code> not
     *            permitted).
     * @param useOverLibForToolTips whether to use OverLIB for tooltips
     *            (http://www.bosrup.com/web/overlib/).
     * @throws IOException if there are any I/O errors.
     */
    private static void writeImageMap(final PrintWriter writer,
            final String name, final ChartRenderingInfo info,
            final boolean useOverLibForToolTips,
            final boolean useNewWindowForURL, final String customJavascript)
            throws IOException {

        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator = null;
        if (useOverLibForToolTips) {
            toolTipTagFragmentGenerator = new OverLIBToolTipTagFragmentGenerator();
        }
        else {
            toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
        }

        if (useNewWindowForURL) {
            final WindowURLTagFragmentGenerator urlTagFragmentGenerator = new WindowURLTagFragmentGenerator();
            urlTagFragmentGenerator.setCustomJavaScript(customJavascript);
            ImageMapUtilities.writeImageMap(writer, name, info,
                    toolTipTagFragmentGenerator, urlTagFragmentGenerator);

        }
        else {
            final URLTagFragmentGenerator urlTagFragmentGenerator = new StandardURLTagFragmentGenerator();
            ImageMapUtilities.writeImageMap(writer, name, info,
                    toolTipTagFragmentGenerator, urlTagFragmentGenerator);
        }
    }

    /**
     * Writes an image map to an output stream.
     * 
     * @param writer the writer (<code>null</code> not permitted).
     * @param name the map name (<code>null</code> not permitted).
     * @param info the chart rendering info (<code>null</code> not
     *            permitted).
     * @param useOverLibForToolTips whether to use OverLIB for tooltips
     *            (http://www.bosrup.com/web/overlib/).
     * @throws IOException if there are any I/O errors.
     */
    private static void writeImageMap(final PrintWriter writer,
            final String name, final ChartRenderingInfo info,
            final boolean useOverLibForToolTips,
            final boolean useNewWindowForURL, final String customJavascript,
            final String frameName) throws IOException {

        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator = null;
        if (useOverLibForToolTips) {
            toolTipTagFragmentGenerator = new OverLIBToolTipTagFragmentGenerator();
        }
        else {
            toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
        }

        if (useNewWindowForURL) {
            final WindowURLTagFragmentGenerator urlTagFragmentGenerator = new WindowURLTagFragmentGenerator();
            urlTagFragmentGenerator.setCustomJavaScript(customJavascript);
            urlTagFragmentGenerator.setFrameName(frameName);
            ImageMapUtilities.writeImageMap(writer, name, info,
                    toolTipTagFragmentGenerator, urlTagFragmentGenerator);

        }
        else {
            final URLTagFragmentGenerator urlTagFragmentGenerator = new StandardURLTagFragmentGenerator();
            ImageMapUtilities.writeImageMap(writer, name, info,
                    toolTipTagFragmentGenerator, urlTagFragmentGenerator);
        }
    }

    /**
     * Writes an image map to an output stream.
     * 
     * @param writer the writer (<code>null</code> not permitted).
     * @param name the map name (<code>null</code> not permitted).
     * @param info the chart rendering info (<code>null</code> not
     *            permitted).
     * @param useOverLibForToolTips whether to use OverLIB for tooltips
     *            (http://www.bosrup.com/web/overlib/).
     * @throws IOException if there are any I/O errors.
     */
    private static void writeImageMap(final PrintWriter writer,
            final String name, final ChartRenderingInfo info,
            final boolean useOverLibForToolTips, final String frameName,
            final String showDiv) throws IOException {

        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator = null;
        if (useOverLibForToolTips) {
            toolTipTagFragmentGenerator = new OverLIBToolTipTagFragmentGenerator();
        }
        else {
            toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
        }

        final WindowURLTagFragmentGenerator urlTagFragmentGenerator = new WindowURLTagFragmentGenerator();
        urlTagFragmentGenerator.setDivString(showDiv);
        urlTagFragmentGenerator.setFrameName(frameName);
        ImageMapUtilities.writeImageMap(writer, name, info,
                toolTipTagFragmentGenerator, urlTagFragmentGenerator);
    }

    /**
     * 当图表无数据时,显示自定义的提示
     * 
     * @param plot Plot
     */
    private static void showNoDataMessage(final Plot plot) {
        plot.setNoDataMessage(ChartUtil.NO_DATA_MESSAGE);
        plot.setNoDataMessageFont(ChartUtil.NO_DATA_MESSAGE_FONT);
        plot.setNoDataMessagePaint(ChartUtil.NO_DATA_MESSAGE_COLOR);
    }

    public static String generateCombInfoChart(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName,
            Double labelPosition, boolean combined) throws Exception {

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        String filename = "";

        final CategoryDataset dataset1 = ChartUtil
                .createCategoryDatasetByTime(statSql);
        final CategoryDataset dataset = ChartUtil
                .createCategoryDatasetByTime(sqlStr);

        final LineAndShapeRenderer standardxyitemrenderer = new LineAndShapeRenderer();
        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);
        // standardxyitemrenderer.setBaseLinesVisible(false);

        final Paint[] paints = ChartUtil.createMonthPaint();

        final List shapes = ChartUtil.createShape();

        final NumberAxis numberaxis = new NumberAxis("Range 1");
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setRange(0d, 6.5d);
        final CategoryPlot xyplot = new CategoryPlot(dataset, null, numberaxis,
                standardxyitemrenderer);

        xyplot.getRangeAxis().setVisible(false);
        // xyplot.setDomainGridlinesVisible(false);
        // xyplot.setRangeGridlinesVisible(false);
        xyplot.setBackgroundPaint(new Color(237, 251, 240));

        final BarRenderer standardxyitemrenderer1 = new BarRenderer();
        standardxyitemrenderer1
                .setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        standardxyitemrenderer.setShapesFilled(true);
        standardxyitemrenderer.setShapesVisible(true);

        LayeredBarRenderer layeredbarrenderer = new LayeredBarRenderer();
        layeredbarrenderer.setDrawBarOutline(false);

        final NumberAxis numberaxis1 = new NumberAxis(null);
        numberaxis1.setNumberFormatOverride(DecimalFormat.getInstance());
        numberaxis1.setAutoRangeIncludesZero(false);
        // CategoryAxis domainAxis = new CategoryAxis();

        final CategoryPlot xyplot1 = new CategoryPlot(dataset1, null,
                numberaxis1, standardxyitemrenderer1);
        xyplot1.setDomainGridlinesVisible(true);
        standardxyitemrenderer1
                .setToolTipGenerator(new StandardCategoryToolTipGenerator());
        xyplot1.setRenderer(layeredbarrenderer);
        xyplot1.setRowRenderingOrder(SortOrder.DESCENDING);

        final CombinedDomainCategoryPlot combineddomainxyplot = new CombinedDomainCategoryPlot();
        combineddomainxyplot.setGap(5D);
        if (combined) {
            combineddomainxyplot.add(xyplot, 1);
        }

        combineddomainxyplot.add(xyplot1, 6);
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);

        // 设置标题倾斜度，用来解决标题过长变成...的问题，但是字体变得相对难看
        if (labelPosition != null) {
            combineddomainxyplot.getDomainAxis().setCategoryLabelPositions(
                    CategoryLabelPositions
                            .createDownRotationLabelPositions(labelPosition
                                    .doubleValue()));
        }

        final JFreeChart jfreechart = new JFreeChart(null,
                JFreeChart.DEFAULT_TITLE_FONT, combineddomainxyplot, showLegend);
        jfreechart.setBackgroundPaint(Color.WHITE);

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        if (isURL) {

            standardxyitemrenderer1
                    .setBaseItemURLGenerator(new StandardCategoryURLGenerator(
                            "mycustfreechart1.jsp"));

        }

        standardxyitemrenderer
                .setBaseItemURLGenerator(new StandardCategoryURLGenerator(
                        "mycustfreechart.jsp"));

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        filename = ServletUtilities.saveChartAsPNG(jfreechart, width, height,
                info, null);

        if ((divString != null) && (divString.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, frameName,
                    divString);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

    public static String generateOverlaidBarChart(final String statSql,
            final String avgSql, final String sqlStr,
            final HttpSession session, final PrintWriter pw,
            final boolean showLegend, final String mapURL,
            final boolean isOpenWindow, final int width, final int height,
            final String divString, final String frameName,
            Double labelPosition, boolean combined) throws Exception {

        CategoryDataset dataset1 = ChartUtil
                .createCategoryDatasetByTime(statSql);
        CategoryDataset dataset = ChartUtil.createCategoryDatasetByTime(sqlStr);

        StandardCategoryItemLabelGenerator standardcategoryitemlabelgenerator = new StandardCategoryItemLabelGenerator();
        BarRenderer barrenderer = new BarRenderer();
        barrenderer.setItemMargin(0d);
        barrenderer.setItemLabelGenerator(standardcategoryitemlabelgenerator);
        barrenderer.setItemLabelsVisible(false);

        barrenderer
                .setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
                        "{2}", NumberFormat.getInstance()));
        CategoryPlot categoryplot = new CategoryPlot();
        categoryplot.setDataset(dataset1);
        categoryplot.setRenderer(barrenderer);
        categoryplot.setDomainAxis(new CategoryAxis(""));
        categoryplot.setRangeAxis(new NumberAxis(""));
        categoryplot.setOrientation(PlotOrientation.VERTICAL);
        categoryplot.setRangeGridlinesVisible(true);
        categoryplot.setDomainGridlinesVisible(true);

        LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
        categoryplot.setDataset(1, dataset);
        categoryplot.setRenderer(1, lineandshaperenderer);
        NumberAxis numberaxis = new NumberAxis("波动比率");
        categoryplot.setRangeAxis(1, numberaxis);
        categoryplot.mapDatasetToRangeAxis(1, 1);
        categoryplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        boolean isURL = false;
        // 创建图表的MAP热点链接
        if ((mapURL != null) && (mapURL.trim().length() > 0)) {
            isURL = true;
        }

        if (isURL) {
            lineandshaperenderer
                    .setBaseItemURLGenerator(new StandardCategoryURLGenerator(
                            "mycustfreechart1.jsp"));

        }
        // 设置标题倾斜度，用来解决标题过长变成...的问题，但是字体变得相对难看
        if (labelPosition != null) {
            categoryplot.getDomainAxis().setCategoryLabelPositions(
                    CategoryLabelPositions
                            .createDownRotationLabelPositions(labelPosition
                                    .doubleValue()));
        }
        JFreeChart jfreechart = new JFreeChart(categoryplot);
        // jfreechart.setTitle("Overlaid Bar Chart");

        // jfreechart.setBackgroundPaint(Color.WHITE);

        // 关闭提示文本的抗锯齿功能，字体设置为12pt-14pt显示最完美
        jfreechart.getRenderingHints().put(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        jfreechart.setBackgroundPaint(Color.white);

        // 设置图例的字体
        if (showLegend) {
            jfreechart.getLegend().setItemFont(ChartUtil.DEFAULT_TITLE_FONT);
        }

        final ChartRenderingInfo info = new ChartRenderingInfo(
                new StandardEntityCollection());

        // 把图片写入临时目录并输出
        String filename = ServletUtilities.saveChartAsPNG(jfreechart, width,
                height, info, null);

        if ((divString != null) && (divString.length() > 0)) {
            ChartUtil.writeImageMap(pw, filename, info, false, frameName,
                    divString);
        }
        else {
            ChartUtil.writeImageMap(pw, filename, info, false, isOpenWindow);
        }
        pw.flush();

        return filename;
    }

}
