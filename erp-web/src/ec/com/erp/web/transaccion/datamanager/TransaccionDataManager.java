package ec.com.erp.web.transaccion.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.TransaccionDTO;
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
public class TransaccionDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "transaccionDataManager";

	private TransaccionDTO transaccionDTOEditar;
	 
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "articulosDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public TransaccionDTO getTransaccionDTOEditar() {
		return transaccionDTOEditar;
	}

	public void setTransaccionDTOEditar(TransaccionDTO transaccionDTOEditar) {
		this.transaccionDTOEditar = transaccionDTOEditar;
	}
}
