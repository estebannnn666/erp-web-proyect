
package ec.com.erp.web.clientes.controller;

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
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ClienteDTO;
import ec.com.erp.cliente.mdl.dto.ContactoDTO;
import ec.com.erp.cliente.mdl.dto.EmpresaDTO;
import ec.com.erp.cliente.mdl.dto.PersonaDTO;
import ec.com.erp.cliente.mdl.dto.UsuariosDTO;
import ec.com.erp.web.clientes.datamanager.ClientesDataManager;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;

/**
 * Controlador para administracion de clientes
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class ClientesController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private ClienteDTO clienteDTO;
//	private UsuariosDTO usuarioDTO;
	private PersonaDTO personaDTO;
	private EmpresaDTO empresaDTO;
	private ContactoDTO contactoDTO;
	private String repetirContrasenia;
	
	// Data Managers
	@ManagedProperty(value="#{clientesDataManager}")
	private ClientesDataManager clientesDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<ClienteDTO> clienteDTOCols;
	private String numeroDocumentoBusqueda;
	private String nombreClienteBusqueda;
	private Integer page;
	private Boolean clienteCreado;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.clienteCreado = Boolean.FALSE;
		this.clienteDTO = new ClienteDTO();
		this.clienteDTO.setPersonaDTO(new PersonaDTO());
		this.clienteDTO.setEmpresaDTO(new EmpresaDTO());
		this.clienteDTO.setUsuariosDTO(new UsuariosDTO());
		this.clienteDTO.setCodigoValorTipoCliente(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA);
		this.clienteDTO.setCodigoTipoCliente(ERPConstantes.CODIGO_CATALOGO_TIPOS_CLIENTES);
		this.personaDTO = new PersonaDTO();
		this.empresaDTO = new EmpresaDTO();
		this.contactoDTO = new ContactoDTO();
		this.page = 0;
		
		if(clientesDataManager.getClienteDTOEditar() != null && clientesDataManager.getClienteDTOEditar().getId().getCodigoCliente() != null)
		{
			this.setClienteDTO(clientesDataManager.getClienteDTOEditar());
			this.setPersonaDTO(clientesDataManager.getClienteDTOEditar().getPersonaDTO());
			this.setEmpresaDTO(clientesDataManager.getClienteDTOEditar().getEmpresaDTO());
			this.setContactoDTO(clientesDataManager.getClienteDTOEditar().getPersonaDTO() == null ? clientesDataManager.getClienteDTOEditar().getEmpresaDTO().getContactoEmpresaDTO() : clientesDataManager.getClienteDTOEditar().getPersonaDTO().getContactoPersonaDTO());
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/clientes/adminBusquedaClientes.xhtml")) {
			this.clienteDTOCols = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreClienteBusqueda);
		}
		
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return clientesDataManager;
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
	public void busquedaClientes(ActionEvent e){
		this.buscarClientes();
	}
	
	public void busquedaClienteEnter(AjaxBehaviorEvent e){
		this.buscarClientes();
	}
	
	public void buscarClientes(){
		try {
			this.clienteDTOCols = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreClienteBusqueda);
			if(CollectionUtils.isEmpty(this.clienteDTOCols)){
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
	public void guadarActualizarCliente(ActionEvent e){
		try {
			this.setClienteCreado(Boolean.FALSE);
			if(this.validarPantallaClientes()) {
				this.clienteDTO.setUserId(loginController.getUsuariosDTO().getId().getUserId());
				this.clienteDTO.setPersonaDTO(this.personaDTO);
				this.clienteDTO.setEmpresaDTO(this.empresaDTO);
				ERPFactory.clientes.getClientesServicio().transGuardarActualizarClientes(this.clienteDTO, this.contactoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setClienteCreado(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("El usuario y datos del cliente se crearon correctamente.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			if(clientesDataManager.getClienteDTOEditar() == null || clientesDataManager.getClienteDTOEditar().getId().getCodigoCliente() == null) {
				this.clienteDTO.getId().setCodigoCliente(null);
			}
			if(this.clienteDTO.getPersonaDTO() != null) {
				this.clienteDTO.getPersonaDTO().getId().setCodigoPersona(null);
			}
			if(this.clienteDTO.getEmpresaDTO() != null) {
				this.clienteDTO.getEmpresaDTO().getId().setCodigoEmpresa(null);
			}
			if(this.clienteDTO.getUsuariosDTO() != null) {
				this.clienteDTO.getUsuariosDTO().getId().setUserId(null);
			}
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e1.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e2) {
			if(clientesDataManager.getClienteDTOEditar() == null || clientesDataManager.getClienteDTOEditar().getId().getCodigoCliente() == null) {
				this.clienteDTO.getId().setCodigoCliente(null);
			}
			if(this.clienteDTO.getPersonaDTO() != null) {
				this.clienteDTO.getPersonaDTO().getId().setCodigoPersona(null);
			}
			if(this.clienteDTO.getEmpresaDTO() != null) {
				this.clienteDTO.getEmpresaDTO().getId().setCodigoEmpresa(null);
			}
			if(this.clienteDTO.getUsuariosDTO() != null) {
				this.clienteDTO.getUsuariosDTO().getId().setUserId(null);
			}
			this.setShowMessagesBar(Boolean.TRUE);
			FacesMessage msg = new FacesMessage(e2.getMessage(), "ERROR MSG");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
	}
	
	/**
	 * Metodo para limpiar mensajes
	 * @param e
	 */
	public void eliminarMensaje(ValueChangeEvent e) {
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para consultar cliente por numero de documento 
	 * @param e
	 */
	public void realizarConsultaDocumento(AjaxBehaviorEvent e) {
		if(StringUtils.isEmpty(this.personaDTO.getNumeroDocumento()) &&  StringUtils.isEmpty(this.empresaDTO.getNumeroRuc())) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, "El campo no debe estar vacio, ingrese el documento que desea buscar.");
		}else {
			String codigoValorTipoCliente = this.clienteDTO.getCodigoValorTipoCliente();
			String numeroDocumento = "";
			if(codigoValorTipoCliente.equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)) {
				numeroDocumento = this.personaDTO.getNumeroDocumento();
			}
			
			if(codigoValorTipoCliente.equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_EMPRESA)) {
				numeroDocumento = this.empresaDTO.getNumeroRuc();
			}
			
			ClienteDTO clienteDTOTemp = ERPFactory.clientes.getClientesServicio().findObtenerClienteByCodigo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumento, codigoValorTipoCliente);
			
			if(clienteDTOTemp != null) {
				this.setClienteDTO(clienteDTOTemp);
				this.setPersonaDTO(clienteDTOTemp.getPersonaDTO());
				this.setEmpresaDTO(clienteDTOTemp.getEmpresaDTO());
				if(clienteDTOTemp.getPersonaDTO() == null){
					this.setContactoDTO(clienteDTOTemp.getEmpresaDTO().getContactoEmpresaDTO());
					this.personaDTO = new PersonaDTO();
					this.personaDTO.setNumeroDocumento(clienteDTOTemp.getEmpresaDTO().getNumeroRuc());
				}
				else {
					this.setContactoDTO(clienteDTOTemp.getPersonaDTO().getContactoPersonaDTO());
				}
			}
			else
			{
				this.setShowMessagesBar(Boolean.TRUE);
		        MensajesController.addWarn(null, "No se encontr\u00F3 el cliente con el documento ingresado.");
			}
		}
	}
	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaClientes() {
		Boolean validado = Boolean.TRUE;
		// Validacion por tipo de cliente
		if(StringUtils.isEmpty(this.clienteDTO.getCodigoValorTipoCompra())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.tipocliente"));
			validado = Boolean.FALSE;
		}
		
		if(clienteDTO.getCodigoValorTipoCliente().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA)){
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
	 * Metodo borrar pantalla y crear un articulo nuevo
	 * @param e
	 */
	public void clearNuevoCliente(ActionEvent e){
		this.setClienteCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.clienteDTO = new ClienteDTO();
		this.clientesDataManager.setClienteDTOEditar(new ClienteDTO());
		this.clienteDTO.setCodigoValorTipoCliente(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CLIENTE_PERSONA);
		this.clienteDTO.setCodigoTipoCliente(ERPConstantes.CODIGO_CATALOGO_TIPOS_CLIENTES);
		this.personaDTO = new PersonaDTO();
		this.empresaDTO = new EmpresaDTO();
		this.contactoDTO = new ContactoDTO();
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarClienteEditar() {
		if(this.clienteDTO == null) {
			return null;
		}else{
			this.clientesDataManager.setClienteDTOEditar(this.clienteDTO);
			return "/modules/clientes/nuevoCliente.xhtml?faces-redirect=true";
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
	public String regresarBusquedaClientes(){
		this.clientesDataManager.setClienteDTOEditar(new ClienteDTO());
		return "/modules/clientes/adminBusquedaClientes.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo articulo
	 * @return
	 */
	public String crearNuevoCliente(){
		return "/modules/clientes/nuevoCliente.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para descargar clientes de fire base
	 * @param e
	 */
	public void descargarClientesFireBase(ActionEvent e){
		System.out.println("Ingreso a realizar proceson con fire base");
		ERPFactory.firebase.getFireBaseServicio().transDescargarClientesFireBase();
		this.clienteDTOCols = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
		this.setShowMessagesBar(Boolean.TRUE);
		MensajesController.addInfo(null, "Se ha terminado de descargar la informacion de clientes de los dispositivos moviles");
		System.out.println("Finalizo proceso con fire base");
	}
	
	/**
	 * Metodo para subir clientes a fire base
	 * @param e
	 */
	public void cargarClientesFireBase(ActionEvent e){
		System.out.println("Ingreso a realizar carga de datos de clientes a fire base");
		ERPFactory.firebase.getFireBaseServicio().findGuardarClientesFireBase();
		this.setShowMessagesBar(Boolean.TRUE);
		MensajesController.addInfo(null, "Se ha terminado de subir la informacion de clientes para dispositivos moviles");
		System.out.println("Finalizo proceso de carga a fire base");
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
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaNombreCliente(ActionEvent e){
		this.nombreClienteBusqueda = "";
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
	
	public void setClientesDataManager(ClientesDataManager clientesDataManager) {
		this.clientesDataManager = clientesDataManager;
	}

	public ClienteDTO getClienteDTO() {
		return clienteDTO;
	}

	public void setClienteDTO(ClienteDTO clienteDTO) {
		this.clienteDTO = clienteDTO;
	}

	public Collection<ClienteDTO> getClienteDTOCols() {
		return clienteDTOCols;
	}

	public void setClienteDTOCols(Collection<ClienteDTO> clienteDTOCols) {
		this.clienteDTOCols = clienteDTOCols;
	}

	public String getNumeroDocumentoBusqueda() {
		return numeroDocumentoBusqueda;
	}

	public void setNumeroDocumentoBusqueda(String numeroDocumentoBusqueda) {
		this.numeroDocumentoBusqueda = numeroDocumentoBusqueda;
	}

	public String getNombreClienteBusqueda() {
		return nombreClienteBusqueda;
	}

	public void setNombreClienteBusqueda(String nombreClienteBusqueda) {
		this.nombreClienteBusqueda = nombreClienteBusqueda;
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

	/*public UsuariosDTO getUsuarioDTO() {
		return usuarioDTO;
	}

	public void setUsuarioDTO(UsuariosDTO usuarioDTO) {
		this.usuarioDTO = usuarioDTO;
	}*/

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

	public Boolean getClienteCreado() {
		return clienteCreado;
	}

	public void setClienteCreado(Boolean clienteCreado) {
		this.clienteCreado = clienteCreado;
	}
}
