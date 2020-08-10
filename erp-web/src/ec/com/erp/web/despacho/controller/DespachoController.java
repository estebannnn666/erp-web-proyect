
package ec.com.erp.web.despacho.controller;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
import org.primefaces.event.SelectEvent;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.cliente.mdl.dto.ArticuloUnidadManejoDTO;
import ec.com.erp.cliente.mdl.dto.GuiaDespachoDTO;
import ec.com.erp.cliente.mdl.dto.GuiaDespachoDetalleDTO;
import ec.com.erp.cliente.mdl.dto.GuiaDespachoExtrasDTO;
import ec.com.erp.cliente.mdl.dto.GuiaDespachoPedidoDTO;
import ec.com.erp.cliente.mdl.dto.InventarioDTO;
import ec.com.erp.cliente.mdl.dto.PedidoDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.TransportistaDTO;
import ec.com.erp.cliente.mdl.dto.VehiculoChoferDTO;
import ec.com.erp.cliente.mdl.dto.VehiculoDTO;
import ec.com.erp.cliente.mdl.dto.id.GuiaDespachoID;
import ec.com.erp.utilitario.commons.util.HtmlPdf;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.despacho.datamanager.DespachoDataManager;
import ec.com.erp.web.pedidos.controller.ArticuloService;

/**
 * Controlador para administracion de guias de despacho
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class DespachoController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private GuiaDespachoDTO guiaDespachoDTO;
	private Collection<GuiaDespachoDTO> guiaDespachoDTOCols;
	private Collection<GuiaDespachoPedidoDTO> guiaDespachoPedidoDTOCols;
	private GuiaDespachoPedidoDTO guiaDespachoPedidoDTO;
	private Collection<GuiaDespachoExtrasDTO> guiaDespachoExtrasDTOCols;
	private GuiaDespachoExtrasDTO guiaDespachoExtrasDTO;
	private Collection<GuiaDespachoDetalleDTO> guiaDespachoDetalleDTOCols;
	private GuiaDespachoDetalleDTO guiaDespachoDetalleDTO;
	private Collection<PedidoDTO> pedidosDTOCols;
	private PedidoDTO pedidoDTO;
	// Data Managers
	@ManagedProperty(value="#{despachoDataManager}")
	private DespachoDataManager despachoDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	@ManagedProperty("#{articuloService}")
	private ArticuloService service;
	
	// Variables
	private String placaBusqueda;
	private String numeroGuiaDespachoBusqueda;
	private Date fechaDespachoBusqueda;
	private Date fechaDespachoBusquedaFin;
	private String numeroDocumentoChoferBusqueda;
	private String nombreChoferBusqueda;
	private Long codigoPedidoSeleccionado;
	private Integer page;
	private Integer orden;
	private Boolean controlPopUp;
	private Boolean despachoCreado;
	private Boolean sePuedeEliminar;
	private Collection<ArticuloDTO> articuloDTOCols;
	private Integer contDetalle;

	@PostConstruct
	public void postConstruct() {
		
		Calendar fechaInicio = Calendar.getInstance();
		Calendar fechaFin = Calendar.getInstance();
		fechaInicio.set(Calendar.MONTH, 0);
		fechaInicio.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInicio);
		fechaDespachoBusqueda = fechaInicio.getTime();
		fechaDespachoBusquedaFin = fechaFin.getTime();
		this.loginController.activarMenusSeleccionado();
		this.guiaDespachoDTO = new GuiaDespachoDTO();
		this.guiaDespachoDTO.setFechaDespacho(new Date());
		SecuenciaDTO secuenciaDespacho = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(GuiaDespachoID.NOMBRE_SECUENCIA);
		this.guiaDespachoDTO.setNumeroGuiaDespacho("GD-"+secuenciaDespacho.getValorSecuencia());
		this.guiaDespachoPedidoDTO = new GuiaDespachoPedidoDTO();
		this.guiaDespachoExtrasDTO = new GuiaDespachoExtrasDTO();
		this.guiaDespachoDetalleDTO = new GuiaDespachoDetalleDTO();
		this.guiaDespachoDTOCols = new ArrayList<GuiaDespachoDTO>();
		this.guiaDespachoPedidoDTOCols = new ArrayList<GuiaDespachoPedidoDTO>();
		this.guiaDespachoExtrasDTOCols = new ArrayList<GuiaDespachoExtrasDTO>();
		this.guiaDespachoDetalleDTOCols = new ArrayList<>();
		this.pedidosDTOCols = new ArrayList<PedidoDTO>();
		this.page = 0;
		this.orden = 1;
		this.despachoCreado = Boolean.FALSE;
		this.sePuedeEliminar = Boolean.FALSE;
		this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, null);
		contDetalle= 1;
		GuiaDespachoExtrasDTO guiaDespachoExtrasDTOTemp = null;
		for(int i = 0; i < 5 ; i++){
			guiaDespachoExtrasDTOTemp = new GuiaDespachoExtrasDTO();
			guiaDespachoExtrasDTOTemp.setArticuloDTO(new ArticuloDTO());
			guiaDespachoExtrasDTOTemp.getId().setCodigoCompania(contDetalle);
			this.guiaDespachoExtrasDTOCols.add(guiaDespachoExtrasDTOTemp);
			contDetalle++;
		}
		
		// Cargar datos a editar
		if(despachoDataManager.getGuiaDespachoDTOEditar() != null && despachoDataManager.getGuiaDespachoDTOEditar().getId().getCodigoGuiaDespacho() != null)
		{
			this.setGuiaDespachoDTO(despachoDataManager.getGuiaDespachoDTOEditar());
			this.setGuiaDespachoExtrasDTOCols(ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaGuiaDespachoExtrasByNumeroGuia(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), despachoDataManager.getGuiaDespachoDTOEditar().getNumeroGuiaDespacho()));
			this.setGuiaDespachoDetalleDTOCols(ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaGuiaDespachoDetalleByNumeroGuia(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), despachoDataManager.getGuiaDespachoDTOEditar().getNumeroGuiaDespacho()));
			this.setGuiaDespachoPedidoDTOCols(ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaGuiaDespachoPedidosByNumeroGuiaDespacho(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), despachoDataManager.getGuiaDespachoDTOEditar().getNumeroGuiaDespacho()));
		}
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/despachos/adminBusquedaDespacho.xhtml")) {
			this.guiaDespachoDTOCols = ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaDespachosByFiltrosBusqueda(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroGuiaDespachoBusqueda, null, null, placaBusqueda, null, nombreChoferBusqueda);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return despachoDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Seleccionar pedido del popUp
	 * @param e
	 */
	public void seleccionPedido(ValueChangeEvent e)
	{
		this.codigoPedidoSeleccionado = (Long)e.getNewValue();
	}
	
	/**
	 * Metodo para agragar el pedido a la vista
	 */
	public void agragarPedido(ActionEvent e) {
		for(PedidoDTO pedido : this.pedidosDTOCols) {
			if(pedido.getSeleccionado()) {
				// Verificar si existe en la coleccion el pedido
				Predicate testPredicate = new BeanPredicate("id.codigoPedido", PredicateUtils.equalPredicate(pedido.getId().getCodigoPedido()));
				// Validacion de objeto existente
				this.pedidoDTO  = (PedidoDTO) CollectionUtils.find(this.pedidosDTOCols, testPredicate);
				this.guiaDespachoPedidoDTO = new GuiaDespachoPedidoDTO();
				this.guiaDespachoPedidoDTO.setCodigoPedido(this.pedidoDTO.getId().getCodigoPedido());
				this.guiaDespachoPedidoDTO.setPedidoDTO(this.pedidoDTO);
				this.guiaDespachoPedidoDTO.setOrden(orden);
				this.guiaDespachoPedidoDTOCols.add(guiaDespachoPedidoDTO);
				agregarDetallePedido(pedidoDTO);
				orden++;
				controlPopUp = Boolean.TRUE;
			}
		}
	}
	
	private void agregarDetallePedido(PedidoDTO pedidoDTO) {
		pedidoDTO.getDetallePedidoDTOCols().stream().forEach(detalle ->{
			GuiaDespachoDetalleDTO detalleGuia = this.guiaDespachoDetalleDTOCols.stream()
	        		.filter(guiaDetalle -> guiaDetalle.getCodigoArticulo().intValue() == detalle.getCodigoArticulo().intValue() && guiaDetalle.getCodigoArticuloUnidadManejo().intValue() == detalle.getCodigoArticuloUnidadManejo().intValue())
	        		.findFirst().orElse(null);
			if(detalleGuia == null) {
				this.guiaDespachoDetalleDTO = new GuiaDespachoDetalleDTO();
				this.guiaDespachoDetalleDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.guiaDespachoDetalleDTO.setCodigoArticulo(detalle.getCodigoArticulo());
				this.guiaDespachoDetalleDTO.setCodigoArticuloUnidadManejo(detalle.getCodigoArticuloUnidadManejo());
				this.guiaDespachoDetalleDTO.setArticuloUnidadManejoDTO(detalle.getArticuloUnidadManejoDTO());
				this.guiaDespachoDetalleDTO.setDescripcionProducto(detalle.getArticuloDTO().getNombreArticulo());
				this.guiaDespachoDetalleDTO.setCantidad(detalle.getCantidad());
				this.guiaDespachoDetalleDTOCols.add(guiaDespachoDetalleDTO);
			}else {
				this.guiaDespachoDetalleDTOCols.stream().forEach(guiaDesp -> {
					if(guiaDesp.getCodigoArticulo().intValue() == detalle.getCodigoArticulo().intValue() && guiaDesp.getCodigoArticuloUnidadManejo().intValue() == detalle.getCodigoArticuloUnidadManejo().intValue()) {
						guiaDesp.setCantidad(guiaDesp.getCantidad()+detalle.getCantidad());
					}
				});
			}
		});
		
		
	}
	
	/**
	 * Metodo para abrir y consultar datos de pedidos pendientes de entrega
	 * @param e
	 */
	public void abrirPopUpPedidos(ActionEvent e){
		controlPopUp = Boolean.FALSE;
		this.pedidosDTOCols = ERPFactory.pedidos.getPedidoServicio().findObtenerPedidosRegistrados(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null, null, null, ERPConstantes.CODIGO_CATALOGO_VALOR_ESTADO_PEDIDO_REGISTRADO);
		Collection<PedidoDTO> pedidoDTOAuxCols = new ArrayList<PedidoDTO>();
		Boolean ban;
		if(CollectionUtils.isNotEmpty(this.guiaDespachoPedidoDTOCols) && CollectionUtils.isNotEmpty(this.pedidosDTOCols)) {
			for(PedidoDTO pedidoDTO : this.pedidosDTOCols) {
				ban = Boolean.TRUE;
				for(GuiaDespachoPedidoDTO guiaDespachoPedidoDTO: this.guiaDespachoPedidoDTOCols) {
					if(guiaDespachoPedidoDTO.getCodigoPedido().longValue() == pedidoDTO.getId().getCodigoPedido().longValue()) {
						ban = Boolean.FALSE;
						break;
					}
				}
				if(ban) {
					pedidoDTOAuxCols.add(pedidoDTO);
				}
			}
			this.setPedidosDTOCols(pedidoDTOAuxCols);
		}
	}
	
	/**
	 * Metodo para borrar un chofer de la lista del vehiculo
	 * @param e
	 */
	public void borrarPedido(ActionEvent e) {
		try {
			ERPFactory.despacho.getGuiaDespachoServicio().transEliminarPedidoDespacho(this.guiaDespachoDTO.getNumeroGuiaDespacho(), this.guiaDespachoPedidoDTO);
			this.guiaDespachoPedidoDTOCols.remove(this.guiaDespachoPedidoDTO);
			quitarArticulosPedido(this.guiaDespachoPedidoDTO);
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.guia.despacho.mensaje.error.eliminar"));
		}
	}
	
	/**
	 * Metodo cancelar la accion borrar un chofer de la lista del vehiculo
	 * @param e
	 */
	public void cancelarBorrarPedido(ActionEvent e) {
		this.guiaDespachoPedidoDTO = new GuiaDespachoPedidoDTO();
	}
	
	/**
	 * Metodo para borrar un chofer de la lista del vehiculo
	 * @param e
	 */
	public void borrarPedidoExtra(ActionEvent e) {
		try {
			ERPFactory.despacho.getGuiaDespachoServicio().transEliminarPedidosExtras(this.guiaDespachoExtrasDTO);
			this.guiaDespachoExtrasDTOCols.remove(this.guiaDespachoExtrasDTO);
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.guia.despacho.mensaje.error.eliminar"));
		}
	}
	
	/**
	 * Auto completado de articulos extras
	 * @param query
	 * @return
	 */
	public List<String> completeNombreArticuloExtra(String query) {
		this.setShowMessagesBar(Boolean.FALSE);
        String queryLowerCase = query.toLowerCase();
        List<ArticuloDTO> allThemes = this.getArticuloDTOCols().stream()
        		.filter(t -> t.getNombreArticulo().toLowerCase().contains(queryLowerCase))
        		.collect(Collectors.toList());
        List<String> results = new ArrayList<>();
        allThemes.stream().forEach(articulo -> results.add(articulo.getNombreArticulo()));
        return results;
    }
	
	public void onItemSelectExtra(SelectEvent event) {
        for(GuiaDespachoExtrasDTO detallePedidoDTOTemp : this.guiaDespachoExtrasDTOCols) {
        	if(detallePedidoDTOTemp.getDescripcionProducto() != null && detallePedidoDTOTemp.getCodigoArticulo() == null) {
        		String queryLowerCase = detallePedidoDTOTemp.getDescripcionProducto().toLowerCase();
        		ArticuloDTO articuloSeleccionado = this.getArticuloDTOCols().stream()
                		.filter(articulo -> articulo.getNombreArticulo().toLowerCase().equals(queryLowerCase))
                		.findFirst().orElse(null);
        		detallePedidoDTOTemp.setArticuloDTO(articuloSeleccionado);
        	}
        	
        	if((detallePedidoDTOTemp.getCantidad() == null ||  detallePedidoDTOTemp.getCantidad().intValue() == 0) && detallePedidoDTOTemp.getArticuloDTO() != null){
        		detallePedidoDTOTemp.setCantidad(1);
			}
			if(detallePedidoDTOTemp.getCantidad() != null && detallePedidoDTOTemp.getArticuloDTO() != null && detallePedidoDTOTemp.getArticuloDTO().getPrecio() != null && detallePedidoDTOTemp.getCodigoArticulo() == null && CollectionUtils.isNotEmpty(detallePedidoDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
				
        		ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(detallePedidoDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
        		detallePedidoDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
        		// Se obtiene existencia actual
				InventarioDTO inventarioDTOAux = ERPFactory.inventario.getInventarioServicio().findObtenerUltimoInventarioByArticulo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), detallePedidoDTOTemp.getArticuloDTO().getCodigoBarras(), articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
				if (detallePedidoDTOTemp.getCantidad().intValue() > inventarioDTOAux.getCantidadExistencia().intValue()) {
					this.setShowMessagesBar(Boolean.TRUE);
					MensajesController.addWarn(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.busqueda.pedidos.mensaje.error.mayor"));
					return;
				}
				detallePedidoDTOTemp.setCodigoArticulo(detallePedidoDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
        		detallePedidoDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
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
		
		for(GuiaDespachoExtrasDTO guiaDespachoExtrasDTOTemp : guiaDespachoExtrasDTOCols) {
			if(guiaDespachoExtrasDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(codigoUnidadManejo != null && guiaDespachoExtrasDTOTemp.getCantidad() != null && guiaDespachoExtrasDTOTemp.getCodigoArticuloUnidadManejo() != null) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = obtenerUnidadManejoPorCodigo(codigoUnidadManejo, guiaDespachoExtrasDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					// Se obtiene existencia actual
					InventarioDTO inventarioDTOAux = ERPFactory.inventario.getInventarioServicio().findObtenerUltimoInventarioByArticulo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), guiaDespachoExtrasDTOTemp.getArticuloDTO().getCodigoBarras(), articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
					if (guiaDespachoExtrasDTOTemp.getCantidad().intValue() > inventarioDTOAux.getCantidadExistencia().intValue()) {
						this.setShowMessagesBar(Boolean.TRUE);
						MensajesController.addWarn(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.busqueda.pedidos.mensaje.error.mayor"));
						return;
					}
				}
				break;
			}
		}
	}
	
	public void verificarExistencia(AjaxBehaviorEvent e) {
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto = idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2]) + 1;
		for(GuiaDespachoExtrasDTO guiaDespachoExtrasDTOTemp : guiaDespachoExtrasDTOCols) {
			if (guiaDespachoExtrasDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if (guiaDespachoExtrasDTOTemp.getCantidad() != null && guiaDespachoExtrasDTOTemp.getCodigoArticuloUnidadManejo() != null) {
					// Se obtiene existencia actual
					InventarioDTO inventarioDTOAux = ERPFactory.inventario.getInventarioServicio().findObtenerUltimoInventarioByArticulo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), guiaDespachoExtrasDTOTemp.getArticuloDTO().getCodigoBarras(), guiaDespachoExtrasDTOTemp.getCodigoArticuloUnidadManejo());
					
					if (guiaDespachoExtrasDTOTemp.getCantidad().intValue() > inventarioDTOAux.getCantidadExistencia().intValue()) {
						this.setShowMessagesBar(Boolean.TRUE);
						MensajesController.addWarn(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.busqueda.pedidos.mensaje.error.mayor"));
						return;
					}
				}
				this.setShowMessagesBar(Boolean.FALSE);
				break;
			}
		}
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
	 * Metodo para obtener unidad de manejo por defecto
	 * @param articuloUnidadManejoCols
	 * @return
	 */
	public ArticuloUnidadManejoDTO obtenerUnidadManejoPorDefecto(Collection<ArticuloUnidadManejoDTO> articuloUnidadManejoCols) {
		return articuloUnidadManejoCols.stream().filter(unidadManejo ->  unidadManejo.getEsPorDefecto()).findFirst().orElse(null);
	}
	
	/**
	 * Metodo cancelar la accion borrar un chofer de la lista del vehiculo
	 * @param e
	 */
	public void cancelarBorrarPedidoExtra(ActionEvent e) {
		this.guiaDespachoExtrasDTO = new GuiaDespachoExtrasDTO();
	}
	
	/**
	 * Agregar nueva fila extra
	 * @param e
	 */
	public void agragarRegistroExtra(ActionEvent e) {
		GuiaDespachoExtrasDTO guiaDespachoExtrasDTOTemp = new GuiaDespachoExtrasDTO();
		guiaDespachoExtrasDTOTemp.setArticuloDTO(new ArticuloDTO());
		guiaDespachoExtrasDTOTemp.getId().setCodigoCompania(contDetalle);
		this.guiaDespachoExtrasDTOCols.add(guiaDespachoExtrasDTOTemp);
		contDetalle++;
	}
	
	/**
	 * Metodo para cargar 
	 * @return
	 */
	public void cargarDatosEliminar(ActionEvent e) {
		System.out.println("Ingreso a cargar detalle");
	}
	
	/**
	 * Metodo para validar si se se debe mostrar el popup de confirmacion 
	 * @return
	 */
	public void validarEstadoPedido(ActionEvent e) {
		this.sePuedeEliminar = Boolean.FALSE;
		if(this.guiaDespachoPedidoDTO.getId().getCodigoGuiaDespachoPedido() == null) {
			this.guiaDespachoPedidoDTOCols.remove(this.guiaDespachoPedidoDTO);
			quitarArticulosPedido(this.guiaDespachoPedidoDTO);
		}else {
			if(this.guiaDespachoPedidoDTO.getPedidoDTO().getEstadoPedidoDTO().getCodigoValorEstadoPedido().equals("ENT")) {
				this.setShowMessagesBar(Boolean.TRUE);
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.guia.despacho.mensaje.eliminar.entregado"));
			}else {
				this.sePuedeEliminar = Boolean.TRUE;
			}
		}
	}
	
	public void quitarArticulosPedido(GuiaDespachoPedidoDTO guiaDespachoPedidoDTO) {
		guiaDespachoPedidoDTO.getPedidoDTO().getDetallePedidoDTOCols().stream().forEach(detalle ->{
			GuiaDespachoDetalleDTO detalleGuia = this.guiaDespachoDetalleDTOCols.stream()
	        		.filter(guiaDetalle -> guiaDetalle.getCodigoArticulo().intValue() == detalle.getCodigoArticulo().intValue())
	        		.findFirst().orElse(null);
			if(detalleGuia != null) {
				this.guiaDespachoDetalleDTOCols.stream().forEach(guiaDesp -> {
					if(guiaDesp.getCodigoArticulo().intValue() == detalle.getCodigoArticulo().intValue()) {
						guiaDesp.setCantidad(guiaDesp.getCantidad()-detalle.getCantidad());
					}
				});
			}
		});
		this.guiaDespachoDetalleDTOCols = this.guiaDespachoDetalleDTOCols.stream().filter(detalle -> detalle.getCantidad().intValue() != 0)
				.collect(Collectors.toList());
	}
	
	/**
	 * Metodo para validar si se se debe mostrar el popup de confirmacion 
	 * @return
	 */
	public void validarExtras(ActionEvent e) {
		this.sePuedeEliminar = Boolean.FALSE;
		if(this.guiaDespachoExtrasDTO.getId().getCodigoGuiaDespachoExtra() == null) {
			this.guiaDespachoExtrasDTOCols.remove(this.guiaDespachoExtrasDTO);
		}else {
			this.sePuedeEliminar = Boolean.TRUE;
		}
	}
	
	/**
	 * Metodo para cargar 
	 * @return
	 */
	public void cargarGuiaImprimir(ActionEvent e) {
		System.out.println("Ingreso a guia para imprimir");
		this.setShowMessagesBar(Boolean.TRUE);
        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.despacho.mensaje.impresion.correcta"));
	}
	
	/**
	 * Metodo para consultar vehiculos por placa 
	 * @param e
	 */
	public void realizarConsultaVehiculoPlaca(AjaxBehaviorEvent e) {
		String placa = this.guiaDespachoDTO.getPlaca().toUpperCase();
		Collection<VehiculoDTO> vehiculoDTOCoslTemp = ERPFactory.vehiculo.getVehiculoServicio().findObtenerListaVehiculos(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), placa, null, null);
		if(CollectionUtils.isNotEmpty(vehiculoDTOCoslTemp)) {
			VehiculoDTO vehiculoDTOTemp = vehiculoDTOCoslTemp.iterator().next();
			if(vehiculoDTOTemp != null) {
				if(vehiculoDTOTemp.getPlaca() == null) {
					this.guiaDespachoDTO.setPlaca(placa);
				}
				else
				{
					this.guiaDespachoDTO.setMarca(vehiculoDTOTemp.getMarca());
				
					if(vehiculoDTOTemp.getTransportistaDTO() != null) {
						this.guiaDespachoDTO.setRucTransportista(vehiculoDTOTemp.getTransportistaDTO().getPersonaDTO() == null ? vehiculoDTOTemp.getTransportistaDTO().getEmpresaDTO().getNumeroRuc() : vehiculoDTOTemp.getTransportistaDTO().getPersonaDTO().getNumeroDocumento());
						this.guiaDespachoDTO.setNombreTransportista(vehiculoDTOTemp.getTransportistaDTO().getPersonaDTO() == null ? vehiculoDTOTemp.getTransportistaDTO().getEmpresaDTO().getRazonSocial() : vehiculoDTOTemp.getTransportistaDTO().getPersonaDTO().getNombreCompleto());
					}
					if(CollectionUtils.isNotEmpty(vehiculoDTOTemp.getVehiculoChoferDTOCols())) {
						for (VehiculoChoferDTO vehiculoChoferDTO : vehiculoDTOTemp.getVehiculoChoferDTOCols()) {
							if(vehiculoChoferDTO.getCodigoValorTipoChofer().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CHOFER_PRINCIPAL)) {
								this.guiaDespachoDTO.setDocumentoChoferA(vehiculoChoferDTO.getChoferDTO().getPersonaDTO().getNumeroDocumento());
								this.guiaDespachoDTO.setNombreChoferA(vehiculoChoferDTO.getChoferDTO().getPersonaDTO().getNombreCompleto());
							}
							if(vehiculoChoferDTO.getCodigoValorTipoChofer().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_CHOFER_AYUDANTE)) {
								this.guiaDespachoDTO.setDocumentoChoferB(vehiculoChoferDTO.getChoferDTO().getPersonaDTO().getNumeroDocumento());
								this.guiaDespachoDTO.setNombreChoferB(vehiculoChoferDTO.getChoferDTO().getPersonaDTO().getNombreCompleto());
							}
						}
					}
				}
			}
		}
		else {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.guia.despacho.mensaje.vehiculo"));
		}
	}
	
	/**
	 * Metodo para consultar transportista por numero documento 
	 * @param e
	 */
	public void realizarConsultaTransportista(AjaxBehaviorEvent e) {
		Collection<TransportistaDTO> transportistaDTOCoslTemp = ERPFactory.transportista.getTransportistasServicio().findObtenerListaTransportistas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.guiaDespachoDTO.getRucTransportista(), null);
		if(CollectionUtils.isNotEmpty(transportistaDTOCoslTemp)) {
			TransportistaDTO transportistaDTO = transportistaDTOCoslTemp.iterator().next();
			this.guiaDespachoDTO.setRucTransportista(transportistaDTO.getPersonaDTO() == null ? transportistaDTO.getEmpresaDTO().getNumeroRuc() : transportistaDTO.getPersonaDTO().getNumeroDocumento());
			this.guiaDespachoDTO.setNombreTransportista(transportistaDTO.getPersonaDTO() == null ? transportistaDTO.getEmpresaDTO().getRazonSocial() : transportistaDTO.getPersonaDTO().getNombreCompleto());
		}
		else {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.guia.despacho.mensaje.transportista"));
		}
	}
	
	/**
	 * Metodo para buscar guias
	 * @param e
	 */
	public void busquedaGuiasDespachos(ActionEvent e){
		this.buscarGuiasDespachos();
	}
	
	/**
	 * Metodo para buscar guias al dar enter
	 * @param e
	 */
	public void busquedaGuiasDespachosEnter(AjaxBehaviorEvent e){
		this.buscarGuiasDespachos();
	}
	
	/**
	 * Metodo para buscar guias por filtros o todos
	 * @param e
	 */
	public void buscarGuiasDespachos(){
		try {
			this.setShowMessagesBar(Boolean.FALSE);
			
			Calendar fechaInicio = Calendar.getInstance();
			Calendar fechaFin = Calendar.getInstance();
			fechaInicio.setTime(fechaDespachoBusqueda);
			fechaFin.setTime(fechaDespachoBusquedaFin);
			UtilitarioWeb.cleanDate(fechaInicio);
			
			// Busqueda por (Integer codigoCompania, String numeroGuia, Timestamp fechaDespacho, String placa, String documentoChofer, String nombreChofer)
			this.guiaDespachoDTOCols = ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaDespachosByFiltrosBusqueda(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroGuiaDespachoBusqueda, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()), placaBusqueda, null, nombreChoferBusqueda);
			if(CollectionUtils.isEmpty(this.guiaDespachoDTOCols)){
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
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		this.loginController.desActivarMenusSeleccionado();
		this.loginController.setActivarInicio(Boolean.TRUE);
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para guardar o actualizar despacho
	 * @param e
	 */
	public void guadarActualizarDespacho(ActionEvent e){
		try {
			this.setDespachoCreado(Boolean.FALSE);
			this.setShowMessagesBar(Boolean.FALSE);
			if(this.validarPantallaCompleta()) {
				guiaDespachoDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				guiaDespachoDTO.setUsuarioRegistro(this.loginController.getUsuariosDTO().getId().getUserId());
				guiaDespachoDTO.setGuiaDespachoPedidoDTOCols(guiaDespachoPedidoDTOCols);
				guiaDespachoDTO.setGuiaDespachoExtrasDTOCols(guiaDespachoExtrasDTOCols);
				guiaDespachoDTO.setGuiaDespachoDetalleDTOCols(guiaDespachoDetalleDTOCols);
				
				ERPFactory.despacho.getGuiaDespachoServicio().transCrearActualizarGuiaDespacho(guiaDespachoDTO);
				this.setDespachoCreado(Boolean.TRUE);
				this.setShowMessagesBar(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.guia.despacho.mensaje.correcto"));
			}else {
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
	public Boolean validarPantallaCompleta() {
		Boolean validado = Boolean.TRUE;
		if(StringUtils.isEmpty(guiaDespachoDTO.getPlaca())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.placa"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(guiaDespachoDTO.getRucTransportista())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.documento.transportista"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(guiaDespachoDTO.getNombreTransportista())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.nombre.transportista"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(guiaDespachoDTO.getDocumentoChoferA())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.principal.documento"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(guiaDespachoDTO.getNombreChoferA())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.principal.nombre"));
			validado = Boolean.FALSE;
		}
		if(CollectionUtils.isEmpty(this.guiaDespachoPedidoDTOCols) && CollectionUtils.isEmpty(this.guiaDespachoExtrasDTOCols)){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.lista.destinos.extras"));
			validado = Boolean.FALSE;
		}
		return validado;
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public void imprimirNotaDespacho() {
		HtmlPdf htmltoPDF;
		try {
			if(this.guiaDespachoDTO.getId().getCodigoGuiaDespacho() != null) {
				this.guiaDespachoDTO.setGuiaDespachoExtrasDTOCols(ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaGuiaDespachoExtrasByNumeroGuia(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.guiaDespachoDTO.getNumeroGuiaDespacho()));
				this.guiaDespachoDTO.setGuiaDespachoPedidoDTOCols(ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaGuiaDespachoPedidosByNumeroGuiaDespacho(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.guiaDespachoDTO.getNumeroGuiaDespacho()));
				this.guiaDespachoDTO.setGuiaDespachoDetalleDTOCols(ERPFactory.despacho.getGuiaDespachoServicio().findObtenerListaGuiaDespachoDetalleByNumeroGuia(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.guiaDespachoDTO.getNumeroGuiaDespacho()));
			}
			if(this.validarPantallaCompleta()) {
				// Plantilla rpincipal que permite la conversion de xsl a pdf
				htmltoPDF = new HtmlPdf(ERPConstantes.PLANTILLA_XSL_FOPRINCIPAL);
				HashMap<String , String> parametros = new HashMap<String, String>();
				byte contenido[] = htmltoPDF.convertir(ERPFactory.despacho.getGuiaDespachoServicio().finObtenerXMLImprimirGuiaDespacho(guiaDespachoDTO).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""), "", "",	parametros,	null);
				UtilitarioWeb.mostrarPDF(contenido);	
			}else {
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir");
		}
	}
	
	
	/**
	 * Metodo borrar pantalla y crear un despacho nuevo
	 * @param e
	 */
	public void clearNuevoDespacho(ActionEvent e){
		this.setDespachoCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.guiaDespachoDTO = new GuiaDespachoDTO();
		this.guiaDespachoDTO.setFechaDespacho(new Date());
		SecuenciaDTO secuenciaDespacho = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(GuiaDespachoID.NOMBRE_SECUENCIA);
		this.guiaDespachoDTO.setNumeroGuiaDespacho("GD-"+secuenciaDespacho.getValorSecuencia());
		this.guiaDespachoExtrasDTOCols = new ArrayList<GuiaDespachoExtrasDTO>();
		GuiaDespachoExtrasDTO guiaDespachoExtrasDTOTemp = null;
		for(int i = 0; i < 5 ; i++){
			guiaDespachoExtrasDTOTemp = new GuiaDespachoExtrasDTO();
			this.guiaDespachoExtrasDTOCols.add(guiaDespachoExtrasDTOTemp);
		}
		this.guiaDespachoPedidoDTOCols = new ArrayList<GuiaDespachoPedidoDTO>();
		this.guiaDespachoDetalleDTOCols = new ArrayList<>();
		this.despachoDataManager.setGuiaDespachoDTOEditar(new GuiaDespachoDTO());
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarGuiaEditar() {
		if(this.guiaDespachoDTO == null) {
			return null;
		}else{
			this.despachoDataManager.setGuiaDespachoDTOEditar(this.guiaDespachoDTO);
			return "/modules/despachos/nuevoDespacho.xhtml?faces-redirect=true";
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
	 * Metodo para regresar a la busqueda de despachos
	 * @param e
	 */
	public String regresarBusquedaDespachos(){
		this.despachoDataManager.setGuiaDespachoDTOEditar(new GuiaDespachoDTO());
		return "/modules/despachos/adminBusquedaDespacho.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo despacho
	 * @return
	 */
	public String crearNuevoDespacho(){
		this.despachoDataManager.setGuiaDespachoDTOEditar(new GuiaDespachoDTO());
		return "/modules/despachos/nuevoDespacho.xhtml?faces-redirect=true";
	}
	
	/**
	 * Borrar filtro de numero de documento
	 */
	public void borrarBusquedaGuiaDespacho(ActionEvent e){
		this.numeroGuiaDespachoBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de numero de documento
	 */
	public void borrarBusquedaPlaca(ActionEvent e){
		this.placaBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de busqueda fecha despacho
	 */
	public void borrarBusquedaFechaDespacho(ActionEvent e){
		this.fechaDespachoBusqueda = new Date();
		this.fechaDespachoBusquedaFin = new Date();
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de busqueda documento chofer
	 */
	public void borrarBusquedaDocumentoChofer(ActionEvent e){
		this.numeroDocumentoChoferBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de busqueda nombre chofer
	 */
	public void borrarBusquedaNombreChofer(ActionEvent e){
		this.nombreChoferBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	
	public void setDespachoDataManager(DespachoDataManager despachoDataManager) {
		this.despachoDataManager = despachoDataManager;
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

	public String getPlacaBusqueda() {
		return placaBusqueda;
	}

	public void setPlacaBusqueda(String placaBusqueda) {
		this.placaBusqueda = placaBusqueda;
	}

	public String getNumeroGuiaDespachoBusqueda() {
		return numeroGuiaDespachoBusqueda;
	}

	public void setNumeroGuiaDespachoBusqueda(String numeroGuiaDespachoBusqueda) {
		this.numeroGuiaDespachoBusqueda = numeroGuiaDespachoBusqueda;
	}

	public Date getFechaDespachoBusqueda() {
		return fechaDespachoBusqueda;
	}

	public void setFechaDespachoBusqueda(Date fechaDespachoBusqueda) {
		this.fechaDespachoBusqueda = fechaDespachoBusqueda;
	}

	public Collection<GuiaDespachoDTO> getGuiaDespachoDTOCols() {
		return guiaDespachoDTOCols;
	}

	public void setGuiaDespachoDTOCols(Collection<GuiaDespachoDTO> guiaDespachoDTOCols) {
		this.guiaDespachoDTOCols = guiaDespachoDTOCols;
	}

	public String getNumeroDocumentoChoferBusqueda() {
		return numeroDocumentoChoferBusqueda;
	}

	public void setNumeroDocumentoChoferBusqueda(String numeroDocumentoChoferBusqueda) {
		this.numeroDocumentoChoferBusqueda = numeroDocumentoChoferBusqueda;
	}

	public String getNombreChoferBusqueda() {
		return nombreChoferBusqueda;
	}

	public void setNombreChoferBusqueda(String nombreChoferBusqueda) {
		this.nombreChoferBusqueda = nombreChoferBusqueda;
	}

	public GuiaDespachoDTO getGuiaDespachoDTO() {
		return guiaDespachoDTO;
	}

	public void setGuiaDespachoDTO(GuiaDespachoDTO guiaDespachoDTO) {
		this.guiaDespachoDTO = guiaDespachoDTO;
	}

	public Collection<GuiaDespachoPedidoDTO> getGuiaDespachoPedidoDTOCols() {
		return guiaDespachoPedidoDTOCols;
	}

	public void setGuiaDespachoPedidoDTOCols(Collection<GuiaDespachoPedidoDTO> guiaDespachoPedidoDTOCols) {
		this.guiaDespachoPedidoDTOCols = guiaDespachoPedidoDTOCols;
	}

	public Collection<GuiaDespachoExtrasDTO> getGuiaDespachoExtrasDTOCols() {
		return guiaDespachoExtrasDTOCols;
	}

	public void setGuiaDespachoExtrasDTOCols(Collection<GuiaDespachoExtrasDTO> guiaDespachoExtrasDTOCols) {
		this.guiaDespachoExtrasDTOCols = guiaDespachoExtrasDTOCols;
	}

	public GuiaDespachoPedidoDTO getGuiaDespachoPedidoDTO() {
		return guiaDespachoPedidoDTO;
	}

	public void setGuiaDespachoPedidoDTO(GuiaDespachoPedidoDTO guiaDespachoPedidoDTO) {
		this.guiaDespachoPedidoDTO = guiaDespachoPedidoDTO;
	}

	public GuiaDespachoExtrasDTO getGuiaDespachoExtrasDTO() {
		return guiaDespachoExtrasDTO;
	}

	public void setGuiaDespachoExtrasDTO(GuiaDespachoExtrasDTO guiaDespachoExtrasDTO) {
		this.guiaDespachoExtrasDTO = guiaDespachoExtrasDTO;
	}

	public Collection<PedidoDTO> getPedidosDTOCols() {
		return pedidosDTOCols;
	}

	public void setPedidosDTOCols(Collection<PedidoDTO> pedidosDTOCols) {
		this.pedidosDTOCols = pedidosDTOCols;
	}

	public Long getCodigoPedidoSeleccionado() {
		return codigoPedidoSeleccionado;
	}

	public void setCodigoPedidoSeleccionado(Long codigoPedidoSeleccionado) {
		this.codigoPedidoSeleccionado = codigoPedidoSeleccionado;
	}

	public PedidoDTO getPedidoDTO() {
		return pedidoDTO;
	}

	public void setPedidoDTO(PedidoDTO pedidoDTO) {
		this.pedidoDTO = pedidoDTO;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	public Boolean getControlPopUp() {
		return controlPopUp;
	}

	public void setControlPopUp(Boolean controlPopUp) {
		this.controlPopUp = controlPopUp;
	}

	public Date getFechaDespachoBusquedaFin() {
		return fechaDespachoBusquedaFin;
	}

	public void setFechaDespachoBusquedaFin(Date fechaDespachoBusquedaFin) {
		this.fechaDespachoBusquedaFin = fechaDespachoBusquedaFin;
	}

	public Boolean getDespachoCreado() {
		return despachoCreado;
	}

	public void setDespachoCreado(Boolean despachoCreado) {
		this.despachoCreado = despachoCreado;
	}

	public Boolean getSePuedeEliminar() {
		return sePuedeEliminar;
	}

	public void setSePuedeEliminar(Boolean sePuedeEliminar) {
		this.sePuedeEliminar = sePuedeEliminar;
	}
	
	public ArticuloService getService() {
		return service;
	}

	public void setService(ArticuloService service) {
		this.service = service;
	}
	
	public Collection<ArticuloDTO> getArticuloDTOCols() {
		return articuloDTOCols;
	}

	public void setArticuloDTOCols(Collection<ArticuloDTO> articuloDTOCols) {
		this.articuloDTOCols = articuloDTOCols;
	}

	public Collection<GuiaDespachoDetalleDTO> getGuiaDespachoDetalleDTOCols() {
		return guiaDespachoDetalleDTOCols;
	}

	public void setGuiaDespachoDetalleDTOCols(Collection<GuiaDespachoDetalleDTO> guiaDespachoDetalleDTOCols) {
		this.guiaDespachoDetalleDTOCols = guiaDespachoDetalleDTOCols;
	}

	public GuiaDespachoDetalleDTO getGuiaDespachoDetalleDTO() {
		return guiaDespachoDetalleDTO;
	}

	public void setGuiaDespachoDetalleDTO(GuiaDespachoDetalleDTO guiaDespachoDetalleDTO) {
		this.guiaDespachoDetalleDTO = guiaDespachoDetalleDTO;
	}

	public Integer getContDetalle() {
		return contDetalle;
	}

	public void setContDetalle(Integer contDetalle) {
		this.contDetalle = contDetalle;
	}
}
