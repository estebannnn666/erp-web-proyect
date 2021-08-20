package ec.com.erp.web.reportes.response;

import java.io.Serializable;

/**
 * 
 * @author Esteban Gudino
 * 2020-09-13
 */
public class ReporteExistenciaResponse implements Serializable{
	
	private static final long serialVersionUID = 7863262235394607247L;
	
	private String fechaMovimiento ;
	
	private String codigoBarras ;
	
	private String nombreArticulo;
	
	private String unidadMedida;
	
	private String detalleMovimiento;

	private String cantidadExistencia;
	
	private String valorUnidadExistencia;
	
	private String valorTotalExistencia;

	public String getFechaMovimiento() {
		return fechaMovimiento;
	}

	public void setFechaMovimiento(String fechaMovimiento) {
		this.fechaMovimiento = fechaMovimiento;
	}

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

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

	public String getDetalleMovimiento() {
		return detalleMovimiento;
	}

	public void setDetalleMovimiento(String detalleMovimiento) {
		this.detalleMovimiento = detalleMovimiento;
	}

	public String getCantidadExistencia() {
		return cantidadExistencia;
	}

	public void setCantidadExistencia(String cantidadExistencia) {
		this.cantidadExistencia = cantidadExistencia;
	}

	public String getValorUnidadExistencia() {
		return valorUnidadExistencia;
	}

	public void setValorUnidadExistencia(String valorUnidadExistencia) {
		this.valorUnidadExistencia = valorUnidadExistencia;
	}

	public String getValorTotalExistencia() {
		return valorTotalExistencia;
	}

	public void setValorTotalExistencia(String valorTotalExistencia) {
		this.valorTotalExistencia = valorTotalExistencia;
	}
}
