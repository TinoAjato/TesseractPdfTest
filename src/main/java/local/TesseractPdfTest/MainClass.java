package local.TesseractPdfTest;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import local.TesseractPdfTest.dto.ConsignmentNoteAfterBatchProcessing;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class MainClass {
	
	private static String absPath = "F:/TesseractTest/tn/";
	
	/**портретная ориентация*/
	private static final Rectangle standardStampArea_Series_p = new Rectangle(180, 375, 260, 110);
	private static final Rectangle standardStampArea_TypeName_p = new Rectangle(400, 430, 800, 160);
	private static final Rectangle unpArea_p = new Rectangle(885, 150, 965, 230);
	private static final Rectangle requisitesArea_p = new Rectangle(0, 595, 2300, 495);
	
	/**альбомная ориентация*/
	private static final Rectangle standardStampArea_Series_l = new Rectangle(180, 265, 260, 110);
	private static final Rectangle standardStampArea_TypeName_l = new Rectangle(400, 335, 1820, 160);
	private static final Rectangle unpArea_l = new Rectangle(1375, 80, 1100, 200);
	private static final Rectangle requisitesArea_l = new Rectangle(0, 470, 3300, 350);
	
	
	public static void main(String[] args) {
		long start = System.nanoTime();
		
		run();
		
		long elapsed = System.nanoTime() - start;
		System.out.println("Время работы программы, с: " + elapsed / 1000000000);
	}
	
	private static void run() {
		
		try(PDDocument document = PDDocument.load(new File(absPath + "af_.pdf"));) {
			
			List<ConsignmentNoteAfterBatchProcessing> consignmentNotesAbp = new ArrayList<ConsignmentNoteAfterBatchProcessing>();
			
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			
			ITesseract tesseract = new Tesseract();
			tesseract.setDatapath("src/main/resources/tessdata");
			tesseract.setLanguage("rus");
			tesseract.setPageSegMode(4);
			tesseract.setOcrEngineMode(2);
			tesseract.setTessVariable("user_defined_dpi", "300");
			
			int lastPage = document.getNumberOfPages();
			
			for (int page = 0; page < lastPage; page++) {
				try {
					System.out.println("**** page = " + page);
					
					ConsignmentNoteAfterBatchProcessing cnAbp = new ConsignmentNoteAfterBatchProcessing(page+1);
					
					//получаем картинку в 300DPI и в RGB
					BufferedImage binaryBufferedImage = convertBufferedImageToBINARY(pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB));
					
					/*Координаты областей распознования*/
					Rectangle standardStampArea_Series;
					Rectangle standardStampArea_TypeName;
					Rectangle unpArea;
					Rectangle requisitesArea;
					
					//если высота листа больше его ширины, то это портретная ориентация
					if(binaryBufferedImage.getHeight() > binaryBufferedImage.getWidth()) {
						standardStampArea_TypeName = standardStampArea_TypeName_p;
						standardStampArea_Series = standardStampArea_Series_p;
						unpArea = unpArea_p;
						requisitesArea = requisitesArea_p;
					} else {
						standardStampArea_TypeName = standardStampArea_TypeName_l;
						standardStampArea_Series = standardStampArea_Series_l;
						unpArea = unpArea_l;
						requisitesArea = requisitesArea_l;
					}
					
					try {
						/*ТИП*/
						String type = tesseract.doOCR(binaryBufferedImage, standardStampArea_TypeName);
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
						String seriesText = tesseract.doOCR(binaryBufferedImage, standardStampArea_Series);
						seriesText = seriesText.toLowerCase().replaceAll("[^а-яё]|(сер[ин]я)", "");
						if(seriesText.length() > 0) {
							cnAbp.setConsignmentSeries(seriesText);
						} else {
							cnAbp.setConsignmentSeries("");
						}
						
					} catch (TesseractException ex) {
						ex.printStackTrace();
					}
					
					try {
						/*УНП*/
						String unpAreaText = tesseract.doOCR(binaryBufferedImage, unpArea);
						unpAreaText = unpAreaText.toLowerCase();
						Pattern pattern1 = Pattern.compile("(\\d{12}|\\d{10}|\\d{9}|[авсенкмabcehkm]{1}\\d{8})(?:.*?|\\s*?)(\\d{12}|\\d{10}|\\d{9}|[авсенкмabcehkm]{1}\\d{8})");
						Matcher matcher1 = pattern1.matcher(unpAreaText);
						if(matcher1.find()) {
							cnAbp.setConsigneeUPN(matcher1.group(2));
						} else {
							cnAbp.setConsigneeUPN("");
						}
						
					} catch (TesseractException ex) {
						ex.printStackTrace();
					}
					
					try {
						String requisitesAreaText = tesseract.doOCR(binaryBufferedImage, requisitesArea);
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
						
					} catch (TesseractException ex) {
						ex.printStackTrace();
					}
					
					consignmentNotesAbp.add(cnAbp);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			PDFMergerUtility mergerUtility = new PDFMergerUtility();
			for(ConsignmentNoteAfterBatchProcessing cnAbp : consignmentNotesAbp) {
				
				try(PDDocument resultDocument = new PDDocument();) {
					Path path = getPath(cnAbp, "__split.pdf");
					
					/*Делим исходную PDF*/
					Splitter splitter = new Splitter();
					splitter.setStartPage(cnAbp.getSplitterStartPage());
					splitter.setEndPage(cnAbp.getSplitterEndPage());
					splitter.setSplitAtPage(cnAbp.getSplitterAtPage());
					
					List<PDDocument> splitList = splitter.split(document);
					
					for(PDDocument splitDocument : splitList) {
						try {
							mergerUtility.appendDocument(resultDocument, splitDocument);
							splitDocument.close();
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if(resultDocument.getNumberOfPages() < 1)
						resultDocument.addPage(new PDPage());
					
					resultDocument.save(path.toFile());
					
					cnAbp.setSplitPdfFilePath(path.toFile().getAbsolutePath());
					
					resultDocument.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			consignmentNotesAbp.forEach(System.out::println);
			
			document.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Абсолютный путь к файлу*/
	private static Path getPath(ConsignmentNoteAfterBatchProcessing consignmentNoteAfterBatchProcessing, String name) {
		return Paths.get(absPath + "output/" + consignmentNoteAfterBatchProcessing.getUuid() + name);
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