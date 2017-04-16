package org.biosphere.tissue.blockchain;

import org.biosphere.tissue.utils.TissueLogger;

public class ChainExceptionHandler {
	public ChainExceptionHandler() {
		super();
	}

	public static void handleUnrecoverableGenericException(Exception e, String module, String message)
			throws BlockException {
		BlockException blockException = new BlockException(message, module, e);
		e.printStackTrace();
		throw blockException;
	}

	public static void handleGenericException(Exception e, String module, String message) {
		TissueLogger logger = new TissueLogger();
		logger.exception(module, message + " Exception message(" + e.getMessage() + ")");
		e.printStackTrace();
	}
}
