package org.biosphere.tissue.handlers;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.biosphere.tissue.exceptions.TissueExceptionHandler;
import org.biosphere.tissue.protocol.ServiceDiscoveryResponse;
import org.biosphere.tissue.services.ServiceManager;
import org.biosphere.tissue.tissue.TissueManager;
import org.eclipse.jetty.server.Request;

public class ServiceNotFoundHandler extends AbstractErrorHandler {

	public ServiceNotFoundHandler() {
		super();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String notHandledRequestID = UUID.randomUUID().toString();
		try {
			String partnerCell = (String) (request.getRemoteHost() + ":" + request.getRemotePort());
			getLogger().debug("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") not handling request (" + request.getRequestURI() + ") from: " + partnerCell);
			String serviceName = ServiceManager.isContextDefined(getCell(), request.getRequestURI());

			if (!(serviceName == null)) {
				getLogger().debug("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") context (" + request.getRequestURI() + ") handled by service (" + serviceName + ") found in DNA");
				if (ServiceManager.isEnabled(getCell(), serviceName)) {
					getLogger().debug("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") service (" + serviceName + ") is enabled in DNA");
					ServiceDiscoveryResponse sdr = ServiceManager.discoverService(serviceName, getCell(), getService(), notHandledRequestID);
					if (sdr.isRunning()) {
						getLogger().debug("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") redirecting to remote cell (" + sdr.getCellName() + ") (" + sdr.getCellNetworkName() + ":"
								+ sdr.getCellServicePort() + ") for service (" + serviceName + ") !");
						redirectToCell(request.getRequestURI(), sdr, response, notHandledRequestID);
					} else {
						getLogger().debug("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") starting service (" + serviceName + ") locally!");
						ServiceManager.start(serviceName, getCell());
						ServiceDiscoveryResponse sdrLocal = new ServiceDiscoveryResponse();
						sdrLocal.setCellNetworkName(getCell().getCellNetworkName());
						sdrLocal.setCellName(getCell().getCellName());
						sdrLocal.setCellServicePort((int) getCell().getDna().getService(serviceName).getParameterValue(TissueManager.TissueServicePortParameter));
						getLogger().debug("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") redirecting to local cell (" + sdrLocal.getCellName() + ") (" + sdrLocal.getCellNetworkName() + ":"
								+ sdrLocal.getCellServicePort() + ") for service (" + serviceName + ") ("+request.getRequestURI()+")!");
						redirectToCell(request.getRequestURI(), sdrLocal, response, notHandledRequestID);
					}
				} else {
					getLogger().debug("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") service (" + serviceName + ") is disabled in DNA, returning HTTP-404");
					return404(request.getRequestURI(), response, notHandledRequestID);
				}
			} else {
				getLogger().warn("ServiceNotFoundHandler.handle(" + notHandledRequestID + ") (" + getService().getName() + ") context (" + request.getRequestURI() + ") not found in DNA! Returning HTTP-404");
				return404(request.getRequestURI(), response, notHandledRequestID);
			}
		} catch (Exception e) {
			return500(request.getRequestURI(), response, notHandledRequestID);
			TissueExceptionHandler.handleGenericException(e, "ServiceNotFoundHandler.handle()", "exception occured:");
		}

	}

	private void return404(String contextPath, HttpServletResponse response, String notHandledRequestID) {
		try {
			getLogger().debug("ServiceNotFoundHandler.return404(" + notHandledRequestID + ") contextPath(" + contextPath + ")  HTTP-404");
			String responseString = "<h1>ServiceNotFoundHandler</h1> HTTP-404 ContextPath: " + contextPath + " not found!";
			response.setContentType(getContentType());
			response.setCharacterEncoding(getContentEncoding());
			response.setContentLength(responseString.getBytes().length);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println(responseString);
			response.flushBuffer();

		} catch (IOException e) {
			TissueExceptionHandler.handleGenericException(e, "ServiceNotFoundHandler.return404()", "Failed to respond to request!");
		}
	}

	private void return500(String contextPath, HttpServletResponse response, String notHandledRequestID) {
		try {
			getLogger().debug("ServiceNotFoundHandler.return500(" + notHandledRequestID + ") contextPath(" + contextPath + ")  HTTP-500");
			String responseString = "<h1>ServiceNotFoundHandler</h1> HTTP-500 ContextPath: " + contextPath + " !";
			response.setContentType(getContentType());
			response.setCharacterEncoding(getContentEncoding());
			response.setContentLength(responseString.getBytes().length);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(responseString);
			response.flushBuffer();

		} catch (IOException e) {
			TissueExceptionHandler.handleGenericException(e, "ServiceNotFoundHandler.return500()", "Failed to respond to request!");
		}
	}

	private void redirectToCell(String contextPath, ServiceDiscoveryResponse sdr, HttpServletResponse response, String notHandledRequestID) {
		try {
			getLogger().debug("ServiceNotFoundHandler.redirectToCell(" + notHandledRequestID + ") contextPath(" + contextPath + ") redirect to (https://" + sdr.getCellNetworkName() + ":" + sdr.getCellServicePort() + contextPath+")");
			response.sendRedirect("https://" + sdr.getCellNetworkName() + ":" + sdr.getCellServicePort() + contextPath);
			response.flushBuffer();
		} catch (IOException e) {
			TissueExceptionHandler.handleGenericException(e, "ServiceNotFoundHandler.redirectToCell()", "Failed to respond to request!");
		}
	}
}
