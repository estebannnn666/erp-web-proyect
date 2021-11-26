package ec.com.erp.web.notascredito.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.NotaCreditoDTO;
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
public class NotasCreditoDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "notasCreditoDataManager";

	private NotaCreditoDTO notaCreditoDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	
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

	public NotaCreditoDTO getNotaCreditoDTOEditar() {
		return notaCreditoDTOEditar;
	}

	public void setNotaCreditoDTOEditar(NotaCreditoDTO notaCreditoDTOEditar) {
		this.notaCreditoDTOEditar = notaCreditoDTOEditar;
	}
}
