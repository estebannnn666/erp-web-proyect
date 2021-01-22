
package ec.com.erp.web.main.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.UtilitarioWeb;

/**
 * Controlador para pantalla principal
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class ChartBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private BarChartModel barModel;
	private LineChartModel lineModel;
	private String anioActual;
	private Double totalVentas;
	private Double totalCompras;
	private Integer numeroFacturas;
	private Integer cuentasPendientes;
	private Integer numeroClientes;
	private Integer clientesNuevos;
	private BigDecimal inventarioTotal;
	private Integer stockDisponible;
	private Double valorPendienteCobro;
	private Integer numeroFacturasCobrar;

	@PostConstruct
	public void postConstruct() {
		totalVentas = 0.0;
		totalCompras = 0.0;
		numeroFacturas = 0;
		cuentasPendientes = 0;
		numeroClientes = 0;
		clientesNuevos = 0;
		inventarioTotal = BigDecimal.ZERO;
		stockDisponible = 0;
		valorPendienteCobro = 0.0;
		numeroFacturasCobrar = 0;
		// Llamar al metodo para contruir grafico estadistico
		this.crearGraficoEstadistico();
        // Obtener valores estadisticos
		this.obtenerValoresEstadisticos();
		// Obtener numero de clientes
		this.obtenerNumerosClientes();
		// Ontener stock existente
		this.obtenerStockExistente();
	}
	
	/**
	 * Metodo para crear grafico estadistico de barras
	 */
	public void crearGraficoEstadistico(){
		barModel = new BarChartModel();
		String [] menses = new String[]{"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
		// Declarar barras de compras
		ChartSeries compras = new ChartSeries();
		compras.setLabel("Compras");
		barModel.setSeriesColors("FFCC33,00a65a");
		
		// Fecha inferior
		Calendar fechaInferior = Calendar.getInstance();
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		fechaInferior.set(Calendar.MONTH,0);
		// Fecha superior
		Calendar fechaSuperior = Calendar.getInstance();
		Collection<String> tiposDocumentos = new ArrayList<>();
		tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		tiposDocumentos.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA);
		BigDecimal cuentasCobrar = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerComprasVentas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), new Timestamp(fechaInferior.getTime().getTime()), new Timestamp(fechaSuperior.getTime().getTime()), tiposDocumentos, Boolean.FALSE);
		if(cuentasCobrar != null){
			valorPendienteCobro = cuentasCobrar.doubleValue();
		}
		fechaSuperior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaSuperior);
		
		anioActual = ""+fechaInferior.get(Calendar.YEAR);
		BigDecimal resultado = BigDecimal.ZERO;
		for(int i = 0; i < menses.length; i++){
			fechaInferior.set(Calendar.MONTH,i);
			fechaSuperior.set(Calendar.MONTH,i);
			fechaSuperior.add(Calendar.MONTH,1);
			Collection<String> tiposDocumentosCompras = new ArrayList<>();
			tiposDocumentosCompras.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);
			resultado = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerComprasVentas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), new Timestamp(fechaInferior.getTime().getTime()), new Timestamp(fechaSuperior.getTime().getTime()), tiposDocumentosCompras, null);
			if(resultado != null){
				compras.set(menses[i], resultado.doubleValue());
				totalCompras = totalCompras + resultado.doubleValue();
			}else{
				compras.set(menses[i], 0);
			}
		}
		// Ventas
		ChartSeries ventas = new ChartSeries();
        ventas.setLabel("Ventas");
        fechaInferior = Calendar.getInstance();
		fechaSuperior = Calendar.getInstance();
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		fechaSuperior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaSuperior);
		resultado = BigDecimal.ZERO;
		for(int i = 0; i < menses.length; i++){
			fechaInferior.set(Calendar.MONTH,i);
			fechaSuperior.set(Calendar.MONTH,i);
			fechaSuperior.add(Calendar.MONTH,1);
			resultado = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerComprasVentas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), new Timestamp(fechaInferior.getTime().getTime()), new Timestamp(fechaSuperior.getTime().getTime()), tiposDocumentos, null);
			if(resultado != null){
				ventas.set(menses[i], resultado.doubleValue());
				totalVentas = totalVentas + resultado.doubleValue();
			}else{
				ventas.set(menses[i], 0);
			}
		}
        barModel.addSeries(compras);
        barModel.addSeries(ventas);
	}
	
	/**
	 * Valores estadisticos
	 */
	public void obtenerValoresEstadisticos(){
		Collection<String> tipoDocumentoVenta = new ArrayList<>();
		tipoDocumentoVenta.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_VENTAS);
		tipoDocumentoVenta.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_NOTA_VENTA);		
		Collection<String> tipoDocumentoCompra = new ArrayList<>();
		tipoDocumentoCompra.add(ERPConstantes.CODIGO_CATALOGO_VALOR_DOCUMENTO_COMPRAS);		
		Long numeroFacturasConsulta = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerNumeroFacturasComprasVentas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), tipoDocumentoVenta, null);
		numeroFacturas = numeroFacturasConsulta.intValue();
		Long cuentasPendientesConsulta = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerNumeroFacturasComprasVentas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), tipoDocumentoCompra, Boolean.FALSE);
		cuentasPendientes = cuentasPendientesConsulta.intValue();
		Long numeroFacturasCobrarConsulta = ERPFactory.facturas.getFacturaCabeceraServicio().findObtenerNumeroFacturasComprasVentas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), tipoDocumentoVenta, Boolean.FALSE);
		numeroFacturasCobrar = numeroFacturasCobrarConsulta.intValue();
	}
	
	/**
	 * Metodo para obtener lista de clientes
	 */
	public void obtenerNumerosClientes(){
		Long clientes = ERPFactory.clientes.getClientesServicio().findObtenerClientesTodosOFecha(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), null, null);
		numeroClientes = clientes.intValue();
		// Fecha inferior
		Calendar fechaInferior = Calendar.getInstance();
		fechaInferior.set(Calendar.DATE, 1);
		UtilitarioWeb.cleanDate(fechaInferior);
		// Fecha superior
		Calendar fechaSuperior = Calendar.getInstance();
		Long clientesNew = ERPFactory.clientes.getClientesServicio().findObtenerClientesTodosOFecha(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), new Timestamp(fechaInferior.getTime().getTime()), new Timestamp(fechaSuperior.getTime().getTime()));
		clientesNuevos = clientesNew.intValue();
	}
	
	/**
	 * Metodo para obtener stock existente
	 */
	public void obtenerStockExistente(){
		inventarioTotal = ERPFactory.inventario.getInventarioServicio().findObtenerTotalExistencias(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO));
		Long stockDisponibleConsulta = ERPFactory.inventario.getInventarioServicio().findObtenerCantidadTotalEntradas(Integer.parseInt(ERPConstantes.ESTADO_ACTIVO_NUMERICO), Boolean.TRUE);
		stockDisponible = stockDisponibleConsulta.intValue();
	}
	
	public BarChartModel getBarModel() {
		return barModel;
	}

	public void setBarModel(BarChartModel barModel) {
		this.barModel = barModel;
	}
	
	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}
	
	public LineChartModel getLineModel() {
        return lineModel;
    }

	public String getAnioActual() {
		return anioActual;
	}

	public void setAnioActual(String anioActual) {
		this.anioActual = anioActual;
	}

	public Double getTotalVentas() {
		return totalVentas;
	}

	public void setTotalVentas(Double totalVentas) {
		this.totalVentas = totalVentas;
	}

	public Double getTotalCompras() {
		return totalCompras;
	}

	public void setTotalCompras(Double totalCompras) {
		this.totalCompras = totalCompras;
	}

	public Integer getNumeroFacturas() {
		return numeroFacturas;
	}

	public void setNumeroFacturas(Integer numeroFacturas) {
		this.numeroFacturas = numeroFacturas;
	}

	public Integer getCuentasPendientes() {
		return cuentasPendientes;
	}

	public void setCuentasPendientes(Integer cuentasPendientes) {
		this.cuentasPendientes = cuentasPendientes;
	}

	public Integer getNumeroClientes() {
		return numeroClientes;
	}

	public void setNumeroClientes(Integer numeroClientes) {
		this.numeroClientes = numeroClientes;
	}

	public Integer getClientesNuevos() {
		return clientesNuevos;
	}

	public void setClientesNuevos(Integer clientesNuevos) {
		this.clientesNuevos = clientesNuevos;
	}

	public BigDecimal getInventarioTotal() {
		return inventarioTotal;
	}

	public void setInventarioTotal(BigDecimal inventarioTotal) {
		this.inventarioTotal = inventarioTotal;
	}

	public Integer getStockDisponible() {
		return stockDisponible;
	}

	public void setStockDisponible(Integer stockDisponible) {
		this.stockDisponible = stockDisponible;
	}

	public Double getValorPendienteCobro() {
		return valorPendienteCobro;
	}

	public void setValorPendienteCobro(Double valorPendienteCobro) {
		this.valorPendienteCobro = valorPendienteCobro;
	}

	public Integer getNumeroFacturasCobrar() {
		return numeroFacturasCobrar;
	}

	public void setNumeroFacturasCobrar(Integer numeroFacturasCobrar) {
		this.numeroFacturasCobrar = numeroFacturasCobrar;
	}

	public void setLineModel(LineChartModel lineModel) {
		this.lineModel = lineModel;
	}
}
