package org.biosphere.tissue.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biosphere.tissue.exceptions.CellException;
import org.biosphere.tissue.services.ServiceManager;
import org.biosphere.tissue.exceptions.TissueExceptionHandler;
import org.biosphere.tissue.utils.RequestUtils;

public class ServiceServletStopHandler extends AbstractHandler {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String partnerCell = request.getRemoteHost() + ":" + request.getRemotePort();
		String requestPayload = RequestUtils.getRequestAsString(request.getInputStream());
		getLogger().debug("ServletServiceStopHandler.doPost() Request from: " + partnerCell);
		String responseString = "<h1>ServletServiceStopHandler.doPost()</h1> Cell stop request from: " + partnerCell;
		
		try {
			ServiceManager.stop("SERVLET", requestPayload);
		} catch (CellException e) {
			TissueExceptionHandler.handleGenericException(e, "ServletServiceStopHandler.doPost()",
					"Failed to stop service:");
			responseString = "<h1>ServletServiceStopHandler.doPost()</h1> Service stop request from: " + partnerCell
					+ " Exception: " + e.getMessage();
		}
		response.setContentType(getContentType());
		response.setCharacterEncoding(getContentEncoding());
		response.setContentLength(responseString.getBytes().length);
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(responseString);	
		response.flushBuffer();
	}

}
