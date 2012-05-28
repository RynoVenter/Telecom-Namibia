package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class PrintTest extends VtiUserExit
{
	
	public VtiUserExitResult execute() throws VtiExitException
	{
		ReportBuilderUtilities rU = new ReportBuilderUtilities();
		VerificationUtilities vU = new VerificationUtilities();
		
		vU.getAndCheckScreenField("Items");
		
		String line1 = "Printing";
		String line2 = "Works";
		String lineSeparator = System.getProperty("line.separator");
		String spaces = "                  ";
		StringBuffer feedFiller = new StringBuffer();
		StringBuffer filler = new StringBuffer();
			
		feedFiller.append(lineSeparator);

		

		//String imgLogoPath = VtiExit.getConfigString("PrinterGraphics", "CompanyLogo");
		
		/*byte imgLogoByte [] = ImageFormatter.getImageBytes(imgLogoPath, false,
								   ImageFormatter.ALIGNMENT_LEFT, 2.0d);*/
		
		
		    VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Line1&", line1),
				new VtiExitKeyValuePair("&Line2&",line2),
				new VtiExitKeyValuePair("&BottomEnd&",feedFiller.toString()),
			};
			
			VtiExitKeyValuePair[] keyOpen = 
			{
			};

    try
    {
	  invokePrintTemplate("LogoTemplate", keyOpen);
      invokePrintTemplate("NamibiaTelecoms", keyValuePairs);
	  //invokePrintTemplate("OpenDrawer", keyOpen);
	  rU.cutOpen();
    }
    catch (VtiExitException ee)
    {
    }
			
		return new VtiUserExitResult();
	}
}
