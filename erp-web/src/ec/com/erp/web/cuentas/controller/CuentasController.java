
package ec.com.erp.web.cuentas.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.ClienteDTO;
import ec.com.erp.cliente.mdl.dto.FacturaCabeceraDTO;
import ec.com.erp.cliente.mdl.dto.FacturaDetalleDTO;
import ec.com.erp.cliente.mdl.dto.ProveedorDTO;
import ec.com.erp.cliente.mdl.dto.SecuenciaDTO;
import ec.com.erp.cliente.mdl.dto.id.FacturaCabeceraID;
import ec.com.erp.utilitario.commons.util.HtmlPdf;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.commons.utils.UtilitarioWeb;
import ec.com.erp.web.commons.utils.XML_Utilidades;
import ec.com.erp.web.cuentas.datamanager.CuentasDataManager;

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
	private Collection<FacturaDetalleDTO> facturaDetalleDTOCols;
	private Collection<FacturaCabeceraDTO> facturaCabeceraDTOCols;
	private Collection<ClienteDTO> clienteDTOCols;
	private Collection<ArticuloDTO> articuloDTOCols;
	
	// Data Managers
	@ManagedProperty(value="#{cuentasDataManager}")
	private CuentasDataManager cuentasDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
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
	private Integer contDetalle;
	private String codigoBarras;
	private Boolean documentoCreado;

	@PostConstruct
	public void postConstruct() {
		this.loginController.activarMenusSeleccionado();
		this.documentoCreado = Boolean.FALSE;
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		this.facturaCabeceraDTO.setFechaDocumento(new Date());
		this.facturaCabeceraDTO.setPagado(Boolean.FALSE);
		this.facturaDetalleDTO = new FacturaDetalleDTO();
		this.facturaCabeceraDTOCols = new ArrayList<FacturaCabeceraDTO>();
		this.facturaDetalleDTOCols = new ArrayList<FacturaDetalleDTO>();
		this.clienteDTOCols =  new ArrayList<ClienteDTO>();
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
		// Inicializar fechas para filtros de busqueda
		Calendar fechaInferior = Calendar.getInstance();
		fechaInferior.set(Calendar.MONTH, 0);
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		Calendar fechaSuperior = Calendar.getInstance();
		fechaFacturaInicio = fechaInferior.getTime();
		fechaFacturaFin = fechaSuperior.getTime();
		
		this.tipoFacturaCatalogoValorDTOCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_DOCUMENTOS);
		this.articuloDTOCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, null);
		
		// Cargar datos a editar
		if(cuentasDataManager.getFacturaCabeceraDTOEditar() != null && cuentasDataManager.getFacturaCabeceraDTOEditar().getId().getCodigoFactura() != null){
			this.setFacturaCabeceraDTO(cuentasDataManager.getFacturaCabeceraDTOEditar());
			this.setFacturaDetalleDTOCols(cuentasDataManager.getFacturaCabeceraDTOEditar().getFacturaDetalleDTOCols());
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/ventas/adminBusquedaVentas.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tipoDocumento);
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/compras/adminBusquedaCompras.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS;
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tipoDocumento);
		}
		
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/cuentas/adminBusquedaCuentas.xhtml")) {
			tipoDocumento = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
			pagado = Boolean.FALSE;
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, null, null, docClienteProveedor, nombClienteProveedor, pagado, tipoDocumento);
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
			Calendar fechaInicio = Calendar.getInstance();
			Calendar fechaFin = Calendar.getInstance();
			fechaInicio.setTime(fechaFacturaInicio);
			fechaFin.setTime(fechaFacturaFin);
			UtilitarioWeb.cleanDate(fechaInicio);
			UtilitarioWeb.cleanDate(fechaFin);
			fechaFin.add(Calendar.DATE, 1);
			
			this.facturaCabeceraDTOCols = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerListaFacturas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroFactura, new Timestamp(fechaInicio.getTime().getTime()), new Timestamp(fechaFin.getTime().getTime()), docClienteProveedor, nombClienteProveedor, pagado, tipoDocumento);
			if(CollectionUtils.isEmpty(this.facturaCabeceraDTOCols)){
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
					this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
				}
				if(this.cuentasDataManager.getTipoFactura() != null && this.cuentasDataManager.getTipoFactura().equals(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS)) {
					this.facturaCabeceraDTO.setCodigoValorTipoDocumento(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
				}
				
				this.facturaCabeceraDTO.getId().setCodigoCompania(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
				this.facturaCabeceraDTO.setUsuarioRegistro(loginController.getUsuariosDTO().getId().getUserId());
				this.facturaCabeceraDTO.setFacturaDetalleDTOCols(facturaDetalleDTOCols);
				ERPFactory.facturas.getFacturaCabeceraServicio().transGuardarActualizarFacturaCabecera(this.facturaCabeceraDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setDocumentoCreado(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.lista.cabecera.factura.mensaje.guardado"));
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
	 * Metodo para cancelar factura ventas
	 * @param e
	 */
	public void cancelarFacturasVentas(ActionEvent e){
		try {
			ERPFactory.facturas.getFacturaCabeceraServicio().transCancelarFacturaInactivar(this.facturaCabeceraDTO);
			String tipoFactura = ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS;
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
		if(StringUtils.isEmpty(this.facturaCabeceraDTO.getNumeroDocumento())) {
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.numero.factura"));
		}
		if(StringUtils.isEmpty(this.facturaCabeceraDTO.getRucDocumento())) {
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.ruccliente.factura"));
		}
		if(StringUtils.isEmpty(this.facturaCabeceraDTO.getNombreClienteProveedor())) {
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.campo.requerido.nombrecliente.factura"));
		}
		if(CollectionUtils.isEmpty(this.facturaDetalleDTOCols)) {
			valido = Boolean.FALSE;
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.label.nuevo.pedidos.mensaje.requerido.factura"));
		}else
		{
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
		System.out.println("Ingreso a cargar detalle");
	}
	
	
	/**
	 * Metodo borrar pantalla y crear una nueva factura en venta
	 * @param e
	 */
	public void clearNuevaCuentaFacturaVentas(ActionEvent e){
		this.setDocumentoCreado(Boolean.FALSE);
		this.cuentasDataManager.setTipoFactura(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		SecuenciaDTO secuenciaPedido = ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_VENTA);
		this.facturaCabeceraDTO.setCodigoReferenciaFactura("FAC-"+secuenciaPedido.getValorSecuencia());
		
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		this.facturaDetalleDTO = new FacturaDetalleDTO();
		this.facturaDetalleDTOCols = new ArrayList<FacturaDetalleDTO>();
	}
	
	/**
	 * Metodo borrar pantalla y crear una nueva factura en compra
	 * @param e
	 */
	public void clearNuevaCuentaFacturaCompra(ActionEvent e){
		this.setDocumentoCreado(Boolean.FALSE);
		this.cuentasDataManager.setTipoFactura(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
		SecuenciaDTO secuenciaPedido =  ERPFactory.secuencias.getSecuenciaServicio().findObtenerSecuenciaByNombre(FacturaCabeceraID.NOMBRE_SECUENCIA_COMPRA);
		this.facturaCabeceraDTO.setCodigoReferenciaFactura("DOC-"+secuenciaPedido.getValorSecuencia());
		
		this.facturaCabeceraDTO = new FacturaCabeceraDTO();
		this.facturaDetalleDTO = new FacturaDetalleDTO();
		this.facturaDetalleDTOCols = new ArrayList<FacturaDetalleDTO>();
	}
	
	/**
	 * Metodo para eliminar registro 
	 * @param facturaDetalleDTO
	 */
	public void eliminarDetalleFactura(FacturaDetalleDTO facturaDetalleDTO) {
		facturaDetalleDTOCols.remove(facturaDetalleDTO);
		this.calcularTotalFactura();
	}
	
	/**
	 * Metodo para calcular el total de la factura
	 */
	public void calcularTotalFactura() {
		this.facturaCabeceraDTO.setTotalCuenta(BigDecimal.ZERO);
		BigDecimal totalFactura = BigDecimal.ZERO;
		for (FacturaDetalleDTO facturaDetalleDTO : facturaDetalleDTOCols) {
			if(facturaDetalleDTO.getSubTotal() != null) {
				totalFactura = totalFactura.add(facturaDetalleDTO.getSubTotal());
			}
		}
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

		for(FacturaDetalleDTO facturaDetalleDTO : facturaDetalleDTOCols) {
			if(facturaDetalleDTO.getId().getCodigoCompania().intValue() == numeroDetalle.intValue()) {
				if(facturaDetalleDTO.getCantidad() != null && facturaDetalleDTO.getValorUnidad() != null) {
					BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+facturaDetalleDTO.getCantidad())).multiply(facturaDetalleDTO.getValorUnidad());
					facturaDetalleDTO.setSubTotal(subTotal);
					this.calcularTotalFactura();
				}
				break;
			}
		}
	}
	
	/**
	 * Calcular el subtotal
	 * @param e
	 */
	public void calcularSubTotalFacturaKeyUp(AjaxBehaviorEvent e) {
		if(this.facturaDetalleDTO.getCantidad() != null && this.facturaDetalleDTO.getValorUnidad() != null) {
			BigDecimal subTotal = BigDecimal.valueOf(Double.valueOf(""+facturaDetalleDTO.getCantidad())).multiply(facturaDetalleDTO.getValorUnidad());
			this.facturaDetalleDTO.setSubTotal(subTotal);			
		}
	}
	
	/**
	 * Metodo para buscar clientes
	 * @param e
	 */
	public void busquedaClientes(ActionEvent e){
		try {
			this.clienteDTOCols = ERPFactory.clientes.getClientesServicio().findObtenerListaClientes(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
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
	public void seleccionCliente(ValueChangeEvent e)
	{
		this.codigoClienteSeleccionado = (Long)e.getNewValue();
	}

	/**
	 * Metodo para agragar el cliente a la vista
	 */
	public void agragarCliente(ActionEvent e) {
		// Verificar si existe en la coleccion el cliente
		Predicate testPredicate = new BeanPredicate("id.codigoCliente", PredicateUtils.equalPredicate(this.codigoClienteSeleccionado));
		// Validacion de objeto existente
		ClienteDTO clienteDTO  = (ClienteDTO) CollectionUtils.find(this.clienteDTOCols, testPredicate);
		this.facturaCabeceraDTO.setRucDocumento(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getNumeroRuc() : clienteDTO.getPersonaDTO().getNumeroDocumento());
		this.facturaCabeceraDTO.setNombreClienteProveedor(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getRazonSocial() : clienteDTO.getPersonaDTO().getNombreCompleto());
		this.facturaCabeceraDTO.setDireccion(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getDireccionPrincipal() : clienteDTO.getPersonaDTO().getContactoPersonaDTO().getDireccionPrincipal());
		this.facturaCabeceraDTO.setTelefono(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getTelefonoPrincipal() : clienteDTO.getPersonaDTO().getContactoPersonaDTO().getTelefonoPrincipal());
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
			ClienteDTO clienteDTO = ERPFactory.clientes.getClientesServicio().findObtenerClienteByCodigo(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), numeroDocumento, null);
			if(clienteDTO != null) {
				this.facturaCabeceraDTO.setRucDocumento(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getNumeroRuc() : clienteDTO.getPersonaDTO().getNumeroDocumento());
				this.facturaCabeceraDTO.setNombreClienteProveedor(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getRazonSocial() : clienteDTO.getPersonaDTO().getNombreCompleto());
				this.facturaCabeceraDTO.setDireccion(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getDireccionPrincipal() : clienteDTO.getPersonaDTO().getContactoPersonaDTO().getDireccionPrincipal());
				this.facturaCabeceraDTO.setTelefono(clienteDTO.getPersonaDTO() == null ? clienteDTO.getEmpresaDTO().getContactoEmpresaDTO().getTelefonoPrincipal() : clienteDTO.getPersonaDTO().getContactoPersonaDTO().getTelefonoPrincipal());
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
		        MensajesController.addWarn(null, "No se encontr\u00F3 el cliente con el documento ingresado.");
			}
		}
	}
	
	/**
	 * Metodo para consultar cliente por numero de documento 
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
	}
	
	/**
	 * Borrar filtro fecha factura
	 */
	public void borrarBusquedaFechaFactura(ActionEvent e){
		this.fechaFacturaInicio = new Date();
		this.fechaFacturaFin = new Date();
	}
	
	/**
	 * Borrar filtro documento cliente o proveedor
	 */
	public void borrarBusquedaDocumento(ActionEvent e){
		this.docClienteProveedor = "";
	}
	
	/**
	 * Borrar filtro nombre cliente o proveedor
	 */
	public void borrarBusquedaClienteProveedor(ActionEvent e){
		this.nombClienteProveedor = "";
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public String imprimirListaFacturas() {
		HtmlPdf htmltoPDF;
		try {
//			PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
//	 
//			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
//			DocPrintJob docPrintJob = printService.createPrintJob();
			
			// Plantilla rpincipal que permite la conversion de xsl a pdf
			htmltoPDF = new HtmlPdf(ERPConstantes.PLANTILLA_XSL_FOPRINCIPAL);
			HashMap<String , String> parametros = new HashMap<String, String>();
			byte contenido[] = htmltoPDF.convertir(ERPFactory.facturas.getFacturaCabeceraServicio().finObtenerXMLReporteFacturas(facturaCabeceraDTOCols).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""), "", "",	parametros,	null);
			UtilitarioWeb.mostrarPDF(contenido);
			
//			Doc doc = new SimpleDoc(contenido, flavor, null);
//			docPrintJob.print(doc, null);
			
//		} catch (PrintException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, "Error al imprimir");
		}
		return null;
	}
	
	/**
	 * Metodo para imprimir lista de facturas
	 */
	public void imprimirFactura() {
//		HtmlPdf htmltoPDF;
		try {
			if(this.validarInformacionRequerida()) {
				
				String texto = "Esto es lo que va a la impresora";
				 
				PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
		 
				DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
				DocPrintJob docPrintJob = printService.createPrintJob();
				Doc doc = new SimpleDoc(texto.getBytes(), flavor, null);
				try {
					docPrintJob.print(doc, null);
				} catch (PrintException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Plantilla rpincipal que permite la conversion de xsl a pdf
//				htmltoPDF = new HtmlPdf(ERPConstantes.PLANTILLA_XSL_FOPRINCIPAL);
//				HashMap<String , String> parametros = new HashMap<String, String>();
//				byte contenido[] = htmltoPDF.convertir(ERPFactory.despacho.getGuiaDespachoServicio().finObtenerXMLImprimirGuiaDespacho(guiaDespachoDTO).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""), "", "",	parametros,	null);
				this.setShowMessagesBar(Boolean.TRUE);
		        MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantall.despacho.mensaje.impresion.correcta"));
//				UtilitarioWeb.mostrarPDF(contenido);	
			}else {
				this.setShowMessagesBar(Boolean.TRUE);
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
	
	public String formatSendPost(String codAcceso){
		String xml = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ec='http://ec.gob.sr"+
			"<soapenv:Header/>"+
				"<soapenv:Body>"+
					"<ec:autorizacionComprobante>"+
						"<claveAccesoComprobante>"+codAcceso+"</claveAccesoComprobante>"+
					"</ec:autorizacionComprobante>"+
				"</soapenv:Body>"+
			"</soapenv:Envelope>";
		return xml;
	}
	
	public String formatSendPostValidar(String bytesEncodeBase64){
		String xml = "<soapenv:Envelope	xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ec='http://ec.gob.sri.ws.recepcion'>"+
				"<soapenv:Header/>"+
					"<soapenv:Body>"+
						"<ec:validarComprobante>"+
							"<xml>"+bytesEncodeBase64+"</xml>"+
						"</ec:validarComprobante>"+
					"</soapenv:Body>"+
				"</soapenv:Envelope>";
		return xml;
	}
	
	public void getAutorizacion(Document doc) throws XPathExpressionException{
		String pathLevelAutorizacon = "//RespuestaAutorizacionComprobante/autorizaciones/autorizacion[last()]/";
		String pathLevelMensajes = "//RespuestaAutorizacionComprobante/autorizaciones/autorizacion/mensajes[last()]/mensaje/";
		String estado = getLastNode(pathLevelAutorizacon, "estado", doc);         
		if(estado.equals("AUTORIZADO")){
			System.out.println("Estado: " + getLastNode(pathLevelAutorizacon,"estado", doc)+"\n"+"N° Auto: " + 
					getLastNode(pathLevelAutorizacon,"numeroAutorizacion", doc)+"\n"+"Fecha Auto: " + 
					getLastNode(pathLevelAutorizacon,"fechaAutorizacion", doc)+"\n"+"Ambiente: " + 
					getLastNode(pathLevelAutorizacon,"ambiente", doc));
		}else if(estado.equals("NO AUTORIZADO")){
			System.out.println("Estado: " + getLastNode(pathLevelAutorizacon,"estado", doc)+"\n"+"Fecha Auto: " + 
					getLastNode(pathLevelAutorizacon,"fechaAutorizacion", doc)+"\n"+"Ambiente: " + 
					getLastNode(pathLevelAutorizacon,"ambiente", doc)+"\n"+"Identificador: " + 
					getLastNode(pathLevelMensajes,"identificador", doc)+"\n"+"Mensaje: " + 
					getLastNode(pathLevelMensajes,"mensaje", doc)+"\n"+"Tipo: " + 
					getLastNode(pathLevelMensajes,"tipo", doc));
		}
	}
	
	 
	public boolean getRequestSoap(String urlWebServices, String method, String host, String getEncodeXML, Proxy proxy) throws IOException{
		try {
			URL URL = new URL(urlWebServices);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection(proxy);
			con.setDoOutput(true);
			con.setRequestMethod(method);
			con.setRequestProperty("Content‐type", "text/xml; charset=utf‐8");
			con.setRequestProperty("SOAPAction", "");
			con.setRequestProperty("Host", host);       
			OutputStream reqStreamOut = con.getOutputStream();
			reqStreamOut.write(getEncodeXML.getBytes());                                      
			System.out.println(con.getErrorStream());
			java.io.BufferedReader rd = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream(), "UTF8"));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = rd.readLine()) != null){
				sb.append(line);
			}
			//System.out.println(sb.toString());
			Document doc = (Document) XML_Utilidades.convertStringToDocument(sb.toString());
			getAutorizacion(doc);
			con.disconnect();
			return true;
		}catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return false;
	}
	
	public String getLastNode(String pathLevelXML, String nodo, Document doc) throws XPathExpressionException{
		//Ejemplo:
		//RespuestaAutorizacionComprobante/autorizaciones/autorizacion[last()]/estado
		String pathFull = pathLevelXML + nodo;
		XPath xpath = XPathFactory.newInstance().newXPath();
		return xpath.evaluate(pathFull, doc);
	}
	
	public boolean sendPostSoap(String urlWebServices, String method, String host, String getEncodeXML, Proxy proxy){
		try {
			URL oURL = new URL(urlWebServices);
			HttpURLConnection con = (HttpURLConnection) oURL.openConnection(proxy);
			con.setDoOutput(true);
			con.setRequestMethod(method);
			con.setRequestProperty("Content-type", "text/xml; charset=utf-8");
			con.setRequestProperty("SOAPAction", "");
			con.setRequestProperty("Host", host);
			OutputStream reqStreamOut = con.getOutputStream();
			reqStreamOut.write(getEncodeXML.getBytes());
			java.io.BufferedReader rd = new java.io.BufferedReader(new
			java.io.InputStreamReader(con.getInputStream(), "UTF8"));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = rd.readLine()) != null){
				sb.append(line);
			}
			getEstadoPostSoap(XML_Utilidades.convertStringToDocument(sb.toString()), "RespuestaRecepcionComprobante", "estado");//está extrae la data de los nodos en un archivo XML
			con.disconnect();
			return true;
			
		}catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return false;
	}
	
	public boolean getEstadoPostSoap(Document doc, String nodoRaiz, String nodoElemento){
		String estado = XML_Utilidades.getNodes(nodoRaiz, nodoElemento, doc);
		boolean respuesta = Boolean.FALSE;
		if(estado.equals("DEVUELTA")){
			System.out.println("Clave de Accceso: " + XML_Utilidades.getNodes("comprobante","claveAcceso", doc));
			System.out.println("Identificador Error: " + XML_Utilidades.getNodes("mensaje","identificador", doc));
			System.out.println("Descripción Error: " + XML_Utilidades.getNodes("mensaje","mensaje",	doc));
			System.out.println("Descripción Adicional Error: " + XML_Utilidades.getNodes("mensaje","informacionAdicional", doc));
			System.out.println("Tipo mensaje: " + XML_Utilidades.getNodes("mensaje","tipo", doc));
			respuesta = Boolean.FALSE;
		}else if(estado.equals("RECIBIDA")){
			System.out.println("RECIBIDA");
			respuesta = Boolean.TRUE;
		}
		return respuesta;
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
}
