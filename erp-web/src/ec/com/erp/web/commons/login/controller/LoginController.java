package ec.com.erp.web.commons.login.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.impl.Log4JLogger;

import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ModuloDTO;
import ec.com.erp.cliente.mdl.dto.ModuloPerfilDTO;
import ec.com.erp.cliente.mdl.dto.UsuariosDTO;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.datamanager.SessionDataManagerBase;
import ec.com.erp.web.commons.utils.ERPWebResources;


/**
 * Simple login bean.
 * 
 * @author egudino
 */
@ManagedBean
@SessionScoped
public class LoginController extends CommonsController implements Serializable{

	private static final long serialVersionUID = 7765876811740798583L;

	public final String ID_SESDATMAN= "sessionDataManagerBase";
	
	private SessionDataManagerBase sessionDataManagerBase;
	
	private boolean banderaToken = Boolean.FALSE; 

	private HtmlForm form;
	
	// Simple user database :)
	private Collection<ModuloDTO> modulosGestionCols;
	private Collection<ModuloDTO> modulosAdministracionCols;
	private UsuariosDTO usuariosDTO;
	private Log4JLogger log;
	private Long codigoModulo;
	private Boolean activarInicio;
	
	private boolean loggedIn;
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}
	
	public void inicializar() {
		activarInicio = Boolean.TRUE;
		// TODO Auto-generated method stub
		log =  new Log4JLogger();
		banderaToken=Boolean.FALSE;
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String tpn = ERPWebResources.getString("ec.com.erp.etiqueta.tokenparameter");
			// Verificaci&oacute;n de sesi&oacute;n por token
			if (request.getParameter(tpn) != null) {
				if( verifySessionByParameter(request, tpn)){
					//se realiza la construcci&oacute;n del men&uacute; principal y el panel de opciones
					log.info("Login existoso");
				}
				banderaToken = Boolean.TRUE	;
			}
		} catch (Exception e) {
			banderaToken = Boolean.FALSE;
			throw new ERPException("Error al intentar hacer login",e);
		}
	}

	/**
	 * Verifica si el usuario tiene un token activo para obtener los datos corporativos y cargarlos en la sesi&oacute;n
	 * @param request
	 * @param tokenParameterName
	 * @param requiredAccessItemSetIds
	 * @return
	 */
	public boolean verifySessionByParameter(ServletRequest request, String  tokenParameterName) throws ERPException{
		try{
			String token = request.getParameter(tokenParameterName);
			if(token != null && !token.equals("")){
				UsuariosDTO usuariosLogerDTO = ERPFactory.usuarios.getUsuariosServicio().findLoginUser(usuariosDTO.getNombreUsuario(), usuariosDTO.getPasswordUsuario());
				if(usuariosLogerDTO != null){
					this.sessionDataManagerBase.setUserDto(usuariosLogerDTO);
					loggedIn = true;
					return true;
				}
				return false;
			}
		}catch(Exception ex){
			throw new ERPException("Error al intentar hacer login",ex);
		}
		return false;
	}
	
	public HtmlForm getForm() {
		inicializar();
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		SessionDataManagerBase dmSession =((SessionDataManagerBase)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ID_SESDATMAN));
		try {
			if (dmSession != null && dmSession.getUserDto() != null  && banderaToken) {
				ec.redirect(ec.getRequestContextPath() + "/modules/principal/menu.jsf");
			} else {
				ec.redirect(ec.getRequestContextPath()+ "/sessionTimeOut.jsp");
			}
		} catch (IOException e) {

		}
		return form;
	}

	public void setForm(HtmlForm form) {
		this.form = form;
	}

	
	@Override
	public CommonDataManager getDataManager() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@PostConstruct
	public void postConstruct(){
		usuariosDTO = new UsuariosDTO();
		if(this.sessionDataManagerBase == null) {
			this.sessionDataManagerBase = new SessionDataManagerBase();
		}
	}

	/**
	 * Login operation.
	 * @return
	 */
	public void doLogin() {
		try {
			activarInicio = Boolean.TRUE;
			usuariosDTO = ERPFactory.usuarios.getUsuariosServicio().findLoginUser(usuariosDTO.getNombreUsuario(), usuariosDTO.getPasswordUsuario());
			if(usuariosDTO.getLogeado()){
				loggedIn = true;
				this.modulosGestionCols = new ArrayList<ModuloDTO>();
				this.modulosAdministracionCols = new ArrayList<ModuloDTO>();
				
				for(ModuloPerfilDTO  moduloPerfilDTO : usuariosDTO.getPerfilDTO().getModuloPerfilDTOCols()) {
					moduloPerfilDTO.getModuloDTO().setMenuActivo(Boolean.FALSE);
					this.modulosGestionCols.add(moduloPerfilDTO.getModuloDTO());
//					if(moduloPerfilDTO.getModuloDTO().getValorTipo().equals("GES")) {
//						this.modulosGestionCols.add(moduloPerfilDTO.getModuloDTO());
//					}
//					if(moduloPerfilDTO.getModuloDTO().getValorTipo().equals("ADM")) {
//						this.modulosAdministracionCols.add(moduloPerfilDTO.getModuloDTO());
//					}
				}
				
				this.sessionDataManagerBase.setUserDto(usuariosDTO);
				
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ID_SESDATMAN, this.sessionDataManagerBase);
				
				String urlConexion = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath().concat("/modules/principal/menu.xhtml?faces-redirect=true");//ontextName();
				FacesContext.getCurrentInstance().getExternalContext().redirect(urlConexion+"?nerptms="+ usuariosDTO.getId().getUserId());
	//			return "/modules/principal/menu.xhtml?faces-redirect=true";
			}
			else{
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("Usuario o contrase\u00F1a incorrecta, intente nuevamente.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Menu para desactivar panel seleccionado
	 * @param e
	 */
	public void desActivarMenusSeleccionado(){
		activarInicio = Boolean.FALSE;
		for(ModuloDTO  moduloTO : this.modulosGestionCols) {
			moduloTO.setMenuActivo(Boolean.FALSE);
		}
	}
	
	/**
	 * Menu para activar panel seleccionado
	 * @param e
	 */
	public void activarMenusSeleccionado(){
		for(ModuloDTO  moduloTO : this.modulosGestionCols) {
			if(codigoModulo.longValue() == moduloTO.getId().getCodigoModulo().longValue()){
				moduloTO.setMenuActivo(Boolean.TRUE);
				activarInicio = Boolean.FALSE;
				break;
			}
		}
	}
	
	public String redirectToInfo(){
		return "/info.xhtml";
	}
	
	public String redirectToWelcome(){
		activarInicio = Boolean.TRUE;
		this.modulosGestionCols = new ArrayList<ModuloDTO>();
		for(ModuloPerfilDTO  moduloPerfilDTO : usuariosDTO.getPerfilDTO().getModuloPerfilDTOCols()) {
			moduloPerfilDTO.getModuloDTO().setMenuActivo(Boolean.FALSE);
			this.modulosGestionCols.add(moduloPerfilDTO.getModuloDTO());
		}
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	public String redirectToArticulos(){
		return "/modules/articulos/adminBusquedaArticulos.xhtml?faces-redirect=true";
	}
	
	public String redirectToClientes(){
		return "/modules/clientes/adminBusquedaClientes.xhtml?faces-redirect=true";
	}
	
	public String redirectToPedidos(){
		return "/modules/pedidos/adminBusquedaPedidos.xhtml?faces-redirect=true";
	}
	
	public String redirectToTransportista(){
		return "/modules/transportistas/adminBusquedaTransportista.xhtml?faces-redirect=true";
	}
	
	public String redirectToChofer(){
		return "/modules/chofer/adminBusquedaChofer.xhtml?faces-redirect=true";
	}
	
	public String redirectToVehiculos(){
		return "/modules/vehiculo/adminBusquedaVehiculo.xhtml?faces-redirect=true";
	}
	
	public String redirectToDespacho(){
		return "/modules/despachos/adminBusquedaDespacho.xhtml?faces-redirect=true";
	}
	
	public String redirectToFacutas(){
		return "/modules/facturas/adminBusquedaCuentas.xhtml?faces-redirect=true";
	}
	
	public String redirectToUsuarios() {
		return "/modules/usuarios/adminBusquedaUsuarios.xhtml?faces-redirect=true";
	}
	
	public String redirectToModulos() {
		return "/modules/modulos/adminBusquedaModulos.xhtml?faces-redirect=true";
	}
	
	public String redirectToPerfiles() {
		return "/modules/perfiles/adminBusquedaPerfil.xhtml?faces-redirect=true";
	}
	
	public String redirectToKardex() {
		return "/modules/inventario/adminBusquedaInventario.xhtml?faces-redirect=true";
	}
	/**
	 * Logout operation.
	 * @return
	 */
	public String doLogout() {
		// Set the paremeter indicating that user is logged in to false
		loggedIn = false;
		this.setShowMessagesBar(Boolean.FALSE);
		// Set logout message
		FacesMessage msg = new FacesMessage("Logout success!", "INFO MSG");
		msg.setSeverity(FacesMessage.SEVERITY_INFO);
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		return "/login.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public void refrescarPantalla(ActionEvent e){
		System.out.println("Ingreso a refrescar pantalla");
	}
	
	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public String refrescar(){
		System.out.println("Ingreso a refrescar pantalla");
		return "";
	}

	// ------------------------------
	// Getters & Setters 

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public UsuariosDTO getUsuariosDTO() {
		return usuariosDTO;
	}

	public void setUsuariosDTO(UsuariosDTO usuariosDTO) {
		this.usuariosDTO = usuariosDTO;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public boolean isBanderaToken() {
		return banderaToken;
	}

	public void setBanderaToken(boolean banderaToken) {
		this.banderaToken = banderaToken;
	}

	public Log4JLogger getLog() {
		return log;
	}

	public void setLog(Log4JLogger log) {
		this.log = log;
	}

	public Collection<ModuloDTO> getModulosGestionCols() {
		return modulosGestionCols;
	}

	public void setModulosGestionCols(Collection<ModuloDTO> modulosGestionCols) {
		this.modulosGestionCols = modulosGestionCols;
	}

	public Collection<ModuloDTO> getModulosAdministracionCols() {
		return modulosAdministracionCols;
	}

	public void setModulosAdministracionCols(Collection<ModuloDTO> modulosAdministracionCols) {
		this.modulosAdministracionCols = modulosAdministracionCols;
	}

	public Long getCodigoModulo() {
		return codigoModulo;
	}

	public void setCodigoModulo(Long codigoModulo) {
		this.codigoModulo = codigoModulo;
	}

	public Boolean getActivarInicio() {
		return activarInicio;
	}

	public void setActivarInicio(Boolean activarInicio) {
		this.activarInicio = activarInicio;
	}
}
