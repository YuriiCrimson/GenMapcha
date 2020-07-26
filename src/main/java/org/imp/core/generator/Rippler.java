// 
// Decompiled by Procyon v0.5.36
// 

package org.imp.core.generator;

import java.awt.image.BufferedImage;

public class Rippler
{
    private final AxisConfig vertical;
    private final AxisConfig horizontal;
    
    public Rippler(final AxisConfig vertical, final AxisConfig horizontal) {
        this.vertical = vertical;
        this.horizontal = horizontal;
    }
    
    public BufferedImage filter(final BufferedImage src, final BufferedImage dest) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        final int[] verticalDelta = this.calcDeltaArray(this.vertical, width);
        final int[] horizontalDelta = this.calcDeltaArray(this.horizontal, height);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                final int ny = (y + verticalDelta[x] + height) % height;
                final int nx = (x + horizontalDelta[ny] + width) % width;
                dest.setRGB(nx, ny, src.getRGB(x, y));
            }
        }
        return dest;
    }
    
    protected int[] calcDeltaArray(final AxisConfig axisConfig, final int num) {
        final int[] delta = new int[num];
        final double start = axisConfig.getStart();
        final double period = axisConfig.getLength() / num;
        final double amplitude = axisConfig.getAmplitude();
        for (int fi = 0; fi < num; ++fi) {
            delta[fi] = (int)Math.round(amplitude * Math.sin(start + fi * period));
        }
        return delta;
    }
    
    public AxisConfig getVertical() {
        return this.vertical;
    }
    
    public AxisConfig getHorizontal() {
        return this.horizontal;
    }
    
    public static class AxisConfig
    {
        private final double start;
        private final double length;
        private final double amplitude;
        
        public AxisConfig(final double start, final double length, final double amplitude) {
            this.start = this.normalize(start, 2);
            this.length = this.normalize(length, 4);
            this.amplitude = amplitude;
        }
        
        protected double normalize(double a, final int multi) {
            final double piMulti = multi * 3.141592653589793;
            a = Math.abs(a);
            final double d = Math.floor(a / piMulti);
            return a - d * piMulti;
        }
        
        public double getStart() {
            return this.start;
        }
        
        public double getLength() {
            return this.length;
        }
        
        public double getAmplitude() {
            return this.amplitude;
        }
    }
}
