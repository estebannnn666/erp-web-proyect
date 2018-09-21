
package ec.com.erp.web.chofer.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
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
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.ChoferDTO;
import ec.com.erp.cliente.mdl.dto.ContactoDTO;
import ec.com.erp.cliente.mdl.dto.PersonaDTO;
import ec.com.erp.cliente.mdl.dto.TransportistaDTO;
import ec.com.erp.cliente.mdl.dto.UsuariosDTO;
import ec.com.erp.web.chofer.datamanager.ChoferDataManager;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;

/**
 * Controlador para administracion de choferes
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class ChoferController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private ChoferDTO choferDTO;
	private UsuariosDTO usuarioDTO;
	private PersonaDTO personaDTO;
	private ContactoDTO contactoDTO;
	private TransportistaDTO transportistaDTO;
	
	// Data Managers
	@ManagedProperty(value="#{choferDataManager}")
	private ChoferDataManager choferDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<ChoferDTO> choferDTOCols;
	private Collection<TransportistaDTO> transportistaDTOCols;
	private Collection<CatalogoValorDTO> tiposLicenciasCols;
	private String documentoTransportista;
	private String numeroDocumentoBusqueda;
	private String nombreChoferBusqueda;
	private Integer page;
	private Long codigoTransportistaSeleccionado;
	private Boolean choferCreado;

	@PostConstruct
	public void postConstruct() {
		this.choferCreado = Boolean.FALSE;
		this.choferDTO = new ChoferDTO();
		this.choferDTO.setFechaCaducidadLicencia(new Date());
		this.usuarioDTO = new UsuariosDTO();
		this.personaDTO = new PersonaDTO();
		this.contactoDTO = new ContactoDTO();
		this.transportistaDTO = new TransportistaDTO();
		this.transportistaDTOCols = new ArrayList<TransportistaDTO>();
		this.page = 0;
		this.tiposLicenciasCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_LICENCIAS);
		
		if(choferDataManager.getChoferDTOEditar() != null && choferDataManager.getChoferDTOEditar().getId().getCodigoChofer() != null)
		{
			this.setChoferDTO(choferDataManager.getChoferDTOEditar());
			this.setPersonaDTO(choferDataManager.getChoferDTOEditar().getPersonaDTO());
			this.setContactoDTO(choferDataManager.getChoferDTOEditar().getPersonaDTO().getContactoPersonaDTO());
			this.setTransportistaDTO(choferDataManager.getChoferDTOEditar().getTransportistaDTO());
			this.documentoTransportista = this.transportistaDTO.getPersonaDTO() == null ? this.transportistaDTO.getEmpresaDTO().getNumeroRuc() : this.transportistaDTO.getPersonaDTO().getNumeroDocumento();
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/chofer/adminBusquedaChofer.xhtml")) {
			this.choferDTOCols = ERPFactory.chofer.getChoferServicio().findObtenerListaChoferes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreChoferBusqueda);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return choferDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para consultar chofer por numero de documento 
	 * @param e
	 */
	public void realizarConsultaDocumento(AjaxBehaviorEvent e) {
		String documento = this.personaDTO.getNumeroDocumento();
		String codigoValorTipoLicencia = this.choferDTO.getCodigoValorTipoLicencia();
		ChoferDTO choferDTOTemp = ERPFactory.chofer.getChoferServicio().findObtenerChoferByDocumento(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.personaDTO.getNumeroDocumento());
		if(choferDTOTemp != null) {
			if(choferDTOTemp.getFechaCaducidadLicencia() == null) {
				choferDTOTemp.setFechaCaducidadLicencia(new Date());
			}
			if(choferDTOTemp.getCodigoValorTipoLicencia() == null) {
				choferDTOTemp.setCodigoValorTipoLicencia(codigoValorTipoLicencia);
			}
			this.setChoferDTO(choferDTOTemp);
			if(choferDTOTemp.getPersonaDTO() == null){
				this.personaDTO = new PersonaDTO();
				this.personaDTO.setNumeroDocumento(documento);
			}
			else {
				this.setPersonaDTO(choferDTOTemp.getPersonaDTO());
				this.setContactoDTO(choferDTOTemp.getPersonaDTO().getContactoPersonaDTO());
			}
			if(choferDTOTemp.getTransportistaDTO() != null) {
				this.setTransportistaDTO(choferDTOTemp.getTransportistaDTO());
				this.documentoTransportista = choferDTOTemp.getTransportistaDTO().getPersonaDTO() == null ? choferDTOTemp.getTransportistaDTO().getEmpresaDTO().getNumeroRuc() : choferDTOTemp.getTransportistaDTO().getPersonaDTO().getNumeroDocumento();
			}
		}
		else
		{
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addWarn(null, "No se encontr\u00F3 el chofer con el documento ingresado.");
		}
	}
	
	/**
	 * Metodo para consultar transprtista por numero de documento 
	 * @param e
	 */
	public void realizarConsultaDocumentoTransportista(AjaxBehaviorEvent e) {
		Collection<TransportistaDTO> transportistaDTOTempCols = ERPFactory.transportista.getTransportistasServicio().findObtenerListaTransportistas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.documentoTransportista, null);
		if(CollectionUtils.isNotEmpty(transportistaDTOTempCols)) {
			this.setTransportistaDTO(transportistaDTOTempCols.iterator().next());
		}
		else
		{
			this.documentoTransportista = "";
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addWarn(null, "No se encontr\u00F3 el transportista con el documento ingresado.");
		}
	}
	
	/**
	 * Metodo para buscar transportistas
	 * @param e
	 */
	public void busquedaTransportistas(ActionEvent e){
		try {
			this.transportistaDTOCols = ERPFactory.transportista.getTransportistasServicio().findObtenerListaTransportistas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
			if(CollectionUtils.isEmpty(this.transportistaDTOCols)){
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
	 * Metodo para buscar choferes por filtros o todos
	 * @param e
	 */
	public void busquedaChofer(ActionEvent e){
		try {
			this.choferDTOCols = ERPFactory.chofer.getChoferServicio().findObtenerListaChoferes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreChoferBusqueda);
			if(CollectionUtils.isEmpty(this.choferDTOCols)){
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
	 * Metodo para guardar o actualizar chofer
	 * @param e
	 */
	public void guadarActualizarChofer(ActionEvent e){
		try {
			this.setChoferCreado(Boolean.FALSE);
			if(this.validarPantallaChofer()) {
				this.choferDTO.setUsuarioRegistro(this.loginController.getUsuariosDTO().getId().getUserId());
				this.choferDTO.setPersonaDTO(this.personaDTO);
				this.choferDTO.setCodigoTransportista(this.transportistaDTO.getId().getCodigoTransportista());
				ERPFactory.chofer.getChoferServicio().transGuardarActualizarChofer(this.choferDTO, this.contactoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setChoferCreado(Boolean.TRUE);
				MensajesController.addInfo(null, "Los datos del chofer se crearon correctamente.");
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.choferDTO.getPersonaDTO().getId().setCodigoPersona(null);
			this.choferDTO.setUsuarioRegistro(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.choferDTO.getPersonaDTO().getId().setCodigoPersona(null);
			this.choferDTO.setUsuarioRegistro(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaChofer() {
		Boolean validado = Boolean.TRUE;
		// Validacion por tipo de chofer
		if(StringUtils.isEmpty(this.personaDTO.getNumeroDocumento())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.numerodocumento"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(this.choferDTO.getCodigoValorTipoLicencia())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.licencia.tipo"));
			validado = Boolean.FALSE;
		}
		if(this.choferDTO.getFechaCaducidadLicencia() == null){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.licencia.caducidad"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(this.documentoTransportista)){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.documento.transportista"));
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
		// Validar datos de contactos
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
	public String  cargarChoferEditar() {
		if(this.choferDTO == null) {
			return null;
		}else{
			this.choferDataManager.setChoferDTOEditar(this.choferDTO);
			return "/modules/chofer/nuevoChofer.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo borrar pantalla y crear un chofer nuevo
	 * @param e
	 */
	public void clearNuevoChofer(ActionEvent e){
		this.setChoferCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.choferDTO = new ChoferDTO();
		this.personaDTO = new PersonaDTO();
		this.contactoDTO = new ContactoDTO();
		this.documentoTransportista = "";
		this.transportistaDTO = new TransportistaDTO();
		this.choferDataManager.setChoferDTOEditar(new ChoferDTO());
	}
	
	/**
	 * Seleccionar transportista del popUp
	 * @param e
	 */
	public void seleccionTransportista(ValueChangeEvent e)
	{
		this.codigoTransportistaSeleccionado = (Long)e.getNewValue();
	}
	
	/**
	 * Metodo para agragar el cliente a la vista
	 */
	public void agragarTransportista(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoTransportista", PredicateUtils.equalPredicate(this.codigoTransportistaSeleccionado));
		// Validacion de objeto existente
		this.transportistaDTO  = (TransportistaDTO) CollectionUtils.find(this.transportistaDTOCols, testPredicate);
		this.documentoTransportista = this.transportistaDTO.getPersonaDTO() == null ? this.transportistaDTO.getEmpresaDTO().getNumeroRuc() : this.transportistaDTO.getPersonaDTO().getNumeroDocumento();
	}
	
	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public void refrescarPantalla(ActionEvent e){
		System.out.println("Ingreso a refrescar pantalla");
	}
	
	/**
	 * Metodo para regresar a la busqueda de choferes
	 * @param e
	 */
	public String regresarBusquedaChoferes(){
		this.choferDataManager.setChoferDTOEditar(new ChoferDTO());
		return "/modules/chofer/adminBusquedaChofer.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo chofer
	 * @return
	 */
	public String crearNuevoChofer(){
		this.choferDataManager.setChoferDTOEditar(new ChoferDTO());
		return "/modules/chofer/nuevoChofer.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Borrar filtro de numero de documento
	 */
	public void borrarBusquedaNumeroDocumento(ActionEvent e){
		this.numeroDocumentoBusqueda = "";
	}
	
	/**
	 * Borrar filtro de nombre de chofer
	 */
	public void borrarBusquedaNombreChofer(ActionEvent e){
		this.nombreChoferBusqueda = "";
	}
	
	/**
	 * Metodo para concatenar los nombres del chofer persona
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
	
	public void setChoferDataManager(ChoferDataManager choferDataManager) {
		this.choferDataManager = choferDataManager;
	}

	public ChoferDTO getChoferDTO() {
		return choferDTO;
	}

	public void setChoferDTO(ChoferDTO choferDTO) {
		this.choferDTO = choferDTO;
	}

	public Collection<ChoferDTO> getChoferDTOCols() {
		return choferDTOCols;
	}

	public void setChoferDTOCols(Collection<ChoferDTO> choferDTOCols) {
		this.choferDTOCols = choferDTOCols;
	}

	public String getNombreChoferBusqueda() {
		return nombreChoferBusqueda;
	}

	public void setNombreChoferBusqueda(String nombreChoferBusqueda) {
		this.nombreChoferBusqueda = nombreChoferBusqueda;
	}

	public String getNumeroDocumentoBusqueda() {
		return numeroDocumentoBusqueda;
	}

	public void setNumeroDocumentoBusqueda(String numeroDocumentoBusqueda) {
		this.numeroDocumentoBusqueda = numeroDocumentoBusqueda;
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

	public ContactoDTO getContactoDTO() {
		return contactoDTO;
	}

	public void setContactoDTO(ContactoDTO contactoDTO) {
		this.contactoDTO = contactoDTO;
	}

	public Collection<CatalogoValorDTO> getTiposLicenciasCols() {
		return tiposLicenciasCols;
	}

	public void setTiposLicenciasCols(Collection<CatalogoValorDTO> tiposLicenciasCols) {
		this.tiposLicenciasCols = tiposLicenciasCols;
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

	public String getDocumentoTransportista() {
		return documentoTransportista;
	}

	public void setDocumentoTransportista(String documentoTransportista) {
		this.documentoTransportista = documentoTransportista;
	}

	public Long getCodigoTransportistaSeleccionado() {
		return codigoTransportistaSeleccionado;
	}

	public void setCodigoTransportistaSeleccionado(Long codigoTransportistaSeleccionado) {
		this.codigoTransportistaSeleccionado = codigoTransportistaSeleccionado;
	}

	public Boolean getChoferCreado() {
		return choferCreado;
	}

	public void setChoferCreado(Boolean choferCreado) {
		this.choferCreado = choferCreado;
	}
}
