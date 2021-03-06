package org.biosphere.tissue.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biosphere.tissue.blockchain.BlockException;
import org.biosphere.tissue.blockchain.ChainExceptionHandler;
import org.biosphere.tissue.protocol.BlockAddRequest;
import org.biosphere.tissue.protocol.BlockAddResponse;
import org.biosphere.tissue.utils.RequestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ChainAddBlockHandler extends AbstractHandler {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String partnerCell = request.getRemoteHost() + ":" + request.getRemotePort();
			String requestPayload = RequestUtils.getRequestAsString(request.getInputStream());
			getLogger().debug("ChainAddBlockHandler.doPost() Payload to be added to the block:" + requestPayload);
			ObjectMapper mapper = new ObjectMapper();
			BlockAddRequest bare = mapper.readValue(requestPayload.getBytes(),BlockAddRequest.class);
			//Thread.currentThread().setName("ChainAddBlockHandler.doPost()Request("+bare.getRequestID()+")");
			getLogger().debug("ChainAddBlockHandler.doPost() Request ("+bare.getRequestID()+") Cell " + getCell().getCellName() + " request from: " + partnerCell);
			BlockAddResponse bar = getCell().getChain().addBlock(bare);
			bar.setRequestID(bare.getRequestID());
			String responseString = mapper.writeValueAsString(bar);
			response.setContentType(getContentType());
			response.setCharacterEncoding(getContentEncoding());
			response.setContentLength(responseString.getBytes().length);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(responseString);
			response.flushBuffer();
			getLogger().debug("ChainAddBlockHandler.doPost() "+responseString);
		} catch (BlockException e) {
			ChainExceptionHandler.handleGenericException(e, "ChainAddBlockHandler.doPost()", "BlockException:");
		}
	}

}
