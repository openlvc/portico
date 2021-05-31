package org.portico.utils.classpath;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Wrapper class to expose the protected addURL method in the class URLClassLoader
 */
public class URLSystemClassLoader extends URLClassLoader
{
	public URLSystemClassLoader( URL[] urls, ClassLoader parent )
	{
		super( urls, parent );
	}

	public void addURL( URL url )
	{
		super.addURL( url );
	}
}
