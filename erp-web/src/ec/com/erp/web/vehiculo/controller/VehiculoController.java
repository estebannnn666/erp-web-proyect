
package ec.com.erp.web.vehiculo.controller;

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

import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.ChoferDTO;
import ec.com.erp.cliente.mdl.dto.EmpresaDTO;
import ec.com.erp.cliente.mdl.dto.PersonaDTO;
import ec.com.erp.cliente.mdl.dto.TransportistaDTO;
import ec.com.erp.cliente.mdl.dto.VehiculoChoferDTO;
import ec.com.erp.cliente.mdl.dto.VehiculoDTO;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.vehiculo.datamanager.VehiculoDataManager;

/**
 * Controlador para administracion de vehiculos
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class VehiculoController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private VehiculoDTO vehiculoDTO;
	private TransportistaDTO transportistaDTO;
	private Collection<TransportistaDTO> transportistaDTOCols;
	private Collection<ChoferDTO> choferDTOCols;
	private Collection<VehiculoDTO> vehiculoDTOCols;
	private VehiculoChoferDTO vehiculoChoferDTO; 
	private Collection<VehiculoChoferDTO> vehiculoChoferDTOCols;
	private ChoferDTO choferDTO;
	private String numeroDocumentoTransportista;
	private Long codigoTransportistaSeleccionado;
	private Long codigoChoferSeleccionado;
	
	// Data Managers
	@ManagedProperty(value="#{vehiculoDataManager}")
	private VehiculoDataManager vehiculoDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<CatalogoValorDTO> tiposVehiculosCols;
	private Collection<CatalogoValorDTO> tiposChoferCols;
	private String placaBusqueda;
	private String numeroDocumentoBusqueda;
	private String nombreTransportistaBusqueda;
	private Integer page;
	private Boolean controlPopUp;
	private String documentoBusqueda;
	private boolean bandera;
	private boolean personaExistente;
	private Boolean vehiculoCreado;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.vehiculoCreado = Boolean.FALSE;
		this.controlPopUp = Boolean.FALSE;
		this.choferDTO = new ChoferDTO();
		this.vehiculoDTO = new VehiculoDTO();
		this.vehiculoChoferDTO = new VehiculoChoferDTO();
		this.vehiculoDTOCols = new ArrayList<VehiculoDTO>();
		this.vehiculoChoferDTOCols = new ArrayList<VehiculoChoferDTO>();
		this.transportistaDTOCols = new ArrayList<TransportistaDTO>();
		this.choferDTOCols = new ArrayList<>();
		this.page = 0;
		this.tiposVehiculosCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_VEHICULOS);
		
		if(vehiculoDataManager.getVehiculoDTOEditar() != null && vehiculoDataManager.getVehiculoDTOEditar().getId().getCodigoVehiculo() != null)
		{
			this.setVehiculoDTO(vehiculoDataManager.getVehiculoDTOEditar());
			this.setVehiculoChoferDTOCols(vehiculoDataManager.getVehiculoDTOEditar().getVehiculoChoferDTOCols());
			this.setTransportistaDTO(vehiculoDataManager.getVehiculoDTOEditar().getTransportistaDTO());
			this.numeroDocumentoTransportista = this.transportistaDTO.getPersonaDTO() == null ? this.transportistaDTO.getEmpresaDTO().getNumeroRuc() : this.transportistaDTO.getPersonaDTO().getNumeroDocumento();
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/vehiculo/adminBusquedaVehiculo.xhtml")) {
			this.vehiculoDTOCols = ERPFactory.vehiculo.getVehiculoServicio().findObtenerListaVehiculos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), placaBusqueda, numeroDocumentoBusqueda, nombreTransportistaBusqueda);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return vehiculoDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para consultar vehiculos por placa 
	 * @param e
	 */
	public void realizarConsultaVehiculoPlaca(AjaxBehaviorEvent e) {
		String placa = this.vehiculoDTO.getPlaca().toUpperCase();
		Collection<VehiculoDTO> vehiculoDTOCoslTemp = ERPFactory.vehiculo.getVehiculoServicio().findObtenerListaVehiculos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), placa, null, null);
		if(CollectionUtils.isNotEmpty(vehiculoDTOCoslTemp)) {
			VehiculoDTO vehiculoDTOTemp = vehiculoDTOCoslTemp.iterator().next();
			if(vehiculoDTOTemp != null) {
				if(vehiculoDTOTemp.getPlaca() == null) {
					vehiculoDTOTemp.setPlaca(placa);
				}
				this.setVehiculoDTO(vehiculoDTOTemp);
				if(vehiculoDTOTemp.getTransportistaDTO() != null) {
					this.setTransportistaDTO(vehiculoDTOTemp.getTransportistaDTO());
					this.numeroDocumentoTransportista = vehiculoDTOTemp.getTransportistaDTO().getPersonaDTO() == null ? vehiculoDTOTemp.getTransportistaDTO().getEmpresaDTO().getNumeroRuc() : vehiculoDTOTemp.getTransportistaDTO().getPersonaDTO().getNumeroDocumento();
				}
				this.setVehiculoChoferDTOCols(vehiculoDTOTemp.getVehiculoChoferDTOCols());
			}
		}
	}
	
	/**
	 * Metodo para consultar transprtista por numero de documento 
	 * @param e
	 */
	public void realizarConsultaTransportista(AjaxBehaviorEvent e) {
		Collection<TransportistaDTO> transportistaDTOTempCols = ERPFactory.transportista.getTransportistasServicio().findObtenerListaTransportistas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.numeroDocumentoTransportista, null);
		if(CollectionUtils.isNotEmpty(transportistaDTOTempCols)) {
			this.setTransportistaDTO(transportistaDTOTempCols.iterator().next());
			this.vehiculoDTO.setCodigoTransportista(this.getTransportistaDTO().getId().getCodigoTransportista());
		}
		else
		{
			this.transportistaDTO = new TransportistaDTO();
			this.numeroDocumentoTransportista = "";
		}
	}
	
	/**
	 * Metodo para buscar chofer por documento
	 * @param e
	 */
	public void buscarDatosPersona(AjaxBehaviorEvent e) {
		ChoferDTO choferDTO = ERPFactory.chofer.getChoferServicio().findObtenerChoferByDocumento(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.documentoBusqueda);
		if(choferDTO == null || choferDTO.getId().getCodigoChofer() == null)
		{
			personaExistente = Boolean.FALSE;
			vehiculoChoferDTO.getChoferDTO().getPersonaDTO().setNombreCompleto("");
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "El chofer aun no se encuentra registrado, por favor ingrese un nuevo chofer en el modulo de choferes.");
		}
		else		
		{
			this.setChoferDTO(choferDTO);
			personaExistente = Boolean.TRUE;
			vehiculoChoferDTO.setChoferDTO(choferDTO);	
		}
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
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarVehiculoEditar() {
		if(this.vehiculoDTO == null) {
			return null;
		}else{
			this.vehiculoDataManager.setVehiculoDTOEditar(this.vehiculoDTO);
			return "/modules/vehiculo/nuevoVehiculo.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo para reiniciar datos
	 * @param e
	 */
	public void abrirPopUp(ActionEvent e){
		personaExistente = Boolean.FALSE;
		controlPopUp = Boolean.FALSE;
		bandera = false;
		vehiculoChoferDTO = new VehiculoChoferDTO();
		vehiculoChoferDTO.setChoferDTO(new ChoferDTO());
		vehiculoChoferDTO.getChoferDTO().setPersonaDTO(new PersonaDTO());
		this.tiposChoferCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_CHOFERES);
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
		this.vehiculoDTO.setCodigoTransportista(this.transportistaDTO.getId().getCodigoTransportista());
		this.numeroDocumentoTransportista = this.transportistaDTO.getPersonaDTO() == null ? this.transportistaDTO.getEmpresaDTO().getNumeroRuc() : this.transportistaDTO.getPersonaDTO().getNumeroDocumento();
	}
	
	/**
	 * Metodo para buscar choferes
	 * @param e
	 */
	public void busquedaChoferes(ActionEvent e){
		try {
			this.choferDTOCols = ERPFactory.chofer.getChoferServicio().findObtenerListaChoferes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
			if(CollectionUtils.isEmpty(this.choferDTOCols)){
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
	 * Seleccionar chofer del popUp
	 * @param e
	 */
	public void seleccionChofer(ValueChangeEvent e)
	{
		this.codigoChoferSeleccionado = (Long)e.getNewValue();
	}
	
	/**
	 * Metodo para agragar el chofer a la vista
	 */
	public void agragarChoferPopUp(ActionEvent e) {
		// Verificar si existe en la coleccion
		Predicate testPredicate = new BeanPredicate("id.codigoChofer", PredicateUtils.equalPredicate(this.codigoChoferSeleccionado));
		// Validacion de objeto existente
		this.setChoferDTO((ChoferDTO) CollectionUtils.find(this.choferDTOCols, testPredicate));
		this.documentoBusqueda = choferDTO.getPersonaDTO().getNumeroDocumento();
		personaExistente = Boolean.TRUE;
		vehiculoChoferDTO.setChoferDTO(choferDTO);
	}
	
	/**
	 * Agrega datos a la lista de choferes
	 * @param e
	 */
	public void agregarChofer(ActionEvent e){
		if(personaExistente)
		{
			// Validar e inicializar lista
			if(this.getVehiculoChoferDTOCols() == null)
			{
				this.setVehiculoChoferDTOCols(new ArrayList<VehiculoChoferDTO>());
			}
			
			boolean ban = false;
			
			if(!bandera)
			{
				if(this.getVehiculoChoferDTOCols().size() > 0)
				{			
					for(VehiculoChoferDTO vehiculoChof: this.getVehiculoChoferDTOCols())
					{
						if(vehiculoChof.getChoferDTO().getPersonaDTO().getNumeroDocumento().equals(vehiculoChoferDTO.getChoferDTO().getPersonaDTO().getNumeroDocumento()))
						{
							ban = true;
							break;
						}
					}		
				}
			}
			
			if(ban)
			{
				controlPopUp = Boolean.FALSE;
				this.setShowMessagesBar(Boolean.TRUE);
				MensajesController.addError(null, "El chofer ya se encuentra asignado al vehiculo");
			}
			else
			{		
				if(choferDTO != null) {
					vehiculoChoferDTO.setCodigoChofer(choferDTO.getId().getCodigoChofer());
				}
				vehiculoChoferDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				vehiculoChoferDTO.setUsuarioRegistro(this.loginController.getUsuariosDTO().getId().getUserId());
				vehiculoChoferDTO.setCodigoTipoChofer(ERPConstantes.CODIGO_CATALOGO_TIPOS_CHOFERES);
				for(CatalogoValorDTO cat: this.tiposChoferCols)
				{
					if(cat.getId().getCodigoCatalogoValor().equals(vehiculoChoferDTO.getCodigoValorTipoChofer()))
					{
						vehiculoChoferDTO.setTipoChoferCatalogoValorDTO(cat);
					}
				}
				if(vehiculoChoferDTO.getDescripcion() != null)
					vehiculoChoferDTO.setDescripcion(vehiculoChoferDTO.getDescripcion().toUpperCase());
				this.getVehiculoChoferDTOCols().add(vehiculoChoferDTO);
				controlPopUp = Boolean.TRUE;
			}
			
			bandera = false;
		}
		else
		{
			controlPopUp = Boolean.FALSE;
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "El chofer que desea ingresar aun no se encuentra registrado.");
		}
	}
	
	/**
	 * Metodo para buscar vehiculos
	 * @param e
	 */
	public void busquedaVehiculos(ActionEvent e){
		this.buscarVehiculos();
	}
	
	/**
	 * Metodo para buscar vehiculos enter
	 * @param e
	 */
	public void busquedaVehiculosEnter(AjaxBehaviorEvent e){
		this.buscarVehiculos();
	}
	
	/**
	 * Metodo para buscar vehiculos por filtros o todos
	 * @param e
	 */
	public void buscarVehiculos(){
		try {
			this.vehiculoDTOCols = ERPFactory.vehiculo.getVehiculoServicio().findObtenerListaVehiculos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), placaBusqueda, numeroDocumentoBusqueda, nombreTransportistaBusqueda);
			if(CollectionUtils.isEmpty(this.vehiculoDTOCols)){
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
	 * Metodo para guardar o actualizar vehiculo
	 * @param e
	 */
	public void guadarActualizarVehiculo(ActionEvent e){
		try {
			this.setVehiculoCreado(Boolean.FALSE);
			this.setShowMessagesBar(Boolean.FALSE);
			if(this.validarDatosCompletos()) {
				this.vehiculoDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.vehiculoDTO.setVehiculoChoferDTOCols(vehiculoChoferDTOCols);
				this.vehiculoDTO.setUsuarioRegistro(this.loginController.getUsuariosDTO().getId().getUserId());
				ERPFactory.vehiculo.getVehiculoServicio().transGuardarActualizarVehiculo(vehiculoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setVehiculoCreado(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.vehiculos.mensaje.correcto"));
			}
			else {
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
	 * Se valida que todos los datos de la pantalla esten completos
	 * @return
	 */
	private Boolean validarDatosCompletos() {
		Boolean valido = Boolean.TRUE;
		if(vehiculoDTO.getPlaca() == null || vehiculoDTO.getPlaca().trim() == "") {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.placa"));
		}
		if(vehiculoDTO.getCodigoTransportista() == null) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.transportista"));
		}
		if(vehiculoDTO.getCodigoValorTipoVehiculo() == null || vehiculoDTO.getCodigoValorTipoVehiculo().trim() == "") {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.tipo.vehiculo"));
		}
		if(vehiculoDTO.getMarca() == null || vehiculoDTO.getMarca().trim() == "") {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.marca"));
		}
		if(vehiculoDTO.getColor() == null || vehiculoDTO.getColor().trim() == "") {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.color"));
		}
		if(vehiculoDTO.getModelo() == null || vehiculoDTO.getModelo().trim() == "") {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.model"));
		}
		if(CollectionUtils.isEmpty(vehiculoChoferDTOCols)) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.lista.chofer"));
		}
		
		return valido;
	}
	
	/**
	 * Metodo para borrar un chofer de la lista del vehiculo
	 * @param e
	 */
	public void borrarChofer(ActionEvent e) {
		this.vehiculoChoferDTOCols.remove(this.vehiculoChoferDTO);
	}
	
	
	/**
	 * Metodo borrar pantalla y crear un vehiculo nuevo
	 * @param e
	 */
	public void clearNuevoVehiculo(ActionEvent e){
		this.setVehiculoCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.vehiculoDataManager.setVehiculoDTOEditar(new VehiculoDTO());
		this.choferDTO = new ChoferDTO();
		this.vehiculoDTO = new VehiculoDTO();
		this.vehiculoChoferDTO = new VehiculoChoferDTO(); 
		this.vehiculoChoferDTOCols = new ArrayList<VehiculoChoferDTO>();
		this.numeroDocumentoTransportista = "";
		this.transportistaDTO = new TransportistaDTO();
		this.transportistaDTO.setPersonaDTO(new PersonaDTO());
		this.transportistaDTO.setEmpresaDTO(new EmpresaDTO());
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
	public String regresarBusquedaVehiculos(){
		this.vehiculoDataManager.setVehiculoDTOEditar(new VehiculoDTO());
		return "/modules/vehiculo/adminBusquedaVehiculo.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo vehiculo
	 * @return
	 */
	public String crearNuevoVehiculo(){
		return "/modules/vehiculo/nuevoVehiculo.xhtml?faces-redirect=true";
	}
	
	/**
	 * Borrar filtro de numero de documento
	 */
	public void borrarBusquedaPlaca(ActionEvent e){
		this.placaBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de numero de documento
	 */
	public void borrarBusquedaNumeroDocumento(ActionEvent e){
		this.numeroDocumentoBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de nombre de transportista duenio del vehiculo
	 */
	public void borrarBusquedaNombreTransportista(ActionEvent e){
		this.nombreTransportistaBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	public void setVehiculoDataManager(VehiculoDataManager vehiculoDataManager) {
		this.vehiculoDataManager = vehiculoDataManager;
	}

	public ChoferDTO getChoferDTO() {
		return choferDTO;
	}

	public void setChoferDTO(ChoferDTO choferDTO) {
		this.choferDTO = choferDTO;
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

	public VehiculoDTO getVehiculoDTO() {
		return vehiculoDTO;
	}

	public void setVehiculoDTO(VehiculoDTO vehiculoDTO) {
		this.vehiculoDTO = vehiculoDTO;
	}

	public VehiculoChoferDTO getVehiculoChoferDTO() {
		return vehiculoChoferDTO;
	}

	public void setVehiculoChoferDTO(VehiculoChoferDTO vehiculoChoferDTO) {
		this.vehiculoChoferDTO = vehiculoChoferDTO;
	}

	public Collection<VehiculoChoferDTO> getVehiculoChoferDTOCols() {
		return vehiculoChoferDTOCols;
	}

	public void setVehiculoChoferDTOCols(Collection<VehiculoChoferDTO> vehiculoChoferDTOCols) {
		this.vehiculoChoferDTOCols = vehiculoChoferDTOCols;
	}

	public Collection<CatalogoValorDTO> getTiposVehiculosCols() {
		return tiposVehiculosCols;
	}

	public void setTiposVehiculosCols(Collection<CatalogoValorDTO> tiposVehiculosCols) {
		this.tiposVehiculosCols = tiposVehiculosCols;
	}

	public String getPlacaBusqueda() {
		return placaBusqueda;
	}

	public void setPlacaBusqueda(String placaBusqueda) {
		this.placaBusqueda = placaBusqueda;
	}

	public String getNombreTransportistaBusqueda() {
		return nombreTransportistaBusqueda;
	}

	public void setNombreTransportistaBusqueda(String nombreTransportistaBusqueda) {
		this.nombreTransportistaBusqueda = nombreTransportistaBusqueda;
	}

	public Collection<VehiculoDTO> getVehiculoDTOCols() {
		return vehiculoDTOCols;
	}

	public void setVehiculoDTOCols(Collection<VehiculoDTO> vehiculoDTOCols) {
		this.vehiculoDTOCols = vehiculoDTOCols;
	}

	public TransportistaDTO getTransportistaDTO() {
		return transportistaDTO;
	}

	public void setTransportistaDTO(TransportistaDTO transportistaDTO) {
		this.transportistaDTO = transportistaDTO;
	}

	public String getNumeroDocumentoTransportista() {
		return numeroDocumentoTransportista;
	}

	public void setNumeroDocumentoTransportista(String numeroDocumentoTransportista) {
		this.numeroDocumentoTransportista = numeroDocumentoTransportista;
	}

	public Collection<TransportistaDTO> getTransportistaDTOCols() {
		return transportistaDTOCols;
	}

	public void setTransportistaDTOCols(Collection<TransportistaDTO> transportistaDTOCols) {
		this.transportistaDTOCols = transportistaDTOCols;
	}

	public Long getCodigoTransportistaSeleccionado() {
		return codigoTransportistaSeleccionado;
	}

	public void setCodigoTransportistaSeleccionado(Long codigoTransportistaSeleccionado) {
		this.codigoTransportistaSeleccionado = codigoTransportistaSeleccionado;
	}

	public Boolean getControlPopUp() {
		return controlPopUp;
	}

	public void setControlPopUp(Boolean controlPopUp) {
		this.controlPopUp = controlPopUp;
	}

	public String getDocumentoBusqueda() {
		return documentoBusqueda;
	}

	public void setDocumentoBusqueda(String documentoBusqueda) {
		this.documentoBusqueda = documentoBusqueda;
	}

	public Collection<CatalogoValorDTO> getTiposChoferCols() {
		return tiposChoferCols;
	}

	public void setTiposChoferCols(Collection<CatalogoValorDTO> tiposChoferCols) {
		this.tiposChoferCols = tiposChoferCols;
	}

	public boolean isBandera() {
		return bandera;
	}

	public void setBandera(boolean bandera) {
		this.bandera = bandera;
	}

	public boolean isPersonaExistente() {
		return personaExistente;
	}

	public void setPersonaExistente(boolean personaExistente) {
		this.personaExistente = personaExistente;
	}

	public Boolean getVehiculoCreado() {
		return vehiculoCreado;
	}

	public void setVehiculoCreado(Boolean vehiculoCreado) {
		this.vehiculoCreado = vehiculoCreado;
	}

	public Collection<ChoferDTO> getChoferDTOCols() {
		return choferDTOCols;
	}

	public void setChoferDTOCols(Collection<ChoferDTO> choferDTOCols) {
		this.choferDTOCols = choferDTOCols;
	}

	public Long getCodigoChoferSeleccionado() {
		return codigoChoferSeleccionado;
	}

	public void setCodigoChoferSeleccionado(Long codigoChoferSeleccionado) {
		this.codigoChoferSeleccionado = codigoChoferSeleccionado;
	}
}
