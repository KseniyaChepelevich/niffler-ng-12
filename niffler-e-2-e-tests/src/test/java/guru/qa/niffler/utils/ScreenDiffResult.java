package guru.qa.niffler.utils;


import guru.qa.niffler.jupiter.extension.ScreenShotTestExtension;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.comparison.PointsMarkupPolicy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;

public class ScreenDiffResult implements BooleanSupplier {

    private final BufferedImage expected;
    private final BufferedImage actual;
    private final ImageDiff diff;
    private final boolean hasDif;

    public ScreenDiffResult(BufferedImage expected, BufferedImage actual) {
        this.expected = expected;

        this.actual = resizeImage(actual, expected.getWidth(), expected.getHeight());

        PointsMarkupPolicy policy = new PointsMarkupPolicy();
        policy.setDiffSizeTrigger(2000);

        ImageDiffer differ = new ImageDiffer()
                .withColorDistortion(40)
                .withDiffMarkupPolicy(policy);

        this.diff = differ.makeDiff(this.expected, this.actual);
        this.hasDif = diff.hasDiff();
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    @Override
    public boolean getAsBoolean() {
        if (hasDif) {
            ScreenShotTestExtension.setExpected(expected);
            ScreenShotTestExtension.setActual(actual);
            ScreenShotTestExtension.setDiff(diff.getMarkedImage());
        }
        return hasDif;
    }

    public boolean hasDiff() {
        return diff.hasDiff();
    }

    @Override
    public String toString() {
        return String.format(
                "hasDiff=%s, diffPoints=%d, expected=%dx%d, actual=%dx%d",
                hasDif, diff.getDiffSize(),
                expected.getWidth(), expected.getHeight(),
                actual.getWidth(), actual.getHeight()
        );
    }

    public int getDiffSize() {
        return diff.getDiffSize();
    }

    public int getExpectedWidth() {
        return expected.getWidth();
    }

    public int getExpectedHeight() {
        return expected.getHeight();
    }

    public int getActualWidth() {
        return actual.getWidth();
    }

    public int getActualHeight() {
        return actual.getHeight();
    }

    public BufferedImage getDiffImage() {
        return diff.getMarkedImage();
    }

}
