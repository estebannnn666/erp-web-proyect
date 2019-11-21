package ec.com.erp.web.commons.utils;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

public class Test {

	

	 public static void main(String[] args) {
	 try {
	
		 StringBuilder builder = new StringBuilder();
		 
		// Message
		builder.append("MOTEL OASIS\r\n");
		builder.append("AV ESPINOZA 267, COL OBRERA\r\n");
		builder.append("ENSENADA, B.C.\r\n");
		builder.append("\r\n");
		builder.append("DIA EXTRA\r\n");
		builder.append("Fecha : ").append("2019-03-21").append("  Hora: ").append("21:05").append("\r\n");
		builder.append(1).append(" ").append("BOTELLON PET").append("            $").append(2.50).append("\r\n");
		builder.append("\r\n");
		builder.append("***GRACIAS POR SU PREFERENCIA***");
		builder.append("\r\n");
		builder.append("\r\n");
		builder.append("\r\n");

		// Paper Cut
		char[] cutPaper = new char[] { 0x1d, 'i'};
		builder.append(cutPaper);
		
	 //String texto = "Esto es lo que va a la impresora";
	 PrintService printService =
	 PrintServiceLookup.lookupDefaultPrintService();
	 DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
	 DocPrintJob docPrintJob = printService.createPrintJob();
	 Doc doc = new SimpleDoc(builder.toString().getBytes(), flavor, null);
	 try {
	 docPrintJob.print(doc, null);
	 } catch (PrintException e) {
	 // TODO Auto-generated catch block
	 e.printStackTrace();
	 }
	
	 } catch (Exception e) {
	 System.out.println("Error: " + e.getMessage());
	 }
	 }
}

