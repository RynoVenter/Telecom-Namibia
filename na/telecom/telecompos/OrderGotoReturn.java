package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class OrderGotoReturn extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.          
    VtiUserExitScreenField orderField = getScreenField("ORDER_NO");
    
    if (orderField == null)
      return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");
    
    String orderNo = orderField.getFieldValue();
    
    if (orderNo.length() > 0)
        return new VtiUserExitResult(999, "Clear Current Order First");    
    
    return new VtiUserExitResult(000, "Returns Processing Not Fully Configured");
    
  }
}
