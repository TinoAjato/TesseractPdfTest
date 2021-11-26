package local.TesseractPdfTest;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class MainClass {
	
	private static String absPath = "D:/Users/beliaev/Downloads/Кривые ТН/";
	private static int[] coords;
	private static ArrayList<int[]> coordsInStamp;
	private static List<ImageType> imageTypes = Arrays.asList(ImageType.ARGB);//, ImageType.BINARY, ImageType.GRAY, ImageType.RGB);
	
	private static int[] pageSegMode = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
	
	public static void main(String[] args) {
		long start = System.nanoTime();
		System.out.println("TST");
		
//		try {
//			FileOutputStream fos = new FileOutputStream(new File(absPath  + "output/" + "resultStat.xlsx"));
//			XSSFWorkbook  workbook = new XSSFWorkbook();            
			
//			for(int psm : pageSegMode) {
//				XSSFSheet sheet = workbook.createSheet(psm + " SegMode");  
//				
//				Row row = sheet.createRow(0);
//				int indexCell = 0;
//				for(ImageType it : imageTypes) {
//					Cell cell = row.createCell(indexCell++);
//					cell.setCellValue(it + "");
//				}
				
				try {
					run(null, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
//			}
			
//			workbook.write(fos);
//			fos.flush();
//			fos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
//		File image = new File("src/main/resources/test/ИдеальнаяКартинка." + "tif");//jpeg png tif
////		File image = new File("D:/tst.png");
//		Tesseract tesseract = new Tesseract();
//		tesseract.setDatapath("src/main/resources/tessdata");
//		tesseract.setLanguage("rus");
//		tesseract.setPageSegMode(3);
//		tesseract.setOcrEngineMode(1);
//		String result = tesseract.doOCR(image);
////		String result = tesseract.doOCR(image, new Rectangle(795, 175, 180, 30));
//		System.out.println(result);
		
		long elapsed = System.nanoTime() - start;
		System.out.println("Время работы программы, с: " + elapsed / 1000000000);
	}
	
	private static void run(XSSFSheet sheet, int psm) throws IOException {
		
		PDDocument document = PDDocument.load(new File(absPath + "af.pdf"));//tstPdf
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		
		ITesseract tesseract = new Tesseract();
		
		tesseract.setDatapath("src/main/resources/tessdata");
		tesseract.setLanguage("rus");
		tesseract.setPageSegMode(psm);
		tesseract.setOcrEngineMode(2);
		tesseract.setTessVariable("user_defined_dpi", "300");
		
		int lastPage = document.getNumberOfPages();
		
		
		for (int page = 0; page < lastPage; page++) {
			System.out.println("**** page = " + page + " ****");
			
//			StringBuffer result = new StringBuffer();
			
//			Row row = sheet.createRow(page+1);
//			int indexCell = 0;
			
			for(ImageType it : imageTypes) {
				
				coordsInStamp = new ArrayList<int[]>();
				
				//получаем картинку в 300DPI и в градации серого
				BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, it);
				
				//если высота листа больше его ширины, то это портретная ориентация
				if(bufferedImage.getHeight() > bufferedImage.getWidth()) {
					//координаты всего штампа
					coords = new int[]{180, 375, 2150, 255};//размер 2150 на 255 пикселей
					
					//прямоугольник серии
					coordsInStamp.add(new int[]{0, 0, 260, 110});
	//				//прямоугольник наименования типа
	//				coordsInStamp.add(new int[]{220, 70, 800, 160});
	//				//прямоугольник номера штрихкода
	//				coordsInStamp.add(new int[]{1060, 125, 510, 110});
				} else {
					//координаты всего штампа
					coords = new int[]{180, 265, 3150, 280};//размер 3150 на 280 пикселей
					
					//прямоугольник серии
					coordsInStamp.add(new int[]{0, 0, 250, 125});
	//				//прямоугольник наименования типа
	//				coordsInStamp.add(new int[]{250, 60, 780, 190});
	//				//прямоугольник номера штрихкода
	//				coordsInStamp.add(new int[]{1030, 125, 570, 125});
				}
				
				//получаем часть изображения где идет указание серии, типа и штрих-код
				BufferedImage partOfBufferedImage = deepCopy(bufferedImage.getSubimage(coords[0], coords[1], coords[2], coords[3]));
				
				areasMarkup(partOfBufferedImage, coordsInStamp);
				
				String res = "";
				
//				try {
//					for(int[] coordInStamp : coordsInStamp) {
//						String text = tesseract.doOCR(partOfBufferedImage, new Rectangle(coordInStamp[0], coordInStamp[1], coordInStamp[2], coordInStamp[3]));
//						
//						res += text.replaceAll("[^\\da-zA-Zа-яёА-ЯЁ]", "");
//	//					System.out.print(Arrays.toString(coordInStamp) + "; " + res);
//						result.append(it + ": " + res + "; ");
//						
////						Cell cell = row.createCell(indexCell++);
////						cell.setCellValue(res);
//					}
//				} catch (TesseractException ex) {
//					ex.printStackTrace();
//				}
				
				ImageIO.write(partOfBufferedImage, "png", new File(absPath + "output/" + page + "_" + it + "_" + res + ".png"));
			}
//			System.out.println(result.toString());
		}
		document.close();
		
	}
	
	private static void areasMarkup(BufferedImage partOfBufferedImage, ArrayList<int[]> coordsInStamp) {
		Graphics2D g2d = partOfBufferedImage.createGraphics();
		g2d.setColor(Color.red);
		
		for(int[] coordInStamp : coordsInStamp)
			g2d.drawRect(coordInStamp[0], coordInStamp[1], coordInStamp[2], coordInStamp[3]);
		
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
	1 Автоматическая сегментация страниц с экранным меню.
	2 Автоматическая сегментация страниц, но без OSD или OCR. (не реализована)
	3 Полностью автоматическая сегментация страниц, но без экранного меню. (Дефолт)
	4 Предположим, что один столбец текста переменного размера.
	5 Предположим, что это единый однородный блок вертикально выровненного текста.
	6 Предположим, что это один однородный блок текста.
	7 Рассматривайте изображение как одну текстовую строку.
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
}