package com.kanven.record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrapper {

	private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

	public static void main(String[] args) {
		try {
			Server server = new Server();
			Runtime.getRuntime().addShutdownHook(new Thread() {

				@Override
				public void run() {
					try {
						server.stop();
						log.info("the server stop success");
					} catch (Exception e) {
						log.error("the server stop has an error", e);
					}
				}

			});
			server.start();
			log.info("the server start success");
		} catch (Throwable t) {
			log.error("the server has an error", t);
			System.exit(0);
		}
	}

}
