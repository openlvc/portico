/*
 *   This file is provided as a copy from the SISO (http://www.sisostds.org) DLC standard for
 *   HLA 1516 (SISO-STD-004.1-2004).
 * 
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *   OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED.  IN NO EVENT SHALL THE DEVELOPERS OF THIS PROJECT OR
 *   ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *   SUCH DAMAGE.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 */
package hla.rti1516.jlc;

import hla.rti1516.RTIinternalError;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.io.FileInputStream;
import java.io.IOException;

public class RtiFactoryFactory
{
	private static final String SETTINGS_FILE = "RTI1516-list.properties";

	public static RtiFactory getRtiFactory( String factoryClassName ) throws RTIinternalError
	{
		try
		{
			Class<?> clazz = Class.forName( factoryClassName );
			return (RtiFactory)clazz.getConstructor().newInstance();
		}
		catch( ClassNotFoundException e )
		{
			throw new RTIinternalError( "Cannot find class " + factoryClassName );
		}
		catch( InstantiationException e )
		{
			throw new RTIinternalError( "Cannot instantiate class " + factoryClassName );
		}
		catch( IllegalAccessException e )
		{
			throw new RTIinternalError( "Cannot access class " + factoryClassName );
		}
		catch( InvocationTargetException e )
		{
			throw new RTIinternalError( "Cannot construct class " + factoryClassName );
		}
		catch( NoSuchMethodException e )
		{
			throw new RTIinternalError( "Cannot get constructor for class " + factoryClassName );
		}
	}

	public static RtiFactory getRtiFactory() throws RTIinternalError
	{
		String userHomeDir = System.getProperty( "user.home" );
		File propertiesFile = new File( userHomeDir, SETTINGS_FILE );
		if( propertiesFile.exists() )
		{
			Properties properties = new Properties();
			try
			{
				InputStream is = new FileInputStream( propertiesFile );
				properties.load( is );
				is.close();
			}
			catch( IOException e )
			{
				throw new RTIinternalError( "Error reading Link Compatibility settings file" );
			}
			String defaultRTI = properties.getProperty( "Default" );
			if( defaultRTI != null )
			{
				String factoryClassName = properties.getProperty( defaultRTI + ".factory" );
				if( factoryClassName == null )
				{
					throw new RTIinternalError( "Cannot find factory class setting for default RTI" );
				}
				return getRtiFactory( factoryClassName );
			}
		}
		// Provide a reasonable default if no setting found
		return getRtiFactory( "org.portico.dlc.HLA1516RTIFactory" );
	}

	public static Map<String,String> getAvailableRtis() throws RTIinternalError
	{
		String userHomeDir = System.getProperty( "user.home" );
		File propertiesFile = new File( userHomeDir, SETTINGS_FILE );
		if( !propertiesFile.exists() )
		{
			throw new RTIinternalError( "Cannot find file " + propertiesFile );
		}
		Properties properties = new Properties();
		try
		{
			InputStream is = new FileInputStream( propertiesFile );
			properties.load( is );
			is.close();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "Error reading Link Compatibility settings file" );
		}
		Map<String,String> map = new HashMap<String,String>();
		int index = 1;
		while( true )
		{
			String rtiName = properties.getProperty( index + ".name" );
			String rtiFactory = properties.getProperty( index + ".factory" );
			if( rtiName == null || rtiFactory == null )
			{
				break;
			}
			map.put( rtiName, rtiFactory );
			index++;
		}
		return map;
	}
}
