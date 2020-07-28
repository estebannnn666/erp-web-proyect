
package ec.com.erp.web.vendedor.controller;

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
import ec.com.erp.cliente.mdl.dto.PersonaDTO;
import ec.com.erp.cliente.mdl.dto.VendedorDTO;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.vendedor.datamanager.VendedorDataManager;

/**
 * Controlador para administracion de vendedores
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class VendedorController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private VendedorDTO vendedorDTO;
	private PersonaDTO personaDTO;
	private ContactoDTO contactoDTO;
	
	// Data Managers
	@ManagedProperty(value="#{vendedorDataManager}")
	private VendedorDataManager vendedorDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<VendedorDTO> vendedorDTOCols;
	private String numeroDocumentoBusqueda;
	private String nombreVendedorBusqueda;
	private Integer page;
	private Boolean vendedorCreado;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.vendedorCreado = Boolean.FALSE;
		this.vendedorDTO = new VendedorDTO();
		this.personaDTO = new PersonaDTO();
		this.contactoDTO = new ContactoDTO();
		this.page = 0;
		
		if(vendedorDataManager.getVendedorDTOEditar() != null && vendedorDataManager.getVendedorDTOEditar().getId().getCodigoVendedor() != null){
			this.setVendedorDTO(vendedorDataManager.getVendedorDTOEditar());
			this.setPersonaDTO(vendedorDataManager.getVendedorDTOEditar().getPersonaDTO());
			this.setContactoDTO(vendedorDataManager.getVendedorDTOEditar().getPersonaDTO().getContactoPersonaDTO());
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/vendedores/adminBusquedaVendedor.xhtml")) {
			this.vendedorDTOCols = ERPFactory.vendedor.getVendedorServicio().findObtenerListaVendedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreVendedorBusqueda);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return vendedorDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para consultar vendedor por numero de documento 
	 * @param e
	 */
	public void realizarConsultaDocumento(AjaxBehaviorEvent e) {
		VendedorDTO vendedorDTOTemp = ERPFactory.vendedor.getVendedorServicio().findObtenerVendedor(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.personaDTO.getNumeroDocumento());
		if(vendedorDTOTemp != null) {
			this.setVendedorDTO(vendedorDTOTemp);
			this.setPersonaDTO(vendedorDTOTemp.getPersonaDTO());
			this.setContactoDTO(vendedorDTOTemp.getPersonaDTO().getContactoPersonaDTO());
		}else{
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addWarn(null, "No se encontr\u00F3 el vendedor con el documento ingresado.");
		}
	}
	
	/**
	 * Metodo para buscar vendedores 
	 * @param e
	 */
	public void busquedaVendedores(ActionEvent e){
		this.buscarVendedor();
	}
	
	/**
	 * Metodo para buscar vendedores al dar enter
	 * @param e
	 */
	public void busquedaVendedorEnter(AjaxBehaviorEvent e){
		this.buscarVendedor();
	}
	
	/**
	 * Metodo para buscar vendedores 
	 * @param e
	 */
	public void buscarVendedor(){
		try {
			this.setShowMessagesBar(Boolean.FALSE);
			this.vendedorDTOCols = ERPFactory.vendedor.getVendedorServicio().findObtenerListaVendedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreVendedorBusqueda);
			if(CollectionUtils.isEmpty(this.vendedorDTOCols)){
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
	 * Metodo para guardar o actualizar vendedor
	 * @param e
	 */
	public void guadarActualizarVendedor(ActionEvent e){
		try {
			this.setVendedorCreado(Boolean.FALSE);
			if(this.validarPantallaVendedores()) {
				this.vendedorDTO.setPersonaDTO(this.personaDTO);
				this.vendedorDTO.setUsuarioRegistro(this.loginController.getUsuariosDTO().getId().getUserId());
				ERPFactory.vendedor.getVendedorServicio().transGuardarActualizarVendedor(this.vendedorDTO, this.contactoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setVendedorCreado(Boolean.TRUE);
				MensajesController.addInfo(null, "Los datos del vendedor se crearon correctamente.");
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.vendedorDTO.getPersonaDTO().getId().setCodigoPersona(null);
			this.vendedorDTO.setUsuarioRegistro(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.vendedorDTO.getPersonaDTO().getId().setCodigoPersona(null);
			this.vendedorDTO.setUsuarioRegistro(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}

	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaVendedores() {
		Boolean validado = Boolean.TRUE;
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
	public String  cargarVendedorEditar() {
		if(this.vendedorDTO == null) {
			return null;
		}else{
			this.vendedorDataManager.setVendedorDTOEditar(this.vendedorDTO);
			return "/modules/vendedores/nuevoVendedor.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo borrar pantalla y crear un vendedor nuevo
	 * @param e
	 */
	public void clearNuevoVendedor(ActionEvent e){
		this.setVendedorCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.vendedorDataManager.setVendedorDTOEditar(new VendedorDTO());
		this.vendedorDTO = new VendedorDTO();
		this.personaDTO = new PersonaDTO();
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
	 * Metodo para regresar a la busqueda de vendedores
	 * @param e
	 */
	public String regresarBusquedaVendedor(){
		this.setVendedorCreado(Boolean.FALSE);
		this.vendedorDataManager.setVendedorDTOEditar(new VendedorDTO());
		return "/modules/vendedores/adminBusquedaVendedor.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo vendedor
	 * @return
	 */
	public String crearNuevoVendedor(){
		return "/modules/vendedores/nuevoVendedor.xhtml?faces-redirect=true";
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
	 * Borrar filtro de nombre vendedor
	 */
	public void borrarBusquedaNombreVendedor(ActionEvent e){
		this.nombreVendedorBusqueda = "";
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
	
	public void setVendedorDataManager(VendedorDataManager vendedorDataManager) {
		this.vendedorDataManager = vendedorDataManager;
	}

	public VendedorDTO getVendedorDTO() {
		return vendedorDTO;
	}

	public void setVendedorDTO(VendedorDTO vendedorDTO) {
		this.vendedorDTO = vendedorDTO;
	}

	public Collection<VendedorDTO> getVendedorDTOCols() {
		return vendedorDTOCols;
	}

	public void setVendedorDTOCols(Collection<VendedorDTO> vendedorDTOCols) {
		this.vendedorDTOCols = vendedorDTOCols;
	}

	public String getNumeroDocumentoBusqueda() {
		return numeroDocumentoBusqueda;
	}

	public void setNumeroDocumentoBusqueda(String numeroDocumentoBusqueda) {
		this.numeroDocumentoBusqueda = numeroDocumentoBusqueda;
	}

	public String getNombreVendedorBusqueda() {
		return nombreVendedorBusqueda;
	}

	public void setNombreVendedorBusqueda(String nombreVendedorBusqueda) {
		this.nombreVendedorBusqueda = nombreVendedorBusqueda;
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

	public PersonaDTO getPersonaDTO() {
		return personaDTO;
	}

	public void setPersonaDTO(PersonaDTO personaDTO) {
		this.personaDTO = personaDTO;
	}

	public ContactoDTO getContactoDTO() {
		return contactoDTO;
	}

	public void setContactoDTO(ContactoDTO contactoDTO) {
		this.contactoDTO = contactoDTO;
	}


	public Boolean getVendedorCreado() {
		return vendedorCreado;
	}

	public void setVendedorCreado(Boolean vendedorCreado) {
		this.vendedorCreado = vendedorCreado;
	}
	
}
