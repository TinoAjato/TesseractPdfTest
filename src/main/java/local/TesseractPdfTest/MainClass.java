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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import local.TesseractPdfTest.dto.ConsignmentNoteAfterBatchProcessing;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class MainClass {
	
	private static String absPath = "F:/TesseractTest/ttn/";
	
	private static final int scl = 1;
	
	private static List<ConsignmentNoteAfterBatchProcessing> consignmentNotesAbp = new ArrayList<ConsignmentNoteAfterBatchProcessing>();
	
	
	public static void main(String[] args) {
		long start = System.nanoTime();
		
		try {
			run();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long elapsed = System.nanoTime() - start;
		System.out.println("Время работы программы, с: " + elapsed / 1000000000);
	}
	
	private static void run() throws IOException {
		
		PDDocument document = PDDocument.load(new File(absPath + "af_.pdf"));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		
		ITesseract tesseract = new Tesseract();
		tesseract.setDatapath("src/main/resources/tessdata");
		tesseract.setLanguage("rus");
		tesseract.setPageSegMode(4);
		tesseract.setOcrEngineMode(2);
		tesseract.setTessVariable("user_defined_dpi", "300");//300*scl
		
		int lastPage = document.getNumberOfPages();
		
		for (int page = 0; page < lastPage; page++) {
			System.out.println("**** page = " + page);
			
			ConsignmentNoteAfterBatchProcessing cnAbp = new ConsignmentNoteAfterBatchProcessing(page+1);
			
			//получаем картинку в 300DPI и в градации серого
			BufferedImage binaryBufferedImage = convertBufferedImageToBINARY(
					pdfRenderer.renderImageWithDPI(page, 300*scl, ImageType.RGB));
			
			int[] coords;
			ArrayList<Rectangle> coordsInStandardStamp = new ArrayList<Rectangle>();
			
			//если высота листа больше его ширины, то это портретная ориентация
			if(binaryBufferedImage.getHeight() > binaryBufferedImage.getWidth()) {
				//портретная ориентация
				coords = new int[]{
						180*scl, 375*scl, 2150*scl, 255*scl,//область стандартного штампа
						885*scl, 150*scl, 965*scl, 230*scl,//область УНП
						0*scl, 595*scl, 2300*scl, 495*scl,//область реквизитов
				};
				
				//прямоугольник серии
				coordsInStandardStamp.add(new Rectangle(0*scl, 0*scl, 260*scl, 110*scl));
				//прямоугольник наименования типа
				coordsInStandardStamp.add(new Rectangle(220*scl, 55*scl, 800*scl, 160*scl));//70
				//прямоугольник номера штрихкода
				coordsInStandardStamp.add(new Rectangle(1060*scl, 125*scl, 510*scl, 110*scl));
			} else {
				//альбомная ориентация
				coords = new int[]{
						180*scl, 265*scl, 3150*scl, 280*scl,//область стандартного штампа
						1375*scl, 80*scl, 1100*scl, 200*scl,//область УНП
						0*scl, 470*scl, 3300*scl, 350*scl,//область реквизитов
				};
				
				//прямоугольник серии
				coordsInStandardStamp.add(new Rectangle(0*scl, 0*scl, 260*scl, 110*scl));
				//прямоугольник наименования типа
				coordsInStandardStamp.add(new Rectangle(220*scl, 70*scl, 1820*scl, 160*scl));
				//прямоугольник номера штрихкода
				coordsInStandardStamp.add(new Rectangle(2070*scl, 160*scl, 510*scl, 110*scl));
			}
			
			
			try {
				System.out.println("Область стандартного штампа");
				
				//получаем часть изображения где идет указание серии, типа и штрих-код
				BufferedImage partOfBufferedImage1 = deepCopy(binaryBufferedImage.getSubimage(coords[0], coords[1], coords[2], coords[3]));
				
				/*ТИП*/
				String type = tesseract.doOCR(partOfBufferedImage1, coordsInStandardStamp.get(1));
				type = type.toLowerCase().replaceAll("[^а-яё]", "");
				
				//с помощью коэффициента Жаккарда отпределяем к какому типу относится накладная
				double jaccardIndex_tn = new JaccardSimilarity().apply("товарнаянакладная", type);
				double jaccardIndex_ttn = new JaccardSimilarity().apply("товарнотранспортнаянакладная", type);
				
				//если больше 0.61 то достоверно, иначе не ТН и не ТТН
				if(Double.max(jaccardIndex_tn, jaccardIndex_ttn) > 0.61) {
					if(Double.compare(jaccardIndex_tn, jaccardIndex_ttn) >= 0) {
						cnAbp.setDocumentTypeName("ТН");
					} else {
						cnAbp.setDocumentTypeName("ТТН");
					}
				} else {
					int cnAbpLastIndex = consignmentNotesAbp.size() - 1;
					if(cnAbpLastIndex >= 0) {
						consignmentNotesAbp.get(cnAbpLastIndex).setSplitterEndPage(consignmentNotesAbp.get(cnAbpLastIndex).getSplitterEndPage() + 1);
					}
					continue;
				}
				
				/*СЕРИЯ*/
				String seriesText = tesseract.doOCR(partOfBufferedImage1, coordsInStandardStamp.get(0));
				seriesText = seriesText.toLowerCase().replaceAll("[^а-яё]|(сер[ин]я)", "");
				if(seriesText.length() > 0) {
					cnAbp.setConsignmentSeries(seriesText);
				} else {
					cnAbp.setConsignmentSeries("");
				}
				
				/*ШТРИХКОД*/
//				String barcode = tesseract.doOCR(partOfBufferedImage1, coordsInStandardStamp.get(2));
//				barcode = barcode.replaceAll("[^\\d]", "");
//				if(barcode.length() > 0)
//					System.out.println("Штрихкод: " + barcode);
//				else
//					System.out.println("Штрихкод: отсутствует");
				
				//обводка областей стандартного штампа, где идет определение
				for(Rectangle coordInStandardStamp : coordsInStandardStamp)
					areasMarkup(partOfBufferedImage1, coordInStandardStamp);
				
				cnAbp.setArea1Path(absPath + "output/" + cnAbp.getUuid() + "_area1.png");
				
				ImageIO.write(partOfBufferedImage1, "png", new File(cnAbp.getArea1Path()));
				
			} catch (TesseractException ex) {
				ex.printStackTrace();
			}
			
			try {
				System.out.println("Область УНП");
				
				//получаем часть изображения где идет УНП грузополучателя
				BufferedImage partOfBufferedImage2 = deepCopy(binaryBufferedImage.getSubimage(coords[4], coords[5], coords[6], coords[7]));
				
				/*УНП*/
				String unpAreaText = tesseract.doOCR(partOfBufferedImage2);
				unpAreaText = unpAreaText.toLowerCase();
				Pattern pattern1 = Pattern.compile("(\\d{12}|\\d{10}|\\d{9}|[авсенкмabcehkm]{1}\\d{8})(?:.*?|\\s*?)(\\d{12}|\\d{10}|\\d{9}|[авсенкмabcehkm]{1}\\d{8})");
				Matcher matcher1 = pattern1.matcher(unpAreaText);
				if(matcher1.find()) {
					cnAbp.setConsigneeUPN(matcher1.group(2));
				} else {
					cnAbp.setConsigneeUPN("");
				}
				
				cnAbp.setArea2Path(absPath + "output/" + cnAbp.getUuid() + "_area2.png");
				
				ImageIO.write(partOfBufferedImage2, "png", new File(cnAbp.getArea2Path()));
				
			} catch (TesseractException ex) {
				ex.printStackTrace();
			}
			
			try {
				System.out.println("Область реквизитов");
				
				//получаем часть изображения с реквизитами
				BufferedImage partOfBufferedImage3 = deepCopy(binaryBufferedImage.getSubimage(coords[8], coords[9], coords[10], coords[11]));
				
				String requisitesAreaText = tesseract.doOCR(partOfBufferedImage3);
				requisitesAreaText = requisitesAreaText.toLowerCase();
				
				/*ДАТА*/
				Pattern pattern2_2 = Pattern.compile("(0?[1-9]|[1-2][0-9]|3[01])(?:(?!0?[1-9]|[1-2][0-9]|3[01]).)*(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря){1}.*?(20[0-9]?[0-9]?)");
				Matcher matcher2_2 = pattern2_2.matcher(requisitesAreaText);
				if(matcher2_2.find()) {
					cnAbp.setDate(matcher2_2.group(1) + "." + matcher2_2.group(2) + "." + matcher2_2.group(3));
				} else {
					cnAbp.setDate("");
				}
				
				/*НАИМЕНОВАНИЕ ГРУЗОПОЛУЧАТЕЛЯ*/
				Pattern pattern2 = Pattern.compile("(?:чатель)(.+?)(?:\\n)");
				Matcher matcher2 = pattern2.matcher(requisitesAreaText);
				if(matcher2.find()) {
					cnAbp.setConsigneeName(matcher2.group(1));
				} else {
					cnAbp.setConsigneeName("");
				}
				
				cnAbp.setArea3Path(absPath + "output/" + cnAbp.getUuid() + "_area3.png");
				
				ImageIO.write(partOfBufferedImage3, "png", new File(cnAbp.getArea3Path()));
				
			} catch (TesseractException ex) {
				ex.printStackTrace();
			}
			
			consignmentNotesAbp.add(cnAbp);
		}
		
		PDFMergerUtility mergerUtility = new PDFMergerUtility();
		for(ConsignmentNoteAfterBatchProcessing cnAbp : consignmentNotesAbp) {
			/*Делим исходную PDF*/
			Splitter splitter = new Splitter();
			splitter.setStartPage(cnAbp.getSplitterStartPage());
			splitter.setEndPage(cnAbp.getSplitterEndPage());
			splitter.setSplitAtPage(cnAbp.getSplitterAtPage());
			
			List<PDDocument> splitList = splitter.split(document);
			
			cnAbp.setSplitPdfFilePath(absPath + "output/" + cnAbp.getUuid() + "_split_file.pdf");
			
			PDDocument resultDocument = new PDDocument();
			
			for(PDDocument splitDocument : splitList) {
				mergerUtility.appendDocument(resultDocument, splitDocument);
				splitDocument.close();
			}
			
			resultDocument.save(cnAbp.getSplitPdfFilePath());
			resultDocument.close();
		}
		
		consignmentNotesAbp.forEach(System.out::println);
		
		document.close();
	}
	
	/**Рисуем прямоугольник на переданном изображении*/
	private static void areasMarkup(BufferedImage partOfBufferedImage, Rectangle coordInStandardStamp) {
		Graphics2D g2d = partOfBufferedImage.createGraphics();
		g2d.setColor(Color.BLACK);
		g2d.drawRect(coordInStandardStamp.x,coordInStandardStamp.y, coordInStandardStamp.width, coordInStandardStamp.height);
		g2d.dispose();
	}
	
	/**Конвертируем цветное изображение в черно-белое. Так качество чуть лучше...*/
	public static BufferedImage convertBufferedImageToBINARY(BufferedImage bufferedImage) {
		return binaryImage(grayImage(bufferedImage));
	}
	
	/**Возвращаем изображение в оттенках серого*/
	public static BufferedImage grayImage(BufferedImage srcImage) {
		return copyImage(srcImage, new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY));
	}
	
	/**Возвращаем черно-белое изображение*/
	public static BufferedImage binaryImage(BufferedImage srcImage) {
		return copyImage(srcImage, new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY));
	}
	
	/**Возвращает копию изображения*/
	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	/**Возвращает копию изображения*/
	public static BufferedImage copyImage(BufferedImage srcImage, BufferedImage destImage) {
		for (int y = 0; y < srcImage.getHeight(); y++) {
			for (int x = 0; x < srcImage.getWidth(); x++) {
				destImage.setRGB(x, y, srcImage.getRGB(x, y));
			}
		}
		return destImage;
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
}