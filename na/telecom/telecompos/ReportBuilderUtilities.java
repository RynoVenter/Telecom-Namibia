package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;



public class ReportBuilderUtilities
{

	public VtiUserExitResult openDrawer() throws VtiExitException
	 {
		VtiExitKeyValuePair[] keyOpen = 
			{
			};
		
		    try
			{
				VtiExit.invokePrintTemplate("OpenDrawer", keyOpen);
			}
			
			catch (VtiExitException ee)
			{
			}
			return new VtiUserExitResult();
	 }
	
	
	public VtiUserExitResult cutPaper() throws VtiExitException
	 {
		VtiExitKeyValuePair[] keyOpen = 
			{
			};
		
		    try
			{
				VtiExit.invokePrintTemplate("PaperCut", keyOpen);
			}
			
			catch (VtiExitException ee)
			{
				return new VtiUserExitResult(999, "Cutting the paper failed.");
			}
			return new VtiUserExitResult();
	 }
	
	public VtiUserExitResult cutOpen() throws VtiExitException
	 {
		VtiExitKeyValuePair[] keyOpen = 
			{
			};
		
		    try
			{
				VtiExit.invokePrintTemplate("PaperCut", keyOpen);
				VtiExit.invokePrintTemplate("OpenDrawer", keyOpen);
			}
			
			catch (VtiExitException ee)
			{
				return new VtiUserExitResult(999, "Failed to cut paper and open drawer.");
			}
			return new VtiUserExitResult();
	 }
			
	public VtiUserExitResult printLogo() throws VtiExitException
	 {
		VtiExitKeyValuePair[] keyOpen = 
			{
			};
		
		    try
			{
				VtiExit.invokePrintTemplate("LogoTemplate", keyOpen);
			}
			
			catch (VtiExitException ee)
			{
				return new VtiUserExitResult(999, "Logo failed to print.");
			}
			return new VtiUserExitResult();
	}

	
	
	
}//end
