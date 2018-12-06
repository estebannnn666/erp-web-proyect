
package ec.com.erp.web.proveedor.controller;

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
import ec.com.erp.cliente.mdl.dto.ContactoDTO;
import ec.com.erp.cliente.mdl.dto.EmpresaDTO;
import ec.com.erp.cliente.mdl.dto.PersonaDTO;
import ec.com.erp.cliente.mdl.dto.ProveedorDTO;
import ec.com.erp.cliente.mdl.dto.UsuariosDTO;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.proveedor.datamanager.ProveedorDataManager;

/**
 * Controlador para administracion de proveedores
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class ProveedorController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private ProveedorDTO proveedorDTO;
	private UsuariosDTO usuarioDTO;
	private PersonaDTO personaDTO;
	private EmpresaDTO empresaDTO;
	private ContactoDTO contactoDTO;
	
	// Data Managers
	@ManagedProperty(value="#{proveedorDataManager}")
	private ProveedorDataManager proveedorDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<ProveedorDTO> proveedorDTOCols;
	private String numeroDocumentoBusqueda;
	private String nombreProveedorBusqueda;
	private Integer page;
	private Boolean proveedorCreado;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.proveedorCreado = Boolean.FALSE;
		this.proveedorDTO = new ProveedorDTO();
		this.proveedorDTO.setCodigoValorTipoProveedor(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA);
		this.proveedorDTO.setCodigoTipoProveedor(ERPConstantes.CODIGO_CATALOGO_TIPOS_CLIENTES);
		this.usuarioDTO = new UsuariosDTO();
		this.personaDTO = new PersonaDTO();
		this.empresaDTO = new EmpresaDTO();
		this.contactoDTO = new ContactoDTO();
		this.page = 0;
		
		if(proveedorDataManager.getProveedorDTOEditar() != null && proveedorDataManager.getProveedorDTOEditar().getId().getCodigoProveedor() != null)
		{
			this.setProveedorDTO(proveedorDataManager.getProveedorDTOEditar());
			this.setPersonaDTO(proveedorDataManager.getProveedorDTOEditar().getPersonaDTO());
			this.setEmpresaDTO(proveedorDataManager.getProveedorDTOEditar().getEmpresaDTO());
			this.setContactoDTO(proveedorDataManager.getProveedorDTOEditar().getPersonaDTO() == null ? proveedorDataManager.getProveedorDTOEditar().getEmpresaDTO().getContactoEmpresaDTO() : proveedorDataManager.getProveedorDTOEditar().getPersonaDTO().getContactoPersonaDTO());
			if(proveedorDataManager.getProveedorDTOEditar().getPersonaDTO() == null) {
				this.personaDTO = new PersonaDTO();
				this.personaDTO.setNumeroDocumento(proveedorDataManager.getProveedorDTOEditar().getEmpresaDTO().getNumeroRuc());
			}
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/proveedores/adminBusquedaProveedor.xhtml")) {
			this.proveedorDTOCols = ERPFactory.proveedor.getProveedorServicio().findObtenerListaProveedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreProveedorBusqueda);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return proveedorDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para consultar proveedor por numero de documento 
	 * @param e
	 */
	public void realizarConsultaDocumento(AjaxBehaviorEvent e) {
		ProveedorDTO proveedorDTOTemp = ERPFactory.proveedor.getProveedorServicio().findObtenerProveedor(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.personaDTO.getNumeroDocumento(), this.proveedorDTO.getCodigoValorTipoProveedor());
		if(proveedorDTOTemp != null) {
			this.setProveedorDTO(proveedorDTOTemp);
			this.setPersonaDTO(proveedorDTOTemp.getPersonaDTO());
			this.setEmpresaDTO(proveedorDTOTemp.getEmpresaDTO());
			if(proveedorDTOTemp.getPersonaDTO() == null){
				this.setContactoDTO(proveedorDTOTemp.getEmpresaDTO().getContactoEmpresaDTO());
				this.personaDTO = new PersonaDTO();
				this.personaDTO.setNumeroDocumento(proveedorDTOTemp.getEmpresaDTO().getNumeroRuc());
			}
			else {
				this.setContactoDTO(proveedorDTOTemp.getPersonaDTO().getContactoPersonaDTO());
			}
		}else
		{
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addWarn(null, "No se encontr\u00F3 el proveedor con el documento ingresado.");
		}
	}
	
	/**
	 * Metodo para buscar proveedores 
	 * @param e
	 */
	public void busquedaProveedores(ActionEvent e){
		this.buscarProveedor();
	}
	
	/**
	 * Metodo para buscar proveedores al dar enter
	 * @param e
	 */
	public void busquedaProveedoresEnter(AjaxBehaviorEvent e){
		this.buscarProveedor();
	}
	
	/**
	 * Metodo para buscar proveedores 
	 * @param e
	 */
	public void buscarProveedor(){
		try {
			this.proveedorDTOCols = ERPFactory.proveedor.getProveedorServicio().findObtenerListaProveedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreProveedorBusqueda);
			if(CollectionUtils.isEmpty(this.proveedorDTOCols)){
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
	 * Metodo para guardar o actualizar proveedor
	 * @param e
	 */
	public void guadarActualizarProveedor(ActionEvent e){
		try {
			this.setProveedorCreado(Boolean.FALSE);
			if(this.validarPantallaTransportistas()) {
				if(this.proveedorDTO.getCodigoValorTipoProveedor().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_EMPRESA)) {
					this.empresaDTO.setNumeroRuc(this.personaDTO.getNumeroDocumento());
					this.proveedorDTO.setEmpresaDTO(this.empresaDTO);
				}
				if(this.proveedorDTO.getCodigoValorTipoProveedor().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)) {
					this.proveedorDTO.setPersonaDTO(this.personaDTO);
				}
				
				this.proveedorDTO.setUsuarioRegistro(this.loginController.getUsuariosDTO().getId().getUserId());
				
				ERPFactory.proveedor.getProveedorServicio().transGuardarActualizarProveedor(this.proveedorDTO, this.contactoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setProveedorCreado(Boolean.TRUE);
				MensajesController.addInfo(null, "Los datos del proveedor se crearon correctamente.");
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			if(this.proveedorDTO.getCodigoValorTipoProveedor().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)) {
				this.proveedorDTO.getPersonaDTO().getId().setCodigoPersona(null);
			}
			if(this.proveedorDTO.getCodigoValorTipoProveedor().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_EMPRESA)) {
				this.proveedorDTO.getEmpresaDTO().getId().setCodigoEmpresa(null);
			}
			this.proveedorDTO.setUsuarioRegistro(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			if(this.proveedorDTO.getCodigoValorTipoProveedor().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)) {
				this.proveedorDTO.getPersonaDTO().getId().setCodigoPersona(null);
			}
			if(this.proveedorDTO.getCodigoValorTipoProveedor().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_EMPRESA)) {
				this.proveedorDTO.getEmpresaDTO().getId().setCodigoEmpresa(null);
			}
			this.proveedorDTO.setUsuarioRegistro(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}

	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaTransportistas() {
		Boolean validado = Boolean.TRUE;
		// Validacion por tipo de proveedor
		if(proveedorDTO.getCodigoValorTipoProveedor().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)){
			if(StringUtils.isEmpty(this.personaDTO.getNumeroDocumento())){
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.numerodocumento"));
				validado = Boolean.FALSE;
			}
			if(StringUtils.isEmpty(this.personaDTO.getPrimerApellido())){
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.primerapellido"));
				validado = Boolean.FALSE;
			}
			
			if(StringUtils.isEmpty(this.personaDTO.getPrimerNombre())){
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.primernombre"));
				validado = Boolean.FALSE;
			}
		}else
		{
			if(StringUtils.isEmpty(this.empresaDTO.getNumeroRuc())){
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.numeroruc"));
				validado = Boolean.FALSE;
			}
			
			if(StringUtils.isEmpty(this.empresaDTO.getRazonSocial())){
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.razonsocial"));
				validado = Boolean.FALSE;
			}
		}
		
		if(StringUtils.isEmpty(contactoDTO.getCallePrincipal())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.calleprincipal"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(contactoDTO.getNumeroCasa())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.numerocasa"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(contactoDTO.getTelefonoPrincipal())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.telefonoconvencional"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(contactoDTO.getCiudad())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.ciudad"));
			validado = Boolean.FALSE;
		}
		
		return validado;
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarProveedorEditar() {
		if(this.proveedorDTO == null) {
			return null;
		}else{
			this.proveedorDataManager.setProveedorDTOEditar(this.proveedorDTO);
			return "/modules/proveedores/nuevoProveedor.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo borrar pantalla y crear un proveedor nuevo
	 * @param e
	 */
	public void clearNuevoProveedor(ActionEvent e){
		this.setProveedorCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.proveedorDataManager.setProveedorDTOEditar(new ProveedorDTO());
		this.proveedorDTO = new ProveedorDTO();
		this.proveedorDTO.setCodigoValorTipoProveedor(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA);
		this.proveedorDTO.setCodigoTipoProveedor(ERPConstantes.CODIGO_CATALOGO_TIPOS_CLIENTES);
		this.personaDTO = new PersonaDTO();
		this.empresaDTO = new EmpresaDTO();
		this.contactoDTO = new ContactoDTO();
	}
	
	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public void refrescarPantalla(ActionEvent e){
		System.out.println("Ingreso a refrescar pantalla");
	}
	
	/**
	 * Metodo para regresar a la busqueda de proveedores
	 * @param e
	 */
	public String regresarBusquedaProveedor(){
		this.setProveedorCreado(Boolean.FALSE);
		this.proveedorDataManager.setProveedorDTOEditar(new ProveedorDTO());
		return "/modules/proveedores/adminBusquedaProveedor.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo proveedor
	 * @return
	 */
	public String crearNuevoProveedor(){
		return "/modules/proveedores/nuevoProveedor.xhtml?faces-redirect=true";
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
	public void borrarBusquedaNumeroDocumento(ActionEvent e){
		this.numeroDocumentoBusqueda = "";
	}
	
	/**
	 * Borrar filtro de nombre Transportista
	 */
	public void borrarBusquedaNombreProveedor(ActionEvent e){
		this.nombreProveedorBusqueda = "";
	}
	
	/**
	 * Metodo para concatenar los nombres del cliente persona
	 * @param e
	 */
	public void concatenarNombrePersona(AjaxBehaviorEvent e){
		String nombreConcatenado = "";
		if(this.personaDTO.getPrimerApellido() != null)
		{
			nombreConcatenado = this.personaDTO.getPrimerApellido();
		}
		if(this.personaDTO.getSegundoApellido() != null){
			nombreConcatenado +=" "+this.personaDTO.getSegundoApellido();
		}
		if(this.personaDTO.getPrimerNombre() != null){
			nombreConcatenado += " "+this.personaDTO.getPrimerNombre();
		}
		if(this.personaDTO.getSegundoNombre() != null){
			nombreConcatenado +=" "+this.personaDTO.getSegundoNombre();
		}
		this.personaDTO.setNombreCompleto(nombreConcatenado);
	}
	
	/**
	 * Metodo para concatenar la direccion
	 * @param e
	 */
	public void concatenarDireccion(AjaxBehaviorEvent e){
		String direccionConcatenada = "";
		if(this.contactoDTO.getCallePrincipal() != null)
		{
			direccionConcatenada = this.contactoDTO.getCallePrincipal();
		}
		if(this.contactoDTO.getNumeroCasa() != null){
			direccionConcatenada +=" "+this.contactoDTO.getNumeroCasa();
		}
		if(this.contactoDTO.getCalleSecundaria() != null){
			direccionConcatenada += " "+this.contactoDTO.getCalleSecundaria();
		}
		this.contactoDTO.setDireccionPrincipal(direccionConcatenada);
	}
	
	public void setProveedorDataManager(ProveedorDataManager proveedorDataManager) {
		this.proveedorDataManager = proveedorDataManager;
	}

	public ProveedorDTO getProveedorDTO() {
		return proveedorDTO;
	}

	public void setProveedorDTO(ProveedorDTO proveedorDTO) {
		this.proveedorDTO = proveedorDTO;
	}

	public Collection<ProveedorDTO> getProveedorDTOCols() {
		return proveedorDTOCols;
	}

	public void setProveedorDTOCols(Collection<ProveedorDTO> proveedorDTOCols) {
		this.proveedorDTOCols = proveedorDTOCols;
	}

	public String getNumeroDocumentoBusqueda() {
		return numeroDocumentoBusqueda;
	}

	public void setNumeroDocumentoBusqueda(String numeroDocumentoBusqueda) {
		this.numeroDocumentoBusqueda = numeroDocumentoBusqueda;
	}

	public String getNombreProveedorBusqueda() {
		return nombreProveedorBusqueda;
	}

	public void setNombreProveedorBusqueda(String nombreProveedorBusqueda) {
		this.nombreProveedorBusqueda = nombreProveedorBusqueda;
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

	public PersonaDTO getPersonaDTO() {
		return personaDTO;
	}

	public void setPersonaDTO(PersonaDTO personaDTO) {
		this.personaDTO = personaDTO;
	}

	public EmpresaDTO getEmpresaDTO() {
		return empresaDTO;
	}

	public void setEmpresaDTO(EmpresaDTO empresaDTO) {
		this.empresaDTO = empresaDTO;
	}

	public ContactoDTO getContactoDTO() {
		return contactoDTO;
	}

	public void setContactoDTO(ContactoDTO contactoDTO) {
		this.contactoDTO = contactoDTO;
	}


	public Boolean getProveedorCreado() {
		return proveedorCreado;
	}

	public void setProveedorCreado(Boolean proveedorCreado) {
		this.proveedorCreado = proveedorCreado;
	}
	
}
