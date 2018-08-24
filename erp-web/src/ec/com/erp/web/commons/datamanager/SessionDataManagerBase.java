package ec.com.erp.web.commons.datamanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.richfaces.component.UIDropDownMenu;

import ec.com.erp.cliente.mdl.dto.UsuariosDTO;


/**
 * Estructura que contiene los datos corporativos y de acceso para cada usuario durante toda la sesi?n
 * @author fmunoz
 * @author cbarahona
 *
 */
@SuppressWarnings("serial")
public class SessionDataManagerBase implements Serializable{

	public static final String ID = "sessionDataManagerBase";
	private transient UIDropDownMenu rapidAccessMenu;
	private transient UIDropDownMenu favoriteMenu;
	private byte[] contenidoReporteByte;
	private String contenidoReporteString;
	private UsuariosDTO userDto;
	//Mapa de atributos generico se llena con los parametros enviados desde el sistema externo
	private  Map<String, String> mapInterconexionParameters = new HashMap<String, String>();
	
	//Se agrega la variable para poder mostar u oculatar el boton de regresar al sistema externo, tambien oculta a los botonoes genericos de la barra de menu
	private boolean interconexion = Boolean.FALSE;
	
	private boolean activateJsfLog;
	private Collection<String> timeBaseRequest = new ArrayList<String>(1);
	
	private Boolean configLifeCycle;
	
	private String queryStringUrl;
	
	private String adWinUser;//almacena el usuario del ActiveDirectory
	
	public void clearTimeBaseRequestStack(){
		try {
			getTimeBaseRequest().clear();
		} catch (Exception e) {
		}
	}
	
	/**
	 * @param vistaFuncionarioPerfilOpcionCol the vistaFuncionarioPerfilOpcionCol to set
	 */
	
	/**
	 * @return the rapidAccessMenu
	 */
	public UIDropDownMenu getRapidAccessMenu() {
		return rapidAccessMenu;
	}
	/**
	 * @param rapidAccessMenu the rapidAccessMenu to set
	 */
	public void setRapidAccessMenu(UIDropDownMenu rapidAccessMenu) {
		this.rapidAccessMenu = rapidAccessMenu;
	}

	/**
	 * @return the contenidoReporteByte
	 */
	public byte[] getContenidoReporteByte() {
		return contenidoReporteByte;
	}
	/**
	 * @param contenidoReporteByte the contenidoReporteByte to set
	 */
	public void setContenidoReporteByte(byte[] contenidoReporteByte) {
		this.contenidoReporteByte = contenidoReporteByte;
	}
	/**
	 * @return the contenidoReporteString
	 */
	public String getContenidoReporteString() {
		return contenidoReporteString;
	}
	/**
	 * @param contenidoReporteString the contenidoReporteString to set
	 */
	public void setContenidoReporteString(String contenidoReporteString) {
		this.contenidoReporteString = contenidoReporteString;
	}

	

	/**
	 * @return the favoriteMenu
	 */
	public UIDropDownMenu getFavoriteMenu() {
		return favoriteMenu;
	}
	/**
	 * @param favoriteMenu the favoriteMenu to set
	 */
	public void setFavoriteMenu(UIDropDownMenu favoriteMenu) {
		this.favoriteMenu = favoriteMenu;
	}

	public Boolean getConfigLifeCycle() {
		return configLifeCycle;
	}

	public void setConfigLifeCycle(Boolean configLifeCycle) {
		this.configLifeCycle = configLifeCycle;
	}

	public boolean isActivateJsfLog() {
		return activateJsfLog;
	}

	public void setActivateJsfLog(boolean activateJsfLog) {
		this.activateJsfLog = activateJsfLog;
	}

	public Collection<String> getTimeBaseRequest() {
		return timeBaseRequest;
	}

	public void setTimeBaseRequest(Collection<String> timeBaseRequest) {
		this.timeBaseRequest = timeBaseRequest;
	}

	/**
	 * @return the queryStringUrl
	 */
	public String getQueryStringUrl() {
		return queryStringUrl;
	}


	/**
	 * @param queryStringUrl the queryStringUrl to set
	 */
	public void setQueryStringUrl(String queryStringUrl) {
		this.queryStringUrl = queryStringUrl;
	}


	/**
	 * @return the adWinUser
	 */
	public String getAdWinUser() {
		return adWinUser;
	}


	/**
	 * @param adWinUser the adWinUser to set
	 */
	public void setAdWinUser(String adWinUser) {
		this.adWinUser = adWinUser;
	}


	public Map<String, String> getMapInterconexionParameters() {
		return mapInterconexionParameters;
	}


	public void setMapInterconexionParameters(
			Map<String, String> mapInterconexionParameters) {
		this.mapInterconexionParameters = mapInterconexionParameters;
	}


	public boolean isInterconexion() {
		return interconexion;
	}


	public void setInterconexion(boolean interconexion) {
		this.interconexion = interconexion;
	}

	public UsuariosDTO getUserDto() {
		return userDto;
	}

	public void setUserDto(UsuariosDTO userDto) {
		this.userDto = userDto;
	}
}