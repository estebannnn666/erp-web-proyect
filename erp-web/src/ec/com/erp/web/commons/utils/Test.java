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

			String texto = "Esto es lo que va a la impresora";
			PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
			DocPrintJob docPrintJob = printService.createPrintJob();
			Doc doc = new SimpleDoc(texto.getBytes(), flavor, null);
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
