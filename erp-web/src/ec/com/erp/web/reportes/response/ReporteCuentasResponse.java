package ec.com.erp.web.reportes.response;

import java.io.Serializable;

/**
 * 
 * @author Esteban Gudino
 * 2020-09-13
 */
public class ReporteCuentasResponse implements Serializable{
	
	private static final long serialVersionUID = 7863262235394607247L;
	
	private String numeroDocumento ;
	
	private String rucDocumentoCliente ;
	
	private String razonSocialCliente ;
	
	private String vendedor;
	
	private String pagada;
	
	private String fechaVenta;
	
	private String subTotal ;
	
	private String abonos ;
	
	private String saldos ;
	
	private String totalVenta ;
	
	private String totalDescuento ;
	
	private String tarifa0;
	
	private String tarifa12;
	
	private String iva12;
	
	private String retencion30;
	
	private String retencion70;
	
	private String renta1;

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getRucDocumentoCliente() {
		return rucDocumentoCliente;
	}

	public void setRucDocumentoCliente(String rucDocumentoCliente) {
		this.rucDocumentoCliente = rucDocumentoCliente;
	}

	public String getRazonSocialCliente() {
		return razonSocialCliente;
	}

	public void setRazonSocialCliente(String razonSocialCliente) {
		this.razonSocialCliente = razonSocialCliente;
	}

	public String getVendedor() {
		return vendedor;
	}

	public void setVendedor(String vendedor) {
		this.vendedor = vendedor;
	}

	public String getPagada() {
		return pagada;
	}

	public void setPagada(String pagada) {
		this.pagada = pagada;
	}

	public String getFechaVenta() {
		return fechaVenta;
	}

	public void setFechaVenta(String fechaVenta) {
		this.fechaVenta = fechaVenta;
	}

	public String getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(String subTotal) {
		this.subTotal = subTotal;
	}

	public String getAbonos() {
		return abonos;
	}

	public void setAbonos(String abonos) {
		this.abonos = abonos;
	}

	public String getSaldos() {
		return saldos;
	}

	public void setSaldos(String saldos) {
		this.saldos = saldos;
	}

	public String getTotalVenta() {
		return totalVenta;
	}

	public void setTotalVenta(String totalVenta) {
		this.totalVenta = totalVenta;
	}

	public String getTotalDescuento() {
		return totalDescuento;
	}

	public void setTotalDescuento(String totalDescuento) {
		this.totalDescuento = totalDescuento;
	}

	public String getTarifa0() {
		return tarifa0;
	}

	public void setTarifa0(String tarifa0) {
		this.tarifa0 = tarifa0;
	}

	public String getTarifa12() {
		return tarifa12;
	}

	public void setTarifa12(String tarifa12) {
		this.tarifa12 = tarifa12;
	}

	public String getIva12() {
		return iva12;
	}

	public void setIva12(String iva12) {
		this.iva12 = iva12;
	}

	public String getRetencion30() {
		return retencion30;
	}

	public void setRetencion30(String retencion30) {
		this.retencion30 = retencion30;
	}

	public String getRetencion70() {
		return retencion70;
	}

	public void setRetencion70(String retencion70) {
		this.retencion70 = retencion70;
	}

	public String getRenta1() {
		return renta1;
	}

	public void setRenta1(String renta1) {
		this.renta1 = renta1;
	}
}
