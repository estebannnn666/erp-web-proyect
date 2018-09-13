
package ec.com.erp.web.modulos.controller;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ModuloDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.id.ModuloID;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.modulos.datamanager.ModulosDataManager;

/**
 * Controlador para administracion de articulos
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class ModulosController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{modulosDataManager}")
	private ModulosDataManager modulosDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private ModuloDTO moduloDTO;
	private Collection<ModuloDTO> moduloDTOCols;
	private String nombreModulo;
	private Integer page;
	private Boolean moduloCreado;

	@PostConstruct
	public void postConstruct() {
		this.moduloCreado = Boolean.FALSE;
		this.moduloDTO = new ModuloDTO();
		SecuenciaDTO secuenciaDespacho = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(ModuloID.NOMBRE_SECUENCIA);
		this.moduloDTO.setCodigoReferencia("MOD-"+secuenciaDespacho.getValorSecuencia());
		
		this.page = 0;
		if(modulosDataManager.getModuloDTOEditar() != null && modulosDataManager.getModuloDTOEditar().getId().getCodigoModulo() != null)
		{
			this.setModuloDTO(modulosDataManager.getModuloDTOEditar());
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/modulos/adminBusquedaModulos.xhtml")) {
			this.moduloDTOCols = ERPFactory.modulos.getModuloServicio().findObtenerListaModulos(nombreModulo);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return modulosDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para buscar modulos
	 * @param e
	 */
	public void busquedaModulos(ActionEvent e){
		try {
			this.moduloDTOCols = ERPFactory.modulos.getModuloServicio().findObtenerListaModulos(nombreModulo);
			if(CollectionUtils.isEmpty(this.moduloDTOCols)){
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
	 * Metodo para guardar o actualizar modulos
	 * @param e
	 */
	public void guadarActualizarModulo(ActionEvent e){
		try {
			this.setModuloCreado(Boolean.FALSE);
			if(this.validarPantallaModulos()) {
				this.moduloDTO.setNombreModulo(this.moduloDTO.getNombreModulo().toUpperCase());
				if(StringUtils.isNotEmpty(this.moduloDTO.getDescripcion())) {
					this.moduloDTO.setDescripcion(this.moduloDTO.getDescripcion().toUpperCase());
				}
				ERPFactory.modulos.getModuloServicio().transCrearActualizarModulo(this.moduloDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setModuloCreado(Boolean.TRUE);
				MensajesController.addInfo(null, "El m\u00F3dulo se cre\u00F3 correctamente.");
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
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaModulos() {
		Boolean validado = Boolean.TRUE;
		if(moduloDTO.getOrden() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.modulos.orden.modulo.requerido"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(moduloDTO.getNombreModulo())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.modulos.nombre.modulo.requerido"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(moduloDTO.getEstilo())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.modulos.estilo.modulo.requerido"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(moduloDTO.getUrl())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.modulos.url.modulo.requerido"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(moduloDTO.getEstado())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.modulos.estado.modulo.requerido"));
			validado = Boolean.FALSE;
		}
		return validado;
	}
	
	/**
	 * Metodo borrar pantalla y crear un articulo nuevo
	 * @param e
	 */
	public void clearNuevoModulo(ActionEvent e){
		this.setModuloCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.moduloDTO = new ModuloDTO();
		this.modulosDataManager.setModuloDTOEditar(new ModuloDTO());
		SecuenciaDTO secuenciaDespacho = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(ModuloID.NOMBRE_SECUENCIA);
		this.moduloDTO.setCodigoReferencia("MOD-"+secuenciaDespacho.getValorSecuencia());
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarModulo() {
		if(this.moduloDTO == null) {
			return null;
		}else{
			this.modulosDataManager.setModuloDTOEditar(this.moduloDTO);
			return "/modules/modulos/nuevoModulo.xhtml?faces-redirect=true";
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
	 * Metodo para regresar a la busqueda de modulos
	 * @param e
	 */
	public String regresarBusquedaModulos(){
		this.modulosDataManager.setModuloDTOEditar(new ModuloDTO());
		return "/modules/modulos/adminBusquedaModulos.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo modulo
	 * @return
	 */
	public String crearNuevoModulo(){
		return "/modules/modulos/nuevoModulo.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Borrar filtro de nombre de modulo
	 */
	public void borrarBusquedaNombreModulo(ActionEvent e){
		this.nombreModulo = "";
	}
	
	public void setModulosDataManager(ModulosDataManager modulosDataManager) {
		this.modulosDataManager = modulosDataManager;
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

	public ModuloDTO getModuloDTO() {
		return moduloDTO;
	}

	public void setModuloDTO(ModuloDTO moduloDTO) {
		this.moduloDTO = moduloDTO;
	}

	public Collection<ModuloDTO> getModuloDTOCols() {
		return moduloDTOCols;
	}

	public void setModuloDTOCols(Collection<ModuloDTO> moduloDTOCols) {
		this.moduloDTOCols = moduloDTOCols;
	}

	public String getNombreModulo() {
		return nombreModulo;
	}

	public void setNombreModulo(String nombreModulo) {
		this.nombreModulo = nombreModulo;
	}

	public Boolean getModuloCreado() {
		return moduloCreado;
	}

	public void setModuloCreado(Boolean moduloCreado) {
		this.moduloCreado = moduloCreado;
	}
}
