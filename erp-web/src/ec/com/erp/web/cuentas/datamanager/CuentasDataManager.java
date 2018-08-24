package ec.com.erp.web.cuentas.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.FacturaCabeceraDTO;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.datamanager.SessionDataManagerBase;

/**
 * 
 * @author hgudino
 *
 */
@ManagedBean
@SessionScoped
@SuppressWarnings("serial")
public class CuentasDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "cuentasDataManager";

	private FacturaCabeceraDTO facturaCabeceraDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	private String tipoFactura;
	
	@Override
	public String getIdDataManager() {
		return "cuentasDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public String getTipoFactura() {
		return tipoFactura;
	}

	public void setTipoFactura(String tipoFactura) {
		this.tipoFactura = tipoFactura;
	}

	public FacturaCabeceraDTO getFacturaCabeceraDTOEditar() {
		return facturaCabeceraDTOEditar;
	}

	public void setFacturaCabeceraDTOEditar(FacturaCabeceraDTO facturaCabeceraDTOEditar) {
		this.facturaCabeceraDTOEditar = facturaCabeceraDTOEditar;
	}
}
