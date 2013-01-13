package org.langke.jetty.common.chart;


/**
 * @version 创建时间：2006-8-23 15:49:11
 * 类说明
 */
import java.awt.*;
import java.awt.geom.*;
import org.jfree.ui.Drawable;

public class MyDrawer implements Drawable {

    public MyDrawer(Paint paint, Stroke stroke, Paint paint1) {
        outlinePaint = paint;
        outlineStroke = stroke;
        fillPaint = paint1;
    }

    public void draw(Graphics2D graphics2d, Rectangle2D rectangle2d) {
        java.awt.geom.Ellipse2D.Double double1 = new java.awt.geom.Ellipse2D.Double(
                rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getWidth(),
                rectangle2d.getHeight());
        if (fillPaint != null) {
            graphics2d.setPaint(fillPaint);
            graphics2d.fill(double1);
        }
        if (outlinePaint != null && outlineStroke != null) {
            graphics2d.setPaint(outlinePaint);
            graphics2d.setStroke(outlineStroke);
            graphics2d.draw(double1);
        }
    }

    private Paint outlinePaint;

    private Stroke outlineStroke;

    private Paint fillPaint;
}

