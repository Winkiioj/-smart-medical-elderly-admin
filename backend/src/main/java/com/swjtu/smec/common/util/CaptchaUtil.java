package com.swjtu.smec.common.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * 图形验证码工具类
 *
 * <p>生成 4 位字符验证码图片，Base64 编码返回。</p>
 */
public class CaptchaUtil {

    private static final int WIDTH = 130;
    private static final int HEIGHT = 48;
    private static final int CHAR_COUNT = 4;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";  // 去掉易混淆的 0/O/1/I
    private static final Random RANDOM = new Random();

    /**
     * 生成一张验证码图片
     *
     * @return { code: 验证码文本, key: Redis key, image: "data:image/png;base64,..." }
     */
    public static CaptchaResult generate() {
        String code = randomCode();
        String image = drawImage(code);

        CaptchaResult result = new CaptchaResult();
        result.code = code;
        result.image = image;
        return result;
    }

    /**
     * 生成随机 4 位字符
     */
    private static String randomCode() {
        StringBuilder sb = new StringBuilder(CHAR_COUNT);
        for (int i = 0; i < CHAR_COUNT; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 把验证码画成扭曲图片，返回 Base64 data URL
     */
    private static String drawImage(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // 开启抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 背景：浅灰渐变
        GradientPaint bgGrad = new GradientPaint(0, 0, new Color(250, 251, 252),
                0, HEIGHT, new Color(235, 238, 242));
        g.setPaint(bgGrad);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 2. 干扰线（3 条，灰蓝细线）
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(180 + RANDOM.nextInt(50), 190 + RANDOM.nextInt(40), 220 + RANDOM.nextInt(30), 120));
            int x1 = RANDOM.nextInt(20);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = WIDTH - RANDOM.nextInt(20);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 3. 逐个绘制字符（随机旋转 + 随机偏移 + 随机颜色）
        String[] fontFamilies = {"Arial", "Dialog", "SansSerif"};
        for (int i = 0; i < code.length(); i++) {
            String ch = String.valueOf(code.charAt(i));

            // 字体：随机大小
            int fontSize = 28 + RANDOM.nextInt(8);
            String fontFamily = fontFamilies[RANDOM.nextInt(fontFamilies.length)];
            Font font = new Font(fontFamily, Font.BOLD, fontSize);
            g.setFont(font);

            // 颜色：深蓝/深灰
            int r = 20 + RANDOM.nextInt(60);
            int gn = 50 + RANDOM.nextInt(60);
            int b = 100 + RANDOM.nextInt(100);
            g.setColor(new Color(r, gn, b));

            // 每个字符的参考 x 位置
            int baseX = 22 + i * (WIDTH - 30) / CHAR_COUNT;

            // 随机旋转 ±15°
            double angle = Math.toRadians(-15 + RANDOM.nextDouble() * 30);
            g.rotate(angle, baseX + 12, HEIGHT / 2.0 + 6);

            // 绘制
            g.drawString(ch, baseX + RANDOM.nextInt(6), 30 + RANDOM.nextInt(10));

            // 恢复旋转
            g.rotate(-angle, baseX + 12, HEIGHT / 2.0 + 6);
        }

        // 4. 干扰噪点（约 30 个）
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(120 + RANDOM.nextInt(80), 140 + RANDOM.nextInt(60), 180 + RANDOM.nextInt(50), 80));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }

        g.dispose();

        // 5. 编码为 Base64
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", bos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    // ===== 结果 VO =====
    public static class CaptchaResult {
        public String code;
        public String image;
    }
}
