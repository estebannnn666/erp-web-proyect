
package ec.com.erp.web.pedidos.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.ClienteDTO;
import ec.com.erp.cliente.mdl.dto.DetallePedidoDTO;
import ec.com.erp.cliente.mdl.dto.EstadoPedidoDTO;
import ec.com.erp.cliente.mdl.dto.PedidoDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.id.PedidoID;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.pedidos.datamanager.PedidosDataManager;

/**
 * Controlador para administracion de pedidos
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class PedidosController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private PedidoDTO pedidoDTO;
	private DetallePedidoDTO detallePedidoDTO;
	private List<DetallePedidoDTO> detallePedidoDTOCols;
	private EstadoPedidoDTO estadoPedidoDTO;
	private ClienteDTO clienteDTO;
	private Collection<ClienteDTO> clienteDTOCols;
	
	// Data Managers
	@ManagedProperty(value="#{pedidosDataManager}")
	private PedidosDataManager pedidosDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private Collection<PedidoDTO> pedidosDTOCols;
	private Collection<ArticuloDTO> articuloDTOCols;
	private ArticuloDTO articuloDTO;
	private String numeroDocumentoBusqueda;
	private String nombreClienteBusqueda;
	private String razonSocialBusqueda;
	private String codigoBarrasBusqueda;
	private String nombreArticuloBusqueda;
	private Date fechaPedidoBusqueda;
	private String estadoPedidoBusqueda;
	private String documentoCliente;
	private Collection<CatalogoValorDTO> estadoPedidosCatalogoValorDTOCols;
	private Integer page;
	private Long codigoClienteSeleccionado;
	private String valorCodigoBarras;
	private String value;
	private Integer contDetalle;
	private Boolean pedidoGuardado;
	private String mensaje;
	private String valorAccion;
	private Boolean mostrarIcono;

	@PostConstruct
	public void postConstruct() {
		this.pedidoGuardado = Boolean.FALSE;
		this.pedidoDTO = new PedidoDTO();
		this.pedidoDTO.setFechaPedido(new Date());
		this.pedidoDTO.setTotalCompra(BigDecimal.ZERO);
		SecuenciaDTO secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(PedidoID.NOMBRE_SECUENCIA);
		this.pedidoDTO.setNumeroPedido("P-"+secuenciaPedido.getValorSecuencia());
		this.detallePedidoDTO = new DetallePedidoDTO();
		this.estadoPedidoDTO = new EstadoPedidoDTO();
		this.detallePedidoDTOCols = new ArrayList<DetallePedidoDTO>();
		DetallePedidoDTO detalle = null;
		contDetalle = 1;
		for(int i=0; i< 10; i++) {
			detalle = new DetallePedidoDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.detallePedidoDTOCols.add(detalle);
			contDetalle++;
		}
		this.page = 0;
		this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, null);
		
		if(pedidosDataManager.getPedidoDTOEditar() != null && pedidosDataManager.getPedidoDTOEditar().getId().getCodigoPedido() != null)
		{
			this.setPedidoDTO(pedidosDataManager.getPedidoDTOEditar());
			this.setClienteDTO(pedidosDataManager.getPedidoDTOEditar().getClienteDTO());
			this.documentoCliente = this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getNumeroRuc() : this.clienteDTO.getPersonaDTO().getNumeroDocumento();
			this.setDetallePedidoDTOCols((List<DetallePedidoDTO>)pedidosDataManager.getPedidoDTOEditar().getDetallePedidoDTOCols());
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/pedidos/adminBusquedaPedidos.xhtml")) {
			this.pedidosDTOCols = ERPFactory.pedidos.getPedidoServicio().findObtenerPedidosRegistrados(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return pedidosDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}
	
	public void reloadPagina(ActionEvent e) {
		System.out.println("Reload");
	}
	
	/**
	 * Calcular el total y subtotal
	 * @param e
	 */
	public void calcularTotalPedido(AjaxBehaviorEvent e) {
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto =  idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2])+1;

		for(DetallePedidoDTO detallePedidoDTO : detallePedidoDTOCols) {
			if(detallePedidoDTO.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(detallePedidoDTO.getCantidad() != null && detallePedidoDTO.getArticuloDTO().getPrecio() != null) {
					BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+detallePedidoDTO.getCantidad())).multiply(detallePedidoDTO.getArticuloDTO().getPrecio());
					detallePedidoDTO.setSubTotal(subTotal);
					this.calcularTotal();
				}
				break;
			}
		}
	}
	
	/**
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para consultar cliente por documento 
	 * @param e
	 */
	public void realizarConsultaClienteByDocumento(AjaxBehaviorEvent e) {
		Collection<ClienteDTO> clienteDTOColsTemp = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), documentoCliente, null);
		if(CollectionUtils.isNotEmpty(clienteDTOColsTemp)) {
			ClienteDTO clienteDTOTemp = clienteDTOColsTemp.iterator().next();
			if(clienteDTOTemp != null) {
				this.setClienteDTO(clienteDTOTemp);
			}
		}
		else
		{
			this.setClienteDTO(new ClienteDTO());
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.busqueda.cliente"));
		}
	}
	
	/**
	 * Agregar nueva fila pedido
	 * @param e
	 */
	public void agragarRegistroDetallePedido(ActionEvent e) {
		DetallePedidoDTO detalle = new DetallePedidoDTO();
		detalle.setArticuloDTO(new ArticuloDTO());
		detalle.getId().setCodigoCompania(contDetalle);
		this.detallePedidoDTOCols.add(detalle);
		contDetalle++;
	}
	
	/**
	 * Metodo para asignar los valores del articulo seleccionado
	 * @param e
	 */
	public void asignarValoresArticulo(AjaxBehaviorEvent e) {
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto =  idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2])+1;
		
		for(DetallePedidoDTO detallePedidoDTO : detallePedidoDTOCols) {
			if(detallePedidoDTO.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				// Se busca el artculo con el codigo de barras seleccionado para asignar el resto de informacion del articulo
				ArticuloDTO articuloDTOTemp = new ArticuloDTO();
				for(ArticuloDTO articuloDTOSearch : this.articuloDTOCols) {
					if(detallePedidoDTO.getArticuloDTO().getNombreArticulo().equals(articuloDTOSearch.getNombreArticulo())) {
						articuloDTOTemp.setCantidadStock(articuloDTOSearch.getCantidadStock());
						articuloDTOTemp.setCodigoBarras(articuloDTOSearch.getCodigoBarras());
						articuloDTOTemp.setNombreArticulo(articuloDTOSearch.getNombreArticulo());
						articuloDTOTemp.setPeso(articuloDTOSearch.getPeso());
						articuloDTOTemp.setPrecio(articuloDTOSearch.getPrecio());
						articuloDTOTemp.getId().setCodigoArticulo(articuloDTOSearch.getId().getCodigoArticulo());
						break;
					}
				}				
				// Se agrega el articulo al detalle del pedido y se realiza las respectivas operaciones
				detallePedidoDTO.setArticuloDTO(articuloDTOTemp);
				if(detallePedidoDTO.getCantidad() != null && detallePedidoDTO.getArticuloDTO().getPrecio() != null) {
					BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+detallePedidoDTO.getCantidad())).multiply(detallePedidoDTO.getArticuloDTO().getPrecio());
					detallePedidoDTO.setSubTotal(subTotal);
					detallePedidoDTO.setCodigoArticulo(articuloDTOTemp.getId().getCodigoArticulo());
					this.calcularTotal();
				}
				break;
			}
		}
	}
	
	/**
	 * Metodo para buscar articulos
	 * @param e
	 */
	public void busquedaPedididos(ActionEvent e){
		try {
			this.pedidosDTOCols = ERPFactory.pedidos.getPedidoServicio().findObtenerPedidosRegistrados(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null);
			if(CollectionUtils.isEmpty(this.pedidosDTOCols)){
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
	public void guadarActualizarPedido(ActionEvent e){
		try {
			this.setPedidoGuardado(Boolean.FALSE);
			if(this.validarInformacionRequerida()) {
				this.setShowMessagesBar(Boolean.FALSE);
				this.pedidoDTO.setCodigoCliente(this.clienteDTO.getId().getCodigoCliente());
				this.pedidoDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.pedidoDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.pedidoDTO.setDetallePedidoDTOCols(detallePedidoDTOCols);
				ERPFactory.pedidos.getPedidoServicio().transGuardarPedido(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.pedidoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setPedidoGuardado(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.correcto"));
			}
			else
			{
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
	 * Metodo para validar la informacion requerida de la pantalla
	 */
	private Boolean validarInformacionRequerida() {
		Boolean valido = Boolean.TRUE;
		if(this.clienteDTO == null || this.clienteDTO.getId().getCodigoCliente() == null) {
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.cliente"));
		}
		if(CollectionUtils.isEmpty(detallePedidoDTOCols)) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.detalle"));
		}else
		{
			Boolean ban = Boolean.FALSE;
			for(DetallePedidoDTO detallePedidoDTOAux : detallePedidoDTOCols) {
				if(detallePedidoDTOAux.getCodigoArticulo() != null) {
					ban = Boolean.TRUE;
					break;
				}
			}
			if(!ban) {
				valido = Boolean.FALSE;
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.detalle"));
			}
		}
		
		return valido;
	}
	
	/**
	 * Metodo para buscar articulos
	 * @param e
	 */
	public void busquedaClientes(ActionEvent e){
		try {
			this.clienteDTOCols = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
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
	 * Metodo para agragar el cliente a la vista
	 */
	public void agragarCliente(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoCliente", PredicateUtils.equalPredicate(this.codigoClienteSeleccionado));
		// Validacion de objeto existente
		this.clienteDTO  = (ClienteDTO) CollectionUtils.find(this.clienteDTOCols, testPredicate);
		this.documentoCliente = this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getNumeroRuc() : this.clienteDTO.getPersonaDTO().getNumeroDocumento();
	}
	
	/**
	 * Seleccionar cliente del popUp
	 * @param e
	 */
	public void seleccionCliente(ValueChangeEvent e)
	{
		this.codigoClienteSeleccionado = (Long)e.getNewValue();
	}
	
	/**
	 * Seleccionar cliente del popUp
	 * @param e
	 */
	public void seleccionClientes(AjaxBehaviorEvent e)
	{
		System.out.println("Entro selecionar codigo:");
		System.out.println(":"+this.codigoClienteSeleccionado);
	}
	
	/**
	 * Metodo borrar pantalla y crear un articulo nuevo
	 * @param e
	 */
	public void clearNuevoPedido(ActionEvent e){
		this.setShowMessagesBar(Boolean.FALSE);
		this.pedidoDTO = new PedidoDTO();
		SecuenciaDTO secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(PedidoID.NOMBRE_SECUENCIA);
		this.pedidoDTO.setNumeroPedido("P-"+secuenciaPedido.getValorSecuencia());
		this.pedidoDTO.setFechaPedido(new Date());
		this.clienteDTO = new ClienteDTO();
		this.documentoCliente = "";
		this.detallePedidoDTO = new DetallePedidoDTO();
		this.estadoPedidoDTO = new EstadoPedidoDTO();
		this.detallePedidoDTOCols = new ArrayList<DetallePedidoDTO>();
		DetallePedidoDTO detalle = null;
		contDetalle = 1;
		for(int i=0; i< 10; i++) {
			detalle = new DetallePedidoDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.detallePedidoDTOCols.add(detalle);
			contDetalle++;
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
	public String regresarBusquedaPedidos(){
		this.setPedidoGuardado(Boolean.FALSE);
		this.pedidosDataManager.setPedidoDTOEditar(new PedidoDTO());
		return "/modules/pedidos/adminBusquedaPedidos.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo articulo
	 * @return
	 */
	public String crearNuevoPedido(){
		this.setPedidoGuardado(Boolean.FALSE);
		return "/modules/pedidos/nuevoPedido.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarPedidoEditar() {
		if(this.pedidoDTO == null) {
			return null;
		}else{
			this.pedidosDataManager.setPedidoDTOEditar(this.pedidoDTO);
			return "/modules/pedidos/nuevoPedido.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo para cargar datos del pedido a cancelar o entregar
	 * @return
	 */
	public void cargarDatosPedido(ActionEvent e) {
		String nombreComponente = e.getComponent().getClientId();
		if(nombreComponente.contains("btnEntregar")){
			this.mensaje = ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.confirmacion.estado.entregado");
			this.mostrarIcono = Boolean.TRUE;
		}
		else{
			this.mensaje = ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.confirmacion.estado.cancelar");
			this.mostrarIcono = Boolean.FALSE;
		}
	}
	
	/**
	 * Metodo para cargar datos detalle
	 * @return
	 */
	public void actualizarEstadoPedido(ActionEvent e) {
		try {
			ERPFactory.estadopedido.getEstadoPedidoServicio().transActualizarEstadoPorEstadoyPedido(loginController.getUsuariosDTO().getCodigoCompania(), this.pedidoDTO.getId().getCodigoPedido(), this.valorAccion, loginController.getUsuariosDTO().getId().getUserId());
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.estado.actualizado"));
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Metodo para cargar datos detalle
	 * @return
	 */
	public void cargarPedidoDetalle(ActionEvent e) {
		System.out.println("Ingreso a cargar detalle");
	}
	
	/**
	 * Metodo para agregar nuevo detalle al pedido
	 * @param e
	 */
	public void agregarDetallePedido(ActionEvent e){
		detallePedidoDTOCols.add(new DetallePedidoDTO());
	}
	
	/**
	 * Metodo para eliminar registro 
	 * @param detallePedidoDTO
	 */
	public void eliminarDetallePedido(DetallePedidoDTO detallePedidoDTO) {
		detallePedidoDTOCols.remove(detallePedidoDTO);
		this.calcularTotal();
		this.ordenarDetalles();
	}
	
	/**
	 * Metodo para reordeneas detalles del pedido
	 */
	public void ordenarDetalles() {
		int cont = 1;
		for(DetallePedidoDTO detalle : this.detallePedidoDTOCols) {
			detalle.getId().setCodigoCompania(cont);
			cont++;
		}
	}
	
	/**
	 * Metodo para calcular el total del pedido
	 */
	public void calcularTotal() {
		this.pedidoDTO.setTotalCompra(BigDecimal.ZERO);
		BigDecimal totalPedido = BigDecimal.ZERO;
		for (DetallePedidoDTO detallePedidoDTO : detallePedidoDTOCols) {
			if(detallePedidoDTO.getSubTotal() != null) {
				totalPedido = totalPedido.add(detallePedidoDTO.getSubTotal());
			}
		}
		this.pedidoDTO.setTotalCompra(totalPedido);
	}
	
	/**
	 * Borrar filtro de numero documento
	 */
	public void borrarBusquedaNumeroDocumento(ActionEvent e){
		this.numeroDocumentoBusqueda = "";
	}
	
	/**
	 * Borrar filtro de nombre cliente persona
	 */
	public void borrarBusquedaNombreCliente(ActionEvent e){
		this.nombreClienteBusqueda = "";
	}
	
	/**
	 * Borrar filtro de razon social empresa
	 */
	public void borrarBusquedaRazonSocial(ActionEvent e){
		this.razonSocialBusqueda = "";
	}
	
	/**
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaCodigoBarras(ActionEvent e){
		this.codigoBarrasBusqueda = "";
	}
	
	/**
	 * Borrar filtro de nombre articulo
	 */
	public void borrarBusquedaDescripcionArticulo(ActionEvent e){
		this.nombreArticuloBusqueda = "";
	}
	
	/**
	 * Borrar filtro de estado de pedido
	 */
	public void borrarBusquedaEstadoPedido(ActionEvent e){
		this.estadoPedidoBusqueda = "";
	}
	
	public void setPedidosDataManager(PedidosDataManager pedidosDataManager) {
		this.pedidosDataManager = pedidosDataManager;
	}

	public Collection<PedidoDTO> getPedidosDTOCols() {
		return pedidosDTOCols;
	}

	public void setPedidosDTOCols(Collection<PedidoDTO> pedidosDTOCols) {
		this.pedidosDTOCols = pedidosDTOCols;
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

	public PedidoDTO getPedidoDTO() {
		return pedidoDTO;
	}

	public void setPedidoDTO(PedidoDTO pedidoDTO) {
		this.pedidoDTO = pedidoDTO;
	}

	public DetallePedidoDTO getDetallePedidoDTO() {
		return detallePedidoDTO;
	}

	public void setDetallePedidoDTO(DetallePedidoDTO detallePedidoDTO) {
		this.detallePedidoDTO = detallePedidoDTO;
	}

	public EstadoPedidoDTO getEstadoPedidoDTO() {
		return estadoPedidoDTO;
	}

	public void setEstadoPedidoDTO(EstadoPedidoDTO estadoPedidoDTO) {
		this.estadoPedidoDTO = estadoPedidoDTO;
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

	public String getRazonSocialBusqueda() {
		return razonSocialBusqueda;
	}

	public void setRazonSocialBusqueda(String razonSocialBusqueda) {
		this.razonSocialBusqueda = razonSocialBusqueda;
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

	public String getEstadoPedidoBusqueda() {
		return estadoPedidoBusqueda;
	}

	public void setEstadoPedidoBusqueda(String estadoPedidoBusqueda) {
		this.estadoPedidoBusqueda = estadoPedidoBusqueda;
	}

	public Date getFechaPedidoBusqueda() {
		return fechaPedidoBusqueda;
	}

	public void setFechaPedidoBusqueda(Date fechaPedidoBusqueda) {
		this.fechaPedidoBusqueda = fechaPedidoBusqueda;
	}

	public Collection<CatalogoValorDTO> getEstadoPedidosCatalogoValorDTOCols() {
		return estadoPedidosCatalogoValorDTOCols;
	}

	public void setEstadoPedidosCatalogoValorDTOCols(Collection<CatalogoValorDTO> estadoPedidosCatalogoValorDTOCols) {
		this.estadoPedidosCatalogoValorDTOCols = estadoPedidosCatalogoValorDTOCols;
	}

	public String getDocumentoCliente() {
		return documentoCliente;
	}

	public void setDocumentoCliente(String documentoCliente) {
		this.documentoCliente = documentoCliente;
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

	public Long getCodigoClienteSeleccionado() {
		return codigoClienteSeleccionado;
	}

	public void setCodigoClienteSeleccionado(Long codigoClienteSeleccionado) {
		this.codigoClienteSeleccionado = codigoClienteSeleccionado;
	}

	public Collection<DetallePedidoDTO> getDetallePedidoDTOCols() {
		return detallePedidoDTOCols;
	}

	public void setDetallePedidoDTOCols(List<DetallePedidoDTO> detallePedidoDTOCols) {
		this.detallePedidoDTOCols = detallePedidoDTOCols;
	}

	public Collection<ArticuloDTO> getArticuloDTOCols() {
		return articuloDTOCols;
	}

	public void setArticuloDTOCols(Collection<ArticuloDTO> articuloDTOCols) {
		this.articuloDTOCols = articuloDTOCols;
	}

	public ArticuloDTO getArticuloDTO() {
		return articuloDTO;
	}

	public void setArticuloDTO(ArticuloDTO articuloDTO) {
		this.articuloDTO = articuloDTO;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValorCodigoBarras() {
		return valorCodigoBarras;
	}

	public void setValorCodigoBarras(String valorCodigoBarras) {
		this.valorCodigoBarras = valorCodigoBarras;
	}

	public Integer getContDetalle() {
		return contDetalle;
	}

	public void setContDetalle(Integer contDetalle) {
		this.contDetalle = contDetalle;
	}

	public Boolean getPedidoGuardado() {
		return pedidoGuardado;
	}

	public void setPedidoGuardado(Boolean pedidoGuardado) {
		this.pedidoGuardado = pedidoGuardado;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getValorAccion() {
		return valorAccion;
	}

	public void setValorAccion(String valorAccion) {
		this.valorAccion = valorAccion;
	}

	public Boolean getMostrarIcono() {
		return mostrarIcono;
	}

	public void setMostrarIcono(Boolean mostrarIcono) {
		this.mostrarIcono = mostrarIcono;
	}
	
}
