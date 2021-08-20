package ec.com.erp.web.commons.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Calendar;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

public class UtilitarioWeb {

	/**
	 * Metodo para mostrar PDF
	 * @param contenido
	 * @throws IOException
	 */
	public static void mostrarPDF(byte contenido[]) throws IOException
	{
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpServletResponse response = (HttpServletResponse)externalContext.getResponse();
		HttpServletRequest request = (HttpServletRequest)externalContext.getRequest();

		HttpSession session = request.getSession(Boolean.FALSE.booleanValue());
		session.setAttribute("contenidoPDF", contenido);
		response.sendRedirect(request.getContextPath() + "/ImpresionServlet");
	}
	
	/**
	 * Reset de hora
	 * @param fecha
	 */
	public static void cleanDate(Calendar fecha){
		fecha.set(Calendar.HOUR_OF_DAY, 0);
		fecha.set(Calendar.MINUTE, 0);
		fecha.set(Calendar.SECOND, 0);
		fecha.set(Calendar.MILLISECOND, 0);
	}
	
	public static String numeroFactura(String numeroSecuencia) {
		String cadena = "000000000";
		int totalCadena = cadena.length();
		int numeroCaracteres = numeroSecuencia.length();
		int carRemplazar = totalCadena - numeroCaracteres;
		String res = cadena.substring(0, carRemplazar);
		return res+""+numeroSecuencia;
	}
	
	public static String completarEspaciosCadena(int numTotal, String cadena) {
		StringBuilder cadenaCompleta = new StringBuilder();
		cadenaCompleta.append(cadena);
		int tamCadena = cadena.length();
		int cont = tamCadena;
		while(cont < numTotal) {
			cadenaCompleta.append(" ");
			cont++;
		}
		return cadenaCompleta.toString().substring(0, numTotal);
	}

	public static String completarEspaciosNumeros(int numTotal, String cadena) {
		StringBuilder cadenaCompleta = new StringBuilder();
		int tamCadena = cadena.length();
		int cont = tamCadena;
		while(cont < numTotal) {
			cadenaCompleta.append(" ");
			cont++;
		}
		cadenaCompleta.append(cadena);
		return cadenaCompleta.toString();
	}
	
	public static String htmlToXhtml(String html) {
        org.jsoup.nodes.Document document = Jsoup.parse(html);
        document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        return document.html();
    }

    public static byte[] xhtmlToPdf(String xhtml, String outFileName) throws IOException, DocumentException {
        File output = new File(outFileName);
        ITextRenderer iTextRenderer = new ITextRenderer();
        iTextRenderer.setDocumentFromString(xhtml);
        iTextRenderer.layout();
        OutputStream os = new FileOutputStream(output);
        iTextRenderer.createPDF(os);
        os.close();
        byte[] bytes = Files.readAllBytes(output.toPath());
        return bytes;
    }
}
