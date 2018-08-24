package ec.com.erp.web.pedidos.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.PedidoDTO;
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
public class PedidosDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "pedidosDataManager";

	private PedidoDTO pedidoDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "pedidosDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public PedidoDTO getPedidoDTOEditar() {
		return pedidoDTOEditar;
	}

	public void setPedidoDTOEditar(PedidoDTO pedidoDTOEditar) {
		this.pedidoDTOEditar = pedidoDTOEditar;
	}
}
