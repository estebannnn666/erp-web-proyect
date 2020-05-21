
package ec.com.erp.web.transportista.controller;

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
import ec.com.erp.cliente.mdl.dto.TransportistaDTO;
import ec.com.erp.cliente.mdl.dto.UsuariosDTO;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.transportista.datamanager.TransportistaDataManager;

/**
 * Controlador para administracion de transportistas
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class TransportistaController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private TransportistaDTO transportistaDTO;
	private UsuariosDTO usuarioDTO;
	private PersonaDTO personaDTO;
	private EmpresaDTO empresaDTO;
	private ContactoDTO contactoDTO;
	private String repetirContrasenia;
	
	// Data Managers
	@ManagedProperty(value="#{transportistaDataManager}")
	private TransportistaDataManager transportistaDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<TransportistaDTO> transportistaDTOCols;
	private String numeroDocumentoBusqueda;
	private String nombreTransportistaBusqueda;
	private Integer page;
	private Boolean transportistaCreado;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.transportistaCreado = Boolean.FALSE;
		this.transportistaDTO = new TransportistaDTO();
		this.transportistaDTO.setCodigoValorTipoTransportista(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA);
		this.transportistaDTO.setCodigoTipoTransportista(ERPConstantes.CODIGO_CATALOGO_TIPOS_CLIENTES);
		this.usuarioDTO = new UsuariosDTO();
		this.personaDTO = new PersonaDTO();
		this.empresaDTO = new EmpresaDTO();
		this.contactoDTO = new ContactoDTO();
		this.page = 0;
		
		if(transportistaDataManager.getTransportistaDTOEditar() != null && transportistaDataManager.getTransportistaDTOEditar().getId().getCodigoTransportista() != null)
		{
			this.setTransportistaDTO(transportistaDataManager.getTransportistaDTOEditar());
			this.setPersonaDTO(transportistaDataManager.getTransportistaDTOEditar().getPersonaDTO());
			this.setEmpresaDTO(transportistaDataManager.getTransportistaDTOEditar().getEmpresaDTO());
			this.setContactoDTO(transportistaDataManager.getTransportistaDTOEditar().getPersonaDTO() == null ? transportistaDataManager.getTransportistaDTOEditar().getEmpresaDTO().getContactoEmpresaDTO() : transportistaDataManager.getTransportistaDTOEditar().getPersonaDTO().getContactoPersonaDTO());
			if(transportistaDataManager.getTransportistaDTOEditar().getPersonaDTO() == null) {
				this.personaDTO = new PersonaDTO();
				this.personaDTO.setNumeroDocumento(transportistaDataManager.getTransportistaDTOEditar().getEmpresaDTO().getNumeroRuc());
			}
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/transportistas/adminBusquedaTransportista.xhtml")) {
			this.transportistaDTOCols = ERPFactory.transportista.getTransportistasServicio().findObtenerListaTransportistas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreTransportistaBusqueda);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return transportistaDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para consultar transprtista por numero de documento 
	 * @param e
	 */
	public void realizarConsultaDocumento(AjaxBehaviorEvent e) {
		TransportistaDTO transportistaDTOTemp = ERPFactory.transportista.getTransportistasServicio().findObtenerTransportista(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.personaDTO.getNumeroDocumento(), this.transportistaDTO.getCodigoValorTipoTransportista());
		if(transportistaDTOTemp != null) {
			this.setTransportistaDTO(transportistaDTOTemp);
			this.setPersonaDTO(transportistaDTOTemp.getPersonaDTO());
			this.setEmpresaDTO(transportistaDTOTemp.getEmpresaDTO());
			if(transportistaDTOTemp.getPersonaDTO() == null){
				this.setContactoDTO(transportistaDTOTemp.getEmpresaDTO().getContactoEmpresaDTO());
				this.personaDTO = new PersonaDTO();
				this.personaDTO.setNumeroDocumento(transportistaDTOTemp.getEmpresaDTO().getNumeroRuc());
			}
			else {
				this.setContactoDTO(transportistaDTOTemp.getPersonaDTO().getContactoPersonaDTO());
			}
		}else
		{
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addWarn(null, "No se encontr\u00F3 el transportista con el documento ingresado.");
		}
	}
	
	/**
	 * Metodo para buscar proveedores 
	 * @param e
	 */
	public void busquedaTransportista(ActionEvent e){
		this.buscarTranportista();
	}
	
	/**
	 * Metodo para buscar proveedores al dar enter
	 * @param e
	 */
	public void busquedaTransportistaEnter(AjaxBehaviorEvent e){
		this.buscarTranportista();
	}
	
	/**
	 * Metodo para buscar proveedores 
	 * @param e
	 */
	public void buscarTranportista(){
		try {
			this.transportistaDTOCols = ERPFactory.transportista.getTransportistasServicio().findObtenerListaTransportistas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreTransportistaBusqueda);
			if(CollectionUtils.isEmpty(this.transportistaDTOCols)){
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
	 * Metodo para guardar o actualizar transportista
	 * @param e
	 */
	public void guadarActualizarTransportista(ActionEvent e){
		try {
			this.setTransportistaCreado(Boolean.FALSE);
			if(this.validarPantallaTransportistas()) {
				if(this.transportistaDTO.getCodigoValorTipoTransportista().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_EMPRESA)) {
					this.empresaDTO.setNumeroRuc(this.personaDTO.getNumeroDocumento());
					this.transportistaDTO.setEmpresaDTO(this.empresaDTO);
				}
				if(this.transportistaDTO.getCodigoValorTipoTransportista().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)) {
					this.transportistaDTO.setPersonaDTO(this.personaDTO);
				}
				
				this.transportistaDTO.setUsuarioRegistro(this.loginController.getUsuariosDTO().getId().getUserId());
				
				ERPFactory.transportista.getTransportistasServicio().transGuardarActualizarTransportista(this.transportistaDTO, this.contactoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setTransportistaCreado(Boolean.TRUE);
				MensajesController.addInfo(null, "Los datos del transportista se crearon correctamente.");
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			if(this.transportistaDTO.getCodigoValorTipoTransportista().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)) {
				this.transportistaDTO.getPersonaDTO().getId().setCodigoPersona(null);
			}
			if(this.transportistaDTO.getCodigoValorTipoTransportista().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_EMPRESA)) {
				this.transportistaDTO.getEmpresaDTO().getId().setCodigoEmpresa(null);
			}
			this.transportistaDTO.setUsuarioRegistro(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			if(this.transportistaDTO.getCodigoValorTipoTransportista().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)) {
				this.transportistaDTO.getPersonaDTO().getId().setCodigoPersona(null);
			}
			if(this.transportistaDTO.getCodigoValorTipoTransportista().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_EMPRESA)) {
				this.transportistaDTO.getEmpresaDTO().getId().setCodigoEmpresa(null);
			}
			this.transportistaDTO.setUsuarioRegistro(null);
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
		// Validacion por tipo de cliente
		if(transportistaDTO.getCodigoValorTipoTransportista().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)){
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
			if(StringUtils.isEmpty(this.personaDTO.getNumeroDocumento())){
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
	public String  cargarTransportistaEditar() {
		if(this.transportistaDTO == null) {
			return null;
		}else{
			this.transportistaDataManager.setTransportistaDTOEditar(this.transportistaDTO);
			return "/modules/transportistas/nuevoTransportista.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo borrar pantalla y crear un articulo nuevo
	 * @param e
	 */
	public void clearNuevoTransportista(ActionEvent e){
		this.setTransportistaCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.transportistaDataManager.setTransportistaDTOEditar(new TransportistaDTO());
		this.transportistaDTO = new TransportistaDTO();
		this.transportistaDTO.setCodigoValorTipoTransportista(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA);
		this.transportistaDTO.setCodigoTipoTransportista(ERPConstantes.CODIGO_CATALOGO_TIPOS_CLIENTES);
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
	 * Metodo para regresar a la busqueda de articulos
	 * @param e
	 */
	public String regresarBusquedaTransportistas(){
		this.setTransportistaCreado(Boolean.FALSE);
		this.transportistaDataManager.setTransportistaDTOEditar(new TransportistaDTO());
		return "/modules/transportistas/adminBusquedaTransportista.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo articulo
	 * @return
	 */
	public String crearNuevoTransportista(){
		return "/modules/transportistas/nuevoTransportista.xhtml?faces-redirect=true";
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
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de nombre Transportista
	 */
	public void borrarBusquedaNombreTransportista(ActionEvent e){
		this.nombreTransportistaBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
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
	
	public void setTransportistaDataManager(TransportistaDataManager transportistaDataManager) {
		this.transportistaDataManager = transportistaDataManager;
	}

	public TransportistaDTO getTransportistaDTO() {
		return transportistaDTO;
	}

	public void setTransportistaDTO(TransportistaDTO transportistaDTO) {
		this.transportistaDTO = transportistaDTO;
	}

	public Collection<TransportistaDTO> getTransportistaDTOCols() {
		return transportistaDTOCols;
	}

	public void setTransportistaDTOCols(Collection<TransportistaDTO> transportistaDTOCols) {
		this.transportistaDTOCols = transportistaDTOCols;
	}

	public String getNumeroDocumentoBusqueda() {
		return numeroDocumentoBusqueda;
	}

	public void setNumeroDocumentoBusqueda(String numeroDocumentoBusqueda) {
		this.numeroDocumentoBusqueda = numeroDocumentoBusqueda;
	}

	public String getNombreTransportistaBusqueda() {
		return nombreTransportistaBusqueda;
	}

	public void setNombreTransportistaBusqueda(String nombreTransportistaBusqueda) {
		this.nombreTransportistaBusqueda = nombreTransportistaBusqueda;
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

	public String getRepetirContrasenia() {
		return repetirContrasenia;
	}

	public void setRepetirContrasenia(String repetirContrasenia) {
		this.repetirContrasenia = repetirContrasenia;
	}

	public Boolean getTransportistaCreado() {
		return transportistaCreado;
	}

	public void setTransportistaCreado(Boolean transportistaCreado) {
		this.transportistaCreado = transportistaCreado;
	}
	
}
