
package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class NonStockItem extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.     
    VtiUserExitScreenField gPrChdField = getScreenField("G_PRCHD");	  
	 
    if (gPrChdField == null)
      return new VtiUserExitResult(999, "Screen Field G_PRCHD does not exist");

	gPrChdField.setFieldValue("X");
	
    return new VtiUserExitResult();
    
  }
}

       
