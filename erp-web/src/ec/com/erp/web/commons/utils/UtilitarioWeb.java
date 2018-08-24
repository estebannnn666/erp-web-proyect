package ec.com.erp.web.commons.utils;

import java.io.IOException;
import java.util.Calendar;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class UtilitarioWeb {

	/**
	 * Metodo para mostrar PDF
	 * @param contenido
	 * @throws IOException
	 */
	public static void mostrarPDF(byte contenido[]) throws IOException
	{
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpServletResponse response = (HttpServletResponse)externalContext.getResponse();
		HttpServletRequest request = (HttpServletRequest)externalContext.getRequest();

		HttpSession session = request.getSession(Boolean.FALSE.booleanValue());
		session.setAttribute("contenidoPDF", contenido);
		response.sendRedirect(request.getContextPath() + "/ImpresionServlet");
	}
	
	/**
	 * Reset de hora
	 * @param fecha
	 */
	public static void cleanDate(Calendar fecha){
		fecha.set(Calendar.HOUR_OF_DAY, 0);
		fecha.set(Calendar.MINUTE, 0);
		fecha.set(Calendar.SECOND, 0);
		fecha.set(Calendar.MILLISECOND, 0);
	}
}
