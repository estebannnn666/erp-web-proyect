package ec.com.erp.web.reportes.response;

import java.io.Serializable;

/**
 * 
 * @author Esteban Gudino
 * 2020-09-13
 */
public class ReporteVentasResponse implements Serializable{
	
	private static final long serialVersionUID = 7863262235394607247L;
	
	private String nombreArticulo ;
	
	private String unidadMedida ;
	
	private String precioMayorista;
	
	private String precioMinorista;

	private String porcentajeComision;
	
	private String porcentajeComisionMayor;
	
	private String tipoCliente;
	
	private String cantidadVendida;
	
	private String valorVendido;
	
	private String valoComisionTotal;

	public String getNombreArticulo() {
		return nombreArticulo;
	}

	public void setNombreArticulo(String nombreArticulo) {
		this.nombreArticulo = nombreArticulo;
	}

	public String getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public String getPrecioMayorista() {
		return precioMayorista;
	}

	public void setPrecioMayorista(String precioMayorista) {
		this.precioMayorista = precioMayorista;
	}

	public String getPrecioMinorista() {
		return precioMinorista;
	}

	public void setPrecioMinorista(String precioMinorista) {
		this.precioMinorista = precioMinorista;
	}

	public String getPorcentajeComision() {
		return porcentajeComision;
	}

	public void setPorcentajeComision(String porcentajeComision) {
		this.porcentajeComision = porcentajeComision;
	}

	public String getPorcentajeComisionMayor() {
		return porcentajeComisionMayor;
	}

	public void setPorcentajeComisionMayor(String porcentajeComisionMayor) {
		this.porcentajeComisionMayor = porcentajeComisionMayor;
	}

	public String getTipoCliente() {
		return tipoCliente;
	}

	public void setTipoCliente(String tipoCliente) {
		this.tipoCliente = tipoCliente;
	}

	public String getCantidadVendida() {
		return cantidadVendida;
	}

	public void setCantidadVendida(String cantidadVendida) {
		this.cantidadVendida = cantidadVendida;
	}

	public String getValorVendido() {
		return valorVendido;
	}

	public void setValorVendido(String valorVendido) {
		this.valorVendido = valorVendido;
	}

	public String getValoComisionTotal() {
		return valoComisionTotal;
	}

	public void setValoComisionTotal(String valoComisionTotal) {
		this.valoComisionTotal = valoComisionTotal;
	}
}
