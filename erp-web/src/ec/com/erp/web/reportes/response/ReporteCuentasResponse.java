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
}
