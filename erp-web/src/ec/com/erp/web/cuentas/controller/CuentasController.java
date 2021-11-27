
package ec.com.erp.web.cuentas.controller;

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
import ec.com.erp.cliente.mdl.dto.FacturaDetalleDTO;
import ec.com.erp.cliente.mdl.dto.PagosFacturaDTO;
import ec.com.erp.cliente.mdl.dto.ProveedorDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.VendedorDTO;
import ec.com.erp.cliente.mdl.dto.id.FacturaCabeceraID;
import ec.com.erp.facturacion.electronica.ws.FacturaElectronicaUtil;
import ec.com.erp.facturacion.electronica.ws.NotaVentaUtil;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioReportesWeb;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.cuentas.datamanager.CuentasDataManager;
import ec.com.erp.web.pedidos.controller.ArticuloService;

/**
 * Controlador para administracion de pedidos
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class CuentasController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Variables
	private FacturaCabeceraDTO facturaCabeceraDTO;
	private FacturaDetalleDTO facturaDetalleDTO;
	private PagosFacturaDTO pagosFacturaDTO;
	private ClienteDTO clienteDTO;
	private Collection<FacturaDetalleDTO> facturaDetalleDTOCols;
	private Collection<FacturaCabeceraDTO> facturaCabeceraDTOCols;
	private Collection<ClienteDTO> clienteDTOCols;
	private Collection<VendedorDTO> vendedorDTOCols;
	private Collection<ProveedorDTO> proveedorDTOCols;
	private Collection<ArticuloDTO> articuloDTOCols;
	private Collection<PagosFacturaDTO> pagosFacturaDTOCols;
	private BigDecimal totalPagado;
	private BigDecimal totalPendiente;
	private Integer tamanioPopUp;
	private String[] pagadoBusqueda;
	private Boolean crearFacturaElectronica;
	
	// Data Managers
	@ManagedProperty(value="#{cuentasDataManager}")
	private CuentasDataManager cuentasDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	@ManagedProperty("#{articuloService}")
	private ArticuloService service;
	
	// Variables
	private String numeroFactura;
	private Date fechaFacturaInicio;
	private Date fechaFacturaFin;
	private String docClienteProveedor;
	private String nombClienteProveedor;
	private Boolean pagado;
	private String tipoDocumento;
	private Collection<CatalogoValorDTO> tipoFacturaCatalogoValorDTOCols;
	private Integer page;
	private Long codigoClienteSeleccionado;
	private Long codigoVendedorSeleccionado;
	private Long codigoProveedorSeleccionado;
	private Integer contDetalle;
	private String codigoBarras;
	private Boolean documentoCreado;
	private Boolean crearNuevaFila;
	private BigDecimal totalCuenta;
	private BigDecimal totalPagos;
	private BigDecimal totalSubTotal;
	private BigDecimal totalDescuento;
	private String tipoRuc;
	private String numeroDocumentoBusqueda;
	private String nombreVendedorBusqueda;
	private String documentoClienteBusqueda;
	private String nombreClienteBusqueda;
	private String documentoVendedorBusqueda;
	private String nombreVendedor;
	private Collection<String> tiposDocumentos;
	private Long codigoVendedor;

	@PostConstruct
	public void postConstruct() {
		if(this.loginController.getUsuariosDTO().getPerfilDTO().getCodigoValorTipoPerfil().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_TIPO_PERFIL_VENDEDORES)) {
			this.codigoVendedor = loginController.getUsuariosDTO().getCodigoVendedor();
		}
		this.crearFacturaElectronica = Boolean.FALSE;
		this.tipoRuc = ERPConstantes.ESTADO_INACTIVO_NUMERICO;
		this.tamanioPopUp = 510;
		this.totalPagado = BigDecimal.ZERO;
		this.totalPendiente = BigDecimal.ZERO;
		this.totalCuenta = BigDecimal.ZERO;
		this.totalPagos = BigDecimal.ZERO;
		this.totalSubTotal = BigDecimal.ZERO;
		this.totalDescuento = BigDecimal.ZERO;
		this.loginController.activarMenusSeleccionado();
		this.documentoCreado = Boolean.FALSE;
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		this.pagosFacturaDTO = new PagosFacturaDTO(); 
		this.facturaCabeceraDTO.setFechaDocumento(new Date());
		this.facturaCabeceraDTO.setPagado(Boolean.FALSE);
		this.facturaCabeceraDTO.setTipoRuc(ERPConstantes.TIPO_RUC_UNO);
		this.tipoRuc = "0";
		this.facturaDetalleDTO = new FacturaDetalleDTO();
		this.facturaCabeceraDTOCols = new ArrayList<FacturaCabeceraDTO>();
		this.facturaDetalleDTOCols = new ArrayList<FacturaDetalleDTO>();
		this.clienteDTOCols =  new ArrayList<ClienteDTO>();
		this.vendedorDTOCols = new ArrayList<>();
		this.proveedorDTOCols = new ArrayList<>();
		this.pagosFacturaDTOCols = new ArrayList<>();
		this.clienteDTO = new ClienteDTO();
		this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		
		SecuenciaDTO secuenciaPedido = new SecuenciaDTO();
		if(this.cuentasDataManager.getTipoFactura() != null && this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS)) {
			secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_VENTA);
			this.facturaCabeceraDTO.setCodigoReferenciaFactura("FAC-"+secuenciaPedido.getValorSecuencia());
		}
		if(this.cuentasDataManager.getTipoFactura() != null && this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS)) {
			secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_COMPRA);
			this.facturaCabeceraDTO.setCodigoReferenciaFactura("DOC-"+secuenciaPedido.getValorSecuencia());
		}
		this.page = 0;
		this.contDetalle = 1;
		
		FacturaDetalleDTO detalle = null;
		
		contDetalle = 1;
		for(int i=0; i< 10; i++) {
			detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
			contDetalle++;
		}
		
		// Inicializar fechas para filtros de busqueda
		Calendar fechaInferior = Calendar.getInstance();
		fechaInferior.set(Calendar.MONTH, 0);
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		Calendar fechaSuperior = Calendar.getInstance();
		fechaFacturaInicio = fechaInferior.getTime();
		fechaFacturaFin = fechaSuperior.getTime();
		tiposDocumentos = new ArrayList<>();
		
		this.tipoFacturaCatalogoValorDTOCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_DOCUMENTOS);
		this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, null);
		
		// Cargar datos a editar
		if(cuentasDataManager.getFacturaCabeceraDTOEditar() != null && cuentasDataManager.getFacturaCabeceraDTOEditar().getId().getCodigoFactura() != null){
			this.setFacturaCabeceraDTO(cuentasDataManager.getFacturaCabeceraDTOEditar());
			this.setFacturaDetalleDTOCols(cuentasDataManager.getFacturaCabeceraDTOEditar().getFacturaDetalleDTOCols());
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/facturas/adminBusquedaFacturas.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA);
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturasCanceladas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, this.codigoVendedor);
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/ventas/adminBusquedaVentas.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA);
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, this.codigoVendedor);
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/compras/adminBusquedaCompras.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS;
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, null);
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/cuentas/adminBusquedaCuentas.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA);
			pagado = Boolean.FALSE;
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, this.codigoVendedor);
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/cuentas/adminBusquedaCuentasPagar.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS;
			tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
			pagado = Boolean.FALSE;
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, null);
		}
		if(CollectionUtils.isNotEmpty(this.facturaCabeceraDTOCols)) {
			this.facturaCabeceraDTOCols.stream().forEach(factura ->{
				if(factura.getPagado()) {
					factura.setTotalPagos(factura.getTotalCuenta());
					this.totalPagos = this.totalPagos.add(factura.getTotalCuenta());
				}else {
					if(factura.getTotalPagos() != null){
						this.totalPagos = this.totalPagos.add(factura.getTotalPagos());
					}
				}
				this.totalCuenta =  this.totalCuenta.add(factura.getTotalCuenta());
				this.totalSubTotal = this.totalSubTotal.add(factura.getSubTotal());
				this.totalDescuento = this.totalDescuento.add(factura.getDescuento());
			});
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return cuentasDataManager;
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
	
	public void consultarFacturas(ActionEvent e) {
		this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, this.codigoVendedor);
		if(CollectionUtils.isNotEmpty(this.facturaCabeceraDTOCols)) {
			this.totalCuenta = BigDecimal.ZERO;
			this.totalPagos = BigDecimal.ZERO;
			this.totalSubTotal = BigDecimal.ZERO;
			this.totalDescuento = BigDecimal.ZERO;
			this.facturaCabeceraDTOCols.stream().forEach(factura ->{
				if(factura.getPagado()) {
					factura.setTotalPagos(factura.getTotalCuenta());
					this.totalPagos = this.totalPagos.add(factura.getTotalCuenta());
				}else {
					this.totalPagos = this.totalPagos.add(factura.getTotalPagos());
				}
				this.totalCuenta =  this.totalCuenta.add(factura.getTotalCuenta());
				this.totalSubTotal = this.totalSubTotal.add(factura.getSubTotal());
				this.totalDescuento = this.totalDescuento.add(factura.getDescuento());
			});
		}
	}
	
	/**
	 * Metodo para descargar facturas de fire base
	 * @param e
	 */
	public void descargarFacturasFireBase(ActionEvent e){
		try {
			System.out.println("Ingreso a realizar proceson con fire base");
			ERPFactory.firebase.getFireBaseServicio().transDescargarFacturasFireBase();		
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addInfo(null, "Se ha terminado de descargar la informacion de facturas de los dispositivos moviles");
			System.out.println("Finalizo proceso con fire base");
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al descargar facturas de dispositivos moviles, "+e1.getMessage() );
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al descargar facturas de dispositivos moviles, "+e2.getMessage());
		}
	}
	
	/**
	 * Metodo para descargar facturas de fire base
	 * @param e
	 */
	public void actualizarFacturasFireBase(ActionEvent e){
		try {
			System.out.println("Ingreso a actualizar facturas con fire base");
			ERPFactory.firebase.getFireBaseServicio().findActualizarPagoFacturas();	
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addInfo(null, "Se ha terminado de actualizar la informacion de facturas de los dispositivos moviles");
			System.out.println("Finalizo proceso con fire base");
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al actualizar facturas en dispositivos moviles, "+e1.getMessage() );
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al actualizar facturas en dispositivos moviles, "+e2.getMessage());
		}
	}
	
	/**
	 * Metodo para agregar calcular 
	 * @param e
	 */
	public void seleccionarTipoDocumento(ValueChangeEvent e) {
		String tipoVenta = (String)e.getNewValue();
		SecuenciaDTO secuenciaPedido;
		if(tipoVenta.equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS)) {
			secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_VENTA);
			this.facturaCabeceraDTO.setCodigoReferenciaFactura("FAC-"+secuenciaPedido.getValorSecuencia());
			this.facturaCabeceraDTO.setActualizarSecuencia(Boolean.FALSE);
		}else {
			secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_NOTA_VENTA);
			this.facturaCabeceraDTO.setCodigoReferenciaFactura("NOT-"+secuenciaPedido.getValorSecuencia());
			SecuenciaDTO secuencia = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_FACTURA_RUC_UNO);
			String numeroFactura = ValidationUtils.obtenerSecuencialFactura(5, String.valueOf(secuencia.getValorSecuencia()));
			this.facturaCabeceraDTO.setNumeroDocumento(numeroFactura);
			this.facturaCabeceraDTO.setActualizarSecuencia(Boolean.TRUE);
		}
	}
	
	/**
	 * Metodo para obtener secuencial de facturas segun el RUC 
	 * @param e
	 */
	public void seleccionarTipoRuc(ValueChangeEvent e) {
		String tipoRuc = (String)e.getNewValue();
		if(tipoRuc.equals(ERPConstantes.TIPO_RUC_DOS)) {
//			SecuenciaDTO secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_FACTURA_RUC_UNO);
//			String numeroFactura = ValidationUtils.obtenerSecuencialFactura(5, String.valueOf(secuenciaPedido.getValorSecuencia()));
//			this.facturaCabeceraDTO.setNumeroDocumento(numeroFactura);
			this.tipoRuc = "1";
		}else {
			this.facturaCabeceraDTO.setNumeroDocumento("");
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
        for(FacturaDetalleDTO facturaDetalleDTOTemp : facturaDetalleDTOCols) {
        	if(facturaDetalleDTOTemp.getDescripcion() != null) {
        		String queryLowerCase = facturaDetalleDTOTemp.getDescripcion().toLowerCase();
        		ArticuloDTO articuloSeleccionado = this.articuloDTOCols.stream()
                		.filter(articulo -> articulo.getNombreArticulo().toLowerCase().equals(queryLowerCase))
                		.findFirst().orElse(null);
        		facturaDetalleDTOTemp.setArticuloDTO(articuloSeleccionado);
        	}
        	
			if((facturaDetalleDTOTemp.getCantidad() == null ||  facturaDetalleDTOTemp.getCantidad().intValue() == 0) && facturaDetalleDTOTemp.getArticuloDTO() != null && facturaDetalleDTOTemp.getArticuloDTO().getPrecio() != null){
				facturaDetalleDTOTemp.setCantidad(1);
			}
			if(facturaDetalleDTOTemp.getCantidad() != null && facturaDetalleDTOTemp.getArticuloDTO() != null && facturaDetalleDTOTemp.getArticuloDTO().getPrecio() != null && facturaDetalleDTOTemp.getArticuloDTO().getPrecioMinorista() != null && (facturaDetalleDTOTemp.getCodigoArticulo() == null || facturaDetalleDTOTemp.getCodigoArticulo() != facturaDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo()) && CollectionUtils.isNotEmpty(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
				ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
				facturaDetalleDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
				if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
					facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getPrecioMinorista());
				}else {
					facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getPrecio());
				}
				BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+(facturaDetalleDTOTemp.getCantidad().intValue() * articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(facturaDetalleDTOTemp.getValorUnidad());
				facturaDetalleDTOTemp.setSubTotal(subTotal);
				facturaDetalleDTOTemp.setCodigoArticulo(facturaDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
				facturaDetalleDTOTemp.setCodigoBarras(facturaDetalleDTOTemp.getArticuloDTO().getCodigoBarras());
				facturaDetalleDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
			}
		}
		this.calcularTotalFactura();
		// Validar filas llenas
		facturaDetalleDTOCols.forEach(detail ->{
			if(detail.getCodigoArticulo() == null) {
				this.crearNuevaFila = Boolean.FALSE;
			}
		});
		if(facturaDetalleDTOCols.size() >= 20) {
			this.crearNuevaFila = Boolean.FALSE;
		}
		if(this.crearNuevaFila) {
			FacturaDetalleDTO detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
			contDetalle++;
		}
    }
	
	public void onItemSelectCompra(SelectEvent event) {
        System.out.println(event.getObject());
        this.setShowMessagesBar(Boolean.FALSE);
        this.crearNuevaFila = Boolean.TRUE;
        for(FacturaDetalleDTO facturaDetalleDTOTemp : facturaDetalleDTOCols) {
        	if(facturaDetalleDTOTemp.getDescripcion() != null) {
        		String queryLowerCase = facturaDetalleDTOTemp.getDescripcion().toLowerCase();
        		ArticuloDTO articuloSeleccionado = this.articuloDTOCols.stream()
                		.filter(articulo -> articulo.getNombreArticulo().toLowerCase().equals(queryLowerCase))
                		.findFirst().orElse(null);
        		facturaDetalleDTOTemp.setArticuloDTO(articuloSeleccionado);
        	}
        	
			if((facturaDetalleDTOTemp.getCantidad() == null ||  facturaDetalleDTOTemp.getCantidad().intValue() == 0) && facturaDetalleDTOTemp.getArticuloDTO().getCosto() != null){
				facturaDetalleDTOTemp.setCantidad(1);
			}
			if(facturaDetalleDTOTemp.getCantidad() != null && facturaDetalleDTOTemp.getArticuloDTO() != null && facturaDetalleDTOTemp.getArticuloDTO().getCosto() != null && (facturaDetalleDTOTemp.getCodigoArticulo() == null || facturaDetalleDTOTemp.getCodigoArticulo() != facturaDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo()) && CollectionUtils.isNotEmpty(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
				ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
				facturaDetalleDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
				BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(facturaDetalleDTOTemp.getCantidad().intValue() * articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(facturaDetalleDTOTemp.getArticuloDTO().getCosto()), 4);
				facturaDetalleDTOTemp.setSubTotal(subTotal);
				facturaDetalleDTOTemp.setCodigoArticulo(facturaDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
				facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getCosto());
				facturaDetalleDTOTemp.setCodigoBarras(facturaDetalleDTOTemp.getArticuloDTO().getCodigoBarras());
				facturaDetalleDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
			}
		}
		this.calcularTotalFactura();
		// Validar filas llenas
		facturaDetalleDTOCols.forEach(detail ->{
			if(detail.getCodigoArticulo() == null) {
				this.crearNuevaFila = Boolean.FALSE;
			}
		});
		if(facturaDetalleDTOCols.size() >= 20) {
			this.crearNuevaFila = Boolean.FALSE;
		}
		if(this.crearNuevaFila) {
			FacturaDetalleDTO detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
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
		
		for(FacturaDetalleDTO facturaDetalleDTOTemp : facturaDetalleDTOCols) {
			if(facturaDetalleDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(facturaDetalleDTOTemp.getCantidad() != null && facturaDetalleDTOTemp.getValorUnidad() != null && facturaDetalleDTOTemp.getCodigoArticuloUnidadManejo() != null) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = obtenerUnidadManejoPorCodigo(codigoUnidadManejo, facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(facturaDetalleDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(facturaDetalleDTOTemp.getValorUnidad()), 4);
					facturaDetalleDTOTemp.setSubTotal(subTotal);
					facturaDetalleDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
					this.calcularTotalFactura();
				}
				break;
			}
		}
	}

	public void guardarPagoCompra(ActionEvent e){
		try {
			if(this.validarInformacionRequeridaPago()) {
				this.pagosFacturaDTO.setCodigoFactura(this.facturaCabeceraDTO.getId().getCodigoFactura());
				this.pagosFacturaDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.pagosFacturaDTO.setDescripcion(this.pagosFacturaDTO.getDescripcion().toUpperCase());
				this.pagosFacturaDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				ERPFactory.transaccion.getTransaccionServicio().transGuardarPago(ERPConstantes.CODIGO_CATALOGO_VALOR_TRANSACCION_GASTO, this.pagosFacturaDTO);
				this.pagosFacturaDTOCols.add(this.pagosFacturaDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.totalPagado = ValidationUtils.redondear(this.totalPagado.add(this.pagosFacturaDTO.getValorPago()), 4);
				this.totalPendiente = ValidationUtils.redondear(this.totalPendiente.subtract(this.pagosFacturaDTO.getValorPago()), 4);
				this.pagosFacturaDTO = new PagosFacturaDTO();
				this.pagosFacturaDTO.setFechaPago(new Date());
				if(this.totalPendiente.intValue() <= 0) {
					this.facturaCabeceraDTO.setPagado(Boolean.TRUE);
					this.tamanioPopUp = 310;
				}else {
					this.tamanioPopUp = 510;
				}
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.pago.factura.mensaje.pago"));
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.pagosFacturaDTO.getId().setCodigoPago(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.pagosFacturaDTO.getId().setCodigoPago(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	public void guardarPagoVenta(ActionEvent e){
		try {
			if(this.validarInformacionRequeridaPago()) {
				this.pagosFacturaDTO.setCodigoFactura(this.facturaCabeceraDTO.getId().getCodigoFactura());
				this.pagosFacturaDTO.setDescripcion(this.pagosFacturaDTO.getDescripcion().toUpperCase());
				this.pagosFacturaDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.pagosFacturaDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				ERPFactory.transaccion.getTransaccionServicio().transGuardarPago(ERPConstantes.CODIGO_CATALOGO_VALOR_TRANSACCION_INGRESO, this.pagosFacturaDTO);
				this.pagosFacturaDTOCols.add(this.pagosFacturaDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.totalPagado = ValidationUtils.redondear(this.totalPagado.add(this.pagosFacturaDTO.getValorPago()), 4);
				this.totalPendiente = ValidationUtils.redondear(this.totalPendiente.subtract(this.pagosFacturaDTO.getValorPago()), 4);
				this.pagosFacturaDTO = new PagosFacturaDTO();
				this.pagosFacturaDTO.setFechaPago(new Date());
				if(this.totalPendiente.intValue() <= 0) {
					this.facturaCabeceraDTO.setPagado(Boolean.TRUE);
					this.tamanioPopUp = 310;
				}else {
					this.tamanioPopUp = 510;
				}
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.pago.factura.mensaje.pago"));
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.pagosFacturaDTO.getId().setCodigoPago(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.pagosFacturaDTO.getId().setCodigoPago(null);
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Metodo para validar la informacion requerida de la pantalla
	 */
	private Boolean validarInformacionRequeridaPago() {
		Boolean valido = Boolean.TRUE;
		if(this.pagosFacturaDTO.getValorPago() == null) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.pago.valorpago"));
		}
		if(this.pagosFacturaDTO.getValorPago() != null && this.pagosFacturaDTO.getValorPago().doubleValue() == 0.0) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.pago.valorencero"));
		}
		if(StringUtils.isEmpty(this.pagosFacturaDTO.getDescripcion())) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.pago.descripcion"));
		}
		return valido;
	}
	
	
	/**
	 * Metodo para buscar facturas en venta
	 * @param e
	 */
	public void busquedaFacturasCuentasCobrar(ActionEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
		this.pagado = Boolean.FALSE;
		this.buscarFacturas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar facturas en venta por filtros de busqueda al dar enter
	 * @param e
	 */
	public void busquedaFacturasCuentasCobrarEnter(AjaxBehaviorEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
		this.pagado = Boolean.FALSE;
		this.buscarFacturas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar facturas en venta
	 * @param e
	 */
	public void busquedaFacturasVentas(ActionEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
		this.buscarFacturas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar facturas en venta por filtros de busqueda al dar enter
	 * @param e
	 */
	public void busquedaFacturasVentasEnter(AjaxBehaviorEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
		this.buscarFacturas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar facturas en venta
	 * @param e
	 */
	public void busquedaFacturasCompras(ActionEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS;
		this.buscarFacturas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar facturas en venta por filtros de busqueda al dar enter
	 * @param e
	 */
	public void busquedaFacturasComprasEnter(AjaxBehaviorEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS;
		this.buscarFacturas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar cuentas o facturas por filtros de busqueda
	 * @param e
	 */
	public void buscarFacturas(String tipoFactura){
		try {
			Long codigoVendedorBuscado = this.codigoVendedor;
			if(this.codigoVendedor == null && StringUtils.isNotBlank(documentoVendedorBusqueda)){
				this.vendedorDTOCols = ERPFactory.vendedor.getVendedorServicio().findObtenerListaVendedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.documentoVendedorBusqueda, null);
				if(CollectionUtils.isNotEmpty(this.vendedorDTOCols) && this.vendedorDTOCols.size() == 1){
					codigoVendedorBuscado = this.vendedorDTOCols.iterator().next().getId().getCodigoVendedor();
				}else {
					codigoVendedorBuscado = 0L;
				}
			}
			
			
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
			
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()), docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, codigoVendedorBuscado);
			if(CollectionUtils.isEmpty(this.facturaCabeceraDTOCols)){
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("No se encontraron resultados para la b\u00FAsqueda realizada.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}else {
				this.setShowMessagesBar(Boolean.FALSE);
				this.totalCuenta = BigDecimal.ZERO;
				this.totalPagos = BigDecimal.ZERO;
				this.totalSubTotal = BigDecimal.ZERO;
				this.totalDescuento = BigDecimal.ZERO;
				this.facturaCabeceraDTOCols.stream().forEach(factura ->{
					if(factura.getPagado()) {
						factura.setTotalPagos(factura.getTotalCuenta());
						this.totalPagos = this.totalPagos.add(factura.getTotalCuenta());
					}else {
						this.totalPagos = this.totalPagos.add(factura.getTotalPagos());
					}
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
	
	
	/**
	 * Metodo para buscar facturas en venta
	 * @param e
	 */
	public void busquedaFacturasCanceladas(ActionEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
		this.buscarFacturasCanceladas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar facturas en venta por filtros de busqueda al dar enter
	 * @param e
	 */
	public void busquedaFacturasCanceladasEnter(AjaxBehaviorEvent e){
		String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
		this.buscarFacturasCanceladas(tipoFactura);
	}
	
	/**
	 * Metodo para buscar cuentas o facturas por filtros de busqueda
	 * @param e
	 */
	public void buscarFacturasCanceladas(String tipoFactura){
		try {
			Long codigoVendedorBuscado = this.codigoVendedor;
			if(this.codigoVendedor == null && StringUtils.isNotBlank(documentoVendedorBusqueda)){
				this.vendedorDTOCols = ERPFactory.vendedor.getVendedorServicio().findObtenerListaVendedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), this.documentoVendedorBusqueda, null);
				if(CollectionUtils.isNotEmpty(this.vendedorDTOCols) && this.vendedorDTOCols.size() == 1){
					codigoVendedorBuscado = this.vendedorDTOCols.iterator().next().getId().getCodigoVendedor();
				}else {
					codigoVendedorBuscado = 0L;
				}
			}
			
			
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
			
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturasCanceladas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()), docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, codigoVendedorBuscado);
			if(CollectionUtils.isEmpty(this.facturaCabeceraDTOCols)){
				this.setShowMessagesBar(Boolean.TRUE);
				FacesMessage msg = new FacesMessage("No se encontraron resultados para la b\u00FAsqueda realizada.", "ERROR MSG");
		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}else {
				this.setShowMessagesBar(Boolean.FALSE);
				this.totalCuenta = BigDecimal.ZERO;
				this.totalPagos = BigDecimal.ZERO;
				this.totalSubTotal = BigDecimal.ZERO;
				this.totalDescuento = BigDecimal.ZERO;
				this.facturaCabeceraDTOCols.stream().forEach(factura ->{
					if(factura.getPagado()) {
						factura.setTotalPagos(factura.getTotalCuenta());
						this.totalPagos = this.totalPagos.add(factura.getTotalCuenta());
					}else {
						if(factura.getTotalPagos() != null){
							this.totalPagos = this.totalPagos.add(factura.getTotalPagos());
						}
					}
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
	public void guadarActualizarCuentasFacturas(ActionEvent e){
		try {
			this.setDocumentoCreado(Boolean.FALSE);
			if(this.validarInformacionRequerida()) {
				if(this.cuentasDataManager.getTipoFactura() != null && this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS)) {
					if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento() == null) {
						this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
					}
				}
				if(this.cuentasDataManager.getTipoFactura() != null && this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS)) {
					this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
				}
				
				this.facturaCabeceraDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.facturaCabeceraDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.facturaDetalleDTOCols.stream().forEach(detail ->{
					if(detail.getCantidad() != null && detail.getCodigoArticulo() != null) {
//						detail.setDescripcion(detail.getArticuloUnidadManejoDTO().getCodigoValorUnidadManejo()+"x"+detail.getArticuloUnidadManejoDTO().getValorUnidadManejo()+" "+detail.getArticuloDTO().getNombreArticulo());
						detail.setDescripcion(detail.getArticuloDTO().getNombreArticulo());
					}
				});
				
				this.facturaCabeceraDTO.setFacturaDetalleDTOCols(facturaDetalleDTOCols);
				
				ERPFactory.facturas.getFacturaCabeceraServicio().transGuardarActualizarFacturaCabecera(Boolean.FALSE, this.facturaCabeceraDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setDocumentoCreado(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.cabecera.factura.mensaje.guardado")+""+this.crearFacturaElectronica);
			}
			else
			{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.facturaCabeceraDTO.getId().setCodigoFactura(null);
			this.facturaDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleFactura(null));
			this.ordenarDetalles();
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.facturaCabeceraDTO.getId().setCodigoFactura(null);
			this.facturaDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleFactura(null));
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
	public void guadarFirmarEnviarFactura(ActionEvent e){
		try {
			if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA)){
				throw new ERPException("Error", "Cambie el tipo de comprobante a FACTURA VENTA") ;
			}
			this.setDocumentoCreado(Boolean.FALSE);
			if(this.validarInformacionRequerida()) {
				if(this.cuentasDataManager.getTipoFactura() != null && this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS)) {
					if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento() == null) {
						this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
					}
				}
				if(this.cuentasDataManager.getTipoFactura() != null && this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS)) {
					this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
				}
				
				this.facturaCabeceraDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.facturaCabeceraDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.facturaDetalleDTOCols.stream().forEach(detail ->{
					if(detail.getCantidad() != null && detail.getCodigoArticulo() != null) {
//						detail.setDescripcion(detail.getArticuloUnidadManejoDTO().getCodigoValorUnidadManejo()+"x"+detail.getArticuloUnidadManejoDTO().getValorUnidadManejo()+" "+detail.getArticuloDTO().getNombreArticulo());
						detail.setDescripcion(detail.getArticuloDTO().getNombreArticulo());
					}
				});
				
				this.facturaCabeceraDTO.setFacturaDetalleDTOCols(facturaDetalleDTOCols);
				
				ERPFactory.facturas.getFacturaCabeceraServicio().transGuardarActualizarFacturaCabecera(Boolean.TRUE, this.facturaCabeceraDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setDocumentoCreado(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.cabecera.factura.mensaje.guardado")+""+this.crearFacturaElectronica);
			}
			else
			{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.facturaCabeceraDTO.getId().setCodigoFactura(null);
			this.facturaDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleFactura(null));
			this.ordenarDetalles();
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.facturaCabeceraDTO.getId().setCodigoFactura(null);
			this.facturaDetalleDTOCols.stream().forEach(detalle -> detalle.getId().setCodigoDetalleFactura(null));
			this.ordenarDetalles();
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	public void ordenarDetalles() {
		int contador = 1;
		for(FacturaDetalleDTO detalleAux : this.facturaDetalleDTOCols) {
			detalleAux.getId().setCodigoCompania(contador);
			contador++;
		}
	}
	
	/**
	 * Metodo para firmar enviar y autorizar facturas
	 * @param e
	 */
	public void enviarFirmarAutorizar(ActionEvent e){
		try {
			byte[] xmlDocument = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerXmlDocumentoFactura(ERPConstantes.CODIGO_COMPANIA, this.facturaCabeceraDTO.getId().getCodigoFactura());
			if(xmlDocument == null){
				ERPFactory.facturas.getFacturaCabeceraServicio().transEnviarFirmarAutorizar(this.facturaCabeceraDTO);
			    MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.factura.electrinoca"));
			    // Enviar notificacion por mail
			    if(StringUtils.isNotBlank(this.facturaCabeceraDTO.getEmail())){
			    	byte[] factura = FacturaElectronicaUtil.imprimirRideFactura(xmlDocument);
					ERPFactory.notificacion.getNotificacionMailServicio().findEnviarFacturaMail(this.facturaCabeceraDTO.getEmail(), factura);
			    }
			    this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tiposDocumentos, this.codigoVendedor);
			}else{
		        MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.factura.creada"));
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
	 * Metodo para cancelar factura ventas
	 * @param e
	 */
	public void cancelarFacturasVentas(ActionEvent e){
		try {
			byte[] xmlDocument = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerXmlDocumentoFactura(ERPConstantes.CODIGO_COMPANIA, this.facturaCabeceraDTO.getId().getCodigoFactura());
			if(xmlDocument == null){
				ERPFactory.facturas.getFacturaCabeceraServicio().transCancelarFacturaInactivar(this.facturaCabeceraDTO);
				String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
				this.buscarFacturas(tipoFactura);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.cancelacion"));
			}else{
		        MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.error.cancelacion"));
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
	 * Metodo para cancelar factura compras
	 * @param e
	 */
	public void cancelarFacturasCompras(ActionEvent e){
		try {
			ERPFactory.facturas.getFacturaCabeceraServicio().transCancelarFacturaInactivar(this.facturaCabeceraDTO);
			String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS;
			this.buscarFacturas(tipoFactura);
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.informacion.cancelacion"));
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
		if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA) && StringUtils.isEmpty(this.facturaCabeceraDTO.getNumeroDocumento())) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.numero.factura"));
		}
		if(StringUtils.isEmpty(this.facturaCabeceraDTO.getRucDocumento())) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.ruccliente.factura"));
		}
		if(StringUtils.isEmpty(this.facturaCabeceraDTO.getNombreClienteProveedor())) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.nombrecliente.factura"));
		}
		
		Boolean existDetail = Boolean.TRUE;
		for(FacturaDetalleDTO detalle : this.getFacturaDetalleDTOCols()){
			if(detalle.getCantidad() != null && detalle.getValorUnidad() != null && detalle.getCodigoArticulo() != null) {
				existDetail = Boolean.FALSE;
				break;
			}
		}
		
		if(CollectionUtils.isNotEmpty(this.facturaCabeceraDTO.getFacturaDetalleDTOCols()) && this.facturaCabeceraDTO.getId().getCodigoFactura() != null && existDetail) {
			this.setFacturaDetalleDTOCols(this.facturaCabeceraDTO.getFacturaDetalleDTOCols());
		}
		
		if(CollectionUtils.isEmpty(this.facturaDetalleDTOCols)) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.factura"));
		}else{
			Boolean ban = Boolean.FALSE;
			for(FacturaDetalleDTO facturaDetalleDTOAux : facturaDetalleDTOCols) {
				if(facturaDetalleDTOAux.getCantidad() != null && facturaDetalleDTOAux.getDescripcion() != null && facturaDetalleDTOAux.getValorUnidad() != null) {
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
	 * Metodo para cargar datos detalle factura
	 * @return
	 */
	public void cargarFacturaDetalle(ActionEvent e) {
		System.out.println("Ingreso a imprimir factura");
		this.setShowMessagesBar(Boolean.TRUE);
        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.despacho.mensaje.impresion.correcta"));
	}
	
	/**
	 * Metodo para cargar datos detalle factura
	 * @return
	 */
	public void simularEvento(ActionEvent e) {
		if(this.facturaCabeceraDTO != null && this.facturaCabeceraDTO.getTipoRuc() != null) {
			if(this.facturaCabeceraDTO.getTipoRuc().equals(ERPConstantes.TIPO_RUC_DOS)) {
				this.tipoRuc = ERPConstantes.ESTADO_ACTIVO_NUMERICO;
			}else {
				this.tipoRuc = ERPConstantes.ESTADO_INACTIVO_NUMERICO;
			}
		}
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para cargar datos detalle factura
	 * @return
	 */
	public void cargarDatosPago(ActionEvent e) {
		System.out.println("Cargar datos");
		this.totalPagado = BigDecimal.ZERO;
		this.pagosFacturaDTO = new PagosFacturaDTO();
		this.pagosFacturaDTO.setFechaPago(new Date());
		this.pagosFacturaDTOCols = ERPFactory.transaccion.getTransaccionServicio().findObtenerListaPagosFactura(1, facturaCabeceraDTO.getId().getCodigoFactura());
		if(CollectionUtils.isNotEmpty(this.pagosFacturaDTOCols)) {
			this.pagosFacturaDTOCols.stream().forEach(pago ->{
				this.totalPagado = this.totalPagado.add(pago.getValorPago());
			});
		}
		this.totalPendiente = this.facturaCabeceraDTO.getTotalCuenta().subtract(this.totalPagado);
		if(this.facturaCabeceraDTO.getPagado()) {
			this.tamanioPopUp = 310;
		}else {
			this.tamanioPopUp = 510;
		}
	}
	
	
	/**
	 * Metodo borrar pantalla y crear una nueva factura en venta
	 * @param e
	 */
	public void clearNuevaCuentaFacturaVentas(ActionEvent e){
		this.setShowMessagesBar(Boolean.FALSE);
		this.setDocumentoCreado(Boolean.FALSE);
		this.clienteDTO = new ClienteDTO();
		this.codigoClienteSeleccionado = null;
		this.codigoVendedorSeleccionado = null;
		this.numeroDocumentoBusqueda = null;
		this.nombreVendedorBusqueda = null;
		this.documentoClienteBusqueda = null;
		this.nombreClienteBusqueda = null;
		this.nombreVendedor = null;
		this.documentoVendedorBusqueda = null;
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		this.facturaDetalleDTO = new FacturaDetalleDTO();
		this.cuentasDataManager.setFacturaCabeceraDTOEditar(new FacturaCabeceraDTO());
		this.facturaDetalleDTOCols = new ArrayList<FacturaDetalleDTO>();
		this.cuentasDataManager.setTipoFactura(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		SecuenciaDTO secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_VENTA);
		this.facturaCabeceraDTO.setCodigoReferenciaFactura("FAC-"+secuenciaPedido.getValorSecuencia());
		this.facturaCabeceraDTO.setTipoRuc(ERPConstantes.TIPO_RUC_UNO);
		this.tipoRuc = "0";
		this.facturaCabeceraDTO.setFechaDocumento(new Date());
		this.facturaCabeceraDTO.setPagado(Boolean.FALSE);
		FacturaDetalleDTO detalle = null;
		contDetalle = 1;
		for(int i=0; i< 10; i++) {
			detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
			contDetalle++;
		}
	}
	
	/**
	 * Metodo borrar pantalla y crear una nueva factura en compra
	 * @param e
	 */
	public void clearNuevaCuentaFacturaCompra(ActionEvent e){
		this.setShowMessagesBar(Boolean.FALSE);
		this.setDocumentoCreado(Boolean.FALSE);
		this.codigoProveedorSeleccionado = null;
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		this.facturaCabeceraDTO.setFechaDocumento(new Date());
		this.facturaCabeceraDTO.setPagado(Boolean.FALSE);
		this.facturaDetalleDTO = new FacturaDetalleDTO();
		this.cuentasDataManager.setFacturaCabeceraDTOEditar(new FacturaCabeceraDTO());
		this.facturaDetalleDTOCols = new ArrayList<FacturaDetalleDTO>();
		this.cuentasDataManager.setTipoFactura(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
		SecuenciaDTO secuenciaPedido =  ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_COMPRA);
		this.facturaCabeceraDTO.setCodigoReferenciaFactura("DOC-"+secuenciaPedido.getValorSecuencia());
		FacturaDetalleDTO detalle = null;
		contDetalle = 1;
		for(int i=0; i< 10; i++) {
			detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
			contDetalle++;
		}
	}
	
	/**
	 * Metodo para eliminar registro 
	 * @param facturaDetalleDTO
	 */
	public void eliminarDetalleFactura(FacturaDetalleDTO facturaDetalleDTO) {
		facturaDetalleDTOCols.remove(facturaDetalleDTO);
		contDetalle = 1;
		facturaDetalleDTOCols.stream().forEach(detail ->{
			detail.getId().setCodigoCompania(contDetalle);
			contDetalle++;
		});
		
		FacturaDetalleDTO detalle = null;
		while(contDetalle < 11) {
			detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
			contDetalle++;
		}
		this.calcularTotalFactura();
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para calcular el total de la factura
	 */
	public void calcularTotalFactura() {
		this.facturaCabeceraDTO.setTotalCuenta(BigDecimal.ZERO);
		BigDecimal subTotal = BigDecimal.ZERO;
		BigDecimal totalIva = BigDecimal.ZERO;
		BigDecimal totalSinImpuesto = BigDecimal.ZERO;
		BigDecimal totalConImpuesto = BigDecimal.ZERO;
		BigDecimal totalFactura = BigDecimal.ZERO;
		BigDecimal totalDescuentos = BigDecimal.ZERO;
		for (FacturaDetalleDTO facturaDetalleDTO : facturaDetalleDTOCols) {
			if(facturaDetalleDTO.getSubTotal() != null) {
				subTotal = ValidationUtils.redondear(subTotal.add(facturaDetalleDTO.getSubTotal()), 4);
			}
			if(facturaDetalleDTO.getArticuloDTO() != null && facturaDetalleDTO.getArticuloDTO().getTieneImpuesto()){
				totalConImpuesto = ValidationUtils.redondear(totalConImpuesto.add(facturaDetalleDTO.getSubTotal()), 4);
				for(ArticuloImpuestoDTO impuesto : facturaDetalleDTO.getArticuloDTO().getArticuloImpuestoDTOCols()){
					totalIva = ValidationUtils.redondear(totalIva.add(BigDecimal.valueOf((facturaDetalleDTO.getSubTotal().doubleValue() * impuesto.getImpuestoDTO().getValorImpuesto().doubleValue())/ Double.valueOf(100))), 4);
				}
			}else {
				if(facturaDetalleDTO.getSubTotal() != null) {
					totalSinImpuesto = ValidationUtils.redondear(totalSinImpuesto.add(facturaDetalleDTO.getSubTotal()), 4);
				}
			}
			if(this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS)) {
				if(facturaDetalleDTO.getCantidad() != null && facturaDetalleDTO.getValorUnidad() != null && facturaDetalleDTO.getArticuloDTO() != null && facturaDetalleDTO.getArticuloDTO().getCosto() != null && facturaDetalleDTO.getValorUnidad().doubleValue() < facturaDetalleDTO.getArticuloDTO().getCosto().doubleValue()) {
					totalDescuentos = ValidationUtils.redondear(totalDescuentos.add(BigDecimal.valueOf(facturaDetalleDTO.getArticuloDTO().getCosto().subtract(facturaDetalleDTO.getValorUnidad()).doubleValue()*facturaDetalleDTO.getCantidad())), 4);
					facturaDetalleDTO.setDescuento(ValidationUtils.redondear(BigDecimal.valueOf(facturaDetalleDTO.getArticuloDTO().getCosto().subtract(facturaDetalleDTO.getValorUnidad()).doubleValue()*facturaDetalleDTO.getCantidad()), 4));
				}
			}else {
				if(facturaDetalleDTO.getCantidad() != null && facturaDetalleDTO.getValorUnidad() != null && facturaDetalleDTO.getArticuloDTO() != null && facturaDetalleDTO.getArticuloDTO().getPrecio() != null && facturaDetalleDTO.getArticuloDTO().getPrecioMinorista() != null) {
					if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)  && facturaDetalleDTO.getValorUnidad().doubleValue() < facturaDetalleDTO.getArticuloDTO().getPrecioMinorista().doubleValue()) {
						totalDescuentos = ValidationUtils.redondear(totalDescuentos.add(BigDecimal.valueOf(facturaDetalleDTO.getArticuloDTO().getPrecioMinorista().subtract(facturaDetalleDTO.getValorUnidad()).doubleValue()*facturaDetalleDTO.getCantidad())), 4);
						facturaDetalleDTO.setDescuento(ValidationUtils.redondear(BigDecimal.valueOf(facturaDetalleDTO.getArticuloDTO().getPrecioMinorista().subtract(facturaDetalleDTO.getValorUnidad()).doubleValue()*facturaDetalleDTO.getCantidad()), 4));
					}
					if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MAYORISTA)  && facturaDetalleDTO.getValorUnidad().doubleValue() < facturaDetalleDTO.getArticuloDTO().getPrecio().doubleValue()) {
						totalDescuentos = ValidationUtils.redondear(totalDescuentos.add(BigDecimal.valueOf(facturaDetalleDTO.getArticuloDTO().getPrecio().subtract(facturaDetalleDTO.getValorUnidad()).doubleValue()*facturaDetalleDTO.getCantidad())), 4);
						facturaDetalleDTO.setDescuento(ValidationUtils.redondear(BigDecimal.valueOf(facturaDetalleDTO.getArticuloDTO().getPrecio().subtract(facturaDetalleDTO.getValorUnidad()).doubleValue()*facturaDetalleDTO.getCantidad()), 4));
					}
				}
			}
		}
		totalFactura = ValidationUtils.redondear(subTotal.add(totalIva),4);
		this.facturaCabeceraDTO.setDescuento(totalDescuentos);
		this.facturaCabeceraDTO.setTotalSinImpuestos(totalSinImpuesto);
		this.facturaCabeceraDTO.setTotalImpuestos(totalConImpuesto);
		this.facturaCabeceraDTO.setSubTotal(subTotal);
		this.facturaCabeceraDTO.setTotalIva(totalIva);
		this.facturaCabeceraDTO.setTotalCuenta(totalFactura);
	}
	
	/**
	 * Calcular el total y subtotal
	 * @param e
	 */
	public void calcularTotalFacturaKeyUp(AjaxBehaviorEvent e) {
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto =  idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2])+1;

		for(FacturaDetalleDTO facturaDetalleDTOTemp : facturaDetalleDTOCols) {
			if(facturaDetalleDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(facturaDetalleDTOTemp.getCantidad() != null && facturaDetalleDTOTemp.getValorUnidad() != null && facturaDetalleDTOTemp.getCodigoArticuloUnidadManejo() != null) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = obtenerUnidadManejoPorCodigo(facturaDetalleDTOTemp.getCodigoArticuloUnidadManejo(), facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(facturaDetalleDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(facturaDetalleDTOTemp.getValorUnidad()), 4);
					facturaDetalleDTOTemp.setSubTotal(subTotal);
					facturaDetalleDTOTemp.setArticuloUnidadManejoDTO(articuloUnidadManejo);
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

		for(FacturaDetalleDTO facturaDetalleDTOTemp : facturaDetalleDTOCols) {
			if(facturaDetalleDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				
				Collection<ArticuloDTO> articuloCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, facturaDetalleDTOTemp.getCodigoBarras(), null);
				if(articuloCols.isEmpty()){
					this.setShowMessagesBar(Boolean.TRUE);
			        MensajesController.addInfo(null, "No existe articulo con el codigo de barras ingresado.");
				}else{
					facturaDetalleDTOTemp.setArticuloDTO(articuloCols.iterator().next());
					if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
						facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getPrecioMinorista());
					}else {
						facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getPrecio());
					}
					facturaDetalleDTOTemp.setDescripcion(facturaDetalleDTOTemp.getArticuloDTO().getNombreArticulo());
					facturaDetalleDTOTemp.setCodigoArticulo(facturaDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
					if(facturaDetalleDTOTemp.getCantidad() == null || facturaDetalleDTOTemp.getCantidad().intValue() == 0){
						facturaDetalleDTOTemp.setCantidad(1);
					}
					this.setShowMessagesBar(Boolean.FALSE);
				}
				
				if(facturaDetalleDTOTemp.getCantidad() != null && facturaDetalleDTOTemp.getValorUnidad() != null && CollectionUtils.isNotEmpty(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
					ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					facturaDetalleDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
					if(this.clienteDTO.getCodigoValorTipoCompra() != null && this.clienteDTO.getCodigoValorTipoCompra().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_CLIENTE_MINORISTA)) {
						facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getPrecioMinorista());
					}else {
						facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getPrecio());
					}
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(facturaDetalleDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(facturaDetalleDTOTemp.getValorUnidad()), 4);
					facturaDetalleDTOTemp.setSubTotal(subTotal);
					this.calcularTotalFactura();
				}
				break;
			}
		}
		// Validar filas llenas
		facturaDetalleDTOCols.forEach(detail ->{
			if(detail.getCodigoArticulo() == null) {
				this.crearNuevaFila = Boolean.FALSE;
			}
		});		
		if(facturaDetalleDTOCols.size() >= 20) {
			this.crearNuevaFila = Boolean.FALSE;
		}
		if(this.crearNuevaFila) {
			FacturaDetalleDTO detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
			contDetalle++;
		}
	}
	
	public void obtenerCodigoBarrasCompraEnter(AjaxBehaviorEvent e) {
		
		String idComponete = e.getComponent().getClientId();
		String[] idCompuesto =  idComponete.split(":");
		Integer numeroDetalle = Integer.parseInt(idCompuesto[2])+1;
		this.crearNuevaFila = Boolean.TRUE;

		for(FacturaDetalleDTO facturaDetalleDTOTemp : facturaDetalleDTOCols) {
			if(facturaDetalleDTOTemp.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				
				Collection<ArticuloDTO> articuloCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, facturaDetalleDTOTemp.getCodigoBarras(), null);
				if(articuloCols.isEmpty()){
					this.setShowMessagesBar(Boolean.TRUE);
			        MensajesController.addInfo(null, "No existe articulo con el codigo de barras ingresado.");
				}else{
					facturaDetalleDTOTemp.setArticuloDTO(articuloCols.iterator().next());
					facturaDetalleDTOTemp.setValorUnidad(facturaDetalleDTOTemp.getArticuloDTO().getCosto());
					facturaDetalleDTOTemp.setDescripcion(facturaDetalleDTOTemp.getArticuloDTO().getNombreArticulo());
					facturaDetalleDTOTemp.setCodigoArticulo(facturaDetalleDTOTemp.getArticuloDTO().getId().getCodigoArticulo());
					if(facturaDetalleDTOTemp.getCantidad() == null || facturaDetalleDTOTemp.getCantidad().intValue() == 0){
						facturaDetalleDTOTemp.setCantidad(1);
					}
					this.setShowMessagesBar(Boolean.FALSE);
				}
				
				if(facturaDetalleDTOTemp.getCantidad() != null && facturaDetalleDTOTemp.getValorUnidad() != null && CollectionUtils.isNotEmpty(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols())) {
 					ArticuloUnidadManejoDTO articuloUnidadManejo = this.obtenerUnidadManejoPorDefecto(facturaDetalleDTOTemp.getArticuloDTO().getArticuloUnidadManejoDTOCols());
					facturaDetalleDTOTemp.setCodigoArticuloUnidadManejo(articuloUnidadManejo.getId().getCodigoArticuloUnidadManejo());
					BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+(facturaDetalleDTOTemp.getCantidad().intValue()*articuloUnidadManejo.getValorUnidadManejo().intValue()))).multiply(facturaDetalleDTOTemp.getValorUnidad()), 4);
					facturaDetalleDTOTemp.setSubTotal(subTotal);
					this.calcularTotalFactura();
				}
				break;
			}
		}
		// Validar filas llenas
		facturaDetalleDTOCols.forEach(detail ->{
			if(detail.getCodigoArticulo() == null) {
				this.crearNuevaFila = Boolean.FALSE;
			}
		});
		if(facturaDetalleDTOCols.size() >= 20) {
			this.crearNuevaFila = Boolean.FALSE;
		}
		if(this.crearNuevaFila) {
			FacturaDetalleDTO detalle = new FacturaDetalleDTO();
			detalle.setArticuloDTO(new ArticuloDTO());
			detalle.getId().setCodigoCompania(contDetalle);
			this.facturaDetalleDTOCols.add(detalle);
			contDetalle++;
		}
	}
	
	public void procesarDetalle(ActionEvent e) {
		System.out.println("Prueba:"+facturaDetalleDTOCols.size());
	}
	
	/**
	 * Calcular el subtotal
	 * @param e
	 */
	public void calcularSubTotalFacturaKeyUp(AjaxBehaviorEvent e) {
		if(this.facturaDetalleDTO.getCantidad() != null && this.facturaDetalleDTO.getValorUnidad() != null) {
			BigDecimal subTotal = ValidationUtils.redondear(BigDecimal.valueOf(Double.valueOf(""+facturaDetalleDTO.getCantidad())).multiply(facturaDetalleDTO.getValorUnidad()), 4);
			this.facturaDetalleDTO.setSubTotal(subTotal);			
		}
	}
	
	/**
	 * Metodo para buscar vendedores
	 * @param e
	 */
	public void busquedaClientes(ActionEvent e){
		this.buscarClientes();
	}
	
	/**
	 * Metodo para buscar vendedores al dar enter
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
			this.numeroDocumentoBusqueda = null;
			this.nombreVendedorBusqueda = null;
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
	 * Seleccionar cliente del popUp
	 * @param e
	 */
	public void seleccionCliente(ValueChangeEvent e){
		this.codigoClienteSeleccionado = (Long)e.getNewValue();
	}
	
	/**
	 * Metodo para buscar clientes
	 * @param e
	 */
	public void busquedaProveedores(ActionEvent e){
		try {
			this.proveedorDTOCols = ERPFactory.proveedor.getProveedorServicio().findObtenerListaProveedores(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
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
	 * Seleccionar proveedor del popUp
	 * @param e
	 */
	public void seleccionProveedor(ValueChangeEvent e)
	{
		this.codigoProveedorSeleccionado = (Long)e.getNewValue();
	}

	/**
	 * Metodo para agragar el cliente a la vista
	 */
	public void agragarCliente(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoCliente", PredicateUtils.equalPredicate(this.codigoClienteSeleccionado));
		// Validacion de objeto existente
		this.clienteDTO  = (ClienteDTO) CollectionUtils.find(this.clienteDTOCols, testPredicate);
		this.facturaCabeceraDTO.setRucDocumento(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getNumeroRuc() : this.clienteDTO.getPersonaDTO().getNumeroDocumento());
		this.facturaCabeceraDTO.setNombreClienteProveedor(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getRazonSocial() : this.clienteDTO.getPersonaDTO().getNombreCompleto());
		this.facturaCabeceraDTO.setDireccion(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getDireccionPrincipal() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getDireccionPrincipal());
		this.facturaCabeceraDTO.setCiudad(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getCiudad() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getCiudad());
		this.facturaCabeceraDTO.setTelefono(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getTelefonoPrincipal() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getTelefonoPrincipal());
		this.facturaCabeceraDTO.setEmail(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getEmail() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getEmail());
		this.facturaCabeceraDTO.setTipoCliente(this.clienteDTO.getCodigoValorTipoCompra());
		if(CollectionUtils.isNotEmpty(this.facturaDetalleDTOCols)) {
			this.facturaDetalleDTOCols.stream().forEach(detalleFact ->{
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
	 * Metodo para agragar el proveedor a la vista
	 */
	public void agragarProveedor(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoProveedor", PredicateUtils.equalPredicate(this.codigoProveedorSeleccionado));
		// Validacion de objeto existente
		ProveedorDTO proveedorDTO  = (ProveedorDTO) CollectionUtils.find(this.proveedorDTOCols, testPredicate);
		this.facturaCabeceraDTO.setRucDocumento(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getNumeroRuc() : proveedorDTO.getPersonaDTO().getNumeroDocumento());
		this.facturaCabeceraDTO.setNombreClienteProveedor(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getRazonSocial() : proveedorDTO.getPersonaDTO().getNombreCompleto());
		this.facturaCabeceraDTO.setDireccion(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getContactoEmpresaDTO().getDireccionPrincipal() : proveedorDTO.getPersonaDTO().getContactoPersonaDTO().getDireccionPrincipal());
		this.facturaCabeceraDTO.setTelefono(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getContactoEmpresaDTO().getTelefonoPrincipal() : proveedorDTO.getPersonaDTO().getContactoPersonaDTO().getTelefonoPrincipal());
	}
	
	/**
	 * Metodo para agragar el vendedor a la vista
	 */
	public void agragarVendedor(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoVendedor", PredicateUtils.equalPredicate(this.codigoVendedorSeleccionado));
		// Validacion de objeto existente
		VendedorDTO vendedorDTO  = (VendedorDTO) CollectionUtils.find(this.vendedorDTOCols, testPredicate);
		this.facturaCabeceraDTO.setVendedorDTO(vendedorDTO);
		this.facturaCabeceraDTO.setCodigoVendedor(vendedorDTO.getId().getCodigoVendedor());
		this.nombreVendedor = vendedorDTO.getPersonaDTO().getNombreCompleto();
	}
	
	/**
	 * Metodo para consultar cliente por numero de documento 
	 * @param e
	 */
	public void realizarConsultaClientes(AjaxBehaviorEvent e) {
		if(StringUtils.isEmpty(this.facturaCabeceraDTO.getRucDocumento())) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, "El campo no debe estar vacio, ingrese el documento del cliente que desea buscar.");
		}else {
			String numeroDocumento = this.facturaCabeceraDTO.getRucDocumento();
			this.clienteDTO = ERPFactory.clientes.getClientesServicio().findObtenerClienteByCodigo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumento, null);
			if(this.clienteDTO != null) {
				this.facturaCabeceraDTO.setRucDocumento(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getNumeroRuc() : this.clienteDTO.getPersonaDTO().getNumeroDocumento());
				this.facturaCabeceraDTO.setNombreClienteProveedor(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getRazonSocial() : this.clienteDTO.getPersonaDTO().getNombreCompleto());
				this.facturaCabeceraDTO.setDireccion(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getDireccionPrincipal() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getDireccionPrincipal());
				this.facturaCabeceraDTO.setCiudad(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getCiudad() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getCiudad());
				this.facturaCabeceraDTO.setTelefono(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getTelefonoPrincipal() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getTelefonoPrincipal());
				this.facturaCabeceraDTO.setEmail(this.clienteDTO.getPersonaDTO() == null ? this.clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getEmail() : this.clienteDTO.getPersonaDTO().getContactoPersonaDTO().getEmail());
				this.facturaCabeceraDTO.setTipoCliente(this.clienteDTO.getCodigoValorTipoCompra());
				this.setShowMessagesBar(Boolean.FALSE);
				if(CollectionUtils.isNotEmpty(this.facturaDetalleDTOCols)) {
					this.facturaDetalleDTOCols.stream().forEach(detalleFact ->{
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
	 * Metodo para consultar proveedor por numero de documento 
	 * @param e
	 */
	public void realizarConsultaProveedor(AjaxBehaviorEvent e) {
		if(StringUtils.isEmpty(this.facturaCabeceraDTO.getRucDocumento())) {
			this.setShowMessagesBar(Boolean.TRUE);
	        MensajesController.addError(null, "El campo no debe estar vacio, ingrese el documento del proveedor que desea buscar.");
		}else {
			String numeroDocumento = this.facturaCabeceraDTO.getRucDocumento();
			ProveedorDTO proveedorDTO = ERPFactory.proveedor.getProveedorServicio().findObtenerProveedor(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumento, null);
			if(proveedorDTO != null) {
				this.facturaCabeceraDTO.setRucDocumento(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getNumeroRuc() : proveedorDTO.getPersonaDTO().getNumeroDocumento());
				this.facturaCabeceraDTO.setNombreClienteProveedor(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getRazonSocial() : proveedorDTO.getPersonaDTO().getNombreCompleto());
				this.facturaCabeceraDTO.setDireccion(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getContactoEmpresaDTO().getDireccionPrincipal() : proveedorDTO.getPersonaDTO().getContactoPersonaDTO().getDireccionPrincipal());
				this.facturaCabeceraDTO.setTelefono(proveedorDTO.getPersonaDTO() == null ? proveedorDTO.getEmpresaDTO().getContactoEmpresaDTO().getTelefonoPrincipal() : proveedorDTO.getPersonaDTO().getContactoPersonaDTO().getTelefonoPrincipal());
				this.setShowMessagesBar(Boolean.FALSE);
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
		        MensajesController.addWarn(null, "No se encontr\u00F3 el proveedor con el documento ingresado.");
			}
		}
	}
	
	
	/**
	 * Agregar nueva fila factura
	 * @param e
	 */
	public void agragarRegistroDetalleFactura(ActionEvent e) {
		FacturaDetalleDTO detalle = new FacturaDetalleDTO();
		detalle.getId().setCodigoCompania(contDetalle);
		detalle.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
		detalle.setCantidad(this.facturaDetalleDTO.getCantidad());
		detalle.setDescripcion(this.facturaDetalleDTO.getDescripcion());
		detalle.setValorUnidad(this.facturaDetalleDTO.getValorUnidad());
		detalle.setCodigoArticulo(this.facturaDetalleDTO.getCodigoArticulo());
		detalle.setArticuloDTO(this.facturaDetalleDTO.getArticuloDTO());
		detalle.setSubTotal(this.facturaDetalleDTO.getSubTotal());
		this.facturaDetalleDTOCols.add(detalle);
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
	public void abrirPopUpDetalleFactura(ActionEvent e) {
		this.facturaDetalleDTO = new FacturaDetalleDTO();
	}

	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public void calcularValoresArticulo(){
		for(ArticuloDTO articuloDTOSearch : this.articuloDTOCols) {
			if(facturaDetalleDTO.getDescripcion().equals(articuloDTOSearch.getNombreArticulo())) {
				this.facturaDetalleDTO.setArticuloDTO(articuloDTOSearch);
				this.facturaDetalleDTO.setCodigoArticulo(articuloDTOSearch.getId().getCodigoArticulo());
				this.facturaDetalleDTO.setValorUnidad(articuloDTOSearch.getPrecio());
				this.facturaDetalleDTO.setSubTotal(BigDecimal.valueOf(facturaDetalleDTO.getCantidad()*facturaDetalleDTO.getValorUnidad().doubleValue()));
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
	 * Metodo para regresar a la busqueda de facturas de ventas
	 * @param e
	 */
	public String regresarBusquedaFacturasVentas(){
		this.setDocumentoCreado(Boolean.FALSE);
		this.cuentasDataManager.setFacturaCabeceraDTOEditar(new FacturaCabeceraDTO());
		return "/modules/ventas/adminBusquedaVentas.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para regresar a la busqueda de facturas de compras
	 * @param e
	 */
	public String regresarBusquedaFacturasCompras(){
		this.setDocumentoCreado(Boolean.FALSE);
		this.cuentasDataManager.setFacturaCabeceraDTOEditar(new FacturaCabeceraDTO());
		return "/modules/compras/adminBusquedaCompras.xhtml?faces-redirect=true";
	}
	
	
	/**
	 * Metodo para ir a la pantalla de nuevo factura compra
	 * @return
	 */
	public String crearNuevaCuentaFacturaCompra(){
		this.setDocumentoCreado(Boolean.FALSE);
		facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
		this.cuentasDataManager.setTipoFactura(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
		this.cuentasDataManager.setFacturaCabeceraDTOEditar(new FacturaCabeceraDTO());
		return "/modules/compras/nuevaFacturaCompra.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nueva factura ventas
	 * @return
	 */
	public String crearNuevaCuentaFacturaVenta(){
		this.setDocumentoCreado(Boolean.FALSE);
		facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		this.cuentasDataManager.setTipoFactura(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		this.cuentasDataManager.setFacturaCabeceraDTOEditar(new FacturaCabeceraDTO());
		return "/modules/ventas/nuevaFacturaVenta.xhtml?faces-redirect=true";
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
		this.numeroFactura = "";
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
	 * Borrar filtro de busqueda por documento vendedor
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
	
	public void borrarBusquedaVendedor(ActionEvent e){
		this.documentoVendedorBusqueda = "";
		this.setShowMessagesBar(Boolean.FALSE);
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public String imprimirListaFacturas() {
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
			params.put("TOTAL_ABONOS", formatoDecimales.format(this.totalPagos));
			params.put("TOTAL_SALDOS", formatoDecimales.format(this.totalCuenta.subtract(this.totalPagos)));
			params.put("TOTAL_VENTA", formatoDecimales.format(this.totalCuenta));
			String tituloReporte = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("tituloReporte");
			params.put("TITULO", tituloReporte);
			contenido = UtilitarioReportesWeb.generarReporteVentas(this.facturaCabeceraDTOCols, params);
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
	
	public void imprimirFacturaImpresora(ActionEvent e){
		try {
			boolean ban = true;
			Collection<FacturaDetalleDTO> detallesCompletos = this.facturaCabeceraDTO.getFacturaDetalleDTOCols().stream().filter(detail -> detail.getCodigoArticulo() != null && detail.getId().getCodigoDetalleFactura() != null).collect(Collectors.toList());
			this.facturaCabeceraDTO.setFacturaDetalleDTOCols(detallesCompletos);
			for(FacturaDetalleDTO detalle : this.facturaCabeceraDTO.getFacturaDetalleDTOCols()) {
				if(detalle == null || detalle.getCantidad() == null) {
					ban = false;
				}
			}
			if(ban) {
				this.facturaCabeceraDTO.setFacturaDetalleDTOCols(this.facturaCabeceraDTO.getFacturaDetalleDTOCols().stream().sorted(Comparator.comparing(FacturaDetalleDTO::getOrdenRegistro)).collect(Collectors.toList()));
			}	
			if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA)) {
				byte[] contenido = NotaVentaUtil.generarNotaVenta(facturaCabeceraDTO);
				impresionComprobante(contenido);
				MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.despacho.mensaje.impresion.correcta"));
			}else if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS)){
				byte[] xmlDocument = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerXmlDocumentoFactura(ERPConstantes.CODIGO_COMPANIA, this.facturaCabeceraDTO.getId().getCodigoFactura());
				if(xmlDocument != null){
					byte[] contenido = FacturaElectronicaUtil.imprimirFacturaFormato(xmlDocument);
					impresionComprobante(contenido);
					MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.despacho.mensaje.impresion.correcta"));
				}else{
					MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.mensaje.error.impresion.factura"));
				}
			}
			this.clearNuevaCuentaFacturaVentas(e);	
			this.setShowMessagesBar(Boolean.TRUE);
		} catch (Exception execption) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir documento seleccionado.");
			execption.printStackTrace();
		}
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public void imprimirFactura() {
		//HtmlPdf htmltoPDF;
		try {
			if(this.validarInformacionRequerida()) {
				// Plantilla rpincipal que permite la conversion de xsl a pdf
				//htmltoPDF = new HtmlPdf(ERPConstantes.PLANTILLA_XSL_FOPRINCIPAL);
				//HashMap<String , String> parametros = new HashMap<String, String>();
//				byte contenido[] = htmltoPDF.convertir(ERPFactory.facturas.getFacturaCabeceraServicio().finObtenerXMLImprimirFacturaVenta(facturaCabeceraDTO).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""), "", "",	parametros,	null);
//				byte[] contenido = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerNotaVenta(facturaCabeceraDTO);
//				byte[] xmlDocument = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerXmlDocumentoFactura(ERPConstantes.CODIGO_COMPANIA, this.facturaCabeceraDTO.getId().getCodigoFactura());
//				byte[] contenido = FacturaElectronocaUtil.imprimirFacturaFormato(xmlDocument);
				byte[] contenido;
				byte[] xmlDocument = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerXmlDocumentoFactura(ERPConstantes.CODIGO_COMPANIA, this.facturaCabeceraDTO.getId().getCodigoFactura());
				if(xmlDocument != null){
					contenido = FacturaElectronicaUtil.imprimirFacturaFormato(xmlDocument);
				}else{
					contenido = NotaVentaUtil.generarNotaVenta(facturaCabeceraDTO);
				}
				UtilitarioWeb.mostrarPDF(contenido);
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
		if(this.facturaCabeceraDTO == null) {
			return null;
		}else{
			this.cuentasDataManager.setFacturaCabeceraDTOEditar(this.facturaCabeceraDTO);
			if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS)) {
				return "/modules/facturas/nuevaFacturaVenta.xhtml?faces-redirect=true";
			}
			if(this.facturaCabeceraDTO.getCodigoValorTipoDocumento().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS)) {
				return "/modules/facturas/nuevaFacturaCompra.xhtml?faces-redirect=true";
			}
			return null;
		}
	}
	
	public void setCuentasDataManager(CuentasDataManager cuentasDataManager) {
		this.cuentasDataManager = cuentasDataManager;
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

	public FacturaCabeceraDTO getFacturaCabeceraDTO() {
		return facturaCabeceraDTO;
	}

	public void setFacturaCabeceraDTO(FacturaCabeceraDTO facturaCabeceraDTO) {
		this.facturaCabeceraDTO = facturaCabeceraDTO;
	}

	public FacturaDetalleDTO getFacturaDetalleDTO() {
		return facturaDetalleDTO;
	}

	public void setFacturaDetalleDTO(FacturaDetalleDTO facturaDetalleDTO) {
		this.facturaDetalleDTO = facturaDetalleDTO;
	}

	public Collection<FacturaDetalleDTO> getFacturaDetalleDTOCols() {
		return facturaDetalleDTOCols;
	}

	public void setFacturaDetalleDTOCols(Collection<FacturaDetalleDTO> facturaDetalleDTOCols) {
		this.facturaDetalleDTOCols = facturaDetalleDTOCols;
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

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
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

	public Long getCodigoProveedorSeleccionado() {
		return codigoProveedorSeleccionado;
	}

	public void setCodigoProveedorSeleccionado(Long codigoProveedorSeleccionado) {
		this.codigoProveedorSeleccionado = codigoProveedorSeleccionado;
	}

	public Collection<ProveedorDTO> getProveedorDTOCols() {
		return proveedorDTOCols;
	}

	public void setProveedorDTOCols(Collection<ProveedorDTO> proveedorDTOCols) {
		this.proveedorDTOCols = proveedorDTOCols;
	}

	public Boolean getCrearNuevaFila() {
		return crearNuevaFila;
	}

	public void setCrearNuevaFila(Boolean crearNuevaFila) {
		this.crearNuevaFila = crearNuevaFila;
	}

	public PagosFacturaDTO getPagosFacturaDTO() {
		return pagosFacturaDTO;
	}

	public void setPagosFacturaDTO(PagosFacturaDTO pagosFacturaDTO) {
		this.pagosFacturaDTO = pagosFacturaDTO;
	}

	public Collection<PagosFacturaDTO> getPagosFacturaDTOCols() {
		return pagosFacturaDTOCols;
	}

	public void setPagosFacturaDTOCols(Collection<PagosFacturaDTO> pagosFacturaDTOCols) {
		this.pagosFacturaDTOCols = pagosFacturaDTOCols;
	}

	public BigDecimal getTotalPagado() {
		return totalPagado;
	}

	public void setTotalPagado(BigDecimal totalPagado) {
		this.totalPagado = totalPagado;
	}

	public BigDecimal getTotalPendiente() {
		return totalPendiente;
	}

	public void setTotalPendiente(BigDecimal totalPendiente) {
		this.totalPendiente = totalPendiente;
	}

	public Integer getTamanioPopUp() {
		return tamanioPopUp;
	}

	public void setTamanioPopUp(Integer tamanioPopUp) {
		this.tamanioPopUp = tamanioPopUp;
	}

	public BigDecimal getTotalCuenta() {
		return totalCuenta;
	}

	public void setTotalCuenta(BigDecimal totalCuenta) {
		this.totalCuenta = totalCuenta;
	}

	public String getTipoRuc() {
		return tipoRuc;
	}

	public void setTipoRuc(String tipoRuc) {
		this.tipoRuc = tipoRuc;
	}

	public Collection<VendedorDTO> getVendedorDTOCols() {
		return vendedorDTOCols;
	}

	public void setVendedorDTOCols(Collection<VendedorDTO> vendedorDTOCols) {
		this.vendedorDTOCols = vendedorDTOCols;
	}

	public Long getCodigoVendedorSeleccionado() {
		return codigoVendedorSeleccionado;
	}

	public void setCodigoVendedorSeleccionado(Long codigoVendedorSeleccionado) {
		this.codigoVendedorSeleccionado = codigoVendedorSeleccionado;
	}

	public String getNumeroDocumentoBusqueda() {
		return numeroDocumentoBusqueda;
	}

	public void setNumeroDocumentoBusqueda(String numeroDocumentoBusqueda) {
		this.numeroDocumentoBusqueda = numeroDocumentoBusqueda;
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

	public Collection<String> getTiposDocumentos() {
		return tiposDocumentos;
	}

	public void setTiposDocumentos(Collection<String> tiposDocumentos) {
		this.tiposDocumentos = tiposDocumentos;
	}

	public BigDecimal getTotalPagos() {
		return totalPagos;
	}

	public void setTotalPagos(BigDecimal totalPagos) {
		this.totalPagos = totalPagos;
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

	public Long getCodigoVendedor() {
		return codigoVendedor;
	}

	public void setCodigoVendedor(Long codigoVendedor) {
		this.codigoVendedor = codigoVendedor;
	}

	public Boolean getCrearFacturaElectronica() {
		return crearFacturaElectronica;
	}

	public void setCrearFacturaElectronica(Boolean crearFacturaElectronica) {
		this.crearFacturaElectronica = crearFacturaElectronica;
	}

	public String getDocumentoVendedorBusqueda() {
		return documentoVendedorBusqueda;
	}

	public void setDocumentoVendedorBusqueda(String documentoVendedorBusqueda) {
		this.documentoVendedorBusqueda = documentoVendedorBusqueda;
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
}
