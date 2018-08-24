package ec.com.erp.web.commons.controller;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

/**
 * Agrega tipos de mensaje
 * @author egudino
 *
 */
@SuppressWarnings("serial")
public final class MensajesController implements Serializable{

	
	private MensajesController() {}
	
	/**
	 * Agrega un mensaje informativo
	 * @param keyMessage	- Id del mensaje, este puede ser nulo
	 * @param message		- Contenido del mensaje
	 */
	public static void addInfo(String keyMessage, String message){
    	FacesContext.getCurrentInstance().addMessage(keyMessage, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }
	/**
	 * Agrega un mensaje de advertencia
	 * @param keyMessage	- Id del mensaje, este puede ser nulo
	 * @param message		- Contenido del mensaje
	 */
    public static void addWarn(String keyMessage, String message){
        FacesContext.getCurrentInstance().addMessage(keyMessage, new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
    }
    /**
     * Agrega un mensaje de error
     * @param keyMessage	- Id del mensaje, este puede ser nulo
     * @param message		- Contenido del mensaje
     */
    public static void addError(String keyMessage, String message){
    	//Patron referente a paquetes
    	Pattern validJavaIdentifier = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*");
    	
    	String messageToShow;    	    	    	
    	if(validJavaIdentifier.matcher(message).matches()){//Filtra mensajes no controlados de paquetes
    		messageToShow = "Ha ocurrido un error en la solicitud.";
    	}else{
    		messageToShow = message;
    	}    	
        FacesContext.getCurrentInstance().addMessage(keyMessage, new FacesMessage(FacesMessage.SEVERITY_ERROR, messageToShow, messageToShow));
    }
    /**
     * Agrega un mensaje de error grave
     * @param keyMessage	- Id del mensaje, este puede ser nulo
     * @param message		- Contenido del mensaje
     */
    public static void addFatal(String keyMessage, String message){
        FacesContext.getCurrentInstance().addMessage(keyMessage, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message));
    }
    
    
	/**
	 * Agrega un mensaje informativo a la sesion
	 * @param keyMessage	- Id de la variable de sesion utilizada para guardar el mensaje, esta no puede ser nula
	 * @param message		- Contenido del mensaje
	 */
	public static void addInfoSession(String keyMessage, String message){
		addMessageSession(keyMessage, message, FacesMessage.SEVERITY_INFO);
    }
	/**
	 * Agrega un mensaje de advertencia a la sesion
	 * @param keyMessage	- Id de la variable de sesion utilizada para guardar el mensaje, esta no puede ser nula
	 * @param message		- Contenido del mensaje
	 */
	public static void addWarnSession(String keyMessage, String message){
		addMessageSession(keyMessage, message, FacesMessage.SEVERITY_WARN);
    }
	/**
	 * Agrega un mensaje de error a la sesion
	 * @param keyMessage	- Id de la variable de sesion utilizada para guardar el mensaje, esta no puede ser nula
	 * @param message		- Contenido del mensaje
	 */
	public static void addErrorSession(String keyMessage, String message){
		addMessageSession(keyMessage, message, FacesMessage.SEVERITY_ERROR);
    }
	/**
	 * Agrega un mensaje de error grave a la sesion
	 * @param keyMessage	- Id de la variable de sesion utilizada para guardar el mensaje, esta no puede ser nula
	 * @param message		- Contenido del mensaje
	 */
	public static void addFatalSession(String keyMessage, String message){
		addMessageSession(keyMessage, message, FacesMessage.SEVERITY_FATAL);
    }
	
	private static void addMessageSession(String keyMessage, String message, Severity severity){
		if(keyMessage != null){
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(keyMessage, new FacesMessage(severity, message, message));
		}
	}
	
	/**
	 * Asigna un mensaje al contexto de JSF previamente almacenado en sesion mediante una clave
	 * @param keyMessage
	 */
	public static void viewMessageSession(String keyMessage){
		if(keyMessage != null){
			FacesMessage fm = (FacesMessage)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(keyMessage);
			if(fm != null){
				FacesContext.getCurrentInstance().addMessage(keyMessage, fm);
			}
		}
	}
}