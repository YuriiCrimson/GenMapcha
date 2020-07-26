package org.imp.core;
import org.imp.core.generator.CaptchaPainter;
import org.imp.core.generator.map.MapPalette;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.Font;
import java.util.Random;

public final class CaptchaGeneration
{
    public static void generateImages() throws IOException {
        Random random = new Random();
        ThreadLocal<Font[]> fonts = ThreadLocal.withInitial(() -> new Font[] { new Font("SansSerif", 0, random.nextInt(5) + 62), new Font("Serif", 0, random.nextInt(5) + 62), new Font("Monospaced", 1, random.nextInt(5) + 62) });
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        MapPalette.prepareColors();
            Font[] curr = fonts.get();
            CaptchaPainter captchaPainter = new CaptchaPainter();
            int n = 0;
            BufferedImage image;
                image = captchaPainter.draw(curr[random.nextInt(curr.length)], randomNotWhiteColor(random), String.valueOf(714));
        FileOutputStream fileOutputStream = new FileOutputStream("D://cap.png");
        ImageIO.write(image,"png",fileOutputStream);
    }
    
    private static Color randomNotWhiteColor(final Random random) {
        final Color color = MapPalette.colors[random.nextInt(MapPalette.colors.length)];
        if (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255) {
            return randomNotWhiteColor(random);
        }
        return color;
    }
    
    private CaptchaGeneration() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void main(String[] args) throws IOException {
        generateImages();
    }
}
