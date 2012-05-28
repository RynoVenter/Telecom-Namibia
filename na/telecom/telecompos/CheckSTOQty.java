package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckSTOQty extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.          
    VtiUserExitScreenField ATPField = getScreenField("ATP_QTY");
    VtiUserExitScreenField StoreField = getScreenField("STORE_SEL");
    VtiUserExitScreenField FPlantField = getScreenField("PLANT_SEL");
    VtiUserExitScreenField TPlantField = getScreenField("STORE");
	
    
    if (ATPField == null)
      return new VtiUserExitResult(999, "Screen Field ATP_QTY does not exist");
    
    int ATPQty = ATPField.getIntegerFieldValue();
    
    if ( ATPQty == 0)
        return new VtiUserExitResult(999, "Requested Qty must be greater then zero");    
    
    if (StoreField == null)
      return new VtiUserExitResult(999, "Screen Field STORE_SEL does not exist");
    
    String Store = StoreField.getFieldValue();
    
    if (Store.length() == 0)
        return new VtiUserExitResult(999, "Select from which Store");    
	
    if (FPlantField == null)
      return new VtiUserExitResult(999, "Screen Field PLANT_SEL does not exist");
    
    String FPlant = FPlantField.getFieldValue();
	
    if (TPlantField == null)
      return new VtiUserExitResult(999, "Screen Field STORE does not exist");
    
    String TPlant = TPlantField.getFieldValue();	
	
	if (FPlant.equals(TPlant))
      return new VtiUserExitResult(999, "From and To Store may not be the same");
	
    return new VtiUserExitResult();
    
  }
}