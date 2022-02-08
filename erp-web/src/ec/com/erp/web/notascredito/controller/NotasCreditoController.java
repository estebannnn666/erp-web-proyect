
package ec.com.erp.web.notascredito.controller;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.primefaces.event.SelectEvent;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.common.utils.ValidationUtils;
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.cliente.mdl.dto.ArticuloImpuestoDTO;
import ec.com.erp.cliente.mdl.dto.ArticuloUnidadManejoDTO;
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.ClienteDTO;
import ec.com.erp.cliente.mdl.dto.FacturaCabeceraDTO;
import ec.com.erp.cliente.mdl.dto.NotaCreditoDTO;
import ec.com.erp.cliente.mdl.dto.NotaCreditoDetalleDTO;
import ec.com.erp.cliente.mdl.dto.ParametroDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.id.NotaCreditoID;
import ec.com.erp.facturacion.electronica.ws.FacturaElectronicaUtil;
import ec.com.erp.facturacion.electronica.ws.NotaCreditoElectronicaUtil;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.notascredito.datamanager.NotasCreditoDataManager;
import ec.com.erp.web.pedidos.controller.ArticuloService;

/**
 * Controlador para administracion de notas de credito
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class NotasCreditoController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private NotaCreditoDTO notaCreditoDTO;
	private NotaCreditoDetalleDTO notaCreditoDetalleDTO;
	private ClienteDTO clienteDTO;
	private FacturaCabeceraDTO facturaCabeceraDTO;
	private Collection<NotaCreditoDetalleDTO> notaCreditoDetalleDTOCols;
	private Collection<NotaCreditoDTO> notaCreditoDTOCols;
	private Collection<ClienteDTO> clienteDTOCols;
	private Collection<FacturaCabeceraDTO> facturaCabeceraDTOCols;
	private Collection<ArticuloDTO> articuloDTOCols;
	private String[] pagadoBusqueda;
	private Boolean crearFacturaElectronica;
	
	// Data Managers
	@ManagedProperty(value="#{notasCreditoDataManager}")
	private NotasCreditoDataManager notasCreditoDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	@ManagedProperty("#{articuloService}")
	private ArticuloService service;
	
	// Variables
	private String numeroNotaCredito;
	private Date fechaFacturaInicio;
	private Date fechaFacturaFin;
	private String docClienteProveedor;
	private String nombClienteProveedor;
	private Boolean pagado;
	private Collection<CatalogoValorDTO> tipoFacturaCatalogoValorDTOCols;
	private Integer page;
	private Long codigoClienteSeleccionado;
	private Long codigoFacturaSeleccionada;
	private Integer contDetalle;
	private String codigoBarras;
	private Boolean documentoCreado;
	private Boolean crearNuevaFila;
	private BigDecimal totalCuenta;
	private BigDecimal totalSubTotal;
	private BigDecimal totalDescuento;
	private String documentoClienteBusqueda;
	private String nombreClienteBusqueda;
	
	private String numeroFactura;
	private Date fechaFacInicio;
	private Date fechaFacFin;
	private String docClienteFactura;
	private String nombClienteFactura;

	@PostConstruct
	public void postConstruct() {
		this.crearFacturaElectronica = Boolean.FALSE;
		this.totalCuenta = BigDecimal.ZERO;
		this.totalSubTotal = BigDecimal.ZERO;
		this.totalDescuento = BigDecimal.ZERO;
		this.loginController.activarMenusSeleccionado();
		this.documentoCreado = Boolean.FALSE;
		this.notaCreditoDTO = new NotaCreditoDTO();
		this.notaCreditoDTO.setFechaDocumento(new Date());
		this.notaCreditoDTO.setFechaEmisionFactura(new Date());
		this.notaCreditoDTO.setPagado(Boolean.FALSE);
		this.notaCreditoDTO.setTipoRuc(ERPConstantes.TIPO_RUC_UNO);
		this.notaCreditoDetalleDTO = new NotaCreditoDetalleDTO();
		this.notaCreditoDTOCols = new ArrayList<>();
		this.notaCreditoDetalleDTOCols = new ArrayList<>();
		this.clienteDTOCols =  new ArrayList<>();
		this.facturaCabeceraDTOCols = new ArrayList<>();
		this.clienteDTO = new ClienteDTO();
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		
		this.page = 0;
		this.contDetalle = 1;
		
		NotaCreditoDetalleDTO detalle = null;
		
		contDetalle = 1;
		for(int i=0; i< 10; i++) {
			detalle = new NotaCreditoDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.notaCreditoDetalleDTOCols.add(detalle);
			contDetalle++;
		}
		
		// Inicializar fechas para filtros de busqueda
		Calendar fechaInferior = Calendar.getInstance();
		fechaInferior.set(Calendar.MONTH, 0);
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		Calendar fechaSuperior = Calendar.getInstance();
		fechaFacInicio = fechaInferior.getTime();
		fechaFacturaFin = fechaSuperior.getTime();
		
		fechaFacturaInicio = fechaInferior.getTime();
		fechaFacFin = fechaSuperior.getTime();
		
		this.tipoFacturaCatalogoValorDTOCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_DOCUMENTOS);
		this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, null);
		
		// Cargar datos a editar
		if(notasCreditoDataManager.getNotaCreditoDTOEditar() != null && notasCreditoDataManager.getNotaCreditoDTOEditar().getId().getCodigoNotaCredito() != null){
			this.setNotaCreditoDTO(notasCreditoDataManager.getNotaCreditoDTOEditar());
			this.setNotaCreditoDetalleDTOCols(notasCreditoDataManager.getNotaCreditoDTOEditar().getNotaCreditoDetalleDTOCols());
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/notascredito/adminBusquedaNotasCredito.xhtml")) {
			this.notaCreditoDTOCols = ERPFactory.notascredito.getNotaCreditoServicio().findObtenerListaNotasCredito(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroNotaCredito, null, null, docClienteProveedor, nombClienteProveedor);
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/notascredito/nuevaNotaCredito.xhtml")) {
			Collection<String> codigosParametros = new ArrayList<>();
			codigosParametros.add(ERPConstantes.PARAMETRO_NOMBRE_ESTABLECIMIENTO);
			codigosParametros.add(ERPConstantes.PARAMETRO_NOMBRE_PUNTO_EMISION);
			Collection<ParametroDTO> parametrosFactura = ERPFactory.parametro.getParametroServicio().findObtenerParametrosByCodigos(codigosParametros);
			ParametroDTO parametroDTOEsta = parametrosFactura.stream().filter(param -> param.getId().getCodigoParametro().equals(ERPConstantes.PARAMETRO_NOMBRE_ESTABLECIMIENTO)).findFirst().orElse(null);
			ParametroDTO parametroDTOPunt = parametrosFactura.stream().filter(param -> param.getId().getCodigoParametro().equals(ERPConstantes.PARAMETRO_NOMBRE_PUNTO_EMISION)).findFirst().orElse(null);
			SecuenciaDTO secuenciaPedido = new SecuenciaDTO();
			secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(NotaCreditoID.NTC_ELECTRONICA_RUC_UNO);
			String numeroFactura = ValidationUtils.obtenerSecuencialNotaCredito(9, parametroDTOEsta.getValorParametro(), parametroDTOPunt.getValorParametro(), String.valueOf(secuenciaPedido.getValorSecuencia()));
			this.notaCreditoDTO.setNumeroDocumento(numeroFactura);
		}
		
		if(CollectionUtils.isNotEmpty(this.notaCreditoDTOCols)) {
			this.notaCreditoDTOCols.stream().forEach(factura ->{
				this.totalCuenta =  this.totalCuenta.add(factura.getTotalCuenta());
				this.totalSubTotal = this.totalSubTotal.add(factura.getSubTotal());
				this.totalDescuento = this.totalDescuento.add(factura.getDescuento());
			});
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return notasCreditoDataManager;
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
	 * Metodo para obtener secuencial de facturas segun el RUC 
	 * @param e
	 */
	public void seleccionarTipoRuc(ValueChangeEvent e) {
		String tipoRuc = (String)e.getNewValue();
		Collection<String> codigosParametros = new ArrayList<>();
		codigosParametros.add(ERPConstantes.PARAMETRO_NOMBRE_ESTABLECIMIENTO);
		codigosParametros.add(ERPConstantes.PARAMETRO_NOMBRE_PUNTO_EMISION);
		Collection<ParametroDTO> parametrosFactura = ERPFactory.parametro.getParametroServicio().findObtenerParametrosByCodigos(codigosParametros);
		ParametroDTO parametroDTOEsta = parametrosFactura.stream().filter(param -> param.getId().getCodigoParametro().equals(ERPConstantes.PARAMETRO_NOMBRE_ESTABLECIMIENTO)).findFirst().orElse(null);
		ParametroDTO parametroDTOPunt = parametrosFactura.stream().filter(param -> param.getId().getCodigoParametro().equals(ERPConstantes.PARAMETRO_NOMBRE_PUNTO_EMISION)).findFirst().orElse(null);
		SecuenciaDTO secuenciaPedido = new SecuenciaDTO();
		if(tipoRuc.equals(ERPConstantes.TIPO_RUC_DOS)) {
			secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(NotaCreditoID.NTC_ELECTRONICA_RUC_DOS);
		}else {
			secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(NotaCreditoID.NTC_ELECTRONICA_RUC_UNO);
		}
		String numeroFactura = ValidationUtils.obtenerSecuencialNotaCredito(9, parametroDTOEsta.getValorParametro(), parametroDTOPunt.getValorParametro(), String.valueOf(secuenciaPedido.getValorSecuencia()));
		this.notaCreditoDTO.setNumeroDocumento(numeroFactura);
	}
	
	public void consultarFacturas(ActionEvent e) {
		this.notaCreditoDTOCols = ERPFactory.notascredito.getNotaCreditoServicio().findObtenerListaNotasCredito(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroNotaCredito, null, null, docClienteProveedor, nombClienteProveedor);
		if(CollectionUtils.isNotEmpty(this.notaCreditoDTOCols)) {
			this.totalCuenta = BigDecimal.ZERO;
			this.totalSubTotal = BigDecimal.ZERO;
			this.totalDescuento = BigDecimal.ZERO;
			this.notaCreditoDTOCols.stream().forEach(factura ->{
				this.totalCuenta =  this.totalCuenta.add(factura.getTotalCuenta());
				this.totalSubTotal = this.totalSubTotal.add(factura.getSubTotal());
				this.totalDescuento = this.totalDescuento.add(factura.getDescuento());
			});
		}
	}
	
	/**
	 * Metodo para buscar articulos
	 * @param query
	 * @return
	 */
	public List<String> completeNombreArticulo(String query) {
        String queryLowerCase = query.toLowerCase();
        List<ArticuloDTO> allThemes = this.articuloDTOCols.stream()
        		.filter(t -> t.getNombreArticulo().toLowerCase().contains(queryLowerCase))
        		.collect(Collectors.toList());
        List<String> results = new ArrayList<>();
        allThemes.stream().forEach(articulo -> results.add(articulo.getNombreArticulo()));
        return results;
    }
	
	public void onItemSelect(SelectEvent event) {
        System.out.println(event.getObject());
        this.setShowMessagesBar(Boolean.FALSE);
        this.crearNuevaFila = Boolean.TRUE;
        for(NotaCreditoDetalleDTO notaCreditoDetalleDTOTemp : notaCreditoDetalleDTOCols) {
        	if(notaCreditoDetalleDTOTemp.getDescripcion() != null) {
        		String queryLowerCase = notaCreditoDetalleDTOTemp.getDescripcion().toLowerCase();
        		ArticuloDTO articuloSeleccionado = this.articuloDTOCols.stream()
                		.filter(articulo -> articulo.getNombreArticulo().toLowerCase().equals(queryLowerCase))
                		.findFirst().orElse(null);
        		notaCreditoDetalleDTOTemp.setArticuloDTO(articuloSeleccionado);
        	}
        	
			if((notaCreditoDetalleDTOTemp.getCantidad() == null ||  notaCreditoDetalleDTOTemp.getCantidad().intValue() == 0) && notaCreditoDetalleDTOTemp.getArticuloDTO() != null && notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecio() != null){
				notaCreditoDetalleDTOTemp.setCantidad(1);
			}
			if(notaCreditoDetalleDTOTemp.getCantidad() != null && notaCreditoDetalleDTOTemp.getArticuloDTO() != null && notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecio() != null && notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecioMinorista() != null && (notaCreditoDetalleDTOTemp.getCodigoArticulo() == null || notaCreditoDetalleDTOTemp.getCodigoArticulo() != notaCreditoDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo()) && CollectionUtils.isNotEmpty(notaCreditoDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
				ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(notaCreditoDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
				notaCreditoDetalleDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
				if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
					notaCreditoDetalleDTOTemp.setValorUnidad(notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecioMinorista());
				}else {
					notaCreditoDetalleDTOTemp.setValorUnidad(notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecio());
				}
				BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+(notaCreditoDetalleDTOTemp.getCantidad().intValue() * articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(notaCreditoDetalleDTOTemp.getValorUnidad());
				notaCreditoDetalleDTOTemp.setSubTotal(subTotal);
				notaCreditoDetalleDTOTemp.setCodigoArticulo(notaCreditoDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
				notaCreditoDetalleDTOTemp.setCodigoBarras(notaCreditoDetalleDTOTemp.getArticuloDTO().getCodigoBarras());
				notaCreditoDetalleDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
			}
		}
		this.calcularTotalFactura();
		// Validar filas llenas
		notaCreditoDetalleDTOCols.forEach(detail ->{
			if(detail.getCodigoArticulo() == null) {
				this.crearNuevaFila = Boolean.FALSE;
			}
		});
		if(notaCreditoDetalleDTOCols.size() >= 20) {
			this.crearNuevaFila = Boolean.FALSE;
		}
		if(this.crearNuevaFila) {
			NotaCreditoDetalleDTO detalle = new NotaCreditoDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.notaCreditoDetalleDTOCols.add(detalle);
			contDetalle++;
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
		
		for(NotaCreditoDetalleDTO notaCreditoDetalleDTOTemp : notaCreditoDetalleDTOCols) {
			if(notaCreditoDetalleDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(notaCreditoDetalleDTOTemp.getCantidad() != null && notaCreditoDetalleDTOTemp.getValorUnidad() != null && notaCreditoDetalleDTOTemp.getCodigoArticuloUnidadManejo() != null) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = obtenerUnidadManejoPorCodigo(codigoUnidadManejo, notaCreditoDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(notaCreditoDetalleDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(notaCreditoDetalleDTOTemp.getValorUnidad()), 4);
					notaCreditoDetalleDTOTemp.setSubTotal(subTotal);
					notaCreditoDetalleDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
					this.calcularTotalFactura();
				}
				break;
			}
		}
	}

	/**
	 * Metodo para buscar facturas en venta
	 * @param e
	 */
	public void busquedaNotasCredito(ActionEvent e){
		this.buscarNotasCredito();
	}
	
	/**
	 * Metodo para buscar facturas en venta por filtros de busqueda al dar enter
	 * @param e
	 */
	public void busquedaNotasCreditoEnter(AjaxBehaviorEvent e){
		this.buscarNotasCredito();
	}
	
	/**
	 * Metodo para buscar cuentas o facturas por filtros de busqueda
	 * @param e
	 */
	public void buscarNotasCredito(){
		try {
			Calendar fechaInicio = Calendar.getInstance();
			Calendar fechaFin = Calendar.getInstance();
			fechaInicio.setTime(fechaFacturaInicio);
			fechaFin.setTime(fechaFacturaFin);
			UtilitarioWeb.cleanDate(fechaInicio);
			UtilitarioWeb.cleanDate(fechaFin);
			fechaFin.add(Calendar.DATE, 1);
			if(this.pagadoBusqueda != null && this.pagadoBusqueda.length > 0){
				if(this.pagadoBusqueda.length == 2) {
					this.pagado = null; 
				}else {
					if(this.pagadoBusqueda[0].equals(ERPConstantes.ESTADO_ACTIVO_NUMERICO)) {
						this.pagado = Boolean.TRUE;
					}else {
						this.pagado = Boolean.FALSE;
					}
				}
			}else {
				this.pagado = null; 
			}
			
			this.notaCreditoDTOCols = ERPFactory.notascredito.getNotaCreditoServicio().findObtenerListaNotasCredito(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroNotaCredito, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()), docClienteProveedor, nombClienteProveedor);
			if(CollectionUtils.isEmpty(this.notaCreditoDTOCols)){
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("No se encontraron resultados para la b\u00FAsqueda realizada.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}else {
				this.setShowMessagesBar(Boolean.FALSE);
				this.totalCuenta = BigDecimal.ZERO;
				this.totalSubTotal = BigDecimal.ZERO;
				this.totalDescuento = BigDecimal.ZERO;
				this.notaCreditoDTOCols.stream().forEach(factura ->{
					this.totalCuenta =  this.totalCuenta.add(factura.getTotalCuenta());
					this.totalSubTotal = this.totalSubTotal.add(factura.getSubTotal());
					this.totalDescuento = this.totalDescuento.add(factura.getDescuento());
				});
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
	
	/***************************
	 * Metodos privados 
	 ***************************/
	/**
	 * Metodo para guardar o actualizar factura
	 * @param e
	 */
	public void guadarActualizarNotasCredito(ActionEvent e){
		try {
			this.setDocumentoCreado(Boolean.FALSE);
			if(this.validarInformacionRequerida()) {
				
				this.notaCreditoDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.notaCreditoDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.notaCreditoDetalleDTOCols.stream().forEach(detail ->{
					if(detail.getCantidad() != null && detail.getCodigoArticulo() != null) {
						detail.setDescripcion(detail.getArticuloDTO().getNombreArticulo());
					}
				});
				
				this.notaCreditoDTO.setNotaCreditoDetalleDTOCols(notaCreditoDetalleDTOCols);
				
				ERPFactory.notascredito.getNotaCreditoServicio().transGuardarActualizarNotaCredito(Boolean.FALSE, this.notaCreditoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setDocumentoCreado(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.cabecera.factura.mensaje.guardado")+""+this.crearFacturaElectronica);
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.notaCreditoDTO.getId().setCodigoNotaCredito(null);
			this.notaCreditoDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleNotaCredito(null));
			this.ordenarDetalles();
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.notaCreditoDTO.getId().setCodigoNotaCredito(null);
			this.notaCreditoDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleNotaCredito(null));
			this.ordenarDetalles();
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/***************************
	 * Metodos privados 
	 ***************************/
	/**
	 * Metodo para guardar o actualizar factura
	 * @param e
	 */
	public void guadarFirmarEnviarNotaCredito(ActionEvent e){
		try {
			this.setDocumentoCreado(Boolean.FALSE);
			if(this.validarInformacionRequerida()) {
				this.notaCreditoDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.notaCreditoDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.notaCreditoDetalleDTOCols.stream().forEach(detail ->{
					if(detail.getCantidad() != null && detail.getCodigoArticulo() != null) {
						detail.setDescripcion(detail.getArticuloDTO().getNombreArticulo());
					}
				});
				
				this.notaCreditoDTO.setNotaCreditoDetalleDTOCols(notaCreditoDetalleDTOCols);
				
				ERPFactory.notascredito.getNotaCreditoServicio().transGuardarActualizarNotaCredito(Boolean.TRUE, this.notaCreditoDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setDocumentoCreado(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.cabecera.factura.mensaje.guardado")+""+this.crearFacturaElectronica);
			}
			else
			{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.notaCreditoDTO.getId().setCodigoNotaCredito(null);
			this.notaCreditoDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleNotaCredito(null));
			this.ordenarDetalles();
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.notaCreditoDTO.getId().setCodigoNotaCredito(null);
			this.notaCreditoDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleNotaCredito(null));
			this.ordenarDetalles();
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	public void ordenarDetalles() {
		int contador = 1;
		for(NotaCreditoDetalleDTO detalleAux : this.notaCreditoDetalleDTOCols) {
			detalleAux.getId().setCodigoCompania(contador);
			contador++;
		}
	}
	
	/**
	 * Metodo para validar la informacion requerida de la pantalla
	 */
	private Boolean validarInformacionRequerida() {
		Boolean valido = Boolean.TRUE;
		if(StringUtils.isEmpty(this.notaCreditoDTO.getRucCliente())) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.ruccliente.factura"));
		}
		if(StringUtils.isEmpty(this.notaCreditoDTO.getRazonSocial())) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.nombrecliente.factura"));
		}
		
		if(StringUtils.isEmpty(this.notaCreditoDTO.getMotivo())) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.nota.credito.campo.requerido.motivo"));
		}
		
		Boolean existDetail = Boolean.TRUE;
		for(NotaCreditoDetalleDTO detalle : this.getNotaCreditoDetalleDTOCols()){
			if(detalle.getCantidad() != null && detalle.getValorUnidad() != null && detalle.getCodigoArticulo() != null) {
				existDetail = Boolean.FALSE;
				break;
			}
		}
		
		if(CollectionUtils.isNotEmpty(this.notaCreditoDTO.getNotaCreditoDetalleDTOCols()) && this.notaCreditoDTO.getId().getCodigoNotaCredito() != null && existDetail) {
			this.setNotaCreditoDetalleDTOCols(this.notaCreditoDTO.getNotaCreditoDetalleDTOCols());
		}
		
		if(CollectionUtils.isEmpty(this.notaCreditoDetalleDTOCols)) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.factura"));
		}else{
			Boolean ban = Boolean.FALSE;
			for(NotaCreditoDetalleDTO notaCreditoDetalleDTOAux : notaCreditoDetalleDTOCols) {
				if(notaCreditoDetalleDTOAux.getCantidad() != null && notaCreditoDetalleDTOAux.getDescripcion() != null && notaCreditoDetalleDTOAux.getValorUnidad() != null) {
					ban = Boolean.TRUE;
					break;
				}
			}
			if(!ban) {
				valido = Boolean.FALSE;
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.factura"));
			}
		}
		
		return valido;
	}
	
	/**
	 * Metodo para cargar datos detalle nota de credito
	 * @return
	 */
	public void cargarNotaCreditoDetalle(ActionEvent e) {
		System.out.println("Ingreso a imprimir factura");
		this.setShowMessagesBar(Boolean.TRUE);
        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.despacho.mensaje.impresion.correcta"));
	}
	
	/**
	 * Metodo para firmar enviar y autorizar facturas
	 * @param e
	 */
	public void enviarFirmarAutorizar(ActionEvent e){
		try {
			byte[] xmlDocument = ERPFactory.notascredito.getNotaCreditoServicio().findObtenerXmlDocumentoNotaCredito(ERPConstantes.CODIGO_COMPANIA, this.notaCreditoDTO.getId().getCodigoNotaCredito());
			if(xmlDocument == null){
				ERPFactory.notascredito.getNotaCreditoServicio().transEnviarFirmarAutorizarNotaCredito(this.notaCreditoDTO);
			    MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.nota.credito.electrinoca"));
			    // Enviar notificacion por mail
			    if(StringUtils.isNotBlank(this.facturaCabeceraDTO.getEmail())){
			    	byte[] factura = FacturaElectronicaUtil.imprimirRideFactura(xmlDocument);
					ERPFactory.notificacion.getNotificacionMailServicio().findEnviarFacturaMail(this.facturaCabeceraDTO.getEmail(), factura);
			    }
			    this.notaCreditoDTOCols = ERPFactory.notascredito.getNotaCreditoServicio().findObtenerListaNotasCredito(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroNotaCredito, null, null, docClienteProveedor, nombClienteProveedor);
			}else{
		        MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.nota.credito.creada"));
			}
			this.setShowMessagesBar(Boolean.TRUE);
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Metodo para cargar datos detalle nota de credito
	 * @return
	 */
	public void simularEvento(ActionEvent e) {
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	
	/**
	 * Metodo borrar pantalla y crear una nueva nota de credito
	 * @param e
	 */
	public void clearNuevaNotaCredito(ActionEvent e){
		this.setShowMessagesBar(Boolean.FALSE);
		this.setDocumentoCreado(Boolean.FALSE);
		this.clienteDTO = new ClienteDTO();
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		this.codigoClienteSeleccionado = null;
		this.codigoFacturaSeleccionada = null;
		this.documentoClienteBusqueda = null;
		this.nombreClienteBusqueda = null;
		this.notaCreditoDTO = new NotaCreditoDTO();
		Collection<String> codigosParametros = new ArrayList<>();
		codigosParametros.add(ERPConstantes.PARAMETRO_NOMBRE_ESTABLECIMIENTO);
		codigosParametros.add(ERPConstantes.PARAMETRO_NOMBRE_PUNTO_EMISION);
		Collection<ParametroDTO> parametrosFactura = ERPFactory.parametro.getParametroServicio().findObtenerParametrosByCodigos(codigosParametros);
		ParametroDTO parametroDTOEsta = parametrosFactura.stream().filter(param -> param.getId().getCodigoParametro().equals(ERPConstantes.PARAMETRO_NOMBRE_ESTABLECIMIENTO)).findFirst().orElse(null);
		ParametroDTO parametroDTOPunt = parametrosFactura.stream().filter(param -> param.getId().getCodigoParametro().equals(ERPConstantes.PARAMETRO_NOMBRE_PUNTO_EMISION)).findFirst().orElse(null);
		SecuenciaDTO secuenciaPedido = new SecuenciaDTO();
		secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(NotaCreditoID.NTC_ELECTRONICA_RUC_UNO);
		String numeroFactura = ValidationUtils.obtenerSecuencialNotaCredito(9, parametroDTOEsta.getValorParametro(), parametroDTOPunt.getValorParametro(), String.valueOf(secuenciaPedido.getValorSecuencia()));
		this.notaCreditoDTO.setNumeroDocumento(numeroFactura);
		this.notaCreditoDetalleDTO = new NotaCreditoDetalleDTO();
		this.notasCreditoDataManager.setNotaCreditoDTOEditar(new NotaCreditoDTO());
		this.notaCreditoDetalleDTOCols = new ArrayList<>();
		this.notaCreditoDTO.setTipoRuc(ERPConstantes.TIPO_RUC_UNO);
		this.notaCreditoDTO.setFechaDocumento(new Date());
		this.notaCreditoDTO.setFechaEmisionFactura(new Date());
		this.notaCreditoDTO.setPagado(Boolean.FALSE);
		NotaCreditoDetalleDTO detalle = null;
		contDetalle = 1;
		for(int i=0; i< 10; i++) {
			detalle = new NotaCreditoDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.notaCreditoDetalleDTOCols.add(detalle);
			contDetalle++;
		}
	}
	
	/**
	 * Metodo para eliminar registro 
	 * @param notaCreditoDetalleDTO
	 */
	public void eliminarDetalleNotaCredito(NotaCreditoDetalleDTO notaCreditoDetalleDTO) {
		notaCreditoDetalleDTOCols.remove(notaCreditoDetalleDTO);
		contDetalle = 1;
		notaCreditoDetalleDTOCols.stream().forEach(detail ->{
			detail.getId().setCodigoCompania(contDetalle);
			contDetalle++;
		});
		
		NotaCreditoDetalleDTO detalle = null;
		while(contDetalle < 11) {
			detalle = new NotaCreditoDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.notaCreditoDetalleDTOCols.add(detalle);
			contDetalle++;
		}
		this.calcularTotalFactura();
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para calcular el total de la nota credito
	 */
	public void calcularTotalFactura() {
		this.notaCreditoDTO.setTotalCuenta(BigDecimal.ZERO);
		BigDecimal subTotal = BigDecimal.ZERO;
		BigDecimal totalIva = BigDecimal.ZERO;
		BigDecimal totalSinImpuesto = BigDecimal.ZERO;
		BigDecimal totalConImpuesto = BigDecimal.ZERO;
		BigDecimal totalFactura = BigDecimal.ZERO;
		BigDecimal totalDescuentos = BigDecimal.ZERO;
		for (NotaCreditoDetalleDTO notaCreditoDetalleDTO : notaCreditoDetalleDTOCols) {
			if(notaCreditoDetalleDTO.getSubTotal() != null) {
				subTotal = ValidationUtils.redondear(subTotal.add(notaCreditoDetalleDTO.getSubTotal()), 4);
			}
			if(notaCreditoDetalleDTO.getArticuloDTO() != null && notaCreditoDetalleDTO.getArticuloDTO().getTieneImpuesto()){
				totalConImpuesto = ValidationUtils.redondear(totalConImpuesto.add(notaCreditoDetalleDTO.getSubTotal()), 4);
				for(ArticuloImpuestoDTO impuesto : notaCreditoDetalleDTO.getArticuloDTO().getArticuloImpuestoDTOCols()){
					totalIva = ValidationUtils.redondear(totalIva.add(BigDecimal.valueOf((notaCreditoDetalleDTO.getSubTotal().doubleValue() * impuesto.getImpuestoDTO().getValorImpuesto().doubleValue())/ Double.valueOf(100))), 4);
				}
			}else {
				if(notaCreditoDetalleDTO.getSubTotal() != null) {
					totalSinImpuesto = ValidationUtils.redondear(totalSinImpuesto.add(notaCreditoDetalleDTO.getSubTotal()), 4);
				}
			}
			if(notaCreditoDetalleDTO.getCantidad() != null && notaCreditoDetalleDTO.getValorUnidad() != null && notaCreditoDetalleDTO.getArticuloDTO() != null && notaCreditoDetalleDTO.getArticuloDTO().getPrecio() != null && notaCreditoDetalleDTO.getArticuloDTO().getPrecioMinorista() != null) {
				if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)  && notaCreditoDetalleDTO.getValorUnidad().doubleValue() < notaCreditoDetalleDTO.getArticuloDTO().getPrecioMinorista().doubleValue()) {
					totalDescuentos = ValidationUtils.redondear(totalDescuentos.add(BigDecimal.valueOf(notaCreditoDetalleDTO.getArticuloDTO().getPrecioMinorista().subtract(notaCreditoDetalleDTO.getValorUnidad()).doubleValue()*notaCreditoDetalleDTO.getCantidad())), 4);
					notaCreditoDetalleDTO.setDescuento(ValidationUtils.redondear(BigDecimal.valueOf(notaCreditoDetalleDTO.getArticuloDTO().getPrecioMinorista().subtract(notaCreditoDetalleDTO.getValorUnidad()).doubleValue()*notaCreditoDetalleDTO.getCantidad()), 4));
				}
				if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MAYORISTA)  && notaCreditoDetalleDTO.getValorUnidad().doubleValue() < notaCreditoDetalleDTO.getArticuloDTO().getPrecio().doubleValue()) {
					totalDescuentos = ValidationUtils.redondear(totalDescuentos.add(BigDecimal.valueOf(notaCreditoDetalleDTO.getArticuloDTO().getPrecio().subtract(notaCreditoDetalleDTO.getValorUnidad()).doubleValue()*notaCreditoDetalleDTO.getCantidad())), 4);
					notaCreditoDetalleDTO.setDescuento(ValidationUtils.redondear(BigDecimal.valueOf(notaCreditoDetalleDTO.getArticuloDTO().getPrecio().subtract(notaCreditoDetalleDTO.getValorUnidad()).doubleValue()*notaCreditoDetalleDTO.getCantidad()), 4));
				}
			}
		}
		totalFactura = ValidationUtils.redondear(subTotal.add(totalIva),4);
		this.notaCreditoDTO.setDescuento(totalDescuentos);
		this.notaCreditoDTO.setTotalSinImpuestos(totalSinImpuesto);
		this.notaCreditoDTO.setTotalImpuestos(totalConImpuesto);
		this.notaCreditoDTO.setSubTotal(subTotal);
		this.notaCreditoDTO.setTotalIva(totalIva);
		this.notaCreditoDTO.setTotalCuenta(totalFactura);
	}
	
	/**
	 * Calcular el total y subtotal
	 * @param e
	 */
	public void calcularTotalFacturaKeyUp(AjaxBehaviorEvent e) {
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto =  idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2])+1;

		for(NotaCreditoDetalleDTO notaCreditoDetalleDTOTemp : notaCreditoDetalleDTOCols) {
			if(notaCreditoDetalleDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(notaCreditoDetalleDTOTemp.getCantidad() != null && notaCreditoDetalleDTOTemp.getValorUnidad() != null && notaCreditoDetalleDTOTemp.getCodigoArticuloUnidadManejo() != null) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = obtenerUnidadManejoPorCodigo(notaCreditoDetalleDTOTemp.getCodigoArticuloUnidadManejo(), notaCreditoDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(notaCreditoDetalleDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(notaCreditoDetalleDTOTemp.getValorUnidad()), 4);
					notaCreditoDetalleDTOTemp.setSubTotal(subTotal);
					notaCreditoDetalleDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
					this.calcularTotalFactura();
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
	
	public void obtenerCodigoBarrasEnter(AjaxBehaviorEvent e) {
		
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto =  idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2])+1;
		this.crearNuevaFila = Boolean.TRUE;

		for(NotaCreditoDetalleDTO notaCreditoDetalleDTOTemp : notaCreditoDetalleDTOCols) {
			if(notaCreditoDetalleDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				
				Collection<ArticuloDTO> articuloCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, notaCreditoDetalleDTOTemp.getCodigoBarras(), null);
				if(articuloCols.isEmpty()){
					this.setShowMessagesBar(Boolean.TRUE);
			        MensajesController.addInfo(null, "No existe articulo con el codigo de barras ingresado.");
				}else{
					notaCreditoDetalleDTOTemp.setArticuloDTO(articuloCols.iterator().next());
					if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
						notaCreditoDetalleDTOTemp.setValorUnidad(notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecioMinorista());
					}else {
						notaCreditoDetalleDTOTemp.setValorUnidad(notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecio());
					}
					notaCreditoDetalleDTOTemp.setDescripcion(notaCreditoDetalleDTOTemp.getArticuloDTO().getNombreArticulo());
					notaCreditoDetalleDTOTemp.setCodigoArticulo(notaCreditoDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
					if(notaCreditoDetalleDTOTemp.getCantidad() == null || notaCreditoDetalleDTOTemp.getCantidad().intValue() == 0){
						notaCreditoDetalleDTOTemp.setCantidad(1);
					}
					this.setShowMessagesBar(Boolean.FALSE);
				}
				
				if(notaCreditoDetalleDTOTemp.getCantidad() != null && notaCreditoDetalleDTOTemp.getValorUnidad() != null && CollectionUtils.isNotEmpty(notaCreditoDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(notaCreditoDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					notaCreditoDetalleDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
					if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
						notaCreditoDetalleDTOTemp.setValorUnidad(notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecioMinorista());
					}else {
						notaCreditoDetalleDTOTemp.setValorUnidad(notaCreditoDetalleDTOTemp.getArticuloDTO().getPrecio());
					}
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(notaCreditoDetalleDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(notaCreditoDetalleDTOTemp.getValorUnidad()), 4);
					notaCreditoDetalleDTOTemp.setSubTotal(subTotal);
					this.calcularTotalFactura();
				}
				break;
			}
		}
		// Validar filas llenas
		notaCreditoDetalleDTOCols.forEach(detail ->{
			if(detail.getCodigoArticulo() == null) {
				this.crearNuevaFila = Boolean.FALSE;
			}
		});		
		if(notaCreditoDetalleDTOCols.size() >= 20) {
			this.crearNuevaFila = Boolean.FALSE;
		}
		if(this.crearNuevaFila) {
			NotaCreditoDetalleDTO detalle = new NotaCreditoDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.notaCreditoDetalleDTOCols.add(detalle);
			contDetalle++;
		}
	}
	
	
	public void procesarDetalle(ActionEvent e) {
		System.out.println("Prueba:"+notaCreditoDetalleDTOCols.size());
	}
	
	/**
	 * Calcular el subtotal
	 * @param e
	 */
	public void calcularSubTotalNotaCreditoKeyUp(AjaxBehaviorEvent e) {
		if(this.notaCreditoDetalleDTO.getCantidad() != null && this.notaCreditoDetalleDTO.getValorUnidad() != null) {
			BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+notaCreditoDetalleDTO.getCantidad())).multiply(notaCreditoDetalleDTO.getValorUnidad()), 4);
			this.notaCreditoDetalleDTO.setSubTotal(subTotal);			
		}
	}
	
	/**
	 * Metodo para buscar clientes
	 * @param e
	 */
	public void busquedaClientes(ActionEvent e){
		this.buscarClientes();
	}
	
	/**
	 * Metodo para buscar clientes al dar enter
	 * @param e
	 */
	public void busquedaClientesEnter(AjaxBehaviorEvent e){
		this.buscarClientes();
	}
	
	/**
	 * Metodo para buscar clientes
	 * @param e
	 */
	public void buscarClientes(){
		try {
			this.clienteDTOCols = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.documentoClienteBusqueda, this.nombreClienteBusqueda, null);
			this.documentoClienteBusqueda = null;
			this.nombreClienteBusqueda = null;
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
	 * Seleccionar cliente del popUp
	 * @param e
	 */
	public void seleccionCliente(ValueChangeEvent e){
		this.codigoClienteSeleccionado = (Long)e.getNewValue();
	}
	
	/**
	 * Metodo para agragar el cliente a la vista
	 */
	public void agragarCliente(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoCliente", PredicateUtils.equalPredicate(this.codigoClienteSeleccionado));
		// Validacion de objeto existente
		this.clienteDTO  = (ClienteDTO) CollectionUtils.find(this.clienteDTOCols, testPredicate);
		this.notaCreditoDTO.setRucCliente(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getNumeroRuc() : this.clienteDTO.getPersonaDTO().getNumeroDocumento());
		this.notaCreditoDTO.setRazonSocial(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getRazonSocial() : this.clienteDTO.getPersonaDTO().getNombreCompleto());
		this.notaCreditoDTO.setEmail(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getEmail() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getEmail());
		this.notaCreditoDTO.setTipoCliente(this.clienteDTO.getCodigoValorTipoCompra());
		if(CollectionUtils.isNotEmpty(this.notaCreditoDetalleDTOCols)) {
			this.notaCreditoDetalleDTOCols.stream().forEach(detalleFact ->{
				if(detalleFact.getCodigoArticulo() != null && detalleFact.getCodigoArticuloUnidadManejo() != null) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorCodigo(detalleFact.getCodigoArticuloUnidadManejo(), detalleFact.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
						detalleFact.setValorUnidad(detalleFact.getArticuloDTO().getPrecioMinorista());
					}else {
						detalleFact.setValorUnidad(detalleFact.getArticuloDTO().getPrecio());
					}
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(detalleFact.getCantidad().intValue() * articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(detalleFact.getValorUnidad()), 4);
					detalleFact.setSubTotal(subTotal);
				}
			});
			this.calcularTotalFactura();
		}
	}
	
	/**
	 * Metodo para consultar cliente por numero de documento 
	 * @param e
	 */
	public void realizarConsultaClientes(AjaxBehaviorEvent e) {
		if(StringUtils.isEmpty(this.notaCreditoDTO.getRucCliente())) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, "El campo no debe estar vacio, ingrese el documento del cliente que desea buscar.");
		}else {
			String numeroDocumento = this.notaCreditoDTO.getRucCliente();
			this.clienteDTO = ERPFactory.clientes.getClientesServicio().findObtenerClienteByCodigo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumento, null);
			if(this.clienteDTO != null) {
				this.notaCreditoDTO.setRucCliente(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getNumeroRuc() : this.clienteDTO.getPersonaDTO().getNumeroDocumento());
				this.notaCreditoDTO.setRazonSocial(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getRazonSocial() : this.clienteDTO.getPersonaDTO().getNombreCompleto());
				this.notaCreditoDTO.setEmail(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getEmail() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getEmail());
				this.notaCreditoDTO.setTipoCliente(this.clienteDTO.getCodigoValorTipoCompra());
				this.setShowMessagesBar(Boolean.FALSE);
				if(CollectionUtils.isNotEmpty(this.notaCreditoDetalleDTOCols)) {
					this.notaCreditoDetalleDTOCols.stream().forEach(detalleFact ->{
						if(detalleFact.getCodigoArticulo() != null && detalleFact.getCodigoArticuloUnidadManejo() != null) {
							ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorCodigo(detalleFact.getCodigoArticuloUnidadManejo(), detalleFact.getArticuloDTO().getArticuloUnidadManejoDTOCols());
							if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
								detalleFact.setValorUnidad(detalleFact.getArticuloDTO().getPrecioMinorista());
							}else {
								detalleFact.setValorUnidad(detalleFact.getArticuloDTO().getPrecio());
							}
							BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(detalleFact.getCantidad().intValue() * articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(detalleFact.getValorUnidad()), 4);
							detalleFact.setSubTotal(subTotal);
						}
					});
					this.calcularTotalFactura();
				}
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
		        MensajesController.addWarn(null, "No se encontr\u00F3 el cliente con el documento ingresado.");
			}
		}
	}
	
	/**
	 * Metodo para buscar clientes
	 * @param e
	 */
	public void busquedaFacturas(ActionEvent e){
		this.buscarFacturas();
	}
	
	/**
	 * Metodo para buscar clientes al dar enter
	 * @param e
	 */
	public void busquedaFacturasEnter(AjaxBehaviorEvent e){
		this.buscarFacturas();
	}
	
	/**
	 * Metodo para buscar clientes
	 * @param e
	 */
	public void buscarFacturas(){
		try {
//			Calendar fechaInicio = Calendar.getInstance();
//			Calendar fechaFin = Calendar.getInstance();
//			fechaInicio.setTime(fechaFacInicio);
//			fechaFin.setTime(fechaFacFin);
//			UtilitarioWeb.cleanDate(fechaInicio);
//			UtilitarioWeb.cleanDate(fechaFin);
//			fechaFin.add(Calendar.DATE, 1);
			
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturasElectronicas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteFactura, nombClienteFactura);
//			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturasElectronicas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()), docClienteFactura, nombClienteFactura);
			this.docClienteFactura = null;
			this.nombClienteFactura = null;
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
	 * Seleccionar cliente del popUp
	 * @param e
	 */
	public void seleccionFactura(ValueChangeEvent e){
		this.codigoFacturaSeleccionada = (Long)e.getNewValue();
	}
	
	/**
	 * Metodo para agragar el factura a la vista
	 */
	public void agragarFactura(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoFactura", PredicateUtils.equalPredicate(this.codigoFacturaSeleccionada));
		// Validacion de objeto existente
		this.facturaCabeceraDTO  = (FacturaCabeceraDTO) CollectionUtils.find(this.facturaCabeceraDTOCols, testPredicate);
		this.notaCreditoDTO.setNumeroComprobante(this.facturaCabeceraDTO.getNumeroDocumento());
		this.notaCreditoDTO.setFechaEmisionFactura(this.facturaCabeceraDTO.getFechaDocumento());
		this.notaCreditoDTO.setEmail(this.facturaCabeceraDTO.getEmail());
		this.notaCreditoDTO.setPagado(this.facturaCabeceraDTO.getPagado());
		this.notaCreditoDTO.setRucCliente(this.facturaCabeceraDTO.getRucDocumento());
		this.notaCreditoDTO.setRazonSocial(this.facturaCabeceraDTO.getNombreClienteProveedor());
		this.notaCreditoDTO.setTipoCliente(this.facturaCabeceraDTO.getTipoCliente());
		this.notaCreditoDTO.setTipoRuc(this.facturaCabeceraDTO.getTipoRuc());
		this.notaCreditoDTO.setCodigoFactura(this.facturaCabeceraDTO.getId().getCodigoFactura());
		this.notaCreditoDTO.setDescuento(this.facturaCabeceraDTO.getDescuento());
		this.notaCreditoDTO.setSubTotal(this.facturaCabeceraDTO.getSubTotal());
		this.notaCreditoDTO.setTotalCuenta(this.facturaCabeceraDTO.getTotalCuenta());
		this.notaCreditoDTO.setTotalImpuestos(this.facturaCabeceraDTO.getTotalImpuestos());
		this.notaCreditoDTO.setTotalSinImpuestos(this.facturaCabeceraDTO.getTotalSinImpuestos());
		this.notaCreditoDTO.setTotalIva(this.facturaCabeceraDTO.getTotalIva());
		
		this.notaCreditoDetalleDTOCols = new ArrayList<>();
		
		if(CollectionUtils.isNotEmpty(this.facturaCabeceraDTO.getFacturaDetalleDTOCols())) {
			this.facturaCabeceraDTO.getFacturaDetalleDTOCols().stream().forEach(detalleFact ->{
				NotaCreditoDetalleDTO notaCreditoDetalleDTOFact = new NotaCreditoDetalleDTO();
				notaCreditoDetalleDTOFact.getId().setCodigoCompania(detalleFact.getId().getCodigoCompania());
				notaCreditoDetalleDTOFact.setCodigoArticulo(detalleFact.getCodigoArticulo());
				notaCreditoDetalleDTOFact.setCodigoArticuloUnidadManejo(detalleFact.getCodigoArticuloUnidadManejo());
				notaCreditoDetalleDTOFact.setCantidad(detalleFact.getCantidad());
				notaCreditoDetalleDTOFact.setDescripcion(detalleFact.getDescripcion());
				notaCreditoDetalleDTOFact.setDescuento(detalleFact.getDescuento());
				notaCreditoDetalleDTOFact.setValorUnidad(detalleFact.getValorUnidad());
				notaCreditoDetalleDTOFact.setSubTotal(detalleFact.getSubTotal());
				notaCreditoDetalleDTOFact.setArticuloDTO(detalleFact.getArticuloDTO());
				notaCreditoDetalleDTOFact.setArticuloUnidadManejoDTO(detalleFact.getArticuloUnidadManejoDTO());
				this.notaCreditoDetalleDTOCols.add(notaCreditoDetalleDTOFact);
			});
		}
	}
	
	/**
	 * Metodo para consultar factura por numero de documento 
	 * @param e
	 */
	public void realizarConsultaFacturas(AjaxBehaviorEvent e) {
		if(StringUtils.isEmpty(this.notaCreditoDTO.getNumeroComprobante())) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, "El campo no debe estar vacio, ingrese el n\u00famero de comprabante de factura electr\u00F3nica.");
		}else {
			String numeroDocumento = this.notaCreditoDTO.getRucCliente();
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturasElectronicas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumento, null, null, null, null);;
			if(CollectionUtils.isNotEmpty(facturaCabeceraDTOCols)) {
				this.facturaCabeceraDTO = this.facturaCabeceraDTOCols.iterator().next();
				this.notaCreditoDTO.setNumeroComprobante(this.facturaCabeceraDTO.getNumeroDocumento());
				this.notaCreditoDTO.setFechaEmisionFactura(this.facturaCabeceraDTO.getFechaDocumento());
				this.notaCreditoDTO.setEmail(this.facturaCabeceraDTO.getEmail());
				this.notaCreditoDTO.setPagado(this.facturaCabeceraDTO.getPagado());
				this.notaCreditoDTO.setRucCliente(this.facturaCabeceraDTO.getRucDocumento());
				this.notaCreditoDTO.setRazonSocial(this.facturaCabeceraDTO.getNombreClienteProveedor());
				this.notaCreditoDTO.setTipoCliente(this.facturaCabeceraDTO.getTipoCliente());
				this.notaCreditoDTO.setTipoRuc(this.facturaCabeceraDTO.getTipoRuc());
				this.notaCreditoDTO.setCodigoFactura(this.facturaCabeceraDTO.getId().getCodigoFactura());
				this.notaCreditoDTO.setDescuento(this.facturaCabeceraDTO.getDescuento());
				this.notaCreditoDTO.setSubTotal(this.facturaCabeceraDTO.getSubTotal());
				this.notaCreditoDTO.setTotalCuenta(this.facturaCabeceraDTO.getTotalCuenta());
				this.notaCreditoDTO.setTotalImpuestos(this.facturaCabeceraDTO.getTotalImpuestos());
				this.notaCreditoDTO.setTotalSinImpuestos(this.facturaCabeceraDTO.getTotalSinImpuestos());
				this.notaCreditoDTO.setTotalIva(this.facturaCabeceraDTO.getTotalIva());
				
				this.notaCreditoDetalleDTOCols = new ArrayList<>();
				
				if(CollectionUtils.isNotEmpty(this.facturaCabeceraDTO.getFacturaDetalleDTOCols())) {
					this.facturaCabeceraDTO.getFacturaDetalleDTOCols().stream().forEach(detalleFact ->{
						NotaCreditoDetalleDTO notaCreditoDetalleDTOFact = new NotaCreditoDetalleDTO();
						notaCreditoDetalleDTOFact.getId().setCodigoCompania(detalleFact.getId().getCodigoCompania());
						notaCreditoDetalleDTOFact.setCodigoArticulo(detalleFact.getCodigoArticulo());
						notaCreditoDetalleDTOFact.setCodigoArticuloUnidadManejo(detalleFact.getCodigoArticuloUnidadManejo());
						notaCreditoDetalleDTOFact.setCantidad(detalleFact.getCantidad());
						notaCreditoDetalleDTOFact.setDescripcion(detalleFact.getDescripcion());
						notaCreditoDetalleDTOFact.setDescuento(detalleFact.getDescuento());
						notaCreditoDetalleDTOFact.setValorUnidad(detalleFact.getValorUnidad());
						notaCreditoDetalleDTOFact.setSubTotal(detalleFact.getSubTotal());
						notaCreditoDetalleDTOFact.setArticuloDTO(detalleFact.getArticuloDTO());
						notaCreditoDetalleDTOFact.setArticuloUnidadManejoDTO(detalleFact.getArticuloUnidadManejoDTO());
						this.notaCreditoDetalleDTOCols.add(notaCreditoDetalleDTOFact);
					});
				}
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
		        MensajesController.addWarn(null, "No se encontr\u00F3 la factura con el documento ingresado.");
			}
		}
	}
	
	/**
	 * Agregar nueva fila factura
	 * @param e
	 */
	public void agragarRegistroDetalleFactura(ActionEvent e) {
		NotaCreditoDetalleDTO detalle = new NotaCreditoDetalleDTO();
		detalle.getId().setCodigoCompania(contDetalle);
		detalle.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
		detalle.setCantidad(this.notaCreditoDetalleDTO.getCantidad());
		detalle.setDescripcion(this.notaCreditoDetalleDTO.getDescripcion());
		detalle.setValorUnidad(this.notaCreditoDetalleDTO.getValorUnidad());
		detalle.setCodigoArticulo(this.notaCreditoDetalleDTO.getCodigoArticulo());
		detalle.setArticuloDTO(this.notaCreditoDetalleDTO.getArticuloDTO());
		detalle.setSubTotal(this.notaCreditoDetalleDTO.getSubTotal());
		this.notaCreditoDetalleDTOCols.add(detalle);
		this.calcularTotalFactura();
		contDetalle++;
	}
	
	/**
	 * Metodo para consultar articulos autocomplete
	 * @param query
	 * @return
	 */
	public List<ArticuloDTO> completeArticulos(String query) {
        Collection<ArticuloDTO> allThemes = this.service.getArticuloDTOCols();
        
        List<ArticuloDTO> filteredThemes = new ArrayList<ArticuloDTO>();
         
        for (ArticuloDTO skin : allThemes) {
            if(skin.getNombreArticulo().toLowerCase().contains(query)) {
                filteredThemes.add(skin);
            }
        }
        return filteredThemes;
    }
	
	/**
	 * Abrir popup detalle articulos
	 * @param e
	 */
	public void abrirPopUpDetalleNotaCredito(ActionEvent e) {
		this.notaCreditoDetalleDTO = new NotaCreditoDetalleDTO();
	}

	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public void calcularValoresArticulo(){
		for(ArticuloDTO articuloDTOSearch : this.articuloDTOCols) {
			if(notaCreditoDetalleDTO.getDescripcion().equals(articuloDTOSearch.getNombreArticulo())) {
				this.notaCreditoDetalleDTO.setArticuloDTO(articuloDTOSearch);
				this.notaCreditoDetalleDTO.setCodigoArticulo(articuloDTOSearch.getId().getCodigoArticulo());
				this.notaCreditoDetalleDTO.setValorUnidad(articuloDTOSearch.getPrecio());
				this.notaCreditoDetalleDTO.setSubTotal(BigDecimal.valueOf(notaCreditoDetalleDTO.getCantidad()*notaCreditoDetalleDTO.getValorUnidad().doubleValue()));
				break;
			}
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
	 * Metodo para regresar a la busqueda de notas de credito
	 * @param e
	 */
	public String regresarBusquedaNotasCredito(){
		this.setDocumentoCreado(Boolean.FALSE);
		this.notasCreditoDataManager.setNotaCreditoDTOEditar(new NotaCreditoDTO());
		return "/modules/notascredito/adminBusquedaNotasCredito.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nueva nota de credito
	 * @return
	 */
	public String crearNuevaNotaCredito(){
		this.setDocumentoCreado(Boolean.FALSE);
		this.notasCreditoDataManager.setNotaCreditoDTOEditar(new NotaCreditoDTO());
		return "/modules/notascredito/nuevaNotaCredito.xhtml?faces-redirect=true";
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
	 * Borrar filtro de numero factura
	 */
	public void borrarBusquedaNumeroFactura(ActionEvent e){
		this.numeroNotaCredito = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de numero factura
	 */
	public void borrarDocumentoClienteFactura(ActionEvent e){
		this.docClienteFactura = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de numero factura
	 */
	public void borrarNombreClienteFactura(ActionEvent e){
		this.nombClienteFactura = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro fecha factura
	 */
	public void borrarBusquedaFechaFactura(ActionEvent e){
		this.fechaFacturaInicio = new Date();
		this.fechaFacturaFin = new Date();
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro documento cliente o proveedor
	 */
	public void borrarBusquedaDocumento(ActionEvent e){
		this.docClienteProveedor = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro nombre cliente o proveedor
	 */
	public void borrarBusquedaClienteProveedor(ActionEvent e){
		this.nombClienteProveedor = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de busqueda por documento cliente
	 */
	public void borrarBusquedaDocumentoCliente(ActionEvent e){
		this.documentoClienteBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Borrar filtro de nombre cliente
	 */
	public void borrarBusquedaNombreCliente(ActionEvent e){
		this.nombreClienteBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public String imprimirListaNotasCredito() {
		try {
			// Convertidor de decimales
			DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
		    decimalSymbols.setDecimalSeparator('.');
			DecimalFormat formatoDecimales = new DecimalFormat("#.##", decimalSymbols);
			formatoDecimales.setMinimumFractionDigits(2);
			SimpleDateFormat formatoFecha = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
			Map<String, Object> params = new HashMap<>();
			params.put("FECHA_INICIO", formatoFecha.format(fechaFacturaInicio));
			params.put("FECHA_FIN", formatoFecha.format(fechaFacturaFin));
			byte[] contenido = null;
			params.put("TOTAL_SUBTOTAL", formatoDecimales.format(this.totalSubTotal));
			params.put("TOTAL_DESCUENTO", formatoDecimales.format(this.totalDescuento));
			params.put("TOTAL_VENTA", formatoDecimales.format(this.totalCuenta));
			String tituloReporte = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("tituloReporte");
			params.put("TITULO", tituloReporte);
//			contenido = UtilitarioReportesWeb.generarReporteVentas(this.notaCreditoDTOCols, params);
			UtilitarioWeb.mostrarPDF(contenido);
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir");
		}
		return null;
	}
	
	public void imprimirFormatoNotaVenta(byte[] contenido) throws IOException, PrinterException{
	    // Indicamos el nombre del archivo Pdf que deseamos imprimir
	    InputStream targetStream = new ByteArrayInputStream(contenido);
	    PDDocument document = PDDocument.load(targetStream);
	    PrinterJob job = PrinterJob.getPrinterJob();
	    job.setPageable(new PDFPageable(document));
	    job.print();
	}
	
	public void impresionComprobante(byte[] contenido) {
		try{
			imprimirFormatoNotaVenta(contenido);
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir documentos seleccionado.");
		}
	}
	
	public void imprimirNotaCreditoImpresora(ActionEvent e){
		try {
			boolean ban = true;
			Collection<NotaCreditoDetalleDTO> detallesCompletos = this.notaCreditoDTO.getNotaCreditoDetalleDTOCols().stream().filter(detail -> detail.getCodigoArticulo() != null && detail.getId().getCodigoDetalleNotaCredito() != null).collect(Collectors.toList());
			this.notaCreditoDTO.setNotaCreditoDetalleDTOCols(detallesCompletos);
			for(NotaCreditoDetalleDTO detalle : this.notaCreditoDTO.getNotaCreditoDetalleDTOCols()) {
				if(detalle == null || detalle.getCantidad() == null) {
					ban = false;
				}
			}
			if(ban) {
				this.notaCreditoDTO.setNotaCreditoDetalleDTOCols(this.notaCreditoDTO.getNotaCreditoDetalleDTOCols().stream().sorted(Comparator.comparing(NotaCreditoDetalleDTO::getOrdenRegistro)).collect(Collectors.toList()));
			}	
			
			byte[] xmlDocument = ERPFactory.notascredito.getNotaCreditoServicio().findObtenerXmlDocumentoNotaCredito(ERPConstantes.CODIGO_COMPANIA, this.notaCreditoDTO.getId().getCodigoNotaCredito());
			if(xmlDocument != null){
				byte[] contenido = NotaCreditoElectronicaUtil.imprimirnNotaCreditoFormato(xmlDocument);
				impresionComprobante(contenido);
				MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.despacho.mensaje.impresion.correcta"));
			}else{
				MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.mensaje.error.impresion.factura"));
			}
			this.clearNuevaNotaCredito(e);	
			this.setShowMessagesBar(Boolean.TRUE);
		} catch (Exception execption) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir documento seleccionado.");
			execption.printStackTrace();
		}
	}
	
	/**
	 * Metodo para nota de credito
	 */
	public void imprimirNotaCredito() {
		try {
			if(this.validarInformacionRequerida()) {
				byte[] xmlDocument = ERPFactory.notascredito.getNotaCreditoServicio().findObtenerXmlDocumentoNotaCredito(ERPConstantes.CODIGO_COMPANIA, this.notaCreditoDTO.getId().getCodigoNotaCredito());
				if(xmlDocument != null){
					byte[] contenido = NotaCreditoElectronicaUtil.imprimirnNotaCreditoFormato(xmlDocument);
					UtilitarioWeb.mostrarPDF(contenido);
				}
			}
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir");
		}
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarDocumentoEditar() {
		if(this.notaCreditoDTO == null) {
			return null;
		}else{
			this.notasCreditoDataManager.setNotaCreditoDTOEditar(this.notaCreditoDTO);
			return "/modules/notacredito/nuevaNotaCredito.xhtml?faces-redirect=true";
		}
	}
	
	public void setNotasCreditoDataManager(NotasCreditoDataManager notasCreditoDataManager) {
		this.notasCreditoDataManager = notasCreditoDataManager;
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

	

	public String getNumeroNotaCredito() {
		return numeroNotaCredito;
	}

	public void setNumeroNotaCredito(String numeroNotaCredito) {
		this.numeroNotaCredito = numeroNotaCredito;
	}

	public Date getFechaFacturaInicio() {
		return fechaFacturaInicio;
	}

	public void setFechaFacturaInicio(Date fechaFacturaInicio) {
		this.fechaFacturaInicio = fechaFacturaInicio;
	}

	public Date getFechaFacturaFin() {
		return fechaFacturaFin;
	}

	public void setFechaFacturaFin(Date fechaFacturaFin) {
		this.fechaFacturaFin = fechaFacturaFin;
	}

	public String getDocClienteProveedor() {
		return docClienteProveedor;
	}

	public void setDocClienteProveedor(String docClienteProveedor) {
		this.docClienteProveedor = docClienteProveedor;
	}

	public String getNombClienteProveedor() {
		return nombClienteProveedor;
	}

	public void setNombClienteProveedor(String nombClienteProveedor) {
		this.nombClienteProveedor = nombClienteProveedor;
	}

	public Boolean getPagado() {
		return pagado;
	}

	public void setPagado(Boolean pagado) {
		this.pagado = pagado;
	}

	public Collection<CatalogoValorDTO> getTipoFacturaCatalogoValorDTOCols() {
		return tipoFacturaCatalogoValorDTOCols;
	}

	public void setTipoFacturaCatalogoValorDTOCols(Collection<CatalogoValorDTO> tipoFacturaCatalogoValorDTOCols) {
		this.tipoFacturaCatalogoValorDTOCols = tipoFacturaCatalogoValorDTOCols;
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

	public Integer getContDetalle() {
		return contDetalle;
	}

	public void setContDetalle(Integer contDetalle) {
		this.contDetalle = contDetalle;
	}

	public Collection<ArticuloDTO> getArticuloDTOCols() {
		return articuloDTOCols;
	}

	public void setArticuloDTOCols(Collection<ArticuloDTO> articuloDTOCols) {
		this.articuloDTOCols = articuloDTOCols;
	}

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	public Boolean getDocumentoCreado() {
		return documentoCreado;
	}

	public void setDocumentoCreado(Boolean documentoCreado) {
		this.documentoCreado = documentoCreado;
	}

	public ArticuloService getService() {
		return service;
	}

	public void setService(ArticuloService service) {
		this.service = service;
	}

	public Boolean getCrearNuevaFila() {
		return crearNuevaFila;
	}

	public void setCrearNuevaFila(Boolean crearNuevaFila) {
		this.crearNuevaFila = crearNuevaFila;
	}

	public BigDecimal getTotalCuenta() {
		return totalCuenta;
	}

	public void setTotalCuenta(BigDecimal totalCuenta) {
		this.totalCuenta = totalCuenta;
	}

	public String getDocumentoClienteBusqueda() {
		return documentoClienteBusqueda;
	}

	public void setDocumentoClienteBusqueda(String documentoClienteBusqueda) {
		this.documentoClienteBusqueda = documentoClienteBusqueda;
	}

	public String getNombreClienteBusqueda() {
		return nombreClienteBusqueda;
	}

	public void setNombreClienteBusqueda(String nombreClienteBusqueda) {
		this.nombreClienteBusqueda = nombreClienteBusqueda;
	}

	public String[] getPagadoBusqueda() {
		return pagadoBusqueda;
	}

	public void setPagadoBusqueda(String[] pagadoBusqueda) {
		this.pagadoBusqueda = pagadoBusqueda;
	}

	public ClienteDTO getClienteDTO() {
		return clienteDTO;
	}

	public void setClienteDTO(ClienteDTO clienteDTO) {
		this.clienteDTO = clienteDTO;
	}

	public Boolean getCrearFacturaElectronica() {
		return crearFacturaElectronica;
	}

	public void setCrearFacturaElectronica(Boolean crearFacturaElectronica) {
		this.crearFacturaElectronica = crearFacturaElectronica;
	}

	public BigDecimal getTotalSubTotal() {
		return totalSubTotal;
	}

	public void setTotalSubTotal(BigDecimal totalSubTotal) {
		this.totalSubTotal = totalSubTotal;
	}

	public BigDecimal getTotalDescuento() {
		return totalDescuento;
	}

	public void setTotalDescuento(BigDecimal totalDescuento) {
		this.totalDescuento = totalDescuento;
	}

	public NotaCreditoDTO getNotaCreditoDTO() {
		return notaCreditoDTO;
	}

	public void setNotaCreditoDTO(NotaCreditoDTO notaCreditoDTO) {
		this.notaCreditoDTO = notaCreditoDTO;
	}

	public NotaCreditoDetalleDTO getNotaCreditoDetalleDTO() {
		return notaCreditoDetalleDTO;
	}

	public void setNotaCreditoDetalleDTO(NotaCreditoDetalleDTO notaCreditoDetalleDTO) {
		this.notaCreditoDetalleDTO = notaCreditoDetalleDTO;
	}

	public Collection<NotaCreditoDetalleDTO> getNotaCreditoDetalleDTOCols() {
		return notaCreditoDetalleDTOCols;
	}

	public void setNotaCreditoDetalleDTOCols(Collection<NotaCreditoDetalleDTO> notaCreditoDetalleDTOCols) {
		this.notaCreditoDetalleDTOCols = notaCreditoDetalleDTOCols;
	}

	public Collection<NotaCreditoDTO> getNotaCreditoDTOCols() {
		return notaCreditoDTOCols;
	}

	public void setNotaCreditoDTOCols(Collection<NotaCreditoDTO> notaCreditoDTOCols) {
		this.notaCreditoDTOCols = notaCreditoDTOCols;
	}

	public Collection<FacturaCabeceraDTO> getFacturaCabeceraDTOCols() {
		return facturaCabeceraDTOCols;
	}

	public void setFacturaCabeceraDTOCols(Collection<FacturaCabeceraDTO> facturaCabeceraDTOCols) {
		this.facturaCabeceraDTOCols = facturaCabeceraDTOCols;
	}

	public String getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(String numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public Date getFechaFacInicio() {
		return fechaFacInicio;
	}

	public void setFechaFacInicio(Date fechaFacInicio) {
		this.fechaFacInicio = fechaFacInicio;
	}

	public Date getFechaFacFin() {
		return fechaFacFin;
	}

	public void setFechaFacFin(Date fechaFacFin) {
		this.fechaFacFin = fechaFacFin;
	}

	public String getDocClienteFactura() {
		return docClienteFactura;
	}

	public void setDocClienteFactura(String docClienteFactura) {
		this.docClienteFactura = docClienteFactura;
	}

	public String getNombClienteFactura() {
		return nombClienteFactura;
	}

	public void setNombClienteFactura(String nombClienteFactura) {
		this.nombClienteFactura = nombClienteFactura;
	}

	public Long getCodigoFacturaSeleccionada() {
		return codigoFacturaSeleccionada;
	}

	public void setCodigoFacturaSeleccionada(Long codigoFacturaSeleccionada) {
		this.codigoFacturaSeleccionada = codigoFacturaSeleccionada;
	}

	public FacturaCabeceraDTO getFacturaCabeceraDTO() {
		return facturaCabeceraDTO;
	}

	public void setFacturaCabeceraDTO(FacturaCabeceraDTO facturaCabeceraDTO) {
		this.facturaCabeceraDTO = facturaCabeceraDTO;
	}
}
