package ec.com.erp.web.commons.utils;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class ImpresionServlet extends HttpServlet
{
	protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
	{
		getServlet(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException
	{
		getServlet(request, response);
	}

	private void getServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		byte[] content  = (byte[])request.getSession().getAttribute("contenidoPDF");
       
		if (content != null) {
			response.setContentType("application/pdf");
			response.setContentLength(content.length);
			response.getOutputStream().write(content);
		}  
		
	}
}