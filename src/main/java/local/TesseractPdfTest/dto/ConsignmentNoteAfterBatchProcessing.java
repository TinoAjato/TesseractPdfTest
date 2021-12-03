package local.TesseractPdfTest.dto;

import java.io.File;

public class ConsignmentNoteAfterBatchProcessing {
	
	private String consignmentSeries;
	
	private String documentTypeName;
	
	private String consigneeUPN;
	
	private String date;
	
	private String consigneeName;
	
	private File png1Area;
	
	private File png2Area;
	
	private File png3Area;
	
	private File splitPdfFile;
	
	private int splitterStartPage = 0;
	
	private int splitterEndPage = 0;
	
	private int splitterAtPage = 0;
	
	
	public ConsignmentNoteAfterBatchProcessing(int splitterStartPage, int splitterEndPage) {
		this.splitterStartPage = splitterStartPage;
		this.splitterEndPage = splitterEndPage;
	}
	
	
	/**Возвращает */
	public String getConsignmentSeries() {
		return consignmentSeries;
	}
	/**Устанавливает */
	public void setConsignmentSeries(String consignmentSeries) {
		this.consignmentSeries = consignmentSeries;
	}
	
	/**Возвращает */
	public String getDocumentTypeName() {
		return documentTypeName;
	}
	/**Устанавливает */
	public void setDocumentTypeName(String documentTypeName) {
		this.documentTypeName = documentTypeName;
	}
	
	/**Возвращает */
	public String getConsigneeUPN() {
		return consigneeUPN;
	}
	/**Устанавливает */
	public void setConsigneeUPN(String consigneeUPN) {
		this.consigneeUPN = consigneeUPN;
	}
	
	/**Возвращает */
	public String getDate() {
		return date;
	}
	/**Устанавливает */
	public void setDate(String date) {
		this.date = date;
	}
	
	/**Возвращает */
	public String getConsigneeName() {
		return consigneeName;
	}
	/**Устанавливает */
	public void setConsigneeName(String consigneeName) {
		this.consigneeName = consigneeName;
	}
	
	/**Возвращает */
	public File getPng1Area() {
		return png1Area;
	}
	/**Устанавливает */
	public void setPng1Area(File png1Area) {
		this.png1Area = png1Area;
	}
	
	/**Возвращает */
	public File getPng2Area() {
		return png2Area;
	}
	/**Устанавливает */
	public void setPng2Area(File png2Area) {
		this.png2Area = png2Area;
	}
	
	/**Возвращает */
	public File getPng3Area() {
		return png3Area;
	}
	/**Устанавливает */
	public void setPng3Area(File png3Area) {
		this.png3Area = png3Area;
	}
	
	/**Возвращает */
	public File getSplitPdfFile() {
		return splitPdfFile;
	}
	/**Устанавливает */
	public void setSplitPdfFile(File splitPdfFile) {
		this.splitPdfFile = splitPdfFile;
	}
	
	/**Возвращает */
	public int getSplitterStartPage() {
		if(splitterStartPage <= 0)
			throw new RuntimeException("SplitterStartPage не может быть меньше или равен 0; splitterStartPage = " + splitterStartPage);
		
		return splitterStartPage;
	}
	/**Устанавливает */
	public void setSplitterStartPage(int splitterStartPage) {
		this.splitterStartPage = splitterStartPage;
	}
	
	/**Возвращает */
	public int getSplitterEndPage() {
		if(splitterEndPage <= 0)
			throw new RuntimeException("SplitterEndPage не может быть меньше или равен 0; splitterEndPage = " + splitterEndPage);
		
		return splitterEndPage;
	}
	/**Устанавливает */
	public void setSplitterEndPage(int splitterEndPage) {
		this.splitterEndPage = splitterEndPage;
	}
	
	/**Возвращает */
	public int getSplitterAtPage() {
		
		this.splitterAtPage = this.splitterEndPage - this.splitterStartPage + 1;
		
		if(splitterAtPage <= 0)
			throw new RuntimeException("SplitterAtPage не может быть меньше или равен 0; splitterAtPage = " + splitterAtPage);
		
		return splitterAtPage;
	}
	
}