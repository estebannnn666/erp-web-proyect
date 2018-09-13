
package ec.com.erp.web.reportes.controller;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.InventarioDTO;
import ec.com.erp.utilitario.commons.util.HtmlPdf;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.reportes.datamanager.ReporteDataManager;

/**
 * Controlador para administracion de inventarios
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class ReporteController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{reporteDataManager}")
	private ReporteDataManager reporteDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<InventarioDTO> inventarioDTOCols;
	private String codigoBarras;
	private Integer page;
	private Date fechaInicioBusqueda;
	private Date fechaFinBusqueda;
	private String tipoMovimiento;
	

	@PostConstruct
	public void postConstruct() {
		fechaInicioBusqueda = new Date();
		fechaFinBusqueda = new Date();
		this.page = 0;
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/reportes/adminBusquedaReporte.xhtml")) {
			this.inventarioDTOCols = ERPFactory.inventario.getInventarioServicio().findObtenerListaExistenciasByArticuloFechas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), codigoBarras, null, null);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return reporteDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para buscar inventario
	 * @param e
	 */
	public void busquedaInventario(ActionEvent e){
		try {
				Calendar fechaInicio = Calendar.getInstance();
				Calendar fechaFin = Calendar.getInstance();
				fechaInicio.setTime(fechaInicioBusqueda);
				fechaFin.setTime(fechaFinBusqueda);
				UtilitarioWeb.cleanDate(fechaInicio);
				UtilitarioWeb.cleanDate(fechaFin);
				fechaFin.add(Calendar.DATE, 1);
				
				this.inventarioDTOCols = ERPFactory.inventario.getInventarioServicio().findObtenerListaExistenciasByArticuloFechas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), codigoBarras, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()));
				if(CollectionUtils.isEmpty(this.inventarioDTOCols)){
					this.setShowMessagesBar(Boolean.TRUE);
					MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.lista.resultado"));
				}
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public void refrescarPantalla(ActionEvent e){
		System.out.println("Ingreso a refrescar pantalla");
	}
	
	/**
	 * Metodo para regresar a la busqueda de inventarios
	 * @param e
	 */
	public String regresarBusquedaInventario(){
		return "/modules/reportes/adminBusquedaReporte.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaCodigoBarras(ActionEvent e){
		this.codigoBarras = "";
	}
	
	/**
	 * Borrar filtro de fechas 
	 */
	public void borrarBusquedaFecha(ActionEvent e){
		this.fechaInicioBusqueda = new Date();
		this.fechaFinBusqueda = new Date();
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public void imprimirReporte() {
		HtmlPdf htmltoPDF;
		try {
			// Plantilla rpincipal que permite la conversion de xsl a pdf
			htmltoPDF = new HtmlPdf(ERPConstantes.PLANTILLA_XSL_FOPRINCIPAL);
			HashMap<String , String> parametros = new HashMap<String, String>();
			byte contenido[] = htmltoPDF.convertir(ERPFactory.inventario.getInventarioServicio().findObtenerXMLReporteExistencias(this.inventarioDTOCols).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""), "", "",	parametros,	null);
			UtilitarioWeb.mostrarPDF(contenido);				
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir");
		}
	}
	
	public void setReporteDataManager(ReporteDataManager reporteDataManager) {
		this.reporteDataManager = reporteDataManager;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public Collection<InventarioDTO> getInventarioDTOCols() {
		return inventarioDTOCols;
	}

	public void setInventarioDTOCols(Collection<InventarioDTO> inventarioDTOCols) {
		this.inventarioDTOCols = inventarioDTOCols;
	}

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	public Date getFechaInicioBusqueda() {
		return fechaInicioBusqueda;
	}

	public void setFechaInicioBusqueda(Date fechaInicioBusqueda) {
		this.fechaInicioBusqueda = fechaInicioBusqueda;
	}

	public Date getFechaFinBusqueda() {
		return fechaFinBusqueda;
	}

	public void setFechaFinBusqueda(Date fechaFinBusqueda) {
		this.fechaFinBusqueda = fechaFinBusqueda;
	}

	public String getTipoMovimiento() {
		return tipoMovimiento;
	}

	public void setTipoMovimiento(String tipoMovimiento) {
		this.tipoMovimiento = tipoMovimiento;
	}
}
