
package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetNextTR extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.     
    VtiUserExitScreenField VTIField = getScreenField("VTI_REF");	  
	 
    if (VTIField == null)
      return new VtiUserExitResult(999, "Screen Field VTI_REF does not exist");

    long TrNo = VTIField.getIntegerFieldValue();
    
    if (TrNo == 0)
        try
        {
          TrNo = getNextNumberFromNumberRange("YSPS_STO");
        }
        catch (VtiExitException ee)
        {
            return new VtiUserExitResult(999, "Failed to Get STO VTI Ref");
        }
	
        VTIField.setFieldValue(TrNo);

		    
    return new VtiUserExitResult();
    
  }
}

       
