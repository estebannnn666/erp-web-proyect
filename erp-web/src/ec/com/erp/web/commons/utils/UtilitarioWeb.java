package ec.com.erp.web.commons.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
	
	public static void main(String arg[]) {
//		System.out.println(""+completarEspaciosCadena(13, "1002066404001345465"));
		File fileToPrint = new File("C:/archivo.pdf");
		try {
			Desktop.getDesktop().print(fileToPrint);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
