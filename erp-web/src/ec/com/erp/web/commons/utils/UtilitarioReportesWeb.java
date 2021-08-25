package ec.com.erp.web.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.mdl.dto.FacturaCabeceraDTO;
import ec.com.erp.cliente.mdl.dto.InventarioDTO;
import ec.com.erp.cliente.mdl.vo.ReporteVentasFacturasVO;
import ec.com.erp.cliente.mdl.vo.ReporteVentasVO;
import ec.com.erp.facturacion.electronica.ws.FacturaWS;
import ec.com.erp.firebase.model.ImageItem;
import ec.com.erp.web.reportes.response.ReporteCuentasResponse;
import ec.com.erp.web.reportes.response.ReporteExistenciaResponse;
import ec.com.erp.web.reportes.response.ReporteVentasFacturasResponse;
import ec.com.erp.web.reportes.response.ReporteVentasResponse;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class UtilitarioReportesWeb {

	public static byte[] generarReporteVentasArticulo(Collection<ReporteVentasVO> reporteVentasCols, Map<String, Object> params) throws JRException, IOException {
		try {
			JRDataSource dataSource = new JRBeanCollectionDataSource(obtenerDetalleReporte(reporteVentasCols));
			params.put(JRParameter.REPORT_LOCALE, Locale.US);
			try {
				params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\probersa.jpeg"));
			} catch (FileNotFoundException ex) {
				try {
					params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\logo.jpeg"));
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex);
				} catch (FileNotFoundException ex1) {
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
			JasperFillManager.fillReportToFile("C:\\ErpLibreries\\reportes\\ventas_articulo.jasper", params, dataSource);
			String filePdf = JasperExportManager.exportReportToPdfFile("C:\\ErpLibreries\\reportes\\ventas_articulo.jrprint");
			File file = new File(filePdf);
			return Files.readAllBytes(file.toPath());
		}catch (JRException e) {
			throw new ERPException("Error", e.getMessage()) ;
		}catch (Exception e) {
			throw new ERPException("Error", e.getMessage()) ;
		}
	}
	
	public static byte[] generarReporteVentasFactura(Collection<ReporteVentasFacturasVO> reporteVentasFacturaCols, Map<String, Object> params) throws JRException, IOException {
		try {
			JRDataSource dataSource = new JRBeanCollectionDataSource(obtenerDetalleReporteFactura(reporteVentasFacturaCols));
			params.put(JRParameter.REPORT_LOCALE, Locale.US);
			try {
				params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\probersa.jpeg"));
			} catch (FileNotFoundException ex) {
				try {
					params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\logo.jpeg"));
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex);
				} catch (FileNotFoundException ex1) {
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
			JasperFillManager.fillReportToFile("C:\\ErpLibreries\\reportes\\ventas_factura.jasper", params, dataSource);
			String filePdf = JasperExportManager.exportReportToPdfFile("C:\\ErpLibreries\\reportes\\ventas_factura.jrprint");
			File file = new File(filePdf);
			return Files.readAllBytes(file.toPath());
		}catch (JRException e) {
			throw new ERPException("Error", e.getMessage()) ;
		}catch (Exception e) {
			throw new ERPException("Error", e.getMessage()) ;
		}
	}
	
	public static byte[] generarReporteExistencias(Collection<InventarioDTO> inventarioDTOCols, Map<String, Object> params) throws JRException, IOException {
		try {
			JRDataSource dataSource = new JRBeanCollectionDataSource(obtenerReporteExistencias(inventarioDTOCols));
			params.put(JRParameter.REPORT_LOCALE, Locale.US);
			try {
				params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\probersa.jpeg"));
			} catch (FileNotFoundException ex) {
				try {
					params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\logo.jpeg"));
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex);
				} catch (FileNotFoundException ex1) {
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
			JasperFillManager.fillReportToFile("C:\\ErpLibreries\\reportes\\reporte_existencia.jasper", params, dataSource);
			String filePdf = JasperExportManager.exportReportToPdfFile("C:\\ErpLibreries\\reportes\\reporte_existencia.jrprint");
			File file = new File(filePdf);
			return Files.readAllBytes(file.toPath());
		}catch (JRException e) {
			throw new ERPException("Error", e.getMessage()) ;
		}catch (Exception e) {
			throw new ERPException("Error", e.getMessage()) ;
		}
	}
	
	public static byte[] generarReporteVentas(Collection<FacturaCabeceraDTO> facturaCabeceraDTOCols, Map<String, Object> params) throws JRException, IOException {
		try {
			JRDataSource dataSource = new JRBeanCollectionDataSource(obtenerDatosReporteVentas(facturaCabeceraDTOCols));
			params.put(JRParameter.REPORT_LOCALE, Locale.US);
			try {
				params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\probersa.jpeg"));
			} catch (FileNotFoundException ex) {
				try {
					params.put("LOGO", new FileInputStream("C:\\ErpLibreries\\imagenes\\logo.jpeg"));
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex);
				} catch (FileNotFoundException ex1) {
					Logger.getLogger(FacturaWS.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
			JasperFillManager.fillReportToFile("C:\\ErpLibreries\\reportes\\facturas_ventas.jasper", params, dataSource);
			String filePdf = JasperExportManager.exportReportToPdfFile("C:\\ErpLibreries\\reportes\\facturas_ventas.jrprint");
			File file = new File(filePdf);
			return Files.readAllBytes(file.toPath());
		}catch (JRException e) {
			throw new ERPException("Error", e.getMessage()) ;
		}catch (Exception e) {
			throw new ERPException("Error", e.getMessage()) ;
		}
	}
	
	public static Collection<ReporteVentasResponse> obtenerDetalleReporte(Collection<ReporteVentasVO> reporteVentasCols){
		// Convertidor de decimales
		DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
	    decimalSymbols.setDecimalSeparator('.');
		DecimalFormat formatoDecimales = new DecimalFormat("#.##", decimalSymbols);
		formatoDecimales.setMinimumFractionDigits(2);
		Collection<ReporteVentasResponse> response = new ArrayList<>();
		reporteVentasCols.stream().forEach(reporteVentasVO ->{
			ReporteVentasResponse reporteVentas = new ReporteVentasResponse();
			reporteVentas.setNombreArticulo(reporteVentasVO.getNombreArticulo());
			reporteVentas.setUnidadMedida(reporteVentasVO.getCodigoValorUnidadManejo().equals("UNI") ? "UNIDADES" : reporteVentasVO.getCodigoValorUnidadManejo()+" x "+reporteVentasVO.getValorUnidadManejo());
			reporteVentas.setPrecioMayorista(formatoDecimales.format(reporteVentasVO.getPrecioMayorista()));
			reporteVentas.setPrecioMinorista(formatoDecimales.format(reporteVentasVO.getPrecioMinorista()));
			reporteVentas.setPorcentajeComision(formatoDecimales.format(reporteVentasVO.getPorcentajeComision()));
			reporteVentas.setPorcentajeComisionMayor(formatoDecimales.format(reporteVentasVO.getPorcentajeComisionMayor()));
			reporteVentas.setTipoCliente(reporteVentasVO.getTipoCliente().equals("MIN") ? "AL POR MENOR" : "AL POR MAYOR");
			reporteVentas.setCantidadVendida(""+reporteVentasVO.getCantidadVendida());
			reporteVentas.setValorVendido(formatoDecimales.format(reporteVentasVO.getValorVendido()));
			reporteVentas.setValoComisionTotal(formatoDecimales.format(reporteVentasVO.getValoComisionTotal()));
			response.add(reporteVentas);
		});		
		return response;
	}
	
	public static Collection<ReporteVentasFacturasResponse> obtenerDetalleReporteFactura(Collection<ReporteVentasFacturasVO> reporteVentasCols){
		// Convertir fechas
		SimpleDateFormat formatoFecha = new SimpleDateFormat("YYYY-MM-dd");
		// Convertidor de decimales
		DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
	    decimalSymbols.setDecimalSeparator('.');
		DecimalFormat formatoDecimales = new DecimalFormat("#.##", decimalSymbols);
		formatoDecimales.setMinimumFractionDigits(2);
		Collection<ReporteVentasFacturasResponse> response = new ArrayList<>();
		reporteVentasCols.stream().forEach(reporteVentasVO ->{
			ReporteVentasFacturasResponse reporteVentas = new ReporteVentasFacturasResponse();
			reporteVentas.setNumeroDocumento(reporteVentasVO.getNumeroDocumento());
			reporteVentas.setRucDocumentoCliente(reporteVentasVO.getRucDocumentoCliente());
			reporteVentas.setRazonSocialCliente(reporteVentasVO.getRazonSocialCliente());
			reporteVentas.setTipoCliente(reporteVentasVO.getTipoCliente().equals("MIN") ? "MINORISTA":"MAYORISTA");
			reporteVentas.setPagada(reporteVentasVO.getPagada() ? "SI":"NO");
			reporteVentas.setFechaVenta(formatoFecha.format(reporteVentasVO.getFechaVenta()));
			reporteVentas.setSubTotal(formatoDecimales.format(reporteVentasVO.getSubTotal()));
			reporteVentas.setTarifaCero(formatoDecimales.format(reporteVentasVO.getTarifaCero()));
			reporteVentas.setTarifaImpuesto(formatoDecimales.format(reporteVentasVO.getTarifaImpuesto()));
			reporteVentas.setTotalIva(formatoDecimales.format(reporteVentasVO.getTotalIva()));
			reporteVentas.setTotalVenta(formatoDecimales.format(reporteVentasVO.getTotalVenta()));
			reporteVentas.setTotalDescuento(formatoDecimales.format(reporteVentasVO.getTotalDescuento()));
			response.add(reporteVentas);
		});		
		return response;
	}
	
	public static Collection<ReporteExistenciaResponse> obtenerReporteExistencias(Collection<InventarioDTO> inventarioDTOCols){
		// Convertir fechas
		SimpleDateFormat formatoFecha = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		// Convertidor de decimales
		DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
	    decimalSymbols.setDecimalSeparator('.');
		DecimalFormat formatoDecimales = new DecimalFormat("#.##", decimalSymbols);
		formatoDecimales.setMinimumFractionDigits(2);
		Collection<ReporteExistenciaResponse> response = new ArrayList<>();
		inventarioDTOCols.stream().forEach(inventarioDTO ->{
			ReporteExistenciaResponse reporteExistencia = new ReporteExistenciaResponse();
			reporteExistencia.setFechaMovimiento(formatoFecha.format(inventarioDTO.getFechaMovimiento()));
			reporteExistencia.setCodigoBarras(inventarioDTO.getArticuloDTO().getCodigoBarras());
			reporteExistencia.setNombreArticulo(inventarioDTO.getArticuloDTO().getNombreArticulo());
			reporteExistencia.setUnidadMedida(inventarioDTO.getArticuloUnidadManejoDTO().getCodigoValorUnidadManejo().equals("UNI") ? "UNIDADES" : inventarioDTO.getArticuloUnidadManejoDTO().getCodigoValorUnidadManejo()+"X"+inventarioDTO.getArticuloUnidadManejoDTO().getValorUnidadManejo());
			reporteExistencia.setDetalleMovimiento(inventarioDTO.getDetalleMoviento());
			reporteExistencia.setCantidadExistencia(""+inventarioDTO.getCantidadExistencia());
			reporteExistencia.setValorUnidadExistencia(formatoDecimales.format(inventarioDTO.getValorUnidadExistencia().doubleValue()));
			reporteExistencia.setValorTotalExistencia(formatoDecimales.format(inventarioDTO.getValorTotalExistencia().doubleValue()));
			response.add(reporteExistencia);
		});		
		return response;
	}
	
	public static Collection<ReporteCuentasResponse> obtenerDatosReporteVentas(Collection<FacturaCabeceraDTO> reporteVentasCols){
		// Convertir fechas
		SimpleDateFormat formatoFecha = new SimpleDateFormat("YYYY-MM-dd");
		// Convertidor de decimales
		DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
	    decimalSymbols.setDecimalSeparator('.');
		DecimalFormat formatoDecimales = new DecimalFormat("#.##", decimalSymbols);
		formatoDecimales.setMinimumFractionDigits(2);
		Collection<ReporteCuentasResponse> response = new ArrayList<>();
		reporteVentasCols.stream().forEach(facturaCabeceraDTO ->{
			ReporteCuentasResponse reporteVentas = new ReporteCuentasResponse();
			reporteVentas.setNumeroDocumento(facturaCabeceraDTO.getNumeroDocumento());
			reporteVentas.setRucDocumentoCliente(facturaCabeceraDTO.getRucDocumento());
			reporteVentas.setRazonSocialCliente(facturaCabeceraDTO.getNombreClienteProveedor());
			reporteVentas.setVendedor(facturaCabeceraDTO.getVendedorDTO() != null ? facturaCabeceraDTO.getVendedorDTO().getPersonaDTO().getPrimerNombre() +" "+facturaCabeceraDTO.getVendedorDTO().getPersonaDTO().getPrimerApellido() : "N/D");
			reporteVentas.setPagada(facturaCabeceraDTO.getPagado() ? "SI":"NO");
			reporteVentas.setFechaVenta(formatoFecha.format(facturaCabeceraDTO.getFechaDocumento()));
			reporteVentas.setSubTotal(formatoDecimales.format(facturaCabeceraDTO.getSubTotal()));
			reporteVentas.setAbonos(formatoDecimales.format(facturaCabeceraDTO.getTotalPagos()));
			reporteVentas.setSaldos(formatoDecimales.format(facturaCabeceraDTO.getTotalCuenta().subtract(facturaCabeceraDTO.getTotalPagos())));
			reporteVentas.setTotalVenta(formatoDecimales.format(facturaCabeceraDTO.getTotalCuenta()));
			reporteVentas.setTotalDescuento(formatoDecimales.format(facturaCabeceraDTO.getDescuento()));
			response.add(reporteVentas);
		});		
		return response;
	}
	
	public static void main(String[] args){
		try{
			String cadena = "{\"0\":{\"barCode\":\"7862100290728\",\"id\":0,\"image\":\"R0lGODlhqQEt\"}}";
			ObjectMapper mapper = new ObjectMapper();
			Map<String, ImageItem> map = mapper.readValue(cadena, new TypeReference<Map<String, ImageItem>>() {});
			for(Entry<String, ImageItem> valor: map.entrySet()){
				ImageItem imageItem = new ImageItem();
				imageItem.setId(valor.getValue().getId());
				imageItem.setBarCode(valor.getValue().getBarCode());
				imageItem.setImage(valor.getValue().getImage());
			}
			System.out.println(cadena);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
