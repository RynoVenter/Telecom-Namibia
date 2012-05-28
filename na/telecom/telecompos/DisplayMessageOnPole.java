package na.telecom.telecompos;

import java.util.Vector;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitKeyValuePair;
import au.com.skytechnologies.vti.VtiExitPrintTemplateOutput;
import au.com.skytechnologies.vti.VtiUserExit;

public class DisplayMessageOnPole
{
	public static void DisplayMessage(String strMessage)throws VtiExitException
	{
			
		//Clear the price poll
		try
		{
			VtiExitKeyValuePair [] nostVars = new VtiExitKeyValuePair[0];
			VtiExitPrintTemplateOutput clearOutput 
			                                =  VtiUserExit.invokePrintTemplate("PoleReset",nostVars);
			if(clearOutput ==null)
			{
				throw new VtiExitException("Error invoking PoleReset print template");
			}
		}
		catch(VtiExitException e)
		{
			throw new VtiExitException(e.getMessage());
		}
		
		
		
		StringBuffer sbMessage = new StringBuffer();
		Vector v = new Vector();
		sbMessage.append(strMessage);
		
		v.addElement(new VtiExitKeyValuePair("MESSAGE", sbMessage.toString()));
		VtiExitPrintTemplateOutput printOutput = null;

		VtiExitKeyValuePair [] substVars = new VtiExitKeyValuePair[v.size()];
		v.copyInto(substVars);

		//Create the message file
		try
		{
			printOutput =  VtiUserExit.invokePrintTemplate("PoleMessage", substVars);
			if(printOutput==null)
			{
				throw new VtiExitException("Error invoking PoleMessage print template");
			}
		}
		catch(VtiExitException e)
		{
			throw new VtiExitException(e.getMessage());
		}
		

		
	}
}
