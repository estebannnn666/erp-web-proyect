
package ec.com.erp.web.usuarios.controller;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.PerfilDTO;
import ec.com.erp.cliente.mdl.dto.UsuariosDTO;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.usuarios.datamanager.UsuariosDataManager;

/**
 * Controlador para administracion de usuarios
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class UsuariosController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{usuariosDataManager}")
	private UsuariosDataManager usuariosDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private UsuariosDTO usuarioDTO;
	private Collection<UsuariosDTO> usuariosDTOCols;
	private Collection<PerfilDTO> perfilDTOCols;
	private String nombreUsuario;
	private String repetirContrasenia;
	private Integer page;
	private Boolean usuarioCreado;

	@PostConstruct
	public void postConstruct() {
		this.usuarioCreado = Boolean.FALSE;
		this.usuarioDTO = new UsuariosDTO();
		this.page = 0;
		if(CollectionUtils.isEmpty(this.perfilDTOCols)){
			this.perfilDTOCols = ERPFactory.perfiles.getPerfilesServicio().findObtenerListaPerfiles(null);
		}
		if(usuariosDataManager.getUsuariosDTOEditar() != null && usuariosDataManager.getUsuariosDTOEditar().getId().getUserId() != null)
		{
			this.setUsuarioDTO(usuariosDataManager.getUsuariosDTOEditar());
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return usuariosDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para buscar articulos evento click
	 * @param e
	 */
	public void busquedaUsuarios(ActionEvent e){
		this.ejecutarConsultaBusquedaUsuarios();
	}
	
	/**
	 * Metodo para buscar articulos evento enter
	 * @param e
	 */
	public void busquedaUsuariosEnter(AjaxBehaviorEvent e) {
		this.ejecutarConsultaBusquedaUsuarios();
	}
	
	/**
	 * Metodo para buscar articulos
	 */
	public void ejecutarConsultaBusquedaUsuarios() {
		try {
			this.usuariosDTOCols = ERPFactory.usuarios.getUsuariosServicio().findObtenerListaUsuarios(nombreUsuario);
			if(CollectionUtils.isEmpty(this.usuariosDTOCols)){
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
	 * Metodo para guardar o actualizar articulos
	 * @param e
	 */
	public void guadarActualizarUsuario(ActionEvent e){
		try {
			this.setUsuarioCreado(Boolean.FALSE);
			if(this.validarPantallaUsuarios()) { 
				ERPFactory.usuarios.getUsuariosServicio().transGuardarActualizarUsuarios(usuarioDTO);
				this.setUsuarioCreado(Boolean.TRUE);
		        this.setShowMessagesBar(Boolean.TRUE);
				MensajesController.addInfo(null, "El usuario y datos del cliente se crearon correctamente.");
				
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
	public Boolean validarPantallaUsuarios() {
		Boolean validado = Boolean.TRUE;
		if(StringUtils.isEmpty(usuarioDTO.getNombreUsuario())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.nombreusuario"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(usuarioDTO.getPasswordUsuario())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.contrasenia"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(repetirContrasenia)){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.repetircontrasenia"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isNotEmpty(repetirContrasenia) && StringUtils.isNotEmpty(usuarioDTO.getPasswordUsuario()) && !repetirContrasenia.equals(usuarioDTO.getPasswordUsuario())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.contrasenias.diferentes"));
			validado = Boolean.FALSE;
		}
		if(usuarioDTO.getCodigoPerfil() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.perfil.usuario"));
			validado = Boolean.FALSE;
		}
		return validado;
	}
	
	/**
	 * Metodo borrar pantalla y crear un usuario nuevo
	 * @param e
	 */
	public void clearNuevoUsuario(ActionEvent e){
		this.setUsuarioCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.usuarioDTO = new UsuariosDTO();
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarUsuario() {
		if(this.usuarioDTO == null) {
			return null;
		}else{
			this.usuariosDataManager.setUsuariosDTOEditar(this.usuarioDTO);
			return "/modules/usuarios/nuevoUsuario.xhtml?faces-redirect=true";
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
	 * Metodo para regresar a la busqueda de usuarios
	 * @param e
	 */
	public String regresarBusquedaUsuarios(){
		return "/modules/usuarios/adminBusquedaUsuarios.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo usuario
	 * @return
	 */
	public String crearNuevoUsuario(){
		return "/modules/usuarios/nuevoUsuario.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Borrar filtro nombre de usuario
	 */
	public void borrarBusquedaUsuario(ActionEvent e){
		this.nombreUsuario = "";
	}
	
	public void setUsuariosDataManager(UsuariosDataManager usuariosDataManager) {
		this.usuariosDataManager = usuariosDataManager;
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

	public UsuariosDTO getUsuarioDTO() {
		return usuarioDTO;
	}

	public void setUsuarioDTO(UsuariosDTO usuarioDTO) {
		this.usuarioDTO = usuarioDTO;
	}

	public Collection<UsuariosDTO> getUsuariosDTOCols() {
		return usuariosDTOCols;
	}

	public void setUsuariosDTOCols(Collection<UsuariosDTO> usuariosDTOCols) {
		this.usuariosDTOCols = usuariosDTOCols;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getRepetirContrasenia() {
		return repetirContrasenia;
	}

	public void setRepetirContrasenia(String repetirContrasenia) {
		this.repetirContrasenia = repetirContrasenia;
	}

	public Boolean getUsuarioCreado() {
		return usuarioCreado;
	}

	public void setUsuarioCreado(Boolean usuarioCreado) {
		this.usuarioCreado = usuarioCreado;
	}

	public Collection<PerfilDTO> getPerfilDTOCols() {
		return perfilDTOCols;
	}

	public void setPerfilDTOCols(Collection<PerfilDTO> perfilDTOCols) {
		this.perfilDTOCols = perfilDTOCols;
	}
}
