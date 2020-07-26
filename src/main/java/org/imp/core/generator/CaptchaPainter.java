// 
// Decompiled by Procyon v0.5.36
// 

package org.imp.core.generator;

import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.util.Random;
import java.awt.Color;

public class CaptchaPainter
{
    private final int width = 128;
    private final int height = 128;
    private final Color background;
    private final Random rnd;
    
    public CaptchaPainter() {
        this.background = Color.WHITE;
        this.rnd = new Random();
    }
    
    public BufferedImage draw(final Font font, final Color fGround, final String text) {
        if (font == null) {
            throw new IllegalArgumentException("Font can not be null.");
        }
        if (fGround == null) {
            throw new IllegalArgumentException("Foreground color can not be null.");
        }
        if (text == null || text.length() < 1) {
            throw new IllegalArgumentException("No text given.");
        }
        BufferedImage img = this.createImage();
        Graphics g = img.getGraphics();
        try {
            final Graphics2D g2 = this.configureGraphics(g, font, fGround);
            this.draw(g2, text);
        }
        finally {
            g.dispose();
        }
        img = this.postProcess(img);
        return img;
    }
    
    protected BufferedImage createImage() {
        return new BufferedImage(128, 128, 5);
    }
    
    protected Graphics2D configureGraphics(final Graphics g, final Font font, final Color fGround) {
        if (!(g instanceof Graphics2D)) {
            throw new IllegalStateException("Graphics (" + g + ") that is not an instance of Graphics2D.");
        }
        Graphics2D g2 = (Graphics2D)g;
        this.configureGraphicsQuality(g2);
        g2.setColor(fGround);
        g2.setBackground(this.background);
        g2.setFont(font);
        g2.clearRect(0, 0, 128, 128);
        return g2;
    }
    
    protected void configureGraphicsQuality(final Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
    
    protected void draw(final Graphics2D g, final String text) {
        GlyphVector vector = g.getFont().createGlyphVector(g.getFontRenderContext(), text);
        this.transform(g, text, vector);
        Rectangle bounds = vector.getPixelBounds(null, 0.0f, 128.0f);
        float bw = (float)bounds.getWidth();
        float bh = (float)bounds.getHeight();
        boolean outlineEnabled = true;
        float wr = 128.0f / bw * (this.rnd.nextFloat() / 20.0f + 0.89f) * 1.0f;
        float hr = 128.0f / bh * (this.rnd.nextFloat() / 20.0f + 0.68f) * 1.0f;
        g.translate((128.0f - bw * wr) / 2.0f, (128.0f - bh * hr) / 2.0f);
        g.scale(wr, hr);
        float bx = (float)bounds.getX();
        float by = (float)bounds.getY();
        g.draw(vector.getOutline(Math.signum(this.rnd.nextFloat() - 0.5f) * 2.0f * 128.0f / 200.0f - bx, Math.signum(this.rnd.nextFloat() - 0.5f) * 2.0f * 128.0f / 70.0f + 128.0f - by));
        g.drawGlyphVector(vector, -bx, 128.0f - by);
    }
    
    protected void transform(final Graphics2D g, final String text, final GlyphVector v) {
        int glyphNum = v.getNumGlyphs();
        Point2D prePos = null;
        Rectangle2D preBounds = null;
        double rotateCur = (this.rnd.nextDouble() - 0.5) * 3.141592653589793 / 8.0;
        double rotateStep = Math.signum(rotateCur) * (this.rnd.nextDouble() * 3.0 * 3.141592653589793 / 8.0 / glyphNum);
        boolean rotateEnabled = true;
        for (int fi = 0; fi < glyphNum; ++fi) {
            final AffineTransform tr = AffineTransform.getRotateInstance(rotateCur);
            if (this.rnd.nextDouble() < 0.25) {
                rotateStep *= -1.0;
            }
            rotateCur += rotateStep;
            v.setGlyphTransform(fi, tr);
            Point2D pos = v.getGlyphPosition(fi);
            Rectangle2D bounds = v.getGlyphVisualBounds(fi).getBounds2D();
            Point2D newPos;
            if (prePos == null) {
                newPos = new Point2D.Double(pos.getX() - bounds.getX(), pos.getY());
            }
            else {
                newPos = new Point2D.Double(preBounds.getMaxX() + pos.getX() - bounds.getX() - Math.min(preBounds.getWidth(), bounds.getWidth()) * (this.rnd.nextDouble() / 20.0 + 0.27), pos.getY());
            }
            v.setGlyphPosition(fi, newPos);
            prePos = newPos;
            preBounds = v.getGlyphVisualBounds(fi).getBounds2D();
        }
    }
    
    protected BufferedImage postProcess(BufferedImage img) {
        Rippler.AxisConfig vertical = new Rippler.AxisConfig(this.rnd.nextDouble() * 2.0 * 3.141592653589793, (1.0 + this.rnd.nextDouble() * 2.0) * 3.141592653589793, img.getHeight() / (double)(this.rnd.nextInt(5) + 6));
        Rippler.AxisConfig horizontal = new Rippler.AxisConfig(this.rnd.nextDouble() * 2.0 * 3.141592653589793, (2.0 + this.rnd.nextDouble() * 2.0) * 3.141592653589793, img.getWidth() / (double)(this.rnd.nextInt(16) + 85));
        Rippler op = new Rippler(vertical, horizontal);
        img = op.filter(img, this.createImage());
        float[] blurArray = new float[9];
        this.fillBlurArray(blurArray);
        ConvolveOp op2 = new ConvolveOp(new Kernel(3, 3, blurArray), 1, null);
        img = op2.filter(img, this.createImage());
        return img;
    }
    
    protected void fillBlurArray(final float[] array) {
        float sum = 0.0f;
        for (int fi = 0; fi < array.length; ++fi) {
            array[fi] = this.rnd.nextFloat();
            sum += array[fi];
        }
        for (int fi = 0; fi < array.length; ++fi) {
            final int n = fi;
            array[n] /= sum;
        }
    }
}
