package autotests;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.imageio.ImageIO;


public class FirstTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String searchWord = "שקל";
    private ArrayList<String> checkedUrls;
    private XWPFDocument document;
    private String wordFilePath = "report.docx";


    public static void main( final String[] args ) {

    }


    @Before
    public void start(){
        checkedUrls = new ArrayList<String>();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver,10);
        document = new XWPFDocument();
    }

    @Test
    public void MyFirstTest(){
        String mainUrl = "http://www.ynet.co.il/home/0,7340,L-8,00.html";
        checkedUrls.add(mainUrl);
        searchRecurciveInUrl(mainUrl);
    }

    public void searchRecurciveInUrl(String url) {
        driver.get(url);
        searchInUrl(url);
        List<WebElement> elements = driver.findElements(By.tagName("a"));
        for (int i = 0; i < elements.size(); i++) {
            WebElement element = elements.get(i);
            String foundUrl = element.getAttribute("href");
            if(foundUrl != null
                    && isLinkInternal(foundUrl)
                    && !isDuplicatedLink(foundUrl)
                    && !isExeception(foundUrl)) {
                searchRecurciveInUrl(foundUrl);
            }
        }
    }

    public boolean isDuplicatedLink(String linkUrl) {
        if (!checkedUrls.contains(linkUrl)) {

            checkedUrls.add(linkUrl);
            return false;
        }
        return true;
    }

    public boolean isLinkInternal(String linkUrl) {

        boolean startsWithHTTP = linkUrl.startsWith("http");
        boolean startsWithHTTPAndDomain = linkUrl.startsWith("http://www.ynet.co.il");
        if (startsWithHTTP) {
            if (startsWithHTTPAndDomain) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public boolean isExeception(String url) {
        return url.startsWith("javascript:");
    }

    public void searchInUrl(String url) {
        System.out.println("searchInUrl " + url);

        List<WebElement> elements = driver.findElements(By.xpath("//*[contains(text(), '" + searchWord + "')]"));

        if (elements.size() > 0) {
            addToReport(url, elements);
        }
    }

    public void addToReport(String url, List<WebElement> elements) {
        String text = "Url: " + url;
        addParagraph(text);
        for (int i = 0; i < elements.size(); i++) {
            text = elements.get(i).getText();
            System.out.println("Sentence" + text);
            if (text != null && text.length() > 0) {
                addParagraph("Sentence: " + text);
            }
        }
        takeScreenshot();
    }

    public void takeScreenshot() {
        XWPFParagraph p = document.createParagraph();
        XWPFRun r = p.createRun();

        File img1 = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);

        BufferedImage bimg1 = null;
        try {
            bimg1 = ImageIO.read(img1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int width1 = bimg1.getWidth();
        int height1 = bimg1.getHeight();

        String imgFile1 = img1.getName();

        int imgFormat1 = getImageFormat(imgFile1);

        r.setText(imgFile1);
        r.addBreak();
        try {
            r.addPicture(new FileInputStream(img1), imgFormat1, imgFile1, Units.toEMU(width1), Units.toEMU(height1));
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        r.addBreak();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(wordFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static int getImageFormat(String imgFile) {
        int format;
        if (imgFile.endsWith(".emf"))
            format = XWPFDocument.PICTURE_TYPE_EMF;
        else if (imgFile.endsWith(".wmf"))
            format = XWPFDocument.PICTURE_TYPE_WMF;
        else if (imgFile.endsWith(".pict"))
            format = XWPFDocument.PICTURE_TYPE_PICT;
        else if (imgFile.endsWith(".jpeg") || imgFile.endsWith(".jpg"))
            format = XWPFDocument.PICTURE_TYPE_JPEG;
        else if (imgFile.endsWith(".png"))
            format = XWPFDocument.PICTURE_TYPE_PNG;
        else if (imgFile.endsWith(".dib"))
            format = XWPFDocument.PICTURE_TYPE_DIB;
        else if (imgFile.endsWith(".gif"))
            format = XWPFDocument.PICTURE_TYPE_GIF;
        else if (imgFile.endsWith(".tiff"))
            format = XWPFDocument.PICTURE_TYPE_TIFF;
        else if (imgFile.endsWith(".eps"))
            format = XWPFDocument.PICTURE_TYPE_EPS;
        else if (imgFile.endsWith(".bmp"))
            format = XWPFDocument.PICTURE_TYPE_BMP;
        else if (imgFile.endsWith(".wpg"))
            format = XWPFDocument.PICTURE_TYPE_WPG;
        else {
            return 0;
        }
        return format;
    }

    private void addParagraph(String text) {
        XWPFParagraph tmpParagraph = document.createParagraph();
        XWPFRun tmpRun = tmpParagraph.createRun();

        tmpRun.setText(text);

        try {
            File file = new File(wordFilePath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            document.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void stop(){
        driver.quit();
        driver = null;

        try {
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}