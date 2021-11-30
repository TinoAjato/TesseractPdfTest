package local.TesseractPdfTest;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class MainClass {
	
	private static String absPath = "D:/Users/beliaev/Downloads/Кривые ТН/";
	private static int[] coords;
	private static ArrayList<Rectangle> coordsInStandardStamp = new ArrayList<Rectangle>();
	private static List<ImageType> imageTypes = Arrays.asList(/*ImageType.ARGB, */ImageType.RGB/*, ImageType.GRAY, ImageType.BINARY*/);
	
	private static int[] pageSegMode = new int[]{/*0, 1, 2, 3, */4/*, 5*/, 6/*, 7, 8, 9, 10, 11, 12, 13*/};
	
	public static void main(String[] args) {
		long start = System.nanoTime();
		
		for(int psm : pageSegMode) {
			try {
				run(psm);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		long elapsed = System.nanoTime() - start;
		System.out.println("Время работы программы, с: " + elapsed / 1000000000);
	}
	
	private static void run(int psm) throws IOException {
		
		PDDocument document = PDDocument.load(new File(absPath + "af.pdf"));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		
		ITesseract tesseract = new Tesseract();
		
		tesseract.setDatapath("src/main/resources/tessdata");
		tesseract.setLanguage("rus");
		tesseract.setPageSegMode(psm);
		tesseract.setOcrEngineMode(2);
		tesseract.setTessVariable("user_defined_dpi", "300");
		
		int lastPage = document.getNumberOfPages();
		
		
		for (int page = 0; page < lastPage; page++) {
			System.out.println("**** page = " + page + " **** segMode = " + psm);
			
			for(ImageType it : imageTypes) {
				
				//получаем картинку в 300DPI и в градации серого
				BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, it);
				
				bufferedImage = convertBufferedImageToBINARY(bufferedImage);
				
				//если высота листа больше его ширины, то это портретная ориентация
				if(bufferedImage.getHeight() > bufferedImage.getWidth()) {
					//портретная ориентация
					coords = new int[]{
							180, 375, 2150, 255,//область стандартного штампа
							885, 150, 965, 230,//область УНП
							0, 595, 2300, 495,//область реквизитов
					};
					
					//прямоугольник серии
					coordsInStandardStamp.add(new Rectangle(0, 0, 260, 110));
					//прямоугольник наименования типа
					coordsInStandardStamp.add(new Rectangle(220, 70, 800, 160));
					//прямоугольник номера штрихкода
					coordsInStandardStamp.add(new Rectangle(1060, 125, 510, 110));
				} else {
					//альбомная ориентация
					coords = new int[]{
							180, 265, 3150, 280,//область стандартного штампа
							1375, 80, 1100, 200,//область УНП
							885, 150, 965, 230,//область реквизитов
					};
					
					//прямоугольник серии
					coordsInStandardStamp.add(new Rectangle(0, 0, 260, 110));
					//прямоугольник наименования типа
					coordsInStandardStamp.add(new Rectangle(220, 70, 1820, 160));
					//прямоугольник номера штрихкода
					coordsInStandardStamp.add(new Rectangle(2070, 160, 510, 110));
				}
				
				//получаем часть изображения где идет указание серии, типа и штрих-код
				BufferedImage partOfBufferedImage1 = deepCopy(bufferedImage.getSubimage(coords[0], coords[1], coords[2], coords[3]));
				
				//получаем часть изображения где идет УНП грузополучателя
				BufferedImage partOfBufferedImage2 = deepCopy(bufferedImage.getSubimage(coords[4], coords[5], coords[6], coords[7]));
				
				//получаем часть изображения с реквизитами
				BufferedImage partOfBufferedImage3 = deepCopy(bufferedImage.getSubimage(coords[8], coords[9], coords[10], coords[11]));
				
//				//обводка областей стандартного штампа, где идет определение
//				for(int[] coordInStandardStamp : coordsInStandardStamp)
//					areasMarkup(partOfBufferedImage1, coordInStandardStamp);
				
				try {
					System.out.println("Область стандартного штампа");
					
					String seriesText = tesseract.doOCR(partOfBufferedImage1, coordsInStandardStamp.get(0));//segMod 6, BINARY
					seriesText = seriesText.toLowerCase().replaceAll("[^а-яё]", "").replaceAll("серия", "");
					if(seriesText.length() > 0)
						System.out.println("Серия: " + seriesText);
					else
						System.out.println("Серия: отсутствует");
					
					String type = tesseract.doOCR(partOfBufferedImage1, coordsInStandardStamp.get(1));//segMod 6, BINARY
					type = type.toLowerCase().replaceAll("[^а-яё]", "");
					if(type.length() > 0)
						System.out.println("Тип: " + type);
					else
						System.out.println("Тип: отсутствует");
					
					String barcode = tesseract.doOCR(partOfBufferedImage1, coordsInStandardStamp.get(2));//segMod 6, BINARY
					barcode = barcode.replaceAll("[^\\d]", "");
					if(barcode.length() > 0)
						System.out.println("Штрихкод: " + barcode);
					else
						System.out.println("Штрихкод: отсутствует");
					
					
					System.out.println("Область УНП");
					
					String unpAreaText = tesseract.doOCR(partOfBufferedImage2);//segMod 4, GRAY
					unpAreaText = unpAreaText.toLowerCase();
					Pattern pattern1 = Pattern.compile("(\\d{12}|\\d{10}|\\d{9}|[авсенкмabcehkm]{1}\\d{8}).+?(\\d{12}|\\d{10}|\\d{9}|[авсенкмabcehkm]{1}\\d{8})");
					Matcher matcher1 = pattern1.matcher(unpAreaText);
					if(matcher1.find())
						System.out.println("УНП грузополучателя: " + matcher1.group(2));
					else
						System.out.println("УНП грузополучателя: отсутствует");
					
					
					System.out.println("Область реквизитов");
					
					String requisitesAreaText = tesseract.doOCR(partOfBufferedImage3);//segMod 6, BINARY
					requisitesAreaText = requisitesAreaText.toLowerCase();
					Pattern pattern2 = Pattern.compile("(?:атель)(.+?)(?:\\n)");
					Matcher matcher2 = pattern2.matcher(requisitesAreaText);
					if(matcher2.find())
						System.out.println("Наименование грузополучателя: " + matcher2.group(1));
					else
						System.out.println("Наименование грузополучателя: отсутствует");
					
				} catch (TesseractException ex) {
					ex.printStackTrace();
				}
				
//				ImageIO.write(partOfBufferedImage1, "png", new File(absPath + "output/stamp1_" + page + "_" + it + ".png"));
//				ImageIO.write(partOfBufferedImage2, "png", new File(absPath + "output/stamp2_" + page + "_" + it + ".png"));
//				ImageIO.write(partOfBufferedImage3, "png", new File(absPath + "output/stamp3_" + page + "_" + it + ".png"));
			}
			System.out.println();
		}
		document.close();
	}
	
	@SuppressWarnings("unused")
	private static void areasMarkup(BufferedImage partOfBufferedImage, int[] _coords) {
		Graphics2D g2d = partOfBufferedImage.createGraphics();
		g2d.setColor(Color.BLACK);
		
		g2d.drawRect(_coords[0], _coords[1], _coords[2], _coords[3]);
		
		g2d.dispose();
	}
	
	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
/*https://stackoverflow.com/questions/57160892/tesseract-error-warning-invalid-resolution-0-dpi-using-70-instead
PageSegMode - Режимы сегментации страницы:
	0 Только ориентация и обнаружение сценария (OSD).
	+1 Автоматическая сегментация страниц с экранным меню.
	+2 Автоматическая сегментация страниц, но без OSD или OCR. (не реализована)
	+3 Полностью автоматическая сегментация страниц, но без экранного меню. (Дефолт)
	+4 Предположим, что один столбец текста переменного размера.
	5 Предположим, что это единый однородный блок вертикально выровненного текста.
	+6 Предположим, что это один однородный блок текста.
	+7 Рассматривайте изображение как одну текстовую строку.
	8 Относитесь к изображению как к одному слову.
	9 Рассматривайте изображение как отдельное слово в круге.
	10 Относитесь к изображению как к одному символу.
	11 Скудный текст. Найдите как можно больше текста в произвольном порядке.
	12 Разреженный текст с экранным меню.
	13 Необработанная линия. Рассматривайте изображение как одну текстовую строку, обход хаков, специфичных для Tesseract.
	
OcrEngineMode - Режимы OCR Engine:
	0 Только устаревший движок.
	1 Нейронные сети Только движок LSTM.
	2 движка Legacy + LSTM.
	3 По умолчанию, в зависимости от того, что доступно.
*/
	
	/**Так качество чуть лучше...*/
	public static BufferedImage convertBufferedImageToBINARY(BufferedImage bufferedImage) {
		return binaryImage(grayImage(bufferedImage));
	}
	
	/**Оттенки серого*/
	public static BufferedImage grayImage(BufferedImage srcImage) {
		return copyImage(srcImage, new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY));
	}
	/**Бинаризация*/
	public static BufferedImage binaryImage(BufferedImage srcImage) {
		return copyImage(srcImage, new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY));
	}
	public static BufferedImage copyImage(BufferedImage srcImage, BufferedImage destImage) {
		for (int y = 0; y < srcImage.getHeight(); y++) {
			for (int x = 0; x < srcImage.getWidth(); x++) {
				destImage.setRGB(x, y, srcImage.getRGB(x, y));
			}
		}
		return destImage;
	}
	
}