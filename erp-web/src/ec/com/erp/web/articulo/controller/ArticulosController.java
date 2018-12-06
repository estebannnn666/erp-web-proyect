
package ec.com.erp.web.articulo.controller;

import java.io.Serializable;
import java.util.Collection;

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
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
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
	private String codigoBarrasBusqueda;
	private String nombreArticuloBusqueda;
	private Integer page;
	private Boolean articuloCreado;
	private Boolean modoEdicion;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.articuloCreado = Boolean.FALSE;
		this.modoEdicion = Boolean.FALSE;
		this.articuloDTO = new ArticuloDTO();
		this.page = 0;
		if(articulosDataManager.getArticuloDTOEditar() != null && articulosDataManager.getArticuloDTOEditar().getId().getCodigoArticulo() != null)
		{
			this.setArticuloDTO(articulosDataManager.getArticuloDTOEditar());
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
					ERPFactory.articulos.getArticuloServicio().transGuardarActualizarArticulo(articuloDTO);
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
						ERPFactory.articulos.getArticuloServicio().transGuardarActualizarArticulo(articuloDTO);
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
	}
	
	/**
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaNombreArticulo(ActionEvent e){
		this.nombreArticuloBusqueda = "";
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
}
