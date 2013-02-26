package org.langke.jetty.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author langke
 * @version 1.0
 */
public abstract class Config {
	private static final Logger log = LoggerFactory.getLogger(Config.class);
	private static final Config CONFIG = new Config() {
		java.util.Properties p = new Properties();
		boolean changed = false;
		private final File cf = new File("conf/main.properties");
		long lmd;

		{
			try {
				if (!cf.exists()) {
					cf.getParentFile().mkdirs();

					cf.createNewFile();
				}
				java.lang.Runtime.getRuntime().addShutdownHook(
						new Thread("store-config") {
							public void run() {
								try {
									if (changed) {
										boolean autoUpdate = p
												.containsKey("autoUpdate");
										if (autoUpdate) {
											FileOutputStream fos = new java.io.FileOutputStream(
													cf);
											p.store(fos,
													"add an <autoUpdate> key to auto update config form default values");
											fos.close();
										}
									}
								} catch (Exception ex) {
									log.warn("store config", ex);
								}
							}
						});

				p.load(new java.io.FileInputStream(cf));
				log.info("loading config from:" + cf.getAbsolutePath());

				lmd = cf.lastModified();

				Thread t = new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								Thread.sleep(60000);
							} catch (InterruptedException e) {
							}
							long newlmd = cf.lastModified();
							if (newlmd > lmd) {
								lmd = newlmd;
								log.info("Config file " + cf.getAbsolutePath()
										+ " is changed,reloading ...");
								try {
									p.load(new java.io.FileInputStream(cf));
								} catch (IOException e) {
									log.error("Error while loading config file: "
											+ cf.getAbsolutePath());
								}
							}
						}
					}
				}, "Config file refresher");
				t.setDaemon(true);
				log.info(t.getName() + "	setDaemon");
				t.start();

			} catch (IOException ex) {
				log.warn("cannot create log file", ex);
			}

		}

		public String get(String k, String defaultValue) {
			String s = p.getProperty(k);
			if (s == null) {

				p.setProperty(k, defaultValue);
				changed = true;

				return defaultValue;
			}

			return s;
		}

		public int getInt(String k, int defaultValue) {
			String s = this.get(k, defaultValue + "");

			try {
				return Integer.parseInt(s);
			} catch (Exception e) {
				return defaultValue;
			}

		}

		public float getFloat(String k, float defaultValue) {
			String s = this.get(k, defaultValue + "");

			try {
				return Float.parseFloat(s);
			} catch (Exception e) {
				return defaultValue;
			}
		}

		public boolean getBoolean(String k, boolean defaultValue) {
			String s = this.get(k, defaultValue + "");
			try {
				return Boolean.parseBoolean(s);
			} catch (Exception e) {
				return defaultValue;
			}

		}

		public boolean setProperty(String key, String value) {
			p.setProperty(key, value);
			try {
				FileOutputStream fos = new java.io.FileOutputStream(cf);
				p.store(fos, "");
				fos.close();
				return true;
			} catch (Exception ex) {
				log.warn("store config", ex);
				return false;
			}
		}

		public String get(String key) {
			return p.getProperty(key);
		}
	};

	private Config() {
	}

	abstract public String get(String k, String defaultValue);

	abstract public int getInt(String k, int defaultValue);

	abstract public float getFloat(String k, float defaultValue);

	abstract public boolean getBoolean(String k, boolean defaultValue);

	abstract public boolean setProperty(String key, String value);

	abstract public String get(String key);

	public static final Config get() {
		return CONFIG;
	}
}
