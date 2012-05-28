package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class UtilitiesPrinting extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {        
    
    return new VtiUserExitResult(999, "Re-prints are not configured");
  }
}
