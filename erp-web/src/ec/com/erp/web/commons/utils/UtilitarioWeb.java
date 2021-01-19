package ec.com.erp.web.commons.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Finishings;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.Sides;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.xhtmlrenderer.pdf.ITextRenderer;

import ec.com.erp.cliente.mdl.dto.FacturaCabeceraDTO;
import ec.com.erp.cliente.mdl.dto.FacturaDetalleDTO;

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

    public static byte[] xhtmlToPdf(String xhtml, String outFileName) throws IOException {
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
    
	private static void testImpresion() {
		FacturaCabeceraDTO facturaImprimir = new FacturaCabeceraDTO();
        facturaImprimir.setFechaDocumento(new Date());
        facturaImprimir.setNombreClienteProveedor("HENRY ESTEBAN GUDIÑO BARAHONA");
        facturaImprimir.setDireccion("AV MARIOANO ACOSTA Y CAMILO PONCE LOCAL PLAZA 5");
        facturaImprimir.setTelefono("0997122133");
        facturaImprimir.setRucDocumento("1002966404");
        facturaImprimir.setTotalSinImpuestos(BigDecimal.valueOf(100002.87));
        facturaImprimir.setFacturaDetalleDTOCols(new ArrayList<>());
        FacturaDetalleDTO detalleIngreso = new FacturaDetalleDTO();
        detalleIngreso.setCantidad(2);
        detalleIngreso.setDescripcion("BODETALLA DE TESALIA 500 ML");
        detalleIngreso.setValorUnidad(BigDecimal.valueOf(2.5));
        detalleIngreso.setSubTotal(BigDecimal.valueOf(5.0));
        facturaImprimir.getFacturaDetalleDTOCols().add(detalleIngreso);
        FacturaDetalleDTO detalleIngreso1 = new FacturaDetalleDTO();
        detalleIngreso1.setCantidad(10000);
        detalleIngreso1.setDescripcion("BOTELLON PET PERSONALIZADO CON COVERTURA Y LLAVES");
        detalleIngreso1.setValorUnidad(BigDecimal.valueOf(2.5));
        detalleIngreso1.setSubTotal(BigDecimal.valueOf(125000.65));
        facturaImprimir.getFacturaDetalleDTOCols().add(detalleIngreso1);
        FacturaDetalleDTO detalleIngreso2 = new FacturaDetalleDTO();
        detalleIngreso2.setCantidad(2);
        detalleIngreso2.setDescripcion("BODETALLA DE TESALIA 500 ML");
        detalleIngreso2.setValorUnidad(BigDecimal.valueOf(2.5));
        detalleIngreso2.setSubTotal(BigDecimal.valueOf(5.0));
        facturaImprimir.getFacturaDetalleDTOCols().add(detalleIngreso2);
        FacturaDetalleDTO detalleIngreso3 = new FacturaDetalleDTO();
        detalleIngreso3.setCantidad(2);
        detalleIngreso3.setDescripcion("BODETALLA DE TESALIA 500 ML");
        detalleIngreso3.setValorUnidad(BigDecimal.valueOf(2.5));
        detalleIngreso3.setSubTotal(BigDecimal.valueOf(5.0));
        facturaImprimir.getFacturaDetalleDTOCols().add(detalleIngreso3);
        
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat formatoDecimales = new DecimalFormat("#.##");
		formatoDecimales.setMinimumFractionDigits(2);
		String fechaFormateada =  formatoFecha.format(facturaImprimir.getFechaDocumento());
		StringBuilder texto = new StringBuilder();
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("             "+facturaImprimir.getNombreClienteProveedor());
		texto.append("\n");
		texto.append("             "+UtilitarioWeb.completarEspaciosCadena(13, facturaImprimir.getRucDocumento())+"          "+fechaFormateada);
		texto.append("\n");
		texto.append("\n");
		texto.append("     "+UtilitarioWeb.completarEspaciosCadena(30, facturaImprimir.getDireccion())+"     "+facturaImprimir.getTelefono());
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		for(FacturaDetalleDTO detalle : facturaImprimir.getFacturaDetalleDTOCols()) {
			texto.append("    "+UtilitarioWeb.completarEspaciosCadena(6, detalle.getCantidad().toString()));
			texto.append(" "+UtilitarioWeb.completarEspaciosCadena(28, detalle.getDescripcion()));
			texto.append(""+UtilitarioWeb.completarEspaciosNumeros(8, formatoDecimales.format(detalle.getValorUnidad())));
			texto.append(" "+UtilitarioWeb.completarEspaciosNumeros(9, formatoDecimales.format(detalle.getSubTotal())));
			texto.append("\n");
		}
		int tamDetalle = facturaImprimir.getFacturaDetalleDTOCols().size();
		while(tamDetalle < 21 ) {
			texto.append("\n");
			tamDetalle++;
		}
		texto.append("\n");
		texto.append("                                               "+UtilitarioWeb.completarEspaciosNumeros(9, formatoDecimales.format(facturaImprimir.getTotalSinImpuestos())));
		texto.append("\n");
		texto.append("                                               "+UtilitarioWeb.completarEspaciosNumeros(9, formatoDecimales.format(77.98)));
		texto.append("\n");
		texto.append("\n");
		texto.append("                                               "+UtilitarioWeb.completarEspaciosNumeros(9, formatoDecimales.format(77.98)));
		texto.append("\n");
		texto.append("                                               "+UtilitarioWeb.completarEspaciosNumeros(9, formatoDecimales.format(77.98)));
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		texto.append("\n");
		
		
		DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A5);
        aset.add(new Copies(1));
        aset.add(Sides.ONE_SIDED);
        aset.add(Finishings.STAPLE);
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        
		DocPrintJob docPrintJob = printService.createPrintJob();
		Doc doc = new SimpleDoc(texto.toString().getBytes(), flavor, null);
		try {
			docPrintJob.print(doc, aset);
		} catch (PrintException e) {
			e.printStackTrace();
		}  
	}
	
	public static void main(String arg[]) throws IOException {
//		System.out.println(""+completarEspaciosCadena(13, "1002066404001345465"));
//		File fileToPrint = new File("C:/archivo.pdf");
//		try {
//			Desktop.getDesktop().print(fileToPrint);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<html>\r\n" + 
				"  <table>\r\n" + 
				"    <tr>\r\n" + 
				"      <td style=\"height:0.5cm;\" />\r\n" + 
				"    </tr>\r\n" + 
				"    <col style=\"width:20.0cm;\" />\r\n" + 
				"    <tr>\r\n" + 
				"      <td style=\"height:0.5cm;\" />\r\n" + 
				"    </tr>\r\n" + 
				"    <tr>\r\n" + 
				"      <td>\r\n" + 
				"        <table>\r\n" + 
				"          <col style=\"width:20cm; \" />\r\n" + 
				"          <tr>\r\n" + 
				"            <td style=\"text-transform: uppercase;\" valign=\"botton\" align=\"center\">\r\n" + 
				"              <pre>CATALOGO DE ARTICULOS</pre>\r\n" + 
				"            </td>\r\n" + 
				"          </tr>\r\n" + 
				"        </table>\r\n" + 
				"      </td>\r\n" + 
				"    </tr>\r\n" + 
				"    <tr>\r\n" + 
				"      <td style=\"height:0.5cm;\" />\r\n" + 
				"    </tr>\r\n" + 
				"    <col style=\"width:20.0cm;\" />\r\n" + 
				"    <tr>\r\n" + 
				"      <td>\r\n" + 
				"        <table>\r\n" + 
				"          <col style=\"width:1.0cm;\" />\r\n" + 
				"          <col style=\"width:18.0cm;\" />\r\n" + 
				"          <col style=\"width:1.0cm;\" />\r\n" + 
				"          <tr>\r\n" + 
				"            <td />\r\n" + 
				"          </tr>\r\n" + 
				"          <tr>\r\n" + 
				"            <td valign=\"botton\" align=\"center\">\r\n" + 
				"              <table>\r\n" + 
				"                <col style=\"width:1.0cm;\" />\r\n" + 
				"                <col style=\"width:18.0cm;\" />\r\n" + 
				"                <col style=\"width:1.0cm;\" />\r\n" + 
				"                <tr>\r\n" + 
				"                  <td />\r\n" + 
				"                </tr>\r\n" + 
				"                <tr>\r\n" + 
				"                  <td align=\"center\" class=\"borde\">\r\n" + 
				"                    <table>\r\n" + 
				"                      <col style=\"width:18.0cm;\" />\r\n" + 
				"                      <tr>\r\n" + 
				"                        <td style=\"width:1.0cm;\" align=\"center\">\r\n" + 
				"                          <table>\r\n" + 
				"                            <col style=\"width:5.0cm;\" />\r\n" + 
				"                            <col style=\"width:8.0cm;\" />\r\n" + 
				"                            <col style=\"width:5.0cm;\" />\r\n" + 
				"                            <tr>\r\n" + 
				"                              <td rowspan=\"4\">\r\n" + 
				"                                <img height=\"150\" width=\"150\" src=\"data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAABmJLR0QA/wD/AP+gvaeTAAAE00lEQVR4nO2d34sWVRjHP+vqiqsl6EWJLyhFUiAEBWF5U3deFG1aGXRR0FV14X8QXiRkdqGgV14JEphRRlDYjzuhm0IMqSU1CMyEMNofbqzRrhczA+vb7DvnzHnOmee8Ph94YPdl5pzvd767c87MnOEFwzAMw9DFGLAX+BCYBGbLmiw/e7ncJley8rcHuAIsNtRlYHdHGkPIxt8o8AHNQvvrELCiA72+ZOevjdiqDnag15es/O0JEFvVRGrRHmTlb4zifBkq+FcUDYRLyM7fXgGxVb2UQrAnSf1JDDaS/4rPC7YlRXb+fkHuL2gyhWBPsvM3Iyh4JoVgT5L60zb/X+haQGQa/UkEck2gjRhtSZHUn0QgPwi0UfG9YFtSJPUnEcgZgTYqPhNsS4rs/K0CLhE+4F1B54Vhlv52CwjWeA1SkaW/QwFi30sttgXZ+VsBvN9C7EH0Tb/ryNbfBG7n3EvoPk0tR5b+VlHcSDsJ/ExxhTpT/nwSeLHcJleG3Z9hGIaRISMCbWwAXgCeAx4GesBax31vAlcpnhN8DnwK/CWgSZrNFLOrZ4EHyt8HeezE1zjwDjBF+FVsVVNlm+OxxTuyETgM3EK5r63A+UCRg+p82UeXPA78QQa+tgLXhYXW1fUY4h3ZheyTwmi+1gEXIwmtq4tlnyl5jfBTVDJfByILrat3JYQ7so/iMWsWvu4D5hKJXVpzZd8xGQWO5ebr7cSCl9ZbIcIbWA2cytHXlx2JXiz7jsEG4FyuvlzeiYhVl0OEL8Nm4EKHnoJ9dTF+VDUXIryGR4HfO/Qj4qtr8VI8A/ytwE+wr2yFL+EVYN6jz+OafeUeyD7gP8e+FoD9iXy3JtdARigWGrj2M8Odz8K1+soykDGK15Rd+7gKPJbYd2tyC2QdcNaj/e+A+zvw3ZqcAtmE3+OBU8Cajny3JpdAHgF+c2xzgWJV4aAnp1p8JRcmIXwH8Kdje/8Aryrw3RrtgUzgfjfhGvCEEt+t0RzIG8C/ju1cALYo8t0ajYGMUFzAubbxBXCvMt+t0RbISuCEx/5tV6FbII7CjzruNw+8rti3WmE+wtfgdpPwBvB0iOnEvlQJ8xG+3WH7n4AHQwwn8q1WmI/wcQbPqs4C60PMJvStVpiv8CPLbHeUYsCXwgJxFD4KvAn8CExT3ByM8QpZUl9193CCUhsC+o9J7ONxR385vP16V2GBKMMCUUbdbETirSrDjan+D+w/pFu+6VqAKzspxN6kedo4W277VCQtsaa7N4CHImkWZSftXpiZB56MoEc6iCngYzIJA+Bb2pv9KoKeWBe2tWgcwGdxf626bt97BLWA3IWh07HWGEj/AWjS6Lu9L0kDsVmWMlIF0gNOU9wE9D23hp6b+7efBj4Btnk5GCJ6FFM81wMqPXgOmnb2HPQnHdRTcBqZAygdyCLwkYP+oZtlTTN45iN9u9unvWmanywO3SyryVCXgdRt34/Nsu5mLBBlWCDKsECUYYEowwJRhgWiDAtEGRaIMiwQZVggyrBAlGGBKMMCUYYFogwLRBkWiDJSBKLxK7kr/rf6vAYJ/S79AGkC+TpBH21xWX0uoV/VKvdt6F0G5LLguUl/U6lc5d6jWHJT9208/cQOpM3q80H6l6vsVrkbhmEYeXMbu3QlvKPgIn0AAAAASUVORK5CYII=\" />\r\n" + 
				"                              </td>\r\n" + 
				"                            </tr>\r\n" + 
				"                            <tr>\r\n" + 
				"                              <td>Nombre del articulo:</td>\r\n" + 
				"                              <td>AGUA NOBELL</td>\r\n" + 
				"                            </tr>\r\n" + 
				"                            <tr>\r\n" + 
				"                              <td>Precio al por menor:</td>\r\n" + 
				"                              <td>0,19</td>\r\n" + 
				"                            </tr>\r\n" + 
				"                            <tr>\r\n" + 
				"                              <td>Precio al por mayor</td>\r\n" + 
				"                              <td>0,18</td>\r\n" + 
				"                            </tr>\r\n" + 
				"                          </table>\r\n" + 
				"                        </td>\r\n" + 
				"                      </tr>\r\n" + 
				"					</table>\r\n" + 
				"				</tr>\r\n" + 
				"			</table>\r\n" + 
				"		</tr>	\r\n" + 
				"		</table>\r\n" + 
				"	</tr>\r\n" + 
				"</table>\r\n" + 
				"</html>";
		String xhtml = htmlToXhtml(html);
		byte[] contenido = xhtmlToPdf(xhtml, "reporteCotalogo.pdf");
		System.out.println("res:"+contenido);
	}
}
