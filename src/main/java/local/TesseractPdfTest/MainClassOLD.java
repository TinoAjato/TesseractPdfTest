package local.TesseractPdfTest;

public class MainClassOLD {
	
//	private static String absPath = "D:/Users/beliaev/Downloads/Кривые ТН/";
//	private static int[] coords;
//	private static ArrayList<int[]> coordsInStandardStamp;
//	private static List<ImageType> imageTypes = Arrays.asList(/*ImageType.ARGB, ImageType.RGB, */ImageType.GRAY/*, ImageType.BINARY*/);
//	
//	private static int[] pageSegMode = new int[]{/*0, 1, 2, 3, */4/*, 5, 6, 7, 8, 9, 10, 11, 12, 13*/};
	
//	public static void main(String[] args) {
//		long start = System.nanoTime();
//		System.out.println("TST");
//		
////		try {
////			FileOutputStream fos = new FileOutputStream(new File(absPath  + "output/" + "!resultStat.xlsx"));
////			XSSFWorkbook  workbook = new XSSFWorkbook();            
////			
//			for(int psm : pageSegMode) {
////				XSSFSheet sheet = workbook.createSheet(psm + " SegMode");  
////				
////				Row row = sheet.createRow(0);
////				int indexCell = 0;
////				for(ImageType it : imageTypes) {
////					Cell cell = row.createCell(indexCell++);
////					cell.setCellValue(it + "");
////					indexCell++;
////				}
//				
//				try {
//					run(null, psm);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
////			
////			workbook.write(fos);
////			fos.flush();
////			fos.close();
////		} catch (FileNotFoundException e) {
////			e.printStackTrace();
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//		
////		File image = new File("src/main/resources/test/ИдеальнаяКартинка." + "tif");//jpeg png tif
//////		File image = new File("D:/tst.png");
////		Tesseract tesseract = new Tesseract();
////		tesseract.setDatapath("src/main/resources/tessdata");
////		tesseract.setLanguage("rus");
////		tesseract.setPageSegMode(3);
////		tesseract.setOcrEngineMode(1);
////		String result = tesseract.doOCR(image);
//////		String result = tesseract.doOCR(image, new Rectangle(795, 175, 180, 30));
////		System.out.println(result);
//		
//		long elapsed = System.nanoTime() - start;
//		System.out.println("Время работы программы, с: " + elapsed / 1000000000);
//	}
//	
//	private static void run(XSSFSheet sheet, int psm) throws IOException {
//		
//		PDDocument document = PDDocument.load(new File(absPath + "af.pdf"));//tstPdf
//		PDFRenderer pdfRenderer = new PDFRenderer(document);
//		
//		ITesseract tesseract = new Tesseract();
//		
//		tesseract.setDatapath("src/main/resources/tessdata");
//		tesseract.setLanguage("rus");
//		tesseract.setPageSegMode(psm);
//		tesseract.setOcrEngineMode(2);
//		tesseract.setTessVariable("user_defined_dpi", "300");
////		tesseract.setTessVariable("tessedit_char_whitelist", "0123456789 ");
//		
//		int lastPage = document.getNumberOfPages();
//		
//		
//		for (int page = 0; page < lastPage; page++) {
//			System.out.println("**** page = " + page + " **** segMode = " + psm);
//			
////			StringBuffer result = new StringBuffer();
//			
////			Row row = sheet.createRow(page+1);
////			int indexCell = 0;
//			
//			for(ImageType it : imageTypes) {
//				
//				coordsInStandardStamp = new ArrayList<int[]>();
//				
//				//получаем картинку в 300DPI и в градации серого
//				BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, it);
//				
//				//если высота листа больше его ширины, то это портретная ориентация
//				if(bufferedImage.getHeight() > bufferedImage.getWidth()) {
//					//портретная ориентация
//					coords = new int[]{
//							180, 375, 2150, 255,//область стандартного штампа
//							885, 150, 965, 230,//область УНП
//					};
//					
//					//прямоугольник серии
//					coordsInStandardStamp.add(new int[]{0, 0, 260, 110});
//					//прямоугольник наименования типа
//					coordsInStandardStamp.add(new int[]{220, 70, 800, 160});
//					//прямоугольник номера штрихкода
//					coordsInStandardStamp.add(new int[]{1060, 125, 510, 110});
//				} else {
//					//альбомная ориентация
//					coords = new int[]{
//							180, 265, 3150, 280,//область стандартного штампа
//							1375, 80, 1100, 200,//область УНП
//					};
//					
//					//прямоугольник серии
//					coordsInStandardStamp.add(new int[]{0, 0, 260, 110});
//					//прямоугольник наименования типа
//					coordsInStandardStamp.add(new int[]{220, 70, 1820, 160});
//					//прямоугольник номера штрихкода
//					coordsInStandardStamp.add(new int[]{2070, 160, 510, 110});
//				}
//				
////				//получаем часть изображения где идет указание серии, типа и штрих-код
////				BufferedImage partOfBufferedImage1 = deepCopy(bufferedImage.getSubimage(coords[0], coords[1], coords[2], coords[3]));
//				
//				//получаем часть изображения где идет УНП грузополучателя
//				BufferedImage partOfBufferedImage2 = deepCopy(bufferedImage.getSubimage(coords[4], coords[5], coords[6], coords[7]));
//				
//				
////				//обводка областей где идет определение
////				for(int[] coordInStandardStamp : coordsInStandardStamp)
////					areasMarkup(partOfBufferedImage1, coordInStandardStamp);
//				
//				
//				try {
////					for(int[] coordInStamp : coordsInStamp) {
////						String res;
//////						for(Word word : tesseract.getWords(partOfBufferedImage.getSubimage(coordInStamp[0], coordInStamp[1], coordInStamp[2], coordInStamp[3]),
//////								ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE)) {
//////							Rectangle rect = word.getBoundingBox();
//////							
//////							System.out.println(rect.getMinX()+","+rect.getMaxX()+","+rect.getMinY()+","+rect.getMaxY()+": "+word.getText());
//////						}
////						
////						System.out.println("Область стандартного штампа");
////						String stampText = tesseract.doOCR(partOfBufferedImage1, new Rectangle(coordInStamp[0], coordInStamp[1], coordInStamp[2], coordInStamp[3]));//segMod 6, BINARY
////						res = stampText.replaceAll("[^\\da-zA-Zа-яёА-ЯЁ]", "").toLowerCase();
////						System.out.println(Arrays.toString(coordInStamp) + "; " + res);
////						
//////					result.append(res + "; ");//result.append(it + ": " + res + "; ");
////						
//////					Cell cell = row.createCell(indexCell++);
//////					cell.setCellValue(res);
//////					
//////					Cell cellFormula = row.createCell(indexCell);
//////					cellFormula.setCellFormula(getColumnName(indexCell-1) + (row.getRowNum()+1) + "=J" + (row.getRowNum()+1));
//////					indexCell++;
////					}
//					
//					System.out.println("Область УНП");
//					String text = tesseract.doOCR(partOfBufferedImage2);//, new Rectangle(coordInStamp[0], coordInStamp[1], coordInStamp[2], coordInStamp[3]));//segMod 4, GRAY
//					String res = text;//.replaceAll("[^\\da-zA-Zа-яёА-ЯЁ]", "").toLowerCase();
//					System.out.println(res);
//					
//				} catch (TesseractException ex) {
//					ex.printStackTrace();
//				}
//				
////				ImageIO.write(partOfBufferedImage1, "png", new File(absPath + "output/stamp1_" + page + "_" + it + ".png"));
//				ImageIO.write(partOfBufferedImage2, "png", new File(absPath + "output/stamp2_" + page + "_" + it + ".png"));
//			}
////			System.out.println(result.toString());
//		}
//		document.close();
//	}
	
//	private static void areasMarkup(BufferedImage partOfBufferedImage, int[] _coords) {
//		Graphics2D g2d = partOfBufferedImage.createGraphics();
//		g2d.setColor(Color.red);
//		
//		g2d.drawRect(_coords[0], _coords[1], _coords[2], _coords[3]);
//		
//		g2d.dispose();
//	}
//	
//	public static BufferedImage deepCopy(BufferedImage bi) {
//		ColorModel cm = bi.getColorModel();
//		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
//		WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
//		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
//	}
	
//	private static String getColumnName(int columnNumber) {
//		String columnName = "";
//		int dividend = columnNumber + 1;
//		int modulus;
//		while (dividend > 0){
//			modulus = (dividend - 1) % 26;
//			columnName = (char)(65 + modulus) + columnName;
//			dividend = (int)((dividend - modulus) / 26);
//		}
//		return columnName;
//	}
	
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