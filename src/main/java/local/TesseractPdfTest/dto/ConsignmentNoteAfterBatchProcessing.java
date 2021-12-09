package local.TesseractPdfTest.dto;

import java.util.UUID;

public class ConsignmentNoteAfterBatchProcessing {
	
	/***/
	private UUID uuid;
	
	/***/
	private String consignmentSeries;
	
	/***/
	private String documentTypeName;
	
	/***/
	private String consigneeUPN;
	
	/***/
	private String date;
	
	/***/
	private String consigneeName;
	
	/***/
	private String area1Path;
	
	/***/
	private String area2Path;
	
	/***/
	private String area3Path;
	
	/***/
	private String splitPdfFilePath;
	
	private int splitterStartPage = 0;
	
	private int splitterEndPage = 0;
	
	private int splitterAtPage = 0;
	
	
	public ConsignmentNoteAfterBatchProcessing(int page) {
		this.splitterStartPage = page;
		this.splitterEndPage = page;
		this.uuid = UUID.randomUUID();
	}
	
	
	/**Возвращает */
	public UUID getUuid() {
		return uuid;
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
	public String getArea1Path() {
		return area1Path;
	}
	/**Устанавливает */
	public void setArea1Path(String area1Path) {
		this.area1Path = area1Path;
	}
	
	/**Возвращает */
	public String getArea2Path() {
		return area2Path;
	}
	/**Устанавливает */
	public void setArea2Path(String area2Path) {
		this.area2Path = area2Path;
	}
	
	/**Возвращает */
	public String getArea3Path() {
		return area3Path;
	}
	/**Устанавливает */
	public void setArea3Path(String area3Path) {
		this.area3Path = area3Path;
	}
	
	/**Возвращает */
	public String getSplitPdfFilePath() {
		return splitPdfFilePath;
	}
	/**Устанавливает */
	public void setSplitPdfFilePath(String splitPdfFilePath) {
		this.splitPdfFilePath = splitPdfFilePath;
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
	
	
	@Override
	public String toString() {
		return "ConsignmentNoteAfterBatchProcessing"
				+ " [consignmentSeries=" + consignmentSeries
				+ ", documentTypeName=" + documentTypeName
				+ ", consigneeUPN=" + consigneeUPN
				+ ", date=" + date
				+ ", consigneeName=" + consigneeName
				+ ", area1Path=" + area1Path
				+ ", area2Path=" + area2Path
				+ ", area3Path=" + area3Path
				+ ", splitPdfFilePath=" + splitPdfFilePath
				+ "]";
	}
}