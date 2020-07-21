
package ec.com.erp.web.articulo.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.cliente.mdl.dto.ArticuloImpuestoDTO;
import ec.com.erp.cliente.mdl.dto.ImpuestoDTO;
import ec.com.erp.web.articulo.datamanager.ArticulosDataManager;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;

/**
 * Controlador para administracion de articulos
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class ArticulosController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{articulosDataManager}")
	private ArticulosDataManager articulosDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private ArticuloDTO articuloDTO;
	private Collection<ArticuloDTO> articuloDTOCols;
	private Collection<ArticuloImpuestoDTO> articuloImpuestoDTOCols;
	private Collection<ImpuestoDTO> impuestoDTOCols;
	private ArticuloImpuestoDTO articuloImpuestoDTO;
	private String codigoBarrasBusqueda;
	private String nombreArticuloBusqueda;
	private Long codigoImpuestoSeleccionado;
	private Integer page;
	private Boolean articuloCreado;
	private Boolean modoEdicion;
	private ImpuestoDTO impuestoDTO;
	private Boolean impuestoCreado;

	@PostConstruct
	public void postConstruct() {
		this.impuestoCreado = Boolean.FALSE;
		this.loginController.activarMenusSeleccionado();
		this.articuloImpuestoDTOCols = new ArrayList<ArticuloImpuestoDTO>();
		this.impuestoDTOCols = new ArrayList<ImpuestoDTO>();
		articuloImpuestoDTO = new ArticuloImpuestoDTO();
		this.impuestoDTO = new ImpuestoDTO();
		this.articuloCreado = Boolean.FALSE;
		this.modoEdicion = Boolean.FALSE;
		this.articuloDTO = new ArticuloDTO();
		this.page = 0;
		
		if(articulosDataManager.getArticuloDTOEditar() != null && articulosDataManager.getArticuloDTOEditar().getId().getCodigoArticulo() != null){
			this.setArticuloDTO(articulosDataManager.getArticuloDTOEditar());
			if(CollectionUtils.isNotEmpty(articulosDataManager.getArticuloDTOEditar().getArticuloImpuestoDTOCols())){
				this.setArticuloImpuestoDTOCols(articulosDataManager.getArticuloDTOEditar().getArticuloImpuestoDTOCols());
			}
			this.modoEdicion = Boolean.TRUE;
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/articulos/adminBusquedaArticulos.xhtml")) {
			this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), codigoBarrasBusqueda,nombreArticuloBusqueda);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return articulosDataManager;
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
	public void busquedaArticulos(ActionEvent e){
		this.buscarArticulos();
	}
	
	public void busquedaArticulosEnter(AjaxBehaviorEvent e){
		this.buscarArticulos();
	}
	
	public void buscarArticulos(){
		try {
			this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), codigoBarrasBusqueda,nombreArticuloBusqueda);
			if(CollectionUtils.isEmpty(this.articuloDTOCols)){
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("No se encontraron resultados para la b\u00FAsqueda realizada.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
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
	 * Metodo para guardar o actualizar articulos
	 * @param e
	 */
	public void guadarActualizarArticulo(ActionEvent e){
		try {
			this.setArticuloCreado(Boolean.FALSE);
			if(this.validarPantallaArticulos()) {
				if(this.modoEdicion && articuloDTO.getCodigoBarras().equals(articulosDataManager.getArticuloDTOEditar().getCodigoBarras())) {
					articuloDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
					articuloDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
					articuloDTO.setCodigoBarras(articuloDTO.getCodigoBarras().toUpperCase());
					articuloDTO.setNombreArticulo(articuloDTO.getNombreArticulo().toUpperCase());
					ERPFactory.articulos.getArticuloServicio().transGuardarActualizarArticulo(articuloDTO, articuloImpuestoDTOCols);
					this.setShowMessagesBar(Boolean.TRUE);
					this.setArticuloCreado(Boolean.TRUE);
					FacesMessage msg = new FacesMessage("El art\u00EDculo se cre\u00F3 correctamente.", "ERROR MSG");
			        msg.setSeverity(FacesMessage.SEVERITY_INFO);
			        FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else {
					Collection<ArticuloDTO> articuloDTOBarrasCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), articuloDTO.getCodigoBarras(),null);
					if(CollectionUtils.isNotEmpty(articuloDTOBarrasCols) && !this.modoEdicion){
						this.setShowMessagesBar(Boolean.TRUE);
						FacesMessage msg = new FacesMessage("El c\u00F3digo de barras ingresado ya existe.", "ERROR MSG");
				        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				        FacesContext.getCurrentInstance().addMessage(null, msg);
					}
					else
					{
						articuloDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
						articuloDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
						articuloDTO.setCodigoBarras(articuloDTO.getCodigoBarras().toUpperCase());
						articuloDTO.setNombreArticulo(articuloDTO.getNombreArticulo().toUpperCase());
						ERPFactory.articulos.getArticuloServicio().transGuardarActualizarArticulo(articuloDTO, articuloImpuestoDTOCols);
						this.setShowMessagesBar(Boolean.TRUE);
						this.setArticuloCreado(Boolean.TRUE);
						FacesMessage msg = new FacesMessage("El art\u00EDculo se cre\u00F3 correctamente.", "ERROR MSG");
				        msg.setSeverity(FacesMessage.SEVERITY_INFO);
				        FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			System.out.println("Entro 1");
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e1.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e2.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        System.out.println("Entro 2");
		}
		
	}
	
	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaArticulos() {
		Boolean validado = Boolean.TRUE;
		if(StringUtils.isEmpty(articuloDTO.getCodigoBarras())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.articulos.mensaje.campo.requerido.codigo.barras"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(articuloDTO.getNombreArticulo())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.articulos.mensaje.campo.requerido.nombre.articulo"));
			validado = Boolean.FALSE;
		}
		if(articuloDTO.getPeso() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.articulos.mensaje.campo.requerido.peso.articulo"));
			validado = Boolean.FALSE;
		}
		if(articuloDTO.getCosto() ==  null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.articulos.mensaje.campo.requerido.costo.articulo"));
			validado = Boolean.FALSE;
		}
		if(articuloDTO.getPrecio() ==  null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.articulos.mensaje.campo.requerido.precio.articulo"));
			validado = Boolean.FALSE;
		}
		if(articuloDTO.getCantidadStock() ==  null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.articulos.mensaje.campo.requerido.cantidad.stock"));
			validado = Boolean.FALSE;
		}
		return validado;
	}
	
	/**
	 * Metodo borrar pantalla y crear un articulo nuevo
	 * @param e
	 */
	public void clearNuevoArticulo(ActionEvent e){
		this.impuestoCreado = Boolean.FALSE;
		this.codigoImpuestoSeleccionado = null;
		this.articuloImpuestoDTOCols = new ArrayList<ArticuloImpuestoDTO>();
		this.impuestoDTOCols = new ArrayList<ImpuestoDTO>();
		this.articuloImpuestoDTO = new ArticuloImpuestoDTO();
		this.impuestoDTO = new ImpuestoDTO();
		this.setArticuloCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.articuloDTO = new ArticuloDTO();
		this.articulosDataManager.setArticuloDTOEditar(new ArticuloDTO());
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarArticulo() {
		if(this.articuloDTO == null) {
			return null;
		}else{
			this.articulosDataManager.setArticuloDTOEditar(this.articuloDTO);
			return "/modules/articulos/nuevoArticulo.xhtml?faces-redirect=true";
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
	public String regresarBusquedaArticulos(){
		this.articulosDataManager.setArticuloDTOEditar(new ArticuloDTO());
		return "/modules/articulos/adminBusquedaArticulos.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo articulo
	 * @return
	 */
	public String crearNuevoArticulo(){
		return "/modules/articulos/nuevoArticulo.xhtml?faces-redirect=true";
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
		this.codigoBarrasBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaNombreArticulo(ActionEvent e){
		this.nombreArticuloBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para eliminar registro 
	 * @param articuloImpuestoDTO
	 */
	public void eliminarImpuestoArticulo(ArticuloImpuestoDTO articuloImpuestoDTO) {
		articuloImpuestoDTOCols.remove(articuloImpuestoDTO);
	}
	
	/**
	 * Abrir popup detalle articulos
	 * @param e
	 */
	public void abrirPopUpImpuesto(ActionEvent e) {
		this.impuestoCreado = Boolean.FALSE;
		this.codigoImpuestoSeleccionado = null;
		this.articuloImpuestoDTO = new ArticuloImpuestoDTO();
		this.setImpuestoDTOCols(ERPFactory.impuesto.getImpuestoServicio().findObtenerListaImpuestos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null));
	}
	
	/**
	 * Seleccionar cliente del popUp
	 * @param e
	 */
	public void seleccionImpuesto(ValueChangeEvent e)
	{
		this.codigoImpuestoSeleccionado = (Long)e.getNewValue();
		// Verificar si existe en la coleccion el cliente
		for(ImpuestoDTO impuesto: this.impuestoDTOCols){
			if(impuesto.getId().getCodigoImpuesto().intValue() == this.codigoImpuestoSeleccionado.intValue()){
				this.setImpuestoDTO(impuesto);
			}
		}
		System.out.println(""+this.impuestoDTO);
	}
	
	/**
	 * Agregar nuevo impuesto
	 * @param e
	 */
	public void agregarImpuesto(ActionEvent e) {
		ArticuloImpuestoDTO impTemp = this.articuloImpuestoDTOCols.stream()
				.filter(impuesto-> impuesto.getId().getCodigoImpuesto().intValue() == this.impuestoDTO.getId().getCodigoImpuesto().intValue())
				.findFirst().orElse(null);
		if(impTemp == null) {
			ArticuloImpuestoDTO detalle = new ArticuloImpuestoDTO();
			detalle.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
			detalle.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
			detalle.setImpuestoDTO(this.impuestoDTO);
			detalle.getId().setCodigoImpuesto(this.impuestoDTO.getId().getCodigoImpuesto());
			this.articuloImpuestoDTOCols.add(detalle);
			this.impuestoCreado = Boolean.FALSE;
		}else {
			this.impuestoCreado = Boolean.TRUE;
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.articulos.mensaje.error.impuesto.existente"));
		}
	}
	
	/**
	 * Agregar nuevo impuesto
	 * @param e
	 */
	public void cerrarPopUpImpuesto(ActionEvent e) {
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	public void setArticulosDataManager(ArticulosDataManager articulosDataManager) {
		this.articulosDataManager = articulosDataManager;
	}

	public ArticuloDTO getArticuloDTO() {
		return articuloDTO;
	}

	public void setArticuloDTO(ArticuloDTO articuloDTO) {
		this.articuloDTO = articuloDTO;
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

	public Collection<ArticuloDTO> getArticuloDTOCols() {
		return articuloDTOCols;
	}

	public void setArticuloDTOCols(Collection<ArticuloDTO> articuloDTOCols) {
		this.articuloDTOCols = articuloDTOCols;
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

	public Boolean getArticuloCreado() {
		return articuloCreado;
	}

	public void setArticuloCreado(Boolean articuloCreado) {
		this.articuloCreado = articuloCreado;
	}

	public Boolean getModoEdicion() {
		return modoEdicion;
	}

	public void setModoEdicion(Boolean modoEdicion) {
		this.modoEdicion = modoEdicion;
	}

	public Collection<ArticuloImpuestoDTO> getArticuloImpuestoDTOCols() {
		return articuloImpuestoDTOCols;
	}

	public void setArticuloImpuestoDTOCols(Collection<ArticuloImpuestoDTO> articuloImpuestoDTOCols) {
		this.articuloImpuestoDTOCols = articuloImpuestoDTOCols;
	}

	public ArticuloImpuestoDTO getArticuloImpuestoDTO() {
		return articuloImpuestoDTO;
	}

	public void setArticuloImpuestoDTO(ArticuloImpuestoDTO articuloImpuestoDTO) {
		this.articuloImpuestoDTO = articuloImpuestoDTO;
	}

	public Collection<ImpuestoDTO> getImpuestoDTOCols() {
		return impuestoDTOCols;
	}

	public void setImpuestoDTOCols(Collection<ImpuestoDTO> impuestoDTOCols) {
		this.impuestoDTOCols = impuestoDTOCols;
	}

	public Long getCodigoImpuestoSeleccionado() {
		return codigoImpuestoSeleccionado;
	}

	public void setCodigoImpuestoSeleccionado(Long codigoImpuestoSeleccionado) {
		this.codigoImpuestoSeleccionado = codigoImpuestoSeleccionado;
	}

	public ImpuestoDTO getImpuestoDTO() {
		return impuestoDTO;
	}

	public void setImpuestoDTO(ImpuestoDTO impuestoDTO) {
		this.impuestoDTO = impuestoDTO;
	}

	public Boolean getImpuestoCreado() {
		return impuestoCreado;
	}

	public void setImpuestoCreado(Boolean impuestoCreado) {
		this.impuestoCreado = impuestoCreado;
	}
}
