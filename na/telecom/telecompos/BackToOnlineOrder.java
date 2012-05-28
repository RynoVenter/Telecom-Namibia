package na.telecom.telecompos;


import java.text.DecimalFormat;
import java.util.Date;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.*;

public class BackToOnlineOrder extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		String hostName = getHostInterfaceName();
		boolean hostConnected = isHostInterfaceConnected(hostName);
	
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
	

		if(hostConnected)
			sessionHeader.setNextFunctionId("YSPS_ORDER_ONLI");	
		else
			sessionHeader.setNextFunctionId("YSPS_ORDER");	
		
		return new VtiUserExitResult();
	}
}
