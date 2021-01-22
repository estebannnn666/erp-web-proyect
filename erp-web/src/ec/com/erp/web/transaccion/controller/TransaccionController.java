
package ec.com.erp.web.transaccion.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.TransaccionDTO;
import ec.com.erp.cliente.mdl.dto.id.TransaccionID;
import ec.com.erp.utilitario.commons.util.HtmlPdf;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.transaccion.datamanager.TransaccionDataManager;

/**
 * Controlador para administracion de transacciones
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class TransaccionController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{transaccionDataManager}")
	private TransaccionDataManager transaccionDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private TransaccionDTO transaccionDTO;
	private Collection<TransaccionDTO> transaccionDTOCols;
	private Collection<TransaccionDTO> transaccionIngresosDTOCols;
	private Collection<TransaccionDTO> transaccionGastosDTOCols;
	private Collection<CatalogoValorDTO> transaccionesCols;
	private Date fechaInicioTransaccion;
	private Date fechaFinTransaccion;
	private String tipoTransaccion;
	private Integer page;
	private Boolean transaccionCreada;
	private Boolean modoEdicion;
	private Double totalIngresos;
	private Double totalGastos;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.transaccionDTOCols = new ArrayList<>();
		this.transaccionIngresosDTOCols = new ArrayList<>();
		this.transaccionGastosDTOCols = new ArrayList<>();
		this.transaccionCreada = Boolean.FALSE;
		this.modoEdicion = Boolean.FALSE;
		this.transaccionDTO = new TransaccionDTO();
		transaccionDTO.setFechaTransaccion(new Date());
		this.page = 0;
		this.transaccionesCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_TRANSACCION);
		SecuenciaDTO secuenciaTransaccion = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(TransaccionID.NOMBRE_SECUENCIA);
		this.transaccionDTO.setNumeroTransaccion("TRA-"+secuenciaTransaccion.getValorSecuencia());
		// Inicializar fechas para filtros de busqueda
		Calendar fechaInferior = Calendar.getInstance();
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		Calendar fechaSuperior = Calendar.getInstance();
		fechaInicioTransaccion = fechaInferior.getTime();
		fechaFinTransaccion = fechaSuperior.getTime();
		this.totalGastos = 0.0;
		this.totalIngresos = 0.0;
		
		if(transaccionDataManager.getTransaccionDTOEditar() != null && transaccionDataManager.getTransaccionDTOEditar().getId().getCodigoTransaccion() != null){
			this.setTransaccionDTO(transaccionDataManager.getTransaccionDTOEditar());
			this.modoEdicion = Boolean.TRUE;
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/transacciones/adminBusquedaTransaccion.xhtml")) {
			this.transaccionDTOCols = ERPFactory.transaccion.getTransaccionServicio().findObtenerListaTransacciones(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), new Timestamp(fechaInferior.getTime().getTime()), new Timestamp(fechaSuperior.getTime().getTime()),null);
			if(CollectionUtils.isNotEmpty(transaccionDTOCols)) {
				// Obtener ingresos
				this.transaccionIngresosDTOCols = transaccionDTOCols.stream()
						.filter(transac -> transac.getCodigoValorTransaccion().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TRANSACCION_INGRESO))
						.collect(Collectors.toList());
				BigDecimal ingresos = this.transaccionIngresosDTOCols.stream()
						.filter(ingreso ->ingreso != null && ingreso.getValorTransaccion()!=null)
						.map(TransaccionDTO::getValorTransaccion)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				this.totalIngresos = ingresos.doubleValue();
				// Obtener gastos		
				this.transaccionGastosDTOCols = transaccionDTOCols.stream()
						.filter(transac -> transac.getCodigoValorTransaccion().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TRANSACCION_GASTO))
						.collect(Collectors.toList());
				BigDecimal gastos = this.transaccionGastosDTOCols.stream()
						.filter(ingreso ->ingreso != null && ingreso.getValorTransaccion()!=null)
						.map(TransaccionDTO::getValorTransaccion)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				this.totalGastos = gastos.doubleValue();
			}
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return transaccionDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para buscar articulos
	 * @param e
	 */
	public void busquedaTransacciones(ActionEvent e){
		this.buscarTransacciones();
	}
	
	public void busquedaTransaccionesEnter(AjaxBehaviorEvent e){
		this.buscarTransacciones();
	}
	
	
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public String imprimirTransacciones() {
		HtmlPdf htmltoPDF;
		try {
			// Plantilla rpincipal que permite la conversion de xsl a pdf
			htmltoPDF = new HtmlPdf(ERPConstantes.PLANTILLA_XSL_FOPRINCIPAL);
			HashMap<String , String> parametros = new HashMap<String, String>();
			byte contenido[] = htmltoPDF.convertir(ERPFactory.transaccion.getTransaccionServicio().findObtenerXMLReporteTransacciones(this.transaccionDTOCols, this.fechaInicioTransaccion, this.fechaFinTransaccion).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""), "", "",	parametros,	null);
			UtilitarioWeb.mostrarPDF(contenido);
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir");
		}
		return null;
	}
	
	public void buscarTransacciones(){
		try {
			Calendar fechaInicio = Calendar.getInstance();
			Calendar fechaFin = Calendar.getInstance();
			fechaInicio.setTime(fechaInicioTransaccion);
			fechaFin.setTime(fechaFinTransaccion);
			UtilitarioWeb.cleanDate(fechaInicio);
			UtilitarioWeb.cleanDate(fechaFin);
			fechaFin.add(Calendar.DATE, 1);
			
			this.transaccionDTOCols = ERPFactory.transaccion.getTransaccionServicio().findObtenerListaTransacciones(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()),null);
			if(CollectionUtils.isEmpty(transaccionDTOCols)){
				this.transaccionIngresosDTOCols = new ArrayList<>();
				this.transaccionGastosDTOCols = new ArrayList<>();
				this.totalIngresos = 0.0;
				this.totalGastos = 0.0;
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("No se encontraron resultados para la b\u00FAsqueda realizada.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}else {
				this.setShowMessagesBar(Boolean.FALSE);
				// Obtener ingresos
				this.transaccionIngresosDTOCols = transaccionDTOCols.stream()
						.filter(transac -> transac.getCodigoValorTransaccion().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TRANSACCION_INGRESO))
						.collect(Collectors.toList());
				BigDecimal ingresos = this.transaccionIngresosDTOCols.stream()
						.filter(ingreso ->ingreso != null && ingreso.getValorTransaccion()!=null)
						.map(TransaccionDTO::getValorTransaccion)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				this.totalIngresos = ingresos.doubleValue();
				// Obtener gastos		
				this.transaccionGastosDTOCols = transaccionDTOCols.stream()
						.filter(transac -> transac.getCodigoValorTransaccion().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TRANSACCION_GASTO))
						.collect(Collectors.toList());
				BigDecimal gastos = this.transaccionGastosDTOCols.stream()
						.filter(ingreso ->ingreso != null && ingreso.getValorTransaccion()!=null)
						.map(TransaccionDTO::getValorTransaccion)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				this.totalGastos = gastos.doubleValue();
			}
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e1.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e2.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	/**
	 * Metodo para guardar o actualizar transacciones
	 * @param e
	 */
	public void guadarActualizarTransaccion(ActionEvent e){
		try {
			this.setTransaccionCreada(Boolean.FALSE);
			if(this.validarPantallaTransaccion()) {
				transaccionDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				transaccionDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				transaccionDTO.setConcepto(transaccionDTO.getConcepto().toUpperCase());
				ERPFactory.transaccion.getTransaccionServicio().transGuardarTransaccion(transaccionDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setTransaccionCreada(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("El art\u00EDculo se cre\u00F3 correctamente.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e1.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e2.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
	}
	
	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaTransaccion() {
		Boolean validado = Boolean.TRUE;
		if(StringUtils.isEmpty(transaccionDTO.getConcepto())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.transacciones.mensaje.campo.requerido.concepto"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(transaccionDTO.getCodigoValorTransaccion())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.transacciones.mensaje.campo.requerido.tipo"));
			validado = Boolean.FALSE;
		}
		if(transaccionDTO.getFechaTransaccion() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.transacciones.mensaje.campo.requerido.fecha"));
			validado = Boolean.FALSE;
		}
		if(transaccionDTO.getValorTransaccion() ==  null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.transacciones.mensaje.campo.requerido.valor"));
			validado = Boolean.FALSE;
		}
		return validado;
	}
	
	/**
	 * Metodo borrar pantalla y crear una nueva transaccion
	 * @param e
	 */
	public void clearNuevaTransaccion(ActionEvent e){
		this.setTransaccionCreada(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.transaccionDTO = new TransaccionDTO();
		this.transaccionDTO.setFechaTransaccion(new Date());
		SecuenciaDTO secuenciaTransaccion = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(TransaccionID.NOMBRE_SECUENCIA);
		this.transaccionDTO.setNumeroTransaccion("TRA-"+secuenciaTransaccion.getValorSecuencia());
		this.transaccionDataManager.setTransaccionDTOEditar(new TransaccionDTO());
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarTransaccion() {
		if(this.transaccionDTO == null) {
			return null;
		}else{
			this.transaccionDataManager.setTransaccionDTOEditar(this.transaccionDTO);
			return "/modules/transacciones/nuevaTransaccion.xhtml?faces-redirect=true";
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
	 * Metodo para regresar a la busqueda de articulos
	 * @param e
	 */
	public String regresarBusquedaTransacciones(){
		this.transaccionDataManager.setTransaccionDTOEditar(new TransaccionDTO());
		return "/modules/transacciones/adminBusquedaTransaccion.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo articulo
	 * @return
	 */
	public String crearNuevaTransaccion(){
		return "/modules/transacciones/nuevaTransaccion.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		this.loginController.desActivarMenusSeleccionado();
		this.loginController.setActivarInicio(Boolean.TRUE);
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaCondigoBarras(ActionEvent e){
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaNombreArticulo(ActionEvent e){
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	public void setTransaccionDataManager(TransaccionDataManager transaccionDataManager) {
		this.transaccionDataManager = transaccionDataManager;
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

	public Boolean getModoEdicion() {
		return modoEdicion;
	}

	public void setModoEdicion(Boolean modoEdicion) {
		this.modoEdicion = modoEdicion;
	}

	public TransaccionDTO getTransaccionDTO() {
		return transaccionDTO;
	}

	public void setTransaccionDTO(TransaccionDTO transaccionDTO) {
		this.transaccionDTO = transaccionDTO;
	}

	public Collection<TransaccionDTO> getTransaccionIngresosDTOCols() {
		return transaccionIngresosDTOCols;
	}

	public void setTransaccionIngresosDTOCols(Collection<TransaccionDTO> transaccionIngresosDTOCols) {
		this.transaccionIngresosDTOCols = transaccionIngresosDTOCols;
	}

	public Collection<TransaccionDTO> getTransaccionGastosDTOCols() {
		return transaccionGastosDTOCols;
	}

	public void setTransaccionGastosDTOCols(Collection<TransaccionDTO> transaccionGastosDTOCols) {
		this.transaccionGastosDTOCols = transaccionGastosDTOCols;
	}

	public Boolean getTransaccionCreada() {
		return transaccionCreada;
	}

	public void setTransaccionCreada(Boolean transaccionCreada) {
		this.transaccionCreada = transaccionCreada;
	}

	public String getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}

	public Collection<CatalogoValorDTO> getTransaccionesCols() {
		return transaccionesCols;
	}

	public void setTransaccionesCols(Collection<CatalogoValorDTO> transaccionesCols) {
		this.transaccionesCols = transaccionesCols;
	}

	public Date getFechaInicioTransaccion() {
		return fechaInicioTransaccion;
	}

	public void setFechaInicioTransaccion(Date fechaInicioTransaccion) {
		this.fechaInicioTransaccion = fechaInicioTransaccion;
	}

	public Date getFechaFinTransaccion() {
		return fechaFinTransaccion;
	}

	public void setFechaFinTransaccion(Date fechaFinTransaccion) {
		this.fechaFinTransaccion = fechaFinTransaccion;
	}

	public Double getTotalIngresos() {
		return totalIngresos;
	}

	public void setTotalIngresos(Double totalIngresos) {
		this.totalIngresos = totalIngresos;
	}

	public Double getTotalGastos() {
		return totalGastos;
	}

	public void setTotalGastos(Double totalGastos) {
		this.totalGastos = totalGastos;
	}
}
