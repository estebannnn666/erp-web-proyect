package ec.com.erp.web.articulo.datamanager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
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
public class ArticulosDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "articulosDataManager";

	private ArticuloDTO articuloDTOEditar;
	
	private byte[] imagen;
	 
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

	public ArticuloDTO getArticuloDTOEditar() {
		return articuloDTOEditar;
	}

	public void setArticuloDTOEditar(ArticuloDTO articuloDTOEditar) {
		this.articuloDTOEditar = articuloDTOEditar;
	}
	
	/**
	 * Pintar la foto en pantalla
	 * @param stream
	 * @param object
	 * @throws IOException
	 */
	public void paintFotografia(OutputStream stream, Object object) throws IOException {
		if(getFotografia() == null) {
			return;
		}
		stream.write(getFotografia());
		stream.close();
	}
	
	/**
	 * Obtener los bytes de la fotografia
	 * @return
	 */
	public byte[] getFotografia() {
		if(this.imagen == null) {
			return null;
		}
		if(this.imagen == null) {
			return null;
		}
		return this.imagen;
	}
	
	public long getTimeStamp() {
		return System.currentTimeMillis();
	}

	public byte[] getImagen() {
		return imagen;
	}

	public void setImagen(byte[] imagen) {
		this.imagen = imagen;
	}
}
