package com.crawler.selenium;

import org.apache.commons.lang3.RandomUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yuanyong on 17/4/26.
 */
public class Utils {


    public static BufferedImage getBufferedImage(String image) {
        File file = new File(image);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bi;
    }

    public static int[] getpixel(BufferedImage bi, int x, int y) throws Exception {
        int[] rgb = new int[3];
        int pixel = bi.getRGB(x, y); // 下面三行代码将一个数字转换为RGB数字
        rgb[0] = (pixel & 0xff0000) >> 16;
        rgb[1] = (pixel & 0xff00) >> 8;
        rgb[2] = (pixel & 0xff);
        return rgb;
    }

    /**
     * 对比RGB值
     *
     * @param image1
     * @param image2
     * @param x
     * @param y
     * @return
     * @throws Exception
     */
    public static boolean is_similar(BufferedImage image1, BufferedImage image2, int x, int y) throws Exception {
        int[] pixel1 = getpixel(image1, x, y);
        int[] pixel2 = getpixel(image2, x, y);
        for (int i = 0; i < 3; i++) {
            if (Math.abs(pixel1[i] - pixel2[i]) >= 50) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算缺口的位置
     *
     * @param image1
     * @param image2
     * @return
     * @throws Exception
     */
    public static int get_diff_location(BufferedImage image1, BufferedImage image2) throws Exception {
        for (int i = 0; i < 260; i++) {
            for (int j = 0; j < 116; j++) {
                if (is_similar(image1, image2, i, j) == false)
                    return i;
            }
        }
        return 0;
    }

    /**
     * 根据缺口的位置模拟x轴移动的轨迹
     *
     * @param length
     * @return
     * @throws Exception
     */
    public static List<Integer> get_diff_location(int length) throws Exception {
        int x = RandomUtils.nextInt(1, 3);
        List<Integer> list = new ArrayList<>();
        while (length - x >= 5) {
            list.add(x);
            length = length - x;
            x = RandomUtils.nextInt(1, 3);
        }
        for (int i = 0; i < length; i++) {
            list.add(1);
        }
        return list;
    }
    public static List<Integer> get_diff_location2(int length) throws Exception {
        int temp=length;
        int x = RandomUtils.nextInt(1, 3);
        List<Integer> list = new ArrayList<>();
        while (length - x >= 5) {
            length = length - x;
            list.add(length);
            x = RandomUtils.nextInt(1, 3);
        }
        Collections.reverse(list);
        list.add(temp);
        return list;
    }


    public static BufferedImage getImages(WebDriver driver, String className) throws IOException {
        String imgUrl = "";
        List<int[]> positionList = new ArrayList<int[]>();
        List<WebElement> elementList = driver.findElements(By.xpath(className));
        for (WebElement webElement : elementList) {
            String positions = webElement.getCssValue("background-position");
            imgUrl = webElement.getCssValue("background-image");
            String x = positions.split("px")[0].replace(" ", "");
            String y = positions.split("px")[1].replace(" ", "");
            positionList.add(new int[]{Integer.valueOf(x), Integer.valueOf(y)});
//            System.out.println(webElement.getCssValue("background-position"));
        }
        if(driver instanceof ChromeDriver)
        {
            imgUrl = imgUrl.replace("url(\"", "").replace("\")", "");
        }else if(driver instanceof PhantomJSDriver)
        {
            imgUrl = imgUrl.replace("url(", "").replace(")", "");
        }else{
            imgUrl = imgUrl.replace("url(\"", "").replace("\")", "");
        }
//        System.out.println(imgUrl+"哈哈");
        BufferedImage bufferedImage = getGeetestImg(imgUrl, positionList);
//        ImageIO.write(bufferedImage, "jpg", new File("/Users/yuanyong/Desktop/3.jpg"));
        return bufferedImage;
    }




    public static BufferedImage getGeetestImg(String strUrl, List<int[]> positionList) throws IOException {

        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //得到输入流
        InputStream inputStream = conn.getInputStream();
        BufferedImage img = ImageIO.read(inputStream);
        List<BufferedImage> list = new ArrayList<BufferedImage>();
        for (int i = 0; i < positionList.size(); i++) {
//            ImageIO.write(img, "png", new File("/Users/yuanyong/Desktop/Test.png"));
            BufferedImage subimage = img.getSubimage(Math.abs(positionList.get(i)[0]), Math.abs(positionList.get(i)[1]), 10, 58);
            list.add(subimage);
        }
        BufferedImage mergeImageUp = null;
        BufferedImage mergeImageDown = null;
        int mid = list.size() >>> 1;
        for (int i = 0; i < mid - 1; i++) {
            mergeImageUp = mergeImage(mergeImageUp == null ? list.get(i) : mergeImageUp, list.get(i + 1), true);
        }
        for (int i = mid; i < list.size() - 1; i++) {
            mergeImageDown = mergeImage(mergeImageDown == null ? list.get(i) : mergeImageDown, list.get(i + 1), true);
        }
        img = mergeImage(mergeImageUp, mergeImageDown, false);
        return img;
    }

    public static BufferedImage mergeImage(BufferedImage img1,
                                           BufferedImage img2, boolean isHorizontal) throws IOException {
        int w1 = img1.getWidth();
        int h1 = img1.getHeight();
        int w2 = img2.getWidth();
        int h2 = img2.getHeight();
        // 从图片中读取RGB
        int[] ImageArrayOne = new int[w1 * h1];
        ImageArrayOne = img1.getRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 逐行扫描图像中各个像素的RGB到数组中
        int[] ImageArrayTwo = new int[w2 * h2];
        ImageArrayTwo = img2.getRGB(0, 0, w2, h2, ImageArrayTwo, 0, w2);

        // 生成新图片
        BufferedImage DestImage = null;
        if (isHorizontal) { // 水平方向合并
            DestImage = new BufferedImage(w1 + w2, h1, BufferedImage.TYPE_INT_RGB);
            DestImage.setRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            DestImage.setRGB(w1, 0, w2, h2, ImageArrayTwo, 0, w2);
        } else { // 垂直方向合并
            DestImage = new BufferedImage(w1, h1 + h2,
                    BufferedImage.TYPE_INT_RGB);
            DestImage.setRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            DestImage.setRGB(0, h1, w2, h2, ImageArrayTwo, 0, w2); // 设置下半部分的RGB
        }

        return DestImage;
    }
    private static int difference(int[] a, int[] b){
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) + Math.abs(a[2] - b[2]);
    }
    public static void main(String args[]) throws Exception {

        int index = get_diff_location(getBufferedImage("E:\\fosan\\geetestpicfail\\1507775322861-1.jpg"), getBufferedImage("E:\\fosan\\geetestpicfail\\1507775322878-2.jpg"));
        System.out.println(index);
        System.out.println(get_diff_location(index));
    }

}