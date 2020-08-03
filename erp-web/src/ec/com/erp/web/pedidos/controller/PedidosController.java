
package ec.com.erp.web.pedidos.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import org.primefaces.event.SelectEvent;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.cliente.mdl.dto.ArticuloImpuestoDTO;
import ec.com.erp.cliente.mdl.dto.ArticuloUnidadManejoDTO;
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.ClienteDTO;
import ec.com.erp.cliente.mdl.dto.DetallePedidoDTO;
import ec.com.erp.cliente.mdl.dto.EstadoPedidoDTO;
import ec.com.erp.cliente.mdl.dto.InventarioDTO;
import ec.com.erp.cliente.mdl.dto.PedidoDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.VendedorDTO;
import ec.com.erp.cliente.mdl.dto.id.PedidoID;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.pedidos.datamanager.PedidosDataManager;

/**
 * Controlador para administracion de pedidos
 * 
 * @author hgudino
 *
 */
@ManagedBean(name = "pedidosController", eager = true)
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
	@ManagedProperty(value = "#{pedidosDataManager}")
	private PedidosDataManager pedidosDataManager;

	@ManagedProperty(value = "#{loginController}")
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
	private Date fechaPedidoInicioBusqueda;
	private Date fechaPedidoFinBusqueda;
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
	private Collection<VendedorDTO> vendedorDTOCols;
	private String documentoVendedorBusqueda;
	private String nombreVendedorBusqueda;
	private String nombreVendedor;
	private Long codigoVendedorSeleccionado;

	@ManagedProperty("#{articuloService}")
	private ArticuloService service;

	@PostConstruct
	public void postConstruct() {
		Calendar fechaInferior = Calendar.getInstance();
		this.documentoVendedorBusqueda = null;
		this.nombreVendedorBusqueda = null;
		this.nombreVendedor = null;
		this.vendedorDTOCols = new ArrayList<>();
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		Calendar fechaSuperior = Calendar.getInstance();
		fechaPedidoInicioBusqueda = fechaInferior.getTime();
		fechaPedidoFinBusqueda = fechaSuperior.getTime();
		this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, null);
		this.loginController.activarMenusSeleccionado();
		this.pedidoGuardado = Boolean.FALSE;
		this.pedidoDTO = new PedidoDTO();
		this.pedidoDTO.setFechaPedido(new Date());
		this.pedidoDTO.setTotalCompra(BigDecimal.ZERO);
		SecuenciaDTO secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio()
				.findObtenerSecuenciaByNombre(PedidoID.NOMBRE_SECUENCIA);
		this.pedidoDTO.setNumeroPedido("P-" + secuenciaPedido.getValorSecuencia());
		this.detallePedidoDTO = new DetallePedidoDTO();
		this.estadoPedidoDTO = new EstadoPedidoDTO();
		this.detallePedidoDTOCols = new ArrayList<DetallePedidoDTO>();
		DetallePedidoDTO detalle = null;
		contDetalle = 1;
		for (int i = 0; i < 10; i++) {
			detalle = new DetallePedidoDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.detallePedidoDTOCols.add(detalle);
			contDetalle++;
		}
		this.page = 0;

		if (pedidosDataManager.getPedidoDTOEditar() != null
				&& pedidosDataManager.getPedidoDTOEditar().getId().getCodigoPedido() != null) {
			this.setPedidoDTO(pedidosDataManager.getPedidoDTOEditar());
			this.setClienteDTO(pedidosDataManager.getPedidoDTOEditar().getClienteDTO());
			this.documentoCliente = this.clienteDTO.getPersonaDTO() == null
					? this.clienteDTO.getEmpresaDTO().getNumeroRuc()
					: this.clienteDTO.getPersonaDTO().getNumeroDocumento();
			this.setDetallePedidoDTOCols(
					(List<DetallePedidoDTO>) pedidosDataManager.getPedidoDTOEditar().getDetallePedidoDTOCols());
			if (CollectionUtils.isNotEmpty(this.detallePedidoDTOCols)) {
				detallePedidoDTOCols.stream().forEach(
						detalleitem -> detalleitem.setNombreArticulo(detalleitem.getArticuloDTO().getNombreArticulo()));
			}
		}
		if (FacesContext.getCurrentInstance().getViewRoot().getViewId()
				.equals("/modules/pedidos/adminBusquedaPedidos.xhtml")) {
			this.pedidosDTOCols = ERPFactory.pedidos.getPedidoServicio().findObtenerPedidosRegistrados(
					Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null, null, null, null);
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

	/**
	 * Metodo para buscar vendedores
	 * @param e
	 */
	public void busquedaVendedores(ActionEvent e){
		this.busquedaVendedor();
	}
	
	/**
	 * Metodo para buscar vendedores al dar enter
	 * @param e
	 */
	public void busquedaVendedorEnter(AjaxBehaviorEvent e){
		this.busquedaVendedor();
	}
	
	public void busquedaVendedor() {
		try {
			this.vendedorDTOCols = ERPFactory.vendedor.getVendedorServicio().findObtenerListaVendedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda, nombreVendedorBusqueda);
			this.setShowMessagesBar(Boolean.FALSE);
		} catch (ERPException e1) {
	        this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, e1.getMessage());
	        
		} catch (Exception e2) {
	        this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Seleccionar vendedor del popUp
	 * @param e
	 */
	public void seleccionVendedor(ValueChangeEvent e){
		this.codigoVendedorSeleccionado = (Long)e.getNewValue();
	}
	
	/**
	 * Borrar filtro de busqueda por documento vendedor
	 */
	public void borrarBusquedaDocumentoVendedor(ActionEvent e){
		this.documentoVendedorBusqueda = "";
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
	 * Metodo para agragar el vendedor a la vista
	 */
	public void agragarVendedor(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoVendedor", PredicateUtils.equalPredicate(this.codigoVendedorSeleccionado));
		// Validacion de objeto existente
		VendedorDTO vendedorDTO  = (VendedorDTO) CollectionUtils.find(this.vendedorDTOCols, testPredicate);
		this.pedidoDTO.setVendedorDTO(vendedorDTO);
		this.pedidoDTO.setCodigoVendedor(vendedorDTO.getId().getCodigoVendedor());
		this.nombreVendedor = vendedorDTO.getPersonaDTO().getNombreCompleto();
	}
	
	public void reloadPagina(ActionEvent e) {
		System.out.println("Reload");
	}

	public List<ArticuloDTO> completeTheme(String query) {
		String queryLowerCase = query.toLowerCase();
		List<ArticuloDTO> allThemes = service.getArticuloDTOCols();
		return allThemes.stream().filter(t -> t.getNombreArticulo().toLowerCase().contains(queryLowerCase))
				.collect(Collectors.toList());
	}

	/**
	 * Calcular el total y subtotal
	 * 
	 * @param e
	 */
	public void calcularTotalPedido(AjaxBehaviorEvent e) {
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto = idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2]) + 1;

		for (DetallePedidoDTO detallePedidoDTO : detallePedidoDTOCols) {
			if (detallePedidoDTO.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if (detallePedidoDTO.getCantidad() != null && detallePedidoDTO.getArticuloDTO().getPrecio() != null && detallePedidoDTO.getCodigoArticuloUnidadManejo() != null) {
					
					// Se obtiene existencia actual
					InventarioDTO inventarioDTOAux = ERPFactory.inventario.getInventarioServicio().findObtenerUltimoInventarioByArticulo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), detallePedidoDTO.getArticuloDTO().getCodigoBarras(), detallePedidoDTO.getCodigoArticuloUnidadManejo());
					
					if (detallePedidoDTO.getCantidad().intValue() > inventarioDTOAux.getCantidadExistencia().intValue()) {
						detallePedidoDTO.setCantidad(detallePedidoDTO.getArticuloDTO().getCantidadStock());
						this.setShowMessagesBar(Boolean.TRUE);
						MensajesController.addWarn(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.busqueda.pedidos.mensaje.error.mayor"));
						return;
					}
					ArticuloUnidadManejoDTO articuloUnidadManejo = obtenerUnidadManejoPorCodigo(detallePedidoDTO.getCodigoArticuloUnidadManejo(), detallePedidoDTO.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+(detallePedidoDTO.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(detallePedidoDTO.getArticuloDTO().getPrecio());
					detallePedidoDTO.setSubTotal(subTotal);
					detallePedidoDTO.setArticuloUnidadManejoDTO(articuloUnidadManejo);
					this.calcularTotal();
				}
				this.setShowMessagesBar(Boolean.FALSE);
				break;
			}
		}
	}

	public void onItemSelect(SelectEvent event) {
		for (DetallePedidoDTO detallePedidoDTOTemp : detallePedidoDTOCols) {
			if (detallePedidoDTOTemp.getNombreArticulo() != null) {
				String queryLowerCase = detallePedidoDTOTemp.getNombreArticulo().toLowerCase();
				ArticuloDTO articuloSeleccionado = this.getArticuloDTOCols().stream()
						.filter(articulo -> articulo.getNombreArticulo().toLowerCase().equals(queryLowerCase))
						.findFirst().orElse(null);
				this.setShowMessagesBar(Boolean.FALSE);
				detallePedidoDTOTemp.setArticuloDTO(articuloSeleccionado);
			}

			if ((detallePedidoDTOTemp.getCantidad() == null || detallePedidoDTOTemp.getCantidad().intValue() == 0) && detallePedidoDTOTemp.getArticuloDTO().getPrecio() != null) {
				detallePedidoDTOTemp.setCantidad(1);
			}
			if (detallePedidoDTOTemp.getCantidad() != null	&& detallePedidoDTOTemp.getArticuloDTO().getPrecio() != null && CollectionUtils.isNotEmpty(detallePedidoDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
 				ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(detallePedidoDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
				BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+(detallePedidoDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(detallePedidoDTOTemp.getArticuloDTO().getPrecio());
				detallePedidoDTOTemp.setSubTotal(subTotal);
				detallePedidoDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
				detallePedidoDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
				detallePedidoDTOTemp.setCodigoArticulo(detallePedidoDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
				// Se obtiene existencia actual
				InventarioDTO inventarioDTOAux = ERPFactory.inventario.getInventarioServicio().findObtenerUltimoInventarioByArticulo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), detallePedidoDTOTemp.getArticuloDTO().getCodigoBarras(), detallePedidoDTOTemp.getCodigoArticuloUnidadManejo());
				detallePedidoDTOTemp.getArticuloDTO().setCantidadStock(inventarioDTOAux.getCantidadExistencia());
				this.calcularTotal();
			}
		}
	}
	
	/**
	 * Metodo para agregar calcular 
	 * @param e
	 */
	public void seleccionarUnidadManejo(ValueChangeEvent e) {
		Integer codigoUnidadManejo = (Integer)e.getNewValue();
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto =  idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2])+1;
		
		for(DetallePedidoDTO detallePedidoDTOTemp : detallePedidoDTOCols) {
			if(detallePedidoDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(detallePedidoDTOTemp.getCantidad() != null && detallePedidoDTOTemp.getArticuloDTO().getPrecio() != null && detallePedidoDTOTemp.getCodigoArticuloUnidadManejo() != null) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = obtenerUnidadManejoPorCodigo(codigoUnidadManejo, detallePedidoDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+(detallePedidoDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(detallePedidoDTOTemp.getArticuloDTO().getPrecio());
					detallePedidoDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
					detallePedidoDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
					detallePedidoDTOTemp.setSubTotal(subTotal);
					// Se obtiene existencia actual
					InventarioDTO inventarioDTOAux = ERPFactory.inventario.getInventarioServicio().findObtenerUltimoInventarioByArticulo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), detallePedidoDTOTemp.getArticuloDTO().getCodigoBarras(), detallePedidoDTOTemp.getCodigoArticuloUnidadManejo());
					detallePedidoDTOTemp.getArticuloDTO().setCantidadStock(inventarioDTOAux.getCantidadExistencia());
					this.calcularTotal();
				}
				break;
			}
		}
	}
	
	/**
	 * Metodo para obtener unidad de manejo por defecto
	 * @param articuloUnidadManejoCols
	 * @return
	 */
	public ArticuloUnidadManejoDTO obtenerUnidadManejoPorDefecto(Collection<ArticuloUnidadManejoDTO> articuloUnidadManejoCols) {
		return articuloUnidadManejoCols.stream().filter(unidadManejo ->  unidadManejo.getEsPorDefecto()).findFirst().orElse(null);
	}
	
	/**
	 * Metodo para obtener unidad de manejo por codigo
	 * @param articuloUnidadManejoCols
	 * @return
	 */
	public ArticuloUnidadManejoDTO obtenerUnidadManejoPorCodigo(Integer codigoArticuloUnidadManejo, Collection<ArticuloUnidadManejoDTO> articuloUnidadManejoCols) {
		return articuloUnidadManejoCols
				.stream().filter(unidadManejo -> unidadManejo.getId().getCodigoArticuloUnidadManejo().intValue() == codigoArticuloUnidadManejo.intValue())
				.findFirst().orElse(null);
	}

	/**
	 * Metodo para ir a la pantalla menu principal
	 * 
	 * @return
	 */
	public String regresarMenuPrincipal() {
		this.loginController.desActivarMenusSeleccionado();
		this.loginController.setActivarInicio(Boolean.TRUE);
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}

	/**
	 * Metodo para consultar cliente por documento
	 * 
	 * @param e
	 */
	public void realizarConsultaClienteByDocumento(AjaxBehaviorEvent e) {
		Collection<ClienteDTO> clienteDTOColsTemp = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(
				Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), documentoCliente, null);
		if (CollectionUtils.isNotEmpty(clienteDTOColsTemp)) {
			ClienteDTO clienteDTOTemp = clienteDTOColsTemp.iterator().next();
			if (clienteDTOTemp != null) {
				this.setClienteDTO(clienteDTOTemp);
			}
		} else {
			this.setClienteDTO(new ClienteDTO());
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null,
					ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.busqueda.cliente"));
		}
	}

	/**
	 * Agregar nueva fila pedido
	 * 
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
	 * 
	 * @param e
	 */
	public void asignarValoresArticuloPrime(AjaxBehaviorEvent e) {
		for (DetallePedidoDTO detallePedidoDTOTemp : detallePedidoDTOCols) {
			if ((detallePedidoDTOTemp.getCantidad() == null || detallePedidoDTOTemp.getCantidad().intValue() == 0)
					&& detallePedidoDTOTemp.getArticuloDTO().getPrecio() != null) {
				detallePedidoDTOTemp.setCantidad(1);
			}
			if (detallePedidoDTOTemp.getCantidad() != null
					&& detallePedidoDTOTemp.getArticuloDTO().getPrecio() != null) {
				BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf("" + detallePedidoDTOTemp.getCantidad()))
						.multiply(detallePedidoDTOTemp.getArticuloDTO().getPrecio());
				detallePedidoDTOTemp.setSubTotal(subTotal);
				detallePedidoDTOTemp
						.setCodigoArticulo(detallePedidoDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
				this.calcularTotal();
			}
		}
	}

	public List<String> completeNombreArticulo(String query) {
		String queryLowerCase = query.toLowerCase();
		List<ArticuloDTO> allThemes = this.getArticuloDTOCols().stream()
				.filter(t -> t.getNombreArticulo().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());
		List<String> results = new ArrayList<>();
		allThemes.stream().forEach(articulo -> results.add(articulo.getNombreArticulo()));
		return results;
	}

	/**
	 * Seleccion Local
	 * 
	 * @param e
	 */
	public void seleccionLocal(ValueChangeEvent e) {
		e.getNewValue();
	}

	/**
	 * Metodo para buscar articulos
	 * 
	 * @param e
	 */
	public void busquedaPedididos(ActionEvent e) {
		this.buscarPedidos();
	}

	public void busquedaPedididosEnter(AjaxBehaviorEvent e) {
		this.buscarPedidos();
	}

	public void buscarPedidos() {
		try {
			this.pedidosDTOCols = ERPFactory.pedidos.getPedidoServicio().findObtenerPedidosRegistrados(
					Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumentoBusqueda,
					nombreClienteBusqueda, new Timestamp(fechaPedidoInicioBusqueda.getTime()),
					new Timestamp(fechaPedidoFinBusqueda.getTime()), null);
			if (CollectionUtils.isEmpty(this.pedidosDTOCols)) {
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("No se encontraron resultados para la b\u00FAsqueda realizada.",
						"ERROR MSG");
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
	 * 
	 * @param e
	 */
	public void guadarActualizarPedido(ActionEvent e) {
		Boolean esGuardar = Boolean.TRUE;
		if(this.pedidoDTO.getId().getCodigoPedido() != null) {
			esGuardar = Boolean.FALSE;
		}
		try {
			this.setPedidoGuardado(Boolean.FALSE);
			if (this.validarInformacionRequerida()) {
				this.setShowMessagesBar(Boolean.FALSE);
				this.pedidoDTO.setCodigoCliente(this.clienteDTO.getId().getCodigoCliente());
				this.pedidoDTO.setClienteDTO(this.clienteDTO);
				this.pedidoDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.pedidoDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.pedidoDTO.setDetallePedidoDTOCols(detallePedidoDTOCols);
				ERPFactory.pedidos.getPedidoServicio()
						.transGuardarPedido(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.pedidoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setPedidoGuardado(Boolean.TRUE);
				MensajesController.addInfo(null,
						ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.correcto"));
			} else {
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			if(esGuardar) {
				this.pedidoDTO.getId().setCodigoPedido(null);
				this.detallePedidoDTOCols.stream().forEach(detalle ->{
					detalle.getId().setCodigoDetallePedido(null);
				});
			}
			this.setShowMessagesBar(Boolean.TRUE);
			if(e1.getMessage() == null) {
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.error"));
			}else {
				MensajesController.addError(null, e1.getMessage());
			}
		} catch (Exception e2) {
			if(esGuardar) {
				this.pedidoDTO.getId().setCodigoPedido(null);
				this.detallePedidoDTOCols.stream().forEach(detalle ->{
					detalle.getId().setCodigoDetallePedido(null);
				});
			}
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}

	/**
	 * Metodo para validar la informacion requerida de la pantalla
	 */
	private Boolean validarInformacionRequerida() {
		Boolean valido = Boolean.TRUE;
		if (this.clienteDTO == null || this.clienteDTO.getId().getCodigoCliente() == null) {
			MensajesController.addError(null,
					ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.cliente"));
		}
		if (CollectionUtils.isEmpty(detallePedidoDTOCols)) {
			valido = Boolean.FALSE;
			MensajesController.addError(null,
					ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.detalle"));
		} else {
			Boolean ban = Boolean.FALSE;
			for (DetallePedidoDTO detallePedidoDTOAux : detallePedidoDTOCols) {
				if (detallePedidoDTOAux.getCodigoArticulo() != null) {
					ban = Boolean.TRUE;
					break;
				}
			}
			if (!ban) {
				valido = Boolean.FALSE;
				MensajesController.addError(null,
						ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.detalle"));
			}
		}

		return valido;
	}

	/**
	 * Metodo para buscar articulos
	 * 
	 * @param e
	 */
	public void busquedaClientes(ActionEvent e) {
		try {
			this.setShowMessagesBar(Boolean.FALSE);
			this.clienteDTOCols = ERPFactory.clientes.getClientesServicio()
					.findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
			if (CollectionUtils.isEmpty(this.clienteDTOCols)) {
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("No se encontraron resultados para la b\u00FAsqueda realizada.",
						"ERROR MSG");
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
		Predicate testPredicate = new BeanPredicate("id.codigoCliente",
				PredicateUtils.equalPredicate(this.codigoClienteSeleccionado));
		// Validacion de objeto existente
		this.clienteDTO = (ClienteDTO) CollectionUtils.find(this.clienteDTOCols, testPredicate);
		this.documentoCliente = this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getNumeroRuc()
				: this.clienteDTO.getPersonaDTO().getNumeroDocumento();
	}

	/**
	 * Seleccionar cliente del popUp
	 * 
	 * @param e
	 */
	public void seleccionCliente(ValueChangeEvent e) {
		this.codigoClienteSeleccionado = (Long) e.getNewValue();
	}

	/**
	 * Seleccionar cliente del popUp
	 * 
	 * @param e
	 */
	public void seleccionClientes(AjaxBehaviorEvent e) {
		System.out.println("Entro selecionar codigo:");
		System.out.println(":" + this.codigoClienteSeleccionado);
	}

	/**
	 * Metodo borrar pantalla y crear un articulo nuevo
	 * 
	 * @param e
	 */
	public void clearNuevoPedido(ActionEvent e) {
		this.setShowMessagesBar(Boolean.FALSE);
		this.codigoClienteSeleccionado = null;
		this.codigoVendedorSeleccionado = null;
		this.documentoVendedorBusqueda = null;
		this.nombreVendedorBusqueda = null;
		this.nombreVendedor = null;
		this.pedidoDTO = new PedidoDTO();
		SecuenciaDTO secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio()
				.findObtenerSecuenciaByNombre(PedidoID.NOMBRE_SECUENCIA);
		this.pedidoDTO.setNumeroPedido("P-" + secuenciaPedido.getValorSecuencia());
		this.pedidoDTO.setFechaPedido(new Date());
		this.clienteDTO = new ClienteDTO();
		this.documentoCliente = "";
		this.detallePedidoDTO = new DetallePedidoDTO();
		this.estadoPedidoDTO = new EstadoPedidoDTO();
		this.detallePedidoDTOCols = new ArrayList<DetallePedidoDTO>();
		DetallePedidoDTO detalle = null;
		contDetalle = 1;
		for (int i = 0; i < 10; i++) {
			detalle = new DetallePedidoDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.detallePedidoDTOCols.add(detalle);
			contDetalle++;
		}
	}

	/**
	 * Metodo para refrescar pantalla
	 * 
	 * @param e
	 */
	public void refrescarPantalla(ActionEvent e) {
		System.out.println("Ingreso a refrescar pantalla");
	}

	/**
	 * Metodo para regresar a la busqueda de articulos
	 * 
	 * @param e
	 */
	public String regresarBusquedaPedidos() {
		this.setPedidoGuardado(Boolean.FALSE);
		this.pedidosDataManager.setPedidoDTOEditar(new PedidoDTO());
		return "/modules/pedidos/adminBusquedaPedidos.xhtml?faces-redirect=true";
	}

	/**
	 * Metodo para ir a la pantalla de nuevo articulo
	 * 
	 * @return
	 */
	public String crearNuevoPedido() {
		this.setPedidoGuardado(Boolean.FALSE);
		return "/modules/pedidos/nuevoPedido.xhtml?faces-redirect=true";
	}

	/**
	 * Metodo para cargar datos a editar
	 * 
	 * @return
	 */
	public String cargarPedidoEditar() {
		if (this.pedidoDTO == null) {
			return null;
		} else {
			this.pedidosDataManager.setPedidoDTOEditar(this.pedidoDTO);
			return "/modules/pedidos/nuevoPedido.xhtml?faces-redirect=true";
		}
	}

	/**
	 * Metodo para cargar datos del pedido a cancelar o entregar
	 * 
	 * @return
	 */
	public void cargarDatosPedido(ActionEvent e) {
		String nombreComponente = e.getComponent().getClientId();
		if (nombreComponente.contains("btnEntregar")) {
			this.mensaje = ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.confirmacion.estado.entregado");
			this.mostrarIcono = Boolean.TRUE;
		} else {
			this.mensaje = ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.confirmacion.estado.cancelar");
			this.mostrarIcono = Boolean.FALSE;
		}
	}

	/**
	 * Metodo para cargar datos detalle
	 * 
	 * @return
	 */
	public void actualizarEstadoPedido(ActionEvent e) {
		try {
			ERPFactory.estadopedido.getEstadoPedidoServicio().transActualizarEstadoPorEstadoyPedido(
					loginController.getUsuariosDTO().getCodigoCompania(), this.pedidoDTO.getId().getCodigoPedido(),
					this.valorAccion, loginController.getUsuariosDTO().getId().getUserId());
			this.pedidosDTOCols = ERPFactory.pedidos.getPedidoServicio().findObtenerPedidosRegistrados(
					Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null, null, null, null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addInfo(null,
					ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.estado.actualizado"));
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			if(e1.getMessage() == null) {
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.error"));
			}else {
				MensajesController.addError(null, e1.getMessage());
			}
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}

	/**
	 * Metodo para cargar datos detalle
	 * 
	 * @return
	 */
	public void cargarPedidoDetalle(ActionEvent e) {
		System.out.println("Ingreso a cargar detalle");
	}

	/**
	 * Metodo para agregar nuevo detalle al pedido
	 * 
	 * @param e
	 */
	public void agregarDetallePedido(ActionEvent e) {
		detallePedidoDTOCols.add(new DetallePedidoDTO());
	}

	/**
	 * Metodo para eliminar registro
	 * 
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
		for (DetallePedidoDTO detalle : this.detallePedidoDTOCols) {
			detalle.getId().setCodigoCompania(cont);
			cont++;
		}
	}

	/**
	 * Metodo para calcular el total del pedido
	 */
	public void calcularTotal() {
		this.pedidoDTO.setTotalCompra(BigDecimal.ZERO);
		BigDecimal subTotal = BigDecimal.ZERO;
		BigDecimal totalSinImpuesto = BigDecimal.ZERO;
		BigDecimal totalConImpuesto = BigDecimal.ZERO;
		BigDecimal totalIva = BigDecimal.ZERO;
		BigDecimal totalPedido = BigDecimal.ZERO;
		for (DetallePedidoDTO detallePedidoDTO : detallePedidoDTOCols) {
			if (detallePedidoDTO.getSubTotal() != null) {
				subTotal = subTotal.add(detallePedidoDTO.getSubTotal());
			}
			if (detallePedidoDTO.getArticuloDTO().getTieneImpuesto()) {
				totalConImpuesto = totalConImpuesto.add(detallePedidoDTO.getSubTotal());
				for (ArticuloImpuestoDTO impuesto : detallePedidoDTO.getArticuloDTO().getArticuloImpuestoDTOCols()) {
					totalIva = totalIva.add(BigDecimal.valueOf((subTotal.doubleValue()
							* impuesto.getImpuestoDTO().getValorImpuesto().doubleValue()) / Double.valueOf(100)));
				}
			}else {
				if (detallePedidoDTO.getSubTotal() != null) {
					totalSinImpuesto = totalSinImpuesto.add(detallePedidoDTO.getSubTotal());
				}
			}
		}
		totalPedido = subTotal.add(totalIva);
		this.pedidoDTO.setSubTotal(subTotal);
		this.pedidoDTO.setTotalSinImpuestos(totalSinImpuesto);
		this.pedidoDTO.setTotalImpuestos(totalConImpuesto);
		this.pedidoDTO.setTotalIva(totalIva);
		this.pedidoDTO.setTotalCompra(totalPedido);
		this.pedidoDTO.setDescuento(BigDecimal.ZERO);
	}

	/**
	 * Borrar filtro de numero documento
	 */
	public void borrarBusquedaNumeroDocumento(ActionEvent e) {
		this.numeroDocumentoBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}

	/**
	 * Borrar filtro de nombre cliente persona
	 */
	public void borrarBusquedaNombreCliente(ActionEvent e) {
		this.nombreClienteBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}

	/**
	 * Borrar filtro de razon social empresa
	 */
	public void borrarBusquedaRazonSocial(ActionEvent e) {
		this.razonSocialBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}

	/**
	 * Borrar filtro de codigo de barras
	 */
	public void borrarBusquedaCodigoBarras(ActionEvent e) {
		this.codigoBarrasBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}

	/**
	 * Borrar filtro de nombre articulo
	 */
	public void borrarBusquedaDescripcionArticulo(ActionEvent e) {
		this.nombreArticuloBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}

	/**
	 * Borrar filtro de estado de pedido
	 */
	public void borrarBusquedaEstadoPedido(ActionEvent e) {
		this.estadoPedidoBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
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

	public Date getFechaPedidoInicioBusqueda() {
		return fechaPedidoInicioBusqueda;
	}

	public void setFechaPedidoInicioBusqueda(Date fechaPedidoInicioBusqueda) {
		this.fechaPedidoInicioBusqueda = fechaPedidoInicioBusqueda;
	}

	public Date getFechaPedidoFinBusqueda() {
		return fechaPedidoFinBusqueda;
	}

	public void setFechaPedidoFinBusqueda(Date fechaPedidoFinBusqueda) {
		this.fechaPedidoFinBusqueda = fechaPedidoFinBusqueda;
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

	public void setArticuloDTOCols(List<ArticuloDTO> articuloDTOCols) {
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

	public ArticuloService getService() {
		return service;
	}

	public void setService(ArticuloService service) {
		this.service = service;
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

	public String getNombreVendedor() {
		return nombreVendedor;
	}

	public void setNombreVendedor(String nombreVendedor) {
		this.nombreVendedor = nombreVendedor;
	}

	public Long getCodigoVendedorSeleccionado() {
		return codigoVendedorSeleccionado;
	}

	public void setCodigoVendedorSeleccionado(Long codigoVendedorSeleccionado) {
		this.codigoVendedorSeleccionado = codigoVendedorSeleccionado;
	}

	public String getDocumentoVendedorBusqueda() {
		return documentoVendedorBusqueda;
	}

	public void setDocumentoVendedorBusqueda(String documentoVendedorBusqueda) {
		this.documentoVendedorBusqueda = documentoVendedorBusqueda;
	}
}
