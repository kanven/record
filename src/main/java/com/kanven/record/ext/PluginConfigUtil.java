package com.kanven.record.ext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.kanven.record.exception.RecordException;
import org.apache.commons.lang3.StringUtils;

/**
 * 插件配置信息加载
 * 
 * @author kanven
 *
 */
public class PluginConfigUtil {

	private static final String PLUGIN_BASE_DIR = "ext";

	private static final ClassLoader LOADER = PluginConfigUtil.class.getClassLoader();

	public static Properties config(String path) {
		if (StringUtils.isBlank(path)) {
			throw new RecordException("the plugin's config file path is null");
		}
		if (!path.startsWith(PLUGIN_BASE_DIR)) {
			path = PLUGIN_BASE_DIR + File.separator + path;
		}
		InputStream input = LOADER.getResourceAsStream(path);
		Properties properties = new Properties();
		try {
			properties.load(input);
		} catch (IOException e) {
			throw new RecordException("the " + path + " config load error", e);
		}
		return properties;
	}

}
