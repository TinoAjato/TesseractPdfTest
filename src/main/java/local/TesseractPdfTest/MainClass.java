package local.TesseractPdfTest;

import java.awt.Rectangle;
import java.io.File;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class MainClass {
	
	public static void main(String[] args) throws TesseractException {
		System.out.println("TST");
		
		File image = new File("src/main/resources/test/ИдеальнаяКартинка." + "tif");//jpeg png tif
//		File image = new File("D:/tst.png");
		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath("src/main/resources/tessdata");
		tesseract.setLanguage("rus");
		tesseract.setPageSegMode(3);
		tesseract.setOcrEngineMode(1);
		String result = tesseract.doOCR(image);
//		String result = tesseract.doOCR(image, new Rectangle(795, 175, 180, 30));
		
		System.out.println(result);
	}
	
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