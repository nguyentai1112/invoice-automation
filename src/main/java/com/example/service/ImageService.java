package com.example.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

  public String extractTextFromImage(String imagePath) {
    File imageFile = new File(imagePath);
    ITesseract instance = new Tesseract();

    instance.setDatapath("/usr/local/Cellar/tesseract/5.3.3/share/tessdata");
    try {
      String result = instance.doOCR(imageFile);

      System.out.println(result);
    } catch (TesseractException e) {
      System.err.println(e.getMessage());
    }

    return "This is a sample text extracted from image";
  }

  private static Mat keepRedColor(Mat src) {
    Mat hsvImage = new Mat();
    Imgproc.cvtColor(src, hsvImage, Imgproc.COLOR_BGR2HSV);

    Scalar lowerRed1 = new Scalar(0, 120, 70);
    Scalar upperRed1 = new Scalar(10, 255, 255);
    Scalar lowerRed2 = new Scalar(170, 120, 70);
    Scalar upperRed2 = new Scalar(180, 255, 255);

    Mat mask1 = new Mat();
    Mat mask2 = new Mat();
    Core.inRange(hsvImage, lowerRed1, upperRed1, mask1);
    Core.inRange(hsvImage, lowerRed2, upperRed2, mask2);

    Mat redMask = new Mat();
    Core.bitwise_or(mask1, mask2, redMask);

    Mat redOnlyImage = new Mat(src.size(), src.type(), Scalar.all(255));  // white background
    src.copyTo(redOnlyImage, redMask);

    return redOnlyImage;
  }

  private static boolean rotateAndCheckText(Mat image) {
    ITesseract tesseract = new Tesseract();  // Make sure Tesseract is correctly set up
    tesseract.setLanguage("eng");  // Set the OCR language to English

    for (int angle = 0; angle < 360; angle += 3) {
      Mat rotatedImage = rotateImage(image, angle);
      String text = extractText(rotatedImage, tesseract);
      if (text.contains("HOA DON DA HUY")) {
        return true;
      }
    }
    return false;
  }

  private static Mat rotateImage(Mat src, double angle) {
    Point center = new Point(src.cols() / 2.0, src.rows() / 2.0);
    Mat rotMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
    Mat rotatedImage = new Mat();
    Imgproc.warpAffine(src, rotatedImage, rotMatrix, src.size());
    return rotatedImage;
  }

  private static String extractText(Mat image, ITesseract tesseract) {
    try {
      byte[] data = new byte[(int) (image.total() * image.elemSize())];
      image.get(0, 0, data);
      BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
      return tesseract.doOCR(bufferedImage);
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public static void main(String[] args) {

    String imagePath = "attachments/cancel.png";
    ImageService imageService = new ImageService();
    String text = imageService.extractTextFromImage(imagePath);
    System.out.println(text);

  }

}
