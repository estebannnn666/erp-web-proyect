
package ec.com.erp.web.inventario.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang3.StringUtils;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.cliente.mdl.dto.ArticuloUnidadManejoDTO;
import ec.com.erp.cliente.mdl.dto.InventarioDTO;
import ec.com.erp.utilitario.commons.util.HtmlPdf;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.inventario.datamanager.InventarioDataManager;

/**
 * Controlador para administracion de inventarios
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class InventarioController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{inventarioDataManager}")
	private InventarioDataManager inventarioDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private InventarioDTO inventarioDTO;
	private ArticuloDTO articuloDTO;
	private Collection<InventarioDTO> inventarioDTOCols;
	private String codigoBarras;
	private Integer page;
	private Boolean inventarioCreado;
	private Date fechaInicioBusqueda;
	private Date fechaFinBusqueda;
	private String tipoMovimiento;
	private String codigoBarrasNuevo;
	private Collection<ArticuloDTO> articuloDTOCols;
	private Integer codigoArticuloSeleccionado;
	private Boolean existeInventario;
	private String codigoBarrasBusqueda;
	private String nombreArticuloBusqueda;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		// Inicializar fechas para filtros de busqueda
		Calendar fechaInferior = Calendar.getInstance();
		fechaInferior.set(Calendar.MONTH, 0);
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		Calendar fechaSuperior = Calendar.getInstance();
		fechaInicioBusqueda = fechaInferior.getTime();
		fechaFinBusqueda = fechaSuperior.getTime();
		existeInventario = Boolean.FALSE;
				
		this.inventarioCreado = Boolean.FALSE;
		this.inventarioDTO = new InventarioDTO();
		this.articuloDTO = new ArticuloDTO();
		this.articuloDTOCols =  new ArrayList<ArticuloDTO>();

		this.page = 0;
		if(inventarioDataManager.getInventarioDTOEditar() != null && inventarioDataManager.getInventarioDTOEditar().getId().getCodigoInventario() != null)
		{
			this.setInventarioDTO(inventarioDataManager.getInventarioDTOEditar());
			this.setArticuloDTO(inventarioDataManager.getInventarioDTOEditar().getArticuloDTO());
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return inventarioDataManager;
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
		this.buscarInventario();
	}
	/**
	 * Metodo para buscar inventario
	 * @param e
	 */
	public void busquedaInventarioEnter(AjaxBehaviorEvent e){
		this.buscarInventario();
	}
	
	/**
	 * Metodo para buscar inventario
	 * @param e
	 */
	public void buscarInventario(){
		try {
			if(StringUtils.isNotEmpty(codigoBarras)) {
				Calendar fechaInicio = Calendar.getInstance();
				Calendar fechaFin = Calendar.getInstance();
				fechaInicio.setTime(fechaInicioBusqueda);
				fechaFin.setTime(fechaFinBusqueda);
				UtilitarioWeb.cleanDate(fechaInicio);
				UtilitarioWeb.cleanDate(fechaFin);
				fechaFin.add(Calendar.DATE, 1);
				
				this.inventarioDTOCols = ERPFactory.inventario.getInventarioServicio().findObtenerListaInventarioByArticuloFechas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), codigoBarras, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()));
				if(CollectionUtils.isEmpty(this.inventarioDTOCols)){
					this.setShowMessagesBar(Boolean.TRUE);
					MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.lista.resultado"));
				}
				else {
					this.setArticuloDTO(this.inventarioDTOCols.iterator().next().getArticuloDTO());
				}
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.mensaje.codigo.barras.requerido"));
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
	 * Metodo para guardar o actualizar inventario
	 * @param e
	 */
	public void guadarActualizarInventario(ActionEvent e){
		try {
			this.setInventarioCreado(Boolean.FALSE);
			if(this.validarPantallaInventario()) {
				this.inventarioDTO.setDetalleMoviento(this.inventarioDTO.getDetalleMoviento().toUpperCase());
				this.inventarioDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.inventarioDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				// Se obtiene existencia actual
				InventarioDTO inventarioDTOAux = ERPFactory.inventario.getInventarioServicio().findObtenerUltimoInventarioByArticulo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.codigoBarrasNuevo, inventarioDTO.getCodigoArticuloUnidadManejo());
				
				if(this.tipoMovimiento.equals(ERPConstantes.ESTADO_INACTIVO_NUMERICO)) {
					this.inventarioDTO.setCantidadSalida(this.inventarioDTO.getCantidadEntrada());
					this.inventarioDTO.setValorUnidadSalida(this.inventarioDTO.getValorUnidadEntrada());
					this.inventarioDTO.setValorTotalSalida(this.inventarioDTO.getValorTotalEntrada());
					if(inventarioDTOAux == null) {
						this.setShowMessagesBar(Boolean.TRUE);
						MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.mensaje.sin.existencias"));
						return;
					}else {
						if(this.inventarioDTO.getCantidadSalida().intValue() > inventarioDTOAux.getCantidadExistencia().intValue()) {
							this.setShowMessagesBar(Boolean.TRUE);
							MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.mensaje.sin.existencias"));
							return;
						}
					}
					this.inventarioDTO.setCantidadExistencia(inventarioDTOAux.getCantidadExistencia().intValue() - this.inventarioDTO.getCantidadSalida());
					this.inventarioDTO.setValorUnidadExistencia(this.inventarioDTO.getValorUnidadSalida());
					Double cantidadUnidades = Double.valueOf(this.inventarioDTO.getCantidadExistencia() * this.inventarioDTO.getArticuloUnidadManejoDTO().getValorUnidadManejo());
					this.inventarioDTO.setValorTotalExistencia(this.inventarioDTO.getValorUnidadExistencia().multiply(BigDecimal.valueOf(cantidadUnidades)));
					this.inventarioDTO.setCantidadEntrada(null);
					this.inventarioDTO.setValorUnidadEntrada(null);
					this.inventarioDTO.setValorTotalEntrada(null);
				}else {
					this.inventarioDTO.setValorUnidadExistencia(this.inventarioDTO.getValorUnidadEntrada());
					if(inventarioDTOAux == null) {
						this.inventarioDTO.setCantidadExistencia(this.inventarioDTO.getCantidadEntrada());
						this.inventarioDTO.setValorTotalExistencia(this.inventarioDTO.getValorTotalEntrada());
					}else {
						this.inventarioDTO.setCantidadExistencia(inventarioDTOAux.getCantidadExistencia().intValue() + this.inventarioDTO.getCantidadEntrada());
						Double cantidadUnidades = Double.valueOf(this.inventarioDTO.getCantidadExistencia() * this.inventarioDTO.getArticuloUnidadManejoDTO().getValorUnidadManejo());
						this.inventarioDTO.setValorTotalExistencia(this.inventarioDTO.getValorUnidadExistencia().multiply(BigDecimal.valueOf(cantidadUnidades)));
					}
					this.inventarioDTO.setCantidadSalida(null);
					this.inventarioDTO.setValorUnidadSalida(null);
					this.inventarioDTO.setValorTotalSalida(null);
				}
				ERPFactory.inventario.getInventarioServicio().transCrearActualizarInventario(this.inventarioDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setInventarioCreado(Boolean.TRUE);
				MensajesController.addInfo(null, "El movimiento se registr\u00F3 correctamente.");
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
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
	 * Metodo para buscar articulos
	 * @param e
	 */
	public void busquedaArticulos(ActionEvent e){
		this.setShowMessagesBar(Boolean.FALSE);
		buscarArticulos();
	}
	
	/**
	 * Metodo para buscar vendedores al dar enter
	 * @param e
	 */
	public void busquedaArticulosEnter(AjaxBehaviorEvent e){
		this.buscarArticulos();
	}
	
	/**
	 * Metodo para buscar articulos
	 */
	public void buscarArticulos(){
		try {
			this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), codigoBarrasBusqueda, nombreArticuloBusqueda);
			if(CollectionUtils.isEmpty(this.articuloDTOCols)){
				this.setShowMessagesBar(Boolean.TRUE);
				MensajesController.addInfo(null, "No se encontraron resultados para la b\u00FAsqueda realizada.");
			}
			this.codigoBarrasBusqueda = null;
			this.nombreArticuloBusqueda = null;
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Borrar filtro de busqueda por documento vendedor
	 */
	public void borrarBusquedaCodigoBarra(ActionEvent e){
		this.codigoBarrasBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de nombre vendedor
	 */
	public void borrarBusquedaNombreArticulo(ActionEvent e){
		this.nombreArticuloBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	
	/**
	 * Seleccionar articulo del popUp
	 * @param e
	 */
	public void seleccionarArticulo(ValueChangeEvent e)
	{
		this.codigoArticuloSeleccionado = (Integer)e.getNewValue();
	}
	
	/**
	 * Metodo para agragar el articulo a la vista
	 */
	public void agragarArticuloSeleccionado(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoArticulo", PredicateUtils.equalPredicate(this.codigoArticuloSeleccionado));
		// Validacion de objeto existente
		this.articuloDTO  = (ArticuloDTO) CollectionUtils.find(this.articuloDTOCols, testPredicate);
		
		//if(this.tipoMovimiento.equals(ERPConstantes.ESTADO_INACTIVO_NUMERICO)) {
			//this.inventarioDTO.setValorUnidadEntrada(this.articuloDTO.getPrecio());
		//}else {
		this.inventarioDTO.setValorUnidadEntrada(this.articuloDTO.getCosto());
		//}
		this.inventarioDTO.setCodigoArticulo(this.articuloDTO.getId().getCodigoArticulo());
		this.inventarioDTO.setCantidadEntrada(null);
		this.inventarioDTO.setValorTotalEntrada(null);
		this.inventarioDTO.setArticuloDTO(this.articuloDTO);
		this.codigoBarrasNuevo = this.articuloDTO.getCodigoBarras();
	}
	
	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaInventario() {
		Boolean validado = Boolean.TRUE;
		if(StringUtils.isEmpty(this.codigoBarrasNuevo)){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.campos.codigobarra.requerido"));
			validado = Boolean.FALSE;
		}
		if(inventarioDTO.getCodigoArticuloUnidadManejo() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.campos.unidadmanejo.requerido"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(inventarioDTO.getDetalleMoviento())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.campos.descripcion.requerido"));
			validado = Boolean.FALSE;
		}
		if(inventarioDTO.getCantidadEntrada() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.campos.cantidad.requerido"));
			validado = Boolean.FALSE;
		}
		if(inventarioDTO.getValorUnidadEntrada() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.campos.valor.unidad.requerido"));
			validado = Boolean.FALSE;
		}
		return validado;
	}
	
	/**
	 * Metodo borrar pantalla y crear un inventario nuevo
	 * @param e
	 */
	public void clearNuevoInventario(ActionEvent e){
		this.setInventarioCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.inventarioDTO = new InventarioDTO();
		this.inventarioDataManager.setInventarioDTOEditar(new InventarioDTO());
		this.codigoBarrasNuevo = "";
		this.articuloDTO = new ArticuloDTO();
		this.tipoMovimiento = null;
		this.codigoBarrasBusqueda = null;
		this.nombreArticuloBusqueda = null;
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarInventario() {
		if(this.inventarioDTO == null) {
			return null;
		}else{
			this.inventarioDataManager.setInventarioDTOEditar(this.inventarioDTO);
			return "/modules/inventario/nuevoInventario.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo para validar si existen datos que imprimir pantalla
	 * @param e
	 */
	public void validarImpresion(ActionEvent e){
		if(CollectionUtils.isEmpty(this.inventarioDTOCols)) {
			existeInventario = Boolean.FALSE;
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.mensaje.reporte.inventario"));
		}else {
			existeInventario = Boolean.TRUE;
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
		this.inventarioDataManager.setInventarioDTOEditar(new InventarioDTO());
		return "/modules/inventario/adminBusquedaInventario.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nueva entrada o salida
	 * @return
	 */
	public String crearNuevoInventario(){
		return "/modules/inventario/nuevoInventario.xhtml?faces-redirect=true";
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
	public void borrarBusquedaCodigoBarras(ActionEvent e){
		this.codigoBarras = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de fechas 
	 */
	public void borrarBusquedaFecha(ActionEvent e){
		this.fechaInicioBusqueda = new Date();
		this.fechaFinBusqueda = new Date();
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para consultar articulo por codigo de barras 
	 * @param e
	 */
	public void realizarConsultaArticulos(AjaxBehaviorEvent e) {
		this.codigoBarrasBusqueda = null;
		this.nombreArticuloBusqueda = null;
		this.setShowMessagesBar(Boolean.FALSE);
		Collection<ArticuloDTO> articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), codigoBarrasNuevo, null);
		if(CollectionUtils.isNotEmpty(articuloDTOCols)) {
			ArticuloDTO articuloTem = articuloDTOCols.iterator().next();
			if(this.tipoMovimiento.equals(ERPConstantes.ESTADO_INACTIVO_NUMERICO)) {
				this.inventarioDTO.setValorUnidadEntrada(articuloTem.getPrecio());
			}else {
				this.inventarioDTO.setValorUnidadEntrada(articuloTem.getCosto());
			}
			this.inventarioDTO.setCodigoArticulo(articuloTem.getId().getCodigoArticulo());
			this.inventarioDTO.setCantidadEntrada(null);
			this.inventarioDTO.setValorTotalEntrada(null);
			this.inventarioDTO.setArticuloDTO(articuloTem);
		}else {
			this.setShowMessagesBar(Boolean.TRUE);
			this.inventarioDTO.setValorUnidadEntrada(null);
			this.inventarioDTO.setCantidadEntrada(null);
			this.inventarioDTO.setValorTotalEntrada(null);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.inventario.mensaje.informacion.articulo.noexiste"));
		}
	}
	
	/**
	 * Metodo para agregar catalogo de unidad de manejo
	 * @param e
	 */
	public void seleccionarUnidadManejo(ValueChangeEvent e) {
		this.setShowMessagesBar(Boolean.FALSE);
		Integer codigoArticuloUnidadManejo = (Integer)e.getNewValue();
		ArticuloUnidadManejoDTO unidadManejo = inventarioDTO.getArticuloDTO().getArticuloUnidadManejoDTOCols().stream()
				.filter(catalogo-> catalogo.getId().getCodigoArticuloUnidadManejo().equals(codigoArticuloUnidadManejo))
				.findFirst().orElse(null);
		if(unidadManejo != null) {
			this.inventarioDTO.setCodigoArticuloUnidadManejo(codigoArticuloUnidadManejo);
			this.inventarioDTO.setArticuloUnidadManejoDTO(unidadManejo);
			this.calcularTotalMovimientoKeyUp(null);
		}
	}
	
	/**
	 * Metodo para agregar catalogo de unidad de manejo
	 * @param e
	 */
	public void seleccionarTipoInventario(ValueChangeEvent e) {
		this.setShowMessagesBar(Boolean.FALSE);
		String codigoTipoInventario = (String)e.getNewValue();
		if(this.inventarioDTO.getArticuloDTO() != null) {
			if(codigoTipoInventario.equals(ERPConstantes.ESTADO_INACTIVO_NUMERICO)) {
				this.inventarioDTO.setValorUnidadEntrada(this.inventarioDTO.getArticuloDTO().getPrecio());
			}else {
				this.inventarioDTO.setValorUnidadEntrada(this.inventarioDTO.getArticuloDTO().getCosto());
			}
			this.calcularTotalMovimientoKeyUp(null);
		}
	}
	
	/**
	 * Calcular el total
	 * @param e
	 */
	public void calcularTotalMovimientoKeyUp(AjaxBehaviorEvent e) {
		if(this.inventarioDTO.getCantidadEntrada() != null && this.inventarioDTO.getValorUnidadEntrada() != null && this.inventarioDTO.getCodigoArticuloUnidadManejo() != null) {
			Double totalUnidades = Double.valueOf(this.inventarioDTO.getCantidadEntrada().intValue() * this.inventarioDTO.getArticuloUnidadManejoDTO().getValorUnidadManejo().intValue());
			BigDecimal subTotal = BigDecimal.valueOf(totalUnidades).multiply(this.inventarioDTO.getValorUnidadEntrada());
			this.inventarioDTO.setValorTotalEntrada(subTotal);		
		}
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public void imprimirKardex() {
		HtmlPdf htmltoPDF;
		try {
			// Plantilla rpincipal que permite la conversion de xsl a pdf
			htmltoPDF = new HtmlPdf(ERPConstantes.PLANTILLA_XSL_FOPRINCIPAL);
			HashMap<String , String> parametros = new HashMap<String, String>();
			byte contenido[] = htmltoPDF.convertir(ERPFactory.inventario.getInventarioServicio().findObtenerXMLReporteKardex(this.inventarioDTOCols, fechaInicioBusqueda, fechaFinBusqueda).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""), "", "",	parametros,	null);
			UtilitarioWeb.mostrarPDF(contenido);				
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir");
		}
	}
	
	public void setInventarioDataManager(InventarioDataManager inventarioDataManager) {
		this.inventarioDataManager = inventarioDataManager;
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

	public InventarioDTO getInventarioDTO() {
		return inventarioDTO;
	}

	public void setInventarioDTO(InventarioDTO inventarioDTO) {
		this.inventarioDTO = inventarioDTO;
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

	public Boolean getInventarioCreado() {
		return inventarioCreado;
	}

	public void setInventarioCreado(Boolean inventarioCreado) {
		this.inventarioCreado = inventarioCreado;
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

	public String getCodigoBarrasNuevo() {
		return codigoBarrasNuevo;
	}

	public void setCodigoBarrasNuevo(String codigoBarrasNuevo) {
		this.codigoBarrasNuevo = codigoBarrasNuevo;
	}

	public ArticuloDTO getArticuloDTO() {
		return articuloDTO;
	}

	public void setArticuloDTO(ArticuloDTO articuloDTO) {
		this.articuloDTO = articuloDTO;
	}

	public Collection<ArticuloDTO> getArticuloDTOCols() {
		return articuloDTOCols;
	}

	public void setArticuloDTOCols(Collection<ArticuloDTO> articuloDTOCols) {
		this.articuloDTOCols = articuloDTOCols;
	}

	public Integer getCodigoArticuloSeleccionado() {
		return codigoArticuloSeleccionado;
	}

	public void setCodigoArticuloSeleccionado(Integer codigoArticuloSeleccionado) {
		this.codigoArticuloSeleccionado = codigoArticuloSeleccionado;
	}

	public Boolean getExisteInventario() {
		return existeInventario;
	}

	public void setExisteInventario(Boolean existeInventario) {
		this.existeInventario = existeInventario;
	}

	public String getCodigoBarrasBusqueda() {
		return codigoBarrasBusqueda;
	}

	public void setCodigoBarrasBusqueda(String codigoBarrasBusqueda) {
		this.codigoBarrasBusqueda = codigoBarrasBusqueda;
	}

	public String getNombreArticuloBusqueda() {
		return nombreArticuloBusqueda;
	}

	public void setNombreArticuloBusqueda(String nombreArticuloBusqueda) {
		this.nombreArticuloBusqueda = nombreArticuloBusqueda;
	}
	
}
