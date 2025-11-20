package org.jeecg.modules.system.util;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * Login verification code tool class
 * @author: jeecg-boot
 */
public class RandImageUtil {

    public static final String KEY = "JEECG_LOGIN_KEY";

    /**
     * Define graphic size
     */
    private static final int WIDTH = 105;
    /**
     * Define graphic size
     */
    private static final int HEIGHT = 35;

    /**
     * Define the number of interference lines
     */
    private static final int COUNT = 200;

    /**
     * Interference line length=1.414*lineWidth
     */
    private static final int LINE_WIDTH = 2;

    /**
     * Picture format
     */
    private static final String IMG_FORMAT = "JPEG";

    /**
     * base64 Picture prefix
     */
    private static final String BASE64_PRE = "data:image/jpg;base64,";

    /**
     * pass directlyresponse Return to picture
     * @param response
     * @param resultCode
     * @throws IOException
     */
    public static void generate(HttpServletResponse response, String resultCode) throws IOException {
        BufferedImage image = getImageBuffer(resultCode);
        // Output image to page
        ImageIO.write(image, IMG_FORMAT, response.getOutputStream());
    }

    /**
     * generatebase64string
     * @param resultCode
     * @return
     * @throws IOException
     */
    public static String generate(String resultCode) throws IOException {
        BufferedImage image = getImageBuffer(resultCode);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        //write to stream
        ImageIO.write(image, IMG_FORMAT, byteStream);
        //Convert to bytes
        byte[] bytes = byteStream.toByteArray();
        //converted tobase64string
        String base64 = Base64.getEncoder().encodeToString(bytes).trim();
        //delete \r\n
        base64 = base64.replaceAll("\n", "").replaceAll("\r", "");

        //Write to the specified location
        //ImageIO.write(bufferedImage, "png", new File(""));

        return BASE64_PRE+base64;
    }

    private static BufferedImage getImageBuffer(String resultCode){
        // Create image in memory
        final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        // Get graphics context
        final Graphics2D graphics = (Graphics2D) image.getGraphics();
        // Set background color
        // ---1
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        // Set border color
//		graphics.setColor(getRandColor(100, 200)); // ---2
        graphics.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // SHA1PRNGyes-种常用的随机数generate算法,Dealing with weak random number problems
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            random = new SecureRandom();
        }
        // Randomly generate interference lines，Make the authentication code in the image less likely to be detected by other programs
        for (int i = 0; i < COUNT; i++) {
            // ---3
            graphics.setColor(getRandColor(150, 200));

            // Make sure the drawing is within the border
            final int x = random.nextInt(WIDTH - LINE_WIDTH - 1) + 1;
            final int y = random.nextInt(HEIGHT - LINE_WIDTH - 1) + 1;
            final int xl = random.nextInt(LINE_WIDTH);
            final int yl = random.nextInt(LINE_WIDTH);
            graphics.drawLine(x, y, x + xl, y + yl);
        }
        // Get a randomly generated authentication code
        for (int i = 0; i < resultCode.length(); i++) {
            // Display the authentication code into the image,The color of the function call is the same，可能yes因为种子太接近，所以只能直接generate
            // graphics.setColor(new Color(20 + random.nextInt(130), 20 + random
            // .nextInt(130), 20 + random.nextInt(130)));
            // Set font color
            graphics.setColor(Color.BLACK);
            // Set font style
//			graphics.setFont(new Font("Arial Black", Font.ITALIC, 18));
            graphics.setFont(new Font("Times New Roman", Font.BOLD, 24));
            // Set character，character spacing，top margin
            graphics.drawString(String.valueOf(resultCode.charAt(i)), (23 * i) + 8, 26);
        }
        // Image takes effect
        graphics.dispose();
        return image;
    }

    private static Color getRandColor(int fc, int bc) { // Get a random color in a given range
        final Random random = new Random();
        int length = 255;
        if (fc > length) {
            fc = length;
        }
        if (bc > length) {
            bc = length;
        }

        final int r = fc + random.nextInt(bc - fc);
        final int g = fc + random.nextInt(bc - fc);
        final int b = fc + random.nextInt(bc - fc);

        return new Color(r, g, b);
    }
}
