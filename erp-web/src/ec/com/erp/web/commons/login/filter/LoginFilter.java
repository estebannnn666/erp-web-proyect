package ec.com.erp.web.commons.login.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.com.erp.web.commons.datamanager.SessionDataManagerBase;
import ec.com.erp.web.commons.utils.ERPWebResources;


/**
 * Filter checks if LoginBean has loginIn property set to true.
 * If it is not set then request is being redirected to the login.xhml page.
 * 
 * @author itcuties
 *
 */
public class LoginFilter implements Filter {

	public final String ID_SESDATMAN= "sessionDataManagerBase";
	
	/**
	 * Checks if user is logged in. If not it redirects to the login.xhtml page.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// Get the loginBean from session attribute
		/*LoginController loginBean = (LoginController)((HttpServletRequest)request).getSession().getAttribute("loginController");
		
		// For the first application request there is no loginBean in the session so user needs to log in
		// For other requests loginBean is present but we need to check if user has logged in successfully
		/*if (loginBean == null || !loginBean.isLoggedIn()) {
			String contextPath = ((HttpServletRequest)request).getContextPath();
			((HttpServletResponse)response).sendRedirect(contextPath + "/login.xhtml");
			return;
		}
		
		chain.doFilter(request, response);*/
		
		
		
		HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String urlTimeOut=req.getContextPath()+"/sessionTimeOut.jsf";
        SessionDataManagerBase login = (SessionDataManagerBase) req.getSession().getAttribute(ID_SESDATMAN);
        //cuando se cierra el sistema
        if(login==null && req.getParameter("event") != null){
        	if(req.getParameter("event").equals(ERPWebResources.getString("ec.com.erp.etiqueta.evento.logout"))){
            	// Si es una peticion AJAX se debe realizar el redirect por respuesta XML
                if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
                    response.setContentType("text/xml");
                    response.getWriter().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").printf("<partial-response><redirect url=\"%s\"></redirect></partial-response>", urlTimeOut);
                    return;
                }           

        	}
          res.sendRedirect(urlTimeOut);
          return;
        }
        if(req.getRequestURL().toString().endsWith("sessionTimeOut.jsf")){
        	chain.doFilter(request, response);
        	return;
        }
        /**
         * Cuando ingresan solo al contextPath
         */
        if(req.getRequestURL().toString().endsWith(req.getContextPath()+"/")){
        	chain.doFilter(request, response);
        	 return;
        }
        if(req.getRequestURL().toString().contains("/login.jsf")){
        	//validamos que tenga el parametro de token
        		chain.doFilter(request, response);
            	  return;
        }
        if(login == null && !req.getRequestURL().toString().endsWith("login.jsf")){
        	if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
                response.setContentType("text/xml");
                response.getWriter().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").printf("<partial-response><redirect url=\"%s\"></redirect></partial-response>", urlTimeOut);
                return;
            } 
    		res.sendRedirect(urlTimeOut);
        	return;	
        }
        
        if(login != null &&  login.getUserDto() != null){
        	chain.doFilter(request, response);
      	  return;
        }
        /**
         * TODO: esto
         */
        if(req.getRequestURL().toString().endsWith("login.jsf")){
        	chain.doFilter(request, response);
        	  return;
        }
        chain.doFilter(request, response);
	}

	public void init(FilterConfig config) throws ServletException {
		// Nothing to do here!
	}

	public void destroy() {
		// Nothing to do here!
	}	
	
}
