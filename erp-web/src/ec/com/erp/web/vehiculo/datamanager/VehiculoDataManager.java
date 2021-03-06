package ec.com.erp.web.vehiculo.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.VehiculoDTO;
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
public class VehiculoDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "vehiculoDataManager";

	private VehiculoDTO vehiculoDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "vehiculoDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public VehiculoDTO getVehiculoDTOEditar() {
		return vehiculoDTOEditar;
	}

	public void setVehiculoDTOEditar(VehiculoDTO vehiculoDTOEditar) {
		this.vehiculoDTOEditar = vehiculoDTOEditar;
	}
}
